package wb.android.autocomplete;

import android.database.Cursor;

public interface AutoCompleteQueriable {

	public Cursor getAutoCompleteCursor(CharSequence text, CharSequence tag);
	public void onItemSelected(CharSequence text, CharSequence tag);
	
}
