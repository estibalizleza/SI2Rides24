package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data_access.DataAccess;
import domain.Booking;
import domain.Driver;
import domain.Ride;
import domain.Traveler;
import testOperations.TestDataAccess;

public class GetBookingFromDriverBDBlackTest {

	// sut:system under test
	static DataAccess sut = new DataAccess();

	// additional operations needed to execute the test
	static TestDataAccess testDA = new TestDataAccess();

	private Traveler t1;
	private Traveler t2;
	private Driver u1;
	private Driver u2;
	private Driver u3;
	private Ride bidaia1;
	private Ride bidaia2;
	private Ride bidaia3;
	private Booking booking1;
	private Booking booking2;
	private Booking booking3;
	private Date date1;
	private Date date2;

	@Before
	public void setUp() {
		// Initialize the ride before each test
		testDA.open();

		// Data sortu
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		date1 = null;
		date2 = null;
		try {
			date1 = sdf.parse("23/11/2024");
			date2 = sdf.parse("08/12/2024");
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}

		// Gidaria sortu
		u1 = new Driver("driver1", "123");
		u2 = new Driver("driver2", "321");
		u3 = new Driver("driver3", "231");

		// Bidaia sortu gidariari lotuta
		bidaia1 = new Ride("Donosti", "Gasteiz", date1, 4, 3.5, u1);
		bidaia1.setActive(true);

		bidaia2 = new Ride("Donosti", "Bilbo", date1, 4, 3.0, u2);
		bidaia2.setActive(true);

		bidaia3 = new Ride("Gasteiz", "Bilbo", date2, 4, 2.5, u1);
		bidaia3.setActive(true);

		// Bi bidaiari sortu
		t1 = new Traveler("traveler1", "abc");
		t2 = new Traveler("traveler2", "cba");

		// Bidaiari bakoitzarentzat erreserba bat sortu
		booking1 = new Booking(bidaia1, t1, 2);
		booking2 = new Booking(bidaia1, t2, 1);
		booking3 = new Booking(bidaia3, t2, 1);
		booking1.setBookNumber(1);
		booking2.setBookNumber(2);
		booking3.setBookNumber(3);

		// Erreserbak bidaiari lotu
		List<Booking> bookings = new ArrayList<>();
		bookings.add(booking1);
		bookings.add(booking2);
		bidaia1.setBookings(bookings);

		List<Booking> bookings2 = new ArrayList<>();
		bookings2.add(booking3);
		bidaia3.setBookings(bookings2);

		// Bidaia gidariari lotu
		List<Ride> rides = new ArrayList<>();
		rides.add(bidaia1);
		rides.add(bidaia3);
		u1.setCreatedRides(rides);

		List<Ride> rides2 = new ArrayList<>();
		rides2.add(bidaia2);
		u2.setCreatedRides(rides2);

		// Bidaiariei erreserbak lotu
		t1.addBookedRide(booking1);
		t2.addBookedRide(booking2);
		t2.addBookedRide(booking3);

		testDA.addDriver(u1);
		testDA.addDriver(u2);
		testDA.addDriver(u3);
		testDA.addRide(bidaia1);
		testDA.addRide(bidaia2);
		testDA.addRide(bidaia3);
		testDA.addTraveler(t1);
		testDA.addTraveler(t2);
		testDA.addBooking(booking1);
		testDA.addBooking(booking2);
		testDA.addBooking(booking3);

		testDA.close();
	}

	@Test
	// Gidaria hainbat bidai ditu sortuta, eta bidaiak erreserbak dituzte. Bidai
	// guztien erreserbak itzuli
	public void test1() {
		String username = "driver1";
		List<Booking> expected = new ArrayList();
		expected.add(booking1);
		expected.add(booking2);
		expected.add(booking3);
 
		try {
			sut.open();
			List<Booking> emaitza = sut.getBookingFromDriver(username);
			System.out.println("Expected: " + expected);
			System.out.println("Actual: " + emaitza);
			assertEquals(emaitza.size(), expected.size());
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}
	}

	@Test
	// username null bada, null itzuli behar du.
	public void test2() {
		String username = null;

		try {
			sut.open();
			List<Booking> emaitza = sut.getBookingFromDriver(username);
			assertNull(emaitza);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}
	}

	@Test
	// Gidariak ez ditu bidaiak sortuta. Lista hutsa itzuli behar du.
	public void test3() {
		String username = "driver3";

		try {
			sut.open();
			List<Booking> emaitza = sut.getBookingFromDriver(username);
			System.out.println("Actual: " + emaitza);
			assertEquals(emaitza.size(), 0);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}
	}

	@Test
	// Gidariak ez ditu bidai aktiborik. Lista hutsa itzuli behar du.
	public void test4() {
		bidaia1.setActive(false);
		bidaia3.setActive(false);
		
		testDA.open();
		testDA.updateRide(bidaia1);
		testDA.updateRide(bidaia3);
		testDA.close();

		String username = "driver1";
		System.out.println(bidaia1.isActive());

		try {
			sut.open();
			List<Booking> emaitza = sut.getBookingFromDriver(username);
			System.out.println("Actual: " + emaitza);
			assertEquals(emaitza.size(), 0);
		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}
	}
 
	@Test
	// Gidariak bidaiak ditu, baina ez ditu erreserbarik. Lista hutsa itzuli behar
	// du.
	public void test5() {
		String username = "driver2";

		try {
			sut.open();
			List<Booking> emaitza = sut.getBookingFromDriver(username);
			System.out.println("Actual: " + emaitza);
			assertEquals(emaitza.size(), 0);
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
		Date date1 = null;
		try {
			date1 = sdf.parse("23/11/2024");
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}

		testDA.removeRide("driver1", "Donosti", "Gasteiz", date2);
		testDA.removeRide("driver1", "Gasteiz", "Bilbo", date1);
		testDA.removeRide("driver2", "Donosti", "Bilbo", date1);
		testDA.removeDriver("driver1");
		testDA.removeDriver("driver2");
		testDA.removeDriver("driver3");
		testDA.removeTraveler("traveler1");
		testDA.removeTraveler("traveler2");

		testDA.close();
	}
}
