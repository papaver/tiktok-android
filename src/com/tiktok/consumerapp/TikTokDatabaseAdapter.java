//-----------------------------------------------------------------------------
// TikTokDatabaseAdapter
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
                             boolean isSoldOut, boolean wasRedeemed,
                             boolean isRedeemable, Merchant merchant)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, isSoldOut, wasRedeemed, isRedeemable,
            merchant);
        return createCoupon(coupon);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new merchant.
     * @returns The rowId for the new merchant that is created, otherwise a -1.
     */
    public long createMerchant(Merchant merchant)
    {
        ContentValues values = createContentValues(merchant);
        return mDatabase.insert(MerchantTable.sName, null, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new merchant.
     * @returns The rowId for the new merchant that is created, otherwise a -1.
     */
    public long createMerchant(long id, String name, String address,
                               double latitude, double longitude,
                               String phone, String category, String details,
                               int iconId, String iconUrl,
                               String webUrl, String twHandle, long lastUpdated)
    {
        Merchant merchant = new Merchant(id, name, address, latitude, longitude, phone,
            category, details, iconId, iconUrl, webUrl, twHandle, lastUpdated);
        return createMerchant(merchant);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(long id, String title, String details,
                                int iconId, String iconUrl,
                                long startTime, long endTime, String barcode,
                                boolean isSoldOut, boolean wasRedeemed,
                                boolean isRedeemable, Merchant merchant)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, isSoldOut, wasRedeemed, isRedeemable,
            merchant);
        return updateCoupon(coupon);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(Coupon coupon)
    {
        ContentValues values = createContentValues(coupon);
        String equalsSQL = String.format("%s = %d", CouponTable.sKeyId, coupon.id());
        return mDatabase.update(CouponTable.sName, values, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public boolean updateMerchant(long id, String name, String address,
                                  double latitude, double longitude,
                                  String phone, String category, String details,
                                  int iconId, String iconUrl,
                                  String webUrl, String twHandle, long lastUpdated)
    {
        Merchant merchant = new Merchant(id, name, address, latitude, longitude, phone,
            category, details, iconId, iconUrl, webUrl, twHandle, lastUpdated);
        return updateMerchant(merchant);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public boolean updateMerchant(Merchant merchant)
    {
        ContentValues values = createContentValues(merchant);
        String equalsSQL = String.format("%s = %d", MerchantTable.sKeyId, merchant.id());
        return mDatabase.update(MerchantTable.sName, values, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------
    /**
     * Delete an existing coupon.
     */
    public boolean deleteCoupon(long id)
    {
        String equalsSQL = String.format("%s = %d", CouponTable.sKeyId, id);
        return mDatabase.delete(CouponTable.sName, equalsSQL, null) > 0;
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing coupon.
     */
    public boolean deleteMerchant(long id)
    {
        String equalsSQL = String.format("%s = %d", MerchantTable.sKeyId, id);
        return mDatabase.delete(MerchantTable.sName, equalsSQL, null) > 0;
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
            CouponTable.sKeyIsSoldOut,
            CouponTable.sKeyWasRedeemed,
            CouponTable.sKeyIsRedeemable,
            CouponTable.sKeyMerchant,
        };

        long oneDay    = 24 * 60 * 60 * 1000;
        long oneDayAgo = (new Date().getTime() - oneDay) / 1000;
        String orderBy = String.format("%s DESC", CouponTable.sKeyEndTime);
        String where   = String.format("%s > %d", CouponTable.sKeyEndTime, oneDayAgo);
        return mDatabase.query(CouponTable.sName, rows, where, null, null, null, orderBy);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the merchants.
     */
    public Cursor fetchAllMerchants()
    {
        String rows[] = new String[] {
            MerchantTable.sKeyRowId,
            MerchantTable.sKeyId,
            MerchantTable.sKeyName,
            MerchantTable.sKeyAddress,
            MerchantTable.sKeyLatitude,
            MerchantTable.sKeyLongitude,
            MerchantTable.sKeyPhone,
            MerchantTable.sKeyCategory,
            MerchantTable.sKeyDetails,
            MerchantTable.sKeyIconId,
            MerchantTable.sKeyIconUrl,
            MerchantTable.sKeyWebsiteUrl,
            MerchantTable.sKeyTwitterHandle,
            MerchantTable.sKeyLastUpdated
        };
        return mDatabase.query(MerchantTable.sName, rows, null, null, null, null, null);
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
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public Map<Long, Date> fetchAllMerchantIds()
    {
        String rows[] = new String[] {
            MerchantTable.sKeyId,
            MerchantTable.sKeyLastUpdated,
        };

        // grab the data from the database
        Cursor cursor = mDatabase.query(MerchantTable.sName, rows,
            null, null, null, null, null);
        cursor.moveToFirst();

        // create a hash of ids and update times
        Map<Long, Date> ids = new HashMap<Long, Date>();
        for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
            ids.put(cursor.getLong(0), new Date(cursor.getLong(1) * 1000));
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
    public Coupon fetchCoupon(long id) throws SQLException
    {
        return CouponTable.fetch(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch coupon from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public Coupon fetchCouponByRowId(long id) throws SQLException
    {
        return CouponTable.fetchByRowId(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch merchant from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public Merchant fetchMerchant(long id) throws SQLException
    {
        return MerchantTable.fetch(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * @returns Mapping between database columns and values.
     */
    private ContentValues createContentValues(Coupon coupon)
    {
        ContentValues values = new ContentValues();
        values.put(CouponTable.sKeyId,           coupon.id());
        values.put(CouponTable.sKeyTitle,        coupon.title());
        values.put(CouponTable.sKeyDetails,      coupon.details());
        values.put(CouponTable.sKeyIconId,       coupon.iconId());
        values.put(CouponTable.sKeyIconUrl,      coupon.iconUrl());
        values.put(CouponTable.sKeyStartTime,    coupon.startTimeRaw());
        values.put(CouponTable.sKeyEndTime,      coupon.endTimeRaw());
        values.put(CouponTable.sKeyBarcode,      coupon.barcode());
        values.put(CouponTable.sKeyIsSoldOut,    coupon.isSoldOut());
        values.put(CouponTable.sKeyWasRedeemed,  coupon.wasRedeemed());
        values.put(CouponTable.sKeyIsRedeemable, coupon.isRedeemable());
        values.put(CouponTable.sKeyMerchant,     coupon.merchant().id());
        return values;
    }

    //-------------------------------------------------------------------------

    /**
     * @returns Mapping between database columns and values.
     */
    private ContentValues createContentValues(Merchant merchant)
    {
        ContentValues values = new ContentValues();
        values.put(MerchantTable.sKeyId,            merchant.id());
        values.put(MerchantTable.sKeyName,          merchant.name());
        values.put(MerchantTable.sKeyAddress,       merchant.address());
        values.put(MerchantTable.sKeyLatitude,      merchant.latitude());
        values.put(MerchantTable.sKeyLongitude,     merchant.longitude());
        values.put(MerchantTable.sKeyPhone,         merchant.phone());
        values.put(MerchantTable.sKeyCategory,      merchant.category());
        values.put(MerchantTable.sKeyDetails,       merchant.details());
        values.put(MerchantTable.sKeyIconId,        merchant.iconId());
        values.put(MerchantTable.sKeyIconUrl,       merchant.iconUrl());
        values.put(MerchantTable.sKeyWebsiteUrl,    merchant.websiteUrl());
        values.put(MerchantTable.sKeyTwitterHandle, merchant.twitterHandle());
        values.put(MerchantTable.sKeyLastUpdated,   merchant.lastUpdatedRaw());
        return values;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;
    private SQLiteDatabase       mDatabase;
    private TikTokDatabaseHelper mDatabaseHelper;

}
