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
import java.util.HashMap;
import java.util.Map;
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
import android.location.Location;
import android.util.Log;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

/**
 * [moiz] need to have all the networking and json parsing happening in the
 *      background
 *
 */

public final class TikTokApi
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final String kTikTokApiKeyStatus  = "status";
    public static final String kTikTokApiKeyError   = "error";
    public static final String kTikTokApiKeyResults = "results";

    public static final String kTikTokApiStatusOkay      = "OK";
    public static final String kTikTokApiStatusInvalid   = "INVALID REQUEST";
    public static final String kTikTokApiStatusForbidden = "FORBIDDEN";
    public static final String kTikTokApiStatusNotFound  = "NOT FOUND";

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

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public TikTokApi(Context context)
    {
        mContext = context.getApplicationContext();
    }

    //-------------------------------------------------------------------------
    // api
    //-------------------------------------------------------------------------

    /**
     * @return Url to TikTok website.
     */
    public static String getApiUrl()
    {
        // [moiz] implement some sort of debugging options
        boolean TIKTOKAPI_STAGING = true;

        if (TIKTOKAPI_STAGING) {
            return "https://furious-window-5155.herokuapp.com";
        } else {
            return "https://www.tiktok.com";
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Register the device with the server.
     */
    public long registerDevice(String deviceId)
    {
        // get route to register device with server
        String url = String.format("%s/register?uuid=%s", getApiUrl(), deviceId);

        // process request
        HttpGet request    = new HttpGet(url);
        InputStream stream = processRequest(request);

        try {

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            TikTokApiResponse response =
                mapper.readValue(stream, TikTokApiResponse.class);

            // parse the results if the query was a success
            if (response.isOkay()) {
                Map<String, Integer> results = mapper.convertValue(
                    response.getResults(),
                    new TypeReference<Map<String, Integer>>() {});
                return results.get("id").intValue();
            }

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format(
                "exception: %s", e.toString()));
        }

        return -1;
    }

    //-------------------------------------------------------------------------

    /**
     * Check if the device is registered with the server.
     */
    public boolean validateRegistration()
    {
        // get route to register device with server
        String url = String.format("%s/consumers/%d/registered?uuid=%s",
            getApiUrl(), utilities().getConsumerId(), utilities().getDeviceId());

        // process request
        HttpGet request    = new HttpGet(url);
        InputStream stream = processRequest(request);

        try {

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            TikTokApiResponse response =
                mapper.readValue(stream, TikTokApiResponse.class);

            // parse the results if the query was a success
            if (response.isOkay()) {
                Map<String, Boolean> results = mapper.convertValue(
                    response.getResults(),
                    new TypeReference<Map<String, Boolean>>() {});
                return results.get("registered").booleanValue();
            }

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format(
                "exception: %s", e.toString()));
        }

        return false;
    }

    //-------------------------------------------------------------------------

    /**
     * Register the notification token with the server.
     */
    public void registerNotificationToken(String token)
    {
        // [moiz] need to wait for the server to implement this before this
        // can be worked on
    }

    //-------------------------------------------------------------------------

    /**
     * @return Get the list of available coupons.
     */
    public Coupon[] syncActiveCoupons()
    {
        // get the route to the list of coupons
        String url = String.format("%s/consumers/%s/coupons",
            getApiUrl(), utilities().getConsumerId());

        // get the json content from the url
        HttpGet request    = new HttpGet(url);
        InputStream stream = processRequest(request);

        try {

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            TikTokApiResponse response = mapper.readValue(stream, TikTokApiResponse.class);
            if (response.isOkay()) {

                // convert the top level results structure
                Map<String, Object> results = mapper.convertValue(
                    response.getResults(), new TypeReference<Map<String, Object>>() {});

                // process the new coupons
                Coupon[] coupons  = mapper.convertValue(results.get("coupons"), Coupon[].class);
                Integer[] killed  = mapper.convertValue(results.get("killed"), Integer[].class);
                Integer[] soldOut = mapper.convertValue(results.get("sold_out"), Integer[].class);

                return coupons;
            }
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format("exception: %s", e.toString()));
        }

        return null;
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

        // parse the json data
        try {

            // add settings to request
            List<NameValuePair> pairs =
                new ArrayList<NameValuePair>(settings.size());
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            request.setEntity(new UrlEncodedFormEntity(pairs));

            // process request
            processRequest(request);

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format(
                "exception: %s", e.toString()));
        }
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
    public TikTokApiResponse updateCoupon(long couponId, CouponAttribute attribute)
    {
        // construct route to update coupon attribute
        String url = String.format("%s/consumers/%s/coupons/%d",
            getApiUrl(), utilities().getConsumerId(), couponId);

        // setup put request for desired attribute
        HttpPut request = new HttpPut(url);

        // parse the json data
        try {

            // add attribute to request
            List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
            pairs.add(new BasicNameValuePair(attribute.key(), "1"));
            request.setEntity(new UrlEncodedFormEntity(pairs));

            // process request
            InputStream stream = processRequest(request);

            // nothing to do if response did not go through
            if (stream == null) return null;

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            TikTokApiMultiResponse response =
                mapper.readValue(stream, TikTokApiMultiResponse.class);

            // return the response for the attribute that was updated
            return response.getResponse(attribute.key());

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format(
                "exception: %s", e.toString()));
        }

        return null;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Syncs the most current karma points stats from the server.
     */
    public Map<String, Integer> syncKarmaPoints()
    {
        // construct route to retrieve karma points
        String url = String.format("%s/consumers/%s/loyalty_points",
            getApiUrl(), utilities().getConsumerId());

        // pull data from the server
        HttpGet request    = new HttpGet(url);
        InputStream stream = processRequest(request);

        // nothing to do if response did not go through
        if (stream == null) return null;

        // parse the json data
        try {

            // parse the top level response structure
            ObjectMapper mapper = new ObjectMapper();
            TikTokApiResponse response =
                mapper.readValue(stream, TikTokApiResponse.class);

            // parse the results if the query was a success
            if (response.isOkay()) {
                Map<String, Integer> results = mapper.convertValue(
                    response.getResults(),
                    new TypeReference<Map<String, Integer>>() {});
                return results;
            }

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), String.format(
                "exception: %s", e.toString()));
        }

        return null;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Content from the uri request.
     */
    private InputStream processRequest(HttpUriRequest request)
    {
        try {

            // attempt to query the request from the server
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response    = client.execute(request);
            final int statusCode     = response.getStatusLine().getStatusCode();

            // make sure we get a success response
            if (statusCode != HttpStatus.SC_OK) {
                Log.w(getClass().getSimpleName(), String.format(
                    "Error: %d for url %s", statusCode, request.getURI()));
                return null;
            }

            HttpEntity responseEntity = response.getEntity();
            return responseEntity.getContent();

        } catch (IOException e) {
            request.abort();
            Log.w(getClass().getSimpleName(), String.format(
                "Error for URL: %s", request.getURI()));
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    private Utilities utilities()
    {
        if (mUtilities == null) {
            mUtilities = new Utilities(mContext);
        }
        return mUtilities;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context   mContext;
    private Utilities mUtilities;

}
