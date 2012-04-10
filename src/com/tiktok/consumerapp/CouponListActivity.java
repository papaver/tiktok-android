//-----------------------------------------------------------------------------
// CouponListActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponListActivity extends ListActivity
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
        CouponAdapter adapter = new CouponAdapter(this, mCursor);

        // update list view to use adapter
        final ListView listView = getListView();
        listView.setAdapter(adapter);

        // sync coupons
        syncCoupons(adapter);
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

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id)
    {
        super.onListItemClick(listView, view, position, id);
        Intent intent = new Intent(this, CouponActivity.class);
        intent.putExtra(CouponTable.sKeyId, id);
        startActivityForResult(intent, 0);
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.i(kLogTag, String.format("Result of intent: %d", resultCode));
    }

    //-------------------------------------------------------------------------

    private void syncCoupons(final CouponAdapter adapter)
    {
        final Handler handler = new Handler();
        TikTokApi api = new TikTokApi(this, handler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {

                // run database query in another thread
                new Thread(new Runnable() {
                    public void run() {
                        final Cursor cursor = mDatabaseAdapter.fetchAllCoupons();

                        // update the ui in the ui thread
                        handler.post(new Runnable() {
                            public void run() {
                                adapter.changeCursor(cursor);
                                if (mCursor != null) {
                                    mCursor.close();
                                    mCursor = cursor;
                                }
                            }
                        });
                    }
                }).start();
            }

            public void onError(Throwable error) {
            }
        });

        api.syncActiveCoupons();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private TikTokDatabaseAdapter mDatabaseAdapter;
    private Cursor                mCursor;
}


