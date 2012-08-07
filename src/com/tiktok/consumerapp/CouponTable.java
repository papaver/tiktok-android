//-----------------------------------------------------------------------------
// CouponTable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponTable
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final Uri kContentUri =
        Uri.parse("content://" + Constants.kAuthority + "/coupons");

    public static final String kContentType =
        "com.tiktok.cursor.dir/com.tiktok.coupon";
    public static final String kContentTypeItem =
        "com.tiktok.cursor.item/com.tiktok.coupon";

    //-------------------------------------------------------------------------
    // table management
    //-------------------------------------------------------------------------

    /**
     * Runs if table needs to be created.
     */
    public static void onCreate(SQLiteDatabase database)
    {
        database.execSQL(getCreateSQL());
    }

    //-------------------------------------------------------------------------

    /**
     * Runs if table version has changed and the database needs to be upgraded.
     */
    public static void onUpgrade(SQLiteDatabase database,
                                 int oldVersion, int newVersion)
    {
        // nothing to do if the versions match
        if (oldVersion == newVersion) return;

        Log.i(CouponTable.class.getName(), String.format(
            "Upgrading database from version %d to %d ", oldVersion, newVersion));

        // throw data away and recreate the table
        dropTable(database);
        onCreate(database);
    }

    //-------------------------------------------------------------------------

    /**
     * Drop the given table from the database.
     */
    public static void dropTable(SQLiteDatabase database)
    {
        database.execSQL(getTableDropSQL());
    }

    //-------------------------------------------------------------------------
    // entity management
    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The id for the new coupon that is created, otherwise a -1.
     */
    public static long insert(SQLiteDatabase database, ContentValues values)
    {
        return database.insert(sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing coupon.
     */
    public static int update(SQLiteDatabase database, ContentValues values,
                             String where, String[] whereArgs)
    {
        return database.update(sName, values, where, whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing coupon.
     */
    public static int updateById(SQLiteDatabase database, String id,
                                 ContentValues values,
                                 String where, String[] whereArgs)
    {
        return update(database, values, appendWhereId(id, where), whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing coupon.
     */
    public static int delete(SQLiteDatabase database,
                             String where, String[] whereArgs)
    {
        return database.delete(sName, where, whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing coupon.
     */
    public static int deleteById(SQLiteDatabase database,
                                 String id, String where, String[] whereArgs)
    {
        return delete(database, appendWhereId(id, where), whereArgs);
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    private static String appendWhereId(String id, String where)
    {
        String whereId = String.format("%s = %s", sKeyId, id);
        return !TextUtils.isEmpty(where) ?
            String.format("%s AND (%s)", whereId, where) :
            whereId;
    }

    //-------------------------------------------------------------------------
    // sql functions
    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to create the database table.
     */
    private static String getCreateSQL()
    {
        String createSQL =
            "create table "  + sName + "("                            +
            sKeyId           + " integer primary key not null,      " +
            sKeyTitle        + " text    not null,                  " +
            sKeyDetails      + " text    not null,                  " +
            sKeyIconId       + " integer not null,                  " +
            sKeyIconUrl      + " text    not null,                  " +
            sKeyStartTime    + " integer not null,                  " +
            sKeyEndTime      + " integer not null,                  " +
            sKeyBarcode      + " text    not null,                  " +
            sKeyIsSoldOut    + " integer not null default 0,        " +
            sKeyWasRedeemed  + " integer not null,                  " +
            sKeyIsRedeemable + " integer not null default 1,        " +
            sKeyLocations    + " text    not null,                  " +
            sKeyMerchant     + " integer not null                   "
                             + String.format(" references %s(%s)", MerchantTable.sName, MerchantTable.sKeyId)
                             + " on delete cascade                  " + ");";
        return createSQL;
    }

    //-------------------------------------------------------------------------

    public static void onUpgradev1Tov2(SQLiteDatabase database)
    {
        String alterSQL =
            "alter table " + sName +
            " add column " + sKeyIsRedeemable + " integer not null default 1";
        database.execSQL(alterSQL);
    }

    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to drop a table if it exists.
     */
    public static String getTableDropSQL()
    {
        String tableDropSQL =
            String.format("drop table if exists %s", sName);
        return tableDropSQL;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    public static String sName            = "Coupon";
    public static String sKeyId           = "_id";
    public static String sKeyTitle        = "title";
    public static String sKeyDetails      = "details";
    public static String sKeyIconId       = "icon_id";
    public static String sKeyIconUrl      = "icon_url";
    public static String sKeyStartTime    = "start_time";
    public static String sKeyEndTime      = "end_time";
    public static String sKeyBarcode      = "barcode";
    public static String sKeyWasRedeemed  = "was_redeemed";
    public static String sKeyIsRedeemable = "is_redeemable";
    public static String sKeyIsSoldOut    = "is_sold_out";
    public static String sKeyLocations    = "locations";
    public static String sKeyMerchant     = "merchant";

    //-------------------------------------------------------------------------

    public static String sFullProjection[] = new String[] {
        sKeyId,
        sKeyTitle,
        sKeyDetails,
        sKeyIconId,
        sKeyIconUrl,
        sKeyStartTime,
        sKeyEndTime,
        sKeyBarcode,
        sKeyIsSoldOut,
        sKeyWasRedeemed,
        sKeyIsRedeemable,
        sKeyLocations,
        sKeyMerchant,
    };

}
