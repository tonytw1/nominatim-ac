package uk.co.eelpieconsulting.osm.nominatim.solr;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.AutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.collect.Lists;

@Component
public class SolrDAO implements AutoCompleteService {

	@Value("#{autoComplete['solr.url']}") String solrUrl;
	
	private SolrServer solrServer;
	
	public SolrDAO() throws MalformedURLException {
		this.solrServer = new CommonsHttpSolrServer(solrUrl);
	}
	
	@Override
	public List<Place> getSuggestionsFor(String q) {
		try {			
			final String term = new String(q.toLowerCase());
			SolrQuery query = new SolrQuery("*:*");
			query.setQuery("name_string:" + term.replace(" ", "\\ ") + "*");
			query.addFilterQuery("-type:bus_stop");		
			query.addFilterQuery("-type:house");
			query.addFilterQuery("-type:post_box");
			
			QueryResponse response = solrServer.query(query);
			SolrDocumentList results = response.getResults();
			
			final List<Place> suggestions = Lists.newArrayList();
			for (SolrDocument solrDocument : results) {
				suggestions.add(new Place((Long) solrDocument.getFieldValue("osm_id"), 
						(String) solrDocument.getFieldValue("osm_type"),
						null,
						(String) solrDocument.getFieldValue("address_line"),
						(String) solrDocument.getFieldValue("class"),
						(String) solrDocument.getFieldValue("type")));
			}
			return suggestions;
			
		} catch (SolrServerException e) {
			throw new RuntimeException();
		}
	}

}
