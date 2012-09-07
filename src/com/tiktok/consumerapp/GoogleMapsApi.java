//-----------------------------------------------------------------------------
// GoogleMapsApi
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    public static String parseLocality(JsonNode node)
    {
        final String[] keys = {
            "subpremise", "premise", "neighborhood",
            "sublocality", "locality", "colloquial_area",
            "administrative_area_level_3"
        };

        // make sure data exists
        if (node == null) return "Unknown";

        // make sure search results exist
        String status = node.get("status") != null ? node.get("status").getTextValue() : null;
        if ((status == null) || status.equals("ZERO_RESULTS")) {
            return "Unknown";
        }

        // grab the results from the json data
        JsonNode results = node.get("results");

        // loop through all of the results and get as many fits as possbile
        Map<String, String> localities = new HashMap<String, String>();
        for (JsonNode address : results) {
            JsonNode components = address.get("address_components");
            for (JsonNode component : components) {
                for (String key : keys) {

                    // skip if the key was already found
                    if (localities.containsKey(key)) continue;

                    // add key if it matches the type
                    JsonNode types = component.get("types");
                    for (JsonNode type : types) {
                        if (type.getTextValue().equals(key)) {
                            String name = component.get("short_name").getTextValue();
                            localities.put(key, name);
                        }
                    }
                }
            }
        }

        // go through the list and find the smallest locality
        String locality = "Unknown";
        for (String key : keys) {
            String value = localities.get(key);
            if (value != null) {
                locality = value;
                break;
            }
        }

        return locality;
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
