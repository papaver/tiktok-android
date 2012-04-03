//-----------------------------------------------------------------------------
// CouponActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.view.View;

import com.google.android.maps.MapActivity;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponActivity extends MapActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "CouponActivity";

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
        setContentView(R.layout.coupon);
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
    // Events
    //-------------------------------------------------------------------------

    public void onClickMerchant(View view)
    {
        Intent intent = new Intent(this, MerchantActivity.class);
        intent.putExtra(MerchantTable.sKeyId, 0);
        startActivityForResult(intent, 0);
    }

    //-------------------------------------------------------------------------

    public void onClickTwitter(View view)
    {
        Log.i(kLogTag, "Share Twitter");
    }

    //-------------------------------------------------------------------------

    public void onClickFacebook(View view)
    {
        Log.i(kLogTag, "Share Facebook");
    }

    //-------------------------------------------------------------------------

    public void onClickMore(View view)
    {
        Log.i(kLogTag, "Share More");
    }

    //-------------------------------------------------------------------------
    // MapsActivity
    //-------------------------------------------------------------------------

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
}

