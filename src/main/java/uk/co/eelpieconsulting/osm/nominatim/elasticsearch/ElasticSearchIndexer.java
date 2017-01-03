package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.views.json.JsonSerializer;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

import java.util.List;
import java.util.Set;

@Component
public class ElasticSearchIndexer {
	
	private static final Logger log = Logger.getLogger(ElasticSearchIndexer.class);

	private static final Set<String> IGNORED_TAG_CLASSIFICATIONS = Sets.newHashSet("wikipedia", "description", "attribution", "population", "name:prefix", "website");	// TODO is this optimisation actualy useful?

	public static final String TYPE = "places";
	private static final int COMMIT_SIZE = 1000;
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer;

	private final String writeIndex;

	private final Client client;

	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.write}") String writeIndex) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.writeIndex = writeIndex;
		this.jsonSerializer = new JsonSerializer();
		client = elasticSearchClientFactory.getClient();
	}
	
	public void indexLines(OsmPlacesSource osmPlacesSource) {
		log.info("Importing records");
		BulkRequestBuilder bulkRequest = client.prepareBulk();		

		List<Place> places = Lists.newArrayList();
		DateTime countStart = DateTime.now();

		Place currentPlace = null;
		Set<String> currentTags = Sets.newHashSet();
		while (osmPlacesSource.hasNext()) {
			Place place = osmPlacesSource.next();
			if (currentPlace == null) {
				currentPlace = place;
			}

			boolean placeIsDifferentFromTheLast = !(place.getOsmId() + place.getOsmType()).equals(currentPlace.getOsmId() + currentPlace.getOsmType());
			if (placeIsDifferentFromTheLast) {
				place.setTags(Lists.newArrayList(currentTags));
				places.add(place);

				currentPlace = place;
				currentTags = Sets.newHashSet();

			} else {
				currentTags.addAll(place.getTags());
			}

			if (places.size() == COMMIT_SIZE) {
				index(places);
				
				Duration duration = new Duration(countStart.getMillis(), DateTime.now().getMillis());
				log.info("Imported " + COMMIT_SIZE + " in " + duration.getMillis());
				places = Lists.newArrayList();
				countStart = DateTime.now();
			}
		}

		currentPlace.setTags(Lists.newArrayList(currentTags));
		places.add(currentPlace);
		if (!places.isEmpty()) {
			index(places);
		}
		
		log.info("Import completed");
	}

	public void index(List<Place> places) {
		log.info("Importing updates");

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Place place : places) {
			bulkRequest.add(client.prepareIndex(writeIndex, TYPE, place.getOsmId() + place.getOsmType()).setSource(jsonSerializer.serialize(place)));
		}
		bulkRequest.execute().actionGet();
		log.info("Update submitted");
	}

}