package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.AutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.collect.Lists;

@Component
public class ElasticSearchAutoCompleteService implements AutoCompleteService {

	private static final String ADDRESS = "address";
	private static final String TYPE = "type";
	private static final String CLASSIFICATION = "classification";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper mapper;
	
	@Autowired
	public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Override
	public List<Place> getSuggestionsFor(String q) {
		Client client = elasticSearchClientFactory.getClient();
		
		PrefixQueryBuilder startsWith = prefixQuery(ADDRESS, q);		
		BoolQueryBuilder query = boolQuery().must(startsWith).mustNot(unwantedTypes());
		
		SearchResponse response = client.prepareSearch().setQuery(query).execute().actionGet();
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
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "post_box"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "bench"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "recycling"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "bicycle_parking"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "parking"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "leisure")).must(termQuery(TYPE, "picnic_table"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "amenity")).must(termQuery(TYPE, "waste_basket"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "railway")).must(termQuery(TYPE, "crossing"))).
				should(boolQuery().must(termQuery(CLASSIFICATION, "highway")).must(termQuery(TYPE, "bus_stop")));
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

}
