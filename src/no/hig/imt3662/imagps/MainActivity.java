package no.hig.imt3662.imagps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * The starting screen of the application. From here, the user can choose
 * to take a photo or view locations of photos on map.
 * @author Lars Erik Strand, Amund Sørumshagen, Olav Brenna Hansen
 *
 */
public class MainActivity extends Activity implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	// Used with the Google Location Client API.
	private static final int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int ABOUT_ID = Menu.FIRST;
	
	private Uri mImageUri;
	private DatabaseHandler mDbHandler;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	
	public static final int ACTION_IMAGE_CAPTURE = 1;
	public static final String IMAGE_FILE_PREFIX = "IMG_";

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupUI();
		// Instantiate the handler to the SQLite Database
		mDbHandler = new DatabaseHandler(this);
		mDbHandler.open();
		
		mLocationClient = new LocationClient(this, this, this);
	}

	
	
	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}
	
	
	
	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}
	
	
	
	/**
	 * Adds About button to the options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, 0, R.string.about);
		return true;
	}

	
	/**
	 * Launch about activity when selected in the options menu
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ABOUT_ID:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	

	/**
	 * Sets up the UI with event handlers.
	 */
	private void setupUI() {
		// Set up the camera button.
		Button button = (Button) findViewById(R.id.button_camera);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleCameraIntent();
			}
		});
		
		// Set up the map button.
		button = (Button) findViewById(R.id.button_map);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleMapIntent();
			}
		});
	}
	
	
	
	/**
	 * Checks to see if a native camera application is available, and fires
	 * that intent.
	 */
	private void handleCameraIntent() {
		if (isIntentavailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			// Get the destination for the camera to store the image.
			mImageUri = getOutputImageFileUri();
			// Get the current location to associate with the image.
			mCurrentLocation = mLocationClient.getLastLocation();

			// Check if image storage destination exists.
			if (mImageUri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);	
				startActivityForResult(intent,
						CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
			// Tell the user that camera is not available
		} else {
			Toast.makeText(this, this.getString(R.string.camera_unavailable),
					Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	private void handleMapIntent() {
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
	
	
	
	/**
	 * Checks to see if an Intent is available on the device.
	 * @param context Application context.
	 * @param action The intent action to be checked.
	 * @return true if the intent is available.
	 */
	public static boolean isIntentavailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	
	
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Check to see if this was fired by the camera.
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				handleCameraResponse();
			// User canceled the activity
			} else if (resultCode == RESULT_CANCELED) {
				return;
			// Something went wrong using the camera. Notify user
			} else {
				Toast.makeText(this,
						this.getString(R.string.camera_return_error),
						Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
	/**
	 * Returns a Uri. SimpleDateFormat is used to because the image path needs
	 * to be parsed programmatically. 
	 * @return Uri of where to store the image
	 */
	@SuppressLint("SimpleDateFormat")
	private static Uri getOutputImageFileUri() {
		//  Get the system default image storage location. 
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES), "ImaGps");
		
		if (! mediaStorageDir.exists()) {
			// Create directory if it does not exist
			if (! mediaStorageDir.mkdirs()) {
				Log.d("ImaGps", "failed to create directory");
				return null;
			}
		}
		
		String timeStamp = new SimpleDateFormat(
				"yyyyMMdd_HHmmss").format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					IMAGE_FILE_PREFIX + timeStamp + ".jpg");
		
		return Uri.fromFile(mediaFile);
	}

	
	
	/**
	 * Stores data of the image taken in database if the camera returned ok.
	 */
	private void handleCameraResponse() {
		// Show the user that the image is stored and where it was stored.
		Toast.makeText(this, this.getString(R.string.image_saved) + ":\n" +
				mImageUri.getPath(), Toast.LENGTH_LONG).show();
		
		/*
		 * Check to see if the location exists in case the location client
		 * could not connect
		 */
		if (mCurrentLocation != null) {
			mDbHandler.createEntry(mImageUri.getPath(),
					mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude());
		} else {
			Toast.makeText(this, this.getString(R.string.location_unavailable),
					Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey("URI")) {
			mImageUri = savedInstanceState.getParcelable("URI");
		}
		if (savedInstanceState.containsKey("LOC")) {
			mCurrentLocation = savedInstanceState.getParcelable("LOC");
		}
	}

	
	
	/**
	 * Launching camera causes orientation change on some devices.
	 * Key variables must therefore be stored.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mImageUri != null) {
			outState.putParcelable("URI", mImageUri);
		}
		if (mCurrentLocation != null) {
			outState.putParcelable("LOC", mCurrentLocation);
		}
	}


	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, connectionResult.getErrorCode(),
            		Toast.LENGTH_LONG).show();
        }
	}

	
	
	/**
	 * Methods required by the Location Client API.
	 */
	@Override
	public void onConnected(Bundle bundle) {
		
	}

	

	@Override
	public void onDisconnected() {
		
	}
	
	

}
