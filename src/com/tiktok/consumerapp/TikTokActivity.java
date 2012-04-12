//-----------------------------------------------------------------------------
// TikTokActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokActivity extends Activity
{

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
    // events
    //-------------------------------------------------------------------------

    public void openMap(View view)
    {
        Intent mapIntent = new Intent(this, TikTokMapActivity.class);
        startActivity(mapIntent);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

}


