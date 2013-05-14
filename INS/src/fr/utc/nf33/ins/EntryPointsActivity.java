/**
 * 
 */
package fr.utc.nf33.ins;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;

/**
 * 
 * @author
 * 
 */
public class EntryPointsActivity extends FragmentActivity
    implements
      LoaderManager.LoaderCallbacks<Cursor> {
  //
  private CursorAdapter cursorAdapter;

  //
  private InsDbHelper dbHelper;

  //
  private ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry_points);

    //
    dbHelper = new InsDbHelper(this);
    listView = (ListView) findViewById(R.id.entry_points_list);

    // For the cursor adapter, specify which columns go into which views.
    String[] fromColumns = {InsContract.Building.COLUMN_NAME_NAME};
    int[] toViews = {R.id.entry_points_list_item_text};

    // Create an empty adapter we will use to display the loaded data.
    // We pass null for the cursor, then update it in onLoadFinished().
    cursorAdapter =
        new SimpleCursorAdapter(this, R.layout.entry_points_list_item, null, fromColumns, toViews,
            0);
    listView.setAdapter(cursorAdapter);

    // Prepare the loader. Either re-connect with an existing one, or start a new one.
    getSupportLoaderManager().initLoader(0, null, this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    dbHelper.close();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return null;
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    cursorAdapter.swapCursor(null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    cursorAdapter.swapCursor(data);
  }
}
