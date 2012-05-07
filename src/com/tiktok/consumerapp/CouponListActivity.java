//-----------------------------------------------------------------------------
// CouponListActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;

import com.tiktok.consumerapp.utilities.ShareUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponListActivity extends ListActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    static final String kLogTag = "CouponListActivity";

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
        setContentView(R.layout.couponlist);

        Analytics.passCheckpoint("Deals");

        // setup new handler
        mHandler = new Handler();

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

        // setup intent filter to recieve re-sync messages
        setupIntentFilter();

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
        cleanupIntentFilter();
        if (mDatabaseAdapter != null) mDatabaseAdapter.close();
        if (mSyncApi != null) mSyncApi.cancel();
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
                Analytics.passCheckpoint("Deal Header Reload");
                syncCoupons(mCouponAdapter, true);
                break;
            case R.id.promo:
                redeemPromoCode();
                break;
            case R.id.share:
                shareApp();
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

        // update cursor
        if (resultCode == CouponActivity.kResultRedeemed) {
            updateCursor();
        } else if (resultCode == Activity.RESULT_OK) {
            FacebookManager manager = FacebookManager.getInstance(this);
            manager.facebook().authorizeCallback(requestCode, resultCode, intent);
        }
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    private void setupIntentFilter()
    {
        Log.i(kLogTag, "settings up intent filters...");

        // redeemed filter - update cursor
        mRedeemedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(kLogTag, "Received redeemed intent from filter...");
                updateCursor();
            }
        };
        registerReceiver(mRedeemedReceiver,
            new IntentFilter("com.tiktok.consumer.app.redeemed"));

        // sync filter - resync coupons
        mResyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(kLogTag, "Received resync intent from filter...");
                syncCoupons(mCouponAdapter, false);
            }
        };
        registerReceiver(mResyncReceiver,
            new IntentFilter("com.tiktok.consumer.app.resync"));
    }

    //-------------------------------------------------------------------------

    private void cleanupIntentFilter()
    {
        Log.i(kLogTag, "removing intent filters...");

        unregisterReceiver(mRedeemedReceiver);
        unregisterReceiver(mResyncReceiver);
    }

    //-------------------------------------------------------------------------

    private void updateCursor()
    {
        // fetch the cursor on a background thread
        final Activity activity = this;
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

                        // start using the newer cursor, will get closed
                        mCouponAdapter.changeCursor(cursor);

                        // start managing the new cursor
                        mCursor = cursor;
                        activity.startManagingCursor(mCursor);
                    }
                });
            }
        }).start();
    }

    //-------------------------------------------------------------------------

    private void syncCoupons(final CouponAdapter adapter, boolean withDialog)
    {
        // cancel current request if any
        if (mSyncApi != null) mSyncApi.cancel();

        // setup progress dialog
        final ProgressDialog progressDialog = withDialog ? new ProgressDialog(this) : null;
        if (progressDialog != null) {
            progressDialog.setMessage("Syncing Deals...");
            progressDialog.show();
        }

        final Context context   = this;
        final Settings settings = new Settings(this);
        mSyncApi = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                mSyncApi = null;
                settings.setLastUpdate(new Date());
                if (progressDialog != null) progressDialog.cancel();
                updateCursor();
            }

            public void onError(Throwable error) {
                mSyncApi = null;
                if (progressDialog != null) progressDialog.cancel();
                String message = context.getString(R.string.coupon_sync_fail);
                Toast.makeText(context, message, 1000).show();
            }
        });

        // query server
        mSyncApi.syncActiveCoupons(settings.lastUpdate());
    }

    //-------------------------------------------------------------------------

    private void shareApp()
    {
        final String[] items = { "Twitter", "Facebook", "SMS", "Email" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share TikTok With Your Friends!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String choice = items[item];
                if (choice.equals("Twitter")) {
                    shareTwitter();
                } else if (choice.equals("Facebook")) {
                    shareFacebook();
                } else if (choice.equals("SMS")) {
                    shareSMS();
                } else if (choice.equals("Email")) {
                    shareEmail();
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //-------------------------------------------------------------------------

    private void shareTwitter()
    {
        // setup share message
        Utilities utilities = new Utilities(this);
        String message = String.format("@tiktok - Checkout this new daily deals app: " +
                                       "www.tiktok.com/download?ref=%s",
                                       utilities.getConsumerId());

        // setup share callback
        final ListActivity activity = this;
        TwitterManager.CompletionHandler callback = new TwitterManager.CompletionHandler() {

            public void onSuccess(Object object) {
                Analytics.passCheckpoint("App Tweeted");
                String message = getString(R.string.twitter_app_post);
                Toast.makeText(activity, message, 1000).show();
            }

            public void onError(Throwable error) {
                String message = getString(R.string.twitter_app_post_fail);
                Toast.makeText(activity, message, 1000).show();
            }

            public void onCancel() {}
        };

        // tweet
        ShareUtilities.shareTwitter(
            new ShareUtilities.TwitterShare(this, message, mHandler, callback));
    }

    //-------------------------------------------------------------------------

    private void shareFacebook()
    {
        // setup share post
        Utilities utilities = new Utilities(this);
        String icon         = "https://www.tiktok.com/images/logo.png";
        String link         = String.format("http://www.tiktok.com/download?ref=%s",
                                utilities.getConsumerId());
        String details      = "Checkout this new daily deals app TikTok!";

        // pack data into bundle
        Bundle params  = new Bundle();
        params.putString("link",        link);
        params.putString("name",        "TikTok");
        params.putString("picture",     icon);
        params.putString("caption",     link);
        params.putString("description", details);

        // setup share callback
        final ListActivity activity = this;
        FacebookManager.CompletionHandler callback = new FacebookManager.CompletionHandler() {

            public void onSuccess(Bundle values) {
                Analytics.passCheckpoint("App Facebooked");
                String message = getString(R.string.facebook_app_post);
                Toast.makeText(activity, message, 1000).show();
            }

            public void onError(Throwable error) {
                String message = getString(R.string.facebook_app_post_fail);
                Toast.makeText(activity, message, 1000).show();
            }

            public void onCancel() {}
        };

        // post
        ShareUtilities.shareFacebook(
            new ShareUtilities.FacebookShare(this, params, mHandler, callback));
    }

    //-------------------------------------------------------------------------

    private void shareSMS()
    {
        Analytics.passCheckpoint("App SMSed");

        Utilities utilities = new Utilities(this);
        String message      = String.format("TikTok: Checkout this awesome new daily " +
                                            "deal app: www.tiktok.com/download?ref=%s",
                                            utilities.getConsumerId());

        // present sms controller
        ShareUtilities.shareSMS(this, 0, message);
    }

    //-------------------------------------------------------------------------

    private void shareEmail()
    {
        Analytics.passCheckpoint("App Emailed");

        Utilities utilities = new Utilities(this);
        String subject      = "TikTok: Checkout this amazing daily deal app!";
        String body         = String.format("<h3>TikTok</h3>" +
                                            "<a href='http://www.tiktok.com/download?ref=%s'>" +
                                            "Get your deal on!</a>",
                                            utilities.getConsumerId());

        // present email controller
        ShareUtilities.shareEmail(this, 0, subject, body);
    }

    //-------------------------------------------------------------------------

    private void redeemPromoCode()
    {
        Analytics.passCheckpoint("Promo");

        // inflate layout
        LayoutInflater inflator =
            (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflator.inflate(R.layout.promo, null);

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Redeem", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText input = (EditText)view.findViewById(R.id.input);
                validatePromoCode(input.getText().toString());
            }
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //-------------------------------------------------------------------------

    private void validatePromoCode(String promoCode)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Redeeming...");
        progressDialog.show();

        // redeem the coupon with the server
        final Context context = this;
        TikTokApi api = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                TikTokApiResponse response = (TikTokApiResponse)data;

                // cancel dialog
                progressDialog.cancel();

                // verify promo code succeeded
                String message = null;
                String status  = response.getStatus();
                if (status.equals(TikTokApi.kTikTokApiStatusOkay)) {
                    message = getString(R.string.promo_success);
                    syncCoupons(mCouponAdapter, false);
                } else if (status.equals(TikTokApi.kTikTokApiStatusForbidden)) {
                    message = getString(R.string.promo_used);
                } else if (status.equals(TikTokApi.kTikTokApiStatusNotFound)) {
                    message = getString(R.string.promo_invalid);
                }

                // show message
                Toast.makeText(context, message, 3000).show();
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "promo code redemption failed...", error);

                // cancel dialog
                progressDialog.cancel();

                // alert user of a problem
                String message = getString(R.string.promo_fail);
                Toast.makeText(context, message, 2000).show();
            }
        });

        // run the query
        api.redeemPromotion(promoCode);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private BroadcastReceiver     mRedeemedReceiver;
    private BroadcastReceiver     mResyncReceiver;
    private Cursor                mCursor;
    private CouponAdapter         mCouponAdapter;
    private Handler               mHandler;
    private TikTokDatabaseAdapter mDatabaseAdapter;
    private TikTokApi             mSyncApi;

}

