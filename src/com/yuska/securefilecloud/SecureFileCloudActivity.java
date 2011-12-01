package com.yuska.securefilecloud;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class SecureFileCloudActivity extends TabActivity {
	public static final String PREFS_NAME = "SecureFileCloudPrefs";
	public static String user;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user = settings.getString("user_preference", "rohit");
        
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
    
    @Override
    protected void onResume() {
        super.onResume();

        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user = settings.getString("user_preference", "rohit");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.settings:
            openSettings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void openSettings() {
        // Create an Intent for the Settings Activity
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
}