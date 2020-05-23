package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.*;
import uk.co.eelpieconsulting.osm.nominatim.json.JsonDeserializer;
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
public class ElasticSearchAutoCompleteService {

  private static final String SEARCH_TYPE = "places";  // TODO ElasticSearchIndexer.TYPE;

  private static final String ADDRESS = "address";
  private static final String DEFAULT_RADIUS = "100km";
  private static final String LATLONG = "latlong";
  private static final String TAGS = "tags";

  private final ElasticSearchClientFactory elasticSearchClientFactory;
  private final JsonDeserializer jsonDeserializer;
  private final String readIndex;

  private final List<Profile> availableProfiles;

  @Autowired
  public ElasticSearchAutoCompleteService(ElasticSearchClientFactory elasticSearchClientFactory, JsonDeserializer jsonDeserializer,
                                          @Value("${elasticsearch.index.read}") String readIndex) {
    this.elasticSearchClientFactory = elasticSearchClientFactory;
    this.jsonDeserializer = jsonDeserializer;
    this.readIndex = readIndex;

    this.availableProfiles = Lists.newArrayList();
    availableProfiles.add(new Country());
    availableProfiles.add(new CountryCityTownSuburb());
    availableProfiles.add(new CountryStateCity());
    availableProfiles.add(new Everything());
  }

  public List<Profile> getAvailableProfiles() {
    return availableProfiles;
  }

  public List<DisplayPlace> search(String q, String tag, Double lat, Double lon, Double radius, Integer rank, String country, String profileName) throws IOException {
    if (Strings.isNullOrEmpty(q)) {
      return Lists.newArrayList();
    }

    Profile profile = new Everything();
    for (Profile p : availableProfiles) {
      if (p.getName().equals(profileName)) {
        profile = p;
      }
    }

    BoolQueryBuilder query = profile.getQuery();
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
      GeoDistanceQueryBuilder geoCircle = geoDistanceQuery(LATLONG).point(lat, lon).distance(distance);
      query = query.must(boolQuery().must(geoCircle));
    }

    return executeAndParse(query);
  }

  public long indexedItemsCount() throws IOException {
    final BoolQueryBuilder all = boolQuery();

    SearchRequest searchRequest = new SearchRequest(readIndex);
    searchRequest.types(SEARCH_TYPE);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(all);
    searchSourceBuilder.size(0);
    searchRequest.source(searchSourceBuilder);

    RestHighLevelClient client = elasticSearchClientFactory.getClient();
    SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT.DEFAULT);

    return response.getHits().totalHits;
  }

  private List<DisplayPlace> executeAndParse(QueryBuilder query) throws IOException {
    RestHighLevelClient client = elasticSearchClientFactory.getClient();

    SearchRequest searchRequest = new SearchRequest(readIndex);
    searchRequest.types(SEARCH_TYPE);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);
    searchSourceBuilder.sort(SortBuilders.fieldSort("adminLevel").order(SortOrder.ASC)).sort(SortBuilders.fieldSort("addressRank").order(SortOrder.ASC));
    searchRequest.source(searchSourceBuilder);

    SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT.DEFAULT);

    List<DisplayPlace> places = Lists.newArrayList();
    for (int i = 0; i < response.getHits().getHits().length; i++) {
      SearchHit searchHit = response.getHits().getHits()[i];


      Place place = jsonDeserializer.deserializePlace(searchHit.getSourceAsString());

      places.add(new DisplayPlace(place.getOsmId(), place.getOsmType(), place.getAddress(), place.getClassification(),
              place.getType(), place.getLatlong(), place.getCountry(), place.getType(),
              place.getAdminLevel(), place.getAddressRank()));
    }

    return places;
  }

  private PrefixQueryBuilder startsWith(String q) {
    return prefixQuery(ADDRESS, q.toLowerCase());
  }

}