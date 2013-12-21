package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
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
		
	public void buildIndex(String dumpFileName) throws FileNotFoundException, IOException {		
		final URL resource = this.getClass().getClassLoader().getResource(dumpFileName);
		final PlacesDumpParser parser = new PlacesDumpParser(FileUtils.toFile(resource));		
		indexer.indexLines(parser);
	}
	
}
