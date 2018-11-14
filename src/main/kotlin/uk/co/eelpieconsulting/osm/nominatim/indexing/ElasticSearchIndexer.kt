package uk.co.eelpieconsulting.osm.nominatim.indexing

import com.google.common.base.Splitter
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceExtractor
import java.io.IOException

@Component
class ElasticSearchIndexer @Autowired
constructor(elasticSearchClientFactory: ElasticSearchClientFactory,
            val placeExtractor: PlaceExtractor,
            @param:Value("\${elasticsearch.index.write}") val writeIndex: String) {

    private val log = Logger.getLogger(ElasticSearchIndexer::class.java)

    private val jsonSerializer: JsonSerializer = JsonSerializer()
    private val client = elasticSearchClientFactory.client

    val TYPE = "places"
    private val COMMIT_SIZE = 1000
    private val TAG_PREFIXES_WHICH_DO_NOT_NEED_TO_BE_INDEXED = Sets.newHashSet("population", "wikipedia", "wikidata", "website")
    private val pipeSplitter = Splitter.on("|")

    @Throws(IOException::class)
    fun indexLines(osmPlacesSource: OsmPlacesSource) {
        log.info("Importing records")
        //if (!Strings.isNullOrEmpty(place.name)) {  // Discard entires with not specifc name TODO why not do this at sql select time?

        fun filterTags(tags: Set<String>): List<String> {
            return tags.filter { t ->
                val split = pipeSplitter.split(t).iterator()
                if (split.hasNext()) {
                    val prefix = split.next()
                    !TAG_PREFIXES_WHICH_DO_NOT_NEED_TO_BE_INDEXED.contains(prefix)
                } else {
                    false
                }
            }
        }

        var places = emptyList<Place>()
        var countStart = DateTime.now()

        fun indexPlaces(newPlace: Place) {
            places += newPlace.copy(tags = filterTags(newPlace.tags.toSet()))
            if (places.size == COMMIT_SIZE) {
                index(places)

                val duration = Duration(countStart.millis, DateTime.now().millis)
                log.info("Indexed " + COMMIT_SIZE + " in " + duration.millis)
                places = emptyList()
                countStart = DateTime.now()
            }
        }

        placeExtractor.extractPlaces(osmPlacesSource, ::indexPlaces)

        if (!places.isEmpty()) {
            index(places)
        }

        log.info("Import completed")
    }

    fun index(places: List<Place>) {
        if (!places.isEmpty()) {
            log.info("Indexing places")
            val bulkRequest = BulkRequest()
            places.forEach { p ->
                bulkRequest.add(IndexRequest(writeIndex, TYPE, p.osmId.toString() + p.osmType).
                        source(jsonSerializer.serialize(p), XContentType.JSON))
            }
            client.bulk(bulkRequest)
        }
    }

}