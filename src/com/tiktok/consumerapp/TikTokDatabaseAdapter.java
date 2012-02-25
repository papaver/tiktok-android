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
        ContentValues values = createContentValues(coupon);
        return mDatabase.insert(CouponTable.sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The rowId for the new coupon that is created, otherwise a -1.
     */
    public long createCoupon(long id, String title, String details,
                             int iconId, String iconUrl,
                             long startTime, long endTime, String barcode,
                             boolean wasRedeemed)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, wasRedeemed);
        return createCoupon(coupon);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(long rowId,
                                long id, String title, String details,
                                int iconId, String iconUrl,
                                long startTime, long endTime, String barcode,
                                boolean wasRedeemed)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, wasRedeemed);
        ContentValues values = createContentValues(coupon);
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
            CouponTable.sKeyDetails,
            CouponTable.sKeyIconId,
            CouponTable.sKeyIconUrl,
            CouponTable.sKeyStartTime,
            CouponTable.sKeyEndTime,
            CouponTable.sKeyBarcode,
            CouponTable.sKeyWasRedeemed,
            CouponTable.sKeyIsSoldOut,
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

        // grab the data from the database
        Cursor cursor = mDatabase.query(CouponTable.sName, rows, 
            null, null, null, null, null);
        cursor.moveToFirst();

        // create a list of ids
        List<Long> ids = new ArrayList<Long>();
        for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
            ids.add(cursor.getLong(0));
        }

        // cleanup 
        cursor.close();

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
            CouponTable.sKeyDetails,
            CouponTable.sKeyIconId,
            CouponTable.sKeyIconUrl,
            CouponTable.sKeyStartTime,
            CouponTable.sKeyEndTime,
            CouponTable.sKeyBarcode,
            CouponTable.sKeyWasRedeemed,
            CouponTable.sKeyIsSoldOut,
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
    private ContentValues createContentValues(Coupon coupon)
    {
        ContentValues values = new ContentValues();
        values.put(CouponTable.sKeyId,          coupon.id());
        values.put(CouponTable.sKeyTitle,       coupon.title());
        values.put(CouponTable.sKeyDetails,     coupon.details());
        values.put(CouponTable.sKeyIconId,      coupon.iconId());
        values.put(CouponTable.sKeyIconUrl,     coupon.iconUrl());
        values.put(CouponTable.sKeyStartTime,   coupon.startTimeRaw());
        values.put(CouponTable.sKeyEndTime,     coupon.endTimeRaw());
        values.put(CouponTable.sKeyBarcode,     coupon.barcode());
        values.put(CouponTable.sKeyWasRedeemed, coupon.wasRedeemed());
        values.put(CouponTable.sKeyIsSoldOut,   coupon.isSoldOut());
        return values;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;
    private SQLiteDatabase       mDatabase; 
    private TikTokDatabaseHelper mDatabaseHelper;

}
