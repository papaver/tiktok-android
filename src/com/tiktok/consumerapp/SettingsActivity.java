//-----------------------------------------------------------------------------
// SettingsActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class SettingsActivity extends    PreferenceActivity
                              implements OnSharedPreferenceChangeListener
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    static final String kLogTag = "SettingsActivity";

    //-------------------------------------------------------------------------
    // activity
    //-------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // update the summaries
        mSettings = new Settings(this);

        // name
        if (!mSettings.name().equals("")) {
            EditTextPreference textPreference =
                (EditTextPreference)getPreferenceScreen().findPreference(Settings.kTagName);
            textPreference.setSummary(mSettings.name());
        }

        // email
        if (!mSettings.email().equals("")) {
            EditTextPreference textPreference =
                (EditTextPreference)getPreferenceScreen().findPreference(Settings.kTagEmail);
            textPreference.setSummary(mSettings.email());
        }

        // gender
        if (!mSettings.gender().equals("")) {
            ListPreference textPreference =
                (ListPreference)getPreferenceScreen().findPreference(Settings.kTagGender);
            textPreference.setSummary(mSettings.gender());
        }
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to become visible.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity has become visible (it is now "resumed").
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        // set up a listener whenever a key changes
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    //-------------------------------------------------------------------------

    /**
     * Another activity is taking focus (this activity is about to be "paused")
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        // unregister the listener whenever a key changes
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is no longer visible (it is now "stopped")
     */
    @Override
    protected void onStop()
    {
        super.onStop();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to be destroyed.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //-------------------------------------------------------------------------
    // shared preferences listener
    //-------------------------------------------------------------------------

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        Log.i(kLogTag, String.format("Updated setting %s", key));

        // update summary for text fields
        if (key.equals(Settings.kTagName) || key.equals(Settings.kTagEmail)) {
            EditTextPreference textPreference =
                (EditTextPreference)getPreferenceScreen().findPreference(key);
            textPreference.setSummary(sharedPreferences.getString(key, ""));
        }

        // update summary for list fields
        if (key.equals(Settings.kTagGender)) {
            ListPreference listPreference =
                (ListPreference)getPreferenceScreen().findPreference(key);
            listPreference.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Settings mSettings;
}

