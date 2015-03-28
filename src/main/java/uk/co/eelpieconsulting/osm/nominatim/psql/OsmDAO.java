package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.joda.time.DateTime;

public class OsmDAO {

	private final Connection conn;
	private final PreparedStatement places;
	private PreparedStatement placesIndexedFrom;
	
	private final String username;
	private final String password;
	private final String host;
	
	public OsmDAO(String username, String password, String host) throws SQLException {
		this.username = username;
		this.password = password;
		this.host = host;
		
		conn = getConnection();
		places = conn.prepareStatement("SELECT osm_id, osm_type, class, type, housenumber, "
						//+ "get_address_by_language(place_id,  ARRAY['']) AS label,"
						+ "get_address_by_language(place_id, ARRAY['name:en']) AS en_label,"
						+ "calculated_country_code AS country,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
						+ "rank_address AS rank, " 
						+ "admin_level AS admin_level, " 
						+ "extratags "
						+ "FROM placex "
						+ "WHERE osm_id >= ? AND osm_id < ? AND osm_type=?");
		
		placesIndexedFrom = conn.prepareStatement("SELECT osm_id, osm_type, class, type, housenumber, "
				//+ "get_address_by_language(place_id,  ARRAY['']) AS label,"
				+ "get_address_by_language(place_id, ARRAY['name:en']) AS en_label,"
				+ "calculated_country_code AS country,"
				+ "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
				+ "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
				+ "rank_address AS rank, " 
				+ "admin_level AS admin_level, " 
				+ "indexed_date AS indexed_date, " 
				+ "extratags "
				+ "FROM placex "
				+ "WHERE indexed_date > ? " 
				+ "ORDER BY indexed_date "
				+ "LIMIT ?");		
	}
	
	public long getMax(String type) throws SQLException {
		PreparedStatement prepareStatement = conn.prepareStatement("SELECT MAX(osm_id) AS end from placex WHERE osm_type=?");
		prepareStatement.setString(1, type);
		ResultSet rs = prepareStatement.executeQuery();
		rs.next();
		long max = rs.getLong(1);
		rs.close();
		prepareStatement.close();
		return max;
	}
	
	public ResultSet getPlaces(long start, long stepSize, String type) throws SQLException {
		places.setLong(1, start);
		places.setLong(2, start + stepSize);
		places.setString(3, type);
		return places.executeQuery();
	}
	
	public ResultSet getPlacesIndexedAfter(DateTime start, int limit) throws SQLException {	
		placesIndexedFrom.setTimestamp(1, new java.sql.Timestamp(start.getMillis()));
		placesIndexedFrom.setLong(2, limit);
		return placesIndexedFrom.executeQuery();
	}
	
	private Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://" + host + "/nominatim";
		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", password);

		Connection conn = DriverManager.getConnection(url, props);
		return conn;
	}
	
}