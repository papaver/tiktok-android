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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class CouponAdapter extends CursorAdapter
{

    public CouponAdapter(final Context context, final Cursor cursor)
    {
        super(context, cursor);
    }

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
        long merchantId     = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
        String title        = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle));
        long endTimeSeconds = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime));
        Date endTime        = new Date(endTimeSeconds * 1000);

        // query merchant from cursor
        Merchant merchant = adapter.fetchMerchant(merchantId);

        // setting the title view is strait forward
        viewHolder.merchant.setText(merchant.name().toUpperCase());
        viewHolder.title.setText(capitalize(title));
        viewHolder.expiresTime.setText(getExpirationTime(endTime));

        // add formating to subtitle view
        final String formattedText = String.format("Offer expires at %s",
            DateFormat.getTimeInstance(DateFormat.SHORT).format(endTime));
        viewHolder.expiresAt.setText(formattedText);

        // setting image view is also simple
        //viewHolder.icon.setImageResource(0);

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
        public TextView  merchant;
        public TextView  title;
        public TextView  expiresAt;
        public TextView  expiresTime;
        public ImageView icon;
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
    // fields
    //-------------------------------------------------------------------------

}
