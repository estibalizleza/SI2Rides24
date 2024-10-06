package test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import data_access.DataAccess;
import domain.Booking;
import domain.Driver;
import domain.Ride;
import domain.Traveler;

public class GetBookingFromDriverMockBlackTest {

	private DataAccess sut;

	@Mock
	private EntityManager db;

	@Mock
	private EntityTransaction transaction;

	@Mock
	private TypedQuery<Driver> mockDriverQuery;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		sut = new DataAccess(db);
		when(db.getTransaction()).thenReturn(transaction);
	}

	@Test
	// Gidaria hainbat bidai ditu sortuta, eta bidaiak erreserbak dituzte. Bidai
	// guztien erreserbak itzuli
	public void test1() {
		// gidaria sortu
		String username = "driver1";
		Driver driver = new Driver(username, "password");

		// Bidaia 1 sortu
		Ride ride1 = new Ride("CityA", "CityB", null, 4, 2.5, driver);
		ride1.setActive(true); // Bidaia aktibo

		// Bidaia 1-erako erreserbak sortu
		Traveler traveler1 = new Traveler("traveler1", "abc");
		Traveler traveler2 = new Traveler("traveler2", "def");
		Booking booking1 = new Booking(ride1, traveler1, 1);
		Booking booking2 = new Booking(ride1, traveler2, 2);

		// Bidaia 1eko erreserbak
		List<Booking> bookings1 = new ArrayList<>();
		bookings1.add(booking1);
		bookings1.add(booking2);
		ride1.setBookings(bookings1);

		// Bidaia 2 sortu
		Ride ride2 = new Ride("CityC", "CityD", null, 4, 3.0, driver);
		ride2.setActive(true); // Bidaia aktibo

		// Bidaia 2-rako erreserbak sortu
		Traveler traveler3 = new Traveler("traveler3", "ghi");
		Booking booking3 = new Booking(ride2, traveler3, 1);

		// Bidaia 2ko erreserbak
		List<Booking> bookings2 = new ArrayList<>();
		bookings2.add(booking3);
		ride2.setBookings(bookings2);

		// Bidaia gidariari lotu
		List<Ride> rides = new ArrayList<>();
		rides.add(ride1);
		rides.add(ride2);
		driver.setCreatedRides(rides); // Atibatu bidaia gidariari

		// Mockear consulta
		when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
		when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
		when(mockDriverQuery.getSingleResult()).thenReturn(driver); // Return the driver

		// Metodoa frogatu
		List<Booking> result = sut.getBookingFromDriver(username);

		// Emaitza aztertu
		assertNotNull(result); // Emaitza ez da null
		assertEquals(3, result.size()); // Hiru erreserba espero ditugu
		assertTrue(result.contains(booking1)); // Erreserba 1 egon behar da
		assertTrue(result.contains(booking2)); // Erreserba 2 egon behar da
		assertTrue(result.contains(booking3)); // Erreserba 3 egon behar da
	}

	@Test
	// Username null bada, null itzuli behar du.
	public void test2() {
		// Metodoa frogatu
		List<Booking> result = sut.getBookingFromDriver(null);

		// Emaitza aztertu
		assertNull(result); // Emaitza null izan behar da
	}

	@Test
	// Gidariak ez ditu bidaiak sortuta. Lista hutsa itzuli behar du.
	public void test3() {
		// gidaria sortu
		String username = "driver1";
		Driver driver = new Driver(username, "password");

		// Bidaia gidariari lotu (ez da inolako bidaia sortuko)
		List<Ride> rides = new ArrayList<>();
		driver.setCreatedRides(rides); // Atibatu lista hutsa gidariari

		// Mockear consulta
		when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
		when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
		when(mockDriverQuery.getSingleResult()).thenReturn(driver); // Retornar el conductor

		// Metodoa frogatu
		List<Booking> result = sut.getBookingFromDriver(username);

		// Emaitza aztertu
		assertNotNull(result); // Emaitza ez da null
		assertTrue(result.isEmpty()); // Emaitza hutsa, ez dago erreserbarik
	}

	@Test
	// Gidaria bidai bat du sortuta baina ez dauka erreserbarik
	public void test4() {
		// gidaria sortu
		String username = "driver1";
		Driver driver = new Driver(username, "password");

		// bidaia sortu
		Ride ride = new Ride("CityA", "CityB", null, 4, 2.5, driver);
		ride.setActive(true);

		// Bidaia gidariari lotu
		List<Ride> rides = new ArrayList<>();
		rides.add(ride);
		driver.setCreatedRides(rides);
		// erreserbak listan
		List<Booking> bookings = new ArrayList<>();
		ride.setBookings(bookings); // Bidaiari lotu erreserbak

		// Mockear consulta
		when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
		when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
		when(mockDriverQuery.getSingleResult()).thenReturn(driver);

		// Metodoa frogatu
		List<Booking> result = sut.getBookingFromDriver(username);

		// Emaitza aztertu
		assertNotNull(result); // Emaitza ez da null
		assertEquals(0, result.size()); // Emaitza hutsa, ez dago erreserbarik
	}

	@Test
	// Gidaria bidai bat du sortuta errreserbekin, baina bidaia ez dago aktibo
	public void test5() {
		// gidaria sortu
		String username = "driver1";
		Driver driver = new Driver(username, "password");

		// bidaia sortu
		Ride ride = new Ride("CityA", "CityB", null, 4, 2.5, driver);
		ride.setActive(false); // Bidaiaren egoera ez aktibo

		// bidaiariak eta erreserbak sortu
		Traveler traveler1 = new Traveler("traveler1", "abc");
		Traveler traveler2 = new Traveler("traveler2", "def");
		Booking booking1 = new Booking(ride, traveler1, 1);
		Booking booking2 = new Booking(ride, traveler2, 1);

		// erreserbak listan
		List<Booking> bookings = new ArrayList<>();
		bookings.add(booking1);
		bookings.add(booking2);
		ride.setBookings(bookings); // Bidaiari lotu erreserbak

		// Bidaia gidariari lotu
		List<Ride> rides = new ArrayList<>();
		rides.add(ride);
		driver.setCreatedRides(rides); // Gidariaren bidaiak lotu

		// Kontsulta mockeatu
		when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
		when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
		when(mockDriverQuery.getSingleResult()).thenReturn(driver);

		// Metodoa frogatu
		List<Booking> result = sut.getBookingFromDriver(username);

		// emaitza aztertu
		assertNotNull(result);
		assertEquals(0, result.size()); // Emaitza hutsa, ez dago erreserbarik
	}
}
