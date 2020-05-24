package uk.co.eelpieconsulting.osm.nominatim.psql

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

class OsmPlacesSourceTest {

    private val DATABASE_HOST = "localhost:6432"  // TODO inject
    private val DATABASE_USER = "www-data"
    private val DATABASE_PASSWORD = ""

    private var osmDAO = OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST)
    private var placeRowParser = PlaceRowParser()

    @Test
    fun canRetrieveSingleRowPlace() {
        val twickenham = 21099166L

        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(twickenham, "N")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)

        val firstRow = found.first()
        assertEquals("Twickenham", firstRow.name)
        assertEquals("Twickenham, London Borough of Richmond upon Thames, London, Greater London, England, TW1 3RZ, United Kingdom", firstRow.address)
        assertEquals("place", firstRow.classification)
        assertEquals("suburb", firstRow.type)
        assertEquals(15, firstRow.adminLevel) // Admin levels increase with depth; countries are highest
        assertEquals(20, firstRow.addressRank) // This is an address rank; higher numbers will present further to the left in an en address
    }

    @Test
    fun somePlacesHaveNoName() {
        val coleCourtBuilding = 28431475L

        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(coleCourtBuilding, "W")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)

        val firstRow = found.first()
        assertNull(firstRow.name, "Some places have no name")
        assertEquals("Twickenham, London Borough of Richmond upon Thames, London, Greater London, England, TW1 1HD, United Kingdom",
                firstRow.address, "Places with no name still have an address; and this can end up been quite high ranking")

        assertEquals(15, firstRow.adminLevel)
        assertEquals(5, firstRow.addressRank)
    }

    @Test
    fun countriesHaveLowerAdminLevels() {
        val england = 58447L

        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(england, "R")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)

        val firstRow = found.first()
        assertEquals("England", firstRow.name)
        assertEquals(4, firstRow.adminLevel)
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
            osmPlacesSource.next()
            rowsIterated++
        }

        assertEquals(recordCountKnownToExceedPaginationSize, rowsIterated)
    }

}