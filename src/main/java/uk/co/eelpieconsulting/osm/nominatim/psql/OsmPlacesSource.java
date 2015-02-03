package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class OsmPlacesSource implements Iterator<Place> {
	
	private static Logger log = Logger.getLogger(OsmPlacesSource.class);

	private static final List<String> IGNORED_TAG_CLASSIFICATIONS = Lists.newArrayList("wikipedia", "description", "attribution", "population", "name:prefix", "website");
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
			long osmId = places.getLong("osm_id");
			String osmType = places.getString("osm_type");
			String name = places.getString("en_label");
			String classification = places.getString(3);
			String type = places.getString(4);
			int rank = places.getInt("rank");
			double latitude = places.getDouble("latitude");
			double longitude = places.getDouble("longitude");
			String country = places.getString("country");
			int adminLevel = places.getInt("admin_level");
			
			Map<String, String> extratags = (Map<String, String>) places.getObject("extratags");
						
			next = places.next();
						
			Map<String, Double> latlong = Maps.newHashMap();
			latlong.put("lat", latitude);
			latlong.put("lon", longitude);
			
			Set<String> tags = Sets.newHashSet();
			appendTag(classification, type, tags);
			if (extratags != null) {
				for (String key : extratags.keySet()) {
					appendTag(key, extratags.get(key), tags);
				}
			}
			
			return new Place(osmId, osmType, null, name, classification, type, rank, latlong, Lists.newArrayList(tags), country, adminLevel);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void appendTag(String classification, String type, Set<String> tags) {
		if (IGNORED_TAG_CLASSIFICATIONS.contains(classification)) {
			return;
		}
		tags.add(classification + "|" + type);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}