//-----------------------------------------------------------------------------
// C2DMReceiver
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

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
        final String token        = intent.getStringExtra("registration_id");
        final String unregistered = intent.getStringExtra("registered");
        final String error        = intent.getStringExtra("error");

        if (error != null) {
            Log.e(kLogTag, "Registration failed: " + error);
        } else if (unregistered != null) {
            Log.i(kLogTag, "Unregistered: " + unregistered);
            Utilities utilities = new Utilities(context);
            utilities.clearNotificationToken();
        } else if (token != null) {
            Log.i(kLogTag, "Registered: " + token);

            // [moiz] this needs to be implemented on the server side and tested
            final Utilities utilities = new Utilities(context);
            String oldToken           = utilities.getNotificationToken();
            if (!token.equals(oldToken)) {
                TikTokApi api = new TikTokApi(context, new Handler(),
                    new TikTokApi.CompletionHandler() {
                        public void onSuccess(Object data) {
                            Log.i(kLogTag, String.format("Cached new token: %s", token));
                            utilities.cacheNotificationToken(token);
                        }

                        public void onError(Throwable error) {}
                    });

                // run query
                api.registerNotificationToken(token);
            }
        }
    }

    //-------------------------------------------------------------------------

    private void handleMessage(Context context, Intent intent)
    {
        Settings settings = settings(context);
        if (settings.notificationsEnabled()) {
            String message = intent.getStringExtra("message");
            Log.i(kLogTag, "Message: " + message);
            sendNotification(context, message);
        }
    }

    //-------------------------------------------------------------------------

    private void sendNotification(Context context, String message)
    {
        NotificationManager notificationManager =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        int icon                    = R.drawable.ic_launcher;
        long time                   = System.currentTimeMillis();
        Context appContext          = context.getApplicationContext();
        String title                = "New TikTok Deal!";
        String text                 = message;
        String ticker               = "TikTok New Deal Available!";

        // create intent
        Intent notificationIntent = new Intent(appContext, MainTabActivity.class);

        // create pending intent to start intent later
        PendingIntent contentIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // setup notification
        Notification notification = new NotificationCompat.Builder(context)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setTicker(ticker)
            .setWhen(time)
            .setDefaults(settings(context).notificationDefaults())
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build();

        // push out notification
        notificationManager.notify(0, notification);
    }

    //-------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private boolean isAppOnForeground(Context context)
    {
        final Context appContext        = context.getApplicationContext();
        ActivityManager activityManager =
            (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        // grab new activities
        List<RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();

        // can't be in foreground
        if (processes == null) return false;

        // check if app is running
        final String packageName = appContext.getPackageName();
        for (RunningAppProcessInfo process : processes) {
            if (process.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                process.processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    //-------------------------------------------------------------------------

    private Settings settings(Context context)
    {
        if (mSettings == null) mSettings = new Settings(context);
        return mSettings;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Settings mSettings;

}

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

/*
public class C2DMReceiver extends C2DMBaseReceiver
{
    public C2DMReceiver()
    {
        super(Constants.kPushNotificationAccount);
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
                C2DMessaging.register(context, Constants.kPushNotificationAccount);
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
