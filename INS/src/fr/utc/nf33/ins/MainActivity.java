package fr.utc.nf33.ins;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity {

	private SupportMapFragment mapFragment;
	private GoogleMap map;

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
