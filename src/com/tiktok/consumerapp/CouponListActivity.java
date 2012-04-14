//-----------------------------------------------------------------------------
// CouponListActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    private void syncCoupons(final CouponAdapter adapter, boolean withDialog)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = withDialog ? new ProgressDialog(this) : null;
        if (progressDialog != null) {
            progressDialog.setMessage("Syncing Deals...");
            progressDialog.show();
        }

        final Context context   = this;
        final Handler handler   = new Handler();
        final Settings settings = new Settings(this);
        TikTokApi api = new TikTokApi(this, handler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {

                // run database query in another thread
                new Thread(new Runnable() {
                    public void run() {
                        final Cursor cursor = mDatabaseAdapter.fetchAllCoupons();

                        // update the ui in the ui thread
                        handler.post(new Runnable() {
                            public void run() {
                                settings.setLastUpdate(new Date());

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

        // query server
        api.syncActiveCoupons(settings.lastUpdate());
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
            new ShareUtilities.TwitterShare(this, message, new Handler(), callback));
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
            new ShareUtilities.FacebookShare(this, params, new Handler(), callback));
    }

    //-------------------------------------------------------------------------

    private void shareSMS()
    {
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
        TikTokApi api = new TikTokApi(this, new Handler(), new TikTokApi.CompletionHandler() {

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

    private Cursor                mCursor;
    private CouponAdapter         mCouponAdapter;
    private TikTokDatabaseAdapter mDatabaseAdapter;
}

