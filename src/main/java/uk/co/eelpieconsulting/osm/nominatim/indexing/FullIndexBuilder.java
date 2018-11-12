package uk.co.eelpieconsulting.osm.nominatim.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import java.io.IOException;
import java.sql.SQLException;

@Component
public class FullIndexBuilder {
		
	private final ElasticSearchIndexer indexer;
	private final OSMDAOFactory osmDaoFactory;
	private final PlaceRowParser placeRowParser;
	
	@Autowired
	public FullIndexBuilder(ElasticSearchIndexer indexer, OSMDAOFactory osmDaoFactory, PlaceRowParser placeRowParser) {
		this.indexer = indexer;
		this.osmDaoFactory = osmDaoFactory;
		this.placeRowParser = placeRowParser;
	}
		
	public void buildFullIndex() throws SQLException, IOException {
		//indexer.deleteAll();		
		indexer.indexLines(new OsmPlacesSource(osmDaoFactory.build(), placeRowParser, "R"));
		indexer.indexLines(new OsmPlacesSource(osmDaoFactory.build(), placeRowParser, "W"));
		indexer.indexLines(new OsmPlacesSource(osmDaoFactory.build(), placeRowParser, "N"));				
	}
	
}
