package com.yuska.securefilecloud;

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
        	
        	fls.add(new Option(XMLfunctions.getValue(e, "name"),XMLfunctions.getValue(e, "size"),XMLfunctions.getValue(e, "location")));
		}
		
		Collections.sort(fls);
		
		adapter = new FileArrayAdapter(CloudActivity.this,R.layout.file_view,fls);
		this.setListAdapter(adapter);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		onFileClick(o);
	}

	private void onFileClick(Option o)
    {
		//Just creating Toast for now until we actually download files
		test = Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm download
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Download " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //TODO: Actually download file instead of just showing Toast
		        	   test.show();
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
}
