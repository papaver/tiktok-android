//-----------------------------------------------------------------------------
// Coupon
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Coupon 
{
    
    /**
     * Called when the activity is first created. 
     */
    public Coupon(final String title, final String text, 
                  final Date startTime, final Date endTime, final int icon)
    {
        mTitle     = title;
        mText      = text;
        mStartTime = startTime;
        mEndTime   = endTime;
        mIcon      = icon;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Title of coupon.
     */
    public String getTitle()
    {
        return mTitle;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Text description of the coupon.
     */
    public String getText()
    {
        return mText;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Starting time coupon is available for use.
     */
    public Date getStartTime()
    {
        return mStartTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Ending time coupon is expired.
     */
    public Date getEndTime()
    {
        return mEndTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Icon of this coupon.
     */
    public int getIcon() 
    {
        return mIcon;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final String mTitle;
    private final String mText;
    private final Date   mStartTime;
    private final Date   mEndTime;
    private final int    mIcon;

}
