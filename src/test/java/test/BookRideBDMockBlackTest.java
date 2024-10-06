package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import data_access.DataAccess;
import domain.Driver;
import domain.Ride;
import domain.Traveler;

public class BookRideBDMockBlackTest {
	static DataAccess sut = new DataAccess();

	protected MockedStatic<Persistence> persistenceMock;

	@Mock
	protected EntityManagerFactory entityManagerFactory;
	@Mock
	protected EntityManager db;
	@Mock
	protected EntityTransaction et;

	private Ride ride1;
	private Traveler traveler1;
	private Date rideDate;
	private String username;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
		persistenceMock = Mockito.mockStatic(Persistence.class);
		persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
				.thenReturn(entityManagerFactory);

		Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
		Mockito.doReturn(et).when(db).getTransaction();
		TypedQuery<Traveler> mockedQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockedQuery);

		sut = new DataAccess(db);
	}

	@Before
	public void setUp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		rideDate = null;
		try {
			rideDate = sdf.parse("05/08/2025");
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}

		Driver driver1 = new Driver("Esti", "1234");
		ride1 = new Ride("Iruña", "Donosti", rideDate, 2, 7, driver1);

	}

	@Test
	// sut.bookRide: Everything is correct. All the parameters are valid and make
	// sense for the method.
	// The test must return true.
	public void test1() {
		username = "Ane02";
		int seats = 1;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride1, seats, desk);
			assertTrue(result);

			Traveler traveler = sut.getTraveler("Ane02");
			double expectedBalance = 100 - ((ride1.getPrice() - desk) * seats);

			// check if the traveler money has been correctly updated
			assertEquals(expectedBalance, traveler.getMoney(), 0.01);

			// check if the number of seats of the ride is reduced
			assertEquals(1, ride1.getnPlaces());

			// check if the booking has been done
			assertEquals(1, traveler.getBookedRides().size(), 0.01);

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
		username = null;

		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

		try {
			sut.open();
			boolean result = sut.bookRide(username, ride1, seats, desk);
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
		username = "Ane02";
		int seats = 3;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

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
		username = "Ane02";
		int seats = 0;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride1, seats, desk);

			Traveler traveler = sut.getTraveler(username);

			assertFalse(result);

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
		username = "Ane02";
		int seats = 5;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

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

		username = "Ane02";
		int seats = 1;
		double desk = -2.3;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

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

		username = "Ane02";
		int seats = 1;
		double desk = 23;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100);

		Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
		Mockito.when(db.find(Ride.class, ride1.getRideNumber())).thenReturn(ride1);
		TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
		Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
		Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));

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

		persistenceMock.close();
	}
}
