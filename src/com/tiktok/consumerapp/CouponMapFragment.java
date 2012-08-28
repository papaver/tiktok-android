//-----------------------------------------------------------------------------
// CouponMapFragment
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

@SuppressWarnings("deprecation")
public class CouponMapFragment extends SherlockFragment
                               implements LoaderManager.LoaderCallbacks<Cursor>
{

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag                  = "CouponMapFragment";
    private static final String kKeyLocalActivityManager = "LocalActivityManagerState";
    private static final String kMapActivityTag          = "MapActivity";

    //-------------------------------------------------------------------------
    // CouponMapAdapter
    //-------------------------------------------------------------------------

    private class CouponMapAdapter extends CursorAdapter
    {
        public CouponMapAdapter(final Context context, final Cursor cursor)
        {
            super(context, cursor, 0);
        }

        //---------------------------------------------------------------------

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
        }

        //---------------------------------------------------------------------

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    // LoaderManager Callbacks
    //-------------------------------------------------------------------------

    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String selection       = String.format("%s > ?", CouponTable.sKeyEndTime);
        String[] selectionArgs = { Long.toString(new Date().getTime() / 1000) };
        String sortOrder       = String.format("%s DESC", CouponTable.sKeyEndTime);

        return new CursorLoader(getSherlockActivity(), CouponTable.kContentUri,
            CouponTable.sFullProjection, selection, selectionArgs, sortOrder);
    }

    //-------------------------------------------------------------------------

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        boolean centerMap = mCouponAdapter.getCursor() == null;
        mCouponAdapter.swapCursor(cursor);
        populateMap(cursor, centerMap);
    }

    //-------------------------------------------------------------------------

    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCouponAdapter.swapCursor(null);
    }

    //-------------------------------------------------------------------------
    // SherlockFragment
    //-------------------------------------------------------------------------

    /**
     * Called once the fragment is associated with its activity.
     */
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    //-------------------------------------------------------------------------

    /**
     * Called to do initial creation of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // setup fragment attributes
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // attempt to recover state
        Bundle state = null;
        if (savedInstanceState != null) {
            state = savedInstanceState.getBundle(kKeyLocalActivityManager);
        }

        // setup the local activity manager
        mLocalActivityManager =
            new LocalActivityManager(getSherlockActivity(), true);
        mLocalActivityManager.dispatchCreate(state);
    }

    //-------------------------------------------------------------------------

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return getMapView();
    }

    //-------------------------------------------------------------------------

    /**
     * Tells the fragment that it's activity has completed its own
     * Activity.onCreate().
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getView().requestLayout();
    }

    //-------------------------------------------------------------------------

    /**
     * Makes the fragment visible to the user (based on its containing
     * activity being started).
     */
    @Override
    public void onStart()
    {
        super.onStart();
    }

    //-------------------------------------------------------------------------

    /**
     * Makes the fragment interacting with the user (based on its containing
     * activity being resumed).
     */
    @Override
    public void onResume()
    {
        super.onResume();
        mLocalActivityManager.dispatchResume();
    }

    //-------------------------------------------------------------------------

    /**
     * Fragment is no longer interacting with the user either because it's
     * activity is being paused or a fragment operation is modifying it in the
     * activity.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        mLocalActivityManager.dispatchPause(getActivity().isFinishing());
    }

    //-------------------------------------------------------------------------

    /**
     * Fragment is no longer visible to the user either because its activity is
     * being stopped or a fragment operation is modifying it in the activity.
     */
    @Override
    public void onStop()
    {
        super.onStop();
        mLocalActivityManager.dispatchStop();
    }

    //-------------------------------------------------------------------------

    /**
     * Allows the fragment to clean up resources associated with its View.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        // remove the map from the view hierarchy
        View mapView        = getMapView();
        ViewGroup viewGroup = (ViewGroup)mapView.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(mapView);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * called to do final cleanup of the fragment's state.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mLocalActivityManager.dispatchDestroy(getActivity().isFinishing());
    }

    //-------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(kKeyLocalActivityManager,
            mLocalActivityManager.saveInstanceState());
    }

    //-------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.coupon_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.user_location:
                centerMapToCurrentLocation();
                return true;
            default:
                return false;
        }
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private View getMapView()
    {
        // [moiz] this is a complete hack, unfortunately google is not willing
        //   to fix this right now apparently, it is 'too hard'... wtf goog
        //   goog recommends this technique to get maps working correctly with
        //   fragments, pretty much we are creating an activity locally and
        //   running the map inside that while displaying it in a fragment
        //   at the same time... yeah kinda sketch...

        // grab the map activity
        CouponMapActivity mapActivity =
            (CouponMapActivity)mLocalActivityManager.getActivity(kMapActivityTag);

        // if not found create a new instance
        if (mapActivity == null) {

            // create new map activity
            Intent intent = new Intent(getActivity(), CouponMapActivity.class);
            mLocalActivityManager.startActivity(kMapActivityTag, intent);
            mapActivity =
                (CouponMapActivity)mLocalActivityManager.getActivity(kMapActivityTag);

            // get root view and update settings
            ViewGroup rootView = (ViewGroup)mapActivity.getWindow().getDecorView();
            rootView.setVisibility(View.VISIBLE);
            rootView.setFocusableInTouchMode(true);
            rootView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

            // create a new array adapter to manage the cursor
            mCouponAdapter = new CouponMapAdapter(getSherlockActivity(), null);

            // load coupon data
            getLoaderManager().initLoader(0, null, CouponMapFragment.this);
        }

        return mapActivity.getWindow().getDecorView();
    }

    //-------------------------------------------------------------------------

    private void populateMap(Cursor cursor, boolean centerMap)
    {
        // forward populate request to map activity
        CouponMapActivity mapActivity =
            (CouponMapActivity)mLocalActivityManager.getActivity(kMapActivityTag);
        if (mapActivity != null) {
            mapActivity.populateMap(cursor, centerMap);
        }
    }

    //-------------------------------------------------------------------------

    private void centerMapToCurrentLocation()
    {
        // forward center request to map activity
        CouponMapActivity mapActivity =
            (CouponMapActivity)mLocalActivityManager.getActivity(kMapActivityTag);
        if (mapActivity != null) {
            mapActivity.centerMapToCurrentLocation();
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private LocalActivityManager mLocalActivityManager;
    private CouponMapAdapter     mCouponAdapter;

}
