package uk.co.eelpieconsulting.osm.nominatim.indexing

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.Strings
import org.elasticsearch.common.xcontent.XContentType
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchClientFactory
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import java.io.IOException

@Component
class ElasticSearchIndexer @Autowired
constructor(elasticSearchClientFactory: ElasticSearchClientFactory, @param:Value("\${elasticsearch.index.write}") private val writeIndex: String) {

    private val log = Logger.getLogger(ElasticSearchIndexer::class.java)

    private val jsonSerializer: JsonSerializer = JsonSerializer()
    private val client: RestHighLevelClient = elasticSearchClientFactory.client

    val TYPE = "places"
    private val COMMIT_SIZE = 1000
    private val TAG_PREFIXES_WHICH_DO_NOT_NEED_TO_BE_INDEXED = Sets.newHashSet("population", "wikipedia", "wikidata", "website")
    private val pipeSplitter = Splitter.on("|")

    @Throws(IOException::class)
    fun indexLines(osmPlacesSource: OsmPlacesSource) {
        log.info("Importing records")

        var places: MutableList<Place> = Lists.newArrayList()
        var countStart = DateTime.now()

        fun onNewPlace(newPlace: Place) {
            val filteredTags = filterTags(newPlace.tags.toSet())
            newPlace.tags = filteredTags

            places.add(newPlace)

            if (places.size == COMMIT_SIZE) {
                index(places)

                val duration = Duration(countStart.millis, DateTime.now().millis)
                log.info("Imported " + COMMIT_SIZE + " in " + duration.millis)
                places = Lists.newArrayList()
                countStart = DateTime.now()
            }
        }

        extractPlaces(osmPlacesSource, ::onNewPlace)

        if (!places.isEmpty()) {
            index(places)
        }

        log.info("Import completed")
    }

    fun extractPlaces(osmPlacesSource: OsmPlacesSource, callback: (Place) -> Unit) {
        var currentPlace: Place? = null
        var currentTags: MutableSet<String> = Sets.newHashSet()
        while (osmPlacesSource.hasNext()) {
            val place = osmPlacesSource.next()
            if (currentPlace == null) {
                currentPlace = place
            }
            currentTags.addAll(place.tags)

            val placeIsDifferentFromTheLast = place.osmId.toString() + place.osmType != currentPlace!!.osmId.toString() + currentPlace.osmType
            if (placeIsDifferentFromTheLast) {
                place.tags = currentTags.toList()
                callback(place)
                currentPlace = place
                currentTags = Sets.newHashSet()
            }
        }
        if (currentPlace != null) {
            callback(currentPlace!!)
        }
    }

    @Throws(IOException::class)
    fun index(places: List<Place>) {
        log.info("Importing updates")

        val bulkRequest = BulkRequest()
        var bulkRequestHasItems = false
        for (place in places) {
            if (!Strings.isNullOrEmpty(place.name)) {  // Discard entires with not specifc name
                val serialize = jsonSerializer.serialize(place)
                bulkRequest.add(IndexRequest(writeIndex, TYPE, place.osmId.toString() + place.osmType).source(serialize, XContentType.JSON))
                bulkRequestHasItems = true
            }
        }

        if (bulkRequestHasItems) {
            client.bulk(bulkRequest)
        }
    }

    private fun filterTags(tags: Set<String>): List<String> {
        val filtered = Sets.newHashSet<String>()
        for (t in tags) {
            val split = pipeSplitter.split(t).iterator()
            if (split.hasNext()) {
                val prefix = split.next()
                if (!TAG_PREFIXES_WHICH_DO_NOT_NEED_TO_BE_INDEXED.contains(prefix)) {
                    filtered.add(t)
                }
            }
        }
        return Lists.newArrayList(filtered)
    }
}