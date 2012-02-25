//-----------------------------------------------------------------------------
// TikTokApi
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

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
     * @return Return a Guid that represents this device.
     */
    public String getDeviceId()
    {
        // [moiz] temp:: generate a uuid and print it out, use this static id
        //   until we can figure out a place to store the id that is similar
        //   to the keychain store on the iphone
        //String deviceId = Device.generateGUID();
        String deviceId = "838320de-f612-4358-9c8d-da2b81eeeec7";
        return deviceId;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Returns the consumer id regsitered with this device.
     */
    public long getConsumerId()
    {
        // [moiz] temp:: need to figure out where to store temp adata
        return 2;
    }

    //-------------------------------------------------------------------------

    /**
     * Register the device with the server.
     */
    public boolean registerDevice()
    {
        // get route to register device with server
        String url = String.format("%s/register?uuid=%s",
            getApiUrl(), getDeviceId());

        // process request
        InputStream stream = postHttpRequest(url);
        return stream != null;
    }

    //-------------------------------------------------------------------------

    /**
     * Check if the device is registered with the server.
     */
    public boolean validateRegistration()
    {
        // get route to register device with server
        String url = String.format("%s/consumers/%d/registered?uuid=%s",
            getApiUrl(), getConsumerId(), getDeviceId());

        // process request
        InputStream stream = postHttpRequest(url);
        return stream != null;
    }

    //-------------------------------------------------------------------------

    /**
     * Register the notification token with the server.
     */
    public void registerNotificationToken(String token)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * @return Get the list of available coupons.
     */
    public Coupon[] syncActiveCoupons()
    {
        // get the route to the list of coupons
        String url = String.format("%s/consumers/%s/coupons",
            getApiUrl(), getConsumerId());

        // get the json content from the url
        InputStream stream = getHttpRequest(url);

        // set the json parser
        Gson gson        = new Gson();
        Reader reader    = new InputStreamReader(stream);

        // [moiz] temp to test out coupon parsing... probably better to the
        // Jackson Json parser as the elements can be bound dynamically
        JsonElement element   = gson.fromJson(reader, JsonElement.class);
        JsonObject results    = element.getAsJsonObject().getAsJsonObject("results");
        JsonArray couponArray = results.getAsJsonArray("coupons");

        // reparse the coupons
        Coupon[] coupons = new Gson().fromJson(couponArray.toString(), Coupon[].class);

        return coupons;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Content from the get request.
     */
    public InputStream postHttpRequest(String url)
    {
        HttpPost request = new HttpPost(url);
        return processRequest(request);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Content from the post request.
     */
    public InputStream getHttpRequest(String url)
    {
        HttpGet request = new HttpGet(url);
        return processRequest(request);
    }

    //-------------------------------------------------------------------------

    /**
     * @return Content from the uri request.
     */
    public InputStream processRequest(HttpUriRequest request)
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
    // fields
    //-------------------------------------------------------------------------

}
