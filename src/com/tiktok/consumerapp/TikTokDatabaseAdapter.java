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
        return CouponTable.create(mDatabase, values);
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
                             boolean isRedeemable, List<Location> locations,
                             Merchant merchant)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, isSoldOut, wasRedeemed, isRedeemable,
            locations, merchant);
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
        return MerchantTable.create(mDatabase, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new merchant.
     * @returns The rowId for the new merchant that is created, otherwise a -1.
     */
    public long createMerchant(long id, String name, String category,
                               String details, int iconId, String iconUrl,
                               String webUrl, String twHandle, boolean usesPin,
                               long lastUpdated)
    {
        Merchant merchant = new Merchant(id, name, category, details, iconId,
            iconUrl, webUrl, twHandle, usesPin, lastUpdated);
        return createMerchant(merchant);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new location.
     * @returns The rowId for the new location that is created, otherwise a -1.
     */
    public long createLocation(Location location)
    {
        ContentValues values = createContentValues(location);
        return LocationTable.create(mDatabase, values);
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new location.
     * @returns The rowId for the new location that is created, otherwise a -1.
     */
    public long createLocation(long id, String name, String address,
                               double latitude, double longitude,
                               String phone, long lastUpdated)
    {
        Location location = new Location(id, name, address, latitude, longitude,
            phone, lastUpdated);
        return createLocation(location);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(long id, String title, String details,
                                int iconId, String iconUrl,
                                long startTime, long endTime, String barcode,
                                boolean isSoldOut, boolean wasRedeemed,
                                boolean isRedeemable, List<Location> locations,
                                Merchant merchant)
    {
        Coupon coupon = new Coupon(id, title, details, iconId, iconUrl,
            startTime, endTime, barcode, isSoldOut, wasRedeemed, isRedeemable,
            locations, merchant);
        return updateCoupon(coupon);
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public boolean updateCoupon(Coupon coupon)
    {
        ContentValues values = createContentValues(coupon);
        return CouponTable.update(mDatabase, coupon.id(), values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public boolean updateMerchant(long id, String name, String category,
                                  String details, int iconId, String iconUrl,
                                  String webUrl, String twHandle, boolean usesPin,
                                  long lastUpdated)
    {
        Merchant merchant = new Merchant(id, name, category, details, iconId,
            iconUrl, webUrl, twHandle, usesPin, lastUpdated);
        return updateMerchant(merchant);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public boolean updateMerchant(Merchant merchant)
    {
        ContentValues values = createContentValues(merchant);
        return MerchantTable.update(mDatabase, merchant.id(), values);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing location.
     */
    public boolean updateLocation(long id, String name, String address,
                                  double latitude, double longitude,
                                  String phone, long lastUpdated)
    {
        Location location = new Location(id, name, address, latitude, longitude,
             phone, lastUpdated);
        return updateLocation(location);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing location.
     */
    public boolean updateLocation(Location location)
    {
        ContentValues values = createContentValues(location);
        return LocationTable.update(mDatabase, location.id(), values);
    }

    //-------------------------------------------------------------------------
    /**
     * Delete an existing coupon.
     */
    public boolean deleteCoupon(long id)
    {
        return CouponTable.delete(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing merchant.
     */
    public boolean deleteMerchant(long id)
    {
        return MerchantTable.delete(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing location.
     */
    public boolean deleteLocation(long id)
    {
        return LocationTable.delete(mDatabase, id);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public Cursor fetchAllCoupons()
    {
        long oneDay      = 24 * 60 * 60 * 1000;
        long oneDayAgo   = (new Date().getTime() - oneDay) / 1000;
        String orderBy   = String.format("%s DESC", CouponTable.sKeyEndTime);
        String selection = String.format("%s > %d", CouponTable.sKeyEndTime, oneDayAgo);
        return CouponTable.query(mDatabase, selection, orderBy);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the merchants.
     */
    public Cursor fetchAllMerchants()
    {
        return MerchantTable.query(mDatabase, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the locations from the database.
     * @returns Cursor over all the merchants.
     */
    public Cursor fetchAllLocations()
    {
        return LocationTable.query(mDatabase, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public List<Long> fetchAllCouponIds()
    {
        return CouponTable.fetchIds(mDatabase);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public Map<Long, Date> fetchAllMerchantIds()
    {
        return MerchantTable.fetchIds(mDatabase);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public Map<Long, Date> fetchAllLocationIds()
    {
        return LocationTable.fetchIds(mDatabase);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch coupon from the database.
     * @returns Cursor positioned at the requested coupon.
     */
    public Coupon fetchCoupon(long id)
    {
        Cursor cursor = null;
        Coupon coupon = null;

        try {

            // query database
            cursor = CouponTable.fetchById(mDatabase, id);

            // grab locations
            String locationIds       = cursor.getString(cursor.getColumnIndex(CouponTable.sKeyLocations));
            List<Location> locations = fetchLocations(locationIds);

            // grab merchant
            long merchantId   = cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyMerchant));
            Merchant merchant = fetchMerchant(merchantId);

            // sync coupon object with cursor
            if ((merchant != null) && (locations != null) && (locations.size() > 0)) {
                coupon = new Coupon(
                    cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyId)),
                    cursor.getString(cursor.getColumnIndex(CouponTable.sKeyTitle)),
                    cursor.getString(cursor.getColumnIndex(CouponTable.sKeyDetails)),
                    cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIconId)),
                    cursor.getString(cursor.getColumnIndex(CouponTable.sKeyIconUrl)),
                    cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyStartTime)),
                    cursor.getLong(cursor.getColumnIndex(CouponTable.sKeyEndTime)),
                    cursor.getString(cursor.getColumnIndex(CouponTable.sKeyBarcode)),
                    cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIsSoldOut)) == 1,
                    cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyWasRedeemed)) == 1,
                    cursor.getInt(cursor.getColumnIndex(CouponTable.sKeyIsRedeemable)) == 1,
                    locations,
                    merchant
                );
            }

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return coupon;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch merchant from the database.
     * @returns Cursor positioned at the requested merchant.
     */
    public Merchant fetchMerchant(long id)
    {
        Cursor cursor     = null;
        Merchant merchant = null;

        try {

            // query database
            cursor = MerchantTable.fetchById(mDatabase, id);

            // sync merchant object with cursor
            merchant = new Merchant(
                cursor.getLong(cursor.getColumnIndex(MerchantTable.sKeyId)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyName)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyCategory)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyDetails)),
                cursor.getInt(cursor.getColumnIndex(MerchantTable.sKeyIconId)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyIconUrl)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyWebsiteUrl)),
                cursor.getString(cursor.getColumnIndex(MerchantTable.sKeyTwitterHandle)),
                cursor.getInt(cursor.getColumnIndex(MerchantTable.sKeyUsesPin)) == 1,
                cursor.getLong(cursor.getColumnIndex(MerchantTable.sKeyLastUpdated))
            );

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return merchant;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch location from the database.
     * @returns Cursor positioned at the requested location.
     */
    public List<Location> fetchLocations(String ids)
    {
        Cursor cursor            = null;
        List<Location> locations = null;

        try {

            // query database
            cursor = LocationTable.fetchByIds(mDatabase, ids);
            cursor.moveToFirst();

            // sync location object with cursor
            locations = new ArrayList<Location>();
            for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
                locations.add(new Location(
                    cursor.getLong(cursor.getColumnIndex(LocationTable.sKeyId)),
                    cursor.getString(cursor.getColumnIndex(LocationTable.sKeyName)),
                    cursor.getString(cursor.getColumnIndex(LocationTable.sKeyAddress)),
                    cursor.getDouble(cursor.getColumnIndex(LocationTable.sKeyLatitude)),
                    cursor.getDouble(cursor.getColumnIndex(LocationTable.sKeyLongitude)),
                    cursor.getString(cursor.getColumnIndex(LocationTable.sKeyPhone)),
                    cursor.getLong(cursor.getColumnIndex(LocationTable.sKeyLastUpdated))
                ));
            }

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return locations;
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
        values.put(CouponTable.sKeyLocations,    coupon.locationIdsStr());
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
        values.put(MerchantTable.sKeyCategory,      merchant.category());
        values.put(MerchantTable.sKeyDetails,       merchant.details());
        values.put(MerchantTable.sKeyIconId,        merchant.iconId());
        values.put(MerchantTable.sKeyIconUrl,       merchant.iconUrl());
        values.put(MerchantTable.sKeyWebsiteUrl,    merchant.websiteUrl());
        values.put(MerchantTable.sKeyTwitterHandle, merchant.twitterHandle());
        values.put(MerchantTable.sKeyUsesPin,       merchant.usesPin());
        values.put(MerchantTable.sKeyLastUpdated,   merchant.lastUpdatedRaw());
        return values;
    }

    //-------------------------------------------------------------------------

    /**
     * @returns Mapping between database columns and values.
     */
    private ContentValues createContentValues(Location location)
    {
        ContentValues values = new ContentValues();
        values.put(LocationTable.sKeyId,          location.id());
        values.put(LocationTable.sKeyName,        location.name());
        values.put(LocationTable.sKeyAddress,     location.address());
        values.put(LocationTable.sKeyLatitude,    location.latitude());
        values.put(LocationTable.sKeyLongitude,   location.longitude());
        values.put(LocationTable.sKeyPhone,       location.phone());
        values.put(LocationTable.sKeyLastUpdated, location.lastUpdatedRaw());
        return values;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;
    private SQLiteDatabase       mDatabase;
    private TikTokDatabaseHelper mDatabaseHelper;

}
