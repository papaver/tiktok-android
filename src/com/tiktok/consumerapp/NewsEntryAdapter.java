//-----------------------------------------------------------------------------
// NewsEntryAdapter
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

public final class NewsEntryAdapter extends ArrayAdapter<NewsEntry>
{
    
    public NewsEntryAdapter(final Context context, final int newsItemLayoutResource) 
    {
        super(context, 0);
        mNewsItemLayoutResource = newsItemLayoutResource;
    }

    //-------------------------------------------------------------------------

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent)
    {
        // we need to get the best view (reuse if possible) and then retrieve
        // its corresponding ViewHolder, which optimizes lookup efficiency
        final View view             = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final NewsEntry entry       = getItem(position);

        // setting the title view is strait forward
        viewHolder.titleView.setText(entry.getTitle());

        // add formating to subtitle view
        final String formattedSubTitle = String.format("By %s on %s",
            entry.getAuthor(),
            DateFormat.getDateInstance(DateFormat.SHORT).format(entry.getPostDate()));
        viewHolder.subTitleView.setText(formattedSubTitle);

        // setting image view is also simple
        viewHolder.imageView.setImageResource(entry.getIcon());

        return view;
    }

    //-------------------------------------------------------------------------

    /**
     * 
     */
    private View getWorkingView(final View convertView)
    {
        // the working view is just the convertView re-used if possible or 
        // inflated new if not possible
        View workingView = null;

        if (convertView == null) {
            final Context context         = getContext();
            final LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(mNewsItemLayoutResource, null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    //-------------------------------------------------------------------------

    /**
     * 
     */
    private ViewHolder getViewHolder(final View workingView)
    {
        // the viewHolder allows us to re-looking up view references  
        // since views are recycled, these references will never change
        final Object tag      = workingView.getTag();
        ViewHolder viewHolder = null;

        if ((tag == null) || !(tag instanceof ViewHolder)){
            viewHolder              = new ViewHolder();
            viewHolder.titleView    = (TextView)workingView.findViewById(R.id.news_entry_title);
            viewHolder.subTitleView = (TextView)workingView.findViewById(R.id.news_entry_subtitle);
            viewHolder.imageView    = (ImageView)workingView.findViewById(R.id.news_entry_icon);

            workingView.setTag(viewHolder);
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
        public TextView titleView;
        public TextView subTitleView;
        public ImageView imageView;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final int mNewsItemLayoutResource;

}
