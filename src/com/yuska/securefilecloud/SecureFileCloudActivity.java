package com.yuska.securefilecloud;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class SecureFileCloudActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, LocalActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("localFiles").setIndicator("Local Files").setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, CloudActivity.class);
        spec = tabHost.newTabSpec("cloudFiles").setIndicator("Cloud Files").setContent(intent);
        tabHost.addTab(spec);
    }
}