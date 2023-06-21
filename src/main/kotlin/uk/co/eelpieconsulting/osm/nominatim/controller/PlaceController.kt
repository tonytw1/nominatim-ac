package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace

@RestController
class PlaceController(val autoCompleteService: ElasticSearchAutoCompleteService) {

    @CrossOrigin(origins = ["*"])
    @RequestMapping("/places/{id}")
    fun place(@PathVariable id: String): DisplayPlace? {
        val matches = autoCompleteService.byId(id)
        if (matches.isNotEmpty()) {
            return matches[0]
        }
        return null // TODO 404
    }

}