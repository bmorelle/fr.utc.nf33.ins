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
import fr.utc.nf33.ins.location.LocationHelper;
import fr.utc.nf33.ins.location.LocationIntent;
import fr.utc.nf33.ins.location.SnrService;

/**
 * 
 * @author
 * 
 */
public final class IndoorActivity extends Activity {
  //
  private ServiceConnection mSnrConnection;
  //
  private BroadcastReceiver mNewSnrReceiver;

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_indoor);
  }

  @Override
  protected final void onStart() {
    super.onStart();

    // Connect to the SNR Service.
    Intent intent = new Intent(this, SnrService.class);
    mSnrConnection = new ServiceConnection() {
      @Override
      public final void onServiceConnected(ComponentName name, IBinder service) {

      }

      @Override
      public final void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mSnrConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    mNewSnrReceiver = new BroadcastReceiver() {
      @Override
      public final void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) IndoorActivity.this.findViewById(R.id.indoorSNR)).setText("SNR (3 premiers): "
            + Float.toString(snr));

        if (LocationHelper.shouldGoOutdoor(snr))
          startActivity(new Intent(IndoorActivity.this, OutdoorActivity.class));
      }
    };
    lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
  }

  @Override
  protected final void onStop() {
    super.onStop();

    // Unregister receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    lbm.unregisterReceiver(mNewSnrReceiver);
    mNewSnrReceiver = null;

    // Disconnect from the SNR Service.
    unbindService(mSnrConnection);
    mSnrConnection = null;
  }
}
