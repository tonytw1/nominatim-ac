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
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

@Component
public class ElasticSearchIndexer {
	
	public static final String TYPE = "places";
	private static final String INDEX = "osm2015021001";

	private static Logger log = Logger.getLogger(ElasticSearchIndexer.class);
		
	private static final int COMMIT_SIZE = 1000;
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer;
	
	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.jsonSerializer = new JsonSerializer();
	}
	
	public void indexLines(OsmPlacesSource parser) {
		final Client client = elasticSearchClientFactory.getClient();
				
		log.info("Importing records");
		BulkRequestBuilder bulkRequest = client.prepareBulk();		

		int count = 0;
		DateTime countStart = DateTime.now();
		while (parser.hasNext()) {
			Place place = parser.next();
			count++;
			
			final String placeJson = jsonSerializer.serialize(place);
			bulkRequest.add(client.prepareIndex(INDEX, TYPE, place.getOsmId() + place.getOsmType()).setSource(placeJson));
			
			if (count == COMMIT_SIZE) {
				bulkRequest.execute().actionGet();
				
				Duration duration = new Duration(countStart.getMillis(), DateTime.now().getMillis());
				log.info("Imported " + COMMIT_SIZE + " in " + duration.getMillis());
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

	public void deleteAll() {
		final Client client = elasticSearchClientFactory.getClient();
		log.info("Deleting existing records");
		client.prepareDeleteByQuery(INDEX).
			setQuery(QueryBuilders.matchAllQuery()).
			setTypes(TYPE).
			execute().
			actionGet();
	}

}
