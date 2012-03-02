//-----------------------------------------------------------------------------
// Settings
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Settings
{

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kDomain           = "com.tiktok.comsumerapp.preferences";
    private static final String kTagName          = "TTS_name";
    private static final String kTagEmail         = "TTS_email";
    private static final String kTagGender        = "TTS_gender";
    private static final String kTagBirthday      = "TTS_birthday";
    private static final String kTagHome          = "TTS_home";
    private static final String kTagHomeLocality  = "TTS_homeLocality";
    private static final String kTagWork          = "TTS_work";
    private static final String kTagWorkLocality  = "TTS_workLocality";
    private static final String kTagLastUpdate    = "TTS_lastUpdate";
    //private static final String kTagTutorialIndex = "TTS_tutorial";

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public Settings(Context context)
    {
        Context globalContext = context.getApplicationContext();
        mPreferences = globalContext.getSharedPreferences(kDomain, Activity.MODE_PRIVATE);
        mEditor      = mPreferences.edit();
    }

    //-------------------------------------------------------------------------
    // settings
    //-------------------------------------------------------------------------

    public String name()
    {
        return mPreferences.getString(kTagName, "");
    }

    //-------------------------------------------------------------------------

    public void setName(String name)
    {
         mEditor.putString(kTagName, name);
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public String email()
    {
        return mPreferences.getString(kTagEmail, "");
    }

    //-------------------------------------------------------------------------

    public void setEmail(String email)
    {
         mEditor.putString(kTagEmail, email);
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public String gender()
    {
        return mPreferences.getString(kTagGender, "");
    }

    //-------------------------------------------------------------------------

    public void setGender(String gender)
    {
         mEditor.putString(kTagGender, gender);
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public Date birthday()
    {
        long birthday = mPreferences.getLong(kTagBirthday, 0);
        return new Date(birthday);
    }

    //-------------------------------------------------------------------------

    public void setBirthday(Date birthday)
    {
         mEditor.putLong(kTagBirthday, birthday.getTime());
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public Location home()
    {
        return getLocation(kTagHome);
    }

    //-------------------------------------------------------------------------

    public void setHome(Location home)
    {
        setLocation(kTagHome, home);
    }

    //-------------------------------------------------------------------------

    public String homeLocality()
    {
        return mPreferences.getString(kTagHomeLocality, "");
    }

    //-------------------------------------------------------------------------

    public void setHomeLocality(String homeLocality)
    {
         mEditor.putString(kTagHomeLocality, homeLocality);
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public Location work()
    {
        return getLocation(kTagWork);
    }

    //-------------------------------------------------------------------------

    public void setWork(Location work)
    {
        setLocation(kTagWork, work);
    }

    //-------------------------------------------------------------------------

    public String workLocality()
    {
        return mPreferences.getString(kTagWorkLocality, "");
    }

    //-------------------------------------------------------------------------

    public void setWorkLocality(String workLocality)
    {
         mEditor.putString(kTagWorkLocality, workLocality);
         mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public Date lastUpdate()
    {
        long lastUpdate = mPreferences.getLong(kTagLastUpdate, 0);
        return new Date(lastUpdate);
    }

    //-------------------------------------------------------------------------

    public void setLastUpdate(Date lastUpdate)
    {
         mEditor.putLong(kTagLastUpdate, lastUpdate.getTime());
         mEditor.commit();
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public void clearAllSettings()
    {
        mEditor.clear();
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    private Location getLocation(String key)
    {
        // generate keys for parts
        String latKey = key + "_lat";
        String lngKey = key + "_lng";

        // grab coordinates from settings
        float latitude  = mPreferences.getFloat(latKey, 0.0f);
        float longitude = mPreferences.getFloat(lngKey, 0.0f);

        // encapsulate data in location object
        String provider   = null;
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

    //-------------------------------------------------------------------------

    private void setLocation(String key, Location value)
    {
        // generate keys for parts
        String latKey = key + "_lat";
        String lngKey = key + "_lng";

        // update preferences
        mEditor.putFloat(latKey, (float)value.getLatitude());
        mEditor.putFloat(lngKey, (float)value.getLongitude());
        mEditor.commit();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private SharedPreferences mPreferences;
    private Editor            mEditor;

}
