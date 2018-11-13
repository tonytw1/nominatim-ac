package uk.co.eelpieconsulting.osm.nominatim.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Component
public class PartialIndexWatermarkService {

  private static final String WATERMARK = "watermark";

  private final ElasticSearchClientFactory elasticSearchClientFactory;
  private final JsonSerializer jsonSerializer = new JsonSerializer();  // TODO This is a bad use of a view package; get your own one!
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final String writeIndex;

  @Autowired
  public PartialIndexWatermarkService(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.write}") String writeIndex) {
    this.elasticSearchClientFactory = elasticSearchClientFactory;
    this.writeIndex = writeIndex;
  }

  public DateTime getWatermark() throws IOException {
    SearchRequest searchRequest = new SearchRequest(writeIndex);
    searchRequest.types(WATERMARK);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQuery());
    searchSourceBuilder.size(1);
    searchRequest.source(searchSourceBuilder);

    SearchResponse searchResponse = elasticSearchClientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
    if (searchResponse.getHits().getTotalHits() == 0) {
      return null;
    }

    try {
      JsonNode asJson = objectMapper.readTree(searchResponse.getHits().getAt(0).getSourceAsString());
      return new DateTime(asJson.get(WATERMARK).asLong());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setWatermark(DateTime watermark) {
    final RestHighLevelClient client = elasticSearchClientFactory.getClient();

    Map<String, Object> map = Maps.newHashMap();
    map.put("watermark", Long.toString(watermark.getMillis()));
    String json = jsonSerializer.serialize(map);

    try {
      IndexRequest indexRequest = new IndexRequest().index(writeIndex).type(WATERMARK).id("1").source(json);
      client.index(indexRequest);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
