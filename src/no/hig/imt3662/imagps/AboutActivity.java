package no.hig.imt3662.imagps;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;


/**
 * Class that displays general information about the application.
 * @author Lars Erik
 *
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set the title of the activity (concatenation of two strings).
		TextView textView = (TextView) findViewById(R.id.about_title);
		textView.setText(this.getString(R.string.about) + " "
				+ this.getString(R.string.app_name));
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

}
