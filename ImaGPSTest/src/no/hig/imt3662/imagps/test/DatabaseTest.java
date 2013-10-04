package no.hig.imt3662.imagps.test;

import no.hig.imt3662.imagps.DatabaseHandler;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;


/**
 * Test if you can open database.
 * 
 * @author Amund
 *
 */
public class DatabaseTest extends AndroidTestCase {
	private DatabaseHandler myDbHandler;
	private SQLiteDatabase myDb;
	
	
	public DatabaseTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		myDbHandler = new DatabaseHandler(getContext()).open();
		myDb = myDbHandler.mDbHelper.getReadableDatabase();
	}
	
	public void testDb() {
		assertNotNull(myDb.isOpen());
	}
}
