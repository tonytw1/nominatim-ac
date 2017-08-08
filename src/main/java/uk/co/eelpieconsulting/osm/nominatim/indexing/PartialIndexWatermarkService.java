package uk.co.eelpieconsulting.osm.nominatim.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory;
import uk.co.eelpieconsulting.osm.nominatim.views.json.JsonSerializer;

import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Component
public class PartialIndexWatermarkService {

	private static final String WATERMARK = "watermark";

	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final JsonSerializer jsonSerializer = new JsonSerializer();	// TODO This is a bad use of a view package; get your own one!
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final String writeIndex;

	@Autowired
	public PartialIndexWatermarkService(ElasticSearchClientFactory elasticSearchClientFactory, @Value("${elasticsearch.index.write}") String writeIndex) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.writeIndex = writeIndex;
	}

	public DateTime getWatermark() {
		SearchRequestBuilder request = elasticSearchClientFactory.getClient().prepareSearch(writeIndex).setTypes(WATERMARK).setQuery(boolQuery()).setSize(1);
		SearchResponse searchResponse = request.get();
		if (searchResponse.getHits().getTotalHits() == 0) {
			return null;
		}

		try {
			JsonNode asJson = objectMapper.readTree(searchResponse.getHits().getAt(0).sourceAsString());
			return new DateTime(asJson.get(WATERMARK).asLong());
			
		} catch (Exception e) {
			throw new RuntimeException(e);	
		}
	}

	public void setWatermark(DateTime watermark) {
		final Client client = elasticSearchClientFactory.getClient();

		Map<String, Object> map = Maps.newHashMap();
		map.put("watermark", Long.toString(watermark.getMillis()));
		String json = jsonSerializer.serialize(map);

		try {
			IndexRequest indexRequest = new IndexRequest().index(writeIndex).type(WATERMARK).id("1").source(json);
			client.index(indexRequest).get();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
