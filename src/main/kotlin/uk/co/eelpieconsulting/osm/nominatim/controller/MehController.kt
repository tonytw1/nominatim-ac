package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.eelpieconsulting.osm.nominatim.model.Meh

@RestController
class MehController() {

    @GetMapping("/meh")
    fun meh(): Meh {
        return Meh("123", "Meh")
    }

}
