package uk.co.eelpieconsulting.osm.nominatim.json

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonDeserializerTest {

    val jsonDeserializer = uk.co.eelpieconsulting.osm.nominatim.json.JsonDeserializer()

    @Test
    fun canSerializeKotlinModelClass() {
        val placeJson = IOUtils.toString(this.javaClass.classLoader.getResource("place.json"))

        val place = jsonDeserializer.deserializePlace(placeJson)

        assertEquals(4551386, place.osmId)
    }

}