package uk.co.eelpieconsulting.osm.nominatim.controller

import com.google.common.collect.Maps
import org.joda.time.format.ISODateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService
import uk.co.eelpieconsulting.osm.nominatim.indexing.PartialIndexWatermarkService
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory
import java.io.IOException
import java.lang.Long
import java.sql.SQLException

@Controller
class AutoCompleteController(private val autoCompleteService: ElasticSearchAutoCompleteService,
                             private val viewFactory: ViewFactory, osmDAOFactory: OSMDAOFactory,
                             private val partialIndexWatermarkService: PartialIndexWatermarkService) {

    private val osmDAO: OsmDAO = osmDAOFactory.build()
    private val BASIC_DATE_TIME = ISODateTimeFormat.basicDateTime()

    @RequestMapping("/status")
    @Throws(SQLException::class, IOException::class)
    fun status(): ModelAndView {
        val data = mapOf<String, String>(
                "lastImportDate" to BASIC_DATE_TIME.print(osmDAO.lastImportDate),
                "indexedTo" to BASIC_DATE_TIME.print(partialIndexWatermarkService.watermark),
                "indexedItems" to Long.toString(autoCompleteService.indexedItemsCount())
        )

        return ModelAndView(viewFactory.jsonView).addObject("data", data)
    }

    @GetMapping("/search")
    @Throws(IOException::class)
    fun search(
            @RequestParam(required = false) q: String?,
            @RequestParam(required = false) tag: String?,
            @RequestParam(required = false) lat: Double?,
            @RequestParam(required = false) lon: Double?,
            @RequestParam(required = false) radius: Double?,
            @RequestParam(required = false) rank: Int?,
            @RequestParam(required = false) country: String?,
            @RequestParam(required = false) callback: String?,
            @RequestParam(required = false) profile: String?): ModelAndView {

        val mv = ModelAndView(viewFactory.getJsonView(600))
        mv.addObject("data", autoCompleteService.search(q, tag, lat, lon, radius, rank, country, profile))
        if (callback != null) {
            mv.addObject("callback", callback)
        }
        return mv
    }

    @RequestMapping(value = arrayOf("/search"), method = arrayOf(RequestMethod.OPTIONS))
    fun searchOptions(): ModelAndView {
        return ModelAndView()   // TODO this is for CORS support? Be explict about it
    }

    @GetMapping("/profiles")
    @Throws(SQLException::class)
    fun profiles(): ModelAndView {
        val data = Maps.newHashMap<String, String>()
        for (p in autoCompleteService.availableProfiles) {
            data[p.name] = p.name
        }
        return ModelAndView(viewFactory.jsonView).addObject("data", data)
    }

}