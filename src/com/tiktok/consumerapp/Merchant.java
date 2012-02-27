//-----------------------------------------------------------------------------
// Merchant
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import com.google.gson.annotations.SerializedName;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Merchant
{

    /**
     * Called when the merchant is first created.
     */
    public Merchant(final String name, final String address,
                    final double latitude, final double longitude,
                    final String phone, final String category,
                    final String details, final int iconId, final String iconUrl,
                    final String websiteUrl)
    {
        mName       = name;
        mAddress    = address;
        mLatitude   = latitude;
        mLongitude  = longitude;
        mPhone      = phone;
        mCategory   = category;
        mDetails    = details;
        mIconId     = iconId;
        mIconUrl    = iconUrl;
        mWebsiteUrl = websiteUrl;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Name of the merchant.
     */
    public String name()
    {
        return mName;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Address of the merchant.
     */
    public String address()
    {
        return mAddress;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Latitude of merchants location.
     */
    public double latitude()
    {
        return mLatitude;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Longitude of merchants location.
     */
    public double longitude()
    {
        return mLongitude;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Phones number for merchant.
     */
    public String phone()
    {
        return mPhone;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Category merchant belongs to.
     */
    public String category()
    {
        return mCategory;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Details for merchant.
     */
    public String details()
    {
        return mDetails;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Id of icon used by merchant.
     */
    public int iconId()
    {
        return mIconId;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Url of icon used by merchant.
     */
    public String iconUrl()
    {
        return mIconUrl;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Url of merchants website.
     */
    public String websiteUrl()
    {
        return mWebsiteUrl;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");
        String string  =
            "Merchant {"     + newLine +
            "  name: "       + name() + newLine +
            "  address: "    + address() + newLine +
            "  latitude: "   + Double.toString(latitude()) + newLine +
            "  longitude: "  + Double.toString(longitude()) + newLine +
            "  phone: "      + phone() + newLine +
            "  category: "   + category() + newLine +
            "  details: "    + details() + newLine +
            "  iconId: "     + Integer.toString(iconId()) + newLine +
            "  iconUrl: "    + iconUrl() + newLine +
            "  websiteUrl: " + websiteUrl() + newLine +
            "}";
        return string;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    @SerializedName("name")
    private final String mName;

    @SerializedName("full_address")
    private final String mAddress;

    @SerializedName("latitude")
    private final double mLatitude;

    @SerializedName("longitude")
    private final double mLongitude;

    @SerializedName("phone_number")
    private final String mPhone;

    @SerializedName("category")
    private final String mCategory;

    @SerializedName("description")
    private final String mDetails;

    @SerializedName("icon_uid")
    private final int mIconId;

    @SerializedName("icon_url")
    private final String mIconUrl;

    @SerializedName("web_url")
    private final String mWebsiteUrl;
}
