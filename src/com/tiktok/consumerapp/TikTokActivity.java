//-----------------------------------------------------------------------------
// TikTokActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.provider.Settings.Secure;

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

        // open up the database 
        mDatabaseAdapter = new TikTokDatabaseAdapter(this);
        mDatabaseAdapter.open();

        // fill the listview with data
        mCursor = mDatabaseAdapter.fetchAllCoupons();
        startManagingCursor(mCursor);

        // setup data/ui mapping
        String[] from = new String[] { 
            CouponTable.sKeyTitle, 
            CouponTable.sKeyIconId
        };
        int[] to = new int[] { 
            R.id.coupon_entry_title, 
            R.id.coupon_entry_icon 
        };

        // create a new array adapter and set it to display the row
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
            R.layout.coupon_entry_list_item, mCursor, from, to);

        // update list view to use adapter
        final ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);

        // run sync coupons task
        new SyncCouponsTask(adapter, mDatabaseAdapter).execute();

        // run location services
        setupLocationTracking();

        // run notification services
        setupNotifications();

        String id 
            = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.w(getClass().getSimpleName(), "Android DeviceId = " + id);

        /*
        // setup the list view
        final ListView listView     = (ListView)findViewById(R.id.list);
        final CouponAdapter adapter = new CouponAdapter(this, R.layout.coupon_entry_list_item);
        listView.setAdapter(adapter);

        // populate the list adapter
        for (final Coupon entry : getCoupons()) {
            adapter.add(entry);
        }
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

        // [moiz] check that deviceid and generate guid work as expected
        String guid     = Device.generateGUID();
        String deviceId = Device.deviceId(getBaseContext());
        Log.w(getClass().getSimpleName(), "Android DeviceId = " + deviceId);
        Log.w(getClass().getSimpleName(), "Android GUID = " + guid);
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
        if (mDatabaseAdapter != null) mDatabaseAdapter.close();
        if (mCursor != null) mCursor.close();
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
    // notification example
    //-------------------------------------------------------------------------
        
    public void setupNotifications()
    {
        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app", 
            PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", "papaver@gmail.com");
        startService(registrationIntent);
    }

    //-------------------------------------------------------------------------
    // location example
    //-------------------------------------------------------------------------

    protected void setupLocationTracking()
    {
        // grab the global location manager instance
        LocationManager locationManager = 
            (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        // setup a listener for the location updates
        LocationListener locationListener = new LocationListener() {
            
            public void onLocationChanged(Location location) {
                Log.w(getClass().getSimpleName(), String.format(
                    "Location Changed: %f / %f", 
                    location.getLongitude(), location.getLatitude()));

                // update the location if it is better than the last one
                if (isBetterLocation(mLastKnownLocation, location)) {
                    mLastKnownLocation = location;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extra) {
                Log.w(getClass().getSimpleName(), String.format(
                    "Status Changed: %s - %d", provider, status));
            }

            public void onProviderEnabled(String provider) {
                Log.w(getClass().getSimpleName(), String.format(
                    "Provider enabled: %s", provider));
            }

            public void onProviderDisabled(String provider) {
                Log.w(getClass().getSimpleName(), String.format(
                    "Provider disabled: %s", provider));
            }
        };

        // setup minimum time (ms) and distance (m) between updates
        long minTime      = 60 * 1000; 
        float minDistance = 500.0f;    

        // register the listener with the location manager for both cell/gps
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        //locationManager.requestLocationUpdates(
            //LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);

        // get the last known location
        mLastKnownLocation = 
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //mLastKnownLocation = 
            //locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // unregister the listeners to conserve power
        //locationManager.removeUpdates(locationListener);
    }

    //-------------------------------------------------------------------------
    
    protected boolean isBetterLocation(Location location, Location currentBestLocation)
    {

        long TWO_MINUTES = 2 * 60 * 1000;

        // new location is always better than no location
        if (currentBestLocation == null) {
            return true;
        }

        // check weather the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer              = timeDelta > 0;

        // if its been more than two minutes since the current location use
        // the new location since the user has likely moved, if the new 
        // location is older then two minutes it must be worse
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // check weather the new location fix is more or less accurate
        int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate              = accuracyDelta > 0;
        boolean isMoreAccurate              = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // check weather the location is from the same provider
        boolean isSameProvider = isSameProvider(location.getProvider(), 
            currentBestLocation.getProvider());

        // determine the location quality using a combination of timeliness
        // and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isSameProvider) {
            return true;
        }

        return false;
    }

    //-------------------------------------------------------------------------
    
    private boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null) return provider2 == null;
        return provider1.equals(provider2);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
    
    private TikTokDatabaseAdapter mDatabaseAdapter;
    private Cursor                mCursor;
    private Location              mLastKnownLocation;

}

//-----------------------------------------------------------------------------
// SyncCouponsTask
//-----------------------------------------------------------------------------

class SyncCouponsTask extends AsyncTask<Void, Void, Cursor>
{

    public SyncCouponsTask(SimpleCursorAdapter cursorAdapter, 
                           TikTokDatabaseAdapter databaseAdapter)
    {
        mCursorAdapter   = cursorAdapter;
        mDatabaseAdapter = databaseAdapter;
    }

    @Override
    public void onPreExecute() 
    {
    }

    public Cursor doInBackground(Void... params)
    {
        // create and instance of the tiktok api and grab the available coupons
        TikTokApi api     = new TikTokApi();
        Coupon[] coupons = api.syncActiveCoupons();

        // add only new coupons to the database
        List<Long> currentIds = mDatabaseAdapter.fetchAllCouponIds();
        for (final Coupon coupon : coupons) {
            Log.w(getClass().getSimpleName(), coupon.toString());
            if (!currentIds.contains(coupon.id())) {
                mDatabaseAdapter.createCoupon(coupon);
                Log.w(getClass().getSimpleName(), String.format(
                    "Added coupon to db: %s", coupon.title()));
            }
        }

        // update the cursor in the adapter
        return mDatabaseAdapter.fetchAllCoupons();
    }

    @Override
    public void onPostExecute(Cursor cursor) {
        mCursorAdapter.changeCursor(cursor);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
    
    private SimpleCursorAdapter   mCursorAdapter;
    private TikTokDatabaseAdapter mDatabaseAdapter;
}

