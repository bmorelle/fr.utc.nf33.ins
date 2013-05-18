/**
 * 
 */
package fr.utc.nf33.ins;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import fr.utc.nf33.ins.location.LocationIntent;
import fr.utc.nf33.ins.location.OutdoorLocationService;
import fr.utc.nf33.ins.location.OutdoorLocationService.LocalBinder;
import fr.utc.nf33.ins.location.State;

/**
 * 
 * @author
 * 
 */
public class EntryPointsActivity extends ListActivity {
  //
  private ServiceConnection mConnection;
  //
  private BroadcastReceiver mNewCloseBuildingsReceiver;
  //
  private BroadcastReceiver mNewStateReceiver;
  //
  private OutdoorLocationService mService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_entry_points);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Connect to the Outdoor Location Service.
    Intent intent = new Intent(this, OutdoorLocationService.class);
    mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ((LocalBinder) service).getService();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    mNewCloseBuildingsReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

      }
    };
    lbm.registerReceiver(mNewCloseBuildingsReceiver,
        LocationIntent.NewCloseBuildings.newIntentFilter());

    mNewStateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        State newState = State.valueOf(intent.getStringExtra(LocationIntent.NewState.EXTRA_STATE));
        switch (newState) {
          case INDOOR:
            // startActivity(new Intent(EntryPointsActivity.this, IndoorActivity.class));
            Toast.makeText(EntryPointsActivity.this, "indoor", Toast.LENGTH_SHORT).show();
            break;
          case OUTDOOR:
            break;
          default:
            throw new IllegalStateException("Unhandled Application State.");
        }
      }
    };
    lbm.registerReceiver(mNewStateReceiver, LocationIntent.NewState.newIntentFilter());
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Disconnect from the Outdoor Location Service.
    unbindService(mConnection);
    mConnection = null;
    mService = null;

    // Unregister receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
    mNewCloseBuildingsReceiver = null;
    lbm.unregisterReceiver(mNewStateReceiver);
    mNewStateReceiver = null;
  }
}
