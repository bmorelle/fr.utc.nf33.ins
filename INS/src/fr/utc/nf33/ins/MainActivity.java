package fr.utc.nf33.ins;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity {

	private SupportMapFragment mapFragment;
	private LocationManager locationManager;
	private LocationProvider provider;
	private Criteria criteria;

	private static GoogleMapOptions GOOGLE_MAP_OPTIONS = (new GoogleMapOptions())
			.compassEnabled(false).mapType(GoogleMap.MAP_TYPE_NORMAL)
			.rotateGesturesEnabled(true).tiltGesturesEnabled(true)
			.zoomControlsEnabled(false).zoomGesturesEnabled(true);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create a Google map fragment with desired options
		mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
		fragmentTransaction.commit();
		
		// Set up the location manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		String providerName = locationManager.getBestProvider(criteria, true);
		
		if(providerName != null) {
			// There is an available provider
		}
		
//		locationManager.addGpsStatusListener(new GpsStatus.Listener() {
//			@Override
//			public void onGpsStatusChanged(int event) {
//				GpsStatus status = locationManager.getGpsStatus(null);
//				float avg = 0;
//				for (GpsSatellite sat : status.getSatellites())
//					avg += sat.getSnr();
//				avg /= status.getMaxSatellites();
//				((TextView) MainActivity.this.findViewById(R.id.toto)).setText(Float.toString(avg));
//			}
//		});
	}
	
	@Override
	protected void onStart() {
	    super.onStart();

	    // Check if GPS is enabled
	    locationManager =
	            (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

	    if (!gpsEnabled) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(R.string.gps_dialog_content)
	        .setTitle(R.string.gps_dialog_title);
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
}
