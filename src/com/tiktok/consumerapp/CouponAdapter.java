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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.utilities.UIDefaults;

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

        // retrieve merchant
        TikTokDatabaseAdapter adapter = null;
        try {
            adapter = new TikTokDatabaseAdapter(context);
            adapter.open();
        } catch (Exception e) {
        }

        // grab data from the cursor
        final long merchantId       = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
        final String title          = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle));
        final long endTimeSeconds   = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime));
        final long startTimeSeconds = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyStartTime));
        final Date endTime          = new Date(endTimeSeconds * 1000);
        final Date startTime        = new Date(startTimeSeconds * 1000);
        final int iconId            = cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIconId));
        final String iconUrl        = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyIconUrl));

        // query merchant from cursor
        final Merchant merchant = adapter.fetchMerchant(merchantId);

        // setting the title view is strait forward
        viewHolder.merchant.setText(merchant.name().toUpperCase());
        viewHolder.title.setText(capitalize(title));
        viewHolder.expiresTime.setText(getExpirationTime(endTime));

        // add formating to subtitle view
        final String formattedText = String.format("Offer expires at %s",
            DateFormat.getTimeInstance(DateFormat.SHORT).format(endTime));
        viewHolder.expiresAt.setText(formattedText);

        // update color
        GradientDrawable background = (GradientDrawable)viewHolder.linearLayout.getBackground();
        background.setColor(getColor(endTime, startTime));

        // set icon image
        IconManager.IconData iconData = mIconManager.new IconData(iconId, iconUrl);
        BitmapDrawable icon = mIconManager.getImage(iconData);
        if (icon != null) {
            viewHolder.icon.setImageBitmap(icon.getBitmap());

        // use activity indicator and load image from server
        } else {

            // set activity indicator
            viewHolder.icon.setImageResource(R.drawable.activity_indicator);

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

            // and apply it to your imageview
            viewHolder.icon.startAnimation(rotation);

            // download icon from server
            mIconManager.requestImage(iconData, new IconManager.CompletionHandler() {

                public void onSuccess(final BitmapDrawable drawable) {
                    Log.i(kLogTag, String.format("Downloaded icon: %s", iconUrl));
                    viewHolder.icon.post(new Runnable() {
                        public void run() {
                            viewHolder.icon.setImageBitmap(drawable.getBitmap());
                            viewHolder.icon.clearAnimation();
                        }
                    });
                }

                public void onFailure() {
                    Log.e(kLogTag, String.format("Failed to download icon: %s", iconUrl));
                }
            });
        }

        // cleanup
        adapter.close();
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
    // helper methods
    //-------------------------------------------------------------------------

    /**
     * The viewHolder allows us to re-looking up view references;
     * since views are recycled, these references will never change
     */
    private ViewHolder getViewHolder(final View view)
    {
        // create a new holder if the tag is empty
        final Object tag = view.getTag();
        if ((tag == null) || !(tag instanceof ViewHolder)) {
            ViewHolder holder  = new ViewHolder();
            holder.merchant    = (TextView)view.findViewById(R.id.coupon_merchant);
            holder.title       = (TextView)view.findViewById(R.id.coupon_title);
            holder.expiresAt   = (TextView)view.findViewById(R.id.coupon_expires_at);
            holder.expiresTime = (TextView)view.findViewById(R.id.coupon_expire);
            holder.linearLayout    = (LinearLayout)view.findViewById(R.id.coupon_gradient);
            holder.icon        = (ImageView)view.findViewById(R.id.coupon_icon);
            view.setTag(holder);
        }

        return (ViewHolder)view.getTag();
    }

    //-------------------------------------------------------------------------

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    private static class ViewHolder
    {
        public TextView     merchant;
        public TextView     title;
        public TextView     expiresAt;
        public TextView     expiresTime;
        public LinearLayout linearLayout;
        public ImageView    icon;
    }

    //-------------------------------------------------------------------------

    private String capitalize(String text)
    {
        final StringBuilder result = new StringBuilder(text.length());

        String[] words = text.split("\\s");
        for(int index = 0; index < words.length; ++index) {
            if (index > 0) result.append(" ");
            result.append(Character.toUpperCase(words[index].charAt(0)))
                  .append(words[index].substring(1).toLowerCase());
        }

        return result.toString();
    }

    //-------------------------------------------------------------------------

    public boolean isExpired(Date time)
    {
        return time.before(new Date());
    }

    //-------------------------------------------------------------------------

    public String getExpirationTime(Date time)
    {
        // return the default color if expired
        if (isExpired(time)) return "00:00:00";

        // calculate interval value
        long secondsLeft  = (time.getTime() - new Date().getTime()) / 1000;
        float minutesLeft = (float)secondsLeft / 60.0f;

        // update the coupon expire timer
        String timer = String.format("%02d:%02d:%02d",
            (int)minutesLeft / 60, (int)minutesLeft % 60, (int)secondsLeft % 60);
        return timer;
    }

    //-------------------------------------------------------------------------

    public int getColor(Date endTime, Date startTime)
    {
        final int sixty_minutes  = 60 * 60;
        final int thirty_minutes = 30 * 60;
        final int five_minutes   =  5 * 60;

        // return the default color if expired
        if (isExpired(endTime)) return UIDefaults.getTokColor();

        // calculate interp value
        long secondsLeft  = endTime.getTime() - new Date().getTime();
        long totalSeconds = endTime.getTime() - startTime.getTime();

        // green  should be solid until 60 minutes
        // yellow should be solid at 30 minutes
        // orange should be solid at  5 minutes
        float t = 0.0f;
        if (secondsLeft > sixty_minutes) {
            t = 0.0f;
        } else if (secondsLeft > thirty_minutes) {
            t = (secondsLeft - thirty_minutes) / thirty_minutes;
            t = 0.00f + (1.0f - t) * 0.33f;
        } else if (secondsLeft > five_minutes) {
            t = (secondsLeft - five_minutes) / (thirty_minutes - five_minutes);
            t = 0.33f + (1.0f - t) * 0.33f;
        } else {
            t = (secondsLeft / five_minutes);
            t = 0.66f + (1.0f - t) * 0.33f;
        }

        // colors to transition between
        final int tik    = UIDefaults.getTikColor();
        final int yellow = Color.YELLOW;
        final int orange = Color.argb(255, 255, 127, 0);
        final int tok    = UIDefaults.getTokColor();

        // class to make computations cleaner
        class ColorTable
        {
            public ColorTable(float t, float offset, int start, int end)
            {
                this.t      = t;
                this.offset = offset;
                this.start  = start;
                this.end    = end;
            }

            public float t, offset;
            public int start, end;
        }

        ColorTable sColorTable[] = {
            new ColorTable(0.33f, 0.00f, tik,    yellow),
            new ColorTable(0.66f, 0.33f, yellow, orange),
            new ColorTable(1.00f, 0.66f, orange, tok   )
        };

        // return the interpolated color
        int index = 0;
        for (; index < sColorTable.length; ++index) {
            if (t > sColorTable[index].t) continue;

            int start  = sColorTable[index].start;
            int end    = sColorTable[index].end;
            float newT = (t - sColorTable[index].offset) / 0.33f;
            return colorByInterpolatingToColor(start, end, newT);
        }

        // in case something went wrong...
        return Color.BLACK;
    }

    //-------------------------------------------------------------------------

    public int colorByInterpolatingToColor(int colorA, int colorB, float fraction)
    {
        int redA   = Color.red(colorA);
        int greenA = Color.green(colorA);
        int blueA  = Color.blue(colorA);
        int alphaA = Color.alpha(colorA);

        int redB   = Color.red(colorB);
        int greenB = Color.green(colorB);
        int blueB  = Color.blue(colorB);
        int alphaB = Color.alpha(colorB);

        int red   = redA   + (int)(fraction * (float)(redB - redA));
        int green = greenA + (int)(fraction * (float)(greenB - greenA));
        int blue  = blueA  + (int)(fraction * (float)(blueB - blueA));
        int alpha = alphaA + (int)(fraction * (float)(alphaB - alphaA));

        red   = Math.min(red,   255);
        green = Math.min(green, 255);
        blue  = Math.min(blue,  255);
        alpha = Math.min(alpha, 255);

        return Color.argb(alpha, red, green, blue);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private IconManager mIconManager;
}
