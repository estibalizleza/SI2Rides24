package adapterPatroia;

import javax.swing.table.AbstractTableModel;
import domain.Driver;
import  domain.Ride;

public class DriverAdapter extends AbstractTableModel {
	private Driver driver;
	private String[] columnNames = { "From", "To", "Date", "Places", "Price" };

	public DriverAdapter(Driver driver) {
		this.driver = driver;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		// Driver klaseko bidaien lista erabili
		return driver.getCreatedRides().size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		// Lerro jakin baten bidaia lortu
		Ride ride = driver.getCreatedRides().get(row);

		// Zutabe bakoitzerako dagokion balioa bueltatu
		switch (col) {
		case 0:
			return ride.getFrom();
		case 1:
			return ride.getTo();
		case 2:
			return ride.getDate();
		case 3:
			return ride.getnPlaces();
		case 4:
			return ride.getPrice();
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
}
