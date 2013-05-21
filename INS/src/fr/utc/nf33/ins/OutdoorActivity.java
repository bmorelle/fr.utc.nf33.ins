/**
 * 
 */
package fr.utc.nf33.ins;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import fr.utc.nf33.ins.location.Building;
import fr.utc.nf33.ins.location.CloseBuildingsService;
import fr.utc.nf33.ins.location.CloseBuildingsService.LocalBinder;
import fr.utc.nf33.ins.location.LocationHelper;
import fr.utc.nf33.ins.location.LocationIntent;
import fr.utc.nf33.ins.location.SnrService;

/**
 * 
 * @author
 * 
 */
public final class OutdoorActivity extends FragmentActivity
    implements
      GpsDialogFragment.GpsDialogListener {
  //
  private static final float DEFAULT_ZOOM_LEVEL = 17.0F;
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
  private ServiceConnection mCloseBuildingsConnection;
  //
  private CloseBuildingsService mCloseBuildingsService;
  //
  private SupportMapFragment mMapFragment;
  //
  private BroadcastReceiver mNewCloseBuildingsReceiver;
  //
  private BroadcastReceiver mNewLocationReceiver;
  //
  private BroadcastReceiver mNewSnrReceiver;
  //
  private ServiceConnection mSnrConnection;

  @Override
  public final void onBackPressed() {

  }

  /**
   * Called when the user clicks the Entry Points button.
   * 
   * @param view
   */
  public final void onButtonEntryPointsClick(View view) {
    startActivity(new Intent(this, EntryPointsActivity.class));
  }

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_outdoor);

    // Create a Google Map Fragment with desired options.
    mMapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.activity_outdoor_map, mMapFragment);
    fragmentTransaction.commit();

    // Start the SNR Service.
    Intent intent = new Intent(this, SnrService.class);
    startService(intent);

    // Start the Close Buildings Service.
    intent = new Intent(this, CloseBuildingsService.class);
    startService(intent);
  }

  @Override
  public final boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected final void onDestroy() {
    super.onDestroy();

    // Stop the Close Buildings Service.
    Intent intent = new Intent(this, CloseBuildingsService.class);
    stopService(intent);

    // Stop the SNR Service.
    intent = new Intent(this, SnrService.class);
    stopService(intent);
  }

  @Override
  public final void onGpsDialogPositiveClick(DialogFragment dialog) {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  @Override
  protected final void onStart() {
    super.onStart();

    // Check whether the GPS provider is enabled.
    if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      DialogFragment dialog = new GpsDialogFragment();
      dialog.show(getSupportFragmentManager(), GpsDialogFragment.NAME);
    }

    // Setup the map.
    GoogleMap map = mMapFragment.getMap();
    map.setMyLocationEnabled(true);

    // Connect to the SNR Service.
    Intent intent = new Intent(this, SnrService.class);
    mSnrConnection = new ServiceConnection() {
      @Override
      public final void onServiceConnected(ComponentName name, IBinder service) {

      }

      @Override
      public final void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mSnrConnection, Context.BIND_AUTO_CREATE);

    // Connect to the Close Buildings Service.
    intent = new Intent(this, CloseBuildingsService.class);
    mCloseBuildingsConnection = new ServiceConnection() {
      @Override
      public final void onServiceConnected(ComponentName name, IBinder service) {
        // We've bound to CloseBuildingsService, cast the IBinder and get LocationService instance.
        mCloseBuildingsService = ((LocalBinder) service).getService();
        mMapFragment.getMap().setLocationSource(mCloseBuildingsService.getBestLocationProvider());
      }

      @Override
      public final void onServiceDisconnected(ComponentName name) {

      }
    };
    bindService(intent, mCloseBuildingsConnection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

    mNewCloseBuildingsReceiver = new BroadcastReceiver() {
      @Override
      public final void onReceive(Context context, Intent intent) {
        List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
        ((Button) OutdoorActivity.this.findViewById(R.id.activity_outdoor_button_entry_points))
            .setText(Integer.toString(buildings.size()));
      }
    };
    lbm.registerReceiver(mNewCloseBuildingsReceiver,
        LocationIntent.NewCloseBuildings.newIntentFilter());

    mNewLocationReceiver = new BroadcastReceiver() {
      @Override
      public final void onReceive(Context context, Intent intent) {
        double lat = intent.getDoubleExtra(LocationIntent.NewLocation.EXTRA_LATITUDE, 0);
        double lon = intent.getDoubleExtra(LocationIntent.NewLocation.EXTRA_LONGITUDE, 0);
        mMapFragment.getMap().animateCamera(
            CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), DEFAULT_ZOOM_LEVEL));
      }
    };
    lbm.registerReceiver(mNewLocationReceiver, LocationIntent.NewLocation.newIntentFilter());

    mNewSnrReceiver = new BroadcastReceiver() {
      @Override
      public final void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) OutdoorActivity.this.findViewById(R.id.activity_outdoor_textview_snr))
            .setText("SNR (3 premiers): " + Float.toString(snr));

        List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
        if (LocationHelper.shouldGoIndoor(snr, buildings))
          startActivity(new Intent(OutdoorActivity.this, IndoorActivity.class));
      }
    };
    lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
  }

  @Override
  protected final void onStop() {
    super.onStop();

    // Unregister receivers.
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
    mNewCloseBuildingsReceiver = null;
    lbm.unregisterReceiver(mNewLocationReceiver);
    mNewLocationReceiver = null;
    lbm.unregisterReceiver(mNewSnrReceiver);
    mNewSnrReceiver = null;

    // Disconnect from the Close Buildings Service.
    unbindService(mCloseBuildingsConnection);
    mCloseBuildingsConnection = null;
    mCloseBuildingsService = null;

    // Disconnect from the SNR Service.
    unbindService(mSnrConnection);
    mSnrConnection = null;
  }
}
