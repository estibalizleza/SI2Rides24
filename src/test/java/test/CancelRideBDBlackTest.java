package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data_access.DataAccess;
import domain.Booking;
import domain.Driver;
import domain.Movement;
import domain.Ride;
import domain.Traveler;
import testOperations.TestDataAccess;

public class CancelRideBDBlackTest {
	// sut:system under test
	static DataAccess sut = new DataAccess();

	// additional operations needed to execute the test
	static TestDataAccess testDA = new TestDataAccess();

	private Ride ride1;
	private Ride ride2;
	private Ride ride3;
	private Ride ride4;
	private Ride ride5;
	private Ride ride6;

	private Traveler traveler1;
	private Traveler traveler2;
	private Traveler traveler6;

	@Before
	public void setUp() {

		Driver driver = new Driver("Jon", "1234");
		// Initialize Rides, Travelers, and Bookings for testing
		traveler1 = new Traveler("Traveler1", "password");
		traveler1.setIzoztatutakoDirua(10); // Frozen money
		traveler1.setMoney(0); // Initially 0 money

		traveler2 = new Traveler("Maddi05", "1234");
		traveler2.setMoney(5); // Traveler with 5 money

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		testDA.open();
		testDA.removeTraveler("Naroa");
		testDA.removeRide("Jon", "Gasteiz", "Bilbo", rideDate);
		testDA.close();

		// Test Case 1: Ride with accepted and not defined bookings
		ride1 = new Ride("Donostia", "Bilbo", rideDate, 3, 5, driver);
		ride1.setActive(true);
		Booking booking1 = new Booking(ride1, traveler1, 1); // Booking with status Accepted
		booking1.setStatus("Accepted");
		Booking booking2 = new Booking(ride1, traveler1, 1); // Booking with status NotDefined
		booking2.setStatus("NotDefined");
		Booking booking3 = new Booking(ride1, traveler1, 1); // Booking with status NotDefined
		booking3.setStatus("Rejected");
		ride1.setBookings(Arrays.asList(booking3));

		// Test Case 2: Another Ride with multiple bookings
		ride2 = new Ride("Madrid", "Bilbo", rideDate, 3, 5, driver);
		ride2.setActive(true);
		ride2.setBookings(Arrays.asList(booking1));

		// Test Case 3: Ride with no bookings
		ride3 = new Ride("Barakaldo", "Bilbo", rideDate, 3, 5, driver);
		ride3.setBookings(Collections.emptyList());

		// Test Case 4: Ride that is not in the DB
		ride4 = new Ride("Gasteiz", "Bilbo", rideDate, 3, 5, driver);

		// Test Case 5: Simulating a DB connection failure for this ride
		ride5 = new Ride("Gasteiz", "Bilbo", rideDate, 3, 5, driver);
		Booking booking5 = new Booking(ride5, null, 1); // Booking with status Accepted
		booking5.setStatus("Accepted");

		ride6 = new Ride("Donostia", "Bilbo", rideDate, 3, 15, driver);
		ride6.setActive(true);
		traveler6 = new Traveler("Naroa", "1233");
		traveler6.setIzoztatutakoDirua(5);
		traveler6.setMoney(0);
		Booking booking6 = new Booking(ride6, traveler6, 1); // Booking with status Accepted
		booking6.setStatus("Accepted");
		ride6.setBookings(Arrays.asList(booking6));

		testDA.open();
		testDA.cancelRide(ride1);
		testDA.cancelRide(ride2);
		testDA.cancelRide(ride3);
		testDA.cancelRide(ride5);
		testDA.cancelRide(ride6);
		testDA.close();

	}

	@Test
	public void test1() { // normala
		sut.open();
		sut.cancelRide(ride2); // booking-a
		sut.close();
		assertFalse(ride2.isActive());
		assertEquals(5, traveler1.getIzoztatutakoDirua(), 0.0);
		assertEquals(5.0, traveler1.getMoney(), 0.0);
		sut.open();
		List<Movement> movements = sut.getAllMovements(traveler1);
		Movement get = movements.get(movements.size() - 1);
		assertEquals("BookDeny", get.getEragiketa());
		sut.close();

	}

	@Test
	public void test2() { // ride null
		sut.open();

		try {
			// Act: Pass null to cancelRide, it should not throw an exception
			sut.cancelRide(null);
			sut.close();
		} catch (NullPointerException e) {
			fail();
		} catch (Exception e) {
			fail();
		}

	}

	@Test
	public void test3() { // setbookings empty
		sut.open();
		sut.cancelRide(ride3);
		sut.close();
		assertFalse(ride3.isActive());
	}

	@Test
	public void test4() { // traveler null
		sut.open();
		sut.cancelRide(ride5);
		testDA.open();
		assertFalse(ride5.isActive());
		testDA.close();

	}

	@Test
	public void test5() { // izoztutako dirua
		sut.open();
		sut.cancelRide(ride6); // booking-a
		sut.close();
		assertFalse(ride6.isActive());
		System.out.println("DIRUA:" + traveler6.getIzoztatutakoDirua());
		System.out.println("DIRUA:" + traveler6.getMoney());
		assertFalse(traveler6.getIzoztatutakoDirua() < 0);
		sut.open();
		List<Movement> movements = sut.getAllMovements(traveler1);
		Movement get = movements.get(movements.size() - 1);
		assertEquals("BookDeny", get.getEragiketa());
		sut.close();
	}

	@Test
	public void test6() { // setbookings null
		sut.open();
		sut.cancelRide(ride4);
		sut.close();
		assertFalse(ride4.isActive());
	}

	@After
	public void after() {
		testDA.open();
		testDA.removeJon();
		testDA.removeTraveler("Traveler1");
		testDA.removeTraveler("Maddi05");
		testDA.removeTraveler("Naroa");
		testDA.close();
		List<Ride> rides = new ArrayList<>();
		rides.add(ride1);
		rides.add(ride2);
		rides.add(ride3);
		rides.add(ride4);
		rides.add(ride5);

	}

}
