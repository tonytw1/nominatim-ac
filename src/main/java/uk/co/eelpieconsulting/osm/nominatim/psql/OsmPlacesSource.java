package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.collect.Maps;

public class OsmPlacesSource implements Iterator<Place> {
	
	private static Logger log = Logger.getLogger(OsmPlacesSource.class);

	private static final long STEP_SIZE = 1000;
	
	private ResultSet places;
	private boolean next;
	private long start;
	private final OsmDAO osmDAO;

	private long max;
	private final String type;
	
	public OsmPlacesSource(OsmDAO osmDAO, String type) throws SQLException {
		this.osmDAO = osmDAO;
		this.type = type;
		start = 0;
		this.max = osmDAO.getMax(type);
		prepare(osmDAO);
	}

	private void prepare(OsmDAO osmDAO) {
		log.info("Preparing type: " + type);
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
			long osmId = places.getLong("osm_id");
			String osmType = places.getString("osm_type");
			String name = places.getString("en_label");
			String classification = places.getString(3);
			String type = places.getString(4);
			int rank = places.getInt("rank");
			double latitude = places.getDouble("latitude");
			double longitude = places.getDouble("longitude");
			Map<String, String> extratags = (Map<String, String>) places.getObject("extratags");
						
			next = places.next();
						
			Map<String, Double> latlong = Maps.newHashMap();
			latlong.put("lat", latitude);
			latlong.put("lon", longitude);
			
			List<String> tags = Lists.newArrayList();
			appendTag(classification, type, tags);
			if (extratags != null) {
				for (String key : extratags.keySet()) {
					appendTag(key, extratags.get(key), tags);
				}
			}
			
			Place place = new Place(osmId, osmType, null, name, classification, type, rank, latlong, tags);
			log.info(place);
			return place;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void appendTag(String classification, String type, List<String> tags) {
		if (classification.equals("wikipedia") || classification.equals("description")) {
			return;
		}
		tags.add(classification + "|" + type);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}