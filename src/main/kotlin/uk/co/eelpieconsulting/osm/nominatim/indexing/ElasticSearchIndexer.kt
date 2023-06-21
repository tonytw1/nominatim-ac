package uk.co.eelpieconsulting.osm.nominatim.indexing

import com.google.common.base.Splitter
import com.google.common.collect.Sets
import joptsimple.internal.Strings
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.xcontent.XContentType
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexCreator
import uk.co.eelpieconsulting.osm.nominatim.json.JsonSerializer
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceExtractor
import java.io.IOException

@Component
class ElasticSearchIndexer @Autowired
constructor(elasticSearchClientFactory: ElasticSearchClientFactory,
            val indexCreator: ElasticSearchIndexCreator,
            val placeExtractor: PlaceExtractor, val jsonSerializer: JsonSerializer,
            @param:Value("\${elasticsearch.index.write}") val writeIndex: String) {

    private val log = LogManager.getLogger(ElasticSearchIndexer::class.java)

    private val client = elasticSearchClientFactory.getClient()

    private val elasticSearchCommitSize = 10000
    private val tagPrefixesWhichDoNotNeedToBeIndexed = Sets.newHashSet("population", "wikipedia", "wikidata", "website")
    private val pipeSplitter = Splitter.on("|")

    @Throws(IOException::class)
    fun indexLines(osmPlacesSource: OsmPlacesSource) {
        log.info("Importing records")

        indexCreator.ensureIndexExists(writeIndex)

        fun filterTags(tags: Set<String>): List<String> {
            return tags.filter { t ->
                val split = pipeSplitter.split(t).iterator()
                if (split.hasNext()) {
                    val prefix = split.next()
                    !tagPrefixesWhichDoNotNeedToBeIndexed.contains(prefix)
                } else {
                    false
                }
            }
        }

        var places = mutableListOf<Place>()
        var countStart = DateTime.now()

        fun indexPlace(newPlace: Place) {
            places.add(newPlace.copy(tags = filterTags(newPlace.tags.toSet())))
            if (places.size == elasticSearchCommitSize) {
                index(places)

                val duration = Duration(countStart.millis, DateTime.now().millis)
                val rate = elasticSearchCommitSize / duration.standardSeconds
                log.info("Indexed " + elasticSearchCommitSize + " in " + duration.millis + "ms at " + rate + " per second")
                places = mutableListOf()
                countStart = DateTime.now()
            }
        }

        placeExtractor.extractPlaces(osmPlacesSource, ::indexPlace)

        if (places.isNotEmpty()) {
            index(places)
        }

        log.info("Import completed")
    }

    fun index(places: List<Place>) {
        val indexablePlaces = places.filter { p ->
            // Places with no name tend to take the address of their parent which causes clauses
            !Strings.isNullOrEmpty(p.name)
        }

        if (indexablePlaces.isNotEmpty()) {
            log.info("Indexing places")
            val bulkRequest = BulkRequest()
            indexablePlaces.forEach { p ->
                if (!Strings.isNullOrEmpty(p.name)) {
                    val id = p.osmId.toString() + p.osmType
                    val source = IndexRequest(writeIndex).id(id).source(jsonSerializer.serializePlace(p), XContentType.JSON)
                    bulkRequest.add(source)
                }
            }
            client.bulk(bulkRequest, RequestOptions.DEFAULT)
        }
    }

}