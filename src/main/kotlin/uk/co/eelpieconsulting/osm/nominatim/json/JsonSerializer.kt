package uk.co.eelpieconsulting.osm.nominatim.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place

@Component
class JsonSerializer {

    private val mapper: ObjectMapper

    init {
        this.mapper = ObjectMapper().registerKotlinModule()
    }

    fun serializePlace(place: Place): String {  // TODO duplication with below
        return mapper.writeValueAsString(place)
    }

    fun serialize(o: Object): String {
        return mapper.writeValueAsString(o)
    }

}
