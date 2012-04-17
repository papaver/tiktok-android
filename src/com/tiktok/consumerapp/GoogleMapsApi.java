//-----------------------------------------------------------------------------
// GoogleMapsApi
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.Handler;
import android.location.Location;
import android.util.Log;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class GoogleMapsApi
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final String kLogTag = "GoogleMapsApi";

    //-------------------------------------------------------------------------
    // CompletionHandler
    //-------------------------------------------------------------------------

    public interface CompletionHandler
    {
        public abstract void onSuccess(final JsonNode node);
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
        }

        public void run()
        {
            try {

                // attempt to query the request from the server
                DefaultHttpClient client = new DefaultHttpClient();
                HttpResponse response    = client.execute(mRequest);
                Object data              = parseResponse(response, mType);

                // run completion handler
                if (mHandler != null) mHandler.onSuccess(data);

            } catch (Exception e) {
                Log.e("GoogleMapsApi::Downloader", String.format(
                    "Query failed for %s", mRequest.getURI()), e);

                // kill the connection
                mRequest.abort();

                // run completion handler
                if (mHandler != null) mHandler.onError(e);
            }
        }

        private final Class<?>        mType;
        private final HttpUriRequest  mRequest;
        private final DownloadHandler mHandler;
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public GoogleMapsApi(Context context, Handler handler,
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
     * @return Url to Google Maps Api website.
     */
    public static String getApiUrl()
    {
        return "http://maps.google.com/maps";
    }

    //-------------------------------------------------------------------------

    public static String urlForForwardGeocodingForAddress(String address)
    {
        // properly encode/format the address
        String formatted = GoogleMapsApi.formatString(address);

        // construct the url path
        String url = String.format("%s/api/geocode/json?sensor=false&address=%s",
            GoogleMapsApi.getApiUrl(), formatted);

        return url;
    }

    //-------------------------------------------------------------------------

    public static String urlForReverseGeocodingForAddress(Location coordinate)
    {
        // construct the url path
        String url = String.format("%s/api/geocode/json?sensor=false&latlng=%f,%f",
            GoogleMapsApi.getApiUrl(), coordinate.getLatitude(), coordinate.getLongitude());

        return url;
    }

    //-------------------------------------------------------------------------

    public static String formatString(String string)
    {
        return string.replace(" ", "+");
    }

    //-------------------------------------------------------------------------

    /**
     * Returns a dictionary containing geocoding information about the givin
     * coordinate.
     */
    public void getGeocodingForAddress(String address)
    {
        String url = GoogleMapsApi.urlForForwardGeocodingForAddress(address);

        // query the server
        HttpGet request = new HttpGet(url);
        new Thread(new Downloader(request, JsonNode.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {
                    postSuccess((JsonNode)data);
                }
                public void onError(Throwable error) {
                    postError(error);
                }
            })).start();
    }

    //-------------------------------------------------------------------------

    /**
     * Check if the device is registered with the server.
     */
    public void getReverseGeocodingForAddress(Location location)
    {
        String url = GoogleMapsApi.urlForReverseGeocodingForAddress(location);

        // query the server
        HttpGet request = new HttpGet(url);
        new Thread(new Downloader(request, JsonNode.class,
            new DownloadHandler() {
                public void onSuccess(final Object data) {
                    postSuccess((JsonNode)data);
                }
                public void onError(Throwable error) {
                    postError(error);
                }
            })).start();
    }

    //-------------------------------------------------------------------------
    // methods
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

    private void postSuccess(final JsonNode node)
    {
        if (mCompletionHandler == null) return;

        mHandler.post(new Runnable() {
            public void run() {
                mCompletionHandler.onSuccess(node);
            }
        });
    }

    //-------------------------------------------------------------------------

    private void postError(final Throwable error)
    {
        if (mCompletionHandler == null) return;

        mHandler.post(new Runnable() {
            public void run() {
                mCompletionHandler.onError(error);
            }
        });
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    @SuppressWarnings("unused")
    final private Context           mContext;
    final private Handler           mHandler;
    final private CompletionHandler mCompletionHandler;
}
