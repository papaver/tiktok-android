//-----------------------------------------------------------------------------
// TikTokDatabaseHelper
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokDatabaseHelper extends SQLiteOpenHelper
{
    //-------------------------------------------------------------------------
    // static methods
    //-------------------------------------------------------------------------

    /**
     * Delete database from the filesystem.
     */
    public static void purgeDatabase(Context context)
    {
        context.deleteDatabase(sDatabaseName);
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public TikTokDatabaseHelper(Context context)
    {
        super(context, sDatabaseName, null, sDatabaseVersion);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    /**
     * Method is called during creation of the database.
     */
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        LocationTable.onCreate(database);
        MerchantTable.onCreate(database);
        CouponTable.onCreate(database);
    }

    //-------------------------------------------------------------------------

    /**
     * Method is called during upgrade of the database, if the database version
     * is increased.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database,
                          int oldVersion, int newVersion)
    {
        CouponTable.onUpgrade(database, oldVersion, newVersion);
        MerchantTable.onUpgrade(database, oldVersion, newVersion);
        LocationTable.onUpgrade(database, oldVersion, newVersion);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private static final String sDatabaseName    = "TikTokData";
    private static final int    sDatabaseVersion = 3;

}
