/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


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
      Log.d("event GPS", Integer.toString(event));
      if (event == GpsStatus.GPS_EVENT_FIRST_FIX) mFirstFix = true;

      if ((mFirstFix && (event == GpsStatus.GPS_EVENT_STOPPED))
          || ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) && (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS))) {
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
  private class SimpleLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location arg0) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
  }

  //
  private GpsStatusListener mGpsStatusListener;
  //
  private SimpleLocationListener mSimpleLocationListener;

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
    mSimpleLocationListener = new SimpleLocationListener();
  }

  @Override
  public final void onDestroy() {
    mGpsStatusListener = null;
    mSimpleLocationListener = null;
  }

  @Override
  public final void onRebind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (lm.getProvider(LocationManager.GPS_PROVIDER) != null)
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          CloseBuildingsService.BestLocationProvider.GPS_MIN_TIME,
          CloseBuildingsService.BestLocationProvider.GPS_MIN_DISTANCE, mSimpleLocationListener);
    lm.addGpsStatusListener(mGpsStatusListener);
  }

  @Override
  public final boolean onUnbind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.removeGpsStatusListener(mGpsStatusListener);
    lm.removeUpdates(mSimpleLocationListener);

    return true;
  }
}
