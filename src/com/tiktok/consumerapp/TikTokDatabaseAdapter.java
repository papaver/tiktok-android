//-----------------------------------------------------------------------------
// TikTokDatabaseAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokDatabaseAdapter 
{
    
    public TikTokDatabaseAdapter(Context context)
    {
        mContext = context;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Create a new database or retrieves the existing database.
     */
    public TikTokDatabaseAdapter open() throws SQLException
    {
        mDatabaseHelper = new TikTokDatabaseHelper(mContext);
        mDatabase       = mDatabaseHelper.getWritableDatabase();
        return this;
    }

    //-------------------------------------------------------------------------

    /**
     * Close the database.
     */
    public void close() 
    {
        mDatabaseHelper.close();
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The rowId for the new coupon that is created, otherwise a -1.
     */
    public long createCoupon(String title, String description, int icon)
    {
        ContentValues values = createContentValues(title, description, icon);
        return mDatabase.insert(CouponTable.sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(long rowId, 
                                String title, String description, int icon)
    {
        ContentValues values = createContentValues(title, description, icon);
        String equalsSQL     = String.format("%s = %s", CouponTable.sKeyRowId, rowId);
        return mDatabase.update(CouponTable.sName, values, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing coupon.
     */
    public boolean deleteCoupon(long rowId) 
    {
        String equalsSQL     = String.format("%s = %s", CouponTable.sKeyRowId, rowId);
        return mDatabase.delete(CouponTable.sName, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public Cursor fetchAllCoupons() 
    {
        String rows[] = new String[] {
            CouponTable.sKeyRowId,
            CouponTable.sKeyTitle,
            CouponTable.sKeyDescription,
            CouponTable.sKeyIcon,
        };
        return mDatabase.query(CouponTable.sName, rows, null, null, null, null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch coupon from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public Cursor fetchCoupon(long rowId) throws SQLException
    {
        String rows[] = new String[] {
            CouponTable.sKeyRowId,
            CouponTable.sKeyTitle,
            CouponTable.sKeyDescription,
            CouponTable.sKeyIcon,
        };
        String equalsSQL = String.format("%s = %s", CouponTable.sKeyRowId, rowId);
        Cursor cursor    = mDatabase.query(true, CouponTable.sName, rows, equalsSQL,
            null, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * 
     */
    private ContentValues createContentValues(String title, String description, 
                                              int icon)
    {
        ContentValues values = new ContentValues();
        values.put(CouponTable.sKeyTitle, title);
        values.put(CouponTable.sKeyDescription, description);
        values.put(CouponTable.sKeyIcon, icon);
        return values;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;
    private SQLiteDatabase       mDatabase; 
    private TikTokDatabaseHelper mDatabaseHelper;

}
