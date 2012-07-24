//-----------------------------------------------------------------------------
// CouponAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.utilities.UIUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class CouponAdapter extends CursorAdapter
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "CouponAdapter";

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public CouponAdapter(final Context context, final Cursor cursor)
    {
        super(context, cursor);
        mIconManager = new IconManager(context);
    }

    //-------------------------------------------------------------------------
    // cursoradapter overrides
    //-------------------------------------------------------------------------

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        // retrieve corresponding ViewHolder, which optimizes lookup efficiency
        final ViewHolder viewHolder = getViewHolder(view);

        // grab data from the cursor
        final long merchantId       = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
        final String title          = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle));
        final long endTimeSeconds   = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime));
        final long startTimeSeconds = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyStartTime));
        final Date endTime          = new Date(endTimeSeconds * 1000);
        final Date startTime        = new Date(startTimeSeconds * 1000);
        final int iconId            = cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIconId));
        final String iconUrl        = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyIconUrl));
        final boolean isSoldOut     = cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIsSoldOut)) == 1;
        final boolean wasRedeemed   = cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyWasRedeemed)) == 1;
        final boolean isRedeemable  = cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIsRedeemable)) == 1;

        // retrieve merchant
        Merchant merchant             = null;
        TikTokDatabaseAdapter adapter = null;
        try {
            adapter = new TikTokDatabaseAdapter(context);
            adapter.open();
            merchant = adapter.fetchMerchant(merchantId);
        } finally {
            adapter.close();
        }

        // reset alpha
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 1.0f);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        viewHolder.main.startAnimation(alpha);

        // set merchant / title / expires at
        viewHolder.merchant.setText(merchant.name().toUpperCase());
        viewHolder.title.setText(Coupon.formatTitle(title));
        viewHolder.expiresTime.setText(Coupon.getExpirationTime(endTime));

        // add formating to subtitle view
        final String formattedText = String.format("Offer expires at %s",
            DateFormat.getTimeInstance(DateFormat.SHORT).format(endTime));
        viewHolder.expiresAt.setText(formattedText);

        // update color
        GradientDrawable background = (GradientDrawable)viewHolder.linearLayout.getBackground();
        background.setColor(Coupon.getColor(endTime, startTime));

        // update sash
        viewHolder.sash.setVisibility(View.VISIBLE);
        if (wasRedeemed) {
            viewHolder.sash.setImageResource(R.drawable.redeemedsash);
        } else if (isSoldOut) {
            viewHolder.sash.setImageResource(R.drawable.soldoutsash);
        } else if (!isRedeemable) {
            viewHolder.sash.setImageResource(R.drawable.infosash);
        } else {
            viewHolder.sash.setVisibility(View.GONE);
        }

        // setup timer
        setupTimer(viewHolder, startTime, endTime);

        // set icon image
        setupIcon(viewHolder, iconId, iconUrl);
    }

    //-------------------------------------------------------------------------

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.coupon_entry_list_item, parent, false);
        return view;
    }

    //-------------------------------------------------------------------------

    private void setupTimer(final ViewHolder viewHolder,
        final Date startTime, final Date endTime)
    {
        // clear current timer from loop
        mHandler.removeCallbacks(viewHolder.timer);

        // no need to set timer if coupon is already expired
        if (Coupon.isExpired(endTime)) {
            expireCoupon(viewHolder, 0);
            return;
        }

        // setup new timer
        viewHolder.timer = new Runnable() {
           public void run() {

                // update expire time
                viewHolder.expiresTime.setText(Coupon.getExpirationTime(endTime));

                // update background color
                GradientDrawable background = (GradientDrawable)viewHolder.linearLayout.getBackground();
                background.setColor(Coupon.getColor(endTime, startTime));

                // fade out coupon if expired
                if (Coupon.isExpired(endTime)) {
                    expireCoupon(viewHolder, 200);
                } else {
                    mHandler.postDelayed(this, 1000);
                }
           }
        };

        // run timer
        mHandler.post(viewHolder.timer);
    }

    //-------------------------------------------------------------------------

    public void expireCoupon(ViewHolder viewHolder, long milliseconds)
    {
        // fade out coupon
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.6f);
        alpha.setDuration(milliseconds);
        alpha.setFillAfter(true);
        viewHolder.main.startAnimation(alpha);

        // update expire time
        viewHolder.expiresAt.setText("Offer is no longer available.");
    }

    //-------------------------------------------------------------------------

    private void setupIcon(final ViewHolder viewHolder,
        final int iconId, final String iconUrl)
    {
        IconManager.IconData iconData = new IconManager.IconData(iconId, iconUrl);

        // set image if available
        BitmapDrawable icon = mIconManager.getImage(iconData);
        if (icon != null) {
            viewHolder.icon.setImageBitmap(icon.getBitmap());

        // use activity indicator and load image from server
        } else {

            // set activity indicator
            viewHolder.icon.setImageResource(R.drawable.activity_indicator);
            viewHolder.icon.startAnimation(UIUtilities.getActivityIndicatorAnimation());

            // set tag to track view
            viewHolder.icon.setTag(iconUrl);

            // download icon from server
            mIconManager.requestImage(iconData, new IconManager.CompletionHandler() {

                public void onSuccess(final BitmapDrawable drawable) {
                    Log.i(kLogTag, String.format("Downloaded icon: %s", iconUrl));
                    viewHolder.icon.post(new Runnable() {
                        public void run() {
                            String tag = (String)viewHolder.icon.getTag();
                            if (viewHolder.icon.isShown() && iconUrl.equals(tag)) {
                                viewHolder.icon.setImageBitmap(drawable.getBitmap());
                                viewHolder.icon.clearAnimation();
                            }
                        }
                    });
                }

                public void onFailure() {
                    Log.e(kLogTag, String.format("Failed to download icon: %s", iconUrl));
                    String tag = (String)viewHolder.icon.getTag();
                    if (viewHolder.icon.isShown() && iconUrl.equals(tag)) {
                        setupIcon(viewHolder, iconId, iconUrl);
                    }
                }
            });
        }
    }

    //-------------------------------------------------------------------------
    // view holder
    //-------------------------------------------------------------------------

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    private static class ViewHolder
    {
        public FrameLayout  main;
        public TextView     merchant;
        public TextView     title;
        public TextView     expiresAt;
        public TextView     expiresTime;
        public LinearLayout linearLayout;
        public ImageView    icon;
        public Runnable     timer;
        public ImageView    sash;
    }

    //-------------------------------------------------------------------------

    private ViewHolder getViewHolder(final View view)
    {
        // create a new holder if the tag is empty
        final Object tag = view.getTag();
        if ((tag == null) || !(tag instanceof ViewHolder)) {
            ViewHolder holder   = new ViewHolder();
            holder.main         = (FrameLayout)view.findViewById(R.id.coupon);
            holder.merchant     = (TextView)view.findViewById(R.id.coupon_merchant);
            holder.title        = (TextView)view.findViewById(R.id.coupon_title);
            holder.expiresAt    = (TextView)view.findViewById(R.id.coupon_expires_at);
            holder.expiresTime  = (TextView)view.findViewById(R.id.coupon_expire);
            holder.linearLayout = (LinearLayout)view.findViewById(R.id.coupon_gradient);
            holder.icon         = (ImageView)view.findViewById(R.id.coupon_icon);
            holder.sash         = (ImageView)view.findViewById(R.id.coupon_sash);
            view.setTag(holder);
        }

        return (ViewHolder)view.getTag();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private IconManager mIconManager;
    private Handler     mHandler = new Handler();
}
