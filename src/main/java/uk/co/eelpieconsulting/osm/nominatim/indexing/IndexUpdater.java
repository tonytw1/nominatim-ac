package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

@Component
public class IndexUpdater {
	
	private static Logger log = Logger.getLogger(IndexUpdater.class);
	
	private final ElasticSearchIndexer indexer;
	
	@Autowired
	public IndexUpdater(ElasticSearchIndexer indexer) {
		this.indexer = indexer;
	}
		
	public void buildIndex(String filePath) throws FileNotFoundException, IOException, SQLException {
		
		indexer.deleteAll();
		
		for (int i = 0; i <=30; i++) {
			log.info("Starting rank: " + i);
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(), "N", i));
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(), "W", i));
			indexer.indexLines(new OsmPlacesSource(new OsmDAO(), "R", i));			
		}
		
	}
	
}
