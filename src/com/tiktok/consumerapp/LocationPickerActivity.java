//-----------------------------------------------------------------------------
// LocationPickerActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.location.Location;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import org.codehaus.jackson.JsonNode;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class LocationPickerActivity extends SherlockMapActivity
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

        // set action bar
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Set Location");
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflator = getSupportMenuInflater();
        inflator.inflate(R.menu.location_picker, menu);
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onCancel();
                return true;
            case R.id.user_location:
                centerMapToCurrentLocation();
                return true;
            case R.id.save:
                onSave();
                return true;
            default:
                return false;
        }
    }

    //-------------------------------------------------------------------------
    // helper methods
    //-------------------------------------------------------------------------

    private void search(String search)
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Handler handler = new Handler();
        GoogleMapsApi api = new GoogleMapsApi(this, handler, new GoogleMapsApi.CompletionHandler() {
            public void onSuccess(final JsonNode node) {
                progressDialog.cancel();
                centerMapToGeocodingSafe(node);
            }
            public void onError(Throwable error) {
                progressDialog.cancel();
            }
        });

        // run query
        api.getGeocodingForAddress(search);
    }

    //-------------------------------------------------------------------------

    private void centerMapToGeocodingSafe(JsonNode geoData)
    {
        try {
            centerMapToGeocoding(geoData);
        } catch (Exception error) {
            Log.e(kLogTag, "Centering failed...", error);
            String title   = getString(R.string.location_search);
            String message = getString(R.string.location_search_fail);
            Utilities.displaySimpleAlert(this, title, message);
        }
    }

    //-------------------------------------------------------------------------

    private void centerMapToGeocoding(JsonNode geoData)
    {
        if (geoData == null) return;

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
        double latitude    = location.get("lat").getDoubleValue();
        double longitude   = location.get("lng").getDoubleValue();
        double neLatitude  = northEast.get("lat").getDoubleValue();
        double neLongitude = northEast.get("lng").getDoubleValue();
        double swLatitude  = southWest.get("lat").getDoubleValue();
        double swLongitude = southWest.get("lng").getDoubleValue();

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

    public void onSave()
    {
        // setup progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
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

    public void onCancel()
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
        String address = GoogleMapsApi.parseLocality(node);

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
        LocationTrackerManager manager = LocationTrackerManager.getInstance(this);
        Location currentLocation = manager.currentLocation();
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


