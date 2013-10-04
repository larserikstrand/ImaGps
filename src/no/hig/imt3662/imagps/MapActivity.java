package no.hig.imt3662.imagps;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Class that displays the map and handles interactions with the map.
 * @author Lars Erik, Amund Sørumshagen
 *
 */
public class MapActivity extends Activity {	
	private GoogleMap mMap;
	private DatabaseHandler mDbHandler;
	
	// Size of image thumbnail to be used in infowindow.
	public static final int THUMBNAIL_SIZE = 100;
	public static final String EXTRA_IMAGEPATH = "no.hig.imt3662.imagps.IMAGEPATH";

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// Show the Up button in the action bar.
		setupActionBar();
		
		setupMap();
		
		mDbHandler = new DatabaseHandler(this);
		mDbHandler.open();
		
		addMapMarkers();
	}
	
	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
	}

	
	
	/**
	 * Allows user to go one step back (up) from this activity.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	/**
	 * Sets up the Google map with listeners and handlers.
	 */
	private void setupMap() {
		mMap = ((MapFragment) getFragmentManager().
				findFragmentById(R.id.map)).getMap();
		
		// Enable button to find the users location.
		mMap.setMyLocationEnabled(true);
		
		// Display an info window when a marker is clicked.
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				return true;
			}
		});
		
		// Sets up the info window.
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			// Do nothing with the layout of the window.
			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}
			
			/*
			 * Set the content of the info window to display the image title
			 * and the corresponding image thumbnail bitmap
			 */
			@Override
			public View getInfoContents(Marker marker) {
				View view = getLayoutInflater().inflate(
						R.layout.info_window_layout, null);
				TextView textView = (TextView) view.findViewById(
						R.id.info_window_title);
				ImageView imageView = (ImageView) view.findViewById(
						R.id.info_window_thumbnail);
				
				textView.setText(marker.getTitle());
						
				imageView.setImageBitmap(
						Utility.decodeSampledBitmapFromFile(
								new File(marker.getSnippet()),
								THUMBNAIL_SIZE, THUMBNAIL_SIZE));
				
				return view;
			}
		});
		
		// Start activity to view the image in the info window when clicked.
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				Intent intent = new Intent(MapActivity.this,
						ViewImageActivity.class);
				intent.putExtra(EXTRA_IMAGEPATH, marker.getSnippet());
				startActivity(intent);
			}
		});
	}
	
	

	/**
	 * Gets locations of images from the SQLite database and 
	 * displays them on the map as markers.
	 */
	private void addMapMarkers() {
		Cursor c = mDbHandler.fetchEntries();
		if(c.moveToFirst()) {
			do {
				// Get the path.
				String uri = c.getString(1);
				String title = uri.substring(uri.indexOf(
						MainActivity.IMAGE_FILE_PREFIX));
				/*
				 * Add the marker to the map, holding the image data.
				 * Only the position is directly shown on the map.
				 * The rest of the data is used by the info window listener.
				 */
				mMap.addMarker(new MarkerOptions()
					.position(new LatLng(
							Double.parseDouble(c.getString(2)),
							Double.parseDouble(c.getString(3))))
							.title(title)
							.snippet(uri)
				);
			} while (c.moveToNext());
		}
		c.close();
	}
	
	
	
}
