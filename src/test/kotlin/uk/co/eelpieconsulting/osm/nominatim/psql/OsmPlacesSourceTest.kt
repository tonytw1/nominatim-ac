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
        val winton = 17858832L

        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(winton, "N")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)
        assertEquals("Winton", found.first().name)
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
            osmPlacesSource.next();
            rowsIterated++;
        }

        assertEquals(recordCountKnownToExceedPaginationSize, rowsIterated);
    }

}