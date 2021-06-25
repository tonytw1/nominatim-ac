package uk.co.eelpieconsulting.osm.nominatim.psql

import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

// Given a cursor of placex rows provide an iterator of places
// Needs to account for some places been represented by multiple consecutive placex rows
class OsmPlacesSource(val placeRowParser: PlaceRowParser, val cursor: (Long, Long) -> ResultSet, val pageSize: Long) {

    private var osmAtPageStart: Long? = null
    private var hasNext: Boolean = false
    private var currentPage = newPage()
    private var readFromCurrentPage = 0L

    fun hasNext(): Boolean = hasNext

    fun newPage(pageStart: Long = 0L): ResultSet {
        currentPage = cursor(pageStart, pageSize)
        readFromCurrentPage = 0L
        osmAtPageStart = null
        hasNext = currentPage.next()
        if (hasNext) {
            osmAtPageStart = placeRowParser.buildPlaceFromCurrentRow(currentPage).osmId
        }
        return currentPage
    }

    fun next(): Place {
        val place = placeRowParser.buildPlaceFromCurrentRow(currentPage)
        hasNext = currentPage.next()
        readFromCurrentPage += 1

        if (!hasNext) {
            // Should we paginate? Yes if we consumed a full page from this cursor
            val shouldPaginate = readFromCurrentPage == pageSize
            if (shouldPaginate) {
                // Guard against infinate loop caused when a single osm id spans more rows than the pagination page size
                // Living with this means than we don't have to sub order by placex.place_id which is not in an index with osm_type and osm_id.
                // We are living with this so that we don't have to add a new index to the Nominatim schema.
                if (place.osmId == osmAtPageStart) {
                    throw RuntimeException("OSM id ${place.osmId} spans more placex rows than the pagination page size. Increase the pagination size to allow pagination past this record.")
                }
                currentPage = newPage(place.osmId)
            } else {
                hasNext = false
            }
        }

        return place
    }

}