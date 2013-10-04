package no.hig.imt3662.imagps;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.MenuItem;
import android.widget.ImageView;


/**
 * Class that displays an image whose path is sent via an intent.
 * @author Olav Brenna Hansen
 *
 */
public class ViewImageActivity extends Activity {
	private String mImagePath;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		// Show the Up button in the action bar.
		setupActionBar();
		
		setupUI();
	}

	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
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
	 * Sets up the image
	 */
	@SuppressWarnings("deprecation")
	private void setupUI() {
		Intent intent = getIntent();
		
		mImagePath = intent.getStringExtra(MapActivity.EXTRA_IMAGEPATH);

		ImageView imageView = (ImageView) findViewById(R.id.view_image);
		
		// Get the dimensions of the device screen to use as image dimensions.
		Display display = getWindowManager().getDefaultDisplay();
		
		int width = display.getWidth();
		int height = display.getHeight();
		
		// Retrieve bitmap of image and assign it to the view.
		imageView.setImageBitmap(
				Utility.decodeSampledBitmapFromFile(
						new File(mImagePath), width, height));
	}
	
	
	
	/**
	 * Handle orientation change.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey("PATH")) {
			mImagePath = savedInstanceState.getString("PATH");
		}
	}

	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!mImagePath.isEmpty()) {
			outState.putString("PATH", mImagePath);
		}
	}
	
	
}
