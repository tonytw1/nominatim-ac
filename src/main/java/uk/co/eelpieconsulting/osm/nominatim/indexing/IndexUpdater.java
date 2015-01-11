package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

@Component
public class IndexUpdater {
		
	private final ElasticSearchIndexer indexer;
	private final String username;
	private final String password;
	
	@Autowired
	public IndexUpdater(ElasticSearchIndexer indexer, 
			@Value("${database.username}") String username,
			@Value("${database.password}") String password) {
		this.indexer = indexer;
		this.username = username;
		this.password = password;
	}
		
	public void buildIndex() throws SQLException {	
		indexer.deleteAll();		
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "R"));
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "W"));			
		indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "N"));					
	}
	
}
