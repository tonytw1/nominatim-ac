package uk.co.eelpieconsulting.osm.nominatim.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceExtractor
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory
import java.sql.SQLException
import java.util.regex.Pattern

@RestController
class PlaceController(val viewFactory: ViewFactory, val osmDAO: OsmDAO, val placeRowParser: PlaceRowParser) {

    private val OSM_IDENTIFIER_FORMAT = Pattern.compile("^(\\d+)(R|W|N)$")

    @RequestMapping("/places/{p}")
    fun place(@PathVariable p: String): Place {
        val matcher = OSM_IDENTIFIER_FORMAT.matcher(p)
        if (matcher.matches()) {
            val osmId = java.lang.Long.parseLong(matcher.group(1))
            val osmType = matcher.group(2)

            fun cursor(start: Long, pageSize: Long) = osmDAO.getPlace(osmId, osmType)

            val source = OsmPlacesSource(osmDAO, placeRowParser, ::cursor)

            var places = emptyList<Place>()
            fun collectPages(place: Place) {
                places = places + place
            }

            PlaceExtractor().extractPlaces(source, ::collectPages)

            return places.first()

        } else {
            throw IllegalArgumentException()
        }
    }

}