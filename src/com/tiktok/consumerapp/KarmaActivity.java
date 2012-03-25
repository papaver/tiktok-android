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
import android.os.Bundle;
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

        // grab the point count from the server
        TikTokApi api               = new TikTokApi(this);
        Map<String, Integer> points = api.syncKarmaPoints();

        // update the points
        Iterator<Map.Entry<String, Integer>> iterator = points.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();

            // find the id of the text view
            String name = String.format("%s_points", pair.getKey());
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id == 0) continue;

            // update the text view
            final TextView textView = (TextView)findViewById(id);
            textView.setText(pair.getValue().toString());
        }
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
}

