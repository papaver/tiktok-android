//-----------------------------------------------------------------------------
// KarmaActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Map;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class KarmaActivity extends Activity
{

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.karma);
        Analytics.passCheckpoint("Karma");
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
        syncPoints();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.karma, menu);
        return true;
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.refresh) {
            syncPoints();
        }
        return true;
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    public void onClickCopy(View view)
    {
        String url    = "http://www.tiktok.com/karma_details";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    //-------------------------------------------------------------------------

    private void syncPoints()
    {
        // grab the point count from the server
        TikTokApi api = new TikTokApi(this, new Handler(), new TikTokApi.CompletionHandler() {
            @SuppressWarnings("unchecked")
            public void onSuccess(Object data) {
                Map<String, Integer> points = (Map<String, Integer>)data;
                if (points != null) {
                    updatePoints(points);
                }
            }
            public void onError(Throwable error) {
            }
        });

        // run query
        api.syncKarmaPoints();
    }

    //-------------------------------------------------------------------------

    private void updatePoints(Map<String, Integer> points)
    {
        // reset the view, probably not the best way but this view is light
        //  and i don't feel like messing with this shit right now...
        setContentView(R.layout.karma);

        // update the points
        Iterator<Map.Entry<String, Integer>> iterator = points.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();

            // find the id of the text view
            String name = String.format("%s_points", pair.getKey());
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id == 0) continue;

            // update the text view
            TextView textView = (TextView)findViewById(id);
            textView.setText(pair.getValue().toString());
        }
    }
}

