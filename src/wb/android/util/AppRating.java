package wb.android.util;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class AppRating {
    
	private static final String DONT_SHOW = "dont_show";
	private static final String LAUNCH_COUNT = "launches";
	private static final String THRESHOLD = "threshold";
	
	
    public static final void onLaunch(Context context, int launchesUntilPrompt, String appName, String packageName) {
        (new PreferenceChecker(context, launchesUntilPrompt, appName, packageName)).execute(new Void[0]);
    }
	
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class PreferenceChecker extends AsyncTask<Void, Void, Integer> {
		
    	private Context mContext;
		private String mAppName, mPackageName;
		private int mLaunchesTilPrompt, mThreshold;
		
		public PreferenceChecker(Context context, int launchesUntilPrompt, String appName, String packageName) {
			mContext = context;
			mAppName = appName;
			mPackageName = packageName;
			mLaunchesTilPrompt = launchesUntilPrompt;
			mThreshold = mLaunchesTilPrompt;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			SharedPreferences prefs = mContext.getSharedPreferences(mAppName + "rating", 0);
	        if (prefs.getBoolean(DONT_SHOW, false)) 
	        	return -1;
	        SharedPreferences.Editor editor = prefs.edit();
	        final int launchCount = prefs.getInt(LAUNCH_COUNT, 0) + 1;
	        mThreshold = prefs.getInt(THRESHOLD, mLaunchesTilPrompt);
	        editor.putInt(LAUNCH_COUNT, launchCount);
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	        	editor.apply();
	    	else
	    		editor.commit();
			return launchCount;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if (result > mThreshold && mContext != null) {
				SharedPreferences prefs = mContext.getSharedPreferences(mAppName + "rating", 0);
		        final SharedPreferences.Editor editor = prefs.edit();
				final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    		builder.setTitle("Like " + mAppName + "?")
	    			   .setCancelable(true)
	    			   .setPositiveButton("Rate It Now", new DialogInterface.OnClickListener() {
	    		           public void onClick(DialogInterface dialog, int id) {
	    		        	   editor.putBoolean(DONT_SHOW, true);
	    		        	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	    		        	    	editor.apply();
	    		            	else
	    		            		editor.commit();
	    		        	    Log.e("AppRating", ""+ mContext);
	    		        	   	mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mPackageName)));
	    		        	   	clear();
	    		                dialog.cancel();
	    		           }
	    		       })
	    		       .setNeutralButton("Later", new DialogInterface.OnClickListener() {
	    		           public void onClick(DialogInterface dialog, int id) {
	    		        	   editor.putInt(THRESHOLD, mThreshold + 10);
	    		        	   	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	    		        	   		editor.apply();
	    		            	else
	    		            		editor.commit();
	    		        	   	clear();
	    		                dialog.cancel();
	    		           }
	    		       })
	    			   .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
	    		           public void onClick(DialogInterface dialog, int id) {
	    		        	   editor.putBoolean(DONT_SHOW, true);
	    		        	   	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	    		        	   		editor.apply();
	    		            	else
	    		            		editor.commit();
	    		        	   	clear();
	    		                dialog.cancel();
	    		           }
	    		       })
	    		       .create()
	    		       .show();    	
    		}
    		else {
	    		clear();
    		}
		}
		
		@Override
		protected void onCancelled() {
			clear();
		}
		
		private void clear() {
			mContext = null; // Make sure we don't persist this for mem leaks
		}
		
	}
    
}