package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.eelpieconsulting.osm.nominatim.indexing.FullIndexBuilder

@RestController
class ImportController(val fullIndexBuilder: FullIndexBuilder) {

    @PostMapping("/import")
    fun inputIndex(): String {
        fullIndexBuilder.buildFullIndex()
        return "ok"
    }

}
