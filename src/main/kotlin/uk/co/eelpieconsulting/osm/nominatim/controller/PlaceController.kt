package uk.co.eelpieconsulting.osm.nominatim.controller

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory
import java.sql.SQLException
import java.util.regex.Pattern

@Controller
class PlaceController(private val viewFactory: ViewFactory, osmDAOFactory: OSMDAOFactory, private val placeRowParser: PlaceRowParser) {

    private val OSM_IDENTIFIER_FORMAT = Pattern.compile("^(\\d+)(R|W|N)$")
    private val osmDAO = osmDAOFactory.build()

    @RequestMapping("/places/{p}")
    @Throws(SQLException::class)
    fun place(@PathVariable p: String): ModelAndView {
        val matcher = OSM_IDENTIFIER_FORMAT.matcher(p)
        if (matcher.matches()) {
            val osmId = java.lang.Long.parseLong(matcher.group(1))
            val osmType = matcher.group(2)

            val placeRows = osmDAO.getPlace(osmId, osmType)
            var currentPlace: Place? = null
            val currentTags = Sets.newHashSet<String>()
            while (placeRows.next()) {
                val place = placeRowParser.buildPlaceFromCurrentRow(placeRows)
                currentPlace = place
                currentTags.addAll(place.tags)    // TODO deduplicate with indexer
            }

            currentPlace!!.tags = Lists.newArrayList(currentTags)
            return ModelAndView(viewFactory.jsonView).addObject("data", currentPlace)

        } else {
            throw IllegalArgumentException()
        }
    }

}