package uk.co.eelpieconsulting.osm.nominatim.json

import org.apache.commons.io.IOUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonDeserializerTest {

    val jsonDeserializer = JsonDeserializer()

    @Test
    fun canSerializeKotlinModelClass() {
        val placeJson = IOUtils.toString(this.javaClass.classLoader.getResource("place.json"))

        val place = jsonDeserializer.deserializePlace(placeJson)

        assertEquals(4551386, place.osmId)
    }

}