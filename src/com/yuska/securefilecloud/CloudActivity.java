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

public class CloudActivity extends ListActivity {
	private FileArrayAdapter adapter;
	private Toast dlg;
	private Option o;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        getXML(SecureFileCloudActivity.user);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        getXML(SecureFileCloudActivity.user);
    }
    
    private void getXML(String user) {
    	dlg = Toast.makeText(this, "Downloading list...", Toast.LENGTH_SHORT);
    	dlg.show();
    	new FillTask().execute(user);
    }
    
    private class FillTask extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... strings) {
			String encrypted = XMLfunctions.getXML(strings[0]);

			MCrypt mcrypt = new MCrypt(SecureFileCloudActivity.pass); //hard coded password right now
			
			try {
				return new String(mcrypt.decrypt(encrypted));
			} catch (Exception e) {
				//TODO: catch exception
				return e.getMessage();
			}
		}
		protected void onPostExecute(String xml) {
			fill(xml);
			//bug: cancel only registers after first time populating for some reason
			dlg.cancel();
		}
    }
    
    private void fill(String xml){
    	List<Option>fls = new ArrayList<Option>();
    	
        Document doc = XMLfunctions.XMLfromString(xml);
        
        try {
		    int numResults = XMLfunctions.numResults(doc);
		    
		    if((numResults <= 0)){
		    	dlg.cancel();
		    	Toast.makeText(this, xml, Toast.LENGTH_LONG).show();
		    }
		            
			NodeList nodes = doc.getElementsByTagName("result");
						
			for (int i = 0; i < nodes.getLength(); i++) {							
				Element e = (Element)nodes.item(i);
		    	
		    	fls.add(new Option(XMLfunctions.getValue(e, "name"),"File Size: "+XMLfunctions.getValue(e, "size")+" bytes",XMLfunctions.getValue(e, "location")));
			}
			
			Collections.sort(fls);
			
			adapter = new FileArrayAdapter(CloudActivity.this,R.layout.file_view,fls);
			this.setListAdapter(adapter);
        } catch (Exception e) {
        	dlg.cancel();
	    	Toast.makeText(this, "There was an error parsing the xml", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		o = adapter.getItem(position);
		onFileClick();
	}

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
		        	   dlg.show();
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
	
	private class DownloadFileTask extends AsyncTask<Option, Integer, String> {
		protected String doInBackground(Option... options) {
			File newFile = new File("/sdcard/" + options[0].getName());
			
			MCrypt mcrypt = new MCrypt(SecureFileCloudActivity.pass);

		    try {
		    	URL fileUrl = new URL("http://chrisyuska.com/cse651/download.php?user="+SecureFileCloudActivity.user+"&filename=" + new String(mcrypt.encrypt(options[0].getName())));
		    	URLConnection urlConnection = fileUrl.openConnection();
		    	
		    	String hash = urlConnection.getHeaderField("digest");
		    	
		    	InputStream in = urlConnection.getInputStream();
		    	OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
		    	
		    	ByteArrayBuffer buf = new ByteArrayBuffer(1);
		    	for (int b; (b = in.read()) != -1;) {
		    		buf.append(b);
		    	}
		    	
		    	String encrypted = new String(buf.toByteArray());
		    	byte[] decrypted = mcrypt.decrypt(encrypted);
		    	
		    	MessageDigest digest = MessageDigest.getInstance("MD5");
		    	digest.update(decrypted);
		    	
		    	String messageDigest = MCrypt.bytesToHex(digest.digest());
		    	
		    	if (messageDigest.compareTo(hash) == 0) {
		    		out.write(decrypted);
		    	} else {
		    		//hash doesn't match; integrity is lost
		    		newFile = null;
		    		return "Error: Hash doesn't match";
		    	}

		    	out.close();
		    	in.close();

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
		protected void onPostExecute(String str) {
			dlg.setText(str);
			dlg.setDuration(Toast.LENGTH_SHORT);
			dlg.show();
		}
	}
}
