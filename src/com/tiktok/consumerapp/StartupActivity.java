//-----------------------------------------------------------------------------
// StartupActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Map;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class StartupActivity extends SherlockFragmentActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "StartupActivity";

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

        // load content from xml
        setContentView(R.layout.startup);

        // set the action bar defaults
        ActionBar bar = getSupportActionBar();
        bar.hide();

        // configure progress bar
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        startProgressTimer();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to become visible.
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        // startup up analytics session, register checkpoint
        Analytics.startSession(this);
        Analytics.passCheckpoint("Startup");

        // register device with the server if no consumer id found
        Utilities utilities = new Utilities(getApplicationContext());
        String consumerId = utilities.getConsumerId();
        if (consumerId == null) {
            purgeData();
            registerDevice();
        } else {
            validateRegistration();
        }
    }

    //-------------------------------------------------------------------------

    /**
     * The activity has become visible (it is now "resumed").
     */
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    //-------------------------------------------------------------------------

    /**
     * Another activity is taking focus (this activity is about to be "paused")
     */
    @Override
    protected void onPause()
    {
        super.onPause();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is no longer visible (it is now "stopped")
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Analytics.endSession(this);
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
    // methods
    //-------------------------------------------------------------------------

    private void purgeData()
    {
        // purge the database
        TikTokDatabaseHelper.purgeDatabase(getApplicationContext());

        // purge the icon directory
        IconManager iconManager = new IconManager(getApplicationContext());
        iconManager.deleteAllImages();

        // purge settings
        Settings settings = new Settings(getApplicationContext());
        settings.clearAllSettings();
    }

    //-------------------------------------------------------------------------

    private void runStartupProcess()
    {
        // set user id for analytics
        Utilities utilities = new Utilities(this);
        Analytics.setUserId(utilities.getDeviceId());

        // kick off registration for notifications
        registerNotifications();

        // add location tracking operation
        setupLocationTracking();

        // sync settings
        syncSettings();

        // sync coupons
        syncCoupons();
    }

    //-------------------------------------------------------------------------

    private void registerDevice()
    {
        Log.i(kLogTag, "registering device id...");

        // generate a new device id
        final Utilities utilities = new Utilities(getApplicationContext());
        String currentId          = utilities.getDeviceId();
        final String deviceId     = (currentId == null) ?
            Device.deviceId(this) : currentId;

        // setup the api
        final Context context = this;
        TikTokApi api = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                String consumerId = (String)data;

                // cache info and start registration process
                if (consumerId != null) {
                    utilities.cacheDeviceId(deviceId);
                    utilities.cacheConsumerId(consumerId);
                    runStartupProcess();

                // something went horribly wrong
                } else {
                    Log.e(kLogTag, "registration failed...");
                    String title   = "Registration Error";
                    String message = "Failed to register with the server. Please " +
                                     "try again later.";
                    Utilities.displaySimpleAlert(context, title, message);
                }
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "registration failed...", error);
                String title   = "Registration Error";
                String message = "Failed to register with the server. Please " +
                                "try again later.";
                Utilities.displaySimpleAlert(context, title, message);
            }
        });

        // run the query
        api.registerDevice(deviceId);
    }

    //-------------------------------------------------------------------------

    private void validateRegistration()
    {
        Log.i(kLogTag, "validating registration with server...");

        // setup the api
        final Utilities utilities = new Utilities(getApplicationContext());
        TikTokApi api = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                Boolean isRegistered = (Boolean)data;

                // something was wrong with the registeration
                if (isRegistered == null || !isRegistered) {

                    // clean up the existing cached data
                    utilities.clearConsumerId();
                    utilities.clearDeviceId();

                    // re-register with the server
                    purgeData();
                    registerDevice();

                // looks like we are already registered
                } else {
                    runStartupProcess();
                }
            }

            public void onError(Throwable error) {
                validateRegistration();
            }
        });

        // run the query
        api.validateRegistration();
    }

    //-------------------------------------------------------------------------

    private void registerNotifications()
    {
        Log.i(kLogTag, "registering with notification server...");

        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app",
            PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", Constants.kPushNotificationAccount);
        startService(registrationIntent);
    }

    //-------------------------------------------------------------------------

    private void setupLocationTracking()
    {
        Log.i(kLogTag, "setting up location tracking...");
        startService(new Intent(this, LocationTracker.class));
        LocationTrackerManager.getInstance(this);
    }

    //-------------------------------------------------------------------------

    private void syncSettings()
    {
        // only run sync once ever with the server
        final Settings settings = new Settings(getApplicationContext());
        if (settings.syncedSettings()) return;

        Log.i(kLogTag, "syncing settings with the server...");

        // setup the api
        TikTokApi api = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {

                // update settings from the server
                @SuppressWarnings("unchecked")
                Map<String, ?> serverSettings = (Map<String, ?>)data;
                if (settings != null) {
                    settings.syncToServerSettings(serverSettings);
                    settings.setSyncedSettings(true);
                }
            }

            public void onError(Throwable error) {
            }
        });

        // run the query
        api.syncSettings();
    }

    //-------------------------------------------------------------------------

    private void syncCoupons()
    {
        // close this activity, will remove it from the stack
        finish();

        // create an intent to start the main deals activity
        Intent dealsActivity = new Intent(getApplicationContext(), MainTabActivity.class);
        startActivity(dealsActivity);
    }

    //-------------------------------------------------------------------------

    private void startProgressTimer()
    {
        Runnable timer = new Runnable() {
            public void run() {

                // update progress bar
                ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
                progressBar.incrementProgressBy(1);

                // run timer every second unless expired
                if (progressBar.getProgress() < progressBar.getMax()) {
                    mHandler.postDelayed(this, 10);
                } else {
                    mHandler.removeCallbacks(this);
                }
            }
        };

        // run timer
        mHandler.post(timer);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Handler mHandler = new Handler();
}

