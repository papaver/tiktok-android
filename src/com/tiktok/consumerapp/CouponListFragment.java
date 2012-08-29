//-----------------------------------------------------------------------------
// CouponListFragment
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import com.tiktok.consumerapp.utilities.ShareUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponListFragment extends SherlockListFragment
                                implements LoaderManager.LoaderCallbacks<Cursor>
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    static final String kLogTag = "CouponListFragment";

    //-------------------------------------------------------------------------
    // LoaderManager Callbacks
    //-------------------------------------------------------------------------

    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        long oneDay            = 24 * 60 * 60 * 1000;
        long oneDayAgo         = (new Date().getTime() - oneDay) / 1000;
        String selection       = String.format("%s > ?", CouponTable.sKeyEndTime);
        String[] selectionArgs = { Long.toString(oneDayAgo) };
        String sortOrder       = String.format("%s DESC", CouponTable.sKeyEndTime);

        return new CursorLoader(getSherlockActivity(), CouponTable.kContentUri,
            CouponTable.sFullProjection, selection, selectionArgs, sortOrder);
    }

    //-------------------------------------------------------------------------

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mCouponAdapter.swapCursor(cursor);
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

        // checkpoint marker
        Analytics.passCheckpoint("Deals");

        // setup fragment attributes
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // create a new array adapter to manage the cursor
        mCouponAdapter = new CouponAdapter(getSherlockActivity(), null);

        // update list view to use adapter
        setListAdapter(mCouponAdapter);

        // setup handler
        mHandler = new Handler();

        // load coupon data
        getLoaderManager().initLoader(0, null, this);

        // sync coupons
        syncCoupons(mCouponAdapter, false);
    }

    //-------------------------------------------------------------------------

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.couponlist, container, false);
        return view;
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
    }

    //-------------------------------------------------------------------------

    /**
     * Allows the fragment to clean up resources associated with its View.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    //-------------------------------------------------------------------------

    /**
     * called to do final cleanup of the fragment's state.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    //-------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.coupon_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.refresh:
                Analytics.passCheckpoint("Deal Reload");
                syncCoupons(mCouponAdapter, true);
                break;
            case R.id.promo:
                redeemPromoCode();
                break;
            case R.id.share_facebook:
                shareFacebook();
                break;
            case R.id.share_twitter:
                shareTwitter();
                break;
            case R.id.share_sms:
                shareSMS();
                break;
            case R.id.share_email:
                shareEmail();
                break;
        }
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id)
    {
        super.onListItemClick(listView, view, position, id);
        Intent intent = new Intent(getSherlockActivity(), CouponActivity.class);
        intent.putExtra(CouponTable.sKeyId, id);
        startActivity(intent);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    private void syncCoupons(final CouponAdapter adapter, boolean withDialog)
    {
        // grab current context
        final Context context = getSherlockActivity();

        // setup progress dialog
        final ProgressDialog progressDialog =
            withDialog ? new ProgressDialog(context) : null;
        if (progressDialog != null) {
            progressDialog.setMessage("Syncing Deals...");
            progressDialog.show();
        }

        final Settings settings = new Settings(context);
        TikTokApi api = new TikTokApi(context, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                settings.setLastUpdate(new Date());
                if (progressDialog != null) progressDialog.cancel();
            }

            public void onError(Throwable error) {
                if (progressDialog != null) progressDialog.cancel();
                String message = context.getString(R.string.coupon_sync_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

        // query server
        api.syncActiveCoupons(settings.lastUpdate());
    }

    //-------------------------------------------------------------------------

    private void shareTwitter()
    {
        final Context context = getSherlockActivity();

        // setup share message
        Utilities utilities = new Utilities(context);
        String message = String.format(
            "@tiktok - Checkout this new daily deals app: " +
            "www.tiktok.com/download?ref=%s", utilities.getConsumerId());

        // setup share callback
        TwitterManager.CompletionHandler callback = new TwitterManager.CompletionHandler() {

            public void onSuccess(Object object) {
                Analytics.passCheckpoint("App Tweeted");
                String message = getString(R.string.twitter_app_post);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            public void onError(Throwable error) {
                String message = getString(R.string.twitter_app_post_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            public void onCancel() {}
        };

        // tweet
        ShareUtilities.shareTwitter(
            new ShareUtilities.TwitterShare(getSherlockActivity(),
                message, mHandler, callback));
    }

    //-------------------------------------------------------------------------

    private void shareFacebook()
    {
        final Context context = getSherlockActivity();

        // setup share post
        Utilities utilities = new Utilities(context);
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
        FacebookManager.CompletionHandler callback = new FacebookManager.CompletionHandler() {

            public void onSuccess(Bundle values) {
                Analytics.passCheckpoint("App Facebooked");
                String message = getString(R.string.facebook_app_post);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            public void onError(Throwable error) {
                String message = getString(R.string.facebook_app_post_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            public void onCancel() {}
        };

        // post
        ShareUtilities.shareFacebook(
            new ShareUtilities.FacebookShare(getSherlockActivity(),
                params, mHandler, callback));
    }

    //-------------------------------------------------------------------------

    private void shareSMS()
    {
        Analytics.passCheckpoint("App SMSed");

        final SherlockFragmentActivity activity = getSherlockActivity();

        Utilities utilities = new Utilities(activity);
        String message      = String.format(
            "TikTok: Checkout this awesome new daily " +
            "deal app: www.tiktok.com/download?ref=%s", utilities.getConsumerId());

        // present sms controller
        ShareUtilities.shareSMS(activity, 0, message);
    }

    //-------------------------------------------------------------------------

    private void shareEmail()
    {
        Analytics.passCheckpoint("App Emailed");

        final SherlockFragmentActivity activity = getSherlockActivity();

        Utilities utilities = new Utilities(activity);
        String subject      = "TikTok: Checkout this amazing daily deal app!";
        String body         = String.format(
                "<h3>TikTok</h3>" +
                "<a href='http://www.tiktok.com/download?ref=%s'>" +
                "Get your deal on!</a>", utilities.getConsumerId());

        // present email controller
        ShareUtilities.shareEmail(activity, 0, subject, body);
    }

    //-------------------------------------------------------------------------

    private void redeemPromoCode()
    {
        Analytics.passCheckpoint("Promo");

        SherlockFragmentActivity activity = getSherlockActivity();

        // inflate layout
        LayoutInflater inflator =
            (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflator.inflate(R.layout.promo, null);

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Promo Code");
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
        final Context context = getSherlockActivity();

        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Redeeming...");
        progressDialog.show();

        // redeem the coupon with the server
        TikTokApi api = new TikTokApi(context, mHandler, new TikTokApi.CompletionHandler() {

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
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "promo code redemption failed...", error);

                // cancel dialog
                progressDialog.cancel();

                // alert user of a problem
                String message = getString(R.string.promo_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

        // run the query
        api.redeemPromotion(promoCode);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private CouponAdapter mCouponAdapter;
    private Handler       mHandler;

}

