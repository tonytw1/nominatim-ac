package uk.co.eelpieconsulting.osm.nominatim.psql

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.LatLong
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet
import java.sql.SQLException


@Component
class PlaceRowParser {

    private val formattedAddressCorrection = FormattedAddressCorrection()

    @Throws(SQLException::class)
    fun buildPlaceFromCurrentRow(placeRow: ResultSet): Place {

        fun appendTag(classification: String, type: String?, tags: MutableSet<String>) {    // TODO remove optional
            tags.add("$classification|$type")
        }

        val extratags = placeRow.getObject("extratags") as Map<String, String>

        val tags = Sets.newHashSet<String>()
        appendTag(placeRow.getString(3), placeRow.getString(4), tags)
        if (extratags != null) {
            for (key in extratags.keys) {
                appendTag(key, extratags[key], tags)
            }
        }

        val address = placeRow.getString("en_label")
        val correctedAddress = formattedAddressCorrection.appendName(address, placeRow.getObject("name") as Map<String, String>)

        val latlong = LatLong(
                placeRow.getDouble("latitude"),
                placeRow.getDouble("longitude"))

        return Place(osmId = placeRow.getLong("osm_id"),
                osmType = placeRow.getString("osm_type"),
                address = correctedAddress,
                classification = placeRow.getString(3),
                type = placeRow.getString(4),
                rank = placeRow.getInt("rank"),
                latlong = latlong,
                tags = Lists.newArrayList(tags),
                country = placeRow.getString("country"),
                adminLevel = placeRow.getInt("admin_level"))
    }

}
