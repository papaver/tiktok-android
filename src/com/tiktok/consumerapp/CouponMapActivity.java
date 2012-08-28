//-----------------------------------------------------------------------------
// CouponMapActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;

import com.tiktok.consumerapp.map.ItemizedOverlay;
import com.tiktok.consumerapp.utilities.TextUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponMapActivity extends MapActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "CouponMapActivity";

    //-------------------------------------------------------------------------
    // CouponsOverlay
    //-------------------------------------------------------------------------

    private class CouponsOverlay extends ItemizedOverlay
    {

        public CouponsOverlay(Drawable defaultMarker, MapView mapView)
        {
            super(defaultMarker, mapView);

            // [moiz] hack to fix map crash due to google bug
            // http://code.google.com/p/android/issues/detail?id=2035
            populate();

            // iOS-like  defaults
            setShowClose(false);
            setShowDisclosure(true);
            setSnapToCenter(false);

            setBalloonBottomOffset(defaultMarker.getBounds().height());
        }

        //-------------------------------------------------------------------------

        public CouponsOverlay(MapView mapView)
        {
            this(mapView.getContext().getResources().getDrawable(R.drawable.pin), mapView);
        }

        //-------------------------------------------------------------------------

        @Override
        protected boolean onBalloonTap(int index, OverlayItem item)
        {
            Analytics.passCheckpoint("Deal Map Details");

            CouponOverlayItem couponItem = (CouponOverlayItem)item;

            // open up coupon details
            Intent intent = new Intent(mContext, CouponActivity.class);
            intent.putExtra(CouponTable.sKeyId, couponItem.id());
            startActivity(intent);
            return true;
        }
    }

    //-------------------------------------------------------------------------
    // CouponOverlayItem
    //-------------------------------------------------------------------------

    private class CouponOverlayItem extends OverlayItem
    {
        public CouponOverlayItem(GeoPoint point, String title, String snippet, long id)
        {
            super(point, title, snippet);
            mId = id;
        }

        //---------------------------------------------------------------------

        public long id()
        {
            return mId;
        }

        //---------------------------------------------------------------------

        private long mId;
    }

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

        // checkpoint marker
        Analytics.passCheckpoint("Deal Map");

        // load layout containing map
        setContentView(R.layout.map);

        // grab the map view
        mMapView = (TapControlledMapView)findViewById(R.id.map_view);

        // add the map's zoom view to the zoom layout
        mMapView.setBuiltInZoomControls(true);

        // dismiss balloon upon single tap of mapview (iOS behavior)
        mMapView.setOnSingleTapListener(new OnSingleTapListener() {
            public boolean onSingleTap(MotionEvent event) {
                mOverlay.hideAllBalloons();
                return true;
            }
        });

        // setup overlays
        mOverlay = new CouponsOverlay(mMapView);
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

        // [moiz] super fucking ugly hackery to get maps working with multiple
        //   views, only one is allowed per proceses so we need to keep track
        //   of what was being looked at last...
        mMapView.onRestoreInstanceState(mMapState);
        mMapView.requestLayout();
    }

    //-------------------------------------------------------------------------

    /**
     * Another activity is taking focus (this activity is about to be "paused")
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        // keep track of map data to reset map on resume
        mMapView.onSaveInstanceState(mMapState);
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
        switch (keyCode) {

            case KeyEvent.KEYCODE_3:
                mapController.zoomIn();
                break;
            case KeyEvent.KEYCODE_1:
                mapController.zoomOut();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    //-------------------------------------------------------------------------
    // MapsActivity
    //-------------------------------------------------------------------------

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public void populateMap(Cursor cursor, boolean centerMap)
    {
        List<Overlay> mapOverlays = mMapView.getOverlays();

        // clear current overlays
        mapOverlays.clear();
        mOverlay.clear();

        // keep track of min/max of the locations
        double minLatitude  =   90.0;
        double maxLatitude  =  -90.0;
        double minLongitude =  180.0;
        double maxLongitude = -180.0;

        // nothing to do if cursor is empty
        if (!cursor.moveToFirst()) {
            mMapView.invalidate();
            mMapView.requestLayout();
            return;
        }

        // setup adapter
        TikTokDatabaseAdapter databaseAdapter
            = new TikTokDatabaseAdapter(CouponMapActivity.this);

        // add overlays
        for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
            long id            = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyId));
            long merchantId    = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
            String locationIds = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyLocations));
            String headline    = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle));
            Date endTime       = new Date(cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime)) * 1000);

            // only add active coupons
            if (Coupon.isExpired(endTime)) continue;

            // query merchant from cursor
            Merchant merchant        = databaseAdapter.fetchMerchant(merchantId);
            List<Location> locations = databaseAdapter.fetchLocations(locationIds);

            // loop through all of the locations
            for (Location location : locations) {

                // update min/max values
                minLatitude  = Math.min(minLatitude,  location.latitude());
                maxLatitude  = Math.max(maxLatitude,  location.latitude());
                minLongitude = Math.min(minLongitude, location.longitude());
                maxLongitude = Math.max(maxLongitude, location.longitude());

                // setup geo point
                int latitude      = (int)(location.latitude() * 1E6);
                int longitude     = (int)(location.longitude() * 1E6);
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                // add overlay
                String title           = merchant.name().toUpperCase();
                String snippet         = TextUtilities.capitalizeWords(headline);
                CouponOverlayItem item = new CouponOverlayItem(geoPoint, title, snippet, id);
                mOverlay.addOverlay(item);
            }
        }

        // add overlays to map
        mapOverlays.add(mOverlay);

        // calculate center and region
        if (centerMap) {
            double latitude      = ((minLatitude + maxLatitude)   * 0.5);
            double longitude     = ((minLongitude + maxLongitude) * 0.5);
            double latitudeSpan  = (maxLatitude - minLatitude) * 1.05;
            double longitudeSpan = (maxLongitude - minLongitude) * 1.05;

            // center map
            GeoPoint center          = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
            MapController controller = mMapView.getController();
            controller.setCenter(center);
            controller.zoomToSpan((int)(latitudeSpan * 1E6), (int)(longitudeSpan * 1E6));
        }

        // update map
        mMapView.invalidate();
        mMapView.requestLayout();
    }

    //-------------------------------------------------------------------------

    public void centerMapToCurrentLocation()
    {
        LocationTrackerManager manager = LocationTrackerManager.getInstance(this);
        android.location.Location currentLocation = manager.currentLocation();
        if (currentLocation != null) {
            double latitude  = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            centerMap(latitude, longitude);
        }
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private void centerMap(double latitude, double longitude)
    {
        if ((latitude != 0.0) && (longitude != 0.0)) {
            MapController controller = mMapView.getController();
            controller.setCenter(new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6)));
            controller.setZoom(15);
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private TapControlledMapView  mMapView;
    private CouponsOverlay        mOverlay;
    private Bundle                mMapState = new Bundle();

}

