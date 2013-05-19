/**
 * 
 */
package fr.utc.nf33.ins;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;
import fr.utc.nf33.ins.location.Building;
import fr.utc.nf33.ins.location.CloseBuildingsService;
import fr.utc.nf33.ins.location.CloseBuildingsService.LocalBinder;
import fr.utc.nf33.ins.location.LocationHelper;
import fr.utc.nf33.ins.location.LocationIntent;

/**
 * 
 * @author
 * 
 */
public final class EntryPointsActivity extends ListActivity {
  //
  private ServiceConnection mCloseBuildingsConnection;
  //
  private CloseBuildingsService mCloseBuildingsService;
  //
  private BroadcastReceiver mNewCloseBuildingsReceiver;

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_entry_points);
  }

  @Override
  protected final void onStart() {
    super.onStart();

    // Connect to the Close Buildings Service.
    Intent intent = new Intent(this, CloseBuildingsService.class);
    mCloseBuildingsConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mCloseBuildingsService = ((LocalBinder) service).getService();

        List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
        if (buildings == null) return;
        setListAdapter(new ArrayAdapter<Building>(EntryPointsActivity.this,
            R.id.entry_points_list_item_text, buildings.toArray(new Building[0])));
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mCloseBuildingsConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    mNewCloseBuildingsReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        List<Building> closeBuildings = mCloseBuildingsService.getCloseBuildings();
        setListAdapter(new ArrayAdapter<Building>(EntryPointsActivity.this,
            R.id.entry_points_list_item_text, closeBuildings.toArray(new Building[0])));
        if (LocationHelper.shouldGoIndoor(closeBuildings))
          startActivity(new Intent(EntryPointsActivity.this, IndoorActivity.class));
      }
    };
    lbm.registerReceiver(mNewCloseBuildingsReceiver,
        LocationIntent.NewCloseBuildings.newIntentFilter());
  }

  @Override
  protected final void onStop() {
    super.onStop();

    // Disconnect from the Close Buildings Service.
    unbindService(mCloseBuildingsConnection);
    mCloseBuildingsConnection = null;
    mCloseBuildingsService = null;

    // Unregister receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
    mNewCloseBuildingsReceiver = null;
  }
}
