package uk.co.eelpieconsulting.osm.nominatim.elasticsearch

import org.apache.http.HttpHost
import org.apache.log4j.Logger
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.UnknownHostException

@Component
class ElasticSearchClientFactory @Autowired constructor(
        @param:Value("\${elasticsearch.host}") private val host: String,
        @param:Value("\${elasticsearch.port}") private val port: Int) {

    private val log = Logger.getLogger(ElasticSearchClientFactory::class.java)

    private var client: RestHighLevelClient? = null

    @Synchronized
    fun getClient(): RestHighLevelClient {
        if (client == null) {
            client = try {
                connectToCluster()
            } catch (e: UnknownHostException) {
                throw RuntimeException(e)
            }
        }
        return client!!
    }

    @Throws(UnknownHostException::class)
    private fun connectToCluster(): RestHighLevelClient {
        if (client == null) {
            log.info("Setting up elastic rest client with host and port: $host:$port")
            val restClient = RestClient.builder(HttpHost(host, port, "http"))
            client = RestHighLevelClient(restClient)
        }
        return client!!
    }

}