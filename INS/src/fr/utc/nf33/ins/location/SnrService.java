/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;


/**
 * 
 * @author
 * 
 */
public final class SnrService extends Service {
  //
  private final class GpsStatusListener implements GpsStatus.Listener {
    //
    private static final byte SATELLITES_COUNT = 3;

    //
    private float mAverageSnr;
    //
    private boolean mFirstFix;

    //
    private GpsStatusListener() {
      mAverageSnr = Float.MAX_VALUE;
      mFirstFix = false;
    }

    @Override
    public final void onGpsStatusChanged(int event) {
      if (event == GpsStatus.GPS_EVENT_FIRST_FIX) mFirstFix = true;

      if (mFirstFix && (event == GpsStatus.GPS_EVENT_STOPPED)) {
        float[] snrArr = new float[SATELLITES_COUNT];

        LocationManager lm =
            (LocationManager) SnrService.this.getSystemService(Context.LOCATION_SERVICE);
        for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites()) {
          int min = 0;
          for (int s = 0; s < SATELLITES_COUNT; ++s)
            if (snrArr[s] < snrArr[min]) min = s;

          float snr = sat.getSnr();
          if (snr > snrArr[min]) snrArr[min] = snr;
        }

        float newAvgSnr = 0;
        for (float snr : snrArr)
          newAvgSnr += snr;
        newAvgSnr /= SATELLITES_COUNT;

        if (newAvgSnr != 0) mAverageSnr = newAvgSnr;

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(SnrService.this);
        lbm.sendBroadcast(LocationIntent.NewSnr.newIntent(mAverageSnr));
      }
    }
  }

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public SnrService getService() {
      // Return this instance of LocationService so that clients can call public methods.
      return SnrService.this;
    }
  }

  //
  private GpsStatusListener mGpsStatusListener;

  /**
   * 
   * @return
   */
  public final float getAverageSnr() {
    return mGpsStatusListener.mAverageSnr;
  }

  @Override
  public final IBinder onBind(Intent intent) {
    onRebind(intent);

    return new LocalBinder();
  }

  @Override
  public final void onCreate() {
    mGpsStatusListener = new GpsStatusListener();
  }

  @Override
  public final void onDestroy() {
    mGpsStatusListener = null;
  }

  @Override
  public final void onRebind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.addGpsStatusListener(mGpsStatusListener);
  }

  @Override
  public final boolean onUnbind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.removeGpsStatusListener(mGpsStatusListener);

    return true;
  }
}
