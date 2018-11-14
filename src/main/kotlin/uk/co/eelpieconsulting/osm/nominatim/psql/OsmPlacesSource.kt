package uk.co.eelpieconsulting.osm.nominatim.psql

import org.apache.log4j.Logger
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

class OsmPlacesSource(val osmDAO: OsmDAO, val placeRowParser: PlaceRowParser, val cursor: (Long, Long) -> ResultSet) {

    private val log = Logger.getLogger(OsmPlacesSource::class.java)

    private val PAGE_SIZE = 1000L

    private var start = 0L
    private var currentPage = newPage()
    private var readFromCurrentPage = 0L
    private var hasNext: Boolean = false

    fun hasNext(): Boolean {
        return hasNext
    }

    fun newPage(): ResultSet {
        currentPage = cursor(start, PAGE_SIZE)
        readFromCurrentPage = 0L
        hasNext = currentPage.next()
        return currentPage
    }

    fun next(): Place {
        val place = placeRowParser.buildPlaceFromCurrentRow(currentPage)
        readFromCurrentPage = readFromCurrentPage + 1
        start = place.osmId // TODO not a perfect watermark; will jam if a single place spans more than page size row.

        hasNext = currentPage.next()

        if (!hasNext) {
            val shouldPaginate = readFromCurrentPage == PAGE_SIZE
            if (shouldPaginate) {
                currentPage = newPage()
            }
        }

        return place
    }

}