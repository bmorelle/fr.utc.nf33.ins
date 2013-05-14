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

/**
 * 
 * @author
 * 
 */
public class IndoorActivity extends Activity {
  //
  private boolean bound;

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

    // The Location Service is not bound.
    bound = false;
  }

  @Override
  protected void onStart() {
    // Connect to the Location Service.
    connection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        bound = true;
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        bound = false;
      }
    };
    bindService(new Intent(this, LocationService.class), connection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    newSnrReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationService.PrivateIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) IndoorActivity.this.findViewById(R.id.indoorSNR)).setText("SNR (3 premiers): "
            + Float.toString(snr));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(newSnrReceiver,
        LocationService.PrivateIntent.NewSnr.newIntentFilter());

    transitionReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        State newState =
            State.valueOf(intent
                .getStringExtra(LocationService.PrivateIntent.Transition.EXTRA_NEW_STATE));
        switch (newState) {
          case OUTDOOR:
            Intent newIntent = new Intent(IndoorActivity.this, OutdoorActivity.class);
            startActivity(newIntent);
            break;
          default:
            break;
        }
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(transitionReceiver,
        LocationService.PrivateIntent.Transition.newIntentFilter());

    super.onStart();
  }

  @Override
  protected void onStop() {
    // Disconnect from the Location Service.
    if (bound) {
      unbindService(connection);
      bound = false;
    }

    // Unregister receivers.
    LocalBroadcastManager.getInstance(this).unregisterReceiver(newSnrReceiver);
    newSnrReceiver = null;
    LocalBroadcastManager.getInstance(this).unregisterReceiver(transitionReceiver);
    transitionReceiver = null;
    connection = null;

    super.onStop();
  }
}
