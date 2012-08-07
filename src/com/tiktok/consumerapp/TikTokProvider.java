//-----------------------------------------------------------------------------
// TikTokProvider
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokProvider extends ContentProvider
{

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "TikTokProvider";

    //-------------------------------------------------------------------------

    private static final int kCoupons    = 1;
    private static final int kCouponId   = 2;
    private static final int kMerchants  = 3;
    private static final int kMerchantId = 4;
    private static final int kLocations  = 5;
    private static final int kLocationId = 6;

    private static final UriMatcher sUriMatcher;

    private static HashMap<String, String> sCouponProjectionMap;
    private static HashMap<String, String> sMerchantProjectionMap;
    private static HashMap<String, String> sLocationProjectionMap;

    //-------------------------------------------------------------------------
    // static initilization
    //-------------------------------------------------------------------------

    static
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Constants.kAuthority, "coupons",     kCoupons);
        sUriMatcher.addURI(Constants.kAuthority, "coupons/#",   kCouponId);
        sUriMatcher.addURI(Constants.kAuthority, "merchants",   kMerchants);
        sUriMatcher.addURI(Constants.kAuthority, "merchants/#", kMerchantId);
        sUriMatcher.addURI(Constants.kAuthority, "locations",   kLocations);
        sUriMatcher.addURI(Constants.kAuthority, "locations/#", kLocationId);

        // coupon
        sCouponProjectionMap = new HashMap<String, String>();
        sCouponProjectionMap.put(CouponTable.sKeyId,           CouponTable.sKeyId);
        sCouponProjectionMap.put(CouponTable.sKeyTitle,        CouponTable.sKeyTitle);
        sCouponProjectionMap.put(CouponTable.sKeyDetails,      CouponTable.sKeyDetails);
        sCouponProjectionMap.put(CouponTable.sKeyIconId,       CouponTable.sKeyIconId);
        sCouponProjectionMap.put(CouponTable.sKeyIconUrl,      CouponTable.sKeyIconUrl);
        sCouponProjectionMap.put(CouponTable.sKeyStartTime,    CouponTable.sKeyStartTime);
        sCouponProjectionMap.put(CouponTable.sKeyEndTime,      CouponTable.sKeyEndTime);
        sCouponProjectionMap.put(CouponTable.sKeyBarcode,      CouponTable.sKeyBarcode);
        sCouponProjectionMap.put(CouponTable.sKeyIsSoldOut,    CouponTable.sKeyIsSoldOut);
        sCouponProjectionMap.put(CouponTable.sKeyWasRedeemed,  CouponTable.sKeyWasRedeemed);
        sCouponProjectionMap.put(CouponTable.sKeyIsRedeemable, CouponTable.sKeyIsRedeemable);
        sCouponProjectionMap.put(CouponTable.sKeyLocations,    CouponTable.sKeyLocations);
        sCouponProjectionMap.put(CouponTable.sKeyMerchant,     CouponTable.sKeyMerchant);

        // merchant
        sMerchantProjectionMap = new HashMap<String, String>();
        sMerchantProjectionMap.put(MerchantTable.sKeyId,            MerchantTable.sKeyId);
        sMerchantProjectionMap.put(MerchantTable.sKeyName,          MerchantTable.sKeyName);
        sMerchantProjectionMap.put(MerchantTable.sKeyCategory,      MerchantTable.sKeyCategory);
        sMerchantProjectionMap.put(MerchantTable.sKeyDetails,       MerchantTable.sKeyDetails);
        sMerchantProjectionMap.put(MerchantTable.sKeyIconId,        MerchantTable.sKeyIconId);
        sMerchantProjectionMap.put(MerchantTable.sKeyIconUrl,       MerchantTable.sKeyIconUrl);
        sMerchantProjectionMap.put(MerchantTable.sKeyWebsiteUrl,    MerchantTable.sKeyWebsiteUrl);
        sMerchantProjectionMap.put(MerchantTable.sKeyTwitterHandle, MerchantTable.sKeyTwitterHandle);
        sMerchantProjectionMap.put(MerchantTable.sKeyUsesPin,       MerchantTable.sKeyUsesPin);
        sMerchantProjectionMap.put(MerchantTable.sKeyLastUpdated,   MerchantTable.sKeyLastUpdated);

        // location
        sLocationProjectionMap = new HashMap<String, String>();
        sLocationProjectionMap.put(LocationTable.sKeyId,          LocationTable.sKeyId);
        sLocationProjectionMap.put(LocationTable.sKeyName,        LocationTable.sKeyName);
        sLocationProjectionMap.put(LocationTable.sKeyAddress,     LocationTable.sKeyAddress);
        sLocationProjectionMap.put(LocationTable.sKeyLatitude,    LocationTable.sKeyLatitude);
        sLocationProjectionMap.put(LocationTable.sKeyLongitude,   LocationTable.sKeyLongitude);
        sLocationProjectionMap.put(LocationTable.sKeyPhone,       LocationTable.sKeyPhone);
        sLocationProjectionMap.put(LocationTable.sKeyLastUpdated, LocationTable.sKeyLastUpdated);
    }

    //-------------------------------------------------------------------------
    // content provider
    //-------------------------------------------------------------------------

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new TikTokDatabaseHelper(getContext());
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri))
        {
            case kCoupons:
                queryBuilder.setTables(CouponTable.sName);
                queryBuilder.setProjectionMap(sCouponProjectionMap);
                break;

            case kCouponId:
                queryBuilder.setTables(CouponTable.sName);
                queryBuilder.setProjectionMap(sCouponProjectionMap);
                queryBuilder.appendWhere(String.format("%s = %s",
                    CouponTable.sKeyId, uri.getPathSegments().get(1)));
                break;

            case kMerchants:
                queryBuilder.setTables(MerchantTable.sName);
                queryBuilder.setProjectionMap(sMerchantProjectionMap);
                break;

            case kMerchantId:
                queryBuilder.setTables(MerchantTable.sName);
                queryBuilder.setProjectionMap(sMerchantProjectionMap);
                queryBuilder.appendWhere(String.format("%s = %s",
                    MerchantTable.sKeyId, uri.getPathSegments().get(1)));
                break;

            case kLocations:
                queryBuilder.setTables(LocationTable.sName);
                queryBuilder.setProjectionMap(sLocationProjectionMap);
                break;

            case kLocationId:
                queryBuilder.setTables(LocationTable.sName);
                queryBuilder.setProjectionMap(sLocationProjectionMap);
                queryBuilder.appendWhere(String.format("%s = %s",
                    LocationTable.sKeyId, uri.getPathSegments().get(1)));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy = sortOrder;

        // get the database and run the query
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database,
            projection, selection, selectionArgs, null, null, orderBy);

        // tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //-------------------------------------------------------------------------

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
            case kCoupons:
                return CouponTable.kContentType;
            case kCouponId:
                return CouponTable.kContentTypeItem;

            case kMerchants:
                return MerchantTable.kContentType;
            case kMerchantId:
                return MerchantTable.kContentTypeItem;

            case kLocations:
                return LocationTable.kContentType;
            case kLocationId:
                return LocationTable.kContentTypeItem;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    //-------------------------------------------------------------------------

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        // make sure content values exist
        if (values == null) {
            throw new IllegalArgumentException("Content values cannot be null.");
        }

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        long id;
        Uri contentUri;
        switch (sUriMatcher.match(uri))
        {
            case kCoupons:
                id         = CouponTable.insert(database, values);
                contentUri = CouponTable.kContentUri;
                break;

            case kMerchants:
                id         = MerchantTable.insert(database, values);
                contentUri = MerchantTable.kContentUri;
                break;

            case kLocations:
                id         = LocationTable.insert(database, values);
                contentUri = LocationTable.kContentUri;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(contentUri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    //-------------------------------------------------------------------------

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri))
        {
            case kCoupons:
                count = CouponTable.update(database, values, where, whereArgs);
                break;
            case kCouponId:
                String couponId = uri.getPathSegments().get(1);
                count = CouponTable.updateById(database, couponId, values, where, whereArgs);
                break;

            case kMerchants:
                count = MerchantTable.update(database, values, where, whereArgs);
                break;
            case kMerchantId:
                String merchantId = uri.getPathSegments().get(1);
                count = MerchantTable.updateById(database, merchantId, values, where, whereArgs);
                break;

            case kLocations:
                count = LocationTable.update(database, values, where, whereArgs);
                break;
            case kLocationId:
                String locationId = uri.getPathSegments().get(1);
                count = LocationTable.updateById(database, locationId, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //-------------------------------------------------------------------------

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri))
        {
            case kCoupons:
                count = CouponTable.delete(database, where, whereArgs);
                break;
            case kCouponId:
                String couponId = uri.getPathSegments().get(1);
                count = CouponTable.deleteById(database, couponId, where, whereArgs);
                break;

            case kMerchants:
                count = MerchantTable.delete(database, where, whereArgs);
                break;
            case kMerchantId:
                String merchantId = uri.getPathSegments().get(1);
                count = MerchantTable.deleteById(database, merchantId, where, whereArgs);
                break;

            case kLocations:
                count = LocationTable.delete(database, where, whereArgs);
                break;
            case kLocationId:
                String locationId = uri.getPathSegments().get(1);
                count = LocationTable.deleteById(database, locationId, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private TikTokDatabaseHelper mOpenHelper;

}
