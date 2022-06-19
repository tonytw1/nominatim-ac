package uk.co.eelpieconsulting.osm.nominatim.elasticsearch

import org.apache.logging.log4j.LogManager
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.common.xcontent.XContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ElasticSearchIndexCreator @Autowired constructor(private val elasticSearchClientFactory: ElasticSearchClientFactory) {

    private val log = LogManager.getLogger(ElasticSearchIndexCreator::class.java)

    fun ensureIndexExists(index: String): Boolean {
        if (indexExists(index)) {
            log.info("Found existing index $index; not recreating: " + index)
            return true
        }

        // Create index
        val indexDefinitionJson = """
            {
                "settings": {
                    "index": {                    
                        "analysis": {
                            "analyzer":{
                                "startswith":{
                                    "tokenizer":"keyword",
                                    "filter":"lowercase"
                                }
                            }
                        }
                    }
                },
                "mappings": {
                    "properties" : { 
                        "address" : {
                            "type":"text",
                            "search_analyzer":"startswith",
                            "analyzer":"startswith"
                        },
                        "addressRank" : {"type" : "integer" },
                        "adminLevel" : {"type" : "integer"},
                        "tags" : {"type" : "keyword" },
                        "latlong" : {"type" : "geo_point"},
                        "country" : {"type" : "keyword" }
                    }
                }
            }
        """.trimIndent()

        val req = org.elasticsearch.client.indices.CreateIndexRequest(index)
        req.source(indexDefinitionJson, XContentType.JSON)

        val createIndexResponse = elasticSearchClientFactory.getClient().indices().create(req, RequestOptions.DEFAULT)
        log.info("Created index ${createIndexResponse.index()}")
        return true
    }

    fun indexExists(index: String): Boolean {
        val req = org.elasticsearch.client.indices.GetIndexRequest(index)
        return elasticSearchClientFactory.getClient().indices().exists(req, RequestOptions.DEFAULT)
    }

}

