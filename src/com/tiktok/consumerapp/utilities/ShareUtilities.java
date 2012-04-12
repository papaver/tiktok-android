//-----------------------------------------------------------------------------
// ShareUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;

import com.tiktok.consumerapp.R;
import com.tiktok.consumerapp.FacebookManager;
import com.tiktok.consumerapp.TwitterManager;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class ShareUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "ShareUtilities";

    //-------------------------------------------------------------------------
    // twitter
    //-------------------------------------------------------------------------

    public static class TwitterShare
    {
        public TwitterShare(Context context, String message, Handler handler,
            TwitterManager.CompletionHandler callback)
        {
            this.context  = context;
            this.message  = message;
            this.handler  = handler;
            this.callback = callback;
        }

        public final Context                          context;
        public final String                           message;
        public final Handler                          handler;
        public final TwitterManager.CompletionHandler callback;
    }

    //-------------------------------------------------------------------------

    public static void shareTwitter(TwitterShare share)
    {
        TwitterManager manager = TwitterManager.getInstance(share.context);
        if (manager.twitter().isSessionValid()) {
            ShareUtilities.postTwitter(share);
        } else {
            ShareUtilities.setupTwitterAndPost(share);
        }
    }

    //-------------------------------------------------------------------------

    private static void setupTwitterAndPost(final TwitterShare share)
    {
        String title   = share.context.getString(R.string.twitter_setup);
        String message = share.context.getString(R.string.twitter_not_setup);

        // ask user to log into twitter before posting
        AlertDialog alertDialog = new AlertDialog.Builder(share.context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Twitter",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ShareUtilities.authorizeTwitterAndPost(share);
                }
            });

        // display alert
        alertDialog.show();
    }

    //-------------------------------------------------------------------------

    public static void authorizeTwitterAndPost(final TwitterShare share)
    {
        TwitterManager manager = TwitterManager.getInstance(share.context);
        manager.authorize(share.context, new TwitterManager.CompletionHandler() {
            public void onSuccess(Object object) {
                share.handler.post(new Runnable() {
                    public void run() {
                        ShareUtilities.postTwitter(share);
                    }
                });
            }
            public void onError(Throwable error) {}
            public void onCancel() {}
        });
    }

    //-------------------------------------------------------------------------

    public static void postTwitter(final TwitterShare share)
    {
        // make sure message is tweetable
        String message = share.message;
        if (message.length() > 130) {
            message = message.substring(0, 126) + "...";
        }

        // display progress
        final ProgressDialog progressDialog = new ProgressDialog(share.context);
        progressDialog.setMessage("Tweeting...");
        progressDialog.show();

        // tweet deal
        TwitterManager manager = TwitterManager.getInstance(share.context);
        manager.tweet(share.message, share.handler, new TwitterManager.CompletionHandler() {

            public void onSuccess(final Object object) {
                progressDialog.cancel();
                if (share.callback != null) share.callback.onSuccess(object);
            }

            public void onError(final Throwable error) {
                progressDialog.cancel();
                if (share.callback != null) share.callback.onError(error);
            }

            public void onCancel() {
                progressDialog.cancel();
                if (share.callback != null) share.callback.onCancel();
            }
        });
    }

    //-------------------------------------------------------------------------
    // facebook
    //-------------------------------------------------------------------------

    public static class FacebookShare
    {
        public FacebookShare(Activity activity, Bundle params, Handler handler,
            FacebookManager.CompletionHandler callback)
        {
            this.activity = activity;
            this.params   = params;
            this.handler  = handler;
            this.callback = callback;
        }

        public final Activity                          activity;
        public final Bundle                            params;
        public final Handler                           handler;
        public final FacebookManager.CompletionHandler callback;
    }

    //-------------------------------------------------------------------------

    public static void shareFacebook(FacebookShare share)
    {
        FacebookManager manager = FacebookManager.getInstance(share.activity);
        if (manager.facebook().isSessionValid()) {
            ShareUtilities.postFacebook(share);
        } else {
            ShareUtilities.setupFacebook(share);
        }
    }

    //-------------------------------------------------------------------------

    private static void setupFacebook(final FacebookShare share)
    {
        String title   = share.activity.getString(R.string.facebook_setup);
        String message = share.activity.getString(R.string.facebook_not_setup);

        // ask user to log into facebook before posting
        AlertDialog alertDialog = new AlertDialog.Builder(share.activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Facebook",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ShareUtilities.authorizeFacebookAndShare(share);
                }
            });

        // display alert
        alertDialog.show();
    }

    //-------------------------------------------------------------------------

    private static void authorizeFacebookAndShare(final FacebookShare share)
    {
        FacebookManager manager = FacebookManager.getInstance(share.activity);
        manager.authorize(share.activity, new FacebookManager.CompletionHandler() {
            public void onSuccess(Bundle values) {
                share.handler.post(new Runnable() {
                    public void run() {
                        ShareUtilities.postFacebook(share);
                    }
                });
            }
            public void onError(Throwable error) {}
            public void onCancel() {}
        });
    }

    //-------------------------------------------------------------------------

    private static void postFacebook(final FacebookShare share)
    {
        // pop open dialog to allow user to confimr post
        FacebookManager manager = FacebookManager.getInstance(share.activity);
        manager.postToWall(share.activity, share.params, new FacebookManager.CompletionHandler() {

            public void onSuccess(final Bundle values) {
                if (values.containsKey("post_id")) {
                    if (share.callback != null) share.callback.onSuccess(values);
                }
            }

            public void onError(final Throwable error) {
                if (share.callback != null) share.callback.onError(error);
            }

            public void onCancel() {
                if (share.callback != null) share.callback.onCancel();
            }
        });
    }

    //-------------------------------------------------------------------------
    // sms
    //-------------------------------------------------------------------------

    public static void shareSMS(Activity activity, int id, String message)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"));
        intent.putExtra("sms_body", message);
        intent.putExtra("compose_mode", true);
        intent.setType("vnd.android-dir/mms-sms");
        activity.startActivityForResult(intent, id);
    }

    //-------------------------------------------------------------------------
    // email
    //-------------------------------------------------------------------------

    public static void shareEmail(Activity activity, int id, String subject, String body)
    {
        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("email:"));
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
        activity.startActivityForResult(intent, id);
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private ShareUtilities()
    {
    }
}
