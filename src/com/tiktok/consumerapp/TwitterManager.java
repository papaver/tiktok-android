//-----------------------------------------------------------------------------
// TwitterManager
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sugree.twitter.Twitter;
import com.sugree.twitter.TwitterError;
import com.sugree.twitter.DialogError;
import com.sugree.twitter.Twitter.DialogListener;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class TwitterManager
{
    //-------------------------------------------------------------------------
    // CompletionHandler
    //-------------------------------------------------------------------------

    public static interface CompletionHandler
    {
        public abstract void onSuccess(Object object);
        public abstract void onError(Throwable error);
        public abstract void onCancel();
    }

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "TwitterManager";

    //-------------------------------------------------------------------------
    // get instance
    //-------------------------------------------------------------------------

    public static TwitterManager getInstance(Context context)
    {
        if (sManager == null) {
            sManager = new TwitterManager(context.getApplicationContext());
        }
        return sManager;
    }

    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    private TwitterManager(Context context)
    {
        mPreferences  = PreferenceManager.getDefaultSharedPreferences(context);
        mTwitter      = new Twitter(R.drawable.ic_launcher);

        // load session variables
        loadTwitterData();
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public void authorize(final Context context, final CompletionHandler handler)
    {
        // attempt to log into facebook
        String key    = Constants.kTwitterApiKey;
        String secret = Constants.kTwitterApiSecret;
        mTwitter.authorize(context, new Handler(), key, secret, new DialogListener() {

            public void onComplete(Bundle values) {
                saveTwitterData();
                if (handler != null) handler.onSuccess(values);
            }

            public void onTwitterError(TwitterError error) {
                Log.e(kLogTag, String.format("Login failed", error));
                if (handler != null) handler.onError(error);
            }

            public void onError(DialogError error) {
                Log.e(kLogTag, String.format("Login failed", error));
                if (handler != null) handler.onError(error);
            }

            public void onCancel() {
                Log.w(kLogTag, "Login canceled.");
                if (handler != null) handler.onCancel();
            }

        });
    }

    //-------------------------------------------------------------------------

    public void tweet(String message, final Handler handler,
                      final CompletionHandler completionHandler)
    {
        // setup configuration
        Configuration configuration = new ConfigurationBuilder()
            .setOAuthConsumerKey(Constants.kTwitterApiKey)
            .setOAuthConsumerSecret(Constants.kTwitterApiSecret)
            .setOAuthAccessToken(mTwitter.getAccessToken())
            .setOAuthAccessTokenSecret(mTwitter.getSecretToken())
            .build();

        // the factory instance is re-useable and thread safe.
        AsyncTwitterFactory factory = new AsyncTwitterFactory(configuration);
        AsyncTwitter asyncTwitter   = factory.getInstance();
        asyncTwitter.addListener(new TwitterAdapter() {
            public void updatedStatus(final Status status) {
                handler.post(new Runnable() {
                    public void run() {
                        if (completionHandler != null) completionHandler.onSuccess(status);
                    }
                });
            }
            public void onException(final TwitterException error, TwitterMethod method) {
                handler.post(new Runnable() {
                    public void run() {
                        if (completionHandler != null) completionHandler.onError(error);
                    }
                });
            }
        });

        // send message
        asyncTwitter.updateStatus(message);
    }

    //-------------------------------------------------------------------------

    public boolean logout(Context context)
    {
        boolean result = false;

        try {
            mTwitter.logout(context);
            clearTwitterData();
            result = true;
        } catch (IOException e) {
            Log.e(kLogTag, "Failed logout", e);
        }

        return result;
    }

    //-------------------------------------------------------------------------

    public Twitter twitter()
    {
        return mTwitter;
    }

    //-------------------------------------------------------------------------

    public void loadTwitterData()
    {
        String access = mPreferences.getString("tw_access_token", null);
        String secret = mPreferences.getString("tw_secret_token", null);
        if ((access != null)  && (secret != null)) {
            mTwitter.setAccessToken(access);
            mTwitter.setSecretToken(secret);
        }
    }

    //-------------------------------------------------------------------------

    public void saveTwitterData()
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("tw_access_token", mTwitter.getAccessToken());
        editor.putString("tw_secret_token", mTwitter.getSecretToken());
        editor.commit();
    }

    //-------------------------------------------------------------------------

    public void clearTwitterData()
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove("tw_access_token");
        editor.remove("tw_secret_token");
        editor.commit();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private static TwitterManager sManager;

    private Twitter           mTwitter;
    private SharedPreferences mPreferences;
}
