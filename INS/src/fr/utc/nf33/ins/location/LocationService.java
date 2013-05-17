/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
public class LocationService extends Service {
  //
  private final class BestLocationProvider implements LocationSource, LocationListener {
    //
    private static final float GPS_MIN_DISTANCE = 10F;
    //
    private static final short GPS_MIN_TIME = 3000;
    //
    private static final float NETWORK_MIN_DISTANCE = 0F;
    //
    private static final short NETWORK_MIN_TIME = 30000;
    //
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    //
    private Location mCurrentBestLocation;
    //
    private OnLocationChangedListener mListener;

    @Override
    public void activate(OnLocationChangedListener listener) {
      mListener = listener;

      if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null)
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME,
            GPS_MIN_DISTANCE, this);

      if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null)
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME,
            NETWORK_MIN_DISTANCE, this);
    }

    @Override
    public void deactivate() {

    }

    /**
     * Determines whether one Location reading is better than the current Location fix.
     * 
     * @param location the new Location that you want to evaluate.
     * @return true when the new Location is better than the current one, false otherwise.
     */
    protected boolean isBetterLocation(Location location) {
      // A new location is always better than no location.
      if (mCurrentBestLocation == null) return true;

      // Check whether the new location fix is newer or older.
      long timeDelta = location.getTime() - mCurrentBestLocation.getTime();
      boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
      boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
      boolean isNewer = timeDelta > 0;

      // If it's been more than two minutes since the current location, use the new location
      // because the user has likely moved.
      if (isSignificantlyNewer)
        return true;
      // If the new location is more than two minutes older, it must be worse.
      else if (isSignificantlyOlder) return false;

      // Check whether the new location fix is more or less accurate.
      int accuracyDelta = (int) (location.getAccuracy() - mCurrentBestLocation.getAccuracy());
      boolean isLessAccurate = accuracyDelta > 0;
      boolean isMoreAccurate = accuracyDelta < 0;
      boolean isSignificantlyLessAccurate = accuracyDelta > 200;

      // Check if the old and new location are from the same provider.
      boolean isFromSameProvider =
          isSameProvider(location.getProvider(), mCurrentBestLocation.getProvider());

      // Determine location quality using a combination of timeliness and accuracy.
      if (isMoreAccurate)
        return true;
      else if (isNewer && !isLessAccurate)
        return true;
      else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
        return true;
      else
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
    public void onLocationChanged(Location location) {
      if ((mListener == null) || (!isBetterLocation(location))) return;

      mCurrentBestLocation = location;
      mListener.onLocationChanged(mCurrentBestLocation);

      LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(
          LocationIntent.NewLocation.newIntent(mCurrentBestLocation.getLatitude(),
              mCurrentBestLocation.getLongitude()));
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
  }

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public LocationService getService() {
      // Return this instance of LocationService so that clients can call public methods.
      return LocationService.this;
    }
  }

  //
  private BestLocationProvider mBestLocationProvider;

  //
  private GpsStatusListener mGpsStatusListener;

  //
  private State mInitialState;

  //
  private LocationManager mLocationManager;

  //
  private State mState;

  /**
   * 
   * @return
   */
  public BestLocationProvider getBestLocationProvider() {
    return mBestLocationProvider;
  }

  @Override
  public IBinder onBind(Intent intent) {
    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    mState =
        mInitialState =
            State.valueOf(intent.getStringExtra(LocationIntent.Transition.EXTRA_NEW_STATE));
    switch (mState) {
      case INDOOR:
        mLocationManager.addGpsStatusListener(mGpsStatusListener =
            new GpsStatusListener(this, State.INDOOR));
        break;
      case OUTDOOR:
        mBestLocationProvider = new BestLocationProvider();
        mLocationManager.addGpsStatusListener(mGpsStatusListener =
            new GpsStatusListener(this, State.OUTDOOR));
        break;
      default:
        throw new IllegalStateException("Unhandled Application State.");
    }

    return new LocalBinder(); // Binder given to clients.
  }

  @Override
  public void onCreate() {

  }

  @Override
  public boolean onUnbind(Intent intent) {
    switch (mInitialState) {
      case INDOOR:
        mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        break;
      case OUTDOOR:
        mLocationManager.removeUpdates(mBestLocationProvider);
        mBestLocationProvider = null;
        mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        break;
      default:
        throw new IllegalStateException("Unhandled Application State.");
    }

    mGpsStatusListener = null;
    mLocationManager = null;

    return false;
  }
}
