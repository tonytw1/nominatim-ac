package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {

    @GetMapping("/healthz")
    fun ping(): String {
        return "ok"
    }

}
