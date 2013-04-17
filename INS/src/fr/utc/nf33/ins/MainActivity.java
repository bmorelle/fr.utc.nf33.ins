package fr.utc.nf33.ins;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity {

	private SupportMapFragment mapFragment;
	private LocationManager locationManager;
	private LocationProvider provider;

	private static GoogleMapOptions GOOGLE_MAP_OPTIONS = (new GoogleMapOptions())
			.compassEnabled(false).mapType(GoogleMap.MAP_TYPE_NORMAL)
			.rotateGesturesEnabled(true).tiltGesturesEnabled(true)
			.zoomControlsEnabled(false).zoomGesturesEnabled(true);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
		fragmentTransaction.commit();
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
		locationManager.addGpsStatusListener(new GpsStatus.Listener() {
			@Override
			public void onGpsStatusChanged(int event) {
				GpsStatus status = locationManager.getGpsStatus(null);
				float avg = 0;
				for (GpsSatellite sat : status.getSatellites())
					avg += sat.getSnr();
				avg /= status.getMaxSatellites();
				((TextView) MainActivity.this.findViewById(R.id.toto)).setText(Float.toString(avg));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
