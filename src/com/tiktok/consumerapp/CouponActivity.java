//-----------------------------------------------------------------------------
// CouponActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
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

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponActivity extends MapActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "CouponActivity";

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
        Log.i(kLogTag, "Share Facebook");
    }

    //-------------------------------------------------------------------------

    public void onClickMore(View view)
    {
        Log.i(kLogTag, "Share More");
    }

    //-------------------------------------------------------------------------

    public void onClickRedeem(View view)
    {
        Log.i(kLogTag, "Redeemed");

        // redeem the coupon with the server
        TikTokApi api = new TikTokApi(this);
        TikTokApiResponse response =
            api.updateCoupon(mCoupon.id(), TikTokApi.CouponAttribute.kRedeem);

        // validate the registration status
        if (response.getStatus().equals(TikTokApi.kTikTokApiStatusOkay)) {

            // redeem the coupon
            mCoupon.redeem();
            TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(this);
            adapter.open();
            adapter.updateCoupon(mCoupon);

            // update the banner
            updateBanner(CouponState.kActive);

            // cleanup
            adapter.close();

        // alert user of a problem
        } else if (response.getStatus().equals(TikTokApi.kTikTokApiStatusForbidden)) {
            String title   = "Redeem";
            String message = response.getError();
            Utilities.displaySimpleAlert(this, title, message);

            // [moiz] sync coupons to make sure status' are up to date
        }

        // [moiz] should be able to check for network errors
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
    // help functions
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
            iconView.startAnimation(getActivityAnimation());

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

    private RotateAnimation getActivityAnimation()
    {
        RotateAnimation rotation = new RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
        rotation.setDuration(360 * 4);
        rotation.setInterpolator(new LinearInterpolator());
        rotation.setRepeatMode(Animation.RESTART);
        rotation.setRepeatCount(Animation.INFINITE);
        return rotation;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Coupon      mCoupon;
    private IconManager mIconManager;
    private Handler     mHandler = new Handler();
}

