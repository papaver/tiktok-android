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
        String registration = intent.getStringExtra("registration_id");
        String error        = intent.getStringExtra("error");
        String unregistered = intent.getStringExtra("registered");

        if (error != null) {
            Log.w(getClass().getSimpleName(), "Registration failed: " + error);
        } else if (unregistered != null) {
            Log.w(getClass().getSimpleName(), "Unregistered: " + unregistered);
        } else if (registration != null) {
            Log.w(getClass().getSimpleName(), "Registered: " + registration);
        }
    }

    //-------------------------------------------------------------------------
    
    private void handleMessage(Context context, Intent intent)
    {
        String message = intent.getStringExtra("message");
        Log.w(getClass().getSimpleName(), "Message: " + message);
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
