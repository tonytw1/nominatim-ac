package uk.co.eelpieconsulting.osm.nominatim.controller

import org.joda.time.format.ISODateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService
import uk.co.eelpieconsulting.osm.nominatim.indexing.PartialIndexWatermarkService
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO

@RestController
class AutoCompleteController(val autoCompleteService: ElasticSearchAutoCompleteService,
                             val partialIndexWatermarkService: PartialIndexWatermarkService,
                             val osmDAO: OsmDAO) {

    private val BASIC_DATE_TIME = ISODateTimeFormat.basicDateTime()

    @GetMapping("/status")
    fun status(): Map<String, String> {
        return mapOf(
                "lastImportDate" to BASIC_DATE_TIME.print(osmDAO.getLastImportDate()),
                "indexedTo" to BASIC_DATE_TIME.print(partialIndexWatermarkService.getWatermark()),
                "indexedItems" to autoCompleteService.indexedItemsCount().toString())
    }

    @GetMapping("/search")
    fun search(
            q: String?,
            tag: String?,
            lat: Double?,
            lon: Double?,
            radius: Double?,
            rank: Int?,
            country: String?,
            profile: String?): List<DisplayPlace> {
        return autoCompleteService.search(q, tag, lat, lon, radius, rank, country, profile)
    }

    @GetMapping("/profiles")
    fun profiles(): Map<String, String> {
        val profiles = autoCompleteService.getAvailableProfiles().associate { p -> p.getName() to p.getName() }
        return profiles
    }

}