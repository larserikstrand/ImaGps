package no.hig.imt3662.imagps;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
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

public class MapActivity extends Activity {	
	private GoogleMap mMap;
	private DatabaseHandler mDbHandler;
	
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
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void setupMap() {
		mMap = ((MapFragment) getFragmentManager().
				findFragmentById(R.id.map)).getMap();
		
		mMap.setMyLocationEnabled(true);
		
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				return true;
			}
		});
		
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}
			
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
						decodeSampledBitmapFromFile(
								new File(marker.getSnippet()),
								THUMBNAIL_SIZE, THUMBNAIL_SIZE));
				
				return view;
			}
		});
		
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
	
	

	private void addMapMarkers() {
		Cursor c = mDbHandler.fetchEntries();
		if(c.moveToFirst()) {
			do {
				String uri = c.getString(1);
				String title = uri.substring(uri.indexOf("IMG_"));
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
	
	
	
	public static Bitmap decodeSampledBitmapFromFile(File file,
			int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// Avoid memory allocation.
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), options);
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		
		Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
		
		try {
            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
            		bitmap.getHeight(), matrix, true);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		return bitmap;
	}
	
	
	
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Height and width of the original image.
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round(
					(float) height / (float) reqHeight);
			final int widthRatio = Math.round(
					(float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		
		return inSampleSize;
	}
	
}
