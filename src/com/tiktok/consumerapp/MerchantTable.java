//-----------------------------------------------------------------------------
// MerchantTable
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

public class MerchantTable
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
        // nothing to do if the versions match
        if (oldVersion == newVersion) return;

        Log.w(MerchantTable.class.getName(), String.format(
            "Upgrading database from version %d to %d ", oldVersion, newVersion));

        // v1 to v2
        if (oldVersion == 1) {
            onUpgradev1Tov2(database);

        // v2 to v3
        } else if (oldVersion == 2) {
            onUpgradev2Tov3(database);
        }

        // keep upgrading till versions match
        onUpgrade(database, oldVersion + 1, newVersion);
    }

    //-------------------------------------------------------------------------

    public static void onUpgradev1Tov2(SQLiteDatabase database)
    {
        String alterSQL =
            "alter table " + sName +
            " add column " + sKeyLastUpdated + " integer not null default 0";
        database.execSQL(alterSQL);
    }

    //-------------------------------------------------------------------------

    public static void onUpgradev2Tov3(SQLiteDatabase database)
    {
        String alterSQL =
            "alter table " + sName +
            " add column " + sKeyTwitterHandle + " text not null default ''";
        database.execSQL(alterSQL);
    }

    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to create the database table.
     */
    private static String getCreateSQL()
    {
        // [moiz] category should be no null once category string is available
        //   in the json
        String createSQL =
            "create table "   + sName + "("                            +
            sKeyRowId         + " integer primary key autoincrement, " +
            sKeyId            + " integer not null,                  " +
            sKeyName          + " text    not null,                  " +
            sKeyAddress       + " text    not null,                  " +
            sKeyLatitude      + " real    not null,                  " +
            sKeyLongitude     + " real    not null,                  " +
            sKeyPhone         + " text    not null,                  " +
            sKeyCategory      + " text    not null,                  " +
            sKeyDetails       + " text    not null,                  " +
            sKeyIconId        + " integer not null,                  " +
            sKeyIconUrl       + " text    not null,                  " +
            sKeyWebsiteUrl    + " text    not null,                  " +
            sKeyTwitterHandle + " text    not null default '',       " +
            sKeyLastUpdated   + " integer not null default 0         " + ");";
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
     * Fetch merchant from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public static Merchant fetch(SQLiteDatabase database, long id) throws SQLException
    {
        String rows[] = new String[] {
            sKeyRowId,
            sKeyId,
            sKeyName,
            sKeyAddress,
            sKeyLatitude,
            sKeyLongitude,
            sKeyPhone,
            sKeyCategory,
            sKeyDetails,
            sKeyIconId,
            sKeyIconUrl,
            sKeyWebsiteUrl,
            sKeyTwitterHandle,
            sKeyLastUpdated
        };

        String equalsSQL = String.format("%s = '%d'", sKeyId, id);
        Cursor cursor    = database.query(true, sName, rows, equalsSQL,
            null, null, null, null, null);

        // create a merchant from the cursor
        if (cursor != null) {
            cursor.moveToFirst();

            Merchant merchant = new Merchant(
                cursor.getLong(cursor.getColumnIndex(sKeyId)),
                cursor.getString(cursor.getColumnIndex(sKeyName)),
                cursor.getString(cursor.getColumnIndex(sKeyAddress)),
                cursor.getDouble(cursor.getColumnIndex(sKeyLatitude)),
                cursor.getDouble(cursor.getColumnIndex(sKeyLongitude)),
                cursor.getString(cursor.getColumnIndex(sKeyPhone)),
                cursor.getString(cursor.getColumnIndex(sKeyCategory)),
                cursor.getString(cursor.getColumnIndex(sKeyDetails)),
                cursor.getInt(cursor.getColumnIndex(sKeyIconId)),
                cursor.getString(cursor.getColumnIndex(sKeyIconUrl)),
                cursor.getString(cursor.getColumnIndex(sKeyWebsiteUrl)),
                cursor.getString(cursor.getColumnIndex(sKeyTwitterHandle)),
                cursor.getLong(cursor.getColumnIndex(sKeyLastUpdated))
            );
            return merchant;
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    public static String sName             = "Merchant";
    public static String sKeyRowId         = "_id";
    public static String sKeyId            = "merchant_id";
    public static String sKeyName          = "name";
    public static String sKeyAddress       = "address";
    public static String sKeyLatitude      = "latitude";
    public static String sKeyLongitude     = "longitude";
    public static String sKeyPhone         = "phone";
    public static String sKeyCategory      = "category";
    public static String sKeyDetails       = "details";
    public static String sKeyIconId        = "icon_id";
    public static String sKeyIconUrl       = "icon_url";
    public static String sKeyWebsiteUrl    = "website_url";
    public static String sKeyTwitterHandle = "tw_handle";
    public static String sKeyLastUpdated   = "last_updated";

}
