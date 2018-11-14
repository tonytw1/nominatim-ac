package uk.co.eelpieconsulting.osm.nominatim.psql;

import org.apache.log4j.Logger;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class OsmPlacesSource implements Iterator<Place> {
	
	private static Logger log = Logger.getLogger(OsmPlacesSource.class);

	private static final long STEP_SIZE = 1000;
	
	private final OsmDAO osmDAO;
	private PlaceRowParser placeRowParser;
	
	private ResultSet places;
	private long start;
	
	private long max;
	private final String type;

	
	public OsmPlacesSource(OsmDAO osmDAO, PlaceRowParser placeRowParser, String type) throws SQLException {
		this.osmDAO = osmDAO;
		this.placeRowParser = placeRowParser;
		this.type = type;
		start = 0;
		this.max = osmDAO.getMax(type);
		prepare();
	}

	private void prepare() {
		log.info("Preparing type: " + type + " " + start + "/" + max);
		try {
			places = osmDAO.getPlaces(start, STEP_SIZE, type);						
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return start < max;		
	}

	@Override
	public Place next() {
		try {
			if (places.isLast()) {
				log.debug("After last; preparing again");
				prepare();
			}			
			places.next();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
			
		try {
			Place place = placeRowParser.buildPlaceFromCurrentRow(places);
			start = place.getOsmId();
			return place;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}