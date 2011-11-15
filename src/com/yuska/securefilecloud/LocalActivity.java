package com.yuska.securefilecloud;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class LocalActivity extends ListActivity {
	private File currentDir;
	private FileArrayAdapter adapter;
	private Toast test;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDir = new File("/sdcard/");
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
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
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
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(Option o)
    {
		//Just creating Toast for now until we actually upload files
		test = Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT);
		
		//Build alert dialog box to confirm upload
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Upload " + o.getName() + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //TODO: Actually upload file instead of just showing Toast
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
