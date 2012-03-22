//-----------------------------------------------------------------------------
// Merchant
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Merchant
{

    /**
     * Called when the merchant is created through json parsing.
     */
    @JsonCreator
    public Merchant(
        @JsonProperty("id")           final long id,
        @JsonProperty("name")         final String name,
        @JsonProperty("full_address") final String address,
        @JsonProperty("latitude")     final double latitude,
        @JsonProperty("longitude")    final double longitude,
        @JsonProperty("phone_number") final String phone,
        @JsonProperty("category")     final String category,
        @JsonProperty("description")  final String details,
        @JsonProperty("icon_uid")     final int iconId,
        @JsonProperty("icon_url")     final String iconUrl,
        @JsonProperty("web_url")      final String websiteUrl)
    {
        mId         = id;
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
     * @return Unique identifier for the merchant.
     */
    public long id()
    {
        return mId;
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
            "  id: "         + Long.toString(id()) + newLine +
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

    private final long   mId;
    private final String mName;
    private final String mAddress;
    private final double mLatitude;
    private final double mLongitude;
    private final String mPhone;
    private final String mCategory;
    private final String mDetails;
    private final int    mIconId;
    private final String mIconUrl;
    private final String mWebsiteUrl;
}
