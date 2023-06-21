package uk.co.eelpieconsulting.osm.nominatim.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place

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
