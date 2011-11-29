package com.yuska.securefilecloud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class LocalActivity extends ListActivity {
	private File currentDir;
	private FileArrayAdapter adapter;
	private Option o;
	private Toast dlg;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDir = new File("/sdcard/");
        fill(currentDir);
    }
	
	@Override
    protected void onResume() {
        super.onResume();

        fill(currentDir);
    }
	
	/*
	 * Fill list with list of local files (and sort).
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		} else {
			onFileClick();
		}
	}

	private void onFileClick()
    {
		dlg = Toast.makeText(this, "Uploading "+o.getName()+"...", Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm upload
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Upload " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   
		           public void onClick(DialogInterface dialog, int id) {
		        	   dlg.show();
		        	   //uploadFile();
		        	   new UploadFileTask().execute(o);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		
		//Show alert dialog
		alert.show();
    }
	
	private String uploadFile(Option option)
	{
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
	   
		String pathToOurFile = option.getPath(); // Is this the relative or absolute path??
		String urlServer = "http://chrisyuska.com/cse651/upload.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
	
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;
	
		try
		{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
	
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
		
			// Enable POST method
			connection.setRequestMethod("POST");
		
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	
			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + option.getName() +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);
	
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
	
			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
	
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
			/* Responses from the server (code and message)
	   		serverResponseCode = connection.getResponseCode();
	   		serverResponseMessage = connection.getResponseMessage();
			*/
	
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		}
		catch (Exception ex) {
			//Exception handling
			return "Exception: "+ex.getMessage();
	   	}
		try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line, out = "";
            while ((line = rd.readLine()) != null) {
            	out+=line;
            }
            rd.close();
            return out;

        } catch (IOException ex) {
        	return "IO Exception: "+ex.getMessage();
        }
	}
	
	private class UploadFileTask extends AsyncTask<Option, Integer, String> {
		protected String doInBackground(Option... options) {
			return uploadFile(options[0]);
		}
		protected void onPostExecute(String str) {
			dlg.setText(str);
			dlg.setDuration(Toast.LENGTH_SHORT);
			dlg.show();
		}
	}
}
