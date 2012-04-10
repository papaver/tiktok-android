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
//import android.util.Log;
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
            CouponOverlayItem couponItem = (CouponOverlayItem)item;

            // open up coupon details
            Intent intent = new Intent(mContext, CouponActivity.class);
            intent.putExtra(CouponTable.sKeyId, couponItem.rowId());
            startActivityForResult(intent, 0);
            return true;
        }
    }

    //-------------------------------------------------------------------------
    // CouponOverlayItem
    //-------------------------------------------------------------------------

    private class CouponOverlayItem extends OverlayItem
    {
        public CouponOverlayItem(GeoPoint point, String title, String snippet,
                                 long couponRowId)
        {
            super(point, title, snippet);
            mRowId = couponRowId;
        }

        //-------------------------------------------------------------------------

        public long rowId()
        {
            return mRowId;
        }

        //-------------------------------------------------------------------------

        private long mRowId;
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
        mDatabaseAdapter.open();

        // fill the map with data
        mCursor = mDatabaseAdapter.fetchAllCoupons();
        startManagingCursor(mCursor);

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
        if (mDatabaseAdapter != null) mDatabaseAdapter.close();
        if (mCursor != null) mCursor.close();
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

        // add overlays
        cursor.moveToFirst();
        for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
            long rowId      = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyRowId));
            long merchantId = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
            String headline = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle));
            Date endTime    = new Date(cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime)) * 1000);

            // only add active coupons
            if (Coupon.isExpired(endTime)) continue;

            // query merchant from cursor
            final Merchant merchant = mDatabaseAdapter.fetchMerchant(merchantId);

            // update min/max values
            minLatitude  = Math.min(minLatitude,  merchant.latitude());
            maxLatitude  = Math.max(maxLatitude,  merchant.latitude());
            minLongitude = Math.min(minLongitude, merchant.longitude());
            maxLongitude = Math.max(maxLongitude, merchant.longitude());

            // setup geo point
            int latitude      = (int)(merchant.latitude() * 1E6);
            int longitude     = (int)(merchant.longitude() * 1E6);
            GeoPoint location = new GeoPoint(latitude, longitude);

            // add overlay
            String title           = merchant.name().toUpperCase();
            String snippet         = TextUtilities.capitalizeWords(headline);
            CouponOverlayItem item = new CouponOverlayItem(location, title, snippet, rowId);
            mOverlay.addOverlay(item);
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
    // fields
    //-------------------------------------------------------------------------

    private CouponsOverlay        mOverlay;
    private TapControlledMapView  mMapView;
    private Cursor                mCursor;
    private TikTokDatabaseAdapter mDatabaseAdapter;

    private boolean               mPaused = false;
    private GeoPoint              mMapCenter;
    private int                   mMapZoomLevel;
}

