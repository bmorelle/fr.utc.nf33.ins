package fr.utc.nf33.ins;


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
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

import fr.utc.nf33.ins.LocationUpdater.LocalBinder;
import fr.utc.nf33.ins.db.InsContract;
import fr.utc.nf33.ins.db.InsDbHelper;

public final class MainActivity extends FragmentActivity
    implements
      GpsDialogFragment.GpsDialogListener {
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
  
  private LocationManager locationManager;

  private SupportMapFragment mapFragment;
  
  private LocationUpdater mService;
  
  private boolean mBound = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Create a Google Map Fragment with desired options.
    mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
    fragmentTransaction.commit();
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onGpsDialogCancel(DialogFragment dialog) {
    finish();
  }

  @Override
  public void onGpsDialogPositiveClick(DialogFragment dialog) {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Get the Location Manager.
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    // Check whether the GPS provider is enabled.
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      DialogFragment dialog = new GpsDialogFragment();
      dialog.show(getSupportFragmentManager(), "GpsDialogFragment");
    }
    
    // Bind to the Service
    Intent intent = new Intent(this, LocationUpdater.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    
    if(mService == null) {
      Log.d("Service", "NULL");
    }
    
    // Setup the map.
    GoogleMap map = mapFragment.getMap();
    map.setMyLocationEnabled(true);

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
    super.onStop();
    
    if (mBound) {
      unbindService(mConnection);
      mBound = false;
    }
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName className,
              IBinder service) {
          // We've bound to LocationUpdater, cast the IBinder and get LocationUpdater instance
          LocalBinder binder = (LocalBinder) service;
          mService = binder.getService();
          mBound = true;
          mapFragment.getMap().setLocationSource(mService.getBestLocationProvider());
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
          mBound = false;
      }
  };
}