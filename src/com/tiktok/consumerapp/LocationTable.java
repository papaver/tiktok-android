//-----------------------------------------------------------------------------
// LocationTable
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

public class LocationTable
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

        Log.w(LocationTable.class.getName(), String.format(
            "Upgrading database from version %d to %d ", oldVersion, newVersion));

        // keep upgrading till versions match
        onUpgrade(database, oldVersion + 1, newVersion);
    }

    //-------------------------------------------------------------------------

    /**
     * Drop the given table from the database.
     */
    public static void dropTable(SQLiteDatabase database)
    {
        database.execSQL(getDropSQL());
    }

    //-------------------------------------------------------------------------
    // query methods
    //-------------------------------------------------------------------------

    /**
     * Fetch location/s from the database.
     * @returns Cursor object with all columns in location table.
     */
    public static Cursor query(SQLiteDatabase database, String selection) throws SQLException
    {
        String columns[] = new String[] {
            sKeyId,
            sKeyName,
            sKeyAddress,
            sKeyLatitude,
            sKeyLongitude,
            sKeyPhone,
            sKeyLastUpdated
        };

        // attemp to fetch the location from the database
        Cursor cursor = database.query(true, sName, columns, selection,
            null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch single location by id.
     */
    public static Cursor fetchById(SQLiteDatabase database, long id) throws SQLException
    {
        String selection = String.format("%s = %d", sKeyId, id);
        return query(database, selection);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch multiple locations by thier ids.
     */
    public static Cursor fetchByIds(SQLiteDatabase database, String ids) throws SQLException
    {
        String selection = String.format("%s in (%s)", sKeyId, ids);
        return query(database, selection);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the location ids from the database.
     * @returns Hash mapping ids to thier last updated time.
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

            // query database for location data
            cursor = database.query(sName, columns,
                null, null, null, null, null);
            cursor.moveToFirst();

            // copy data out of cursor into hash
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
     * Create a new location.
     * @returns The id for the new location that is created, otherwise a -1.
     */
    public static long create(SQLiteDatabase database, ContentValues values)
    {
        return database.insert(sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing location.
     */
    public static boolean update(SQLiteDatabase database, long id, ContentValues values)
    {
        String whereClause = String.format("%s = %d", sKeyId, id);
        return database.update(sName, values, whereClause, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Deletes an existing location.
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
            "create table " + sName + "("                       +
            sKeyId          + " integer primary key not null, " +
            sKeyName        + " text    not null,             " +
            sKeyAddress     + " text    not null,             " +
            sKeyLatitude    + " real    not null,             " +
            sKeyLongitude   + " real    not null,             " +
            sKeyPhone       + " text    not null,             " +
            sKeyLastUpdated + " integer not null default 0    " + ");";
        return createSQL;
    }

    //-------------------------------------------------------------------------

    /**
     * @return SQL statement used to drop a table if it exists.
     */
    private static String getDropSQL()
    {
        String tableDropSQL =
            String.format("drop table if exists %s", sName);
        return tableDropSQL;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    public static String sName             = "Location";
    public static String sKeyId            = "_id";
    public static String sKeyName          = "name";
    public static String sKeyAddress       = "address";
    public static String sKeyLatitude      = "latitude";
    public static String sKeyLongitude     = "longitude";
    public static String sKeyPhone         = "phone";
    public static String sKeyLastUpdated   = "last_updated";

}
