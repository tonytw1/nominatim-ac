package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.Country;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.CountryCityTownSuburb;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.CountryStateCity;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.Profile;
import uk.co.eelpieconsulting.osm.nominatim.indexing.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
public class ElasticSearchAutoCompleteService {

	private static final String SEARCH_TYPE = ElasticSearchIndexer.TYPE;

	private static final String ADDRESS = "address";
	private static final String DEFAULT_RADIUS = "100km";
	private static final String LATLONG = "latlong";
	private static final String TAGS = "tags";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper mapper;

	private final String readIndex;

	private final List<Profile> availableProfiles;

	@Autowired
	public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.read}") String readIndex) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.readIndex = readIndex;
		this.mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		this.availableProfiles = Lists.newArrayList();
		availableProfiles.add(new Country());
		availableProfiles.add(new CountryCityTownSuburb());
		availableProfiles.add(new CountryStateCity());
	}

	public List<Profile> getAvailableProfiles() {
		return availableProfiles;
	}
	
	public List<DisplayPlace> search(String q, String tag, Double lat, Double lon, Double radius, Integer rank, String country, String profileName) {
		if (Strings.isNullOrEmpty(q)) {
			return Lists.newArrayList();
		}

		Profile profile = null;
		for(Profile p: availableProfiles) {
			if (p.getName().equals(profileName)) {
				profile = p;
			}
		}

		BoolQueryBuilder query = profile != null ? profile.getQuery(): new BoolQueryBuilder();
		query = query.must(startsWith(q));

		if (!Strings.isNullOrEmpty(tag)) {
			query = query.must(boolQuery().must(termQuery(TAGS, tag)));
		}
		if (rank != null) {
			query = query.must(boolQuery().must(termQuery("rank", rank)));
		}
		
		if (!Strings.isNullOrEmpty(country)) {
			query = query.must(termQuery("country", country));
		}
		
		if (lat != null && lon != null) {
			String distance = radius != null ? Double.toString(radius) + "km" : DEFAULT_RADIUS;
			GeoDistanceQueryBuilder geoCircle = geoDistanceQuery(LATLONG).
				lat(lat).lon(lon).
				distance(distance);
			query = query.must(boolQuery().must(geoCircle));
		}
				
		return executeAndParse(query);
	}

	public long indexedItemsCount() {
		final BoolQueryBuilder all = boolQuery();
		final SearchRequestBuilder request = elasticSearchClientFactory.getClient().prepareSearch(readIndex).
			setTypes(SEARCH_TYPE).
			setQuery(all).
			setSize(0);
	
		return request.get().getHits().getTotalHits();		
	}
	
	private List<DisplayPlace> executeAndParse(QueryBuilder query) {
		Client client = elasticSearchClientFactory.getClient();

		SearchRequestBuilder request = client.prepareSearch(readIndex).
			setTypes(SEARCH_TYPE).
			setQuery(query).
			//addFacet(tagsFacet).
			setSize(20);

		SearchResponse response = request.execute().actionGet();
		
		List<DisplayPlace> places = Lists.newArrayList();
		for (int i = 0; i < response.getHits().getHits().length; i++) {
			SearchHit searchHit = response.getHits().getHits()[i];

			try {
				Place place = mapper.readValue(searchHit.getSourceAsString(), Place.class);
				places.add(new DisplayPlace(place.getOsmId(), place.getOsmType(), place.getAddress(), place.getClassification(), place.getType(), place.getLatlong(), place.getCountry(), place.getDisplayType()));

			} catch (JsonParseException e) {
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return places;
	}
	
	private PrefixQueryBuilder startsWith(String q) {
		return prefixQuery(ADDRESS, q.toLowerCase());
	}

}