//-----------------------------------------------------------------------------
// Coupon
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.lang.Double;
import java.util.Date;
import java.util.List;

import android.graphics.Color;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.tiktok.consumerapp.utilities.ColorUtilities;
import com.tiktok.consumerapp.utilities.LocationUtilities;
import com.tiktok.consumerapp.utilities.TextUtilities;

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
        @JsonProperty("is_redeemable")        final boolean isRedeemable,
        @JsonProperty("merchant")             final Merchant merchant,
        @JsonProperty("locations")            final List<Location> locations)
    {
        mId           = id;
        mTitle        = title;
        mDetails      = details;
        mIconId       = iconId;
        mIconUrl      = iconUrl;
        mStartTime    = startTime;
        mEndTime      = endTime;
        mBarcode      = barcode;
        mIsSoldOut    = false;
        mWasRedeemed  = wasRedeemed;
        mIsRedeemable = isRedeemable;
        mMerchant     = merchant;
        mLocations    = locations;
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
        boolean isRedeemable,
        List<Location> locations,
        Merchant merchant)
    {
        mId           = id;
        mTitle        = title;
        mDetails      = details;
        mIconId       = iconId;
        mIconUrl      = iconUrl;
        mStartTime    = startTime;
        mEndTime      = endTime;
        mBarcode      = barcode;
        mIsSoldOut    = isSoldOut;
        mWasRedeemed  = wasRedeemed;
        mIsRedeemable = isRedeemable;
        mLocations    = locations;
        mMerchant     = merchant;
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
     * @return Title of coupon w/ formatting.
     */
    public String formattedTitle()
    {
        return Coupon.formatTitle(mTitle);
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
     * @return Description of coupon w/ formatting.
     */
    public String formattedDetails()
    {
        return Coupon.formatDetails(mDetails);
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
        // [moiz] fuck me fix this shit
        return mStartTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon activation time.
     */
    public Date startTime()
    {
        return new Date(mStartTime * 1000);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public long endTimeRaw()
    {
        // [moiz] fuck me fix this shit
        return mEndTime;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Coupon expiration time in seconds since 1970.
     */
    public Date endTime()
    {
        return new Date(mEndTime * 1000);
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
     * @return Returns weather or not coupon is redeemedable.
     */
    public boolean isRedeemable()
    {
        return mIsRedeemable;
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

    /**
     * @return Returns locations this coupon is valid at.
     */
    public List<Location> locations()
    {
        return mLocations;
    }

    //-------------------------------------------------------------------------

    public Location getClosestLocation(android.location.Location coordinate)
    {
        // early out if only one location
        if (mLocations.size() == 1) {
            return mLocations.get(0);
        }

        // loop through all the locations and find the closest
        double minDistance   = Double.MAX_VALUE;
        Location minLocation = null;
        for (Location location : mLocations) {
            double distance =
                LocationUtilities.distanceBetweenLocations(
                    location.getCoordinate(), coordinate);
            if (distance < minDistance) {
                minDistance = distance;
                minLocation = location;
            }
        }

        return minLocation;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns comma delimited string of location ids.
     */
    public String locationIdsStr()
    {
        String ids = "";
        for (Location location : mLocations) {
            ids += Long.toString(location.id()) + ",";
        }
        return ids.replaceAll(",$", "");
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
    // helper methods
    //-------------------------------------------------------------------------

    public static String formatTitle(String title)
    {
        String formatedTitle = TextUtilities.capitalizeWords(title)
            .replace("Free", "FREE")
            .replace("Entree", "Entr\u00E8e");
        return formatedTitle;
    }

    //-------------------------------------------------------------------------

    public static String formatDetails(String details)
    {
        String terms = "TikTok Terms and Conditions:\nwww.tiktok.com/terms";
        String formatedDetails = String.format("%s\n\n%s", details, terms)
            .replace("entree", "entr\u00E8e")
            .replace("Entree", "Entr\u00E8e");
        return formatedDetails;
    }

    //-------------------------------------------------------------------------

    public static boolean isExpired(Date time)
    {
        return time.before(new Date());
    }

    //-------------------------------------------------------------------------

    public static String getExpirationTime(Date time)
    {
        // return the default color if expired
        if (Coupon.isExpired(time)) return "TIME'S UP!";

        // calculate interval value
        long secondsLeft  = (time.getTime() - new Date().getTime()) / 1000;
        float minutesLeft = (float)secondsLeft / 60.0f;

        // update the coupon expire timer
        String timer = String.format("%02d:%02d:%02d",
            (int)minutesLeft / 60, (int)minutesLeft % 60, (int)secondsLeft % 60);
        return timer;
    }

    //-------------------------------------------------------------------------

    public static int getColor(Date endTime, Date startTime)
    {
        final int sixty_minutes  = 60 * 60 * 1000;
        final int thirty_minutes = 30 * 60 * 1000;
        final int five_minutes   =  5 * 60 * 1000;

        // return the default color if expired
        if (Coupon.isExpired(endTime)) return ColorUtilities.kTok;

        // calculate seconds left till expiration
        float secondsLeft = (float)(endTime.getTime() - new Date().getTime());

        // green  should be solid until 60 minutes
        // yellow should be solid at 30 minutes
        // orange should be solid at  5 minutes
        float t = 0.0f;
        if (secondsLeft > sixty_minutes) {
            t = 0.0f;
        } else if (secondsLeft > thirty_minutes) {
            t = (secondsLeft - thirty_minutes) / thirty_minutes;
            t = 0.00f + (1.0f - t) * 0.33f;
        } else if (secondsLeft > five_minutes) {
            t = (secondsLeft - five_minutes) / (thirty_minutes - five_minutes);
            t = 0.33f + (1.0f - t) * 0.33f;
        } else {
            t = (secondsLeft / five_minutes);
            t = 0.66f + (1.0f - t) * 0.33f;
        }

        // colors to transition between
        final int tik    = ColorUtilities.kTik;
        final int yellow = Color.YELLOW;
        final int orange = ColorUtilities.kOrange;
        final int tok    = ColorUtilities.kTok;

        // class to make computations cleaner
        class ColorTable
        {
            public ColorTable(float t, float offset, int start, int end)
            {
                this.t      = t;
                this.offset = offset;
                this.start  = start;
                this.end    = end;
            }

            public float t, offset;
            public int start, end;
        }

        ColorTable sColorTable[] = {
            new ColorTable(0.33f, 0.00f, tik,    yellow),
            new ColorTable(0.66f, 0.33f, yellow, orange),
            new ColorTable(1.00f, 0.66f, orange, tok   )
        };

        // return the interpolated color
        int index = 0;
        for (; index < sColorTable.length; ++index) {
            if (t > sColorTable[index].t) continue;

            int start  = sColorTable[index].start;
            int end    = sColorTable[index].end;
            float newT = (t - sColorTable[index].offset) / 0.33f;
            int color  = ColorUtilities.interpolateColor(start, end, newT);
            return color;
        }

        // in case something went wrong...
        return Color.BLACK;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public String locationsStr()
    {
        String newLine = System.getProperty("line.separator");
        String string  = "{" + newLine;
        for (Location location : locations()) {
            string += location.toString() + newLine;
        }
        string += "}";
        return string;
    }

    //-------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");
        String string  =
            "Coupon {"          + newLine +
            "  id: "            + Long.toString(id()) + newLine +
            "  title: "         + title() + newLine +
            "  details: "       + details() + newLine +
            "  iconId: "        + Integer.toString(iconId()) + newLine +
            "  iconUrl: "       + iconUrl() + newLine +
            "  startTime: "     + startTime().toString() + newLine +
            "  endTime: "       + endTime().toString() + newLine +
            "  barcode: "       + barcode() + newLine +
            "  isSoldOut: "     + Boolean.toString(isSoldOut()) + newLine +
            "  redeemed: "      + Boolean.toString(wasRedeemed()) + newLine +
            "  is_redeemable: " + Boolean.toString(isRedeemable()) + newLine +
            "  merchant: "      + merchant().toString() + newLine +
            "  locations: "     + locationsStr() + newLine +
            "}";
        return string;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final long           mId;
    private final String         mTitle;
    private final String         mDetails;
    private final int            mIconId;
    private final String         mIconUrl;
    private final long           mStartTime;
    private final long           mEndTime;
    private final String         mBarcode;
    private       boolean        mIsSoldOut;
    private       boolean        mWasRedeemed;
    private final boolean        mIsRedeemable;
    private final List<Location> mLocations;
    private final Merchant       mMerchant;

}
