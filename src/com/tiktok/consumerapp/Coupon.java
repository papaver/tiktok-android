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
     * Called when the coupon is first created.
     */
    public Coupon(final long id, final String title, final String details,
                  final int iconId, final String iconUrl,
                  final long startTime, final long endTime,
                  final String barcode, final boolean wasRedeemed)
    {
        mId          = id;
        mTitle       = title;
        mDetails     = details;
        mIconId      = iconId;
        mIconUrl     = iconUrl;
        mStartTime   = startTime;
        mEndTime     = endTime;
        mBarcode     = barcode;
        mWasRedeemed = wasRedeemed;
        mIsSoldOut   = false;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Unique identifier for the coupon.
     */
    public long id()
    {
        return mId;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Title of coupon.
     */
    public String title()
    {
        return mTitle;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Description of coupon.
     */
    public String details()
    {
        return mDetails;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Global id of icon.
     */
    public int iconId()
    {
        return mIconId;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Url of icon.
     */
    public String iconUrl()
    {
        return mIconUrl;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon activation time in seconds since 1970.
     */
    public long startTimeRaw()
    {
        return mStartTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon activation time.
     */
    public Date startTime()
    {
        return new Date(mStartTime);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public long endTimeRaw()
    {
        return mEndTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public Date endTime()
    {
        return new Date(mEndTime);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Barcode for coupon.
     */
    public String barcode()
    {
        return mBarcode;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns weather or not coupon was redeemed.
     */
    public boolean wasRedeemed()
    {
        return mWasRedeemed;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Redeems the coupon.
     */
    public void redeem()
    {
        mWasRedeemed = true;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns weather or not coupon was redeemed.
     */
    public boolean isSoldOut()
    {
        return mIsSoldOut;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Sell out coupon.
     */
    public void sellOut()
    {
        mIsSoldOut = true;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");
        String string  =
            "Coupon {"      + newLine +
            "  id: "        + Long.toString(id()) + newLine +
            "  title: "     + title() + newLine +
            "  details: "   + details() + newLine +
            "  iconId: "    + Integer.toString(iconId()) + newLine +
            "  iconUrl: "   + iconUrl() + newLine +
            "  startTime: " + startTime().toString() + newLine +
            "  endTime: "   + endTime().toString() + newLine +
            "  barcode: "   + barcode() + newLine +
            "  redeemed: "  + Boolean.toString(wasRedeemed()) + newLine +
            "  isSoldOut: " + Boolean.toString(isSoldOut()) + newLine +
            "}";
        return string;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
    
    @SerializedName("id")
    private final long mId;

    @SerializedName("headline")
    private final String mTitle;

    @SerializedName("description")
    private final String mDetails;

    @SerializedName("icon_uid")
    private final int mIconId;

    @SerializedName("icon_url")
    private final String mIconUrl;

    @SerializedName("enable_time_in_tvsec")
    private final long mStartTime;

    @SerializedName("expiry_time_in_tvsec")
    private final long mEndTime;

    @SerializedName("barcode_number")
    private final String mBarcode;

    @SerializedName("redeemed")
    private boolean mWasRedeemed;

    private boolean mIsSoldOut;
}
