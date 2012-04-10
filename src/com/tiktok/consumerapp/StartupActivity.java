//-----------------------------------------------------------------------------
// StartupActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class StartupActivity extends Activity
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
        setContentView(R.layout.startup);
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to become visible.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Analytics.startSession(this);
    }

    //-------------------------------------------------------------------------

    /**
     * The activity has become visible (it is now "resumed").
     */
    @Override
    protected void onResume()
    {
        super.onResume();

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
            Device.generateGUID() : currentId;

        // setup the api
        final Context context = this;
        TikTokApi api = new TikTokApi(this, new Handler(), new TikTokApi.CompletionHandler() {

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
        TikTokApi api = new TikTokApi(this, new Handler(), new TikTokApi.CompletionHandler() {

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

        LocationTracker tracker = LocationTracker.getInstance(this);
        tracker.startLocationTracking();
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
}

