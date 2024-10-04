import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data_access.DataAccess;
import domain.Ride;
import testOperations.TestDataAccess;

public class WhiteTestDB {
	// sut:system under test
	static DataAccess sut = new DataAccess();

	// additional operations needed to execute the test
	static TestDataAccess testDA = new TestDataAccess();

	private Ride ride;

	@Before
	public void setUp() {
		// Initialize the ride before each test
		testDA.open();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		try {
			rideDate = sdf.parse("05/08/2025");
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}

		// Create the driver and the ride
		ride = testDA.addRideWithDriver("Esti", "1234", "Iruña", "Donosti", rideDate, 2, 7);
		testDA.addTraveler("Ane02", "1234", 100);
		testDA.addTraveler("Maddi05", "1234", 4);
		testDA.close();
	}

	@Test
	// sut.bookRide: Trigger an exception (in this case by passing a null ride).
	// The test will return false and throw a NullPointerException.
	public void test1() {
	    String username = "Ane02";
	    Ride ride2 = null; // This will cause a NullPointerException
	    int seats = 1;
	    double desk = 2.5;

	    try {
	        sut.open();
	        boolean result = sut.bookRide(username, ride2, seats, desk);
	        assertFalse(result); 
	    } catch (Exception e) {
	        fail("An unexpected exception occurred: " + e.getMessage());
	    } finally {
	        sut.close();
	    }
	}


	@Test
	// sut.bookRide: The Traveler("Mikel21") does not exist in the DB. The test must
	// return false
	// The test supposes that the traveler "Mikel21" does not exist in the DB
	public void test2() {

		String username = "Mikel21";
		int seats = 2;
		double desk = 2.5;
		try {
			sut.open();
			boolean result = sut.bookRide(username, ride, seats, desk);
			assertFalse(result);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage()); 
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The amount of seats entered is bigger than the seats left to
	// book on the ride. The test must return false
	public void test3() {

		String username = "Ane02";
		int seats = 3;
		double desk = 2.5;

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride, seats, desk);

			assertFalse(result);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage()); 
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The amount of of money the travaler has isn't enough to pay for
	// the ride. The test must return false
	public void test4() {
		// The traveler "Maddi05" has only 4 euros
		String username = "Maddi05";

		int seats = 3;
		double desk = 2.5;

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride, seats, desk);

			assertFalse(result);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage()); 
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The Traveler exists in the DB, the number of seats is valid and
	// the traveler has enough money to pay.
	// The test must return true.
	public void test5() {

		String username = "Ane02";
		int seats = 1;
		double desk = 2.5;

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride, seats, desk);

			assertTrue(result);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage()); 
		} finally {
			sut.close();
		}

	}

	@After
	public void tearDown() {
		// Clean up any data or reset the state if needed
		testDA.open();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		try {
			rideDate = sdf.parse("05/08/2025");
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}
		testDA.removeRide("Esti", "Iruña", "Donosti", rideDate);
		testDA.removeDriver("Esti");
		testDA.removeTraveler("Ane02");
		testDA.removeTraveler("Maddi05");
		testDA.close();
	}
}
