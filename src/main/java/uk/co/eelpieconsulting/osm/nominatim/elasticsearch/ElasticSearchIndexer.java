package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.views.json.JsonSerializer;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

@Component
public class ElasticSearchIndexer {
	
	private static Logger log = Logger.getLogger(ElasticSearchIndexer.class);
	
	private static final int COMMIT_SIZE = 10000;
	private static final String INDEX = "osm";
	private static final String TYPE = "places";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer;
	
	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.jsonSerializer = new JsonSerializer();
	}
	
	public void indexLines(PlacesDumpParser parser) {
		final Client client = elasticSearchClientFactory.getClient();
		
		log.info("Deleting existing records");
		client.prepareDeleteByQuery(INDEX).
			setQuery(QueryBuilders.matchAllQuery()).
			setTypes(TYPE).
			execute().
			actionGet();
		
		
		log.info("Importing records");
		BulkRequestBuilder bulkRequest = client.prepareBulk();		

		int count = 0;
		DateTime countStart = DateTime.now();
		while (parser.hasNext()) {
			Place place = parser.next();
			count++;
			
			final String placeJson = jsonSerializer.serialize(place);
			bulkRequest.add(client.prepareIndex(INDEX, TYPE, place.getOsmId() + place.getType()).setSource(placeJson));
			
			if (count == COMMIT_SIZE) {
				bulkRequest.execute().actionGet();
				
				Duration duration = new Duration(countStart.getMillis(), DateTime.now().getMillis());
				log.info("Imported " + COMMIT_SIZE + " in " + duration + " at a rate of " + COMMIT_SIZE / duration.getMillis() + " per milli second");
				count = 0;
				countStart = DateTime.now();
				bulkRequest = client.prepareBulk();
			}			
		}
		
		if (count > 0) {
			bulkRequest.execute().actionGet();
		}
		
		log.info("Import completed");
	}

}
