//-----------------------------------------------------------------------------
// BitmapDrawable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.drawable;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class BitmapDrawable extends Drawable
{
    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public BitmapDrawable(Bitmap bitmap)
    {
        mBitmap = bitmap;
    }

    //-------------------------------------------------------------------------
    // properties
    //-------------------------------------------------------------------------

    public Bitmap getBitmap()
    {
        return mBitmap;
    }

    //-------------------------------------------------------------------------
    // drawable overrides
    //-------------------------------------------------------------------------

    @Override
    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
    }

    //-------------------------------------------------------------------------

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSLUCENT;
    }

    //-------------------------------------------------------------------------

    @Override
    public void setAlpha(int alpha)
    {
    }

    //-------------------------------------------------------------------------

    @Override
    public void setColorFilter(ColorFilter colorFilter)
    {
    }

    //-------------------------------------------------------------------------

    @Override
    public int getIntrinsicWidth()
    {
        return mBitmap.getWidth();
    }

    //-------------------------------------------------------------------------

    @Override
    public int getIntrinsicHeight()
    {
        return mBitmap.getHeight();
    }

    //-------------------------------------------------------------------------

    @Override
    public int getMinimumWidth()
    {
        return mBitmap.getWidth();
    }

    //-------------------------------------------------------------------------

    @Override
    public int getMinimumHeight()
    {
        return mBitmap.getHeight();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final Bitmap mBitmap;

}
