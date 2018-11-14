package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Sets
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place

@Component
class PlaceExtractor {

    fun extractPlaces(osmPlacesSource: OsmPlacesSource, callback: (Place) -> Unit) {
        var currentPlace: Place? = null
        var currentTags: MutableSet<String> = Sets.newHashSet()
        while (osmPlacesSource.hasNext()) {
            val place = osmPlacesSource.next()
            if (currentPlace == null) {
                currentPlace = place
            }
            currentTags.addAll(place.tags)

            val placeIsDifferentFromTheLast = place.osmId.toString() + place.osmType != currentPlace!!.osmId.toString() + currentPlace.osmType
            if (placeIsDifferentFromTheLast) {
                place.tags = currentTags.toList()
                callback(place)
                currentPlace = place
                currentTags = Sets.newHashSet()
            }
        }
        if (currentPlace != null) {
            callback(currentPlace!!)
        }
    }

}