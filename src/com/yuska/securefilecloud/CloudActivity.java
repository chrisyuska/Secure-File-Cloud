package com.yuska.securefilecloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
       
        getXML();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        getXML();
    }
    
    private void getXML() {
    	dlg = Toast.makeText(this, "Downloading list...", Toast.LENGTH_SHORT);
    	dlg.show();
    	new FillTask().execute();
    }
    
    private class FillTask extends AsyncTask<Object, Integer, String> {
		protected String doInBackground(Object... objects) {
			return XMLfunctions.getXML();
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

		    try {
		    	URL fileUrl = new URL("http://chrisyuska.com/cse651/download.php?filename=" + options[0].getName());
		    	InputStream in = fileUrl.openStream();
		    	OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));

		    	for (int b; (b = in.read()) != -1;) {
		    		out.write(b);
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
		    }
		}
		protected void onPostExecute(String str) {
			dlg.setText(str);
			dlg.setDuration(Toast.LENGTH_SHORT);
			dlg.show();
		}
	}
}
