//-----------------------------------------------------------------------------
// CouponMapActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    private static final String kLogTag = "CouponMapActivity";

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
            startActivityForResult(intent, 0);
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
        setContentView(R.layout.couponmap);

        Analytics.passCheckpoint("Deal Map");

        // grab the map view
        mMapView = (TapControlledMapView)findViewById(R.id.map_view);

        // add the map's zoom view to the zoom layout
        mMapView.setBuiltInZoomControls(true);

        // setup overlays
        mOverlay = new CouponsOverlay(mMapView);

        // dismiss balloon upon single tap of mapview (iOS behavior)
        mMapView.setOnSingleTapListener(new OnSingleTapListener() {
            public boolean onSingleTap(MotionEvent event) {
                mOverlay.hideAllBalloons();
                return true;
            }
        });

        // open up database connection
        mDatabaseAdapter = new TikTokDatabaseAdapter(this);

        // fill the map with data
        mCursor = mDatabaseAdapter.fetchAllCoupons();
        startManagingCursor(mCursor);

        // create a handler for the activity
        mHandler = new Handler();

        // setup intent receivers
        setupIntentFilter();

        // fill map with items
        populateMap(mCursor);
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
        //   views, only one is allowed per proceses...
        if (mPaused) {
            mPaused = false;
            MapController controller = mMapView.getController();
            controller.setCenter(mMapCenter);
            controller.setZoom(mMapZoomLevel + 1);
            controller.zoomOut();
        }
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
        mPaused           = true;
        mMapCenter        = mMapView.getMapCenter();
        mMapZoomLevel     = mMapView.getZoomLevel();
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
        cleanupIntentFilter();
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
    // helper methods
    //-------------------------------------------------------------------------

    private void setupIntentFilter()
    {
        Log.i(kLogTag, "settings up intent filters...");

        // updatemap filter - update cursor, repopluate map
        mUpdateMapReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(kLogTag, "Received updatemap intent from filter...");
                updateCursor();
            }
        };
        registerReceiver(mUpdateMapReceiver,
            new IntentFilter("com.tiktok.consumer.app.updatemap"));
    }

    //-------------------------------------------------------------------------

    private void cleanupIntentFilter()
    {
        Log.i(kLogTag, "removing intent filters...");

        unregisterReceiver(mUpdateMapReceiver);
    }

    //-------------------------------------------------------------------------

    private void populateMap(Cursor cursor)
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
        if (!cursor.moveToFirst()) return;

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
            final Merchant merchant        = mDatabaseAdapter.fetchMerchant(merchantId);
            final List<Location> locations = mDatabaseAdapter.fetchLocations(locationIds);

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

        // calculate center and region
        double latitude      = ((minLatitude + maxLatitude)   * 0.5);
        double longitude     = ((minLongitude + maxLongitude) * 0.5);
        double latitudeSpan  = (maxLatitude - minLatitude) * 1.05;
        double longitudeSpan = (maxLongitude - minLongitude) * 1.05;

        // add overlays to map
        mapOverlays.add(mOverlay);

        // center map
        GeoPoint center          = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        MapController controller = mMapView.getController();
        controller.setCenter(center);
        controller.zoomToSpan((int)(latitudeSpan * 1E6), (int)(longitudeSpan * 1E6));

        // update map
        mMapView.invalidate();
    }

    //-------------------------------------------------------------------------

    private void updateCursor()
    {
        // fetch the cursor on a background thread
        final MapActivity activity = this;
        new Thread(new Runnable() {
            public void run() {
                Log.i(kLogTag, "Updating cursor...");
                final Cursor cursor = mDatabaseAdapter.fetchAllCoupons();

                // swap to the new cursor on the main thread
                mHandler.post(new Runnable() {
                    public void run() {

                        // clean up the previous cursor
                        if (mCursor != null) {
                            activity.stopManagingCursor(mCursor);
                        }

                        // repopulate map
                        populateMap(cursor);

                        // start managing the new cursor
                        mCursor = cursor;
                        activity.startManagingCursor(mCursor);
                    }
                });
            }
        }).start();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private BroadcastReceiver     mUpdateMapReceiver;
    private CouponsOverlay        mOverlay;
    private Cursor                mCursor;
    private Handler               mHandler;
    private TapControlledMapView  mMapView;
    private TikTokDatabaseAdapter mDatabaseAdapter;

    private boolean               mPaused = false;
    private GeoPoint              mMapCenter;
    private int                   mMapZoomLevel;

}

