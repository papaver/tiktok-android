//-----------------------------------------------------------------------------
// ColorUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import android.graphics.Color;
//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class ColorUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "ColorUtilities";

    //-------------------------------------------------------------------------
    // colors
    //-------------------------------------------------------------------------

    public static final int kTik    = 0xFF82B34F; // (130 / 179 / 79)
    public static final int kTok    = 0xFFD33D3D; // (211 / 61 / 61)
    public static final int kOrange = 0xFFFF8000; // (255 / 128 / 0)

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public static int interpolateColor(int startColor, int endColor, float fraction)
    {
        int redA   = Color.red(startColor);
        int greenA = Color.green(startColor);
        int blueA  = Color.blue(startColor);
        int alphaA = Color.alpha(startColor);

        int redB   = Color.red(endColor);
        int greenB = Color.green(endColor);
        int blueB  = Color.blue(endColor);
        int alphaB = Color.alpha(endColor);

        int red   = redA   + (int)(fraction * (float)(redB - redA));
        int green = greenA + (int)(fraction * (float)(greenB - greenA));
        int blue  = blueA  + (int)(fraction * (float)(blueB - blueA));
        int alpha = alphaA + (int)(fraction * (float)(alphaB - alphaA));

        red   = Math.min(red,   255);
        green = Math.min(green, 255);
        blue  = Math.min(blue,  255);
        alpha = Math.min(alpha, 255);

        return Color.argb(alpha, red, green, blue);
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private ColorUtilities()
    {
    }
}
