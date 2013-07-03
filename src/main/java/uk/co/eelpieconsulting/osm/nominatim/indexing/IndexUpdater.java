package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

@Component
public class IndexUpdater {
	
	private final LineIndexer solrIndexer;
	
	@Autowired
	public IndexUpdater(LineIndexer solrIndexer) {
		this.solrIndexer = solrIndexer;
	}
		
	public void buildIndex(String dumpFileName) throws SolrServerException, IOException {		
		final URL resource = this.getClass().getClassLoader().getResource(dumpFileName);
		final PlacesDumpParser parser = new PlacesDumpParser(FileUtils.toFile(resource));
		
		solrIndexer.indexLines(parser);
	}
	
}
