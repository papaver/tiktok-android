//-----------------------------------------------------------------------------
// CitiesActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CitiesActivity extends ListActivity
{

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cities);
        Analytics.passCheckpoint("Cities");

        // display empty data
        Map<String, List<String>> empty = new HashMap<String, List<String>>();
        empty.put("live", new ArrayList<String>());
        empty.put("soon", new ArrayList<String>());
        empty.put("beta", new ArrayList<String>());
        updateCities(empty);
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
        syncCities();
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
    // helper functions
    //-------------------------------------------------------------------------

    private void syncCities()
    {
        // grab the point count from the server
        final ListActivity activity = this;
        TikTokApi api = new TikTokApi(this, new Handler(), new TikTokApi.CompletionHandler() {
            @SuppressWarnings("unchecked")
            public void onSuccess(Object data) {
                Map<String, List<String>> cities = (Map<String, List<String>>)data;
                if (cities != null) {
                    updateCities(cities);
                } else {
                    String message = getString(R.string.cities_sync_fail);
                    Toast.makeText(activity, message, 1000).show();
                }
            }

            public void onError(Throwable error) {
                String message = getString(R.string.cities_sync_fail);
                Toast.makeText(activity, message, 1000).show();
            }
        });

        // run query
        api.syncCities();
    }

    //-------------------------------------------------------------------------

    private void updateCities(Map<String, List<String>> cities)
    {
        // create an adapter to display the city data
        MultiHeaderListAdapter adapter = new MultiHeaderListAdapter(this);
        adapter.addSection("Live", new ArrayAdapter<String>(this,
            R.layout.list_item, cities.get("live")));
        adapter.addSection("Beta", new ArrayAdapter<String>(this,
            R.layout.list_item, cities.get("beta")));
        adapter.addSection("Coming Soon", new ArrayAdapter<String>(this,
            R.layout.list_item, cities.get("soon")));

        // update list view to use adapter
        final ListView listView = getListView();
        listView.setAdapter(adapter);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

}


