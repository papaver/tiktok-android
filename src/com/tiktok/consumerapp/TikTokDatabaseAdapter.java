//-----------------------------------------------------------------------------
// TikTokDatabaseAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

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
    public long createCoupon(Coupon coupon)
    {
        // TODO:: update this so you get the proper icon...
        int icon = ((int)(coupon.getId()) % 2) == 0 ? R.drawable.coupon_icon_1 : R.drawable.coupon_icon_2;

        ContentValues values = createContentValues(
            coupon.getId(), coupon.getTitle(), coupon.getImageUrl(), 
            coupon.getStartTimeRaw(), coupon.getEndTimeRaw(), icon);
        return mDatabase.insert(CouponTable.sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The rowId for the new coupon that is created, otherwise a -1.
     */
    public long createCoupon(long id, String title, String imageUrl, 
                             long startTime, long endTime, int icon)
    {
        ContentValues values = createContentValues(
            id, title, imageUrl, startTime, endTime, icon);
        return mDatabase.insert(CouponTable.sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(long rowId, 
                                long id, String title, String imageUrl, 
                                long startTime, long endTime, int icon)
    {
        ContentValues values = createContentValues(
            id, title, imageUrl, startTime, endTime, icon);
        String equalsSQL = String.format("%s = %s", CouponTable.sKeyRowId, rowId);
        return mDatabase.update(CouponTable.sName, values, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing coupon.
     */
    public boolean deleteCoupon(long rowId) 
    {
        String equalsSQL = String.format("%s = %s", CouponTable.sKeyRowId, rowId);
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
            CouponTable.sKeyId,
            CouponTable.sKeyTitle,
            CouponTable.sKeyImageUrl,
            CouponTable.sKeyStartTime,
            CouponTable.sKeyEndTime,
            CouponTable.sKeyIcon,
        };
        return mDatabase.query(CouponTable.sName, rows, null, null, null, null, null);
    }

    //-------------------------------------------------------------------------
    
    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public List<Long> fetchAllCouponIds() 
    {
        String rows[] = new String[] {
            CouponTable.sKeyId,
        };
        Cursor cursor = mDatabase.query(CouponTable.sName, rows, 
            null, null, null, null, null);
        cursor.moveToFirst();

        List<Long> ids = new ArrayList<Long>();
        while (!cursor.isAfterLast()) {
            ids.add(cursor.getLong(0));
            cursor.moveToNext();
        }

        return ids;
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
            CouponTable.sKeyId,
            CouponTable.sKeyTitle,
            CouponTable.sKeyImageUrl,
            CouponTable.sKeyStartTime,
            CouponTable.sKeyEndTime,
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
     * @returns Mapping between database columns and values.
     */
    private ContentValues createContentValues(long id, 
                                              String title, String imageUrl, 
                                              long startTime, long endTime, 
                                              int icon)
    {
        ContentValues values = new ContentValues();
        values.put(CouponTable.sKeyId,        id);
        values.put(CouponTable.sKeyTitle,     title);
        values.put(CouponTable.sKeyImageUrl,  imageUrl);
        values.put(CouponTable.sKeyStartTime, startTime);
        values.put(CouponTable.sKeyEndTime,   endTime);
        values.put(CouponTable.sKeyIcon,      icon);
        return values;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;
    private SQLiteDatabase       mDatabase; 
    private TikTokDatabaseHelper mDatabaseHelper;

}
