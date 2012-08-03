//-----------------------------------------------------------------------------
// UIUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class UIUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "UIUtilities";

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public static RotateAnimation getActivityIndicatorAnimation()
    {
        RotateAnimation rotation = new RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
        rotation.setDuration(360 * 4);
        rotation.setInterpolator(new LinearInterpolator());
        rotation.setRepeatMode(Animation.RESTART);
        rotation.setRepeatCount(Animation.INFINITE);
        return rotation;
    }

    //-------------------------------------------------------------------------

    public static void addAddressLinkMask(TextView textView)
    {
        if (textView.getUrls().length == 0) {

            // reset the link mask so we can use custom links
            textView.setAutoLinkMask(0);

            // transform the address to be url compliant
            TransformFilter filter = new TransformFilter() {
                public String transformUrl(Matcher match, String url) {
                    String formatted = url.replace("\n", ",+")
                                          .replace(", ", ",+")
                                          .replace( " ",  "+");
                    return formatted;
                }
            };

            // linkify the address
            String scheme   = "geo:0,0?q=";
            Pattern pattern = Pattern.compile("(.+)", Pattern.DOTALL);
            Linkify.addLinks(textView, pattern, scheme, null, filter);
        }
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private UIUtilities()
    {
    }
}
