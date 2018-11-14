package uk.co.eelpieconsulting.osm.nominatim.controller

import org.joda.time.format.ISODateTimeFormat
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService
import uk.co.eelpieconsulting.osm.nominatim.indexing.PartialIndexWatermarkService
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory
import java.lang.Long

@RestController
class AutoCompleteController(val autoCompleteService: ElasticSearchAutoCompleteService,
                             val viewFactory: ViewFactory,
                             val partialIndexWatermarkService: PartialIndexWatermarkService,
                             val osmDAO: OsmDAO) {

    private val BASIC_DATE_TIME = ISODateTimeFormat.basicDateTime()

    @GetMapping("/status")
    fun status(): Map<String, String> {
        val status = mapOf<String, String>(
                "lastImportDate" to BASIC_DATE_TIME.print(osmDAO.getLastImportDate()),
                "indexedTo" to BASIC_DATE_TIME.print(partialIndexWatermarkService.watermark),
                "indexedItems" to Long.toString(autoCompleteService.indexedItemsCount()))

        return status
    }

    @GetMapping("/search")
    fun search(
            @RequestParam(required = false) q: String?,
            @RequestParam(required = false) tag: String?,
            @RequestParam(required = false) lat: Double?,
            @RequestParam(required = false) lon: Double?,
            @RequestParam(required = false) radius: Double?,
            @RequestParam(required = false) rank: Int?,
            @RequestParam(required = false) country: String?,
            @RequestParam(required = false) callback: String?,
            @RequestParam(required = false) profile: String?): List<DisplayPlace> {

        return autoCompleteService.search(q, tag, lat, lon, radius, rank, country, profile)
    }

    @RequestMapping(value = arrayOf("/search"), method = arrayOf(RequestMethod.OPTIONS))
    fun searchOptions(): ModelAndView {
        return ModelAndView()   // TODO this is for CORS support? Be explict about it
    }

    @GetMapping("/profiles")
    fun profiles(): Map<String, String> {
        val profiles = autoCompleteService.availableProfiles.associate { p -> p.name to p.name }
        return profiles
    }

}