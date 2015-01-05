package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

@Component
public class IndexUpdater {
	
	private static Logger log = Logger.getLogger(IndexUpdater.class);
	
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
		
		for (int i = 30; i >= 0; i--) {
			log.info("Starting rank: " + i);
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "N", i));
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "W", i));
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(username, password), "R", i));			
		}
		
	}
	
}
