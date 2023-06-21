package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Lists
import org.junit.Assert.*
import org.junit.Test

import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.lang.RuntimeException
import java.sql.ResultSet

class PlaceExtractorTest {

    // Default credentials for Nominatim 3.7 Docker image; nothing to see here
    private val databaseHost = "localhost:5432"  // TODO inject
    private val databaseUser = "nominatim"
    private val databasePassword = "qaIACxO6wMR3"

    private var osmDAO: OsmDAO = OsmDAO(databaseUser, databasePassword, databaseHost)
    private var placeRowParser = PlaceRowParser()

    private val limitMuchLargeThanExpectedNumberOfRows= 1000L

    @Test
    fun canExtractMultiRowPlace() {
        // For reasons unknown around 0.3% of the placex row in the Great Britain extract contain places which
        // span more than 1 row. This is probably true for the whole world and needs to be accounted for when reading places
        /*
            SELECT osm_id, osm_type, count(*)
            FROM
            placex
            GROUP BY (osm_id, osm_type)
            HAVING count(*) > 1
        */
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(1618450, "R", pageSize) // The White Horse, Wessex

        val source = OsmPlacesSource(placeRowParser, ::cursor, limitMuchLargeThanExpectedNumberOfRows)

        val places = emptyList<Place>().toMutableList()
        fun collectPages(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPages)

        assertEquals(1, places.size)
        val first = places.first()
        // The placex.type and placex.class fields from all duplicate rows should contribute to the this place's tags.
        assertTrue(first.tags.contains("man_made|geoglyph"))
        assertTrue(first.tags.contains("natural|bare_rock"))
    }

    @Test
    fun shouldErrorWhenPlaceSpansMoreRowsThanThePageSize() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlace(1618450, "R", pageSize) // The White Horse, Wessex

        val source = OsmPlacesSource(placeRowParser, ::cursor, 1)


        val places = emptyList<Place>().toMutableList()
        fun collectPages(place: Place) {
            places += place
        }

        try {
            PlaceExtractor().extractPlaces(source, ::collectPages)
        } catch (e: RuntimeException) {
            assertEquals("OSM id 1618450 spans more placex rows than the pagination page size. Increase the pagination size to allow pagination past this record.", e.message)
            return
        }

        fail("Expected an exception")
    }

    @Test
    fun placesWithMultipleRowsShouldCaptureMultipleTypesAndCategories() {
        val r = osmDAO.getPlace(16431, "R", limitMuchLargeThanExpectedNumberOfRows) // Southsea castle

        val types = Lists.newArrayList<String>()
        val categories = Lists.newArrayList<String>()
        while (r.next()) {
            val place = placeRowParser.buildPlaceFromCurrentRow(r)
            types.add(place.type)
            categories.add(place.classification)
        }

        assertEquals(2, types.size)
        assertEquals(Lists.newArrayList("museum", "castle"), types)
        assertEquals(Lists.newArrayList("tourism", "historic"), categories)
    }

    @Test
    fun placeTagsShouldIncludeExtraTags() {
        fun cursor(start: Long, pageSize: Long) = osmDAO.getPlace(284926920, "W", pageSize)   // Twickenham Rowing club

        val source = OsmPlacesSource(placeRowParser, ::cursor, limitMuchLargeThanExpectedNumberOfRows)

        val places = emptyList<Place>().toMutableList()
        fun collectPages(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPages)

        assertEquals(1, places.size)
        val place = places.first()
        assertTrue(place.tags.contains("sport|rowing"))
    }

    @Test
    fun correctExtractsSequenceOfPlaces() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlaces(start, pageSize, "R")

        val source = OsmPlacesSource(placeRowParser, ::cursor, limitMuchLargeThanExpectedNumberOfRows)

        val places = emptyList<Place>().toMutableList()
        fun collectPlaces(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPlaces)

        assertEquals(11L, places[0].osmId)
        assertEquals(13L, places[1].osmId)
        assertEquals(58L, places[2].osmId)
    }

}