//-----------------------------------------------------------------------------
// Settings
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import org.codehaus.jackson.JsonNode;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Settings implements OnSharedPreferenceChangeListener
{

    /**
     * [moiz] use apply instead of commit where possible, this runs the save
     *   process asynchronously instead of commit which runs it synchronously,
     *   can't use for remove
     */

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    public static final String kDomain                 = "com.tiktok.comsumerapp.preferences";
    public static final String kTagName                = "TTS_name";
    public static final String kTagEmail               = "TTS_email";
    public static final String kTagTwitterHandle       = "TTS_twh";
    public static final String kTagPhone               = "TTS_phone";
    public static final String kTagGender              = "TTS_gender";
    public static final String kTagBirthday            = "TTS_birthday";
    public static final String kTagHome                = "TTS_home";
    public static final String kTagHomeLocality        = "TTS_homeLocality";
    public static final String kTagWork                = "TTS_work";
    public static final String kTagWorkLocality        = "TTS_workLocality";
    public static final String kTagLastUpdate          = "TTS_lastUpdate";
    public static final String kTagNotifications       = "TTS_notifications";
    public static final String kTagNotificationSound   = "TTS_notifications_sound";
    public static final String kTagNotificationVibrate = "TTS_notifications_vibrate";
    public static final String kTagSyncedSettings      = "TTS_synced_settings";

    public static final String kApiName                = "name";
    public static final String kApiEmail               = "email";
    public static final String kApiTwitterHandle       = "twh";
    public static final String kApiPhone               = "phone";
    public static final String kApiGender              = "sex";
    public static final String kApiBirthday            = "birthday";
    public static final String kApiHome                = "home";
    public static final String kApiWork                = "work";

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public Settings(Context context)
    {
        init(context, false);
    }

    //-------------------------------------------------------------------------

    public Settings(Context context, boolean addListener)
    {
        init(context, addListener);
    }

    //-------------------------------------------------------------------------

    private void init(Context context, boolean addListener)
    {
        mContext     = context.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor      = mPreferences.edit();

        if (addListener) mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    //-------------------------------------------------------------------------

    public void close()
    {
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
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

    public void syncName()
    {
        String name = name();
        if (name.equals("")) return;

        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiName, name);
        api.updateSettings(settings);
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

    public void syncEmail()
    {
        String email = email();
        if (email.equals("")) return;

        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiEmail, email);
        api.updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    public String twitterHandle()
    {
        return mPreferences.getString(kTagTwitterHandle, "");
    }

    //-------------------------------------------------------------------------

    public void setTwitterHandle(String handle)
    {
        mEditor.putString(kTagTwitterHandle, handle);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public void syncTwitterHandle()
    {
        String handle = twitterHandle();
        if (handle.equals("")) return;

        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiTwitterHandle, handle);
        api.updateSettings(settings);
    }

    //-------------------------------------------------------------------------

    public String phone()
    {
        return mPreferences.getString(kTagPhone, "");
    }

    //-------------------------------------------------------------------------

    public void setPhone(String phone)
    {
        mEditor.putString(kTagPhone, phone);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public void syncPhone()
    {
        String phone = phone();
        if (phone.equals("")) return;

        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiPhone, phone);
        api.updateSettings(settings);
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

    public void syncGender()
    {
        String gender = gender();
        if (gender.equals("")) return;

        // push update to server
        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiGender, gender.toLowerCase().substring(0, 1));
        api.updateSettings(settings);

        // update analytics
        Analytics.setUserGender(gender());
    }

    //-------------------------------------------------------------------------

    public Date birthday()
    {
        String birthday = mPreferences.getString(kTagBirthday, null);
        try {
            return birthday == null ? null :
                new SimpleDateFormat("yyyy.MM.dd", Locale.US).parse(birthday);
        } catch (ParseException e) {
            return null;
        }
    }

    //-------------------------------------------------------------------------

    public String birthdayStr()
    {
        // format date into string
        Date birthday               = birthday();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, y", Locale.US);
        return birthday == null ? "" : dateFormat.format(birthday());
    }

    //-------------------------------------------------------------------------

    public void setBirthday(Date birthday)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        mEditor.putString(kTagBirthday, dateFormat.format(birthday));
        mEditor.commit();
    }

    //-------------------------------------------------------------------------

    public void syncBirthday()
    {
        String birthday = birthdayStr();
        if (birthday.equals("")) return;

        // push update to server
        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(kApiBirthday, birthday);
        api.updateSettings(settings);

        // update analytics
        Analytics.setUserAge(birthday());
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

    public void syncHome()
    {
        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        api.updateHomeLocation(home());
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

    public void syncWork()
    {
        TikTokApi api = new TikTokApi(mContext, new Handler(), null);
        api.updateWorkLocation(work());
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
        return lastUpdate == 0 ? null : new Date(lastUpdate);
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

    public void syncToServerSettings(Map<String, ?> settings)
    {
        // name
        String name = (String)settings.get(kApiName);
        if (name().equals("") && !name.equals("")) setName(name);

        // email
        String email = (String)settings.get(kApiEmail);
        if (email().equals("") && !email.equals("")) setEmail(email);

        // twitter handle
        String twh = (String)settings.get(kApiTwitterHandle);
        if (twitterHandle().equals("") && !twh.equals("")) setTwitterHandle(twh);

        // phone
        String phone  = (String)settings.get(kApiPhone);
        if (phone().equals("") && !phone.equals("")) setPhone(phone);

        // gender
        String gender = ((String)settings.get(kApiGender)).toLowerCase();
        if (gender().equals("")) {
            if (gender.equals("f")) {
                setGender(mContext.getString(R.string.gender_female));
            } else if (gender.equals("m")) {
                setGender(mContext.getString(R.string.gender_male));
            }
        }

        // birthday
        String birthday = (String)settings.get(kApiBirthday);
        if ((birthday() == null) && !birthday.equals("")) {
            try {
                Date date =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(birthday);
                setBirthday(date);
            } catch (ParseException e) {
            }
        }

        // home
        syncToServerLocation(settings, kApiHome, kTagHome, kTagHomeLocality);

        // work
        syncToServerLocation(settings, kApiWork, kTagWork, kTagWorkLocality);
    }

    //-------------------------------------------------------------------------

    private void syncToServerLocation(
        Map<String, ?> settings, String apiKey, String tagKey, final String localityKey)
    {
        Double latitude  = (Double)settings.get(apiKey + "_latitude");
        Double longitude = (Double)settings.get(apiKey + "_longitude");
        if ((getLocation(tagKey) == null) && (latitude != 0.0) && (longitude != 0.0)) {

            // update location setting
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            setLocation(tagKey, location);

            // update locality setting
            final Handler handler = new Handler();
            GoogleMapsApi api = new GoogleMapsApi(mContext, handler, new GoogleMapsApi.CompletionHandler() {
                public void onSuccess(final JsonNode node) {
                    String locality = GoogleMapsApi.parseLocality(node);
                    mEditor.putString(localityKey, locality);
                    mEditor.commit();
                }
                public void onError(Throwable error) {
                }
            });
            api.getReverseGeocodingForAddress(location);
        }
    }

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
        Location location = null;
        if ((latitude != 0.0f) && (longitude != 0.0f)) {
            String provider = null;
            location        = new Location(provider);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }

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

    public boolean notificationsEnabled()
    {
        return mPreferences.getBoolean(kTagNotifications, true);
    }

    //-------------------------------------------------------------------------

    public int notificationDefaults()
    {
        boolean sound   = mPreferences.getBoolean(kTagNotificationSound, true);
        boolean vibrate = mPreferences.getBoolean(kTagNotificationVibrate, true);
        if (sound && vibrate) {
            return Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        } else if (sound) {
            return Notification.DEFAULT_SOUND;
        } else if (vibrate) {
            return Notification.DEFAULT_VIBRATE;
        } else {
            return 0;
        }
    }

    //-------------------------------------------------------------------------

    public boolean syncedSettings()
    {
        return mPreferences.getBoolean(kTagSyncedSettings, false);
    }

    //-------------------------------------------------------------------------

    public void setSyncedSettings(boolean syncedSettings)
    {
        // update preferences
        mEditor.putBoolean(kTagSyncedSettings, syncedSettings);
        mEditor.commit();
    }

    //-------------------------------------------------------------------------
    // shared preferences listener
    //-------------------------------------------------------------------------

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        Log.i("Settings", String.format("Syncing setting %s", key));

        if (key.equals(kTagName)) {
            syncName();
        } else if (key.equals(kTagEmail)) {
            syncEmail();
        } else if (key.equals(kTagTwitterHandle)) {
            syncTwitterHandle();
        } else if (key.equals(kTagPhone)) {
            syncPhone();
        } else if (key.equals(kTagGender)) {
            syncGender();
        } else if (key.equals(kTagBirthday)) {
            syncBirthday();
        } else if (key.equals(kTagHome + "_lng")) {
            syncHome();
        } else if (key.equals(kTagWork + "_lng")) {
            syncWork();
        }
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private Context           mContext;
    private SharedPreferences mPreferences;
    private Editor            mEditor;

}
