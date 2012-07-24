//-----------------------------------------------------------------------------
// LocationUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.lang.Math;

import android.location.Location;
//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class LocationUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "LocationUtilities";

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public static double distanceBetweenLocations(Location fromLocation,
                                                  Location toLocation)
    {
        final double d2r = Math.PI / 180.0;

        double lat1  = fromLocation.getLatitude();
        double long1 = fromLocation.getLongitude();
        double lat2  = toLocation.getLatitude();
        double long2 = toLocation.getLongitude();

        double dlong = (long2 - long1) * d2r;
        double dlat  = (lat2 - lat1) * d2r;
        double a     = Math.pow(Math.sin(dlat / 2.0), 2) +
                       Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c     = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d     = 6367 * c;

        return d;
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private LocationUtilities()
    {
    }
}
