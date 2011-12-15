//-----------------------------------------------------------------------------
// TikTokActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
            CouponTable.sKeyIcon 
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
        if (mDatabaseAdapter == null) mDatabaseAdapter.close();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
    
    private TikTokDatabaseAdapter mDatabaseAdapter;
    private Cursor                mCursor;

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
        Coupon[] coupons = api.getCoupons();

        // add only new coupons to the database
        List<Long> currentIds = mDatabaseAdapter.fetchAllCouponIds();
        for (final Coupon coupon : coupons) {
            if (!currentIds.contains(coupon.getId())) {
                mDatabaseAdapter.createCoupon(coupon);
                Log.w(getClass().getSimpleName(), String.format(
                    "Added coupon to db: %s", coupon.getTitle()));
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

