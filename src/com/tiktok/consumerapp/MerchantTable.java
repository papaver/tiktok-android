//-----------------------------------------------------------------------------
// MerchantTable
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

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
        // this is obviously the worst implementation ever

        Log.w(MerchantTable.class.getName(), String.format(
            "Upgrading database from version %d to %d " +
            "which will destroy all data.", oldVersion, newVersion));

        // drop the table
        dropTable(database, sName);

        // create the new table
        onCreate(database);
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
            "create table " + sName + "("                            +
            sKeyRowId       + " integer primary key autoincrement, " +
            sKeyName        + " text    not null,                  " +
            sKeyAddress     + " text    not null,                  " +
            sKeyLatitude    + " real    not null,                  " +
            sKeyLongitude   + " real    not null,                  " +
            sKeyPhone       + " text    not null,                  " +
            sKeyCategory    + " text    ,                          " +
            sKeyDetails     + " text    not null,                  " +
            sKeyIconId      + " integer not null,                  " +
            sKeyIconUrl     + " text    not null,                  " +
            sKeyWebsiteUrl  + " text    not null                   " + ");";
        return createSQL;
    }

    //-------------------------------------------------------------------------

    /**
     * Drop the given table from the database.
     */
    public static void dropTable(SQLiteDatabase database, String tableName)
    {
        database.execSQL(getTableDropSQL(tableName));
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
    // fields
    //-------------------------------------------------------------------------

    public static String sName          = "Merchant";
    public static String sKeyRowId      = "_id";
    public static String sKeyName       = "name";
    public static String sKeyAddress    = "address";
    public static String sKeyLatitude   = "latitude";
    public static String sKeyLongitude  = "longitude";
    public static String sKeyPhone      = "phone";
    public static String sKeyCategory   = "category";
    public static String sKeyDetails    = "details";
    public static String sKeyIconId     = "icon_id";
    public static String sKeyIconUrl    = "icon_url";
    public static String sKeyWebsiteUrl = "website_url";

}
