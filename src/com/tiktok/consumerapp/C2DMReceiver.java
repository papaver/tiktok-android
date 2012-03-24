//-----------------------------------------------------------------------------
// C2DMReceiver
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//import android.widget.Toast;
//import com.google.android.c2dm.C2DMBaseReceiver;
//import com.google.android.c2dm.C2DMessaging;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class C2DMReceiver extends BroadcastReceiver
{

    //-------------------------------------------------------------------------

    static final String kLogTag = "NotificationReciever";

    //-------------------------------------------------------------------------

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            handleRegistration(context, intent);
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            handleMessage(context, intent);
        }
    }
    
    //-------------------------------------------------------------------------

    private void handleRegistration(Context context, Intent intent)
    {
        String token        = intent.getStringExtra("registration_id");
        String unregistered = intent.getStringExtra("registered");
        String error        = intent.getStringExtra("error");

        if (error != null) {
            Log.e(kLogTag, "Registration failed: " + error);
        } else if (unregistered != null) {
            Log.i(kLogTag, "Unregistered: " + unregistered);
            Utilities utilities = new Utilities(context);
            utilities.clearNotificationToken();
        } else if (token != null) {
            Log.i(kLogTag, "Registered: " + token);

            // [moiz] this needs to be implemented on the server side and tested
            Utilities utilities = new Utilities(context);
            String oldToken     = utilities.getNotificationToken();
            if (!token.equals(oldToken)) {
                TikTokApi api = new TikTokApi(context);
                if (api.registerNotificationToken(token) != null) {
                    Log.i(kLogTag, String.format("Cached new token: %s", token));
                    utilities.cacheNotificationToken(token);
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    
    private void handleMessage(Context context, Intent intent)
    {
        String message = intent.getStringExtra("message");
        Log.i(kLogTag, "Message: " + message);
    }
}

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

/*
public class C2DMReceiver extends C2DMBaseReceiver
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------
    
    static String sC2DMSender = "papaver@gmail.com";

    //-------------------------------------------------------------------------
    
    public C2DMReceiver()
    {
        super(sC2DMSender);
    }
    
    //-------------------------------------------------------------------------

    @Override
    public void onError(Context context, String errorId)
    {
        Toast.makeText(context, "Messaging registation error: " + errorId,
            Toast.LENGTH_LONG).show();
    }

    //-------------------------------------------------------------------------

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        String accountName = intent.getExtras().getString("account_name");
        String message     = intent.getExtras().getString("message");
        Log.w(getClass().getSimpleName(), String.format(
            "Account: %s, message: %s", accountName, message));
    }

    //-------------------------------------------------------------------------

    /**
     * Register or unregister app with notification server. 
     * /
    public static void registerApp(Context context, boolean registerWithServer)
    {
        boolean registered = !C2DMessaging.getRegistrationId(context).equals("");
        if (registerWithServer != registered) {
            if (registerWithServer) {
                C2DMessaging.register(context, sC2DMSender);
            } else {
                C2DMessaging.unregister(context);
            }
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------
    
}
*/
