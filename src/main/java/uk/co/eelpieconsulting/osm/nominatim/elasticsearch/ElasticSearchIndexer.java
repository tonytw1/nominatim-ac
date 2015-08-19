package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import java.util.List;

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
	
	public static final String READ_INDEX = "osm20150815";	
	public static final String WRITE_INDEX = "osm20150819";
	public static final String TYPE = "places";
	
	private static final Logger log = Logger.getLogger(ElasticSearchIndexer.class);

	private static final int COMMIT_SIZE = 10000;
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer;
	
	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.jsonSerializer = new JsonSerializer();
	}
	
	public void indexLines(OsmPlacesSource osmPlacesSource) {
		final Client client = elasticSearchClientFactory.getClient();
				
		log.info("Importing records");
		BulkRequestBuilder bulkRequest = client.prepareBulk();		

		int count = 0;
		DateTime countStart = DateTime.now();
		while (osmPlacesSource.hasNext()) {
			Place place = osmPlacesSource.next();
			count++;
			
			final String placeJson = jsonSerializer.serialize(place);
			bulkRequest.add(client.prepareIndex(WRITE_INDEX, TYPE, place.getOsmId() + place.getOsmType()).setSource(placeJson));
			
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
		client.prepareDeleteByQuery(WRITE_INDEX).
			setQuery(QueryBuilders.matchAllQuery()).
			setTypes(TYPE).
			execute().
			actionGet();
	}

	public void index(List<Place> places) {
		log.info("Importing updates");

		final Client client = elasticSearchClientFactory.getClient();		
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Place place : places) {
			bulkRequest.add(client.prepareIndex(WRITE_INDEX, TYPE, place.getOsmId() + place.getOsmType()).setSource(jsonSerializer.serialize(place)));
		}
		bulkRequest.execute().actionGet();
		log.info("Update submitted");
	}

}