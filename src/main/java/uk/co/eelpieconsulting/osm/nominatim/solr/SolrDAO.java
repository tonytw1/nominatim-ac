package uk.co.eelpieconsulting.osm.nominatim.solr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

@Component
public class SolrDAO {
	
	public List<Place> getSuggestionsFor(String q) throws SolrServerException, MalformedURLException {
		SolrServer solrServer = new CommonsHttpSolrServer("http://localhost:8080/apache-solr-3.6.1/osm");
		
		SolrQuery query = new SolrQuery("*:*");
		query.setQuery("name_string:" + q.replace(" ", "\\ ") + "*");
		query.addFilterQuery("-type:bus_stop");		
		query.addFilterQuery("-type:house");
		query.addFilterQuery("-type:post_box");
		
		QueryResponse response = solrServer.query(query);
		SolrDocumentList results = response.getResults();
		
		List<Place> suggestions = new ArrayList<Place>();
		for (SolrDocument solrDocument : results) {
			suggestions.add(new Place((Long) solrDocument.getFieldValue("osm_id"), 
					(String) solrDocument.getFieldValue("osm_type"),
					null,
					(String) solrDocument.getFieldValue("address_line"),
					(String) solrDocument.getFieldValue("class"),
					(String) solrDocument.getFieldValue("type")));
		}
		return suggestions;
	}

}
