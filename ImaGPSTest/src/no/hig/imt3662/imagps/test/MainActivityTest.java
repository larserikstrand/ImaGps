package no.hig.imt3662.imagps.test;

import no.hig.imt3662.imagps.MainActivity;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;


/**
 * Test if GUI are set up correctly.
 * 
 * @author Amund
 *
 */
public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	
	private Activity myActivity;
	private Button cameraButton;
	private Button mapButton;


	public MainActivityTest() {
		super(MainActivity.class);
	}
	
	@Override protected void setUp() throws Exception {
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		myActivity = getActivity();
		
		cameraButton = (Button) myActivity.findViewById(no.hig.imt3662.imagps.R.id.button_camera);
		mapButton = (Button) myActivity.findViewById(no.hig.imt3662.imagps.R.id.button_map);
	}
	
	public void testPreConditions() {
		assertNotNull(cameraButton);
		assertNotNull(mapButton);
	}
	
	
	

}
