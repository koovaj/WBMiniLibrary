package wb.android.autocomplete;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

public class AutoCompleteAdapter extends CursorAdapter implements Filterable, FilterQueryProvider {

	private final AutoCompleteQueriable queriable;
	private final CharSequence tag;
	private String last; 
	private MyClickListener myClickListener;
	
	private AutoCompleteAdapter(Activity activity, Cursor cursor, AutoCompleteQueriable queriable, CharSequence tag) {
		super(activity, cursor);
		this.queriable = queriable;
		this.tag = tag;
		this.last = "";
		setFilterQueryProvider(this);
		this.myClickListener = new MyClickListener();
		
	}
	
	public static AutoCompleteAdapter getInstance(Activity activity, AutoCompleteQueriable queriable, CharSequence tag) {
		Cursor cursor = queriable.getAutoCompleteCursor("", tag);
		return new AutoCompleteAdapter(activity, cursor, queriable, tag);
	}
	
	public void reset() {
		this.last = "";
		onPause();
	}
	
	public final void onPause() {
		Cursor cursor = getCursor();
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String text = cursor.getString(0);
		if (text.trim().equalsIgnoreCase(last.trim())) {
			view.getRootView().setVisibility(View.INVISIBLE);
		}
		else {
			((TextView) view).setText(text);
			view.getRootView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		if (parent instanceof ListView) { // Should always be the case (but to prevent against updates, etc)
			((ListView) parent).setOnItemClickListener(myClickListener);
		}
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        String item = cursor.getString(0);
        view.setText(item);
        return view;
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(0);
	}
	
	private class MyClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			queriable.onItemSelected(((TextView)view).getText(), tag);
		}
		
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		last = (constraint == null) ? "" : constraint.toString();
        return queriable.getAutoCompleteCursor(last, tag);
	}

}