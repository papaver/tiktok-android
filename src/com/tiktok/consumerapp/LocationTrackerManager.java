//-----------------------------------------------------------------------------
// LocationTrackerManager
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class LocationTrackerManager
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "LocationTrackerManager";

    //-------------------------------------------------------------------------

    private static LocationTrackerManager sLocationTrackerManager;

    //-------------------------------------------------------------------------
    // get instance
    //-------------------------------------------------------------------------

    public static LocationTrackerManager getInstance(Context context)
    {
        if (sLocationTrackerManager == null) {
            sLocationTrackerManager =
                new LocationTrackerManager(context.getApplicationContext());
        }
        return sLocationTrackerManager;
    }

    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    public LocationTrackerManager(Context context)
    {
        mContext = context;
        bindLocationService(context);
    }

    //-------------------------------------------------------------------------

    protected void finalize()
    {
        unbindLocationService(mContext);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public Location currentLocation()
    {
        if (mLocationService != null) {
            return mLocationService.currentLocation();
        } else {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private void bindLocationService(Context context)
    {
        Intent intent = new Intent(context, LocationTracker.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    //-------------------------------------------------------------------------

    private void unbindLocationService(Context context)
    {
        if (mIsBound) {
            context.unbindService(mConnection);
            mIsBound = false;
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private boolean         mIsBound;
    private Context         mContext;
    private LocationTracker mLocationService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(kLogTag, "LocationTracker service connected.");
            mLocationService =
                ((LocationTracker.LocationTrackerBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
            Log.i(kLogTag, "LocationTracker service disconected.");
            mLocationService = null;
        }
    };

}
