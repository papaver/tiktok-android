//-----------------------------------------------------------------------------
// CouponAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.text.DateFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class CouponAdapter extends ArrayAdapter<Coupon>
{
    
    public CouponAdapter(final Context context, final int couponLayoutResource) 
    {
        super(context, 0);
        mCouponLayoutResource = couponLayoutResource;
    }

    //-------------------------------------------------------------------------

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent)
    {
        // we need to get the best view (reuse if possible) and then retrieve
        // its corresponding ViewHolder, which optimizes lookup efficiency
        final View view             = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final Coupon entry          = getItem(position);

        // setting the title view is strait forward
        viewHolder.titleView.setText(entry.getTitle());

        // add formating to subtitle view
        final String formattedText = String.format("Expires on %s",
            DateFormat.getDateInstance(DateFormat.SHORT).format(entry.getEndTime()));
        viewHolder.textView.setText(formattedText);

        // setting image view is also simple
        viewHolder.imageView.setImageResource(entry.getIcon());

        return view;
    }

    //-------------------------------------------------------------------------

    /**
     * The working view is just the convertView re-used if possible or 
     * inflated new if not possible
     */
    private View getWorkingView(final View convertView)
    {
        View workingView = null;
        if (convertView == null) {
            final Context context         = getContext();
            final LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(mCouponLayoutResource, null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    //-------------------------------------------------------------------------

    /**
     * The viewHolder allows us to re-looking up view references;
     * since views are recycled, these references will never change
     */
    private ViewHolder getViewHolder(final View couponView)
    {
        final Object tag      = couponView.getTag();
        ViewHolder viewHolder = null;

        if ((tag == null) || !(tag instanceof ViewHolder)) {
            viewHolder           = new ViewHolder();
            viewHolder.titleView = (TextView)couponView.findViewById(R.id.coupon_entry_title);
            viewHolder.textView  = (TextView)couponView.findViewById(R.id.coupon_entry_text);
            viewHolder.imageView = (ImageView)couponView.findViewById(R.id.coupon_entry_icon);

            couponView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)tag;
        }

        return viewHolder;
    }

    //-------------------------------------------------------------------------

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    private static class ViewHolder
    {
        public TextView  titleView;
        public TextView  textView;
        public ImageView imageView;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final int mCouponLayoutResource;

}
