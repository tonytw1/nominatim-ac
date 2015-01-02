package uk.co.eelpieconsulting.osm.nominatim.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class OsmDAO {

	private final Connection conn;
	private final PreparedStatement places;
	
	public OsmDAO() throws SQLException {
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
	
	public void getTags() throws SQLException {
		Connection conn = getConnection();
		Statement st = conn.createStatement();
		
		ResultSet rs = st.executeQuery("select tags from planet_osm_rels where id = 65606");

		while (rs.next()) {
			String[] tagsField = (String[]) rs.getArray("tags").getArray();			
			for (int i = 0; i < tagsField.length; i = i + 2) {
				System.out.println(tagsField[i] + ": " + tagsField[i + 1]);	
			}
		}
		
		rs.close();
		st.close();
		conn.close();
		
	}
	
	private Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://localhost/nominatim";
		Properties props = new Properties();
		props.setProperty("user", "");
		props.setProperty("password", "");

		Connection conn = DriverManager.getConnection(url, props);
		return conn;
	}

}