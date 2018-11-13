package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet
import java.sql.SQLException


@Component
class PlaceRowParser {

    private val formattedAddressCorrection = FormattedAddressCorrection()

    @Throws(SQLException::class)
    fun buildPlaceFromCurrentRow(placeRow: ResultSet): Place {
        val osmId = placeRow.getLong("osm_id")
        val osmType = placeRow.getString("osm_type")
        val classification = placeRow.getString(3)
        val type = placeRow.getString(4)
        val rank = placeRow.getInt("rank")
        val latitude = placeRow.getDouble("latitude")
        val longitude = placeRow.getDouble("longitude")
        val country = placeRow.getString("country")
        val adminLevel = placeRow.getInt("admin_level")
        val name = placeRow.getString("name")

        val extratags = placeRow.getObject("extratags") as Map<String, String>

        val latlong = Maps.newHashMap<String, Double>()
        latlong["lat"] = latitude
        latlong["lon"] = longitude

        val tags = Sets.newHashSet<String>()
        appendTag(classification, type, tags)
        if (extratags != null) {
            for (key in extratags.keys) {
                appendTag(key, extratags[key], tags)
            }
        }

        val address = placeRow.getString("en_label")

        val correctedAddress = formattedAddressCorrection.appendName(address, placeRow.getObject("name") as Map<String, String>)

        return Place(osmId, osmType, "TODO", correctedAddress, classification, type, rank, latlong, Lists.newArrayList(tags), country, adminLevel, name)
    }

    private fun appendTag(classification: String, type: String?, tags: MutableSet<String>) {    // TODO remove optional
        tags.add("$classification|$type")
    }

}
