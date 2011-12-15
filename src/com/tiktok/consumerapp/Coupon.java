//-----------------------------------------------------------------------------
// Coupon
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import com.google.gson.annotations.SerializedName;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Coupon 
{
    
    /**
     * Called when the activity is first created. 
     */
    public Coupon(final long id, final String title, final String imageUrl,
                  final long startTime, final long endTime, final int icon)
    {
        mId        = id;
        mTitle     = title;
        mImageUrl  = imageUrl;
        mStartTime = startTime;
        mEndTime   = endTime;
        mIcon      = icon;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Unique identifier for the coupon.
     */
    public long getId()
    {
        return mId;
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
     * @return Url to image icon for coupon.
     */
    public String getImageUrl()
    {
        return mImageUrl;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon activation time in seconds since 1970.
     */
    public long getStartTimeRaw()
    {
        return mStartTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon activation time.
     */
    public Date getStartTime()
    {
        return new Date(mStartTime);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public long getEndTimeRaw()
    {
        return mEndTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public Date getEndTime()
    {
        return new Date(mEndTime);
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
    
    @SerializedName("id")
    private final long mId;

    @SerializedName("description")
    private final String mTitle;

    @SerializedName("image_url")
    private final String mImageUrl;

    @SerializedName("enable_time_in_tvsec")
    private final long mStartTime;

    @SerializedName("expiry_time_in_tvsec")
    private final long mEndTime;

    private final int mIcon;
}
