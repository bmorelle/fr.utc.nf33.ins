/**
 * 
 */
package fr.utc.nf33.ins;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;

/**
 * 
 * @author
 * 
 */
public class EntryPointsActivity extends FragmentActivity {
  //
  private Cursor cursor;

  //
  private InsDbHelper dbHelper;

  //
  private ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry_points);

    //
    listView = (ListView) findViewById(R.id.entry_points_list);
    dbHelper = new InsDbHelper(this);
    cursor =
        dbHelper.getReadableDatabase().rawQuery("SELECT idBuilding AS _id, name FROM Building",
            null);

    // For the cursor adapter, specify which columns go into which views.
    String[] fromColumns = {InsContract.Building.COLUMN_NAME_NAME};
    int[] toViews = {R.id.entry_points_list_item_text};

    // Create an empty adapter we will use to display the loaded data.
    ListAdapter cursorAdapter =
        new SimpleCursorAdapter(this, R.layout.entry_points_list_item, cursor, fromColumns,
            toViews, 0);
    listView.setAdapter(cursorAdapter);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    cursor.close();
    dbHelper.close();
  }
}
