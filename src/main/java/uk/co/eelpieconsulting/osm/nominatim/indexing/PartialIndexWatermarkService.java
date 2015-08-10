package uk.co.eelpieconsulting.osm.nominatim.indexing;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.views.json.JsonSerializer;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;

import com.google.common.collect.Maps;

@Component
public class PartialIndexWatermarkService {

	private static final String WATERMARK = "watermark";

	private ElasticSearchClientFactory elasticSearchClientFactory;

	@Autowired
	public PartialIndexWatermarkService(ElasticSearchClientFactory elasticSearchClientFactory) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
	}

	public DateTime getWatermark() {
		SearchRequestBuilder request = elasticSearchClientFactory.getClient().prepareSearch(ElasticSearchIndexer.INDEX).setTypes(WATERMARK).setQuery(boolQuery()).setSize(1);
		SearchResponse searchResponse = request.get();
		if (searchResponse.getHits().getTotalHits() == 0) {
			return null;
		}

		return null;	// TODO
	}

	public void setWatermark(DateTime watermark) {
		final Client client = elasticSearchClientFactory.getClient();

		Map<String, Object> map = Maps.newHashMap();
		map.put("watermark", Long.toString(watermark.getMillis()));
		String json = new JsonSerializer().serialize(map);

		try {
			IndexRequest indexRequest = new IndexRequest().index(ElasticSearchIndexer.INDEX).type(WATERMARK).id("1").source(json);
			client.index(indexRequest).get();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
