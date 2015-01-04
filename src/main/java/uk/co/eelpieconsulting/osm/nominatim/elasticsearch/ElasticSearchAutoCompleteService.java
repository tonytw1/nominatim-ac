package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.AutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

@Component
public class ElasticSearchAutoCompleteService implements AutoCompleteService {

	private static final String ADDRESS = "address";
	private static final String CLASSIFICATION = "classification";
	private static final String DEFAULT_RADIUS = "100km";
	private static final String LATLONG = "latlong";
	private static final String TAGS = "tags";
	private static final String TYPE = "type";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper mapper;
	
	@Autowired
	public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Override
	public List<Place> getSuggestionsFor(String q) {
		PrefixQueryBuilder startsWith = startsWith(q);
		
		BoolQueryBuilder isCountry = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "country"));
		BoolQueryBuilder isCity = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "city"));		
		BoolQueryBuilder isTown = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "town"));
		
		BoolQueryBuilder query = boolQuery().
				must(startsWith).
				mustNot(unwantedTypes()).
				should(isCountry).boost(10).
				should(isCity).boost(5).
				should(isTown).boost(3);
		
		return executeAndParse(query, null);
	}
	
	@Override
	public List<Place> search(String q, String tag, Double lat, Double lon, Double radius) {
		BoolQueryBuilder query = boolQuery();
		if (!Strings.isNullOrEmpty(q)) {
			query = query.must(startsWith(q));
		}
		if (!Strings.isNullOrEmpty(tag)) {
			query = query.must(boolQuery().must(termQuery(TAGS, tag)));
		}
		
		FilterBuilder filter = null;
		if (lat != null && lon != null) {
			String distance = radius != null ? Double.toString(radius) + "km" : DEFAULT_RADIUS;
			filter = FilterBuilders.geoDistanceFilter(LATLONG).
				lat(lat).lon(lon).
				distance(distance);
		}
		
		return executeAndParse(query, filter);
	}
	
	private List<Place> executeAndParse(QueryBuilder query, FilterBuilder filter) {
		Client client = elasticSearchClientFactory.getClient();

		SearchResponse response = client.prepareSearch().
			setQuery(query).
			setPostFilter(filter).		
			execute().actionGet();
		
		List<Place> places = Lists.newArrayList();
		for (int i = 0; i < response.getHits().getHits().length; i++) {
			SearchHit searchHit = response.getHits().getHits()[i];

			try {
				Place place = mapper.readValue(searchHit.getSourceAsString(), Place.class);
				places.add(place);

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
	
	private BoolQueryBuilder unwantedTypes() {		
		BoolQueryBuilder isUnwantedType = boolQuery().minimumNumberShouldMatch(1).
				should(boolQuery().must(termQuery(CLASSIFICATION, "highway")).must(termQuery(TYPE, "motorway_junction"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "highway")).must(termQuery(TYPE, "speed_camera"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "post_box"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "bench"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "recycling"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "bicycle_parking"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "bicycle_rental"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "parking"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "leisure")).must(termQuery(TYPE, "picnic_table"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "waste_basket"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "railway")).must(termQuery(TYPE, "crossing"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "highway")).must(termQuery(TYPE, "bus_stop"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "house")));
		return isUnwantedType;
	}
	
	private BoolQueryBuilder requiredTypes() {
		BoolQueryBuilder isCountry = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "country"));
		BoolQueryBuilder isCity = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "city"));		
		BoolQueryBuilder isTown = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "town"));		
		BoolQueryBuilder isSuburb = boolQuery().must(termQuery(CLASSIFICATION, "place")).must(termQuery(TYPE, "suburb"));		
		BoolQueryBuilder isRequiredType = boolQuery().minimumNumberShouldMatch(1).should(isCountry).should(isCity).should(isTown).should(isSuburb);
		return isRequiredType;
	}

	private PrefixQueryBuilder startsWith(String q) {
		return prefixQuery(ADDRESS, q);
	}

}