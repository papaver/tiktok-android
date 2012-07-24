//-----------------------------------------------------------------------------
// CouponTable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponTable
{

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
        // this is obviously the worst implementation ever

        Log.w(CouponTable.class.getName(), String.format(
            "Upgrading database from version %d to %d ", oldVersion, newVersion));

        // v1 to v2
        if (oldVersion == 1) {
            onUpgradev1Tov2(database);
            ++oldVersion;
        }
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
    // query methods
    //-------------------------------------------------------------------------

    public static Cursor query(SQLiteDatabase database, String selection,
                               String orderBy) throws SQLException
    {
        String columns[] = new String[] {
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

        // setup cursor
        Cursor cursor = database.query(true, sName, columns, selection,
            null, null, null, orderBy, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch coupon from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public static Cursor fetchById(SQLiteDatabase database, long id) throws SQLException
    {
        String selection = String.format("%s = %d", sKeyId, id);
        return query(database, selection, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public static List<Long> fetchIds(SQLiteDatabase database)
    {
        String columns[] = new String[] {
            sKeyId,
        };

        Cursor cursor  = null;
        List<Long> ids = null;

        try {

            // grab the data from the database
            cursor = database.query(sName, columns,
                null, null, null, null, null);
            cursor.moveToFirst();

            // create a list of ids
            ids = new ArrayList<Long>();
            for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return ids;
    }

    //-------------------------------------------------------------------------
    // entity management
    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The id for the new merchant that is created, otherwise a -1.
     */
    public static long create(SQLiteDatabase database, ContentValues values)
    {
        return database.insert(sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing coupon.
     */
    public static boolean update(SQLiteDatabase database, long id, ContentValues values)
    {
        String whereClause = String.format("%s = %d", sKeyId, id);
        return database.update(sName, values, whereClause, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing coupon.
     */
    public static boolean delete(SQLiteDatabase database, long id)
    {
        String whereClause = String.format("%s = %d", sKeyId, id);
        return database.delete(sName, whereClause, null) > 0;
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

}
