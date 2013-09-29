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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class MainActivity extends Activity implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private final static int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private Uri mImageUri;
	private DatabaseHandler mDbHandler;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	
	public static final int ACTION_IMAGE_CAPTURE = 1;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupUI();
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
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	private void setupUI() {
		Button button = (Button) findViewById(R.id.button_camera);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleCameraIntent();
			}
		});
		
		button = (Button) findViewById(R.id.button_map);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleMapIntent();
			}
		});
	}
	
	
	
	private void handleCameraIntent() {
		if (isIntentavailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			mImageUri = getOutputImageFileUri();
			mCurrentLocation = mLocationClient.getLastLocation();
			
			if (mImageUri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);	
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		} else {
			
		}
	}
	
	
	
	private void handleMapIntent() {
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
	
	
	
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
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				handleCameraResponse();
			} else if (resultCode == RESULT_CANCELED) {
				return;
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
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES), "ImaGps");
		
		if (! mediaStorageDir.exists()) {
			if (! mediaStorageDir.mkdirs()) {
				Log.d("ImaGps", "failed to create directory");
				return null;
			}
		}
		
		String timeStamp = new SimpleDateFormat(
				"yyyyMMdd_HHmmss").format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		
		return Uri.fromFile(mediaFile);
	}

	
	
	private void handleCameraResponse() {
		Toast.makeText(this, this.getString(R.string.image_saved) + ":\n" +
				mImageUri.getPath(), Toast.LENGTH_LONG).show();
		
		if (mCurrentLocation != null) {
			mDbHandler.createEntry(mImageUri.getPath(),
					mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude());
		} else {
			Toast.makeText(this, this.getString(R.string.image_saved) + ":\n" +
					mImageUri.getPath(), Toast.LENGTH_LONG).show();
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

	

	@Override
	public void onConnected(Bundle bundle) {
		
	}

	

	@Override
	public void onDisconnected() {
		
	}
	
	

}
