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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);

        // [moiz] this should be split up into verify and register like
        //   the ios app, should be fine for now...
        //   this needs to be threaded as well and should happen on startup
        //   probably not on create...

        // register device id with the server
        registerDevice();

        // register notifications with C2DM server
        registerNotifications();


        // [moiz] example of grabbing image container
        //ImageView imageView = (ImageView)findViewById(R.id.background);
        //imageView.setImageResource();

        // [moiz] add the startup process here once the final json package
        //   that will be used is integrated into the sytem
        //    1) startup location services
        //    2) register notifications with server
        //    3) validate guid with server, assign a consumer id to the device
        //    4) sync coupons? or let the deals activity automatically do that?

        // setup a thread to wait a few minutes and then start the next
        // main deals activity
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

    private void registerDevice()
    {
        Log.w(getClass().getSimpleName(), "registering device id...");

        // [moiz] clean this logic up so it works correctly!

        TikTokApi api       = new TikTokApi(getApplicationContext());
        Utilities utilities = new Utilities(getApplicationContext());

        // check if this is a new device
        String consumerId = utilities.getConsumerId();
        if (consumerId == null) {

            // generate a new device id
            String deviceId = Device.generateGUID();

            // register device id with server
            consumerId = api.registerDevice(deviceId);
            if (consumerId != null) {
                utilities.cacheConsumerId(consumerId);

            // failed to regsiter with server ask user to try again
            } else {
            }

        // check if the registration is valid
        } else {

            boolean isValid = api.validateRegistration();
            if (!isValid) {
                utilities.clearDeviceId();
                utilities.clearConsumerId();

                // try to register the device again
                registerDevice();
            }
        }
    }

    //-------------------------------------------------------------------------

    private void registerNotifications()
    {
        Log.w(getClass().getSimpleName(), "registering with notification server...");

        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app",
            PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", Constants.kPushNotificationAccount);
        startService(registrationIntent);
    }
}

