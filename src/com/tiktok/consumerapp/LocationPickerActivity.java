//-----------------------------------------------------------------------------
// LocationPickerActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.location.Location;
import android.view.KeyEvent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import org.codehaus.jackson.JsonNode;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class LocationPickerActivity extends MapActivity
{
    //-------------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------------

    public final String kLogTag = "LocationPickerActivity";

    public static final String kIntentExtraAddress   = "address";
    public static final String kIntentExtraLatitude  = "latitude";
    public static final String kIntentExtraLongitude = "longitude";

    //-------------------------------------------------------------------------
    // activity
    //-------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_picker);

        // add zoom controls
        MapView mapView = (MapView)findViewById(R.id.map_view);
        mapView.setBuiltInZoomControls(true);

        // add listener to check box
        EditText textBox = (EditText)findViewById(R.id.input);
        textBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search(v.getText().toString());
                return false;
            }
        });

        // zoom map to intent
        Intent intent = getIntent();
        String latitude  = intent.getStringExtra(kIntentExtraLatitude);
        String longitude = intent.getStringExtra(kIntentExtraLongitude);
        if ((latitude != null) && (longitude != null)) {
            centerMap(Double.valueOf(latitude).doubleValue(),
                      Double.valueOf(longitude).doubleValue());
            Log.i(kLogTag, String.format("Center location to intent: %s %s", latitude, longitude));

        // zoom map to current location
        } else {
            centerMapToCurrentLocation();
            Log.i(kLogTag, "Center location to user.");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to become visible.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity has become visible (it is now "resumed").
     */
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    //-------------------------------------------------------------------------

    /**
     * Another activity is taking focus (this activity is about to be "paused")
     */
    @Override
    protected void onPause()
    {
        super.onPause();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is no longer visible (it is now "stopped")
     */
    @Override
    protected void onStop()
    {
        super.onStop();
    }

    //-------------------------------------------------------------------------

    /**
     * The activity is about to be destroyed.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private void search(String search)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching...");
        progressDialog.show();

        final Handler handler = new Handler();
        GoogleMapsApi api = new GoogleMapsApi(this, handler, new GoogleMapsApi.CompletionHandler() {
            public void onSuccess(final JsonNode node) {
                progressDialog.cancel();
                centerMapToGeocoding(node);
            }
            public void onError(Throwable error) {
                progressDialog.cancel();
            }
        });

        // run query
        api.getGeocodingForAddress(search);
    }

    //-------------------------------------------------------------------------

    private void centerMapToGeocoding(JsonNode geoData)
    {
        // make sure search results exist
        String status = geoData.get("status") != null ? geoData.get("status").getTextValue() : null;
        if ((status == null) || status.equals("ZERO_RESULTS")) {
            String title   = getString(R.string.location_search);
            String message = getString(R.string.location_not_found);
            Utilities.displaySimpleAlert(this, title, message);
            return;
        }

        // grab the results from the json data
        JsonNode results   = geoData.get("results").get(0);
        JsonNode location  = results.get("geometry").get("location");
        JsonNode northEast = results.get("geometry").get("bounds").get("northeast");
        JsonNode southWest = results.get("geometry").get("bounds").get("southwest");
        double latitude        = location.get("lat").getDoubleValue();
        double longitude       = location.get("lng").getDoubleValue();
        double neLatitude      = northEast.get("lat").getDoubleValue();
        double neLongitude     = northEast.get("lng").getDoubleValue();
        double swLatitude      = southWest.get("lat").getDoubleValue();
        double swLongitude     = southWest.get("lng").getDoubleValue();

        // calculate center and region
        double latitudeSpan  = Math.abs(neLatitude - swLatitude) * 1.05;
        double longitudeSpan = Math.abs(neLongitude - swLongitude) * 1.05;

        // center map
        MapView mapView          = (MapView)findViewById(R.id.map_view);
        GeoPoint center          = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        MapController controller = mapView.getController();
        controller.setCenter(center);
        controller.zoomToSpan((int)(latitudeSpan * 1E6), (int)(longitudeSpan * 1E6));

        // update map
        mapView.invalidate();
    }

    //-------------------------------------------------------------------------
    // events
    //-------------------------------------------------------------------------

    public void onSave(View view)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        final Handler handler = new Handler();
        GoogleMapsApi api = new GoogleMapsApi(this, handler, new GoogleMapsApi.CompletionHandler() {
            public void onSuccess(final JsonNode node) {
                progressDialog.cancel();
                save(node);
            }
            public void onError(Throwable error) {
                progressDialog.cancel();
                save(null);
            }
        });

        //  grab center from map
        MapView mapView = (MapView)findViewById(R.id.map_view);
        GeoPoint center = mapView.getMapCenter();

        // run query
        Location location = new Location("");
        location.setLatitude((double)center.getLatitudeE6() / 1E6);
        location.setLongitude((double)center.getLongitudeE6() / 1E6);
        api.getReverseGeocodingForAddress(location);
    }

    //-------------------------------------------------------------------------

    public void onCancel(View view)
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    //-------------------------------------------------------------------------

    private void save(JsonNode node)
    {
        //  grab center from map
        MapView mapView = (MapView)findViewById(R.id.map_view);
        GeoPoint center = mapView.getMapCenter();

        // grab best location if possible
        String address = parseAddress(node);

        // convert latitude / longitude to double
        double latitude  = (double)center.getLatitudeE6() / 1E6;
        double longitude = (double)center.getLongitudeE6() / 1E6;

        // pack data in intent
        Intent intent = getIntent();
        intent.putExtra(kIntentExtraAddress, address);
        intent.putExtra(kIntentExtraLatitude, Double.toString(latitude));
        intent.putExtra(kIntentExtraLongitude, Double.toString(longitude));
        setResult(Activity.RESULT_OK, intent);

        // close activity
        finish();
    }

    //-------------------------------------------------------------------------

    private String parseAddress(JsonNode node)
    {
        final String[] keys = {
            "subpremise", "premise", "neighborhood",
            "sublocality", "locality", "colloquial_area",
            "administrative_area_level_3"
        };

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

    private void centerMap(double latitude, double longitude)
    {
        if ((latitude != 0.0) && (longitude != 0.0)) {
            MapView mapView          = (MapView)findViewById(R.id.map_view);
            MapController controller = mapView.getController();
            controller.setCenter(new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6)));
            controller.setZoom(15);
        }
    }

    //-------------------------------------------------------------------------

    private void centerMapToCurrentLocation()
    {
        LocationTracker tracker  = LocationTracker.getInstance(this);
        Location currentLocation = tracker.currentLocation();
        if (currentLocation != null) {
            double latitude  = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            centerMap(latitude, longitude);
        }
    }

    //-------------------------------------------------------------------------
    // MapsActivity
    //-------------------------------------------------------------------------

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

}


