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

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import com.tiktok.consumerapp.R;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class ItemizedOverlay extends BalloonItemizedOverlay<OverlayItem>
{
    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    public ItemizedOverlay(Drawable defaultMarker, MapView mapView)
    {
        super(boundCenterBottom(defaultMarker), mapView);
        mContext = mapView.getContext();
    }

    //-------------------------------------------------------------------------

    public ItemizedOverlay(MapView mapView)
    {
        this(mapView.getContext().getResources().getDrawable(R.drawable.pin), mapView);
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

    public void clear()
    {
        mOverlays.clear();
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

    protected Context           mContext;
    protected List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
}
