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
import fr.utc.nf33.ins.location.SnrService.LocalBinder;

// MOD_010
/**
 * 
 * @author
 * 
 */
public final class IndoorActivity extends Activity {
  //
  private BroadcastReceiver mNewSnrReceiver;
  //
  private SnrService mSnrService;
  //
  private boolean mSnrServiceBound;
  //
  private ServiceConnection mSnrServiceConnection;

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_indoor);

    ((TextView) findViewById(R.id.activity_indoor_textview_access_point)).setText(getIntent()
        .getStringExtra(LocationIntent.NewCloseBuildings.EXTRA_ENTRY_POINT));
  }

  @Override
  protected final void onStart() {
    super.onStart();

    // Connect to the SNR Service.
    Intent snrIntent = new Intent(this, SnrService.class);
    mSnrServiceConnection = new ServiceConnection() {
      @Override
      public final void onServiceConnected(ComponentName name, IBinder service) {
        mSnrService = ((LocalBinder) service).getService();
        ((TextView) findViewById(R.id.activity_indoor_textview_snr)).setText(String.format("%.02f",
            mSnrService.getAverageSnr()));
        mSnrServiceBound = true;
      }

      @Override
      public final void onServiceDisconnected(ComponentName name) {
        mSnrServiceBound = false;
      }
    };
    bindService(snrIntent, mSnrServiceConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    // SPECIFICATION : TRS_040
    mNewSnrReceiver = new BroadcastReceiver() {
      @Override
      public final void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) IndoorActivity.this.findViewById(R.id.activity_indoor_textview_snr))
            .setText(String.format("%.02f", snr));

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
    if (mSnrServiceBound) {
      unbindService(mSnrServiceConnection);
      mSnrService = null;
      mSnrServiceBound = false;
    }
    mSnrServiceConnection = null;
  }
}
