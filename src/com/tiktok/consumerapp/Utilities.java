//-----------------------------------------------------------------------------
// Utilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Utilities
{

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kDomain               = "com.tiktok.comsumerapp.utilities";
    private static final String kKeyDeviceId          = "TTDeviceKey";
    private static final String kKeyConsumerId        = "TTCustomerKey";
    private static final String kKeyNotificationToken = "TTNotificationKey";

    //-------------------------------------------------------------------------
    // static methods
    //-------------------------------------------------------------------------

    public static void displaySimpleAlert(Context context, String title, String message)
    {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        context.startActivity(intent);
    }

    //-------------------------------------------------------------------------

    /* [moiz] example of using a alertbuilder to create an dialog fragment
     *   this isn't themed correctly to match holo so looks like shit, using
     *   the custom dialog activity from abs instead right now, hopefully some
     *   bored sap will port it over and get it working.
     *
    public static void displaySimpleFragmentAlert(final FragmentActivity activity,
                                                  final String title,
                                                  final String message)
    {
        final class AlertDialogFragment extends SherlockDialogFragment
        {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState)
            {
                return new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // nothing to see here
                            }
                        }
                    ).create();
            }
        }

        //---------------------------------------------------------------------

        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.show(activity.getSupportFragmentManager(), "dialog");
    }
    */

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public Utilities(Context context)
    {
        Context globalContext = context.getApplicationContext();
        mPreferences = globalContext.getSharedPreferences(kDomain, Activity.MODE_PRIVATE);
        mEditor      = mPreferences.edit();
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    /**
     * @returns the device id, null for new devices.
     */
    public String getDeviceId()
    {
        return mPreferences.getString(kKeyDeviceId, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Caches the device id in both user shared preferences and external
     *  storage if it is available.
     */
    public void cacheDeviceId(String deviceId)
    {
        mEditor.putString(kKeyDeviceId, deviceId);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    /**
     * Clears any existing cached device ids.
     */
    public void clearDeviceId()
    {
        mEditor.remove(kKeyDeviceId);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    /**
     * @returns the consumer id, nil for new devices.
     */
    public String getConsumerId()
    {
        return mPreferences.getString(kKeyConsumerId, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Caches the consumer id in the user shared preferences.
     */
    public void cacheConsumerId(String consumerId)
    {
        mEditor.putString(kKeyConsumerId, consumerId);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    /**
     * Clears any existing cached consumer ids.
     */
    public void clearConsumerId()
    {
        mEditor.remove(kKeyConsumerId);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    /**
     * @returns the token if it exists.
     */
    public String getNotificationToken()
    {
        return mPreferences.getString(kKeyNotificationToken, null);
    }

    //-------------------------------------------------------------------------

    /**
     * Caches the notification token in the user shared preferences.
     */
    public void cacheNotificationToken(String token)
    {
        mEditor.putString(kKeyNotificationToken, token);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    /**
     * Clears existing cached notification token.
     */
    public void clearNotificationToken()
    {
        mEditor.remove(kKeyNotificationToken);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private SharedPreferences mPreferences;
    private Editor            mEditor;

}
