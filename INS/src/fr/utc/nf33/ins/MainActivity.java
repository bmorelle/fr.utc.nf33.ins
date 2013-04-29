package fr.utc.nf33.ins;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
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
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity {

  private SupportMapFragment mapFragment;
  private LocationManager locationManager;
  private Criteria criteria;
  private String providerName;
  private OnLocationChangedListener mapListener;
  private final LocationListener listener = new LocationListener() {

    @Override
    public void onLocationChanged(Location location) {
      if (mapListener != null) {
        mapListener.onLocationChanged(location);
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      // TODO Auto-generated method stub

    }
  };

  private static GoogleMapOptions GOOGLE_MAP_OPTIONS = (new GoogleMapOptions())
      .compassEnabled(false).mapType(GoogleMap.MAP_TYPE_NORMAL).rotateGesturesEnabled(true)
      .tiltGesturesEnabled(true).zoomControlsEnabled(false).zoomGesturesEnabled(true);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Create a Google map fragment with desired options
    mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
    fragmentTransaction.commit();

    // Set up the location manager
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setCostAllowed(false);
    providerName = locationManager.getBestProvider(criteria, true);

    if (providerName != null) {
      locationManager.requestLocationUpdates(providerName, 3000, 10, listener);
    }

    locationManager.addGpsStatusListener(new GpsStatus.Listener() {
      @Override
      public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_STOPPED) {
          GpsStatus status = locationManager.getGpsStatus(null);
          float[] snrs = new float[3];
          int count = 0;
          float snr = 0;
          for (@SuppressWarnings("unused")
          GpsSatellite sat : status.getSatellites()) {
            snr = sat.getSnr();
            if (snr > snrs[0]) {
              snrs[0] = snr;
            } else if (snr > snrs[1]) {
              snrs[1] = snr;
            } else if (snr > snrs[2]) {
              snrs[2] = snr;
            }
          }
          float avg = (snrs[0] + snrs[1] + snrs[2]) / 3;
          ((TextView) MainActivity.this.findViewById(R.id.bottom)).setText("SNR (3 premiers): "
              + Float.toString(avg));

          snr = 0;
          for (@SuppressWarnings("unused")
          GpsSatellite sat : status.getSatellites()) {
            snr += sat.getSnr();
            ++count;
          }
          snr /= count;
          ((TextView) MainActivity.this.findViewById(R.id.top)).setText("SNR (tous): "
              + Float.toString(snr));
        }
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Check if GPS is enabled
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    if (!gpsEnabled) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.gps_dialog_content).setTitle(R.string.gps_dialog_title);
      builder.setPositiveButton(R.string.gps_dialog_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
          enableLocationSettings();
        }
      });
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          finish();
        }
      });
      AlertDialog dialog = builder.create();
      dialog.show();
    }

    mapFragment.getMap().setLocationSource(new LocationSource() {

      @Override
      public void activate(OnLocationChangedListener listener) {
        mapListener = listener;
      }

      @Override
      public void deactivate() {
        mapListener = null;
      }

    });
    mapFragment.getMap().setMyLocationEnabled(true);
  }

  // Display Location settings
  private void enableLocationSettings() {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onStop() {
    super.onStop();
    locationManager.removeUpdates(listener);
  }
}
