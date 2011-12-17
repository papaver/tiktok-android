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
import android.provider.Settings.Secure;

import com.google.gson.Gson;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class TikTokApi 
{
    
    /**
     * @return Url to TikTok website.
     */
    public static String getApiUrl()
    {
        return "http://electric-dusk-7349.herokuapp.com";
    }

    //-------------------------------------------------------------------------
    
    /**
     * @return The unique push notification identifier for this device.
     */
    public String getDeviceId()
    {
        //String id 
            //= Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        //Log.w(getClass().getSimpleName(), "Android DeviceId = " + id);

        String deviceId = "4ebb7e88 4dcccb1c 8e407ae3 eaed0650 8919066d a1b8b6af 62298351 38801311";
        Log.w(getClass().getSimpleName(), "iPhone DeviceId = " + deviceId);
        return deviceId.replaceAll(" ", "%20");
    }

    //-------------------------------------------------------------------------

    /**
     * Register the device with the server.
     */
    public boolean registerDevice()
    {
        // get route to register device with server
        String url = String.format("%s/register?token=%s", 
            getApiUrl(), getDeviceId());

        // process request
        InputStream stream = postHttpRequest(url);
        return stream != null;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Get the list of available coupons.
     */
    public Coupon[] getCoupons()
    {
        // get the route to the list of coupons
        String url = String.format("%s/coupons?token=%s", 
            getApiUrl(), getDeviceId());

        // get the json content from the url
        InputStream stream = getHttpRequest(url);

        // set the json parser
        Gson gson        = new Gson();
        Reader reader    = new InputStreamReader(stream);
        Coupon[] coupons = gson.fromJson(reader, Coupon[].class);
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
