package uk.co.eelpieconsulting.osm.nominatim.indexing

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory

@Component
class PartialIndexWatermarkService @Autowired constructor(private val elasticSearchClientFactory: ElasticSearchClientFactory,
                                                          @param:Value("\${elasticsearch.index.write}") private val writeIndex: String) {
    // Uses a simple watermark type to persist the current index watermark in Elasticsearch.

    private val objectMapper = ObjectMapper()

    private val WATERMARK = "watermark"

    fun getWatermark(): DateTime? {
        val searchRequest = SearchRequest(writeIndex)
        searchRequest.types(WATERMARK)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(QueryBuilders.boolQuery())
        searchSourceBuilder.size(1)
        searchRequest.source(searchSourceBuilder)

        val searchResponse = elasticSearchClientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT)
        if (searchResponse.hits.getTotalHits().value == 0L) {
            return null
        } else {
            try {
                val asJson = objectMapper.readTree(searchResponse.hits.getAt(0).sourceAsString)
                return DateTime(asJson[WATERMARK].asLong())
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    fun setWatermark(watermark: DateTime) {
        val client = elasticSearchClientFactory.getClient()
        val map: MutableMap<String, String> = Maps.newHashMap()
        map["watermark"] = java.lang.Long.toString(watermark.millis)
        val json = objectMapper.writeValueAsString(map)
        try {
            val indexRequest = IndexRequest().index(writeIndex).type(WATERMARK).id("1").source(json)
            client.index(indexRequest, RequestOptions.DEFAULT)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}