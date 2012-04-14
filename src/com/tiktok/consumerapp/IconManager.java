//-----------------------------------------------------------------------------
// IconManager
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.lang.ref.SoftReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tiktok.consumerapp.drawable.BitmapDrawable;
import com.tiktok.consumerapp.utilities.IOUtilities;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class IconManager
{
    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "IconManager";

    private static final BitmapDrawable kNullDrawable = new BitmapDrawable(null);

    private static final int kConnectTimeout = 5000;
    private static final int kReadTimeout    = 10000;

    //-------------------------------------------------------------------------
    // IconData
    //-------------------------------------------------------------------------

    public static class IconData
    {
        public IconData(int id, String url)
        {
            this.id  = id;
            this.url = url;
        }

        public String getImageName()
        {
            return Integer.toString(id);
        }

        public final int    id;
        public final String url;
    }

    //-------------------------------------------------------------------------
    // FileCache
    //-------------------------------------------------------------------------

    private class FileCache
    {
        public FileCache(Context context)
        {
            mDirectory = context.getDir("icons", android.content.Context.MODE_PRIVATE);

            // make sure the directory exists
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }

            // add no media tag
            File noMedia = new File(mDirectory, ".nomedia");
            try {
                if (!noMedia.exists()) noMedia.createNewFile();
            } catch (Exception e) {
            }
        }

        public File getFile(IconData data)
        {
            String filename = data.getImageName();
            File file       = new File(mDirectory, filename);
            return file;
        }

        public void removeFile(IconData data)
        {
            String filename = data.getImageName();
            File file       = new File(mDirectory, filename);
            if (file.exists()) file.delete();
        }

        public void clear()
        {
            File[] files = mDirectory.listFiles();

            // nothing to delete
            if (files == null) return;

            // delete all the files in the directory
            for (File file: files) {
                file.delete();
            }
        }

        private File mDirectory;
    }

    //-------------------------------------------------------------------------
    // CompletionHandler
    //-------------------------------------------------------------------------

    interface CompletionHandler
    {
        public abstract void onSuccess(final BitmapDrawable drawable);
        public abstract void onFailure();
    }

    //-------------------------------------------------------------------------
    // IconDownloader
    //-------------------------------------------------------------------------

    private class IconDownloader implements Runnable
    {
        public IconDownloader(IconData iconData, CompletionHandler handler)
        {
            mHandler  = handler;
            mIconData = iconData;
        }

        public void run()
        {
            // download icon from server
            BitmapDrawable drawable = downloadIcon(mIconData);

            // add to image cache
            String imageId = mIconData.getImageName();
            sImages.put(imageId, new SoftReference<BitmapDrawable>(drawable));

            // run the completion handler
            if (drawable != kNullDrawable) {
                if (mHandler != null) mHandler.onSuccess(drawable);
            } else {
                if (mHandler != null) mHandler.onFailure();
            }
        }

        private final IconData          mIconData;
        private final CompletionHandler mHandler;
    }

    //-------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------

    public IconManager(Context context)
    {
        mFileCache = new FileCache(context);
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public BitmapDrawable getImage(IconData iconData)
    {
        BitmapDrawable drawable = null;
        String imageId          = iconData.getImageName();

        // check if image already exist in the memory cache
        SoftReference<BitmapDrawable> reference = sImages.get(imageId);
        if (reference != null) {
            drawable = reference.get();
        }

        // grab image from file system cache if it exists
        if (drawable == null) {
            final Bitmap bitmap = loadIcon(iconData);
            if (bitmap != null) {
                drawable = new BitmapDrawable(bitmap);
            } else {
                drawable = kNullDrawable;
            }

            sImages.put(imageId, new SoftReference<BitmapDrawable>(drawable));
        }

        return drawable == kNullDrawable ? null : drawable;
    }

    //-------------------------------------------------------------------------

    public void requestImage(IconData iconData, CompletionHandler handler)
    {
        mExecutorService.submit(new IconDownloader(iconData, handler));
    }

    //-------------------------------------------------------------------------

    public void deleteImage(IconData iconData)
    {
        sImages.remove(iconData.getImageName());
        mFileCache.removeFile(iconData);
    }

    //-------------------------------------------------------------------------

    public void deleteAllImages()
    {
        sImages.clear();
        mFileCache.clear();
    }

    //-------------------------------------------------------------------------

    public void clearAllRequests()
    {
        mExecutorService.shutdownNow();
    }

    //-------------------------------------------------------------------------

    private Bitmap loadIcon(IconData iconData)
    {
        final File file = mFileCache.getFile(iconData);
        if (file.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(file);
                return BitmapFactory.decodeStream(stream, null, null);
            } catch (FileNotFoundException e) {
                // ignore
            } finally {
                IOUtilities.closeStream(stream);
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------

    private void saveIcon(IconData iconData, Bitmap bitmap)
    {
        // remove the file if it already exists
        final File file = mFileCache.getFile(iconData);
        if (file.exists()) {
            file.delete();
        }

        // save the file to the file system
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } catch (FileNotFoundException e) {
            Log.e(kLogTag, String.format(
                "Failed to save icon: %s.", iconData.getImageName()), e);
        } finally {
            IOUtilities.closeStream(output);
        }
    }

    //-------------------------------------------------------------------------

    private BitmapDrawable downloadIcon(IconData iconData)
    {
        // [moiz] looks like URLConnection will not automatically forward
        //   requests across protocols, because of the jump from http to https
        //   we have to manually check weather a redirect occured and deal with
        //   it manually

        BitmapDrawable drawable      = kNullDrawable;
        HttpURLConnection connection = null;

        try {

            // create a connection to the server
            connection = (HttpURLConnection)new URL(iconData.url).openConnection();
            connection.setConnectTimeout(kConnectTimeout);
            connection.setReadTimeout(kReadTimeout);

            // get the response code
            int responseCode = connection.getResponseCode();
            if ((responseCode == java.net.HttpURLConnection.HTTP_MOVED_PERM) ||
                (responseCode == java.net.HttpURLConnection.HTTP_MOVED_TEMP)) {

                // grab the url redirect
                URL redirectUrl = connection.getURL();

                // close the existing connection and create another one
                connection.disconnect();
                connection = (HttpURLConnection)redirectUrl.openConnection();
                connection.setConnectTimeout(kConnectTimeout);
                connection.setReadTimeout(kReadTimeout);
            }

            // check if we got a proper response this time
            responseCode = connection.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                saveIcon(iconData, bitmap);
                drawable = new BitmapDrawable(bitmap);
            } else {
                Log.e(kLogTag, "Failed to download icon.");
            }

        } catch (Exception e) {
            Log.e(kLogTag, "Failed to download icon.", e);
        } finally {
            connection.disconnect();
        }

        return drawable;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    /**
     * Memory cache for icons.
     */
    private static Map<String, SoftReference<BitmapDrawable>> sImages =
        Collections.synchronizedMap(new HashMap<String, SoftReference<BitmapDrawable>>());

    /**
     * File cache for icons.
     */
    private FileCache mFileCache;

    /**
     * Thread service for downloading images in the background.
     */
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);

}
