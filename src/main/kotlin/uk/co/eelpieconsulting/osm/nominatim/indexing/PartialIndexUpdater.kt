package uk.co.eelpieconsulting.osm.nominatim.indexing

import com.google.common.collect.Lists
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.json.JsonSerializer
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser
import java.io.IOException
import java.sql.SQLException

@Component
class PartialIndexUpdater @Autowired constructor(private val osmDAO: OsmDAO, private val placeRowParser: PlaceRowParser, private val elasticSearchIndexer: ElasticSearchIndexer,
                                                 private val partialIndexWatermarkService: PartialIndexWatermarkService, private val jsonSerializer: JsonSerializer) {

    private val log = Logger.getLogger(PartialIndexUpdater::class.java)
    private val COMMIT_SIZE = 1000

    @Scheduled(fixedRate = 60000)
    @Throws(SQLException::class, IOException::class)
    fun update() {
        var watermark = readPersistedWatermark()
        while (watermark.isBefore(DateTime.now().minusHours(1))) {
            log.info("Updating indexed after: $watermark")
            val countStart = DateTime.now()
            val places = osmDAO.getPlacesIndexedAfter(watermark, COMMIT_SIZE)
            var highWater = watermark
            val updates: MutableList<Place> = Lists.newArrayList()
            while (!places.isAfterLast) {
                val next = places.next()
                if (next) {
                    val place = placeRowParser.buildPlaceFromCurrentRow(places)
                    updates.add(place)
                    highWater = DateTime(places.getTimestamp("indexed_date"))
                }
            }
            elasticSearchIndexer.index(updates)
            val duration = Duration(countStart.millis, DateTime.now().millis)
            log.info("Imported " + updates.size + " partial updates in " + duration.millis)
            log.info("Setting watermark to: $highWater")
            partialIndexWatermarkService.setWatermark(highWater)
            watermark = highWater
        }
    }

    @Throws(IOException::class)
    private fun readPersistedWatermark(): DateTime {
        val persistedWaterMark = partialIndexWatermarkService.getWatermark()
        return persistedWaterMark ?: DateTime(0L)
    }

}