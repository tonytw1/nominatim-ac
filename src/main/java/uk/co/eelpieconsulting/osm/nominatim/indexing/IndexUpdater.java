package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

@Component
public class IndexUpdater {
	
	private final ElasticSearchIndexer indexer;
	
	@Autowired
	public IndexUpdater(ElasticSearchIndexer indexer) {
		this.indexer = indexer;
	}
		
	public void buildIndex(String filePath) throws FileNotFoundException, IOException {				
		final PlacesDumpParser parser = new PlacesDumpParser(new File(filePath));		
		indexer.indexLines(parser);
	}
	
}
