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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

    private ContentResolver contentResolver()
    {
        return mContext.getContentResolver();
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new coupon.
     * @returns The rowId for the new coupon that is created, otherwise a -1.
     */
    public Uri createCoupon(Coupon coupon)
    {
        return contentResolver().insert(
            CouponTable.kContentUri, coupon.contentValues());
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new merchant.
     * @returns The rowId for the new merchant that is created, otherwise a -1.
     */
    public Uri createMerchant(Merchant merchant)
    {
        return contentResolver().insert(
            MerchantTable.kContentUri, merchant.contentValues());
    }

    //-------------------------------------------------------------------------

    /**
     * Create a new location.
     * @returns The rowId for the new location that is created, otherwise a -1.
     */
    public Uri createLocation(Location location)
    {
        return contentResolver().insert(
            LocationTable.kContentUri, location.contentValues());
    }

    //-------------------------------------------------------------------------

    /**
     * Update an existing coupon.
     */
    public int updateCoupon(Coupon coupon)
    {
        Uri uri = ContentUris.withAppendedId(
            CouponTable.kContentUri, coupon.id());
        return contentResolver().update(uri, coupon.contentValues(), null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing merchant.
     */
    public int updateMerchant(Merchant merchant)
    {
        Uri uri = ContentUris.withAppendedId(
            MerchantTable.kContentUri, merchant.id());
        return contentResolver().update(uri, merchant.contentValues(), null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Updates an existing location.
     */
    public int updateLocation(Location location)
    {
        Uri uri = ContentUris.withAppendedId(
            LocationTable.kContentUri, location.id());
        return contentResolver().update(uri, location.contentValues(), null, null);
    }

    //-------------------------------------------------------------------------
    /**
     * Delete an existing coupon.
     */
    public int deleteCoupon(long id)
    {
        Uri uri = ContentUris.withAppendedId(CouponTable.kContentUri, id);
        return contentResolver().delete(uri, null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing merchant.
     */
    public int deleteMerchant(long id)
    {
        Uri uri = ContentUris.withAppendedId(MerchantTable.kContentUri, id);
        return contentResolver().delete(uri, null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Delete an existing location.
     */
    public int deleteLocation(long id)
    {
        Uri uri = ContentUris.withAppendedId(LocationTable.kContentUri, id);
        return contentResolver().delete(uri, null, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public Cursor fetchAllCoupons()
    {
        Cursor cursor = contentResolver().query(CouponTable.kContentUri,
            CouponTable.sFullProjection, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the merchants.
     */
    public Cursor fetchAllMerchants()
    {
        Cursor cursor = contentResolver().query(MerchantTable.kContentUri,
            MerchantTable.sFullProjection, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the locations from the database.
     * @returns Cursor over all the merchants.
     */
    public Cursor fetchAllLocations()
    {
        Cursor cursor = contentResolver().query(LocationTable.kContentUri,
            LocationTable.sFullProjection, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the coupons from the database.
     * @returns Cursor over all the coupons.
     */
    public List<Long> fetchAllCouponIds()
    {
        String projection[] = new String[] {
            CouponTable.sKeyId
        };

        Cursor cursor  = null;
        List<Long> ids = null;

        try {

            cursor = contentResolver().query(CouponTable.kContentUri,
                projection, null, null, null);
            cursor.moveToFirst();

            // create a list of ids
            ids = new ArrayList<Long>();
            for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return ids;
    }

    //-------------------------------------------------------------------------

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public Map<Long, Date> fetchAllMerchantIds()
    {
        String projection[] = new String[] {
            MerchantTable.sKeyId,
            MerchantTable.sKeyLastUpdated,
        };

        Cursor cursor       = null;
        Map<Long, Date> ids = null;

        try {

            // grab the data from the database
            cursor = contentResolver().query(MerchantTable.kContentUri,
                projection, null, null, null);
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

    /**
     * Fetch all the merchants from the database.
     * @returns Cursor over all the coupons.
     */
    public Map<Long, Date> fetchAllLocationIds()
    {
        String projection[] = new String[] {
            LocationTable.sKeyId,
            LocationTable.sKeyLastUpdated,
        };

        Cursor cursor       = null;
        Map<Long, Date> ids = null;

        try {

            // query database for location data
            cursor = contentResolver().query(LocationTable.kContentUri,
                projection, null, null, null);
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
            Uri uri = ContentUris.withAppendedId(
                CouponTable.kContentUri, id);
            cursor = contentResolver().query(uri,
                CouponTable.sFullProjection, null, null, null);
            cursor.moveToFirst();

            // grab locations
            int locationIndex        = cursor.getColumnIndex(CouponTable.sKeyLocations);
            String locationIds       = cursor.getString(locationIndex);
            List<Location> locations = fetchLocations(locationIds);

            // grab merchant
            int merchantIndex = cursor.getColumnIndex(CouponTable.sKeyMerchant);
            long merchantId   = cursor.getLong(merchantIndex);
            Merchant merchant = fetchMerchant(merchantId);

            // sync coupon object with cursor
            if ((merchant != null) && (locations != null) && (locations.size() > 0)) {
                coupon = new Coupon(cursor, locations, merchant);
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
            Uri uri = ContentUris.withAppendedId(
                MerchantTable.kContentUri, id);
            cursor = contentResolver().query(uri,
                MerchantTable.sFullProjection, null, null, null);
            cursor.moveToFirst();

            // sync merchant object with cursor
            merchant = new Merchant(cursor);

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
            String selection = String.format("%s in (%s)", LocationTable.sKeyId, ids);
            cursor = contentResolver().query(LocationTable.kContentUri,
                LocationTable.sFullProjection, selection, null, null);
            cursor.moveToFirst();

            // sync location object with cursor
            locations = new ArrayList<Location>();
            for ( ; !cursor.isAfterLast(); cursor.moveToNext()) {
                locations.add(new Location(cursor));
            }

        // cleanup
        } finally {
            if (cursor != null) cursor.close();
        }

        return locations;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context              mContext;

}
