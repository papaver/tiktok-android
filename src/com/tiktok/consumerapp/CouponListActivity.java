//-----------------------------------------------------------------------------
// CouponListActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponListActivity extends Activity
{
    static final String kLogTag = "CouponListActivity";

    //-------------------------------------------------------------------------
    // DBObserver
    //-------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.couponlist);

        // open up the database
        mDatabaseAdapter = new TikTokDatabaseAdapter(this);
        mDatabaseAdapter.open();

        // fill the listview with data
        mCursor = mDatabaseAdapter.fetchAllCoupons();
        startManagingCursor(mCursor);

        // create a new array adapter and set it to display the row
        final CouponAdapter adapter = new CouponAdapter(this, mCursor);

        // update list view to use adapter
        final ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);

        // run sync coupons task
        new SyncCouponsTask(this, new SyncTaskCompletionHandler() {
            public void onCursorUpdate(Cursor cursor) {
                adapter.changeCursor(cursor);
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = cursor;
                }
            }
        }).execute();
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
        if (mDatabaseAdapter != null) mDatabaseAdapter.close();
        if (mCursor != null) mCursor.close();
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

    static final String kLogTag = "SyncCouponsTask";

    //-------------------------------------------------------------------------

    public SyncCouponsTask(Context context,
                           SyncTaskCompletionHandler handler)
    {
        mHandler         = handler;
        mContext         = context;
        mDatabaseAdapter = new TikTokDatabaseAdapter(context);
        mDatabaseAdapter.open();
    }

    //-------------------------------------------------------------------------

    @Override
    public void onPreExecute()
    {
    }

    //-------------------------------------------------------------------------

    public Cursor doInBackground(Void... params)
    {
        // create and instance of the tiktok api and grab the available coupons
        TikTokApi api = new TikTokApi(mContext);
        Map<TikTokApi.CouponKey, Object> data = api.syncActiveCoupons();

        // process new coupons
        Coupon[] coupons = (Coupon[])data.get(TikTokApi.CouponKey.kCoupons);
        processCoupons(coupons);

        // kill coupons
        Long[] killed = (Long[])data.get(TikTokApi.CouponKey.kKilled);
        processKilled(killed);

        // update sold out coupons
        Long[] soldOut = (Long[])data.get(TikTokApi.CouponKey.kSoldOut);
        processSoldOut(soldOut);

        // update the cursor in the adapter
        return mDatabaseAdapter.fetchAllCoupons();
    }

    //-------------------------------------------------------------------------

    private void processCoupons(Coupon[] coupons)
    {
        // add only new coupons to the database
        List<Long> couponIds   = mDatabaseAdapter.fetchAllCouponIds();
        List<Long> merchantIds = mDatabaseAdapter.fetchAllMerchantIds();
        for (final Coupon coupon : coupons) {

            if (!merchantIds.contains(coupon.merchant().id())) {
                mDatabaseAdapter.createMerchant(coupon.merchant());
                Log.i(kLogTag, String.format(
                    "Added merchant to db: %s", coupon.merchant().name()));
            }

            if (!couponIds.contains(coupon.id())) {
                Log.w(getClass().getSimpleName(), coupon.toString());
                mDatabaseAdapter.createCoupon(coupon);
                Log.i(kLogTag, String.format(
                    "Added coupon to db: %s", coupon.title()));
            }
        }
    }

    //-------------------------------------------------------------------------

    private void processKilled(Long[] killed)
    {
        List<Long> couponIds = mDatabaseAdapter.fetchAllCouponIds();
        for (final Long id : killed) {
            if (couponIds.contains(id)) {
                Log.i(kLogTag, String.format("Killed deal id: %d", id));
                mDatabaseAdapter.deleteCoupon(id);
            }
        }
    }

    //-------------------------------------------------------------------------

    private void processSoldOut(Long[] soldOut)
    {
        List<Long> couponIds = mDatabaseAdapter.fetchAllCouponIds();
        for (final Long id : soldOut) {
            if (couponIds.contains(id)) {
                Coupon coupon = mDatabaseAdapter.fetchCoupon(id);
                if (!coupon.isSoldOut()) {
                    coupon.sellOut();
                    Log.i(kLogTag, String.format(
                        "SoldOut deal: %d / %s", coupon.id(), coupon.title()));
                    boolean result = mDatabaseAdapter.updateCoupon(coupon);
                    Log.i(kLogTag, String.format("Update result: %s", result ? "yes" : "no"));
                }
            }
        }
    }

    //-------------------------------------------------------------------------

    @Override
    public void onPostExecute(Cursor cursor)
    {
        if (mHandler != null) mHandler.onCursorUpdate(cursor);
        mDatabaseAdapter.close();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private SyncTaskCompletionHandler mHandler;
    private Context                   mContext;
    private TikTokDatabaseAdapter     mDatabaseAdapter;
}

//-----------------------------------------------------------------------------
// SyncCompletionHandler
//-----------------------------------------------------------------------------

interface SyncTaskCompletionHandler
{
    public abstract void onCursorUpdate(Cursor cursor);
}

