package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.views.json.JsonSerializer;
import uk.co.eelpieconsulting.osm.nominatim.indexing.LineIndexer;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

@Component
public class ElasticSearchIndexer implements LineIndexer {

	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer;
	
	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.jsonSerializer = new JsonSerializer();
	}
	
	@Override
	public void indexLines(PlacesDumpParser parser) {
		final Client client = elasticSearchClientFactory.getClient();
		final BulkRequestBuilder bulkRequest = client.prepareBulk();
		
		while (parser.hasNext()) {
			Place place = parser.next();
			System.out.println(place);
			
			final String placeJson = jsonSerializer.serialize(place);
			System.out.println(placeJson);
			bulkRequest.add(client.prepareIndex("osm", "places", place.getOsmId() + place.getType()).setSource(placeJson));             
              
			bulkRequest.execute().actionGet(); 
		}		
	}

}
