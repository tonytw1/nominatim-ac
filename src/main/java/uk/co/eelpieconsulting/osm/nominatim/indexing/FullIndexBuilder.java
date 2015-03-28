package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

@Component
public class FullIndexBuilder {
		
	private final ElasticSearchIndexer indexer;
	private final String username;
	private final String password;
	private final String host;
	
	@Autowired
	public FullIndexBuilder(ElasticSearchIndexer indexer, 
			@Value("${database.username}") String username,
			@Value("${database.password}") String password,
			@Value("${database.host}") String host) {
		this.indexer = indexer;
		this.username = username;
		this.password = password;
		this.host = host;
	}
		
	public void buildFullIndex() throws SQLException {	
		indexer.deleteAll();		
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password, host), "R"));
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password, host), "W"));			
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password, host), "N"));					
	}
	
}
