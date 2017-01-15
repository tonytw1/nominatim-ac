package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
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
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
public class ElasticSearchAutoCompleteService {

	public static final String COUNTRY_CITY_TOWN_SUBURB = "countryCityTownSuburb";
	
	private static final Logger log = Logger.getLogger(ElasticSearchAutoCompleteService.class);
	
	private static final String COUNTRY = "country";
	private static final String SEARCH_TYPE = ElasticSearchIndexer.TYPE;

	private static final String ADDRESS = "address";
	private static final String DEFAULT_RADIUS = "100km";
	private static final String LATLONG = "latlong";
	private static final String TAGS = "tags";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper mapper;

	private final String readIndex;
	
	@Autowired
	public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.read}") String readIndex) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.readIndex = readIndex;
		this.mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public List<Place> search(String q, String tag, Double lat, Double lon, Double radius, Integer rank, String country, String profile) {
		if (Strings.isNullOrEmpty(q)) {
			return Lists.newArrayList();
		}
		
		BoolQueryBuilder query = boolQuery();
		query = query.must(startsWith(q));
		
		if (COUNTRY_CITY_TOWN_SUBURB.equals(profile)) {			
			query.must(taggedAsCountryCityTownSuburb());
		}
		if (COUNTRY.equals(profile)) {			
			query.must(taggedAsCountry());
		}
		
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
	
	@Deprecated
	public List<Place> getSuggestionsFor(String q) {
		log.info("Finding sugestions for: " + q);
		return search(q, null, null, null, null, null, null, COUNTRY_CITY_TOWN_SUBURB);
	}
	
	public long indexedItemsCount() {
		final BoolQueryBuilder all = boolQuery();
		final SearchRequestBuilder request = elasticSearchClientFactory.getClient().prepareSearch(readIndex).
			setTypes(SEARCH_TYPE).
			setQuery(all).
			setSize(0);
	
		return request.get().getHits().getTotalHits();		
	}
	
	private List<Place> executeAndParse(QueryBuilder query) {
		Client client = elasticSearchClientFactory.getClient();

		SearchRequestBuilder request = client.prepareSearch(readIndex).
			setTypes(SEARCH_TYPE).
			setQuery(query).
			//addFacet(tagsFacet).
			setSize(20);

		SearchResponse response = request.execute().actionGet();
		
		List<Place> places = Lists.newArrayList();
		for (int i = 0; i < response.getHits().getHits().length; i++) {
			SearchHit searchHit = response.getHits().getHits()[i];

			try {
				places.add(mapper.readValue(searchHit.getSourceAsString(), Place.class));

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
	
	private BoolQueryBuilder taggedAsCountry() {
		QueryBuilder isCountry = termQuery(TAGS, "place|country");
		return boolQuery().minimumNumberShouldMatch(1).should(isCountry);
	}
	
	private BoolQueryBuilder taggedAsCountryCityTownSuburb() {
		QueryBuilder isCountry = termQuery(TAGS, "place|country");
		QueryBuilder isCity = termQuery(TAGS, "place|city");	
		QueryBuilder isCounty = termQuery(TAGS, "place|county");
		QueryBuilder isTown = termQuery(TAGS, "place|town");
		QueryBuilder isSuburb = termQuery(TAGS, "place|suburb");
		QueryBuilder isNationalPark = termQuery(TAGS, "boundary|national_park");
		QueryBuilder isLeisurePark = termQuery(TAGS, "leisure|npark");
		QueryBuilder isPeak = termQuery(TAGS, "natural|peak");
		QueryBuilder isVillage = termQuery(TAGS, "place|village");
		QueryBuilder isBoundary = termQuery(TAGS, "boundary|administrative");
		QueryBuilder isAdminLevelSix = termQuery("adminLevel", "6");
		QueryBuilder isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix);

		return boolQuery().minimumNumberShouldMatch(1).
			should(isCountry).boost(10).
			should(isCity).boost(8).
			should(isNationalPark).boost(8).
			should(isAdminLevelSixBoundary).boost(5).
			should(isCounty).boost(4).
			should(isTown).boost(3).
			should(isPeak).boost(3).
			should(isLeisurePark).
			should(isVillage).
			should(isSuburb);
	}
	
}