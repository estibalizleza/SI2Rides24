package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import data_access.DataAccess;
import domain.Booking;
import domain.Driver;
import domain.Ride;
import domain.Traveler;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

public class CancelRideMockWhiteTest {

	static DataAccess sut;

	protected MockedStatic<Persistence> persistenceMock;

	@Mock
	protected EntityManagerFactory entityManagerFactory;
	@Mock
	protected EntityManager db;
	@Mock
	protected EntityTransaction et;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
		persistenceMock = Mockito.mockStatic(Persistence.class);
		persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
				.thenReturn(entityManagerFactory);

		Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
		Mockito.doReturn(et).when(db).getTransaction();
		sut = new DataAccess(db);

	}

	@After
	public void tearDown() {
		persistenceMock.close();
	}

	Driver driver;

	@Test
	// sut.createRide: The Driver is null. The test must return null. If an
	// Exception is returned the createRide method is not well implemented.
	public void test1() {
		try {
			Ride ride1;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date rideDate = null;
			;
			try {
				rideDate = sdf.parse("05/10/2026");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Traveler traveler1 = new Traveler("Traveler1", "password");
			traveler1.setIzoztatutakoDirua(10); // Frozen money
			traveler1.setMoney(0); // Initially 0 money
			ride1 = new Ride("Donostia", "Bilbo", rideDate, 3, 5, driver);
			ride1.setActive(true);
			Booking booking3 = new Booking(ride1, traveler1, 1); // Booking with status NotDefined
			booking3.setStatus("Rejected");
			ride1.setBookings(Arrays.asList(booking3));

			Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
			Mockito.when(db.find(Traveler.class, traveler1.getUsername())).thenReturn(traveler1);

			// invoke System Under Test (sut)
			sut.open();
			sut.cancelRide(ride1);

			// verify the results
			assertFalse(ride1.isActive());
			assertEquals(10, traveler1.getIzoztatutakoDirua(), 0.0); // Izoztutako dirua ez du aldatu behar

		} catch (Exception e) {
			e.toString();
			fail();

		} finally {
			sut.close();
		}

	}

	@Test
	// sut.createRide: The Driver("Driver Test") does not exist in the DB. The test
	// must return null
	// The test supposes that the "Driver Test" does not exist in the DB
	public void test2() {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		;
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Traveler traveler1 = new Traveler("Traveler1", "password");
		traveler1.setIzoztatutakoDirua(10);
		Ride ride2 = new Ride("Madrid", "Bilbo", rideDate, 3, 5, driver);
		ride2.setActive(true);
		Booking booking1 = new Booking(ride2, traveler1, 1); // Booking with status Accepted
		booking1.setStatus("Accepted");
		ride2.setBookings(Arrays.asList(booking1));

		// configure the state through mocks
		Mockito.when(db.find(Ride.class, ride2.getRideNumber())).thenReturn(ride2);
		Mockito.when(db.find(Traveler.class, traveler1.getUsername())).thenReturn(traveler1);
		// Mockito.when(db.find(Movement.class, db).thenReturn(new Movement(traveler1,
		// "BookDeny", 0));

		// invoke System Under Test (sut)
		sut.open();
		sut.cancelRide(ride2); // booking-a
		sut.close();
		assertFalse(ride2.isActive());
		assertEquals(5, traveler1.getIzoztatutakoDirua(), 0.0);
		assertEquals(5.0, traveler1.getMoney(), 0.0);
		sut.open();
		// List<Movement> movements = sut.getAllMovements(traveler1);
		// Movement get = movements.get(movements.size() - 1);
		// assertEquals("BookDeny", get.getEragiketa());
		sut.close();
	}

	@Test
	// sut.createRide: the date of the ride must be later than today. The
	// RideMustBeLaterThanTodayException
	// exception must be thrown.
	public void test3() {
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date rideDate = null;
			;
			try {
				rideDate = sdf.parse("05/10/2018");
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Ride ride3 = new Ride("Barakaldo", "Bilbo", rideDate, 3, 5, driver);
			ride3.setActive(true);
			ride3.setBookings(Collections.emptyList());

			Mockito.when(db.find(Ride.class, ride3.getRideNumber())).thenReturn(ride3);

			// invoke System Under Test (sut)
			sut.open();
			sut.cancelRide(ride3);
			sut.close();

			// verify the results
			assertFalse(ride3.isActive());
		} catch (Exception e) {
			e.toString();
			fail();

		} finally {
			sut.close();
		}

	}

	@Test
	// sut.createRide: The Driver("Driver Test") HAS one ride "from" "to" in that
	// "date".
	public void test4() {

		String driverUsername = "Driver Test";
		String driverPassword = "123";

		String rideFrom = "Donostia";
		String rideTo = "Zarautz";

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		;
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {

			driver = new Driver(driverUsername, driverPassword);
			driver.addRide(rideFrom, rideTo, rideDate, 2, 10);
			// configure the state through mocks
			Mockito.when(db.find(Driver.class, driver.getUsername())).thenReturn(driver);

			// invoke System Under Test (sut)
			sut.open();
			sut.createRide(rideFrom, rideTo, rideDate, 0, 0, driverUsername);
			sut.close();

			fail();

		} catch (RideAlreadyExistException e) {
			// verify the results
			sut.close();
			assertTrue(true);
		} catch (RideMustBeLaterThanTodayException e) {
			fail();
		}
	}

	@Test

	// sut.createRide: The Driver("Driver Test") HAS NOT one ride "from" "to" in
	// that "date".
	// and the Ride must be created in DB
	// The test supposes that the "Driver Test" does not exist in the DB before the
	// test
	public void test5() {
		// define parameters
		String driverUsername = "Driver Test";
		String driverPassword = "123";

		String rideFrom = "Donostia";
		String rideTo = "Zarautz";

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		;
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			// configure the state through mocks
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);

			// invoke System Under Test (sut)
			sut.open();
			Ride ride = sut.createRide(rideFrom, rideTo, rideDate, 0, 0, driverUsername);
			sut.close();
			// verify the results
			assertNotNull(ride);
			assertEquals(ride.getFrom(), rideFrom);
			assertEquals(ride.getTo(), rideTo);
			assertEquals(ride.getDate(), rideDate);

		} catch (RideAlreadyExistException e) {
			// if the program goes to this point fail
			fail();

		} catch (RideMustBeLaterThanTodayException e) {
			// if the program goes to this point fail

			fail();
			// redone state of the system (create object in the database)

		}
	}

}
