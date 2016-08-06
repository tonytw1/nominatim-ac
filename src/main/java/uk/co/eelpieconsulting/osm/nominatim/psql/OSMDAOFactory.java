package uk.co.eelpieconsulting.osm.nominatim.psql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class OSMDAOFactory {
	
	private String username;
	private String password;
	private String host;

	@Autowired
	public OSMDAOFactory(@Value("${database.username}") String username,
		@Value("${database.password}") String password,
		@Value("${database.host}") String host) {
			this.username = username;
			this.password = password;
			this.host = host;
	}

	public OsmDAO build() throws SQLException {
		return new OsmDAO(username, password, host);
	}
	
}
