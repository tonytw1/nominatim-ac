package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Strings;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class ElasticSearchAutoCompleteService {

	public static final String COUNTRY_CITY_TOWN_SUBURB = "countryCityTownSuburb";
	
	private static final Logger log = Logger.getLogger(ElasticSearchAutoCompleteService.class);
	
	private static final String COUNTRY = "country";
	private static final String SEARCH_INDEX = ElasticSearchIndexer.READ_INDEX;
	private static final String SEARCH_TYPE = ElasticSearchIndexer.TYPE;

	private static final String ADDRESS = "address";
	private static final String DEFAULT_RADIUS = "100km";
	private static final String LATLONG = "latlong";
	private static final String TAGS = "tags";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper mapper;
	
	@Autowired
	public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
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
		
		BoolFilterBuilder filter = FilterBuilders.boolFilter();
		if (!Strings.isNullOrEmpty(country)) {
			filter = filter.must(termFilter("country", country));
		}
		
		if (lat != null && lon != null) {
			String distance = radius != null ? Double.toString(radius) + "km" : DEFAULT_RADIUS;
			FilterBuilder geoCircle = FilterBuilders.geoDistanceFilter(LATLONG).
				lat(lat).lon(lon).
				distance(distance);
			filter = filter.must(geoCircle);
		}
				
		return executeAndParse(query, filter);
	}
	
	@Deprecated
	public List<Place> getSuggestionsFor(String q) {
		log.info("Finding sugestions for: " + q);
		return search(q, null, null, null, null, null, null, COUNTRY_CITY_TOWN_SUBURB);
	}
	
	public long indexedItemsCount() {
		final BoolQueryBuilder all = boolQuery();
		final SearchRequestBuilder request = elasticSearchClientFactory.getClient().prepareSearch(SEARCH_INDEX).
			setTypes(SEARCH_TYPE).
			setQuery(all).
			setSize(0);
	
		return request.get().getHits().getTotalHits();		
	}
	
	private List<Place> executeAndParse(QueryBuilder query, BoolFilterBuilder filter) {
		Client client = elasticSearchClientFactory.getClient();
		
        TermsFacetBuilder tagsFacet = FacetBuilders.termsFacet(TAGS).fields(TAGS).order(ComparatorType.COUNT).size(Integer.MAX_VALUE);
		
		SearchRequestBuilder request = client.prepareSearch(SEARCH_INDEX).
			setTypes(SEARCH_TYPE).
			setQuery(query).
			addFacet(tagsFacet).
			setSize(20);
		
		if (filter != null && filter.hasClauses()) {
			request = request.setPostFilter(filter);
		}
		
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
		
		Map<String, Facet> facets = response.getFacets().facetsAsMap();
		if (facets.containsKey(TAGS)) {
			TermsFacet facet = (TermsFacet) facets.get(TAGS);
			 final Map<String, Long> facetMap = Maps.newHashMap();
             for (Entry entry : (List<? extends Entry>) facet.getEntries()) {
            	 facetMap.put(entry.getTerm().string(), new Long(entry.getCount()));
             }
             
             List<String> keySet = Lists.newArrayList(facetMap.keySet());
             java.util.Collections.sort(keySet);            
		}		
		return places;
	}
	
	private PrefixQueryBuilder startsWith(String q) {
		return prefixQuery(ADDRESS, q.toLowerCase());
	}
	
	private BoolQueryBuilder taggedAsCountry() {
		QueryBuilder isCountry = termQuery(TAGS, "place|country");
		BoolQueryBuilder isRequiredType = boolQuery().minimumNumberShouldMatch(1).should(isCountry);
		return isRequiredType;
	}
	
	private BoolQueryBuilder taggedAsCountryCityTownSuburb() {
		QueryBuilder isCountry = termQuery(TAGS, "place|country");
		QueryBuilder isCity = termQuery(TAGS, "place|city");	
		QueryBuilder isCounty = termQuery(TAGS, "place|county");
		QueryBuilder isTown = termQuery(TAGS, "place|town");
		QueryBuilder isSuburb = termQuery(TAGS, "place|suburb");
		QueryBuilder isBoundary = termQuery(TAGS, "boundary|administrative");
		QueryBuilder isAdminLevelSix = termQuery("adminLevel", "6");
		QueryBuilder isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix);
				
		BoolQueryBuilder isRequiredType = boolQuery().minimumNumberShouldMatch(1).
			should(isCountry).boost(10).
			should(isCity).boost(5).
			should(isAdminLevelSixBoundary).boost(5).
			should(isCounty).boost(4).
			should(isTown).boost(3).
			should(isSuburb);
		return isRequiredType;
	}
	
}