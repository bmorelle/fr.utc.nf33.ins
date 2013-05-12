package fr.utc.nf33.ins;

import fr.utc.nf33.ins.LocationUpdater.LocalBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

public class IndoorActivity extends Activity {
  
  private LocationUpdater mService;

  private boolean mBound = false;
  
  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_indoor);
  }
  
  @Override
  protected void onStart() {
    
 // Bind to the Service
    Intent intent = new Intent(this, LocationUpdater.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

 // Register for Broadcasts
    LocalBroadcastManager.getInstance(this).registerReceiver(
      mTransitionBroadcast, new IntentFilter("transition"));
    LocalBroadcastManager.getInstance(this).registerReceiver(
      mSNRBroadcast, new IntentFilter("snr"));
    
    super.onStart();
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mTransitionBroadcast);
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mSNRBroadcast);
    
  }
  
  private BroadcastReceiver mTransitionBroadcast = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int newSituation = intent.getIntExtra("situation", LocationUpdater.OUTDOOR);
      if (newSituation == LocationUpdater.OUTDOOR) {
        Intent newIntent = new Intent(IndoorActivity.this, MainActivity.class);
        startActivity(newIntent);
      }
    }
  };
  
  private BroadcastReceiver mSNRBroadcast = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      float snr = intent.getFloatExtra("snr", 0);
      ((TextView) IndoorActivity.this.findViewById(R.id.indoorSNR)).setText("SNR (3 premiers): "
          + Float.toString(snr));
    }
  };
  
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocationUpdater, cast the IBinder and get LocationUpdater instance
      LocalBinder binder = (LocalBinder) service;
      mService = binder.getService();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

}
