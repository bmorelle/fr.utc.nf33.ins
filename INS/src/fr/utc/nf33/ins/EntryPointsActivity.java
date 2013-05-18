/**
 * 
 */
package fr.utc.nf33.ins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;
import fr.utc.nf33.ins.location.OutdoorLocationService;

/**
 * 
 * @author
 * 
 */
public class EntryPointsActivity extends FragmentActivity {
  //
  private Cursor mCursor;
  //
  private InsDbHelper mDbHelper;
  //
  private ListView mListView;
  //
  private ServiceConnection mConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry_points);

    //
    mListView = (ListView) findViewById(R.id.entry_points_list);
    mDbHelper = new InsDbHelper(this);
    mCursor = mDbHelper.getBuildings();

    // For the cursor adapter, specify which columns go into which views.
    String[] fromColumns = {InsContract.Building.COLUMN_NAME_NAME};
    int[] toViews = {R.id.entry_points_list_item_text};

    // Create an adapter we will use to display the loaded data.
    ListAdapter cursorAdapter =
        new SimpleCursorAdapter(this, R.layout.entry_points_list_item, mCursor, fromColumns,
            toViews, 0);
    mListView.setAdapter(cursorAdapter);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Connect to the Outdoor Location Service.
    Intent intent = new Intent(this, OutdoorLocationService.class);
    mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {

      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    // TODO
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Disconnect from the Outdoor Location Service.
    unbindService(mConnection);
    mConnection = null;

    // Unregister receivers.
    // TODO
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    mCursor.close();
    mDbHelper.close();
  }
}
