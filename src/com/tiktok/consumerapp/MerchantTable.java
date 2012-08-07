//-----------------------------------------------------------------------------
// MerchantTable
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

public class MerchantTable
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final Uri kContentUri =
        Uri.parse("content://" + Constants.kAuthority + "/merchants");

    public static final String kContentType =
        "com.tiktok.cursor.dir/com.tiktok.merchant";
    public static final String kContentTypeItem =
        "com.tiktok.cursor.item/com.tiktok.merchant";

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
    // entity management
    //-------------------------------------------------------------------------

    /**
     * Create a new merchant.
     * @returns The id for the new merchant that is created, otherwise a -1.
     */
    public static long insert(SQLiteDatabase database, ContentValues values)
    {
        return database.insert(sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public static int update(SQLiteDatabase database, ContentValues values,
                             String where, String[] whereArgs)
    {
        return database.update(sName, values, where, whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public static int updateById(SQLiteDatabase database, String id,
                                 ContentValues values,
                                 String where, String[] whereArgs)
    {
        return update(database, values, appendWhereId(id, where), whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing merchant.
     */
    public static int delete(SQLiteDatabase database,
                             String where, String[] whereArgs)
    {
        return database.delete(sName, where, whereArgs);
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing merchant.
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

    //-------------------------------------------------------------------------

    public static String sFullProjection[] = new String[] {
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

}
