//-----------------------------------------------------------------------------
// TikTokActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokActivity extends SherlockActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "TikTokActivity";

    //-------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub);

        // set the action bar defaults
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // create some fake fragments
        SherlockFragment fragmentA = new FragmentTabStub();

        // setup deals list tab
        ActionBar.Tab tab;
        tab = bar.newTab()
                 .setTag("TikTok")
                 .setText("TikTok")
                 .setTabListener(new TikTokTabListener(fragmentA));
        bar.addTab(tab);
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

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

}

//-----------------------------------------------------------------------------
// TikTokTabListener
//-----------------------------------------------------------------------------

final class TikTokTabListener implements ActionBar.TabListener
{
    public TikTokTabListener(SherlockFragment fragment)
    {
        this.fragment = fragment;
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
        if (transaction != null) {
            transaction.add(R.id.fragment_container, fragment, null);
        }
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
    }

    private SherlockFragment fragment;

}

//-----------------------------------------------------------------------------
// FragmentTabStub
//-----------------------------------------------------------------------------

final class FragmentTabStub extends SherlockFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_stub, container, false);
    }
}

