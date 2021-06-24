package uk.co.eelpieconsulting.osm.nominatim.psql

import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

// Given a cursor of placex rows provide an iterator of places
// Needs to account for some places been represented by multiple consecutive placex rows
class OsmPlacesSource(val placeRowParser: PlaceRowParser, val cursor: (Long, Long) -> ResultSet, val pageSize: Long) {

    private var start = 0L
    private var currentPage = newPage()
    private var readFromCurrentPage = 0L
    private var hasNext: Boolean = false

    fun hasNext(): Boolean {
        return hasNext
    }

    fun newPage(): ResultSet {
        currentPage = cursor(start, pageSize)
        readFromCurrentPage = 0L
        hasNext = currentPage.next()
        return currentPage
    }

    fun next(): Place {
        val place = placeRowParser.buildPlaceFromCurrentRow(currentPage)
        readFromCurrentPage += 1
        start = place.osmId // TODO not a perfect watermark; will jam if a single place spans more than page size row.

        hasNext = currentPage.next()
        if (!hasNext) {
            val shouldPaginate = readFromCurrentPage == pageSize
            if (shouldPaginate) {
                currentPage = newPage()
            }
        }

        return place
    }

}