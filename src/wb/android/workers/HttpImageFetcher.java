/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wb.android.workers;

import java.io.InputStream;
import java.net.URL;

import wb.android.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * Adapted from: http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class HttpImageFetcher extends ImageResizer {
	
    private static final String TAG = "HttpImageFetcher";
    
    private final Context mContext;

    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public HttpImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        mContext = context;
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public HttpImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        mContext = context;
    }

    /**
    * Simple network connection check.
    *
    * @param context
    */
    public static boolean hasInternet(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return false;
        }
        else {
        	return true;
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        if (Utils.DEBUG) Log.d(TAG, "processBitmap - " + data);
        if (hasInternet(mContext)) {
	        disableConnectionReuseIfNecessary();
	        try {
	        	return BitmapFactory.decodeStream((InputStream)new URL(data).getContent());
	        } catch (final Exception e) {
	            if(Utils.DEBUG) Log.e(TAG, "Error in downloadBitmap - " + e);
	            return null;
	        }
        }
        else {
        	return null;
        }
        
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
