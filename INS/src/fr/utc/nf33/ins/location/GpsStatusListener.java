/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 
 * @author
 * 
 */
final class GpsStatusListener implements GpsStatus.Listener {
  //
  private float mAverageSnr;

  //
  private final Context mContext;

  //
  private boolean mFirstFix;

  //
  private final State mInitialState;

  //
  private final byte SATELLITES_COUNT = 3;

  //
  private final byte SNR_THRESHOLD = 35;

  //
  GpsStatusListener(Context context, State initialState) {
    mContext = context;
    mInitialState = initialState;
    mAverageSnr = 0F;
    mFirstFix = false;
  }

  @Override
  public void onGpsStatusChanged(int event) {
    if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
      mFirstFix = true;
    }

    if ((event == GpsStatus.GPS_EVENT_STOPPED) && mFirstFix) {
      float[] snrArr = new float[SATELLITES_COUNT];

      LocationManager locationManager =
          (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
      for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
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

      LocalBroadcastManager lbMngr = LocalBroadcastManager.getInstance(mContext);
      lbMngr.sendBroadcast(LocationIntent.NewSnr.newIntent(mAverageSnr));

      if ((mAverageSnr < SNR_THRESHOLD) && (mInitialState != State.INDOOR)) {
        lbMngr.sendBroadcast(LocationIntent.Transition.newIntent(State.INDOOR.toString()));
      } else if ((mAverageSnr >= SNR_THRESHOLD) && (mInitialState != State.OUTDOOR)) {
        lbMngr.sendBroadcast(LocationIntent.Transition.newIntent(State.OUTDOOR.toString()));
      }
    }
  }
}
