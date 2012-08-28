//-----------------------------------------------------------------------------
// SettingsActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

@SuppressWarnings("deprecation")
public class SettingsActivity extends    SherlockPreferenceActivity
                              implements OnSharedPreferenceChangeListener,
                                         OnPreferenceChangeListener
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    static final String kLogTag             = "SettingsActivity";
    static final String kFBConnectedSummary = "Connected.";
    static final String kFBDefaultSummary   = "Log into Facebook to receive customized deals!";

    static final int kIntentWorkLocation = 100;
    static final int kIntentHomeLocation = 101;

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

        final Context context = this;

        // setup action bar
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Settings");
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        // update the summaries
        mSettings = new Settings(this, true);

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

        // twitter handle
        if (!mSettings.twitterHandle().equals("")) {
            EditTextPreference textPreference =
                (EditTextPreference)getPreferenceScreen().findPreference(Settings.kTagTwitterHandle);
            textPreference.setSummary(mSettings.twitterHandle());
        }

        // phone
        if (!mSettings.phone().equals("")) {
            EditTextPreference textPreference =
                (EditTextPreference)getPreferenceScreen().findPreference(Settings.kTagPhone);
            textPreference.setSummary(mSettings.phone());
        }

        // gender
        if (!mSettings.gender().equals("")) {
            ListPreference textPreference =
                (ListPreference)getPreferenceScreen().findPreference(Settings.kTagGender);
            textPreference.setSummary(mSettings.gender());
        }

        // add listener to deal with facebook connect and make sure its in sync
        FacebookManager manager            = FacebookManager.getInstance(this);
        CheckBoxPreference facebookConnect = (CheckBoxPreference)findPreference("TTS_fb");
        facebookConnect.setOnPreferenceChangeListener(this);
        if (manager.facebook().isSessionValid() && !facebookConnect.isChecked()) {
            facebookConnect.setChecked(true);
        }
        if (facebookConnect.isChecked()) {
            facebookConnect.setSummary(kFBConnectedSummary);
        }

        // home location
        Preference homePreference = (Preference)findPreference("TTS_home");
        homePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Location home = mSettings.home();
                Intent intent = new Intent(context, LocationPickerActivity.class);
                if (home != null) {
                    intent.putExtra(LocationPickerActivity.kIntentExtraLatitude,
                        Double.toString(home.getLatitude()));
                    intent.putExtra(LocationPickerActivity.kIntentExtraLongitude,
                        Double.toString(home.getLongitude()));
                }
                startActivityForResult(intent, kIntentHomeLocation);
                return true;
            }
        });
        if (!mSettings.homeLocality().equals("")) {
            homePreference.setSummary(mSettings.homeLocality());
        }

        // work location
        Preference workPreference = (Preference)findPreference("TTS_work");
        workPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Location work = mSettings.work();
                Intent intent = new Intent(context, LocationPickerActivity.class);
                if (work != null) {
                    intent.putExtra(LocationPickerActivity.kIntentExtraLatitude,
                        Double.toString(work.getLatitude()));
                    intent.putExtra(LocationPickerActivity.kIntentExtraLongitude,
                        Double.toString(work.getLongitude()));
                }
                startActivityForResult(intent, kIntentWorkLocation);
                return true;
            }
        });
        if (!mSettings.workLocality().equals("")) {
            workPreference.setSummary(mSettings.workLocality());
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
        mSettings.close();
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    //-------------------------------------------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == kIntentHomeLocation) {
                saveHomeLocation(data);
            } else if (requestCode == kIntentWorkLocation) {
                saveWorkLocation(data);
            } else if (requestCode == FacebookManager.kIntentFacebook) {
                FacebookManager manager = FacebookManager.getInstance(this);
                manager.facebook().authorizeCallback(requestCode, resultCode, data);
            }
        }
    }

    //-------------------------------------------------------------------------
    // shared preferences listener
    //-------------------------------------------------------------------------

    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        final Boolean connected                     = (Boolean)newValue;
        final CheckBoxPreference checkboxPreference = (CheckBoxPreference)preference;

        // track weather to allow change to occur
        Boolean result = false;

        // log into facebook
        FacebookManager manager = FacebookManager.getInstance(this);
        if (connected) {

            // attempt to authorize session
            manager.authorize(this, new FacebookManager.CompletionHandler() {

                public void onSuccess(Bundle values) {
                    Log.i(kLogTag, "Logged into facebook!");
                    checkboxPreference.setChecked(true);
                    checkboxPreference.setSummary(kFBConnectedSummary);
                }

                public void onError(Throwable error) {
                    checkboxPreference.setSummary(kFBDefaultSummary);
                }

                public void onCancel() {
                    checkboxPreference.setSummary(kFBDefaultSummary);
                }

            });

        // log out of facebook
        } else {
            Log.i(kLogTag, "Logged out of facebook!");
            manager.logout(this, null);
            checkboxPreference.setSummary(kFBDefaultSummary);
            result = true;
        }

        return result;
    }

    //-------------------------------------------------------------------------

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          String key)
    {
        Log.i(kLogTag, String.format("Updated setting %s", key));

        // update summary for text fields
        if (key.equals(Settings.kTagName) || key.equals(Settings.kTagEmail) ||
            key.equals(Settings.kTagTwitterHandle) || key.equals(Settings.kTagPhone)) {
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

    private void saveHomeLocation(Intent data)
    {
        String address   = data.getStringExtra(LocationPickerActivity.kIntentExtraAddress);
        String latitude  = data.getStringExtra(LocationPickerActivity.kIntentExtraLatitude);
        String longitude = data.getStringExtra(LocationPickerActivity.kIntentExtraLongitude);
        Log.i(kLogTag, String.format("Home Location: %s %s %s", latitude, longitude, address));

        // update settings
        Location home = new Location("");
        home.setLatitude(Double.valueOf(latitude).doubleValue());
        home.setLongitude(Double.valueOf(longitude).doubleValue());
        mSettings.setHome(home);
        mSettings.setHomeLocality(address);

        // update summary
        Preference preference = findPreference("TTS_home");
        preference.setSummary(address);
    }

    //-------------------------------------------------------------------------

    private void saveWorkLocation(Intent data)
    {
        String address   = data.getStringExtra(LocationPickerActivity.kIntentExtraAddress);
        String latitude  = data.getStringExtra(LocationPickerActivity.kIntentExtraLatitude);
        String longitude = data.getStringExtra(LocationPickerActivity.kIntentExtraLongitude);
        Log.i(kLogTag, String.format("Work Location: %s %s %s", latitude, longitude, address));

        // update settings
        Location work = new Location("");
        work.setLatitude(Double.valueOf(latitude).doubleValue());
        work.setLongitude(Double.valueOf(longitude).doubleValue());
        mSettings.setWork(work);
        mSettings.setWorkLocality(address);

        // update summary
        Preference preference = findPreference("TTS_work");
        preference.setSummary(address);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Settings mSettings;

}
