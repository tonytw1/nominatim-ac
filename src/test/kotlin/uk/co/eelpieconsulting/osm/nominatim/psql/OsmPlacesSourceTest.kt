package uk.co.eelpieconsulting.osm.nominatim.psql

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

class OsmPlacesSourceTest {

    // Default credentials for Nominatim 3.7 Docker image; nothing to see here
    private val DATABASE_HOST = "localhost:5432"  // TODO inject
    private val DATABASE_USER = "nominatim"
    private val DATABASE_PASSWORD = "qaIACxO6wMR3"

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
        val chargingStationWithNoName = 6919655077L

        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(chargingStationWithNoName, "N")

        val osmPlacesSource = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var found = emptyList<Place>()
        while (osmPlacesSource.hasNext()) {
            found = found + osmPlacesSource.next()
        }

        assertEquals(1, found.size)

        val place = found.first()
        assertNull("Some places have no name", place.name)
        assertEquals("Places with no name still have an address; and this can end up been quite high ranking which is why it's important to filter them out",
            "St Marks Gate, Fish Island, Bow, London Borough of Tower Hamlets, London, Greater London, England, E9 5HP, United Kingdom",
                place.address)

        assertEquals(15, place.adminLevel)
        assertEquals(30, place.addressRank)
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
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(16431, "R")    // Southsea castle

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
        val recordCountKnownToExceedPaginationSize = 2000
        while (osmPlacesSource.hasNext() && rowsIterated < recordCountKnownToExceedPaginationSize) {
            osmPlacesSource.next()
            rowsIterated++
        }

        assertEquals(recordCountKnownToExceedPaginationSize, rowsIterated)
    }

}