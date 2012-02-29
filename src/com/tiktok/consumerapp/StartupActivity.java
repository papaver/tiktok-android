//-----------------------------------------------------------------------------
// StartupActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;

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
                    Intent dealsActivity = new Intent(context, TikTokActivity.class);
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
}

