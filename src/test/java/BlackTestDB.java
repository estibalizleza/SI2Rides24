import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data_access.DataAccess;
import domain.Booking;
import domain.Ride;
import domain.Traveler;
import testOperations.TestDataAccess;

public class BlackTestDB {
	// sut:system under test
	static DataAccess sut = new DataAccess();

	// additional operations needed to execute the test
	static TestDataAccess testDA = new TestDataAccess();

	private Ride ride1;

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
		ride1 = testDA.addRideWithDriver("Esti", "1234", "Iruña", "Donosti", rideDate, 2, 7);
		testDA.addTraveler("Ane02", "1234", 100);
		testDA.addTraveler("Maddi05", "1234", 4);
		testDA.close();
	}

	@Test
	// sut.bookRide: Everything is correct. All the parameters are valid and make
	// sense for the method.
	// The test must return true.
	public void test1() {
		String username = "Ane02";
		int seats = 1;
		double desk = 2.5;

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertTrue(result);

			Traveler traveler = sut.getTraveler("Ane02");
			double expectedBalance = 100 - ((ride1.getPrice() - desk) * seats);

			// check if the traveler money has been correctly updated
			assertEquals(expectedBalance, traveler.getMoney(), 0.01);

			// check if the number of seats of the ride is reduced
			assertEquals(ride1.getnPlaces(), 1);

			// check if the booking has been done
			testDA.open();
			Booking booking = testDA.getBooking(ride1, "Ane02");
			testDA.close();
			assertNotNull(booking);

			boolean bookingExists = traveler.getBookedRides().stream()
					.anyMatch(b -> b.getBookNumber() == (booking.getBookNumber()));

			assertTrue(bookingExists);

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}
	}

	@Test
	// sut.bookRide: The username entered is null, therefore, the traveler object
	// created will be null and the method will return false
	// return false
	public void test2() {
		int seats = 2;
		double desk = 2.5;
		try {
			sut.open();
			boolean result = sut.bookRide(null, ride1, seats, desk);
			assertFalse(result);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride1.getnPlaces());

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The ride entered is null
	// The test must return false, and a NullPointerException will be thrown
	public void test3() {

		String username = "Ane02";
		int seats = 3;
		double desk = 2.5;

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, null, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The amount of seats entered is 0, therefore, the method will
	// return false
	// The test must return false
	public void test4() {
		String username = "Ane02";

		int seats = 0;
		double desk = 2.5;

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride1.getnPlaces());

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The seats entered are bigger than the seats left to book on the
	// ride.
	// The test must return false
	public void test5() {
		String username = "Ane02";
		int seats = 5;
		double desk = 2.5;

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride1.getnPlaces());

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The discount entered is negative, therefore, the method will
	// return false
	// The test must return false.
	public void test6() {

		String username = "Ane02";
		int seats = 1;
		double desk = -2.3;

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride1.getnPlaces());

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The discount entered is bigger than the total price of the
	// ride(price per seat * number of seats).
	// The test must return false
	public void test7() {

		String username = "Ane02";
		int seats = 1;
		double desk = 23;

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride1.getnPlaces());

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
