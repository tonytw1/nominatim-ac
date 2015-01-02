package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

public class OsmPlacesSource implements Iterator<Place> {

	private static final long STEP_SIZE = 1000;
	
	private ResultSet places;
	private boolean next;
	private long start;
	private final OsmDAO osmDAO;

	private long max;
	private final String type;
	private final int rank;
	
	public OsmPlacesSource(OsmDAO osmDAO, String type, int rank) throws SQLException {
		this.osmDAO = osmDAO;
		this.type = type;
		this.rank = rank;
		start = 0;
		this.max = osmDAO.getMax(type, rank);
		System.out.println("Max: " + max);
		prepare(osmDAO);
	}

	private void prepare(OsmDAO osmDAO) {
		try {
			places = osmDAO.getPlaces(start, STEP_SIZE, type, rank);
			next = places.isBeforeFirst();
			if (next) {
				places.next();
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		if (next) {
			return true;
		}
		
		while (!next && start < max) {
			start = start + STEP_SIZE;
			prepare(osmDAO);
		}
		
		return next;
	}

	@Override
	public Place next() {
		try {		
			long osmId = places.getLong("osm_id");
			String osmType = places.getString("osm_type");
			String name = places.getString("en_label");
			String classification = places.getString(3);
			String type = places.getString(4);
			int rank = places.getInt("rank");
			double latitude = places.getDouble("latitude");
			double longitude = places.getDouble("longitude");

			next = places.next();
			
			return new Place(osmId, osmType, null, name, classification, type, rank, new LatLong(latitude, longitude));
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}