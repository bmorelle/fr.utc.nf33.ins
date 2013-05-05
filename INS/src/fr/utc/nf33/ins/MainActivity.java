package fr.utc.nf33.ins;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * 
 * @author
 * 
 */
public final class MainActivity extends FragmentActivity {
  //
  private final class BestLocationProvider implements LocationSource, LocationListener {
    //
    private static final float GPS_MIN_DISTANCE = 10;
    //
    private static final long GPS_MIN_TIME = 3000;
    //
    private static final float NETWORK_MIN_DISTANCE = 0;
    //
    private static final long TWO_MINUTES = 1000 * 60 * 2;
    //
    private static final long NETWORK_MIN_TIME = 30000;

    //
    private Location currentBestLocation;

    //
    private OnLocationChangedListener listener;

    @Override
    public void activate(OnLocationChangedListener listener) {
      this.listener = listener;

      if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME,
            GPS_MIN_DISTANCE, this);

      if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME,
            NETWORK_MIN_DISTANCE, this);
    }

    @Override
    public void deactivate() {
      locationManager.removeUpdates(this);
    }

    /**
     * Determines whether one Location reading is better than the current Location fix.
     * 
     * @param location the new Location that you want to evaluate.
     * @return true when the new Location is better than the current one, false otherwise.
     */
    protected boolean isBetterLocation(Location location) {
      // A new location is always better than no location.
      if (currentBestLocation == null) return true;

      // Check whether the new location fix is newer or older.
      long timeDelta = location.getTime() - currentBestLocation.getTime();
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
      int accuracyDelta =
          (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
      boolean isLessAccurate = accuracyDelta > 0;
      boolean isMoreAccurate = accuracyDelta < 0;
      boolean isSignificantlyLessAccurate = accuracyDelta > 200;

      // Check if the old and new location are from the same provider.
      boolean isFromSameProvider =
          isSameProvider(location.getProvider(), currentBestLocation.getProvider());

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
      if ((listener == null) || (!isBetterLocation(location))) return;

      currentBestLocation = location;
      listener.onLocationChanged(currentBestLocation);

      // TODO
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

  //
  private final class GpsStatusListener implements GpsStatus.Listener {
    
    float avg_snr = 0;
    int NUMBER_SATS = 3;
    int SNR_LIMIT = 35;
    
    @Override
    public void onGpsStatusChanged(int event) {
      
      if (event == GpsStatus.GPS_EVENT_STOPPED) {
        // TODO

        GpsStatus status = locationManager.getGpsStatus(null);
        
        float[] snrs = new float[NUMBER_SATS];
        float snr = 0;
        float avg = 0;
        int min = 0;
        
        for (GpsSatellite sat : status.getSatellites()) {
          min = 0;
          snr = sat.getSnr();
          for (int i = 0; i < NUMBER_SATS; ++i) {
            if(snrs[i] < snrs[min]) { min = i; }
          }
          if(snr > snrs[min]) { snrs[min] = snr; }
        }
        for (float i : snrs) {
          avg += i;
        }
        avg /= NUMBER_SATS;
        
        if(avg != 0)
        {
          avg_snr = avg;
        }
        
        ((TextView) MainActivity.this.findViewById(R.id.bottom)).setText("SNR (3 premiers): "
            + Float.toString(avg_snr));
        if(avg_snr < SNR_LIMIT)
        {
          showNotification();
        }
        else
        {
          dismissNotification();
        }
      }
    }
  }

  //
  private static final GoogleMapOptions GOOGLE_MAP_OPTIONS = new GoogleMapOptions();
  static {
    GOOGLE_MAP_OPTIONS.compassEnabled(false);
    GOOGLE_MAP_OPTIONS.mapType(GoogleMap.MAP_TYPE_NORMAL);
    GOOGLE_MAP_OPTIONS.rotateGesturesEnabled(true);
    GOOGLE_MAP_OPTIONS.tiltGesturesEnabled(true);
    GOOGLE_MAP_OPTIONS.zoomControlsEnabled(false);
    GOOGLE_MAP_OPTIONS.zoomGesturesEnabled(true);
  }

  //
  private BestLocationProvider bestLocationProvider;
  //
  private GpsStatus.Listener gpsStatusListener;
  //
  private LocationManager locationManager;
  //
  private SupportMapFragment mapFragment;
  
  private AlertDialog notification;
  
  public void showNotification() {
    notification.show();
  }
  
  public void dismissNotification() {
    notification.dismiss();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Create a Google Map Fragment with desired options.
    mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
    fragmentTransaction.commit();
    
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.snr_above_limit_title);
    builder.setMessage(R.string.snr_above_limit_content);
    builder.setCancelable(false);
    notification = builder.create();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Get the Location Manager.
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    // Check whether the GPS provider is enabled.
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.gps_dialog_title);
      builder.setMessage(R.string.gps_dialog_content);
      builder.setPositiveButton(R.string.gps_dialog_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
          Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          startActivity(settingsIntent);
        }
      });
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          finish();
        }
      });

      builder.create().show();
    }

    // Setup the map.
    GoogleMap map = mapFragment.getMap();
    map.setMyLocationEnabled(true);
    map.setLocationSource(bestLocationProvider = new BestLocationProvider());

    // Add the GPS status listener.
    locationManager.addGpsStatusListener(gpsStatusListener = new GpsStatusListener());
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Remove the GPS status listener.
    locationManager.removeGpsStatusListener(gpsStatusListener);
  }
}
