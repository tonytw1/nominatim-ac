package uk.co.eelpieconsulting.osm.nominatim.elasticsearch

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.indices.GetMappingsRequest
import org.elasticsearch.client.indices.GetMappingsResponse
import org.junit.Test
import java.util.*

class ElasticSearchIndexCreatorTest {

    private val elasticSearchClientFactory = ElasticSearchClientFactory("http://localhost:9200")
    private val indexCreator = ElasticSearchIndexCreator(elasticSearchClientFactory)

    @Test
    fun canCreateMissingIndex() {
        val index = "test" + UUID.randomUUID().toString()
        assertFalse(indexCreator.indexExists(index))

        indexCreator.ensureIndexExists(index)

        assertTrue(indexCreator.indexExists(index))
    }

    @Test
    fun createdIndexesShouldHaveCorrectMappings () {
        val index = "test" + UUID.randomUUID().toString()

        indexCreator.ensureIndexExists(index)

        val req = GetMappingsRequest()
        req.indices(index)

        val mappingsResponse: GetMappingsResponse = elasticSearchClientFactory.getClient().indices().getMapping(req, RequestOptions.DEFAULT)
        val indexMapping = mappingsResponse.mappings().get(index)!!.sourceAsMap()
        val indexMappingProperties = indexMapping.get("properties") as MutableMap<String, Any>

        assertTrue(indexMappingProperties.keys.contains("latlong"))
    }

}