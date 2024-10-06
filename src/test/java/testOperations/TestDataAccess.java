package testOperations;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import configuration.ConfigXML;
import domain.Booking;
import domain.Driver;
import domain.Ride;
import domain.Traveler;

public class TestDataAccess {
	protected EntityManager db;
	protected EntityManagerFactory emf;

	ConfigXML c = ConfigXML.getInstance();

	public TestDataAccess() {

		System.out.println("TestDataAccess created");

		// open();

	}

	public void open() {

		String fileName = c.getDbFilename();

		if (c.isDatabaseLocal()) {
			emf = Persistence.createEntityManagerFactory("objectdb:" + fileName);
			db = emf.createEntityManager();
		} else {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("javax.persistence.jdbc.user", c.getUser());
			properties.put("javax.persistence.jdbc.password", c.getPassword());

			emf = Persistence.createEntityManagerFactory(
					"objectdb://" + c.getDatabaseNode() + ":" + c.getDatabasePort() + "/" + fileName, properties);

			db = emf.createEntityManager();
		}
		System.out.println("TestDataAccess opened");

	}

	public void close() {
		db.close();
		System.out.println("TestDataAccess closed");
	}

	public boolean removeDriver(String name) {
		System.out.println(">> TestDataAccess: removeDriver");
		Driver d = db.find(Driver.class, name);
		if (d != null) {
			db.getTransaction().begin();
			db.remove(d);
			db.getTransaction().commit();
			return true;
		} else
			return false;
	}

