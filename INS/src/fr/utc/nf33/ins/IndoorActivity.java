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
import fr.utc.nf33.ins.location.IndoorLocationService;
import fr.utc.nf33.ins.location.LocationIntent;
import fr.utc.nf33.ins.location.State;

/**
 * 
 * @author
 * 
 */
public class IndoorActivity extends Activity {
  //
  private ServiceConnection mConnection;
  //
  private BroadcastReceiver mNewSnrReceiver;
  //
  private BroadcastReceiver mTransitionReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_indoor);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Connect to the Indoor Location Service.
    Intent intent = new Intent(this, IndoorLocationService.class);
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
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    mNewSnrReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) IndoorActivity.this.findViewById(R.id.indoorSNR)).setText("SNR (3 premiers): "
            + Float.toString(snr));
      }
    };
    lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());

    mTransitionReceiver = new BroadcastReceiver() {
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
    lbm.registerReceiver(mTransitionReceiver, LocationIntent.Transition.newIntentFilter());
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Disconnect from the Indoor Location Service.
    unbindService(mConnection);
    mConnection = null;

    // Unregister receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    lbm.unregisterReceiver(mNewSnrReceiver);
    mNewSnrReceiver = null;
    lbm.unregisterReceiver(mTransitionReceiver);
    mTransitionReceiver = null;
  }
}
