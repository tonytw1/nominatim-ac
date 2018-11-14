package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Sets
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place

@Component
class PlaceExtractor {

    fun extractPlaces(osmPlacesSource: OsmPlacesSource, callback: (Place) -> Unit) {
        var currentPlace: Place? = null
        var currentTags: MutableSet<String> = Sets.newHashSet()

        fun send(place: Place) {
            place.tags = currentTags.toList()
            callback(place)
            currentPlace = null
            currentTags = Sets.newHashSet()
        }

        while (osmPlacesSource.hasNext()) {
            val place = osmPlacesSource.next()
            if (currentPlace == null) {
                currentPlace = place
            }

            val placeIsDifferentFromTheLast = place.osmId.toString() + place.osmType != currentPlace!!.osmId.toString() + currentPlace!!.osmType
            if (placeIsDifferentFromTheLast) {
               send(currentPlace!!)
            }
            currentTags.addAll(place.tags)
        }

        if (currentPlace != null) {
            send(currentPlace!!)
        }
    }

}