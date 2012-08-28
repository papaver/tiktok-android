//-----------------------------------------------------------------------------
// MainTabActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import com.ijsbrandslob.appirater.Appirater;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class MainTabActivity extends SherlockFragmentActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "MainTabActivity";

    private static final String kTagCouponList = "list";
    private static final String kTagCouponMap  = "map";
    private static final String kTagKarma      = "karma";
    private static final String kTagSettings   = "settings";
    private static final String kTagCities     = "cities";

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

        // set the action bar defaults
        ActionBar bar = getSupportActionBar();
        bar.setTitle("");
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // setup tabs in tab view
        setupTabs();

        // create appirater instance
        mAppirater = new Appirater(this, new Handler());
        mAppirater.appLaunched(true);
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
        mAppirater.appEnteredForeground(false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflator = getSupportMenuInflater();
        inflator.inflate(R.menu.main, menu);
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return false;
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Log.i(kLogTag, "New intent...");

        // broadcast intent
        Intent newIntent = new Intent();
        newIntent.setAction("com.tiktok.consumer.app.resync");
        sendBroadcast(newIntent);
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FacebookManager.kIntentFacebook) {
                FacebookManager manager = FacebookManager.getInstance(this);
                manager.facebook().authorizeCallback(requestCode, resultCode, data);
            }
        }
    }

    //-------------------------------------------------------------------------
    // activity events
    //-------------------------------------------------------------------------

    /**
     * Create all the tabs for the tab host.
     */
    private void setupTabs()
    {
        ActionBar bar = getSupportActionBar();
        ActionBar.Tab tab;

        // setup deals list tab
        tab = bar.newTab()
                 .setTag(kTagCouponList)
                 .setText("Deals")
                 //.setIcon(R.drawable.icon_tab_couponlist)
                 .setTabListener(new TabListener<CouponListFragment>(this, kTagCouponList, CouponListFragment.class));
        bar.addTab(tab);

        // setup deals map tab
        tab = bar.newTab()
                 .setTag(kTagCouponMap)
                 .setText("Map")
                 //.setIcon(R.drawable.icon_tab_couponmap)
                 .setTabListener(new TabListener</* CouponMapActivity */ FragmentTabStub>(this, kTagCouponMap, FragmentTabStub.class));
        bar.addTab(tab);

        // setup karma tab
        tab = bar.newTab()
                 .setTag(kTagKarma)
                 .setText("Karma")
                 //.setIcon(R.drawable.icon_tab_karma)
                 .setTabListener(new TabListener<KarmaFragment>(this, kTagKarma, KarmaFragment.class));
        bar.addTab(tab);

        // [moiz] no v2.0 support for preferences in framgents at this time,
        //   google you are really pissing me off, wtf do you except devs
        //   to do for fucks sake.  may be possbile to port the preferences
        //   to the actionbar sherlock library and use them through there
        //   but this could turn out to be a major undertaking...
        /*/ setup settings tab
        tab = bar.newTab()
                 .setTag(kTagSettings)
                 .setText("Settings")
                 //.setIcon(R.drawable.icon_tab_settings)
                 .setTabListener(new TabListener<SettingsFragment>(this, kTagSettings, SettingsFragment.class));
        bar.addTab(tab);
        */

        // setup city tab
        tab = bar.newTab()
                 .setTag(kTagCities)
                 .setText("Cities")
                 //.setIcon(R.drawable.icon_tab_cities)
                 .setTabListener(new TabListener<CitiesFragment>(this, kTagCities, CitiesFragment.class));
        bar.addTab(tab);

        // select tab
        bar.setSelectedNavigationItem(0);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    Appirater mAppirater;
}

//-----------------------------------------------------------------------------
// TabListener
//-----------------------------------------------------------------------------

final class TabListener<T extends Fragment> implements ActionBar.TabListener
{

    public TabListener(SherlockFragmentActivity activity, String tag, Class<T> cls)
    {
        mActivity = activity;
        mTag      = tag;
        mClass    = cls;
    }

    //-------------------------------------------------------------------------

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
        // nothing to do without a transaction manager
        if (transaction == null) return;

        // use existing fragment if possible
        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);

        // add/attach the fragment to the activity
        if (mFragment == null) {
            mFragment = SherlockFragment.instantiate(mActivity, mClass.getName());
            transaction.add(R.id.content, mFragment, mTag);
        } else {
            transaction.attach(mFragment);
        }
    }

    //-------------------------------------------------------------------------

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
        // nothing to do without a transaction manager
        if (transaction == null) return;

        // detach the fragment to the activity
        if (mFragment != null) {
            transaction.detach(mFragment);
        }
    }

    //-------------------------------------------------------------------------

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction)
    {
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private       Fragment                 mFragment;
    private final SherlockFragmentActivity mActivity;
    private final String                   mTag;
    private final Class<T>                 mClass;

}
