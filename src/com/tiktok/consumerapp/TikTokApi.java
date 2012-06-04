//-----------------------------------------------------------------------------
// TikTokApi
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Handler;
import android.location.Location;
import android.util.Log;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class TikTokApi
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final String kLogTag = "TikTokApi";

    public static final String kTikTokApiKeyStatus  = "status";
    public static final String kTikTokApiKeyError   = "error";
    public static final String kTikTokApiKeyResults = "results";

    public static final String kTikTokApiStatusOkay      = "OK";
    public static final String kTikTokApiStatusInvalid   = "INVALID REQUEST";
    public static final String kTikTokApiStatusForbidden = "FORBIDDEN";
    public static final String kTikTokApiStatusNotFound  = "NOT_FOUND";

    //-------------------------------------------------------------------------
    // enums
    //-------------------------------------------------------------------------

    public enum CouponAttribute
    {
        kRedeem     ("redeem"),
        kFacebook   ("fb"),
        kTwitter    ("tw"),
        kGooglePlus ("gplus"),
        kSMS        ("sms"),
        kEmail      ("email");

        CouponAttribute(String key) { mKey = key; }
        public String key() { return mKey; }
        private final String mKey;
    }

    public enum CouponKey
    {
        kCoupons ("coupons"),
        kKilled  ("killed"),
        kSoldOut ("sold_out");

        CouponKey(String key) { mKey = key; }
        public String key() { return mKey; }
        private final String mKey;
    }

    //-------------------------------------------------------------------------
    // CompletionHandler
    //-------------------------------------------------------------------------

    public interface CompletionHandler
    {
        public abstract void onSuccess(final Object drawable);
        public abstract void onError(Throwable error);
    }

    //-------------------------------------------------------------------------
    // DownloadHandler
    //-------------------------------------------------------------------------

    private interface DownloadHandler
    {
        public abstract void onSuccess(final Object data);
        public abstract void onError(final Throwable error);
    }

    //-------------------------------------------------------------------------
    // Downloader
    //-------------------------------------------------------------------------

    private class Downloader implements Runnable
    {
        public Downloader(HttpUriRequest request, Class<?> type,
                          DownloadHandler handler)
        {
            mType     = type;
            mHandler  = handler;
            mRequest  = request;
            mClient   = new DefaultHttpClient();
        }

        public void run()
        {
            Thread thread = Thread.currentThread();

            try {

                // attempt to query the request from the server
                HttpResponse response = mClient.execute(mRequest);

                // parse the response
                if (thread.isInterrupted()) return;
                Object data = parseResponse(response, mType);

                // run completion handler
                if (!thread.isInterrupted() && (mHandler != null)) {
                    mHandler.onSuccess(data);
                }

            } catch (Exception e) {
                Log.e("TikTokApi::Downloader", String.format(
                    "Query failed for %s", mRequest.getURI()), e);

                // kill the connection
                mRequest.abort();

                // run completion handler
                if (!thread.isInterrupted() && (mHandler != null)) {
                    mHandler.onError(e);
                }
            }
        }

        public void interrupt()
        {
            mClient.getConnectionManager().shutdown();
        }

        private final Class<?>          mType;
        private final DefaultHttpClient mClient;
        private final DownloadHandler   mHandler;
        private final HttpUriRequest    mRequest;
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public TikTokApi(Context context, Handler handler,
                     CompletionHandler completionHandler)
    {
        mContext           = context.getApplicationContext();
        mHandler           = handler;
        mCompletionHandler = completionHandler;
    }

    //-------------------------------------------------------------------------
    // api
    //-------------------------------------------------------------------------

    /**
     * @return Url to TikTok website.
     */
    public static String getApiUrl()
    {
        if (Debug.kTikTokApiStaging) {
            return "https://furious-window-5155.herokuapp.com";
        } else {
            return "https://www.tiktok.com";
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Register the device with the server.
     */
    public void registerDevice(String deviceId)
    {
        // get route to register device with server
        String url = String.format("%s/register?uuid=%s", getApiUrl(), deviceId);

        // query the server
        HttpGet request       = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {

                    // parse out the consumerId
                    String consumerId = null;
                    TikTokApiResponse response = (TikTokApiResponse)data;
                    if (response.isOkay()) {
                        ObjectMapper mapper          = new ObjectMapper();
                        Map<String, Integer> results = mapper.convertValue(
                            response.getResults(),
                            new TypeReference<Map<String, Integer>>() {});
                        consumerId = results.get("id").toString();
                    }

                    // run handler
                    postSuccess(consumerId);
                }

                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * Check if the device is registered with the server.
     */
    public void validateRegistration()
    {
        // get route to register device with server
        String url = String.format("%s/consumers/%s/registered?uuid=%s",
            getApiUrl(), utilities().getConsumerId(), utilities().getDeviceId());

        // query the server
        HttpGet request = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {

                    // parse out the consumerId
                    Boolean isRegistered = null;
                    TikTokApiResponse response = (TikTokApiResponse)data;
                    if (response.isOkay()) {
                        ObjectMapper mapper          = new ObjectMapper();
                        Map<String, Boolean> results = mapper.convertValue(
                            response.getResults(),
                            new TypeReference<Map<String, Boolean>>() {});
                        isRegistered = results.get("registered");
                    }

                    // run handler
                    postSuccess(isRegistered);
                }

                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * Register the notification token with the server.
     */
    public void registerNotificationToken(String token)
    {
        // add to hash
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("registration_id", token);

        // update server
        updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Get the list of available coupons.
     */
    public void syncActiveCoupons(Date date)
    {
        // get the route to the list of coupons
        String url = String.format("%s/consumers/%s/coupons",
            getApiUrl(), utilities().getConsumerId());

        // add time if needed
        if (date != null) {
            url = String.format("%s?min_time=%f", url, (float)date.getTime() / 1000.0f);
        }

        // query the server
        HttpGet request = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {

                    // parse the coupon data on another thread
                    new Thread(new Runnable() {
                        public void run() {
                            processCouponData((TikTokApiResponse)data);
                        }
                    }).start();
                }

                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Updates server with consumers current location, currently
     *   ignores the response from the server.
     */
    public void updateCurrentLocation(Location location)
    {
        // convert location into string
        String latitude  = String.format("%f", location.getLatitude());
        String longitude = String.format("%f", location.getLongitude());

        // add to hash
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("latitude", latitude);
        settings.put("longitude", longitude);

        // update server
        updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Updates server with consumer settings, currently ignores the
     *   response from the server.
     */
    public void updateSettings(Map<String, String> settings)
    {
        // construct route to update coupon attribute
        String url = String.format("%s/consumers/%s",
            getApiUrl(), utilities().getConsumerId());

        // setup put request for desired attribute
        HttpPut request = new HttpPut(url);

        // add settings to request
        List<NameValuePair> pairs =
            new ArrayList<NameValuePair>(settings.size());
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        // add data to the entity
        try {
            request.setEntity(new UrlEncodedFormEntity(pairs));
        } catch (Exception e) {
        }

        // query the server
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {
                    postSuccess(data);
                }
                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Updates server with consumers home location, currently
     *   ignores the response from the server.
     */
    public void updateHomeLocation(Location location)
    {
        // convert location into string
        String latitude  = String.format("%f", location.getLatitude());
        String longitude = String.format("%f", location.getLongitude());

        // add to hash
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("home_latitude", latitude);
        settings.put("home_longitude", longitude);

        // update server
        updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Updates server with consumers work location, currently
     *   ignores the response from the server.
     */
    public void updateWorkLocation(Location location)
    {
        // convert location into string
        String latitude  = String.format("%f", location.getLatitude());
        String longitude = String.format("%f", location.getLongitude());

        // add to hash
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("work_latitude", latitude);
        settings.put("work_longitude", longitude);

        // update server
        updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Update coupon attribute.
     */
    public void updateCoupon(long couponId, final CouponAttribute attribute)
    {
        // construct route to update coupon attribute
        String url = String.format("%s/consumers/%s/coupons/%d",
            getApiUrl(), utilities().getConsumerId(), couponId);

        // setup put request for desired attribute
        HttpPut request = new HttpPut(url);

        // add attribute to request
        List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
        pairs.add(new BasicNameValuePair(attribute.key(), "1"));

        // add data to entity
        try {
            request.setEntity(new UrlEncodedFormEntity(pairs));
        } catch (Exception e) {
        }

        // query the server
        Downloader downloader = new Downloader(request, TikTokApiMultiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {
                    TikTokApiMultiResponse response = (TikTokApiMultiResponse)data;
                    postSuccess(response.getResponse(attribute.key()));
                }
                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Syncs the most current karma points stats from the server.
     */
    public void syncKarmaPoints()
    {
        // construct route to retrieve karma points
        String url = String.format("%s/consumers/%s/loyalty_points",
            getApiUrl(), utilities().getConsumerId());

        // query the server
        HttpGet request = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {

                    // parse out the karma points
                    Map<String, Integer> karma = null;
                    TikTokApiResponse response   = (TikTokApiResponse)data;
                    if (response.isOkay()) {
                        ObjectMapper mapper = new ObjectMapper();
                        karma = mapper.convertValue(
                            response.getResults(),
                            new TypeReference<Map<String, Integer>>() {});
                    }

                    // run handler
                    postSuccess(karma);
                }

                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    public void redeemPromotion(String promoCode)
    {
        String url = String.format("%s/consumers/%s/promotions/redeem?code=%s",
            getApiUrl(), utilities().getConsumerId(), promoCode.replaceAll("[^a-zA-Z0-9_\\-\\.\\*]", ""));

        // query the server
        HttpGet request = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {
                    postSuccess(data);
                }
                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Syncs the city list.
     */
    public void syncCities()
    {
        // construct route to retrieve karma points
        String url = String.format("%s/cities", getApiUrl());

        // query the server
        HttpGet request = new HttpGet(url);
        Downloader downloader = new Downloader(request, TikTokApiResponse.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {

                    // parse out the city data
                    Map<String, List<String>> cities = null;
                    TikTokApiResponse response  = (TikTokApiResponse)data;
                    if (response.isOkay()) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode cityNodes  = mapper.convertValue(
                            response.getResults(),
                            new TypeReference<JsonNode>() {});

                        // pull out city names
                        cities = new HashMap<String, List<String>>();
                        for (String key : new String[] { "live", "beta", "soon" }) {
                            List<String> cityList = new LinkedList<String>();
                            for (JsonNode city : cityNodes.get(key)) {
                                cityList.add(city.get("name").getTextValue());
                            }
                            cities.put(key, cityList);
                        }
                    }

                    // run handler
                    postSuccess(cities);
                }

                public void onError(Throwable error) {
                    postError(error);
                }
            });

        runOnThread(downloader);
    }

    //-------------------------------------------------------------------------

    public void cancel()
    {
        if (mDownloader != null) mDownloader.interrupt();
        if (mThread != null) mThread.interrupt();
        mHandler = null;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    private void runOnThread(Downloader downloader)
    {
        mDownloader = downloader;
        mThread     = new Thread(downloader);
        mThread.start();
    }

    //-------------------------------------------------------------------------

    private Object parseResponse(HttpResponse response, Class<?> type)
        throws IOException, org.codehaus.jackson.map.JsonMappingException
    {
        // make sure we get a success response
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {

            // grab the content from the response
            HttpEntity responseEntity = response.getEntity();
            InputStream stream        = responseEntity.getContent();

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            Object value        = mapper.readValue(stream, type);
            return value;

        } else {
            Log.e(kLogTag, String.format("Invalid response: %d", statusCode));
        }

        return null;
    }

    //-------------------------------------------------------------------------

    private void postSuccess(final Object response)
    {
        if (Thread.currentThread().isInterrupted()) return;
        if ((mCompletionHandler == null) || (mHandler == null)) return;

        mHandler.post(new Runnable() {
            public void run() {
                mCompletionHandler.onSuccess(response);
            }
        });
    }

    //-------------------------------------------------------------------------

    private void postError(final Throwable error)
    {
        if (Thread.currentThread().isInterrupted()) return;
        if ((mCompletionHandler == null) || (mHandler == null)) return;

        mHandler.post(new Runnable() {
            public void run() {
                mCompletionHandler.onError(error);
            }
        });
    }

    //-------------------------------------------------------------------------

    private Map<CouponKey, Object> repackCouponData(TikTokApiResponse response)
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> results = mapper.convertValue(
            response.getResults(), new TypeReference<Map<String, Object>>() {});

        // process the new coupons
        Coupon[] coupons  = mapper.convertValue(
            results.get(CouponKey.kCoupons.key()), Coupon[].class);
        Long[] killed  = mapper.convertValue(
            results.get(CouponKey.kKilled.key()), Long[].class);
        Long[] soldOut = mapper.convertValue(
            results.get(CouponKey.kSoldOut.key()), Long[].class);

        // pack into map
        Map<CouponKey, Object> data = new HashMap<CouponKey, Object>();
        data.put(CouponKey.kCoupons, coupons);
        data.put(CouponKey.kKilled, killed);
        data.put(CouponKey.kSoldOut, soldOut);

        return data;
    }

    //-------------------------------------------------------------------------

    private void processCouponData(TikTokApiResponse response)
    {
        // nothing to do if the response is empty
        if (response == null) return;

        // process the coupons
        if (response.isOkay()) {

            // open up a database connection
            TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(mContext);
            adapter.open();

            // repack the data for easier processing
            Map<CouponKey, Object> data = repackCouponData(response);

            // process new coupons
            Coupon[] coupons = (Coupon[])data.get(TikTokApi.CouponKey.kCoupons);
            processCoupons(coupons, adapter);

            // kill coupons
            Long[] killed = (Long[])data.get(TikTokApi.CouponKey.kKilled);
            processKilled(killed, adapter);

            // update sold out coupons
            Long[] soldOut = (Long[])data.get(TikTokApi.CouponKey.kSoldOut);
            processSoldOut(soldOut, adapter);

            // cleanup
            adapter.close();
        }

        // run handler
        postSuccess(response);
    }

    //-------------------------------------------------------------------------

    private void processCoupons(Coupon[] coupons, TikTokDatabaseAdapter adapter)
    {
        if (Thread.currentThread().isInterrupted()) return;

        // add only new coupons to the database
        List<Long> couponIds        = adapter.fetchAllCouponIds();
        Map<Long, Date> merchantIds = adapter.fetchAllMerchantIds();
        for (final Coupon coupon : coupons) {

            if (!merchantIds.keySet().contains(coupon.merchant().id())) {
                adapter.createMerchant(coupon.merchant());
                merchantIds.put(coupon.merchant().id(), coupon.merchant().lastUpdated());
                Log.i(kLogTag, String.format(
                    "Added merchant to db: %s", coupon.merchant().name()));
            } else {
                Date lastUpdated = merchantIds.get(coupon.merchant().id());
                if (lastUpdated.compareTo(coupon.merchant().lastUpdated()) < 0) {
                    adapter.updateMerchant(coupon.merchant());
                    merchantIds.put(coupon.merchant().id(), coupon.merchant().lastUpdated());
                    Log.i(kLogTag, String.format(
                        "Updated merchant in db: %s", coupon.merchant().name()));
                }
            }

            if (!couponIds.contains(coupon.id())) {
                Log.w(getClass().getSimpleName(), coupon.toString());
                adapter.createCoupon(coupon);
                Log.i(kLogTag, String.format(
                    "Added coupon to db: %s", coupon.title()));
            }
        }
    }

    //-------------------------------------------------------------------------

    private void processKilled(Long[] killed, TikTokDatabaseAdapter adapter)
    {
        if (Thread.currentThread().isInterrupted()) return;

        List<Long> couponIds = adapter.fetchAllCouponIds();
        for (final Long id : killed) {
            if (couponIds.contains(id)) {
                Log.i(kLogTag, String.format("Killed deal id: %d", id));
                adapter.deleteCoupon(id);
            }
        }
    }

    //-------------------------------------------------------------------------

    private void processSoldOut(Long[] soldOut, TikTokDatabaseAdapter adapter)
    {
        if (Thread.currentThread().isInterrupted()) return;

        List<Long> couponIds = adapter.fetchAllCouponIds();
        for (final Long id : soldOut) {
            if (couponIds.contains(id)) {
                Coupon coupon = adapter.fetchCoupon(id);
                if (!coupon.isSoldOut()) {
                    Log.i(kLogTag, String.format(
                        "SoldOut deal: %d / %s", coupon.id(), coupon.title()));
                    coupon.sellOut();
                    adapter.updateCoupon(coupon);
                }
            }
        }
    }

    //-------------------------------------------------------------------------

    private Utilities utilities()
    {
        if (mUtilities == null) mUtilities = new Utilities(mContext);
        return mUtilities;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Handler           mHandler;
    private Context           mContext;
    private CompletionHandler mCompletionHandler;

    private Thread     mThread;
    private Downloader mDownloader;
    private Utilities  mUtilities;
}
