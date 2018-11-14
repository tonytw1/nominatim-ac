package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.osm.nominatim.indexing.FullIndexBuilder
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory

@Controller
class ImportController(val fullIndexBuilder: FullIndexBuilder, val viewFactory: ViewFactory) {

    @GetMapping("/import")
    fun inputIndex(): ModelAndView {
        fullIndexBuilder.buildFullIndex()
        return ModelAndView(viewFactory.jsonView).addObject("data", "ok")
    }

}
