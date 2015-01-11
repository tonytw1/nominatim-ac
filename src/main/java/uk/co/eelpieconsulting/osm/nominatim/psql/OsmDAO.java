package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OsmDAO {

	private final Connection conn;
	private final PreparedStatement places;
	
	private final String username;
	private final String password;
	
	public OsmDAO(String username, String password) throws SQLException {
		this.username = username;
		this.password = password;
		
		conn = getConnection();
		places = conn.prepareStatement("SELECT osm_id, osm_type, class, type, housenumber, "
						//+ "get_address_by_language(place_id,  ARRAY['']) AS label,"
						+ "get_address_by_language(place_id, ARRAY['name:en']) AS en_label,"
						+ "calculated_country_code AS country,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
						+ "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
						+ "rank_address AS rank, " 
						+ "extratags "
						+ "FROM placex "
						+ "WHERE osm_id >= ? AND osm_id < ? AND osm_type=?");
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
	
	private Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://localhost/nominatim";
		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", password);

		Connection conn = DriverManager.getConnection(url, props);
		return conn;
	}

}