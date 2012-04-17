//-----------------------------------------------------------------------------
// LocationTracker
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class LocationTracker implements LocationListener
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "LocationTracker";

    //-------------------------------------------------------------------------
    // get instance
    //-------------------------------------------------------------------------

    public static LocationTracker getInstance(Context context)
    {
        if (sTracker == null) {
            sTracker = new LocationTracker(context.getApplicationContext());
        }
        return sTracker;
    }

    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    private LocationTracker(Context context)
    {
        mContext           = context;
        mHandler           = new Handler();
    }

    //-------------------------------------------------------------------------
    // LocationListener
    //-------------------------------------------------------------------------

    public void onLocationChanged(Location location)
    {
        Log.i(kLogTag, String.format(
            "Location Changed: %f / %f",
            location.getLatitude(), location.getLongitude()));

        // update the location if it is better than the last one
        if (isBetterLocation(location, mLastKnownLocation)) {
            updateLocation(location);
        }
    }

    //-------------------------------------------------------------------------

    public void onStatusChanged(String provider, int status, Bundle extra)
    {
        Log.i(kLogTag, String.format("Status Changed: %s - %d", provider, status));
    }

    //-------------------------------------------------------------------------

    public void onProviderEnabled(String provider)
    {
        Log.i(kLogTag, String.format("Provider enabled: %s", provider));
    }

    //-------------------------------------------------------------------------

    public void onProviderDisabled(String provider)
    {
        Log.i(kLogTag, String.format("Provider disabled: %s", provider));
    }

    //-------------------------------------------------------------------------
    // location example
    //-------------------------------------------------------------------------

    public void startLocationTracking()
    {
        // grab the global location manager instance
        LocationManager locationManager =
            (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        // setup minimum time (ms) and distance (m) between updates
        long minTime      = 60 * 60 * 1000;
        float minDistance = 500.0f;

        // register the listener with the location manager for both cell/gps
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);

        // get the last known location
        Location gpsLocation =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location wifiLocation =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // use best location
        Location bestLocation = isBetterLocation(gpsLocation, wifiLocation) ?
            gpsLocation : wifiLocation;
        updateLocation(bestLocation);
    }

    //-------------------------------------------------------------------------

    public void stopLocationTracking()
    {
        // grab the global location manager instance
        LocationManager locationManager =
            (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        // unregister the listeners to conserve power
        locationManager.removeUpdates(this);
    }

    //-------------------------------------------------------------------------

    public Location currentLocation()
    {
        return mLastKnownLocation;
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private void updateLocation(Location location)
    {
        if (location == null) return;

        // update location
        mLastKnownLocation = location;

        // send new location to server
        TikTokApi api = new TikTokApi(mContext, mHandler, null);
        api.updateCurrentLocation(location);
    }

    //-------------------------------------------------------------------------

    private boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        long TWO_MINUTES = 2 * 60 * 1000;

        // new location is always better than no location
        if (currentBestLocation == null) {
            return true;
        }

        // old location is always better than no location
        if (location == null) {
            return false;
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

    private static LocationTracker sTracker;

    private Handler  mHandler;
    private Context  mContext;
    private Location mLastKnownLocation;

}
