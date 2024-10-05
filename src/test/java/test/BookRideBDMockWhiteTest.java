package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import testOperations.TestDataAccess;

public class BookRideBDMockWhiteTest {
static DataAccess sut;
	
	protected MockedStatic<Persistence> persistenceMock;

	@Mock
	protected  EntityManagerFactory entityManagerFactory;
	@Mock
	protected  EntityManager db;
	@Mock
    protected  EntityTransaction  et;

	// additional operations needed to execute the test
	static TestDataAccess testDA = new TestDataAccess();

	private Ride ride;
	private Date rideDate;
	private String username;
	private Traveler traveler1;
	

	@Before
    public  void init() {
        MockitoAnnotations.openMocks(this);
        persistenceMock = Mockito.mockStatic(Persistence.class);
		persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
        .thenReturn(entityManagerFactory);
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
		Mockito.doReturn(et).when(db).getTransaction();
		TypedQuery<Traveler> mockedQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockedQuery);
        
	    sut=new DataAccess(db);
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
		
	}

	@Test
	// sut.bookRide: Trigger an exception (in this case by passing a null ride).
	// The test will return false and throw a NullPointerException.
	public void test1() {
		username = "Ane02";
		ride = null; // This will cause a NullPointerException
		int seats = 1;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100); 
        
        Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
        TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1)); 

		try {
			Mockito.when(db.find(Ride.class, null)).thenReturn(null);
			
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride, seats, desk);
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
	// sut.bookRide: The Traveler("Mikel21") does not exist in the DB. The test must
	// return false
	// The test supposes that the traveler "Mikel21" does not exist in the DB
	public void test2() {

		username = "Mikel21";
		int seats = 2;
		double desk = 2.5;
        Driver driver1 = new Driver("Esti", "1234"); 
        ride = new Ride("Iruña", "Donosti", rideDate, 2, 7, driver1);
        
        Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
        Mockito.when(db.find(Ride.class, ride.getRideNumber())).thenReturn(ride);
        TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));  
		try {
			sut.open();

			// check that the traveler is not in the DB
			assertNull(sut.getTraveler("Mikel21"));

			boolean result = sut.bookRide(username, ride, seats, desk);
			assertFalse(result);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride.getnPlaces());

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

		username = "Ane02";
		int seats = 3;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100); 
        Driver driver1 = new Driver("Esti", "1234"); 
        ride = new Ride("Iruña", "Donosti", rideDate, 2, 7, driver1);
        
        Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
        Mockito.when(db.find(Ride.class, ride.getRideNumber())).thenReturn(ride);
        TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));  

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(100, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride.getnPlaces());

		} catch (Exception e) {
			fail("An unexpected exception occurred: " + e.getMessage());
		} finally {
			sut.close();
		}

	}

	@Test
	// sut.bookRide: The amount of of money the traveler has isn't enough to pay for
	// the ride. The test must return false
	public void test4() {
		// The traveler "Maddi05" has only 4 euros
		username = "Maddi05";
		int seats = 1;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(4); 
        Driver driver1 = new Driver("Esti", "1234"); 
        ride = new Ride("Iruña", "Donosti", rideDate, 2, 7, driver1);
        
        Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
        Mockito.when(db.find(Ride.class, ride.getRideNumber())).thenReturn(ride);
        TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));  

		try {
			sut.open();
			int numBookingsBefore = sut.getTraveler(username).getBookedRides().size();
			boolean result = sut.bookRide(username, ride, seats, desk);
			assertFalse(result);

			Traveler traveler = sut.getTraveler(username);

			// Check that the traveler's balance hasn't changed
			assertEquals(4, traveler.getMoney(), 0.01);

			// Check that no new booking was added
			assertEquals(traveler.getBookedRides().size(), numBookingsBefore);

			// Check that the number of seats in the ride hasn't changed
			assertEquals(2, ride.getnPlaces());

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

		username = "Ane02";
		int seats = 1;
		double desk = 2.5;
		traveler1 = new Traveler(username, "1234");
		traveler1.setMoney(100); 
        Driver driver1 = new Driver("Esti", "1234"); 
        ride = new Ride("Iruña", "Donosti", rideDate, 2, 7, driver1);
        
        Mockito.when(db.find(Traveler.class, username)).thenReturn(traveler1);
        Mockito.when(db.find(Ride.class, ride.getRideNumber())).thenReturn(ride);
        TypedQuery<Traveler> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(traveler1));   
		

		try {
			
			sut.open();
			boolean result = sut.bookRide(username, ride, seats, desk);
			// check if the booking was successful
			assertTrue(result);
			
			Traveler traveler = sut.getTraveler("Ane02");
			double expectedBalance = 100 - ((ride.getPrice() - desk) * seats);

			// check if the traveler money has been correctly updated
			assertEquals(expectedBalance, traveler.getMoney(), 0.01);

			// check if the number of 	seats of the ride is reduced
			assertEquals(1, ride.getnPlaces());
			
			// check if the booking has been done
			assertEquals(1, traveler.getBookedRides().size(), 0.01);
			


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
