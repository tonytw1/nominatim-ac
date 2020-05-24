package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Lists
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet

class PlaceExtractorTest {

    private val DATABASE_HOST = "localhost:6432"  // TODO inject
    private val DATABASE_USER = "www-data"
    private val DATABASE_PASSWORD = ""

    private var osmDAO: OsmDAO = OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST)
    private var placeRowParser = PlaceRowParser()

    @Test
    fun thisBehaviourIsRequiredBecauseWithMultipleRowsWithTypeAndCategoriesSpanMultipleRows() {
        val r = osmDAO.getPlace(4599, "R")

        val types = Lists.newArrayList<String>()
        val categories = Lists.newArrayList<String>()
        while (r.next()) {
            val place = placeRowParser.buildPlaceFromCurrentRow(r)
            types.add(place.type)
            categories.add(place.classification)
        }

        assertEquals(2, types.size)
        assertEquals(Lists.newArrayList("government", "attraction"), types)
        assertEquals(Lists.newArrayList("office", "tourism"), categories)
    }

    @Test
    fun canExtractMultiRowPlaceWithAllExpectedTags() {
        fun cursor(start: Long, pageSize: Long) = osmDAO.getPlace(4599, "R")

        val source = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var places = emptyList<Place>()
        fun collectPages(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPages)

        assertEquals(1, places.size)
        val first = places.first()
        assertTrue(first.tags.contains("office|government"))
        assertTrue(first.tags.contains("tourism|attraction"))
    }

    @Test
    fun placeTagsShouldIncludeExtraTags() {
        fun cursor(start: Long, pageSize: Long) = osmDAO.getPlace(284926920, "W")

        val source = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var places = emptyList<Place>()
        fun collectPages(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPages)

        assertTrue(places.first().tags.contains("sport|rowing"))
    }

    @Test
    fun correctExtractsSequenceOfPlaces() {
        fun cursor(start: Long, pageSize: Long): ResultSet = osmDAO.getPlaces(start, pageSize, "R")

        val source = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

        var places = emptyList<Place>()
        fun collectPlaces(place: Place) {
            places += place
        }

        PlaceExtractor().extractPlaces(source, ::collectPlaces)

        assertEquals(11L, places[0].osmId)
        assertEquals(13L, places[1].osmId)
        assertEquals(58L, places[2].osmId)
    }

}