//-----------------------------------------------------------------------------
// CouponTable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CouponTable
{

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
            "Upgrading database from version %d to %d " +
            "which will destroy all data.", oldVersion, newVersion));

        // drop the table 
        dropTable(database);

        // create the new table
        onCreate(database);
    }

    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to create the database table.
     */
    private static String getCreateSQL()
    {
        String createSQL = 
            "create table "  + sName + "("                            +
            sKeyRowId        + " integer primary key autoincrement, " +
            sKeyId           + " integer not null,                  " +
            sKeyTitle        + " text    not null,                  " +
            sKeyDetails      + " text    not null,                  " +
            sKeyIconId       + " integer not null,                  " +
            sKeyIconUrl      + " text    not null,                  " +
            sKeyStartTime    + " integer not null,                  " +
            sKeyEndTime      + " integer not null,                  " +
            sKeyBarcode      + " text    not null,                  " +
            sKeyWasRedeemed  + " integer not null,                  " +
            sKeyIsSoldOut    + " integer not null default 0,        " +
            sMerchant        + " integer not null                   "
                             + String.format(" references %s(%s)", MerchantTable.sName, MerchantTable.sKeyId)
                             + " on delete cascade                  " + ");";
        return createSQL;
    }

    //-------------------------------------------------------------------------

    /**
     * Drop the given table from the database.
     */
    public static void dropTable(SQLiteDatabase database)
    {
        database.execSQL(getTableDropSQL(sName));
    }

    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to drop a table if it exists.
     */
    public static String getTableDropSQL(String tableName)
    {
        String tableDropSQL = 
            String.format("drop table if exists %s", tableName); 
        return tableDropSQL;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch coupon from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public static Coupon fetch(SQLiteDatabase database, long id) throws SQLException
    {
        String rows[] = new String[] {
            sKeyRowId,
            sKeyId,
            sKeyTitle,
            sKeyDetails,
            sKeyIconId,
            sKeyIconUrl,
            sKeyStartTime,
            sKeyEndTime,
            sKeyBarcode,
            sKeyWasRedeemed,
            sKeyIsSoldOut,
            sMerchant,
        };

        String equalsSQL = String.format("%s = %d", sKeyId, id);
        Cursor cursor    = database.query(true, sName, rows, equalsSQL,
            null, null, null, null, null);

        // create a coupon from the cursor
        if (cursor != null) {
            cursor.moveToFirst();

            // grab merchant
            long merchantId   = cursor.getLong(cursor.getColumnIndex(sMerchant));
            Merchant merchant = MerchantTable.fetch(database, merchantId);

            // can't have coupons without merchants!
            if (merchant != null) {
                Coupon coupon = new Coupon(
                    cursor.getLong(cursor.getColumnIndex(sKeyId)),
                    cursor.getString(cursor.getColumnIndex(sKeyTitle)),
                    cursor.getString(cursor.getColumnIndex(sKeyDetails)),
                    cursor.getInt(cursor.getColumnIndex(sKeyIconId)),
                    cursor.getString(cursor.getColumnIndex(sKeyIconUrl)),
                    cursor.getLong(cursor.getColumnIndex(sKeyStartTime)),
                    cursor.getLong(cursor.getColumnIndex(sKeyEndTime)),
                    cursor.getString(cursor.getColumnIndex(sKeyBarcode)),
                    cursor.getInt(cursor.getColumnIndex(sKeyWasRedeemed)) == 1,
                    cursor.getInt(cursor.getColumnIndex(sKeyIsSoldOut)) == 1,
                    merchant
                );
                return coupon;
            }
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    public static String sName           = "Coupon";
    public static String sKeyRowId       = "_id";
    public static String sKeyId          = "coupon_id";
    public static String sKeyTitle       = "title";
    public static String sKeyDetails     = "details";
    public static String sKeyIconId      = "icon_id";
    public static String sKeyIconUrl     = "icon_url";
    public static String sKeyStartTime   = "start_time";
    public static String sKeyEndTime     = "end_time";
    public static String sKeyBarcode     = "barcode";
    public static String sKeyWasRedeemed = "was_redeemed";
    public static String sKeyIsSoldOut   = "is_sold_out";
    public static String sMerchant       = "merchant";

}
