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

/**
 * SecureFileCloudActivity wraps the two tab activities in the application.  It 
 * handles initialization and menu actions.
 * 
 * @author Chris Yuska
 *
 */
public class SecureFileCloudActivity extends TabActivity {
	public static final String PREFS_NAME = "SecureFileCloudPrefs";
	public static String user;
	public static String pass;
	public static int nonce;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user = settings.getString("user_preference", "rohit");
        //TODO: get pass from preferences (along with nonce too)
        pass = "Buckeyes12345678"; 
        nonce = settings.getInt("nonce", 100);
        
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
        //TODO: get pass from preferences (along with nonce too)
        pass = "Buckeyes12345678"; 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//On press of device's menu button, create menu
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection within menu
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