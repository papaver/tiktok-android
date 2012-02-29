//-----------------------------------------------------------------------------
// CouponMapActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.os.Bundle;
import android.view.KeyEvent;
//import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponMapActivity extends MapActivity
{

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.couponmap);

        // grab the map view
        mMapView = (MapView)findViewById(R.id.map_view);

        // add the map's zoom view to the zoom layout
        mMapView.setBuiltInZoomControls(true);
        /*
        LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);
        zoomLayout.addView(mMapView.getZoomControls(),
            new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mMapView.displayZoomControls(true);
        */

        // zoom in on a specific location
        int latitude        = (int)(49.27697 * 1e6);
        int longitude       = (int)(-123.01148 * 1e6);
        GeoPoint coordinate = new GeoPoint(latitude, longitude);

        // zoom to location
        MapController mapController = mMapView.getController();
        mapController.animateTo(coordinate);
        mapController.setZoom(17);
        mMapView.invalidate();
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

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        MapController mapController = mMapView.getController();
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_3:
                mapController.zoomIn();
                break;
            case KeyEvent.KEYCODE_1:
                mapController.zoomOut();
                break;
        }

        // switch to satellite view
        //mMapView.setSatellite(true);

        // swtich to street view
        //mMapView.setStreetView(true);

        return super.onKeyDown(keyCode, event);
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

    private MapView mMapView;

}

