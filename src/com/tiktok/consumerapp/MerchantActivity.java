//-----------------------------------------------------------------------------
// MerchantActivity
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.widget.IcsProgressBar;
import com.actionbarsherlock.view.MenuItem;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.utilities.ColorUtilities;
import com.tiktok.consumerapp.utilities.UIUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class MerchantActivity extends SherlockActivity
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "MerchantActivity";

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
        setContentView(R.layout.merchant);

        // checkpoint marker
        Analytics.passCheckpoint("Merchant");

        // setup action bar
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Merchant");
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        // grab merchant id from intent
        Long id = (savedInstanceState == null) ? null :
            (Long)savedInstanceState.getSerializable(CouponTable.sKeyId);
        if (id == null) {
            Bundle extras = getIntent().getExtras();
            id = extras != null ? extras.getLong(CouponTable.sKeyId) : null;
        }

        // can't be here without a valid coupon id
        if (id == null) finish();

        // retrieve the coupon from the database
        TikTokDatabaseAdapter adapter = new TikTokDatabaseAdapter(this);
        mCoupon = adapter.fetchCoupon(id);

        // can only continue with a valid coupon
        if (mCoupon != null) {

            // update text views with fonts
            updateFonts();

            // update view with merchant details
            setupMerchantDetails(mCoupon);

        // alert user of booboo
        } else {
            String title   = getString(R.string.tiktok_fail_title);
            String message = getString(R.string.tiktok_fail_message);
            Utilities.displaySimpleAlert(this, title, message);
            finish();
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
        if (mIconManager != null) mIconManager.clearAllRequests();
    }

    //-------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    private void updateFonts()
    {
        // grab fonts
        AssetManager manager = getAssets();
        Typeface helvBd      = Typeface.createFromAsset(manager, "fonts/HelveticaBd.ttf");
        Typeface helvNuBd    = Typeface.createFromAsset(manager, "fonts/HelveticaNeueBd.ttf");
        Typeface helvNuMed   = Typeface.createFromAsset(manager, "fonts/HelveticaNeueMed.ttf");
        Typeface helvNuLt    = Typeface.createFromAsset(manager, "fonts/HelveticaNeueLt.ttf");

        // grab text views
        TextView name = (TextView)findViewById(R.id.name);
        TextView address = (TextView)findViewById(R.id.address);
        TextView phone = (TextView)findViewById(R.id.phone);
        TextView category = (TextView)findViewById(R.id.category);
        TextView url = (TextView)findViewById(R.id.url);
        TextView details = (TextView)findViewById(R.id.details);

        // update font
        name.setTypeface(helvNuBd);
        address.setTypeface(helvNuMed);
        phone.setTypeface(helvNuBd);
        category.setTypeface(helvBd);
        url.setTypeface(helvNuMed);
        details.setTypeface(helvNuLt);
    }

    //-------------------------------------------------------------------------

    public void setupMerchantDetails(final Coupon coupon)
    {
        Merchant merchant         = coupon.merchant();
        Location location         = coupon.locations().get(0);
        boolean multipleLocations = coupon.locations().size() > 1;

        // name
        TextView name = (TextView)findViewById(R.id.name);
        name.setText(merchant.name().toUpperCase());

        // address
        if (multipleLocations) {
            TextView addressView = (TextView)findViewById(R.id.address);
            addressView.setText("Multiple locations,\nsee below.");
        } else {
            setupAddress(location.address());
        }

        // phone
        TextView phone = (TextView)findViewById(R.id.phone);
        if (multipleLocations) {
            phone.setText("");
        } else {
            phone.setText(location.phone());
        }

        // category
        TextView category = (TextView)findViewById(R.id.category);
        category.setText(merchant.category());

        // url
        TextView url = (TextView)findViewById(R.id.url);
        url.setText(merchant.websiteUrl().replace("http://", ""));

        // details
        TextView details = (TextView)findViewById(R.id.details);
        details.setText(merchant.details());

        // gradient
        FrameLayout frameLayout     = (FrameLayout)findViewById(R.id.gradient);
        GradientDrawable background = (GradientDrawable)frameLayout.getBackground();
        background.setColor(ColorUtilities.kTik);

        // icon
        setupIcon(merchant);

        // setup locations table
        if (multipleLocations) {
            setupLocationsTable(coupon);
        } else {
            ExpandableListView locationListView =
                (ExpandableListView)findViewById(R.id.locationlist);
            ViewGroup.LayoutParams params = locationListView.getLayoutParams();
            params.height = locationListView.getMeasuredHeight();
            locationListView.setLayoutParams(params);
        }
    }

    //-------------------------------------------------------------------------

    private void setupLocationsTable(final Coupon coupon)
    {
        // create a new list adapter
        mListAdapter = new ExpadableLocationAdapter(this,
            coupon.merchant(), coupon.locations());

        // get window metrics
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // setup the list view
        final ExpandableListView locationListView =
            (ExpandableListView)findViewById(R.id.locationlist);
        locationListView.setAdapter(mListAdapter);
        locationListView.setIndicatorBounds(
            metrics.widthPixels - GetDipsFromPixel(40),
            metrics.widthPixels - GetDipsFromPixel(20));

        // calculate group view height
        View groupView = mListAdapter.newGroupView(this, null);
        groupView.measure(0, 0);
        final int groupHeight = groupView.getMeasuredHeight();

        // calculate child view height
        View childView = mListAdapter.newChildView(this, null);
        childView.measure(0, 0);
        final int childHeight = childView.getMeasuredHeight();

        // calculate divider height
        final int dividerHeight = locationListView.getDividerHeight();

        // resize list view to just its header
        ViewGroup.LayoutParams params = locationListView.getLayoutParams();
        params.height = groupHeight;
        locationListView.setLayoutParams(params);

        // on expand update the layout and shift details view down
        locationListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition) {
                ViewGroup.LayoutParams params = locationListView.getLayoutParams();
                params.height = groupHeight +
                    (childHeight + dividerHeight) * mListAdapter.getChildrenCount(0);
                locationListView.setLayoutParams(params);
            }
        });

        // on collapse update the layout and shift details view up
        locationListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            public void onGroupCollapse(int groupPosition) {
                ViewGroup.LayoutParams params = locationListView.getLayoutParams();
                params.height = groupHeight;
                locationListView.setLayoutParams(params);
            }
        });
    }

    //-------------------------------------------------------------------------

    private void setupAddress(final String address)
    {
        TextView addressView = (TextView)findViewById(R.id.address);
        String splitAddress  = TextUtils.join("\n", address.split(", ", 2));
        addressView.setText(splitAddress);

        // make sure address link mask exists
        UIUtilities.addAddressLinkMask(addressView);
    }

    //-------------------------------------------------------------------------

    private void setupIcon(final Merchant merchant)
    {
        final ImageView iconView              = (ImageView)findViewById(R.id.icon);
        final IcsProgressBar iconProgressView = (IcsProgressBar)findViewById(R.id.icon_progress);
        final IconManager.IconData iconData   = merchant.iconData();

        // setup the icon manager
        if (mIconManager == null) {
            mIconManager = new IconManager(this);
        }

        // use cached icon if available
        BitmapDrawable icon = mIconManager.getImage(iconData);
        if (icon != null) {

            // hide activity indicator
            iconView.setVisibility(View.VISIBLE);
            iconProgressView.setVisibility(View.GONE);

            // update icon
            iconView.setImageBitmap(icon.getBitmap());

        // use activity indicator and load image from server
        } else {

            // show activity indicator
            iconView.setVisibility(View.INVISIBLE);
            iconProgressView.setVisibility(View.VISIBLE);

            // download icon from server
            mIconManager.requestImage(iconData, new IconManager.CompletionHandler() {

                public void onSuccess(final BitmapDrawable drawable) {
                    Log.i(kLogTag, String.format("Downloaded icon: %s", iconData.url));
                    iconView.post(new Runnable() {
                        public void run() {
                            iconView.setImageBitmap(drawable.getBitmap());
                            iconView.setVisibility(View.VISIBLE);
                            iconProgressView.setVisibility(View.GONE);
                        }
                    });
                }

                public void onFailure() {
                    Log.e(kLogTag, String.format(
                        "Failed to download icon: %s", iconData.url));
                }

            });
        }
    }

    //-------------------------------------------------------------------------

    public int GetDipsFromPixel(float pixels)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Coupon                   mCoupon;
    private IconManager              mIconManager;
    private ExpadableLocationAdapter mListAdapter;

}

