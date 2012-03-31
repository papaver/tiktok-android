//-----------------------------------------------------------------------------
// IOUtilities
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp.utilities;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Closeable;

import android.util.Log;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class IOUtilities
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "IOUtilities";

    public static final int kIOBufferSize = 4 * 1024;

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    /**
     * Copy the content of the input stream into the output stream, using a temporary
     * byte array buffer whose size is defined by {@link #kIOBufferSize}.
     */
    public static boolean copy(InputStream input, OutputStream output)
    {
        try {
            int read;
            byte[] buffer = new byte[kIOBufferSize];
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e(kLogTag, "Failed to copy stream.", e);
            return false;
        }

        return true;
    }

    //-------------------------------------------------------------------------

    /**
     * Closes the specified stream.
     */
    public static void closeStream(Closeable stream)
    {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(kLogTag, "Failed to close stream.", e);
            }
        }
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    private IOUtilities()
    {
    }
}
