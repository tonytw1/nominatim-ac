package uk.co.eelpieconsulting.osm.nominatim.json

import org.apache.commons.io.IOUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonSerializerTest {

    val jsonDeserializer = uk.co.eelpieconsulting.osm.nominatim.json.JsonDeserializer()
    val jsonSerializer = uk.co.eelpieconsulting.osm.nominatim.json.JsonSerializer()

    @Test
    fun canSerializeKotlinModelClass() {
        val placeJson = IOUtils.toString(this.javaClass.classLoader.getResource("place.json"))
        val place = jsonDeserializer.deserializePlace(placeJson)

        val json = jsonSerializer.serializePlace(place)

        assertTrue(json.contains("Twickenham"))
    }

}