//-----------------------------------------------------------------------------
// ExpadableLocationAdapter
//-----------------------------------------------------------------------------

final class ExpadableLocationAdapter extends BaseExpandableListAdapter
{
    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public ExpadableLocationAdapter(Context context, Merchant merchant,
                                    List<Location> locations)
    {
        mContext   = context;
        mMerchant  = merchant;
        mLocations = locations;
    }

    //-------------------------------------------------------------------------
    // overrides
    //-------------------------------------------------------------------------

    public int getGroupCount()
    {
        return 1;
    }

    //-------------------------------------------------------------------------

    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    //-------------------------------------------------------------------------

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent)
    {
        View groupView = convertView == null ?
            newGroupView(mContext, parent) : convertView;
        TextView title = (TextView)groupView.findViewById(R.id.list_header_title);
        title.setText(isExpanded ? "Close" : "Expand to see all locations");
        return groupView;
    }

    //-------------------------------------------------------------------------

    public Object getChild(int groupPosition, int childPosition)
    {
        return mLocations.get(childPosition);
    }

    //-------------------------------------------------------------------------

    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    //-------------------------------------------------------------------------

    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView,
                             ViewGroup parent)
    {
        View childView = convertView == null ?
            newChildView(mContext, parent) : convertView;
        bindChildView(childView, mContext, (Location)getChild(0, childPosition));
        return childView;
    }

    //-------------------------------------------------------------------------

    public int getChildrenCount(int groupPosition)
    {
        return mLocations.size();
    }

    //-------------------------------------------------------------------------

    public Object getGroup(int groupPosition)
    {
        return mLocations;
    }

    //-------------------------------------------------------------------------

    public boolean hasStableIds()
    {
        return true;
    }

    //-------------------------------------------------------------------------

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    public View newGroupView(Context context, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.list_header, parent, false);

        // grab fonts
        AssetManager manager = context.getAssets();
        Typeface helv        = Typeface.createFromAsset(manager, "fonts/HelveticaMed.ttf");

        // grab text views
        TextView title = (TextView)view.findViewById(R.id.list_header_title);

        // update font
        title.setTypeface(helv);

        return view;
    }

    //-------------------------------------------------------------------------

    public View newChildView(Context context, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.location_list_item, parent, false);

        // grab fonts
        AssetManager manager = context.getAssets();
        Typeface helvNuBd    = Typeface.createFromAsset(manager, "fonts/HelveticaNeueBd.ttf");
        Typeface helvNuMed   = Typeface.createFromAsset(manager, "fonts/HelveticaNeueMed.ttf");

        // grab text views
        TextView name = (TextView)view.findViewById(R.id.name);
        TextView address = (TextView)view.findViewById(R.id.address);
        TextView phone = (TextView)view.findViewById(R.id.phone);

        // update font
        name.setTypeface(helvNuBd);
        address.setTypeface(helvNuMed);
        phone.setTypeface(helvNuBd);

        return view;
    }

    //-------------------------------------------------------------------------

    public void bindChildView(View view, Context context, Location location)
    {
        // retrieve corresponding ViewHolder, which optimizes lookup efficiency
        final ViewHolder viewHolder = getViewHolder(view);

        // format name / address
        String name = location.name().equals("") ?
            mMerchant.name().toUpperCase() : location.name();
        String address = TextUtils.join("\n", location.address().split(", ", 2));

        // set location data
        viewHolder.name.setText(name);
        viewHolder.address.setText(address);
        viewHolder.phone.setText(location.phone());

        // make sure address link mask exists
        UIUtilities.addAddressLinkMask(viewHolder.address);
    }

    //-------------------------------------------------------------------------
    // view holder
    //-------------------------------------------------------------------------

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    private static class ViewHolder
    {
        public TextView name;
        public TextView address;
        public TextView phone;
    }

    //-------------------------------------------------------------------------

    private ViewHolder getViewHolder(final View view)
    {
        // create a new holder if the tag is empty
        final Object tag = view.getTag();
        if ((tag == null) || !(tag instanceof ViewHolder)) {
            ViewHolder holder = new ViewHolder();
            holder.name       = (TextView)view.findViewById(R.id.name);
            holder.address    = (TextView)view.findViewById(R.id.address);
            holder.phone      = (TextView)view.findViewById(R.id.phone);
            view.setTag(holder);
        }

        return (ViewHolder)view.getTag();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    Context        mContext;
    Merchant       mMerchant;
    List<Location> mLocations;

}

