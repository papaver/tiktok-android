//-----------------------------------------------------------------------------
// KarmaFragment
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Map;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class KarmaFragment extends SherlockFragment
{

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kTagKarmaCopy = "karma_copy";
    private static final String kTagKarmaTok  = "karma_tok";

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
        Analytics.passCheckpoint("Karma");

        // setup fragment attributes
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // sync points in the background
        syncPoints(null);
    }

    //-------------------------------------------------------------------------

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.karma, container, false);
        setupButtons(view);
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
        if (mPoints != null) updatePointsUI(mPoints);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.karma, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.refresh) {
            ProgressDialog progressDialog = new ProgressDialog(getSherlockActivity());
            progressDialog.setMessage("Syncing Points...");
            syncPoints(progressDialog);
        }
        return true;
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    private void setupButtons(View view)
    {
        // setup listener
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                onClickCopy(view);
            }
        };

        // karma copy
        View textView = view.findViewWithTag(kTagKarmaCopy);
        textView.setOnClickListener(listener);

        // karma image
        View imageView = view.findViewWithTag(kTagKarmaTok);
        imageView.setOnClickListener(listener);
    }

    //-------------------------------------------------------------------------

    private void onClickCopy(View view)
    {
        String url    = "http://www.tiktok.com/karma_details";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    //-------------------------------------------------------------------------

    private void syncPoints(final ProgressDialog progressDialog)
    {
        // manage progress dialog
        if (progressDialog != null) progressDialog.show();

        // grab the point count from the server
        final Context context = getSherlockActivity();
        TikTokApi api = new TikTokApi(context, new Handler(), new TikTokApi.CompletionHandler() {

            public void onSuccess(Object data) {
                if (progressDialog != null) progressDialog.cancel();

                @SuppressWarnings("unchecked")
                Map<String, Integer> points = (Map<String, Integer>)data;
                if (points != null) {
                    mPoints = points;
                    updatePointsUI(mPoints);
                } else {
                    String message = getString(R.string.karma_sync_fail);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            }

            public void onError(Throwable error) {
                if (progressDialog != null) progressDialog.cancel();
                String message = getString(R.string.karma_sync_fail);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

        });

        // run query
        api.syncKarmaPoints();
    }

    //-------------------------------------------------------------------------

    private void updatePointsUI(Map<String, Integer> points)
    {
        // nothing to update if no ui
        if (isDetached()) return;

        View view                         = getView();
        SherlockFragmentActivity activity = getSherlockActivity();

        // update the points
        String packageName = activity.getPackageName();
        Iterator<Map.Entry<String, Integer>> iterator = points.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();

            // find the id of the text view
            String name = String.format("%s_points", pair.getKey());
            int id = getResources().getIdentifier(name, "id", packageName);
            if (id == 0) continue;

            // update the text view
            TextView textView = (TextView)view.findViewById(id);
            textView.setText(pair.getValue().toString());
        }

        // cleanup layout
        view.requestLayout();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    Map<String, Integer> mPoints;

}
