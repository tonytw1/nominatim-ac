package uk.co.eelpieconsulting.osm.nominatim.solr;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.indexing.LineIndexer;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

import com.google.common.base.Splitter;

@Component
public class SolrIndexer implements LineIndexer {
	
	private static final int COMMIT_SIZE = 10000;
	
	private final Splitter onCommaSeperator = Splitter.on(",");
	
	private final SolrServer solrServer;
	
	@Value("#{autoComplete['solr.url']}") String solrUrl;
	
	public SolrIndexer() throws MalformedURLException {
		this.solrServer = new CommonsHttpSolrServer(solrUrl);
	}
	
	public void indexLines(final PlacesDumpParser parser) {
		try {
			int count = 0;
			int total = 0;
			
			UpdateRequest updateRequest = new UpdateRequest();
			while (parser.hasNext()) {
				Place place = parser.next();
				final SolrInputDocument inputDocument = new SolrInputDocument();
				inputDocument.addField("id", place.getOsmId() + place.getType());
				inputDocument.addField("osm_id", place.getOsmId());
				inputDocument.addField("osm_type", place.getOsmType());
				
				final String name = onCommaSeperator.split(place.getAddress()).iterator().next();			
				inputDocument.addField("name", name);
				inputDocument.addField("name_string", place.getAddress().replaceFirst("^The ", ""));
				inputDocument.addField("address_line", place.getAddress());
				inputDocument.addField("class", place.getClassification());
				inputDocument.addField("type", place.getType());
		
				updateRequest.add(inputDocument);
				count++;
				if (count > COMMIT_SIZE) {
						updateRequest.process(solrServer);
						solrServer.commit();
						updateRequest = new UpdateRequest();
						total = total + count;
						count = 0;
					
				}
			}
			
			updateRequest.process(solrServer);
			solrServer.commit();
		
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
