//-----------------------------------------------------------------------------
// Location
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
public final class Location
{

    /**
     * Called when the location is created through json parsing.
     */
    @JsonCreator
    public Location(
        @JsonProperty("id")           final long id,
        @JsonProperty("name")         final String name,
        @JsonProperty("full_address") final String address,
        @JsonProperty("latitude")     final double latitude,
        @JsonProperty("longitude")    final double longitude,
        @JsonProperty("phone_number") final String phone,
        @JsonProperty("last_update")  final long lastUpdated)
    {
        mId          = id;
        mName        = name;
        mAddress     = address;
        mLatitude    = latitude;
        mLongitude   = longitude;
        mPhone       = phone;
        mLastUpdated = lastUpdated;
    }

    //-------------------------------------------------------------------------

    public Location(Cursor cursor)
    {
        mId          = cursor.getLong(cursor.getColumnIndex(LocationTable.sKeyId));
        mName        = cursor.getString(cursor.getColumnIndex(LocationTable.sKeyName));
        mAddress     = cursor.getString(cursor.getColumnIndex(LocationTable.sKeyAddress));
        mLatitude    = cursor.getDouble(cursor.getColumnIndex(LocationTable.sKeyLatitude));
        mLongitude   = cursor.getDouble(cursor.getColumnIndex(LocationTable.sKeyLongitude));
        mPhone       = cursor.getString(cursor.getColumnIndex(LocationTable.sKeyPhone));
        mLastUpdated = cursor.getLong(cursor.getColumnIndex(LocationTable.sKeyLastUpdated));
    }

    //-------------------------------------------------------------------------

    /**
     * @return Unique identifier for the location.
     */
    public long id()
    {
        return mId;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Name of the location.
     */
    public String name()
    {
        return mName;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Address of the location.
     */
    public String address()
    {
        return mAddress;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Latitude of locations location.
     */
    public double latitude()
    {
        return mLatitude;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Longitude of locations location.
     */
    public double longitude()
    {
        return mLongitude;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Phones number for location.
     */
    public String phone()
    {
        return mPhone;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns date location data was last updated.
     */
    public long lastUpdatedRaw()
    {
        return mLastUpdated;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns date location data was last updated.
     */
    public Date lastUpdated()
    {
        return new Date(mLastUpdated * 1000);
    }

    //-------------------------------------------------------------------------

    public android.location.Location getCoordinate()
    {
        android.location.Location coordinate = new android.location.Location("");
        coordinate.setLatitude(mLatitude);
        coordinate.setLongitude(mLongitude);
        return coordinate;
    }

    //-------------------------------------------------------------------------

    public String getCity()
    {
        try {
            return mAddress.split(", ")[1].replace(" ", "");
        } catch (Exception e) {
            return "";
        }
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
        values.put(LocationTable.sKeyId,          id());
        values.put(LocationTable.sKeyName,        name());
        values.put(LocationTable.sKeyAddress,     address());
        values.put(LocationTable.sKeyLatitude,    latitude());
        values.put(LocationTable.sKeyLongitude,   longitude());
        values.put(LocationTable.sKeyPhone,       phone());
        values.put(LocationTable.sKeyLastUpdated, lastUpdatedRaw());
        return values;
    }

    //-------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");
        String string  =
            "Location {"      + newLine +
            "  id: "          + Long.toString(id()) + newLine +
            "  name: "        + name() + newLine +
            "  address: "     + address() + newLine +
            "  latitude: "    + Double.toString(latitude()) + newLine +
            "  longitude: "   + Double.toString(longitude()) + newLine +
            "  phone: "       + phone() + newLine +
            "  lastUpdated: " + lastUpdated().toString() + newLine +
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
    private final long   mLastUpdated;

}
