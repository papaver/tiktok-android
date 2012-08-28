//-----------------------------------------------------------------------------
// CouponActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class DialogActivity extends SherlockActivity
{
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
        setContentView(R.layout.dialog);

        // grab coupon id from intent
        Bundle extras  = getIntent().getExtras();
        String title   = extras != null ? extras.getString("title") : "";
        String message = extras != null ? extras.getString("message") : "";

        // update title
        setTitle(title);

        // update message
        TextView messageView = (TextView)findViewById(R.id.message);
        messageView.setText(message);
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
    // events
    //-------------------------------------------------------------------------

    public void onClickClose(View view)
    {
        finish();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

}

