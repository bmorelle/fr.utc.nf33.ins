/**
 * 
 */
package fr.utc.nf33.ins;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import fr.utc.nf33.ins.LocationService.LocalBinder;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;

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
  private boolean bound;

  // Defines callbacks for service binding, passed to bindService().
  private ServiceConnection connection;

  //
  private SupportMapFragment mapFragment;

  //
  private BroadcastReceiver newLocationReceiver;

  //
  private BroadcastReceiver newSnrReceiver;

  //
  private BroadcastReceiver transitionReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Create a Google Map Fragment with desired options.
    mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
    fragmentTransaction.commit();

    // The Location Service is not bound.
    bound = false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onGpsDialogPositiveClick(DialogFragment dialog) {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Check whether the GPS provider is enabled.
    if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      DialogFragment dialog = new GpsDialogFragment();
      dialog.show(getSupportFragmentManager(), GpsDialogFragment.NAME);
    }

    // Setup the map.
    GoogleMap map = mapFragment.getMap();
    map.setMyLocationEnabled(true);

    // Connect to the Location Service.
    Intent intent = new Intent(this, LocationService.class);
    intent.putExtra(LocationService.PrivateIntent.Transition.EXTRA_NEW_STATE,
        State.OUTDOOR.toString());
    connection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        // We've bound to LocationService, cast the IBinder and get LocationService instance.
        bound = true;
        mapFragment.getMap().setLocationSource(
            ((LocalBinder) service).getService().getBestLocationProvider());
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        bound = false;
      }
    };
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    // Register receivers.
    newLocationReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        double lat =
            intent.getDoubleExtra(LocationService.PrivateIntent.NewLocation.EXTRA_LATITUDE, 0);
        double lon =
            intent.getDoubleExtra(LocationService.PrivateIntent.NewLocation.EXTRA_LONGITUDE, 0);
        mapFragment.getMap().animateCamera(
            CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), DEFAULT_ZOOM_LEVEL));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(newLocationReceiver,
        LocationService.PrivateIntent.NewLocation.newIntentFilter());

    newSnrReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        float snr = intent.getFloatExtra(LocationService.PrivateIntent.NewSnr.EXTRA_SNR, 0);
        ((TextView) OutdoorActivity.this.findViewById(R.id.bottom)).setText("SNR (3 premiers): "
            + Float.toString(snr));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(newSnrReceiver,
        LocationService.PrivateIntent.NewSnr.newIntentFilter());

    transitionReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        State newState =
            State.valueOf(intent
                .getStringExtra(LocationService.PrivateIntent.Transition.EXTRA_NEW_STATE));
        switch (newState) {
          case INDOOR:
            Intent newIntent = new Intent(OutdoorActivity.this, IndoorActivity.class);
            startActivity(newIntent);
            break;
          case OUTDOOR:
            break;
          default:
            throw new IllegalStateException("Unhandled Application State.");
        }
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(transitionReceiver,
        LocationService.PrivateIntent.Transition.newIntentFilter());

    // TODO: remove dataBase stuff.
    SQLiteDatabase db = new InsDbHelper(this).getReadableDatabase();
    Cursor c =
        db.rawQuery(
            "SELECT * FROM Building b INNER JOIN EntryPoint ep ON b.idBuilding = ep.Building_idBuilding",
            null);
    while (c.moveToNext()) {
      double latitude =
          c.getDouble(c.getColumnIndexOrThrow(InsContract.EntryPoint.COLUMN_NAME_LATITUDE));
      double longitude =
          c.getDouble(c.getColumnIndexOrThrow(InsContract.EntryPoint.COLUMN_NAME_LONGITUDE));
      StringBuilder sb = new StringBuilder();
      sb.append("Entry Point at (").append(latitude).append(", ").append(longitude).append(")");
      Log.d("MainActivity", sb.toString());
    }
    db.close();
  }

  @Override
  protected void onStop() {
    // Disconnect from the Location Service.
    if (bound) {
      unbindService(connection);
      bound = false;
    }

    // Unregister receivers.
    LocalBroadcastManager.getInstance(this).unregisterReceiver(newLocationReceiver);
    newLocationReceiver = null;
    LocalBroadcastManager.getInstance(this).unregisterReceiver(newSnrReceiver);
    newSnrReceiver = null;
    LocalBroadcastManager.getInstance(this).unregisterReceiver(transitionReceiver);
    transitionReceiver = null;
    connection = null;

    super.onStop();
  }
}
