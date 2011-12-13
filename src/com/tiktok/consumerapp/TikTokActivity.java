//-----------------------------------------------------------------------------
// TikTokActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokActivity extends Activity 
{
    
    /**
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // setup the list view
        final ListView listView     = (ListView)findViewById(R.id.list);
        final CouponAdapter adapter = new CouponAdapter(this, R.layout.coupon_entry_list_item);
        listView.setAdapter(adapter);

        // populate the list adapter
        for (final Coupon entry : getCoupons()) {
            adapter.add(entry);
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
    
    /**
     * 
     */
    protected List<Coupon> getCoupons()
    {
        // lets setup some test data
        // normally this wound come from some asynchronous fetch into a data
        // source such as a sqlite db, or an HTPP request
        final List<Coupon> entries = new ArrayList<Coupon>();
        for (int i = 0; i < 50; ++i) {
            int icon = i % 2 == 0 ? R.drawable.coupon_icon_1 : R.drawable.coupon_icon_2;
            Coupon entry = new Coupon(
                "Coupon Entry " + i,
                "Coupon Text " + i,
                new GregorianCalendar(2011, 11, i).getTime(),
                new GregorianCalendar(2011, 11, i + 1).getTime(),
                icon);
            entries.add(entry);
        }

        return entries;
    }

    //-------------------------------------------------------------------------
}
