package uk.co.eelpieconsulting.osm.nominatim.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import com.fasterxml.jackson.module.kotlin.*

@Component
class JsonDeserializer {

    private val mapper: ObjectMapper

    init {
        this.mapper = ObjectMapper().registerKotlinModule()
    }

    fun deserializePlace(sourceAsString: String): Place {
        return mapper.readValue<Place>(sourceAsString)
    }

}
