package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class PlaceRowParser {
	
	private static final List<String> IGNORED_TAG_CLASSIFICATIONS = Lists.newArrayList("wikipedia", "description", "attribution", "population", "name:prefix", "website");

	public Place buildPlaceFromRow(ResultSet places) throws SQLException {
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
	}
	
	private void appendTag(String classification, String type, Set<String> tags) {
		if (IGNORED_TAG_CLASSIFICATIONS.contains(classification)) {
			return;
		}
		tags.add(classification + "|" + type);
	}
	
}
