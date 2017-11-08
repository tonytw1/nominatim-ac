package uk.co.eelpieconsulting.osm.nominatim.indexing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

import java.util.List;
import java.util.Set;

@Component
public class ElasticSearchIndexer {
	
	private static final Logger log = Logger.getLogger(ElasticSearchIndexer.class);

	public static final String TYPE = "places";
	private static final int COMMIT_SIZE = 1000;
	
	private final JsonSerializer jsonSerializer;

	private final String writeIndex;

	private final Client client;

	@Autowired
	public ElasticSearchIndexer(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.write}") String writeIndex) {
		this.writeIndex = writeIndex;
		this.jsonSerializer = new JsonSerializer();
		client = elasticSearchClientFactory.getClient();
	}
	
	public void indexLines(OsmPlacesSource osmPlacesSource) {
		log.info("Importing records");
		List<Place> places = Lists.newArrayList();
		DateTime countStart = DateTime.now();

		Place currentPlace = null;
		Set<String> currentTags = Sets.newHashSet();
		while (osmPlacesSource.hasNext()) {
			Place place = osmPlacesSource.next();
			if (currentPlace == null) {
				currentPlace = place;
			}
			currentTags.addAll(place.getTags());

			boolean placeIsDifferentFromTheLast = !(place.getOsmId() + place.getOsmType()).equals(currentPlace.getOsmId() + currentPlace.getOsmType());
			if (placeIsDifferentFromTheLast) {
				place.setTags(filterTags(currentTags));
				places.add(place);

				currentPlace = place;
				currentTags = Sets.newHashSet();
			}

			if (places.size() == COMMIT_SIZE) {
				index(places);
				
				Duration duration = new Duration(countStart.getMillis(), DateTime.now().getMillis());
				log.info("Imported " + COMMIT_SIZE + " in " + duration.getMillis());
				places = Lists.newArrayList();
				countStart = DateTime.now();
			}
		}

		currentPlace.setTags(filterTags(currentTags));
		places.add(currentPlace);
		if (!places.isEmpty()) {
			index(places);
		}
		
		log.info("Import completed");
	}

	public void index(List<Place> places) {
		log.info("Importing updates");

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		boolean bulkRequestHasItems = false;
		for (Place place : places) {
			if (!Strings.isNullOrEmpty(place.getName())) {	// Discard entires with not specifc name
				bulkRequest.add(client.prepareIndex(writeIndex, TYPE, place.getOsmId() + place.getOsmType()).setSource(jsonSerializer.serialize(place)));
				bulkRequestHasItems = true;
			}
		}

		if (bulkRequestHasItems) {
			bulkRequest.execute().actionGet();
			log.info("Update submitted");
		}
	}

	private List<String> filterTags(Set<String> tags) {
		return Lists.newArrayList(tags);	// TODO drop non searchable tags to trim the index size
	}

}