	public Driver createDriver(String name, String pass) {
		System.out.println(">> TestDataAccess: addDriver");
		Driver driver = null;
		db.getTransaction().begin();
		try {
			driver = new Driver(name, pass);
			db.persist(driver);
			db.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return driver;
	}

	public boolean existDriver(String email) {
		return db.find(Driver.class, email) != null;

	}

	public Driver addDriverWithRide(String name, String from, String to, Date date, int nPlaces, float price) {
		System.out.println(">> TestDataAccess: addDriverWithRide");
		Driver driver = null;
		db.getTransaction().begin();
		try {
			driver = db.find(Driver.class, name);
			if (driver == null) {
				System.out.println("Entra en null");
				driver = new Driver(name, null);
				db.persist(driver);
			}
			driver.addRide(from, to, date, nPlaces, price);
			db.getTransaction().commit();
			System.out.println("Driver created " + driver);

			return driver;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Add a driver and a ride to the driver
	 * 
	 * @param name    the driver name
	 * @param from    the origin
	 * @param to      the destination
	 * @param date    the date
	 * @param nPlaces the number of places
	 * @param price   the price
	 * @return the ride added
	 */
	public Ride addRideWithDriver(String name, String pass, String from, String to, Date date, int nPlaces,
			float price) {
		System.out.println(">> TestDataAccess: addDriverWithRide");
		Driver driver = null;
		db.getTransaction().begin();
		try {
			driver = db.find(Driver.class, name);
			if (driver == null) {
				System.out.println("Entra en null");
				driver = new Driver(name, pass);
				db.persist(driver);
			}
			Ride ride = driver.addRide(from, to, date, nPlaces, price);
			db.getTransaction().commit();
			System.out.println("Driver created " + driver);

			return ride;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean existRide(String name, String from, String to, Date date) {
		System.out.println(">> TestDataAccess: existRide");
		Driver d = db.find(Driver.class, name);
		if (d != null) {
			return d.doesRideExists(from, to, date);
		} else
			return false;
	}

	public Ride removeRide(String name, String from, String to, Date date) {
		System.out.println(">> TestDataAccess: removeRide");
		Driver d = db.find(Driver.class, name);
		if (d != null) {
			db.getTransaction().begin();
			Ride r = d.removeRide(from, to, date);
			db.getTransaction().commit();
			System.out.println("created rides" + d.getCreatedRides());
			return r;

		} else
			return null;

	}

	/**
	 * Add a traveler
	 * 
	 * @param name  the name
	 * @param pass  the password
	 * @param money the money
	 * @return the traveler added
	 */
	public Traveler addTraveler(String name, String pass, int money) {
		System.out.println(">> TestDataAccess: addTraveler");
		Traveler traveler = null;
		db.getTransaction().begin();
		try {
			traveler = db.find(Traveler.class, name);
			if (traveler == null) {
				traveler = new Traveler(name, pass);
				traveler.setMoney(money);
				db.persist(traveler);
			}
			db.getTransaction().commit();
			System.out.println("Traveler created " + traveler);

			return traveler;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 

	public boolean removeTraveler(String name) {
		System.out.println(">> TestDataAccess: removetraveler");
		Traveler t = db.find(Traveler.class, name);
		if (t != null) {
			db.getTransaction().begin();
			for (Booking b : t.getBookedRides()) {
				db.remove(b);
			}
			db.remove(t);
			db.getTransaction().commit();
			return true;
		} else
			return false;
	}

	public boolean existTraveler(String name) {
		System.out.println(">> TestDataAccess: existTraveler");
		Traveler d = db.find(Traveler.class, name);
		if (d != null) {
			return true;
		} else
			return false;
	}

	public Booking getBooking(Ride ride, String travelerName) {
		System.out.println(">> TestDataAccess: getBooking");

		try {
			Traveler traveler = db.find(Traveler.class, travelerName);

			if (traveler == null) {
				System.out.println("Traveler not found: " + travelerName);
				return null;
			}

			return db
					.createQuery("SELECT b FROM Booking b WHERE b.ride = :ride AND b.traveler = :traveler",
							Booking.class)
					.setParameter("ride", ride).setParameter("traveler", traveler).getSingleResult();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public boolean removeBooking(int bookNumber) {
	    System.out.println(">> TestDataAccess: removeBooking");
	    
	    // Busca la reserva en la base de datos usando el bookNumber
	    Booking booking = db.find(Booking.class, bookNumber);
	    
	    if (booking != null) {
	        db.getTransaction().begin(); // Inicia la transacción
	        db.remove(booking); // Elimina la reserva
	        db.getTransaction().commit(); // Confirma los cambios
	        return true; // La eliminación fue exitosa
	    } else {
	        return false; // La reserva no fue encontrada
	    }
	}
	public void addDriver(Driver driver) {
	    db.getTransaction().begin();
	    db.persist(driver);
	    db.getTransaction().commit();
	}

	public void addRide(Ride ride) {
	    db.getTransaction().begin();
	    db.persist(ride);
	    db.getTransaction().commit();
	}

	public void addTraveler(Traveler traveler) {
	    db.getTransaction().begin();
	    db.persist(traveler);
	    db.getTransaction().commit();
	}

	public void addBooking(Booking booking) {
	    db.getTransaction().begin();
	    db.persist(booking);
	    db.getTransaction().commit();
	}
	
	public void updateRide(Ride ride) {
	    try {
	        db.getTransaction().begin();
	        db.merge(ride); // Merge se usa para actualizar la entidad en la base de datos
	        db.getTransaction().commit();
	    } catch (Exception e) {
	        e.printStackTrace();
	        db.getTransaction().rollback();
	    }
	}
	
	public void cancelRide(Ride ride) {
		System.out.println(">> TestDataAccess: cancelRide");
		db.getTransaction().begin();
		try {
			if (!this.existRide(ride.getDriver().getUsername(), ride.getFrom(), ride.getTo(), ride.getDate())) {
				db.persist(ride);
				db.persist(ride.getDriver());
			}
		    List<Booking> bookings = ride.getBookings();
		    Traveler t;
		    if (bookings != null) {
		    	for(Booking b : bookings) {
			    	t = b.getTraveler();
			    	db.persist(t);
			    	db.persist(b);
			    }
		    }
		    db.getTransaction().commit();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void removeTestData(List<Ride> rides) {
		db.getTransaction().begin();
		try {
			for (Ride r : rides) {
				db.remove(r);
			}
			db.getTransaction().commit();
		} catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            e.printStackTrace();
        }
	}


	public void removeJon() {
		db.getTransaction().begin();
		try {
			Driver d = db.find(Driver.class, "Jon");
			if (d != null) {
				db.remove(d);
			}
			db.getTransaction().commit();
		} catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            e.printStackTrace();
        }
		
	}


	public void removeTravelerJon(String string) {
		db.getTransaction().begin();
		try {
			 Traveler d = db.find(Traveler.class, string);
			if (d != null) {
				db.remove(d);
			}
			db.getTransaction().commit();
		} catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            e.printStackTrace();
        }
		
	}


}