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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;
import fr.utc.nf33.ins.db.DbCursorLoader;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;
import fr.utc.nf33.ins.location.OutdoorLocationService;

/**
 * 
 * @author
 * 
 */
public class EntryPointsActivity extends FragmentActivity
    implements
      LoaderManager.LoaderCallbacks<Cursor> {
  //
  private static final String[] FROM_COLUMNS = {InsContract.Building.COLUMN_NAME_NAME};
  //
  private static final int[] TO_VIEWS = {R.id.entry_points_list_item_text};

  //
  private ServiceConnection mConnection;
  //
  private CursorAdapter mCursorAdapter;
  //
  private InsDbHelper mDbHelper;
  //
  private ListView mListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry_points);

    //
    mListView = (ListView) findViewById(R.id.entry_points_list);
    mDbHelper = new InsDbHelper(this);

    // Create an empty adapter we will use to display the loaded data.
    // We pass null for the cursor, then update it in onLoadFinished().
    mCursorAdapter =
        new SimpleCursorAdapter(this, R.layout.entry_points_list_item, null, FROM_COLUMNS,
            TO_VIEWS, 0);
    mListView.setAdapter(mCursorAdapter);

    // Prepare the loader. Either re-connect with an existing one, or start a new one.
    getSupportLoaderManager().initLoader(0, null, this);
  }

  // Called when a new Loader needs to be created.
  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    return new DbCursorLoader(this) {
      @Override
      public Cursor getCursor() {
        return mDbHelper.getBuildings();
      }
    };
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    mDbHelper.close();
  }

  // Called when a previously created loader is reset, making the data unavailable.
  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed. We need to make sure we are no
    // longer using it.
    mCursorAdapter.swapCursor(null);
  }

  // Called when a previously created loader has finished loading.
  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    // Swap the new cursor in. (The framework will take care of closing the
    // old cursor once we return.)
    mCursorAdapter.swapCursor(data);
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
}
