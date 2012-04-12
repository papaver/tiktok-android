//-----------------------------------------------------------------------------
// CouponListActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
        mCouponAdapter = new CouponAdapter(this, mCursor);

        // update list view to use adapter
        final ListView listView = getListView();
        listView.setAdapter(mCouponAdapter);

        // sync coupons
        syncCoupons(mCouponAdapter, false);
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.coupon_list, menu);
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.refresh:
                syncCoupons(mCouponAdapter, true);
                break;
            case R.id.promo:
                break;
            case R.id.share:
                break;
        }
        return true;
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

    private void syncCoupons(final CouponAdapter adapter, boolean withDialog)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = withDialog ? new ProgressDialog(this) : null;
        if (progressDialog != null) {
            progressDialog.setMessage("Syncing Deals...");
            progressDialog.show();
        }

        final Context context = this;
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

                                // update listview
                                adapter.changeCursor(cursor);
                                if (mCursor != null) {
                                    mCursor.close();
                                    mCursor = cursor;
                                }

                                // close dialog
                                if (progressDialog != null) {
                                    progressDialog.cancel();
                                }
                            }
                        });
                    }
                }).start();
            }

            public void onError(Throwable error) {
                if (progressDialog != null) progressDialog.cancel();
                String message = context.getString(R.string.coupon_sync_fail);
                Toast.makeText(context, message, 1000).show();
            }
        });

        api.syncActiveCoupons();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Cursor                mCursor;
    private CouponAdapter         mCouponAdapter;
    private TikTokDatabaseAdapter mDatabaseAdapter;
}


