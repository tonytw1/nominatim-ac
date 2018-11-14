package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Lists
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaceExtractorTest {

    private val DATABASE_HOST = "10.0.45.11"  // TODO inject
    private val DATABASE_USER = "www-data"
    private val DATABASE_PASSWORD = ""

    private var osmDAO: OsmDAO =  OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST)
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
        assertEquals(Lists.newArrayList("attraction", "government"), types)
        assertEquals(Lists.newArrayList("tourism", "office"), categories)
    }

}