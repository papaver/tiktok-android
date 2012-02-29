//-----------------------------------------------------------------------------
// MainTabActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class MainTabActivity extends TabActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    static final String kTagCouponList = "list";
    static final String kTagCouponMap  = "map";
    static final String kTagKarma      = "karma";
    static final String kTagSettings   = "settings";

    //-------------------------------------------------------------------------
    // activity events
    //-------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // load content from xml
        setContentView(R.layout.main);

        // setup tabs in tab view
        setupTabs();
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
    // activity events
    //-------------------------------------------------------------------------

    /**
     * Create all the tabs for the tab host.
     */
    private void setupTabs()
    {
        // grab resources
        Resources res   = getResources();
        TabHost tabHost = getTabHost();

        Intent intent;
        TabHost.TabSpec spec;

        // setup deals list tab
        intent = new Intent().setClass(this, CouponListActivity.class);
        spec   = tabHost.newTabSpec(kTagCouponList)
                        .setIndicator("Deals", res.getDrawable(R.drawable.icon_tab_couponlist))
                        .setContent(intent);
        tabHost.addTab(spec);

        // setup deals map tab
        intent = new Intent().setClass(this, CouponMapActivity.class);
        spec   = tabHost.newTabSpec(kTagCouponMap)
                        .setIndicator("Map", res.getDrawable(R.drawable.icon_tab_couponmap))
                        .setContent(intent);
        tabHost.addTab(spec);

        // setup karma tab
        intent = new Intent().setClass(this, KarmaActivity.class);
        spec   = tabHost.newTabSpec(kTagKarma)
                        .setIndicator("Karma", res.getDrawable(R.drawable.icon_tab_karma))
                        .setContent(intent);
        tabHost.addTab(spec);

        // setup settings tab
        intent = new Intent().setClass(this, SettingsActivity.class);
        spec   = tabHost.newTabSpec(kTagSettings)
                        .setIndicator("Settings", res.getDrawable(R.drawable.icon_tab_settings))
                        .setContent(intent);
        tabHost.addTab(spec);

        // set the current tab
        tabHost.setCurrentTabByTag(kTagCouponList);
    }
}
