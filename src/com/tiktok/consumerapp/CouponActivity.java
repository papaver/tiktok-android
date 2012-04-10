//-----------------------------------------------------------------------------
// CouponActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.map.ItemizedOverlay;
import com.tiktok.consumerapp.utilities.TextUtilities;
import com.tiktok.consumerapp.utilities.UIUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponActivity extends MapActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "CouponActivity";

    private static final int kIntentSMS   = 1;
    private static final int kIntentEmail = 2;

    //-------------------------------------------------------------------------
    // enum
    //-------------------------------------------------------------------------

    private enum CouponState
    {
        kDefault,
        kExpired,
        kSoldOut,
        kActive
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
        setContentView(R.layout.coupon);

        // grab coupon id from intent
        Long id = (savedInstanceState == null) ? null :
            (Long)savedInstanceState.getSerializable(CouponTable.sKeyId);
        if (id == null) {
            Bundle extras = getIntent().getExtras();
            id = extras != null ? extras.getLong(CouponTable.sKeyId) : null;
        }

        // can't be here without a valid coupon id
        if (id == null) {
            finish();
        }

        // retrieve the coupon from the database
        TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(this);
        adapter.open();

        // grab coupon using id
        mCoupon = adapter.fetchCouponByRowId(id);
        setupCouponDetails(mCoupon);

        // close
        adapter.close();

        // setup coupon depending on status
        if (!Coupon.isExpired(mCoupon.endTime())) {
            startTimer(mCoupon);
        } else {
            expireCoupon(mCoupon, 0);
        }
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
        if (mIconManager != null)  mIconManager.clearAllRequests();
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == kIntentEmail) {
            TikTokApi api = new TikTokApi(this, new Handler(), null);
            api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kEmail);
        } else if (requestCode == kIntentSMS) {
            TikTokApi api = new TikTokApi(this, new Handler(), null);
            api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kSMS);
        }
    }

    //-------------------------------------------------------------------------
    // Events
    //-------------------------------------------------------------------------

    public void onClickMerchant(View view)
    {
        Intent intent = new Intent(this, MerchantActivity.class);
        intent.putExtra(MerchantTable.sKeyId, mCoupon.merchant().id());
        startActivityForResult(intent, 0);
    }

    //-------------------------------------------------------------------------

    public void onClickTwitter(View view)
    {
        Log.i(kLogTag, "Share Twitter");
    }

    //-------------------------------------------------------------------------

    public void onClickFacebook(View view)
    {
        FacebookManager manager = FacebookManager.getInstance(this);
        if (manager.facebook().isSessionValid()) {
            postFacebook();
        } else {
            setupFacebook();
        }
    }

    //-------------------------------------------------------------------------

    public void onClickMore(View view)
    {
        final String[] items = { "SMS", "Email" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Deal");
        builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    String choice = items[item];
                    if (choice.equals("SMS")) {
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

    public void onClickRedeem(View view)
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

                // validate the registration status
                String status = response.getStatus();
                if (status.equals(TikTokApi.kTikTokApiStatusOkay)) {

                    // redeem the coupon
                    mCoupon.redeem();
                    TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(context);
                    adapter.open();
                    adapter.updateCoupon(mCoupon);
                    adapter.close();

                    // update the banner
                    updateBanner(CouponState.kActive);

                // alert user of a problem
                } else if (status.equals(TikTokApi.kTikTokApiStatusForbidden)) {
                    String title   = "Redeem";
                    String message = response.getError();
                    Utilities.displaySimpleAlert(context, title, message);
                }
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "registration failed...", error);

                // cancel dialog
                progressDialog.cancel();

                // alert user of a problem
                String title   = "Redeem";
                String message = "Failed to redeem deal. Try again.";
                Utilities.displaySimpleAlert(context, title, message);
            }
        });

        // run the query
        api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kRedeem);
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
    // helper functions
    //-------------------------------------------------------------------------

    private void setupCouponDetails(Coupon coupon)
    {
        Merchant merchant = coupon.merchant();

        // title
        TextView title = (TextView)findViewById(R.id.title);
        title.setText(TextUtilities.capitalizeWords(coupon.title()));

        // gradient
        LinearLayout linearLayout   = (LinearLayout)findViewById(R.id.gradient);
        GradientDrawable background = (GradientDrawable)linearLayout.getBackground();
        background.setColor(Coupon.getColor(coupon.endTime(), coupon.startTime()));

        // merchant
        TextView name = (TextView)findViewById(R.id.merchant);
        name.setText(merchant.name().toUpperCase());

        // address
        TextView address = (TextView)findViewById(R.id.address);
        address.setText(merchant.address());

        // details
        String terms = "TikTok Terms and Conditions:\nwww.tiktok.com/terms";
        TextView details = (TextView)findViewById(R.id.details);
        details.setText(String.format("%s\n\n%s", coupon.details(), terms));

        // barcode
        Button barcode = (Button)findViewById(R.id.barcode);
        barcode.setText(coupon.barcode());

        // icon
        setupIcon(coupon);

        // map
        setupMap(merchant);

        // expire
        TextView expire = (TextView)findViewById(R.id.expire);
        expire.setText(Coupon.getExpirationTime(coupon.endTime()));

        // banner
        updateBanner(getCouponState(coupon));
    }

    //-------------------------------------------------------------------------

    private void setupIcon(final Coupon coupon)
    {
        final ImageView iconView            = (ImageView)findViewById(R.id.icon);
        final IconManager.IconData iconData = coupon.iconData();

        // setup the icon manager
        if (mIconManager == null) {
            mIconManager = new IconManager(this);
        }

        // use cached icon if available
        BitmapDrawable icon = mIconManager.getImage(iconData);
        if (icon != null) {
            iconView.setImageBitmap(icon.getBitmap());

        // use activity indicator and load image from server
        } else {

            // set activity indicator
            iconView.setImageResource(R.drawable.activity_indicator);
            iconView.startAnimation(UIUtilities.getActivityIndicatorAnimation());

            // download icon from server
            mIconManager.requestImage(iconData, new IconManager.CompletionHandler() {

                public void onSuccess(final BitmapDrawable drawable) {
                    Log.i(kLogTag, String.format("Downloaded icon: %s", iconData.url));
                    iconView.post(new Runnable() {
                        public void run() {
                            iconView.setImageBitmap(drawable.getBitmap());
                            iconView.clearAnimation();
                        }
                    });
                }

                public void onFailure() {
                    Log.e(kLogTag, String.format("Failed to download icon: %s", iconData.url));
                }

            });
        }
    }

    //-------------------------------------------------------------------------

    private void setupMap(Merchant merchant)
    {
        int latitude      = (int)(merchant.latitude() * 1E6);
        int longitude     = (int)(merchant.longitude() * 1E6);
        GeoPoint location = new GeoPoint(latitude, longitude);

        // center the map around the location
        MapView mapView             = (MapView)findViewById(R.id.map);
        MapController mapController = mapView.getController();
        mapController.setCenter(location);
        mapController.setZoom(17);

        // add a pin
        List<Overlay> mapOverlays = mapView.getOverlays();
        ItemizedOverlay overlay   = new ItemizedOverlay(this);
        OverlayItem item          = new OverlayItem(location, "", "");
        overlay.addOverlay(item);
        mapOverlays.add(overlay);
    }

    //-------------------------------------------------------------------------

    private void startTimer(final Coupon coupon)
    {
        Runnable timer = new Runnable() {
            public void run() {
                final Date endTime   = coupon.endTime();
                final Date startTime = coupon.startTime();

                // update expire time
                TextView expire = (TextView)findViewById(R.id.expire);
                expire.setText(Coupon.getExpirationTime(endTime));

                // update expire color
                LinearLayout linearLayout   = (LinearLayout)findViewById(R.id.gradient);
                GradientDrawable background = (GradientDrawable)linearLayout.getBackground();
                background.setColor(Coupon.getColor(endTime, startTime));
                linearLayout.invalidate();

                // run timer every second unless expired
                if (!Coupon.isExpired(endTime)) {
                    mHandler.postDelayed(this, 1000);
                } else {
                    mHandler.removeCallbacks(this);
                    expireCoupon(coupon, 200);
                }
            }
        };

        // run timer
        mHandler.post(timer);
    }

    //-------------------------------------------------------------------------

    private CouponState getCouponState(Coupon coupon)
    {
        CouponState state = CouponState.kDefault;
        state             = coupon.isSoldOut() ? CouponState.kSoldOut : state;
        state             = Coupon.isExpired(coupon.endTime()) ? CouponState.kExpired : state;
        state             = coupon.wasRedeemed() ? CouponState.kActive : state;
        return state;
    }

    //-------------------------------------------------------------------------

    private void expireCoupon(Coupon coupon, long milliseconds)
    {
        // fade out
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.6f);
        alpha.setDuration(milliseconds);
        alpha.setFillAfter(true);
        findViewById(R.id.coupon).startAnimation(alpha);

        // update coupon banner
        if (!coupon.wasRedeemed()) {
            updateBanner(CouponState.kExpired);
        }
    }

    //-------------------------------------------------------------------------

    private void updateBanner(CouponState state)
    {
        View redeem   = findViewById(R.id.banner_redeem);
        View expired  = findViewById(R.id.banner_expired);
        View soldout  = findViewById(R.id.banner_soldout);
        View redeemed = findViewById(R.id.banner_redeemed);

        redeem.setVisibility(View.GONE);
        expired.setVisibility(View.GONE);
        soldout.setVisibility(View.GONE);
        redeemed.setVisibility(View.GONE);

        switch (state)
        {
            case kDefault:
                redeem.setVisibility(View.VISIBLE);
                break;

            case kExpired:
                expired.setVisibility(View.VISIBLE);
                break;

            case kSoldOut:
                soldout.setVisibility(View.VISIBLE);
                break;

            case kActive:
                redeemed.setVisibility(View.VISIBLE);
                break;
        }
    }

    //-------------------------------------------------------------------------
    // share functions
    //-------------------------------------------------------------------------

    private void setupFacebook()
    {
        String title   = getString(R.string.facebook_setup);
        String message = getString(R.string.facebook_not_setup);

        // ask user to log into facebook before posting
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Facebook",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    authorizeAndPostFacebook();
                }
            });

        // display alert
        alertDialog.show();
    }

    //-------------------------------------------------------------------------

    private void authorizeAndPostFacebook()
    {
        FacebookManager manager = FacebookManager.getInstance(this);
        manager.authorize(this, new FacebookManager.CompletionHandler() {
            public void onSuccess(Bundle values) {
                postFacebook();
            }
            public void onError(Throwable error) {}
            public void onCancel() {}
        });
    }

    //-------------------------------------------------------------------------

    private void postFacebook()
    {
        // format the post description
        String formatted = TextUtilities.capitalizeWords(mCoupon.title());
        String deal      = String.format("%s at %s! " +
                                         "Grab your free deal at www.tiktok.com!",
                                         formatted, mCoupon.merchant().name());

        // package up the params
        Bundle params = new Bundle();
        params.putString("link",        "www.tiktok.com");
        params.putString("picture",     mCoupon.iconUrl());
        params.putString("name",        "TikTok");
        params.putString("caption",     "www.tiktok.com");
        params.putString("description", deal);

        // pop open dialog to allow user to confimr post
        final Context context   = this;
        FacebookManager manager = FacebookManager.getInstance(this);
        manager.postToWall(this, params, new FacebookManager.CompletionHandler() {
            public void onSuccess(Bundle values) {
                if (values.containsKey("post_id")) {

                    // let server know of share
                    TikTokApi api = new TikTokApi(context, new Handler(), null);
                    api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kFacebook);

                    // alert user of successful post
                    String title   = getString(R.string.facebook);
                    String message = getString(R.string.facebook_deal_post);
                    Utilities.displaySimpleAlert(context, title, message);
                }
            }
            public void onError(Throwable error) {}
            public void onCancel() {}
        });
    }

    //-------------------------------------------------------------------------

    public void shareEmail()
    {
        // present the email controller
        String merchant  = mCoupon.merchant().name();
        String formatted = TextUtilities.capitalizeWords(mCoupon.title());
        String subject   = String.format("TikTok: Checkout this amazing deal for %s!", merchant);
        String body      = String.format("<h3>TikTok</h3>" +
                                         "<b>%s</b> at <b>%s</b>" +
                                         "<br><br>" +
                                         "<a href='http://www.tiktok.com'>Get your deal on!</a>",
                                         formatted, merchant);

        // present email controller
        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("email:"));
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
        startActivityForResult(intent, kIntentEmail);
    }

    //-------------------------------------------------------------------------

    public void shareSMS()
    {
        String merchant  = mCoupon.merchant().name();
        String formatted = TextUtilities.capitalizeWords(mCoupon.title());
        String deal      = String.format("%s at %s", formatted, merchant);
        String body      = String.format("TikTok: %s! www.tiktok.com", deal);

        // present sms controller
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"));
        intent.putExtra("sms_body", body);
        intent.putExtra("compose_mode", true);
        intent.setType("vnd.android-dir/mms-sms");
        startActivityForResult(intent, kIntentSMS);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Coupon      mCoupon;
    private IconManager mIconManager;
    private Handler     mHandler = new Handler();
}

