//-----------------------------------------------------------------------------
// Device
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.UUID;

import android.content.Context;
import android.provider.Settings.Secure;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class Device
{

    /**
     * @return Return a newly generated GUID.
     */
    static public String generateGUID()
    {
        UUID guid = UUID.randomUUID();
        return guid.toString();
    }

    //-------------------------------------------------------------------------

    /**
     * @return Return the device's unique identifier.
     */
    static public String deviceId(Context context)
    {
        String deviceId = Secure.getString(context.getContentResolver(),
            Secure.ANDROID_ID);
        return deviceId;
    }

}
