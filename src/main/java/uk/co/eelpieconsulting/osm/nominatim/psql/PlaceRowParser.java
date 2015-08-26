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
	
	private static final String NAME = "name";	
	private static final List<String> IGNORED_TAG_CLASSIFICATIONS = Lists.newArrayList("wikipedia", "description", "attribution", "population", "name:prefix", "website");
	
	public Place buildPlaceFromCurrentRow(ResultSet placeRow) throws SQLException {
		long osmId = placeRow.getLong("osm_id");
		String osmType = placeRow.getString("osm_type");
		String classification = placeRow.getString(3);
		String type = placeRow.getString(4);
		int rank = placeRow.getInt("rank");
		double latitude = placeRow.getDouble("latitude");
		double longitude = placeRow.getDouble("longitude");
		String country = placeRow.getString("country");
		int adminLevel = placeRow.getInt("admin_level");
		
		Map<String, String> extratags = (Map<String, String>) placeRow.getObject("extratags");
		
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
		
		String address = placeRow.getString("en_label");
		Map<String, String> name = (Map<String, String>) placeRow.getObject(NAME);
		if (name != null && name.containsKey(NAME)) {
			String n = name.get(NAME);
			if (!address.startsWith(n)) {
				address = n + ", " + address;
			}
  		}
		
		return new Place(osmId, osmType, null, address, classification, type, rank, latlong, Lists.newArrayList(tags), country, adminLevel);
	}
	
	private void appendTag(String classification, String type, Set<String> tags) {
		if (IGNORED_TAG_CLASSIFICATIONS.contains(classification)) {
			return;
		}
		tags.add(classification + "|" + type);
	}
	
}
