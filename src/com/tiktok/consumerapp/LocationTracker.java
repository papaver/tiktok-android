//-----------------------------------------------------------------------------
// LocationTracker
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.text.DateFormat;
import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class LocationTracker extends Service implements LocationListener
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
       return null;
    }

    //-------------------------------------------------------------------------
    // Service
    //-------------------------------------------------------------------------

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    //-------------------------------------------------------------------------

    @Override
    public void onCreate()
    {
        startLocationTracking();
    }

    //-------------------------------------------------------------------------

    @Override
    public void onDestroy()
    {
        stopLocationTracking();
    }

    //-------------------------------------------------------------------------

    @Override
    public void onStart(Intent intent, int startid)
    {
    }

    //-------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int start_id)
    {
        return START_STICKY;
    }

    //-------------------------------------------------------------------------
    // LocationListener
    //-------------------------------------------------------------------------

    public void onLocationChanged(Location location)
    {
        log(String.format(
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
        log(String.format("Status Changed: %s - %d", provider, status));
    }

    //-------------------------------------------------------------------------

    public void onProviderEnabled(String provider)
    {
        log(String.format("Provider enabled: %s", provider));
    }

    //-------------------------------------------------------------------------

    public void onProviderDisabled(String provider)
    {
        log(String.format("Provider disabled: %s", provider));
    }

    //-------------------------------------------------------------------------
    // location example
    //-------------------------------------------------------------------------

    public void startLocationTracking()
    {
        // grab the global location manager instance
        LocationManager locationManager =
            (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // setup minimum time (ms) and distance (m) between updates
        long minTime      = 60 * 60 * 1000;
        float minDistance = 500.0f;

        // grab criteria to get proper providers
        Criteria criteria = new Criteria();

        // set coarse provider
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String coarseProvider = locationManager.getBestProvider(criteria, true);
        log(String.format("Coarse Provider: %s", coarseProvider));
        if (coarseProvider != null) {
            locationManager.requestLocationUpdates(
                coarseProvider, minTime, minDistance, this);
        }

        // set fine provider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String fineProvider = locationManager.getBestProvider(criteria, true);
        log(String.format("Fine Provider: %s", fineProvider));
        if ((fineProvider != null) && !fineProvider.equals(coarseProvider)) {
            locationManager.requestLocationUpdates(
                fineProvider, minTime, minDistance, this);
        }

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
            (LocationManager)getSystemService(Context.LOCATION_SERVICE);

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
        TikTokApi api = new TikTokApi(this, mHandler, null);
        api.updateCurrentLocation(location);

        log(String.format("Updated location -> %s", location.toString()));
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

    private void log(String message)
    {
        String timeStamp = DateFormat.getDateTimeInstance().format(
            Calendar.getInstance().getTime());
        Log.i(kLogTag, String.format("TikTok - %s - %s", timeStamp, message));
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Handler  mHandler = new Handler();
    private Location mLastKnownLocation;

}
