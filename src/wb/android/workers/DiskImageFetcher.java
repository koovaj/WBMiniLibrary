package wb.android.workers;

import wb.android.util.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DiskImageFetcher extends ImageResizer {

private static final String TAG = "HttpImageFetcher";
    
    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public DiskImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public DiskImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
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
        try {
        	return BitmapFactory.decodeFile(data);
        } catch (final Exception e) {
            if(Utils.DEBUG) Log.e(TAG, "Error in getFile - " + e);
            return null;
        }
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }
	
}