//-----------------------------------------------------------------------------
// Coupon
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Coupon
{

    /**
     * Called when the coupon is created through json parsing.
     */
    @JsonCreator
    public Coupon(
        @JsonProperty("id")                   final long id,
        @JsonProperty("headline")             final String title,
        @JsonProperty("description")          final String details,
        @JsonProperty("icon_uid")             final int iconId,
        @JsonProperty("icon_url")             final String iconUrl,
        @JsonProperty("enable_time_in_tvsec") final long startTime,
        @JsonProperty("expiry_time_in_tvsec") final long endTime,
        @JsonProperty("barcode_number")       final String barcode,
        @JsonProperty("redeemed")             final boolean wasRedeemed,
        @JsonProperty("merchant")             final Merchant merchant)
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
        mMerchant    = merchant;
    }

    //-------------------------------------------------------------------------

    public Coupon(
        long id,
        String title,
        String details,
        int iconId,
        String iconUrl,
        long startTime,
        long endTime,
        String barcode,
        boolean isSoldOut,
        boolean wasRedeemed,
        Merchant merchant)
    {
        mId          = id;
        mTitle       = title;
        mDetails     = details;
        mIconId      = iconId;
        mIconUrl     = iconUrl;
        mStartTime   = startTime;
        mEndTime     = endTime;
        mBarcode     = barcode;
        mIsSoldOut   = isSoldOut;
        mWasRedeemed = wasRedeemed;
        mIsSoldOut   = isSoldOut;
        mMerchant    = merchant;
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

    /**
     * @return Returns merchant who owns this coupon.
     */
    public Merchant merchant()
    {
        return mMerchant;
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
            "  merchant: "  + merchant().toString() + newLine +
            "}";
        return string;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final long     mId;
    private final String   mTitle;
    private final String   mDetails;
    private final int      mIconId;
    private final String   mIconUrl;
    private final long     mStartTime;
    private final long     mEndTime;
    private final String   mBarcode;
    private       boolean  mWasRedeemed;
    private       boolean  mIsSoldOut;
    private final Merchant mMerchant;

}
