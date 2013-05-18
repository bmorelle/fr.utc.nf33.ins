/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.LocationSource;


/**
 * 
 * @author
 * 
 */
public class OutdoorLocationService extends Service {
  //
  private final class BestLocationProvider implements LocationSource, LocationListener {
    //
    private final class BestLocationTask extends AsyncTask<Void, Location, Cursor> {
      //
      private static final int TWO_MINUTES = 1000 * 60 * 2;

      //
      private final Location mBestLocation;
      //
      private final Location mLocation;

      //
      private BestLocationTask(Location location, Location bestLocation) {
        mLocation = location;
        mBestLocation = bestLocation;
      }

      @Override
      protected Cursor doInBackground(Void... params) {
        if (!isBetterLocation(mLocation, mBestLocation)) return null;

        publishProgress(mLocation);

        // TODO

        return null;
      }

      /**
       * Determines whether one Location reading is better than the current Location fix
       * 
       * @param location The new Location that you want to evaluate
       * @param currentBestLocation The current Location fix, to which you want to compare the new
       *        one
       */
      private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
          // A new location is always better than no location
          return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
          return true;
          // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
          return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider =
            isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
          return true;
        } else if (isNewer && !isLessAccurate) {
          return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
          return true;
        }
        return false;
      }

      // Checks whether two providers are the same.
      private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null)
          return provider2 == null;
        else
          return provider1.equals(provider2);
      }

      @Override
      protected void onCancelled(Cursor cursor) {
        mBestLocationTask = null;
      }

      @Override
      protected void onPostExecute(Cursor cursor) {
        // TODO

        mBestLocationTask = null;
      }

      @Override
      protected void onProgressUpdate(Location... location) {
        BestLocationProvider.this.mBestLocation = location[0];
        mListener.onLocationChanged(location[0]);
        LocalBroadcastManager.getInstance(OutdoorLocationService.this).sendBroadcast(
            LocationIntent.NewLocation.newIntent(location[0].getLatitude(),
                location[0].getLongitude()));
      }
    }

    //
    private static final float GPS_MIN_DISTANCE = 10F;
    //
    private static final short GPS_MIN_TIME = 3000;
    //
    private static final float NETWORK_MIN_DISTANCE = 0F;
    //
    private static final short NETWORK_MIN_TIME = 30000;

    //
    private Location mBestLocation;
    //
    private AsyncTask<Void, Location, Cursor> mBestLocationTask;
    //
    private OnLocationChangedListener mListener;

    @Override
    public void activate(OnLocationChangedListener listener) {
      mListener = listener;

      LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      if (lm.getProvider(LocationManager.GPS_PROVIDER) != null)
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE,
            this);
      if (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null)
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME,
            NETWORK_MIN_DISTANCE, this);
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onLocationChanged(Location location) {
      if ((mListener == null) || (mBestLocationTask != null)) return;
      mBestLocationTask = new BestLocationTask(location, mBestLocation);
      mBestLocationTask.execute();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    //
    private void onUnbind() {
      if (mBestLocationTask != null) mBestLocationTask.cancel(true);
    }
  }

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public OutdoorLocationService getService() {
      // Return this instance of LocationService so that clients can call public methods.
      return OutdoorLocationService.this;
    }
  }

  //
  private BestLocationProvider mBestLocationProvider;
  //
  private GpsStatusListener mGpsStatusListener;

  /**
   * 
   * @return
   */
  public BestLocationProvider getBestLocationProvider() {
    return mBestLocationProvider;
  }

  @Override
  public IBinder onBind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.addGpsStatusListener(mGpsStatusListener = new GpsStatusListener(this, State.OUTDOOR));

    return new LocalBinder();
  }

  @Override
  public void onCreate() {
    mBestLocationProvider = new BestLocationProvider();
  }

  @Override
  public void onDestroy() {
    mBestLocationProvider = null;
  }

  @Override
  public void onRebind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.addGpsStatusListener(mGpsStatusListener = new GpsStatusListener(this, State.OUTDOOR));
  }

  @Override
  public boolean onUnbind(Intent intent) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    lm.removeUpdates(mBestLocationProvider);
    mBestLocationProvider.onUnbind();
    lm.removeGpsStatusListener(mGpsStatusListener);
    mGpsStatusListener = null;

    return true;
  }
}
