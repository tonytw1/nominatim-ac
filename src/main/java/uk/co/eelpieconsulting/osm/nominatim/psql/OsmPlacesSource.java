package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

public class OsmPlacesSource implements Iterator<Place> {
	
	private static Logger log = Logger.getLogger(OsmPlacesSource.class);

	private static final long STEP_SIZE = 1000;
	
	private final OsmDAO osmDAO;
	private PlaceRowParser placeRowParser;
	
	private ResultSet places;
	private boolean next;
	private long start;
	
	private long max;
	private final String type;

	
	public OsmPlacesSource(OsmDAO osmDAO, PlaceRowParser placeRowParser, String type) throws SQLException {
		this.osmDAO = osmDAO;
		this.placeRowParser = placeRowParser;
		this.type = type;
		start = 0;
		this.max = osmDAO.getMax(type);
		prepare(osmDAO);
	}

	private void prepare(OsmDAO osmDAO) {
		log.info("Preparing type: " + type + " " + start + "/" + max);
		try {
			places = osmDAO.getPlaces(start, STEP_SIZE, type);
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
			return placeRowParser.buildPlaceFromRow(places);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}