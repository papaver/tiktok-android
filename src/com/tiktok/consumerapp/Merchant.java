//-----------------------------------------------------------------------------
// Merchant
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

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
        @JsonProperty("id")                  final long id,
        @JsonProperty("name")                final String name,
        @JsonProperty("category")            final String category,
        @JsonProperty("description")         final String details,
        @JsonProperty("icon_uid")            final int iconId,
        @JsonProperty("icon_url")            final String iconUrl,
        @JsonProperty("web_url")             final String websiteUrl,
        @JsonProperty("tw_handle")           final String twitterHandle,
        @JsonProperty("use_merchant_redeem") final boolean usesPin,
        @JsonProperty("last_update")         final long lastUpdated)
    {
        mId            = id;
        mName          = name;
        mCategory      = category;
        mDetails       = details;
        mIconId        = iconId;
        mIconUrl       = iconUrl;
        mWebsiteUrl    = websiteUrl;
        mTwitterHandle = twitterHandle;
        mUsesPin       = usesPin;
        mLastUpdated   = lastUpdated;
    }

    //-------------------------------------------------------------------------

    public Merchant(Cursor cursor)
    {
        mId            = cursor.getLong(cursor.getColumnIndex(MerchantTable.sKeyId));
        mName          = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyName));
        mCategory      = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyCategory));
        mDetails       = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyDetails));
        mIconId        = cursor.getInt(cursor.getColumnIndex(MerchantTable.sKeyIconId));
        mIconUrl       = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyIconUrl));
        mWebsiteUrl    = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyWebsiteUrl));
        mTwitterHandle = cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyTwitterHandle));
        mUsesPin       = cursor.getInt(cursor.getColumnIndex(MerchantTable.sKeyUsesPin)) == 1;
        mLastUpdated   = cursor.getLong(cursor.getColumnIndex(MerchantTable.sKeyLastUpdated));
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

    /**
     * @return Twitter hande of the merchant.
     */
    public String twitterHandle()
    {
        return mTwitterHandle;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Weather merchant used redemption pin.
     */
    public boolean usesPin()
    {
        return mUsesPin;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns icon data representing icon's id and url for download.
     */
    public IconManager.IconData iconData()
    {
        return new IconManager.IconData(iconId(), iconUrl());
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns date merchant data was last updated.
     */
    public long lastUpdatedRaw()
    {
        return mLastUpdated;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns date merchant data was last updated.
     */
    public Date lastUpdated()
    {
        return new Date(mLastUpdated * 1000);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    /**
     * @returns Mapping between database columns and values.
     */
    public ContentValues contentValues()
    {
        ContentValues values = new ContentValues();
        values.put(MerchantTable.sKeyId,            id());
        values.put(MerchantTable.sKeyName,          name());
        values.put(MerchantTable.sKeyCategory,      category());
        values.put(MerchantTable.sKeyDetails,       details());
        values.put(MerchantTable.sKeyIconId,        iconId());
        values.put(MerchantTable.sKeyIconUrl,       iconUrl());
        values.put(MerchantTable.sKeyWebsiteUrl,    websiteUrl());
        values.put(MerchantTable.sKeyTwitterHandle, twitterHandle());
        values.put(MerchantTable.sKeyUsesPin,       usesPin());
        values.put(MerchantTable.sKeyLastUpdated,   lastUpdatedRaw());
        return values;
    }

    //-------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");
        String string  =
            "Merchant {"        + newLine +
            "  id: "            + Long.toString(id()) + newLine +
            "  name: "          + name() + newLine +
            "  category: "      + category() + newLine +
            "  details: "       + details() + newLine +
            "  iconId: "        + Integer.toString(iconId()) + newLine +
            "  iconUrl: "       + iconUrl() + newLine +
            "  websiteUrl: "    + websiteUrl() + newLine +
            "  twitterHandle: " + twitterHandle() + newLine +
            "  usesPin: "       + Boolean.toString(usesPin()) + newLine +
            "  lastUpdated: "   + lastUpdated().toString() + newLine +
            "}";
        return string;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final long    mId;
    private final String  mName;
    private final String  mCategory;
    private final String  mDetails;
    private final int     mIconId;
    private final String  mIconUrl;
    private final String  mWebsiteUrl;
    private final String  mTwitterHandle;
    private final boolean mUsesPin;
    private final long    mLastUpdated;

}
