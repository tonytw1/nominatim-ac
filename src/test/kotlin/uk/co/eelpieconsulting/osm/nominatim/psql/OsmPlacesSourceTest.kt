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
    fun canRetrieveSingleRowPlace() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(11, "R")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)
    }

    @Test
    fun canRetrieveRowsForMultiRowPlaces() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(4599, "R")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(2, found.size)
    }

    @Test
    fun canPaginateBeyondTheFirstPage() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlaces(start, pageSize, "R")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var rowsIterated = 0
        val recordCountKnownToExceedPaginationSize = 20
        while (osmPlacesSource.hasNext() && rowsIterated < recordCountKnownToExceedPaginationSize) {
            val next = osmPlacesSource.next();
            System.out.println(next.osmId)
            rowsIterated++;
        }

        assertEquals(recordCountKnownToExceedPaginationSize, rowsIterated);
    }

}