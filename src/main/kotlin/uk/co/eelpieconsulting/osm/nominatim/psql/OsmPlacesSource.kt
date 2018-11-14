package uk.co.eelpieconsulting.osm.nominatim.psql

import org.apache.log4j.Logger
import uk.co.eelpieconsulting.osm.nominatim.model.Place

import java.sql.ResultSet
import java.sql.SQLException

class OsmPlacesSource(val osmDAO: OsmDAO, val placeRowParser: PlaceRowParser, val cursor: (Long, Long) -> ResultSet) {

    private val log = Logger.getLogger(OsmPlacesSource::class.java)

    private val PAGE_SIZE = 1000L

    private var start: Long = 0
    private var currentPage: ResultSet = prepare(start, PAGE_SIZE)

    private fun prepare(start: Long, stepSize: Long): ResultSet {
        return cursor(start, stepSize)
    }

    fun hasNext(): Boolean {
        return !currentPage.isLast()    // TODO breaks pagination
    }

    fun next(): Place {
        try {
            if (currentPage.isLast) {
                log.debug("After last; preparing again")
                this.currentPage = prepare(start, PAGE_SIZE)
            }
            currentPage.next()

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }

        try {
            val place = placeRowParser.buildPlaceFromCurrentRow(currentPage)
            start = place.osmId
            return place

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

}