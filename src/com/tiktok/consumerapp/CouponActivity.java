//-----------------------------------------------------------------------------
// CouponActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.map.ItemizedOverlay;
import com.tiktok.consumerapp.utilities.ShareUtilities;
import com.tiktok.consumerapp.utilities.UIUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponActivity extends SherlockMapActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "CouponActivity";

    private static final int kIntentSMS   = 100;
    private static final int kIntentEmail = 101;

    public static final int kResultDefault  = 1;
    public static final int kResultRedeemed = 2;

    //-------------------------------------------------------------------------
    // enum
    //-------------------------------------------------------------------------

    private enum CouponState
    {
        kDefault,
        kExpired,
        kSoldOut,
        kActive,
        kInfo,
    }

    //-------------------------------------------------------------------------

    private class MapOverlay extends ItemizedOverlay
    {

        public MapOverlay(Drawable defaultMarker, MapView mapView)
        {
            super(defaultMarker, mapView);

            // iOS-like  defaults
            setShowClose(false);
            setShowDisclosure(true);
            setSnapToCenter(false);

            setBalloonBottomOffset(defaultMarker.getBounds().height());
        }

        //-------------------------------------------------------------------------

        public MapOverlay(MapView mapView)
        {
            this(mapView.getContext().getResources().getDrawable(R.drawable.pin), mapView);
        }

        //-------------------------------------------------------------------------

            @Override
            public boolean onTouchEvent(MotionEvent e, MapView mapView)
            {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    onClickMap(mapView);
                }
                return false;
            }
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

        // checkpoint marker
        Analytics.passCheckpoint("Deal");

        // setup action bar
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Deal");
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        // grab coupon id from intent
        Long id = (savedInstanceState == null) ? null :
            (Long)savedInstanceState.getSerializable(CouponTable.sKeyId);
        if (id == null) {
            Bundle extras = getIntent().getExtras();
            id = extras != null ? extras.getLong(CouponTable.sKeyId) : null;
        }

        // can't be here without a valid coupon id
        if (id == null) finish();

        // update text views with fonts
        updateFonts();

        // retrieve the coupon from the database
        TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(this);
        mCoupon = adapter.fetchCoupon(id);
        setupCouponDetails(mCoupon);

        // setup coupon depending on status
        if (!Coupon.isExpired(mCoupon.endTime())) {
            startTimer(mCoupon);
        } else {
            expireCoupon(mCoupon, 0);
        }

        // set default result
        setResult(kResultDefault);
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
        if (mIconManager != null) mIconManager.clearAllRequests();
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == kIntentEmail) {
            Analytics.passCheckpoint("Deal Emailed");
            TikTokApi api = new TikTokApi(this, new Handler(), null);
            api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kEmail);
        } else if (requestCode == kIntentSMS) {
            Analytics.passCheckpoint("Deal SMSed");
            TikTokApi api = new TikTokApi(this, new Handler(), null);
            api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kSMS);
        } else if (resultCode == Activity.RESULT_OK) {
            FacebookManager manager = FacebookManager.getInstance(this);
            manager.facebook().authorizeCallback(requestCode, resultCode, data);
        }
    }

    //-------------------------------------------------------------------------
    // events
    //-------------------------------------------------------------------------

    public void onClickMap(View view)
    {
        Analytics.passCheckpoint("Deal Map Opened");

        // get current location
        android.location.Location coordinate = getCurrentLocation();
        Location location = mCoupon.getClosestLocation(coordinate);

        // open map app
        String merchant = mCoupon.merchant().name();
        String address  = location.address().replace(" ", "+");
        String uri      = String.format("geo:0,0?q=%s+%s", merchant, address);
        Intent intent   = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    //-------------------------------------------------------------------------

    public void onClickMerchant(View view)
    {
        Intent intent = new Intent(this, MerchantActivity.class);
        intent.putExtra(CouponTable.sKeyId, mCoupon.id());
        startActivityForResult(intent, 0);
    }

    //-------------------------------------------------------------------------

    public void onClickTwitter(View view)
    {
        shareTwitter();
    }

    //-------------------------------------------------------------------------

    public void onClickFacebook(View view)
    {
        shareFacebook();
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
                    adapter.updateCoupon(mCoupon);

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

    public void OnClickValidateMerchantPin(View view)
    {
        // only valid for merchants that use the pin
        if (!mCoupon.merchant().usesPin()) return;

        Analytics.passCheckpoint("Merchant Pin");

        // inflate layout
        LayoutInflater inflator =
            (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertView = inflator.inflate(R.layout.merchant_pin, null);

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertView);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText input = (EditText)alertView.findViewById(R.id.input);
                validateMerchantPin(input.getText().toString());
            }
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void updateFonts()
    {
        // grab fonts
        AssetManager manager = getAssets();
        Typeface helvBd      = Typeface.createFromAsset(manager, "fonts/HelveticaNeueBd.ttf");
        Typeface helvMed     = Typeface.createFromAsset(manager, "fonts/HelveticaNeueMed.ttf");
        Typeface helvLt      = Typeface.createFromAsset(manager, "fonts/HelveticaNeueLt.ttf");
        Typeface neutraBd    = Typeface.createFromAsset(manager, "fonts/NeutraDisp-BoldAlt.otf");
        Typeface unitedBd    = Typeface.createFromAsset(manager, "fonts/UnitedSansRgBd.otf");

        // grab text views
        TextView title   = (TextView)findViewById(R.id.title);
        TextView name    = (TextView)findViewById(R.id.merchant);
        TextView address = (TextView)findViewById(R.id.address);
        TextView details = (TextView)findViewById(R.id.details);
        Button barcode   = (Button)findViewById(R.id.barcode);
        TextView expire  = (TextView)findViewById(R.id.expire);

        // update font
        title.setTypeface(helvBd);
        name.setTypeface(helvBd);
        address.setTypeface(helvMed);
        details.setTypeface(helvLt);
        barcode.setTypeface(unitedBd);
        expire.setTypeface(neutraBd);
    }

    //-------------------------------------------------------------------------

    private void setupCouponDetails(Coupon coupon)
    {
        Merchant merchant = coupon.merchant();
        int locationCount = coupon.locations().size();
        Location location = coupon.locations().get(0);

        // title
        TextView title = (TextView)findViewById(R.id.title);
        title.setText(coupon.formattedTitle());

        // gradient
        LinearLayout linearLayout   = (LinearLayout)findViewById(R.id.gradient);
        GradientDrawable background = (GradientDrawable)linearLayout.getBackground();
        background.setColor(Coupon.getColor(coupon.endTime(), coupon.startTime()));

        // merchant
        TextView name = (TextView)findViewById(R.id.merchant);
        name.setText(merchant.name().toUpperCase());

        // address
        TextView address = (TextView)findViewById(R.id.address);
        address.setText(locationCount > 1 ?
                String.format("Available at %d locations.", locationCount) :
                location.address());

        // details
        TextView details = (TextView)findViewById(R.id.details);
        details.setText(coupon.formattedDetails());

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
                    Log.e(kLogTag, String.format(
                        "Failed to download icon: %s", iconData.url));
                }

            });
        }
    }

    //-------------------------------------------------------------------------

    private void setupMap(Merchant merchant)
    {
        // get current location
        android.location.Location coordinate = getCurrentLocation();
        Location location = mCoupon.getClosestLocation(coordinate);

        // setup geo coordinate
        int latitude      = (int)(location.latitude() * 1E6);
        int longitude     = (int)(location.longitude() * 1E6);
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        // center the map around the location
        MapView mapView             = (MapView)findViewById(R.id.map);
        MapController mapController = mapView.getController();
        mapController.setCenter(geoPoint);
        mapController.setZoom(17);

        // add a pin
        List<Overlay> mapOverlays = mapView.getOverlays();
        MapOverlay overlay        = new MapOverlay(mapView);
        OverlayItem item          = new OverlayItem(geoPoint, "", "");
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
        state             = !coupon.isRedeemable() ? CouponState.kInfo : state;
        return state;
    }

    //-------------------------------------------------------------------------

    private void expireCoupon(Coupon coupon, long milliseconds)
    {
        Analytics.passCheckpoint("Deal Expired");

        // fade out
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.6f);
        alpha.setDuration(milliseconds);
        alpha.setFillAfter(true);
        findViewById(R.id.coupon).startAnimation(alpha);

        // update coupon banner
        if (!coupon.wasRedeemed() && coupon.isRedeemable()) {
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
        View info     = findViewById(R.id.banner_info);

        redeem.setVisibility(View.GONE);
        expired.setVisibility(View.GONE);
        soldout.setVisibility(View.GONE);
        redeemed.setVisibility(View.GONE);
        info.setVisibility(View.GONE);

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

            case kInfo:
                info.setVisibility(View.VISIBLE);
                break;
        }
    }

    //-------------------------------------------------------------------------

    private void validateMerchantPin(String merchantPin)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Validating...");
        progressDialog.show();

        // redeem the coupon with the server
        final Context context = this;
        TikTokApi api = new TikTokApi(this, mHandler, new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                TikTokApiResponse response = (TikTokApiResponse)data;

                // cancel dialog
                progressDialog.cancel();

                // verify merchant pin validation succeeded
                String message = null;
                String status  = response.getStatus();
                if (status.equals(TikTokApi.kTikTokApiStatusOkay)) {
                    message = getString(R.string.merchant_pin_success);
                } else {
                    message = response.getError();
                }

                // show message
                String title = getString(R.string.merchant_pin);
                Utilities.displaySimpleAlert(context, title, message);
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "merchant pin validation failed...", error);

                // cancel dialog
                progressDialog.cancel();

                // alert user of a problem
                String title   = getString(R.string.merchant_pin);
                String message = getString(R.string.merchant_pin_fail);
                Utilities.displaySimpleAlert(context, title, message);
            }
        });

        // run the query
        api.validateMerchantPin(mCoupon.id(), merchantPin);
    }

    //-------------------------------------------------------------------------

    private android.location.Location getCurrentLocation()
    {
        // attempt to get current location
        if (mLocation == null) {
            LocationTrackerManager manager = LocationTrackerManager.getInstance(this);
            mLocation = manager.currentLocation();
        }

        if (mLocation != null) {
            return mLocation;
        } else {
            return new android.location.Location("");
        }
    }

    //-------------------------------------------------------------------------
    // share functions
    //-------------------------------------------------------------------------

    private void shareTwitter()
    {
        // get current location
        android.location.Location coordinate = getCurrentLocation();
        Location location = mCoupon.getClosestLocation(coordinate);

        // setup share message
        Merchant merchant = mCoupon.merchant();
        String handle     = merchant.twitterHandle().equals("") ?
                            merchant.name() :
                            merchant.twitterHandle();
        String city       = location.getCity().toLowerCase();
        String formatted  = mCoupon.formattedTitle();
        String deal       = String.format(
            "I just got %s from %s! @TikTok #FREEisBETTER #%s", formatted, handle, city);

        // setup share callback
        final Handler handler      = new Handler();
        final MapActivity activity = this;
        TwitterManager.CompletionHandler callback = new TwitterManager.CompletionHandler() {

            public void onSuccess(Object object) {
                Analytics.passCheckpoint("Deal Tweeted");
                String message = getString(R.string.twitter_deal_post);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

                // let server know of share
                TikTokApi api = new TikTokApi(activity, handler, null);
                api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kTwitter);
            }

            public void onError(Throwable error) {
                Log.e(kLogTag, "Failed to tweet deal", error);
                String message = getString(R.string.twitter_deal_post_fail);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }

            public void onCancel() {}
        };

        // tweet
        ShareUtilities.shareTwitter(
            new ShareUtilities.TwitterShare(this, deal, handler, callback));
    }

    //-------------------------------------------------------------------------

    private void shareFacebook()
    {
        // format the post description
        String formatted = mCoupon.formattedTitle();
        String deal      = String.format(
            "%s at %s! - " +
            "I just scored this awesome deal with my " +
            "TikTok app. Sad you missed it? Don't be " +
            "a square... download the app and start " +
            "getting your own deals right now.",
            formatted, mCoupon.merchant().name());

        // package up the params
        Bundle params = new Bundle();
        params.putString("link",        "www.tiktok.com");
        params.putString("picture",     mCoupon.iconUrl());
        params.putString("name",        "TikTok");
        params.putString("caption",     "www.tiktok.com");
        params.putString("description", deal);

        // setup share callback
        final Handler handler      = new Handler();
        final MapActivity activity = this;
        FacebookManager.CompletionHandler callback = new FacebookManager.CompletionHandler() {

            public void onSuccess(Bundle values) {
                Analytics.passCheckpoint("Deal Facebooked");
                String message = getString(R.string.facebook_deal_post);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

                // let server know of share
                TikTokApi api = new TikTokApi(activity, handler, null);
                api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kFacebook);
           }

            public void onError(Throwable error) {
                Log.e(kLogTag, "Failed to post deal.", error);
                String message = getString(R.string.facebook_deal_post_fail);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }

            public void onCancel() {}
        };

        // post
        ShareUtilities.shareFacebook(
            new ShareUtilities.FacebookShare(this, params, handler, callback));
    }

    //-------------------------------------------------------------------------

    public void shareSMS()
    {
        String merchant  = mCoupon.merchant().name();
        String formatted = mCoupon.formattedTitle();
        String deal      = String.format("%s at %s", formatted, merchant);
        String body      = String.format("TikTok: %s! www.tiktok.com", deal);

        // present sms controller
        try {
            ShareUtilities.shareSMS(this, kIntentSMS, body);
        } catch (ActivityNotFoundException error) {
            String title   = getString(R.string.device_support);
            String message = getString(R.string.device_no_sms);
            Utilities.displaySimpleAlert(this, title, message);
        }
    }

    //-------------------------------------------------------------------------

    public void shareEmail()
    {
        // present the email controller
        String merchant  = mCoupon.merchant().name();
        String formatted = mCoupon.formattedTitle();
        String subject   = String.format(
            "TikTok: Checkout this amazing deal for %s!", merchant);
        String body      = String.format(
            "<h3>TikTok</h3>" +
            "<b>%s</b> at <b>%s</b>" +
            "<br><br>" +
            "I just scored this awesome deal with my " +
            "TikTok app. Sad you missed it? Don't be " +
            "a square... download the app and start " +
            "getting your own deals right now." +
            "<br><br>" +
            "<a href='http://www.tiktok.com'>Get your deal on!</a>",
            formatted, merchant);

        // present email controller
        try {
            ShareUtilities.shareEmail(this, kIntentEmail, subject, body);
        } catch (ActivityNotFoundException error) {
            String title   = getString(R.string.device_support);
            String message = getString(R.string.device_no_email);
            Utilities.displaySimpleAlert(this, title, message);
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Coupon                    mCoupon;
    private IconManager               mIconManager;
    private Handler                   mHandler = new Handler();
    private android.location.Location mLocation;
}

