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

public class GetBookingFromDriverMockWhiteTest {

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
    //Gidaria ez da existitzen. Null itzuli behar du
    public void test1() {
    	
        String username = "nonExistentDriver";

        // mock-a konfiguratu
        when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
        when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
        when(mockDriverQuery.getSingleResult()).thenThrow(new javax.persistence.NoResultException("No driver found"));

        // Metodoa frogatu
        List<Booking> result = sut.getBookingFromDriver(username);

        // Verificamos que el resultado es null
        assertNull(result);
    }
    @Test
    //Gidaria bidai bat dauka erreserbakin. Erreserben lista itzuli behar du.
    public void test2() {
        // gidaria eta bidaia sortu
        String username = "driver1";
        Driver driver = new Driver(username, "password");
        Ride ride = new Ride("CityA", "CityB", null, 4, 2.5, driver);
        ride.setActive(true); 

        // bidaiariak eta erreserbak sortu
        Traveler traveler1 = new Traveler("traveler1", "abc");
        Traveler traveler2 = new Traveler("traveler2", "def");
        Booking booking1 = new Booking(ride, traveler1, 1);
        Booking booking2 = new Booking(ride, traveler2, 1);

        // erreserbak listan
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);
        
 
        when(db.createQuery(anyString(), eq(Driver.class))).thenReturn(mockDriverQuery);
        when(mockDriverQuery.setParameter(eq("username"), any())).thenReturn(mockDriverQuery);
        when(mockDriverQuery.getSingleResult()).thenReturn(driver);


        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        driver.setCreatedRides(rides);
        ride.setBookings(bookings);

        // metodoa aztertu
        List<Booking> result = sut.getBookingFromDriver(username); 

        //emaitza aztertu
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(booking1));
        assertTrue(result.contains(booking2));
    }
    @Test
 // Gidaria bidai bat du sortuta baina ez dauka erreserbarik
 public void test3() {
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
 public void test4() {
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
