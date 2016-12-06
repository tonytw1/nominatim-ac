package uk.co.eelpieconsulting.osm.nominatim.psql;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

@Component
public class PlaceRowParser {

	private final FormattedAddressCorrection formattedAddressCorrection = new FormattedAddressCorrection();

	private static final String NAME = "name";

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

		String correctedAddress = formattedAddressCorrection.appendName(address, (Map<String, String>) placeRow.getObject(NAME));

		return new Place(osmId, osmType, null, correctedAddress, classification, type, rank, latlong, Lists.newArrayList(tags), country, adminLevel);
	}
	
	private void appendTag(String classification, String type, Set<String> tags) {
		tags.add(classification + "|" + type);
	}
	
}
