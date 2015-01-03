package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.common.collect.Lists;

public class OsmDAO {

	private final Connection conn;
	private final PreparedStatement places;
	private final PreparedStatement nodeTags;
	private final PreparedStatement relationTags;
	private final PreparedStatement wayTags;
	
	private final String username;
	private final String password;
	
	public OsmDAO(String username, String password) throws SQLException {
		this.username = username;
		this.password = password;
		
		conn = getConnection();
		places = conn.prepareStatement("SELECT osm_id, osm_type, class, type, housenumber, "
						+ "get_address_by_language(place_id,  ARRAY['']) AS label,"
						+ "get_address_by_language(place_id, ARRAY['name:en']) AS en_label,"
						+ "calculated_country_code AS country,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
						+ "rank_address AS rank " 
						+ "FROM placex "
						+ "WHERE osm_id >= ? AND osm_id < ? AND osm_type=? AND rank_address = ?");
		
		nodeTags = conn.prepareStatement("select tags from planet_osm_nodes where id = ?");
		relationTags = conn.prepareStatement("select tags from planet_osm_rels where id = ?");
		wayTags = conn.prepareStatement("select tags from planet_osm_ways where id = ?");
	}
	
	public long getMax(String type, int rank) throws SQLException {
		PreparedStatement prepareStatement = conn.prepareStatement("SELECT MAX(osm_id) AS end from placex WHERE osm_type=? AND rank_address = ?");
		prepareStatement.setString(1, type);
		prepareStatement.setInt(2, rank);
		ResultSet rs = prepareStatement.executeQuery();
		rs.next();
		long max = rs.getLong(1);

		rs.close();
		prepareStatement.close();
		return max;
	}
	
	public ResultSet getPlaces(long start, long stepSize, String type, int rank) throws SQLException {
		places.setLong(1, start);
		places.setLong(2, start + stepSize);
		places.setString(3, type);
		places.setInt(4, rank);
		return places.executeQuery();
	}
	
	public List<String> getTags(long osmId, String osmType) throws SQLException {
		List<String> tags = Lists.newArrayList();
		if (osmType.equals("N")) {			
			nodeTags.setLong(1, osmId);
			ResultSet rs = nodeTags.executeQuery();
			readTags(tags, rs);
			return tags;		
		}
		if (osmType.equals("R")) {			
			relationTags.setLong(1, osmId);
			ResultSet rs = relationTags.executeQuery();
			readTags(tags, rs);
			return tags;		
		}
		if (osmType.equals("W")) {			
			wayTags.setLong(1, osmId);
			ResultSet rs = wayTags.executeQuery();
			readTags(tags, rs);
			return tags;		
		}
		return Lists.newArrayList();		
	}

	private void readTags(List<String> tags, ResultSet rs) throws SQLException {
		while (rs.next()) {
			String[] tagsField = (String[]) rs.getArray("tags").getArray();			
			for (int i = 0; i < tagsField.length; i = i + 2) {
				tags.add(tagsField[i] + "|" + tagsField[i + 1]);	
			}
		}
	}
	
	private Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://localhost/nominatim";
		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", password);

		Connection conn = DriverManager.getConnection(url, props);
		return conn;
	}

}