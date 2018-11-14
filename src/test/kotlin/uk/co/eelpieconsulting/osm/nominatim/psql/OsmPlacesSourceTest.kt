package uk.co.eelpieconsulting.osm.nominatim.psql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

class OsmPlacesSourceTest {

    private val DATABASE_HOST = "10.0.45.11"  // TODO inject
    private val DATABASE_USER = "www-data"
    private val DATABASE_PASSWORD = ""

    private var osmDAO: OsmDAO =  OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST)
    private var placeRowParser = PlaceRowParser()

    @Test
    fun canRetrievePlacesFromRecordSet() {
        fun cursor(start: Long, pageSize: Long): ResultSet {
            return osmDAO.getPlace(4599, "R")
        }

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            val place = osmPlacesSource.next()
            found = found + place
        }

        assertEquals(2, found.size)
    }

    @Test
    fun canPaginateBeyondTheFirstPage() {
        fun cursor(start: Long, pageSize: Long): ResultSet {
            return osmDAO.getPlaces(start, pageSize, "R")
        }

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var rowsIterated = 0
        val recordCountKnownToExceedPaginationSize = 2000
        while (osmPlacesSource.hasNext() && rowsIterated < recordCountKnownToExceedPaginationSize) {
            osmPlacesSource.next();
            rowsIterated++;
        }

        assertEquals(recordCountKnownToExceedPaginationSize, rowsIterated);
    }

}