//-----------------------------------------------------------------------------
// ItemizedOverlay
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.map;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.OverlayItem;

import com.tiktok.consumerapp.R;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class ItemizedOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem>
{
    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    public ItemizedOverlay(Drawable defaultMarker, Context context)
    {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    //-------------------------------------------------------------------------

    public ItemizedOverlay(Context context)
    {
        this(context.getResources().getDrawable(R.drawable.pin), context);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public void addOverlay(OverlayItem overlay)
    {
        mOverlays.add(overlay);
        populate();
    }

    //-------------------------------------------------------------------------
    // ItemizedOverlay<OverlayItem>
    //-------------------------------------------------------------------------

    @Override
    protected OverlayItem createItem(int index)
    {
        return mOverlays.get(index);
    }

    //-------------------------------------------------------------------------

    @Override
    public int size()
    {
        return mOverlays.size();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private Context           mContext;
    private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
}
