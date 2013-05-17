/**
 * 
 */
package fr.utc.nf33.ins;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import fr.utc.nf33.ins.location.LocationIntent;
import fr.utc.nf33.ins.location.LocationService;
import fr.utc.nf33.ins.location.State;

/**
 * 
 * @author
 * 
 */
public class IndoorActivity extends Activity {
  //
  private ServiceConnection connection;

  //
  private BroadcastReceiver newSnrReceiver;

  //
  private BroadcastReceiver transitionReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_indoor);
  }

  @Override
  protected void onStart() {
    // Connect to the Location Service.
    Intent intent = new Intent(this, LocationService.class);
    intent.putExtra(LocationIntent.Transition.EXTRA_NEW_STATE, State.INDOOR.toString());
    connection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {

      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    newSnrReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) IndoorActivity.this.findViewById(R.id.indoorSNR)).setText("SNR (3 premiers): "
            + Float.toString(snr));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(newSnrReceiver,
        LocationIntent.NewSnr.newIntentFilter());

    transitionReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        State newState =
            State.valueOf(intent.getStringExtra(LocationIntent.Transition.EXTRA_NEW_STATE));
        switch (newState) {
          case OUTDOOR:
            startActivity(new Intent(IndoorActivity.this, OutdoorActivity.class));
            break;
          case INDOOR:
            break;
          default:
            throw new IllegalStateException("Unhandled Application State.");
        }
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(transitionReceiver,
        LocationIntent.Transition.newIntentFilter());

    super.onStart();
  }

  @Override
  protected void onStop() {
    // Disconnect from the Location Service.
    unbindService(connection);

    // Unregister receivers.
    LocalBroadcastManager.getInstance(this).unregisterReceiver(newSnrReceiver);
    newSnrReceiver = null;
    LocalBroadcastManager.getInstance(this).unregisterReceiver(transitionReceiver);
    transitionReceiver = null;
    connection = null;

    super.onStop();
  }
}
