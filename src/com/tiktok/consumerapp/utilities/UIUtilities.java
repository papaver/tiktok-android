//-----------------------------------------------------------------------------
// UIUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
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
    // constructor
    //-------------------------------------------------------------------------

    private UIUtilities()
    {
    }
}
