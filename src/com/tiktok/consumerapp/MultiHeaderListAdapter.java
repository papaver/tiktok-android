//-----------------------------------------------------------------------------
// MultiHeaderListAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Map;
import java.util.LinkedHashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class MultiHeaderListAdapter extends BaseAdapter
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final int kTypeSectionHeader = 0;

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public MultiHeaderListAdapter(final Context context)
    {
        mHeaders = new ArrayAdapter<String>(context, R.layout.list_header);
    }

    //-------------------------------------------------------------------------
    // adapter overrides
    //-------------------------------------------------------------------------

    public void addSection(String section, Adapter adapter)
    {
        mHeaders.add(section);
        mSections.put(section, adapter);
    }

    //-------------------------------------------------------------------------

    public Object getItem(int position)
    {
        for (Object section : mSections.keySet())
        {
            Adapter adapter = mSections.get(section);
            int size        = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) return section;
            if (position < size) return adapter.getItem(position - 1);

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    //-------------------------------------------------------------------------

    public int getCount()
    {
        // total together all sections, plus one for each section header
        int total = 0;
        for (Adapter adapter : mSections.values()) {
            total += adapter.getCount() + 1;
        }
        return total;
    }

    //-------------------------------------------------------------------------

    public int getViewTypeCount()
    {
        // assume that headers count as one, then total all sections
        int total = 1;
        for (Adapter adapter : mSections.values()) {
            total += adapter.getViewTypeCount();
        }
        return total;
    }

    //-------------------------------------------------------------------------

    public int getItemViewType(int position)
    {
        int type = 1;
        for (Object section : mSections.keySet()) {
            Adapter adapter = mSections.get(section);
            int size        = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) return kTypeSectionHeader;
            if (position < size) {
                return type + adapter.getItemViewType(position - 1);
            }

            // otherwise jump into next section
            position -= size;
            type     += adapter.getViewTypeCount();
        }
        return -1;
    }

    //-------------------------------------------------------------------------

    public boolean areAllItemsSelectable()
    {
        return false;
    }

    //-------------------------------------------------------------------------

    public boolean isEnabled(int position)
    {
        return (getItemViewType(position) != kTypeSectionHeader);
    }

    //-------------------------------------------------------------------------

    public View getView(int position, View convertView, ViewGroup parent)
    {
        int sectionnum = 0;
        for (Object section : mSections.keySet()) {
            Adapter adapter = mSections.get(section);
            int size        = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                return mHeaders.getView(sectionnum, convertView, parent);
            }
            if (position < size) {
                return adapter.getView(position - 1, convertView, parent);
            }

            // otherwise jump into next section
            position -= size;
            ++sectionnum;
        }
        return null;
    }

    //-------------------------------------------------------------------------

    public long getItemId(int position)
    {
        return position;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    public final Map<String,Adapter>  mSections = new LinkedHashMap<String, Adapter>();
    public final ArrayAdapter<String> mHeaders;
}
