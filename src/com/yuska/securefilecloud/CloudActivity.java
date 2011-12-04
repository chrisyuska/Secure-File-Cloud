package com.yuska.securefilecloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * CloudActivity handles all functionality for dealing with cloud files (getting list and downloading).
 * 
 * @author Chris Yuska
 *
 */
public class CloudActivity extends ListActivity {
	private FileArrayAdapter adapter;
	private Toast dlg;
	private Option o;
	
	/**
	 * On activity creation, get file list (from xml feed).
	 */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        //get feed for current user
        getXML(SecureFileCloudActivity.user);
    }
    
    /**
     * On resume, get file list (no different from first load).
     */
    @Override
    protected void onResume() {
        super.onResume();

        //get feed for current user
        getXML(SecureFileCloudActivity.user);
    }
    
    /**
     * Basic function for enabling ability both to call FillTask and show toast. 
     * 
     * @param user user for which to obtain file list for
     */
    private void getXML(String user) {
    	// create and show status text
    	dlg = Toast.makeText(this, "Downloading list...", Toast.LENGTH_SHORT);
    	dlg.show();
    	
    	// Download file list and fill list view (in new thread)
    	new FillTask().execute(user);
    }
    
    /**
     * Class for asynchronously downloading and parsing XML separately from UI.
     * 
     * @author Chris Yuska
     *
     */
    private class FillTask extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... strings) {
			//get encrypted xml feed
			String encrypted = XMLfunctions.getXML(strings[0]);
			
			//create new MCrypt instance based on current user
			MCrypt mcrypt = new MCrypt(SecureFileCloudActivity.pass);
			
			//return decrypted XML feed
			try {
				return new String(mcrypt.decrypt(encrypted));
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		
		//On return from downloading/decrypting xml feed, fill list view with files
		protected void onPostExecute(String xml) {
			fill(xml);
			
			//close downloading... toast dialog
			//TODO: bug: cancel only registers after first time populating for some reason
			dlg.cancel();
		}
    }
    
    /**
     * Fill list view based on xml provided.
     * 
     * @param xml XML feed to parse for files
     */
    private void fill(String xml){
    	List<Option>fls = new ArrayList<Option>();
    	
    	//parse XML feed into XML Document
        Document doc = XMLfunctions.XMLfromString(xml);
        
        try {
		    int numResults = XMLfunctions.numResults(doc);
		    
		    //if no results, just output XML for debugging for now
		    if((numResults <= 0)){
		    	dlg.cancel();
		    	Toast.makeText(this, xml, Toast.LENGTH_LONG).show();
		    }
		    
		    //get all results from Document
			NodeList nodes = doc.getElementsByTagName("result");
			
			//for each result...
			for (int i = 0; i < nodes.getLength(); i++) {
				Element e = (Element)nodes.item(i);
		    	
				//add file listing to file array
		    	fls.add(new Option(XMLfunctions.getValue(e, "name"),"File Size: "+XMLfunctions.getValue(e, "size")+" bytes",XMLfunctions.getValue(e, "location")));
			}
			
			//sort files alphabetically
			Collections.sort(fls);
			
			//set adapter for displaying files
			adapter = new FileArrayAdapter(CloudActivity.this,R.layout.file_view,fls);
			this.setListAdapter(adapter);
        } catch (Exception e) {
        	//catch exception
        	dlg.cancel();
	    	Toast.makeText(this, "There was an error parsing the xml", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * On click of item (file), deal with regular click right now.
     */
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		o = adapter.getItem(position);
		onFileClick();
	}

    /**
     * On short press of file, download file. 
     */
	private void onFileClick()
    {
		//Just creating Toast for now until we actually download files
		dlg.cancel();
		dlg = Toast.makeText(this, "Downloading "+o.getName()+"...", Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm download
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Download " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   //show Toast for showing progress
		        	   dlg.show();
		        	   //start new thread for downloading file
		        	   new DownloadFileTask().execute(o);
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
	
	/**
	 * Asychronously download file in background, separate from UI.
	 * 
	 * @author Chris Yuska
	 *
	 */
	private class DownloadFileTask extends AsyncTask<Option, Integer, String> {
		protected String doInBackground(Option... options) {
			//Create new File for storing data
			File newFile = new File("/sdcard/" + options[0].getName());
			
			//Create instance of MCrypt based on current user's password
			MCrypt mcrypt = new MCrypt(SecureFileCloudActivity.pass);

		    try {
		    	//Start connection...
		    	URL fileUrl = new URL("http://chrisyuska.com/cse651/download.php?user="+SecureFileCloudActivity.user+"&filename=" + new String(mcrypt.encrypt(options[0].getName()))+
		    			"&nonce="+new String(mcrypt.encrypt(SecureFileCloudActivity.nonce)));
		    	URLConnection urlConnection = fileUrl.openConnection();
		    	
		    	//get message digest for comparing later
		    	String hash = urlConnection.getHeaderField("digest");
		    	
		    	//get new nonce
		    	SecureFileCloudActivity.updateNonce(new String(mcrypt.decrypt(urlConnection.getHeaderField("nonce"))));
		    	
		    	//open streams for reading/writing file contents
		    	InputStream in = urlConnection.getInputStream();
		    	OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
		    	
		    	//read file contents into buffer
		    	ByteArrayBuffer buf = new ByteArrayBuffer(1);
		    	for (int b; (b = in.read()) != -1;) {
		    		buf.append(b);
		    	}
		    	
		    	//put file contents into new string from buffer
		    	String encrypted = new String(buf.toByteArray());
		    	
		    	//decrypt file contents into new buffer
		    	byte[] decrypted = mcrypt.decrypt(encrypted);
		    	
		    	//get message digest of decrypted file contents
		    	MessageDigest digest = MessageDigest.getInstance("MD5");
		    	digest.update(decrypted);
		    	
		    	String messageDigest = MCrypt.bytesToHex(digest.digest());
		    	
		    	//if message digests match, then integrity kept. store file contents
		    	if (messageDigest.compareTo(hash) == 0) {
		    		out.write(decrypted);
		    	} else {
		    		//otherwise, hash doesn't match; integrity is lost
		    		newFile = null;
		    		return "Error: Hash doesn't match";
		    	}

		    	//close connections
		    	out.close();
		    	in.close();

		    	//return download status
		    	return "Download Complete";
		    } catch (MalformedURLException e) {
		    	newFile = null;
		    	return "Error: "+e.getMessage();
		    } catch (IOException e) {
		    	newFile = null;
		    	return "IOException: "+e.getMessage();
		    } catch (Exception e) {
		    	//Handle mcrypt exception
		    	newFile = null;
		    	return "Encryption exception: "+e.getMessage();
		    }
		}
		
		//on finish of download, deal with UI
		protected void onPostExecute(String str) {
			//set new toast text to download status
			dlg.setText(str);
			
			//set new duration to display
			dlg.setDuration(Toast.LENGTH_SHORT);
			
			//show new toast dialog to user
			dlg.show();
		}
	}
}
