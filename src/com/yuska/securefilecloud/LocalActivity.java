package com.yuska.securefilecloud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * LocalActivity contains all functionality for dealing with local files (uploading to server).
 * 
 * @author Chris Yuska
 *
 */
public class LocalActivity extends ListActivity {
	private File currentDir;
	private FileArrayAdapter adapter;
	private Option o;
	private Toast dlg;
	
	/**
	 * On activity creation, set current directory to root of sdcard and fill list view with contained files.
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDir = new File("/sdcard/");
        fill(currentDir);
    }
	
	/**
	 * On resume, just file list view with contained files (stay in previous directory).
	 */
	@Override
    protected void onResume() {
        super.onResume();

        fill(currentDir);
    }
	
	/**
	 * Fill list with list of local files and sort.
	 */
	private void fill(File f)
    {	
        File[]dirs = f.listFiles();
         this.setTitle("Current Dir: "+f.getName());
         List<Option>dir = new ArrayList<Option>();
         List<Option>fls = new ArrayList<Option>();
         try{
             for(File ff: dirs)
             {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length()+" bytes",ff.getAbsolutePath()));
                }
             }
         }catch(Exception e)
         {
             
         }
         Collections.sort(dir);
         Collections.sort(fls);
         dir.addAll(fls);
         if(!f.getName().equalsIgnoreCase("sdcard"))
             dir.add(0,new Option("..","Parent Directory",f.getParent()));
         
         adapter = new FileArrayAdapter(LocalActivity.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter);
    }

	/**
	 * Deal with item (file or directory) once clicked.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		o = adapter.getItem(position);
		
		//if item clicked is a directory, move to new directory and fill list view. 
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		} else {
			//otherwise, deal with file.
			onFileClick();
		}
	}

	/**
	 * File has been clicked, now deal with it.
	 */
	private void onFileClick()
    {
		//build Toast dialog for potential use.
		dlg = Toast.makeText(this, "Uploading "+o.getName()+"...", Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm upload
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Upload " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   
		    	   //if clicked "Yes", then upload file
		           public void onClick(DialogInterface dialog, int id) {
		        	   //flash Toast dialog indicating uploading is now in progress
		        	   dlg.show();
		        	   //upload file (in new thread)
		        	   new UploadFileTask().execute(o);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		
		//show confirmation dialog
		alert.show();
    }
	
	/**
	 * Class for asynchronously uploading in new thread.
	 * 
	 * @author Chris Yuska
	 *
	 */
	private class UploadFileTask extends AsyncTask<Option, Integer, String> {
		//upload file provided in passed parameter
		protected String doInBackground(Option... options) {
			return uploadFile(options[0]);
		}
		
		//on finish of upload, deal with UI changes
		protected void onPostExecute(String str) {
			//Set Toast dialog text
			dlg.setText(str);
			//Set new duration time for message
			dlg.setDuration(Toast.LENGTH_SHORT);
			//show toast dialog
			dlg.show();
		}
	}
	
	/**
	 * Upload file to web server.
	 * 
	 * @param option file to be uploaded
	 * @return status message of upload (Success or error message)
	 */
	private String uploadFile(Option option)
	{
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
	   
		//populate data needed to create url connection and form
		String pathToOurFile = option.getPath();
		String urlServer = "http://chrisyuska.com/cse651/upload.php?user="+SecureFileCloudActivity.user;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
	
		//Create new mcrypt instance based on current user's password
		MCrypt mcrypt = new MCrypt(SecureFileCloudActivity.pass);
		
		try
		{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
	
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
		
			// Enable POST method and specify form content type
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			//read data content into buffer
			ByteArrayBuffer buf = new ByteArrayBuffer(1);
	    	for (int b; (b = fileInputStream.read()) != -1;) {
	    		buf.append(b);
	    	}
	    	
	    	//create message digest based on buffer contents (file contents)
	    	MessageDigest digest = MessageDigest.getInstance("MD5");
	    	digest.update(buf.toByteArray());
	    	String messageDigest = MCrypt.bytesToHex(digest.digest());
	    	
	    	//add message digest to form headers
	    	connection.setRequestProperty("digest", messageDigest);
			
	    	//write file-portion of http form
			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + new String(mcrypt.encrypt(option.getName())) +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			//output file contents (encrypted)
	    	try {
	    		outputStream.writeBytes(new String(mcrypt.encrypt(new String(buf.toByteArray()))));
	    	} catch (Exception e) {
	    		//Catch encryption exception
	    	}
			
	    	//finish file-portion of http form
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
			//finish transmission to server
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		}
		catch (Exception ex) {
			//Exception handling
			return "Exception: "+ex.getMessage();
	   	}
		
		//Get response message from server
		try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line, out = "";
            while ((line = rd.readLine()) != null) {
            	out+=line;
            }
            rd.close();
            
            //decrypt and return message from server
            return new String(mcrypt.decrypt(out));

        } catch (IOException ex) {
        	return "IO Exception: "+ex.getMessage();
        } catch (Exception e) {
        	//Catch encryption error
        	return "Encryption error: "+e.getMessage();
        }
	}
}
