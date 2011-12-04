package com.yuska.securefilecloud;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings Activity for changing user.  Android automates much of this activity in PreferenceActivity.
 * 
 * @author Chris Yuska
 *
 */
public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}