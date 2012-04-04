//-----------------------------------------------------------------------------
// TextUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

//import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class TextUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    //private static final String kLogTag = "TextUtilities";

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public static String capitalizeWords(String text)
    {
        final StringBuilder result = new StringBuilder(text.length());

        String[] words = text.split("\\s");
        for (int index = 0; index < words.length; ++index) {
            if (index > 0) result.append(" ");
            result.append(Character.toUpperCase(words[index].charAt(0)))
                  .append(words[index].substring(1).toLowerCase());
        }

        return result.toString();
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private TextUtilities()
    {
    }
}
