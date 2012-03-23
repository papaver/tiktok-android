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

        /*
        // [moiz] example of grabbing image container
        //ImageView imageView = (ImageView)findViewById(R.id.background);
        //imageView.setImageResource();

        // [moiz] add the startup process here once the final json package
        //   that will be used is integrated into the sytem
        //    1) startup location services
        //    2) register notifications with server
        //    3) validate guid with server, assign a consumer id to the device
        //    4) sync coupons? or let the deals activity automatically do that?

        final Context context = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    // just wait for a while
                    int waited = 50;
                    while (waited > 0) {
                        sleep(100);
                        --waited;
                    }

                } catch (InterruptedException e) {
                    // nothing to see here

                } finally {

                    // close this activity, will remove it from the stack
                    finish();

                    // create an intent to start the main deals activity
                    Intent dealsActivity = new Intent(context, MainTabActivity.class);
                    startActivity(dealsActivity);
                }
            };
        };

        // start the thread
        thread.start();
        */
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

        //Analytics.passCheckpoint("Startup");

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

        // purge settings
        Settings settings = new Settings(getApplicationContext());
        settings.clearAllSettings();
    }

    //-------------------------------------------------------------------------

    private void runStartupProcess()
    {
        // set user id for analytics
        //Utilities utilities = new Utilities(getApplicationContext());
        //Analytics.setUserId(utilities.getDeviceId());

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

        TikTokApi api       = new TikTokApi(getApplicationContext());
        Utilities utilities = new Utilities(getApplicationContext());

        // generate a new device id
        String deviceId = utilities.getDeviceId();
        if (deviceId == null) {
            deviceId = Device.generateGUID();
        }

        // attempt to register the device
        String consumerId = api.registerDevice(deviceId);
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
            Utilities.displaySimpleAlert(this, title, message);
        }
    }

    //-------------------------------------------------------------------------

    private void validateRegistration()
    {
        Log.i(kLogTag, "validating registration with server...");

        TikTokApi api       = new TikTokApi(getApplicationContext());
        Utilities utilities = new Utilities(getApplicationContext());

        // allow the startup process to continue
        boolean isRegistered = api.validateRegistration();
        if (isRegistered) {
            runStartupProcess();

        // re-run the registration process if server no longer registered
        } else {

            // clean up the existing cached data
            utilities.clearConsumerId();
            utilities.clearDeviceId();

            // re-register with the server
            purgeData();
            registerDevice();
        }
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

