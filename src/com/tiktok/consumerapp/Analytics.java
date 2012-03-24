//-----------------------------------------------------------------------------
// Analytics
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;

import com.flurry.android.FlurryAgent;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Analytics
{

    //-------------------------------------------------------------------------
    // static methods
    //-------------------------------------------------------------------------

    public static void startSession(Context context)
    {
        // start analytics only if enabled
        if (Debug.kAnalytics) {
            String key = Debug.kAnalyticsModeDebug ?
                Constants.kFlurryApiDevKey : Constants.kFlurryApiKey;
            FlurryAgent.onStartSession(context, key);
        }
    }

    //-------------------------------------------------------------------------

    public static void endSession(Context context)
    {
        // stop analytics only if enabled
        if (Debug.kAnalytics) {
            FlurryAgent.onEndSession(context);
        }
    }

    //-------------------------------------------------------------------------

    public static void setUserId(String userId)
    {
        FlurryAgent.setUserId(userId);
    }

    //-------------------------------------------------------------------------

    public static void setUserGender(String gender)
    {
        boolean female = gender.toLowerCase().startsWith("f");
        FlurryAgent.setGender(female ? com.flurry.android.Constants.FEMALE :
                                       com.flurry.android.Constants.MALE);
    }

    //-------------------------------------------------------------------------

    public static void setUserAge(Date birthday)
    {
        FlurryAgent.setAge(Analytics.getAge(birthday));
    }

    //-------------------------------------------------------------------------

    public static void passCheckpoint(String checkpoint)
    {
        FlurryAgent.logEvent(checkpoint);
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private Analytics()
    {
    }

    //-------------------------------------------------------------------------
    // helper functions
    //-------------------------------------------------------------------------

    public static int getAge(Date birthday)
    {
        GregorianCalendar calendar = new GregorianCalendar();

        // get current date
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        // set birthday
        calendar.setTime(birthday);

        // calculate age
        int age = year - calendar.get(Calendar.YEAR);
        if ((month < calendar.get(Calendar.MONTH)) ||
            ((month == calendar.get(Calendar.MONTH)) &&
             (day < calendar.get(Calendar.DAY_OF_MONTH)))) {
            --age;
        }

        // don't allow invalid ages
        if (age < 0) age = 0;

        return age;
    }
}
