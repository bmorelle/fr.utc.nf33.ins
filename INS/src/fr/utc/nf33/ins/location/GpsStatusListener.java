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
  private final State mState;

  //
  private static final byte SATELLITES_COUNT = 3;
  //
  public static final byte SNR_THRESHOLD = 35;

  //
  GpsStatusListener(Context context, State state) {
    mContext = context;
    mState = state;
    mAverageSnr = Float.MAX_VALUE;
    mFirstFix = false;
  }

  //
  float getAverageSnr() {
    return mAverageSnr;
  }

  @Override
  public void onGpsStatusChanged(int event) {
    if (event == GpsStatus.GPS_EVENT_FIRST_FIX) mFirstFix = true;

    if ((event == GpsStatus.GPS_EVENT_STOPPED) && mFirstFix) {
      float[] snrArr = new float[SATELLITES_COUNT];

      LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
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

      LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
      lbm.sendBroadcast(LocationIntent.NewSnr.newIntent(mAverageSnr));

      if ((mState == State.INDOOR) && (mAverageSnr >= SNR_THRESHOLD))
        lbm.sendBroadcast(LocationIntent.NewState.newIntent(State.OUTDOOR.toString()));
    }
  }
}
