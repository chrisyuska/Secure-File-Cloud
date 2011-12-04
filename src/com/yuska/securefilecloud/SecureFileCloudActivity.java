package com.yuska.securefilecloud;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.TabActivity;
import android.content.Context;
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
	public static String nonce;
	private static Context context;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user = settings.getString("user_preference", "rohit");
        
        //TODO: get pass from preferences, should be separated by user
        pass = "Buckeyes12345678";
        
        //initial nonce
        String hash = "0123456789abcdef";
        
        //get initial nonce value (hash of current hash)
        //TODO: should be threaded, separated by user
        try {
        	MessageDigest digest = MessageDigest.getInstance("MD5");
        	digest.update(hash.getBytes());
        	hash = MCrypt.bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
        	//Catch exception
        }
        
        nonce = settings.getString("nonce", hash);
        
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
        
        context = this;

        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user = settings.getString("user_preference", "rohit");
        //TODO: get pass from preferences (along with nonce too)
        pass = "Buckeyes12345678";
        
        //initial nonce
        String hash = "0123456789abcdef";
        
        //get initial nonce value (hash of current hash)
        //TODO: should be threaded, separated by user
        try {
        	MessageDigest digest = MessageDigest.getInstance("MD5");
        	digest.update(hash.getBytes());
        	hash = MCrypt.bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
        	//Catch exception
        }
        nonce = settings.getString("nonce", hash);
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
    
    public static void updateNonce(String hash) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("nonce", hash);
    	
    	nonce = settings.getString("nonce", hash);
    }
}