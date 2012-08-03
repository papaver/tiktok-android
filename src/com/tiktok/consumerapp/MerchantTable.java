//-----------------------------------------------------------------------------
// MerchantTable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class MerchantTable
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
        // nothing to do if the versions match
        if (oldVersion == newVersion) return;

        Log.i(MerchantTable.class.getName(), String.format(
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
    // query methods
    //-------------------------------------------------------------------------

    /**
     * Fetch merchant/s from the database.
     * @returns Cursor with all columns in the merchant table.
     */
    public static Cursor query(SQLiteDatabase database, String selection) throws SQLException
    {
        String columns[] = new String[] {
            sKeyId,
            sKeyName,
            sKeyCategory,
            sKeyDetails,
            sKeyIconId,
            sKeyIconUrl,
            sKeyWebsiteUrl,
            sKeyTwitterHandle,
            sKeyUsesPin,
            sKeyLastUpdated
        };

        Cursor cursor = database.query(true, sName, columns, selection,
            null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch single merchant by id.
     */
    public static Cursor fetchById(SQLiteDatabase database, long id) throws SQLException
    {
        String selection = String.format("%s = %d", sKeyId, id);
        return query(database, selection);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public static Map<Long, Date> fetchIds(SQLiteDatabase database)
    {
        String columns[] = new String[] {
            sKeyId,
            sKeyLastUpdated,
        };

        Cursor cursor       = null;
        Map<Long, Date> ids = null;

        try {

            // grab the data from the database
            cursor = database.query(sName, columns,
                null, null, null, null, null);
            cursor.moveToFirst();

            // create a hash of ids and update times
            ids = new HashMap<Long, Date>();
            for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
                ids.put(cursor.getLong(0), new Date(cursor.getLong(1) * 1000));
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
     * Create a new merchant.
     * @returns The id for the new merchant that is created, otherwise a -1.
     */
    public static long create(SQLiteDatabase database, ContentValues values)
    {
        return database.insert(sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public static boolean update(SQLiteDatabase database, long id, ContentValues values)
    {
        String whereClause = String.format("%s = %d", sKeyId, id);
        return database.update(sName, values, whereClause, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing merchant.
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
            "create table "   + sName + "("                       +
            sKeyId            + " integer primary key not null, " +
            sKeyName          + " text    not null,             " +
            sKeyCategory      + " text    not null,             " +
            sKeyDetails       + " text    not null,             " +
            sKeyIconId        + " integer not null,             " +
            sKeyIconUrl       + " text    not null,             " +
            sKeyWebsiteUrl    + " text    not null,             " +
            sKeyTwitterHandle + " text    not null,             " +
            sKeyUsesPin       + " integer not null,             " +
            sKeyLastUpdated   + " integer not null default 0    " + ");";
        return createSQL;
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

    public static String sName             = "Merchant";
    public static String sKeyId            = "_id";
    public static String sKeyName          = "name";
    public static String sKeyCategory      = "category";
    public static String sKeyDetails       = "details";
    public static String sKeyIconId        = "icon_id";
    public static String sKeyIconUrl       = "icon_url";
    public static String sKeyWebsiteUrl    = "website_url";
    public static String sKeyTwitterHandle = "tw_handle";
    public static String sKeyUsesPin       = "uses_pin";
    public static String sKeyLastUpdated   = "last_updated";

}
