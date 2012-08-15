//-----------------------------------------------------------------------------
// CitiesFragment
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class CitiesFragment extends SherlockListFragment
{

    //-------------------------------------------------------------------------
    // SherlockFragment
    //-------------------------------------------------------------------------

    /**
     * Called once the fragment is associated with its activity.
     */
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    //-------------------------------------------------------------------------

    /**
     * Called to do initial creation of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // checkpoint marker
        Analytics.passCheckpoint("Cities");

        // setup frament attributes
        setRetainInstance(true);

        // initialize empty cities data
        mCities = new HashMap<String, List<String>>();
        mCities.put("live", new ArrayList<String>());
        mCities.put("soon", new ArrayList<String>());
        mCities.put("beta", new ArrayList<String>());

        // sync cities in the background
        syncCities();
    }

    //-------------------------------------------------------------------------

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.cities, container, false);
        return view;
    }

    //-------------------------------------------------------------------------

    /**
     * Tells the fragment that it's activity has completed its own
     * Activity.onCreate().
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    //-------------------------------------------------------------------------

    /**
     * Makes the fragment visible to the user (based on its containing
     * activity being started).
     */
    @Override
    public void onStart()
    {
        super.onStart();
    }

    //-------------------------------------------------------------------------

    /**
     * Makes the fragment interacting with the user (based on its containing
     * activity being resumed).
     */
    @Override
    public void onResume()
    {
        super.onResume();
        updateCitiesUI(mCities);
    }

    //-------------------------------------------------------------------------

    /**
     * Fragment is no longer interacting with the user either because it's
     * activity is being paused or a fragment operation is modifying it in the
     * activity.
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    //-------------------------------------------------------------------------

    /**
     * Fragment is no longer visible to the user either because its activity is
     * being stopped or a fragment operation is modifying it in the activity.
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    //-------------------------------------------------------------------------

    /**
     * Allows the fragment to clean up resources associated with its View.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    //-------------------------------------------------------------------------

    /**
     * called to do final cleanup of the fragment's state.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    private void syncCities()
    {
        // grab the point count from the server
        final Context context = getSherlockActivity();
        TikTokApi api = new TikTokApi(context, new Handler(), new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {

                @SuppressWarnings("unchecked")
                Map<String, List<String>> cities = (Map<String, List<String>>)data;
                if (cities != null) {
                    mCities = cities;
                    updateCitiesUI(mCities);
                } else {
                    String message = getString(R.string.cities_sync_fail);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            }

            public void onError(Throwable error) {
                String message = getString(R.string.cities_sync_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

        // run query
        api.syncCities();
    }

    //-------------------------------------------------------------------------

    private void updateCitiesUI(Map<String, List<String>> cities)
    {
        // nothing to update if no ui
        if (isDetached()) return;

        // create an adapter to display the city data
        final Context context          = getSherlockActivity();
        MultiHeaderListAdapter adapter = new MultiHeaderListAdapter(context);
        adapter.addSection("Live", new ArrayAdapter<String>(context,
            R.layout.list_item, cities.get("live")));
        adapter.addSection("Beta", new ArrayAdapter<String>(context,
            R.layout.list_item, cities.get("beta")));
        adapter.addSection("Coming Soon", new ArrayAdapter<String>(context,
            R.layout.list_item, cities.get("soon")));

        // update list view to use adapter
        setListAdapter(adapter);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    Map<String, List<String>> mCities;
}
