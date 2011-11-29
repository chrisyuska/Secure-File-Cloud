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
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class CloudActivity extends ListActivity {
	private FileArrayAdapter adapter;
	private Toast test;
	private Option o;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        fill();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        fill();
    }
    
    private void fill(){
    	List<Option>fls = new ArrayList<Option>();
        
        String xml = XMLfunctions.getXML();
        Document doc = XMLfunctions.XMLfromString(xml);
        
        int numResults = XMLfunctions.numResults(doc);
        
        if((numResults <= 0)){
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
		test = Toast.makeText(this, "Downloading "+o.getName()+"...", Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm download
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Download " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //TODO: Actually download file instead of just showing Toast
		        	   test.show();
		        	   downloadFile();
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
	
	private void downloadFile()
	{

	    File newFile = new File("/sdcard/" + o.getName());

	    // Create directories
	    //new File("/sdcard/").mkdirs();

	    try {
	    	URL fileUrl = new URL("http://chrisyuska.com/cse651/download.php?filename=" + o.getName());
	    	InputStream in = fileUrl.openStream();
	    	OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));

	    	for (int b; (b = in.read()) != -1;) {
	    		out.write(b);
	    	}
	    	out.close();
	    	in.close();
	    	Toast.makeText(this, "Download Complete", Toast.LENGTH_SHORT).show();
	    } catch (MalformedURLException e) {
	    	newFile = null;
	    	Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
	    } catch (IOException e) {
	    	newFile = null;
	    	Toast.makeText(this, "IOException: "+e.getMessage(), Toast.LENGTH_SHORT).show();
	    }
}
	
}
