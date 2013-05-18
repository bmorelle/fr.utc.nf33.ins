/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;


/**
 * 
 * @author
 * 
 */
public class IndoorLocationService extends Service {
  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public IndoorLocationService getService() {
      // Return this instance of LocationService so that clients can call public methods.
      return IndoorLocationService.this;
    }
  }

  //
  private GpsStatusListener mGpsStatusListener;

  @Override
  public IBinder onBind(Intent intent) {
    onRebind(intent);

    return new LocalBinder();
  }

  @Override
  public void onRebind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.addGpsStatusListener(mGpsStatusListener = new GpsStatusListener(this, State.INDOOR));
  }

  @Override
  public boolean onUnbind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.removeGpsStatusListener(mGpsStatusListener);
    mGpsStatusListener = null;

    return true;
  }
}
