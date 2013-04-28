package wb.android.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public abstract class ProgressTask<T, V> extends AsyncTask<T, Void, V> {

	private ProgressDialog dialog;
	private final String progressMessage;
	private final boolean showDialog;
	
	public ProgressTask(Activity activity, String progressMessage, boolean showDialog) {
		this.progressMessage = progressMessage;
		this.showDialog = showDialog;
		if (showDialog) this.dialog = new ProgressDialog(activity);
	}
	
	protected final void onPreExecute() {
		if (showDialog) {
	        dialog.setMessage(progressMessage);
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(false);
	        dialog.show();
		}
	}
	
	@Override
	protected final void onPostExecute(V v) {
		if (showDialog) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
		onTaskCompleted(v);
	}
	
	protected abstract void onTaskCompleted(V v);
	
	
}
