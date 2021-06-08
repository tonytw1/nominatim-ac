package uk.co.eelpieconsulting.osm.nominatim.psql

import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.model.LatLong
import uk.co.eelpieconsulting.osm.nominatim.model.Place
import java.sql.ResultSet
import java.sql.SQLException

@Component
class PlaceRowParser {

    @Throws(SQLException::class)
    fun buildPlaceFromCurrentRow(placeRow: ResultSet): Place {

        fun extractTagsFromRow(): List<String> {
            fun asTag(classification: String, type: String): String {
                return "$classification|$type"
            }

            val tag = listOf(asTag(placeRow.getString(3), placeRow.getString(4)))
            val extratagsField = placeRow.getObject("extratags") as Map<String, String>?
            val extraTags = if (extratagsField != null) {
                extratagsField.entries.map {
                    asTag(it.key, it.value)
                }
            } else {
                emptyList()
            }
            return tag + extraTags
        }

        val latlong = LatLong(
                placeRow.getDouble("latitude"),
                placeRow.getDouble("longitude"))

        val nameField = placeRow.getObject("name") as Map<String, String>?
        val name = nameField?.get("name")

        val placeAddressRank = placeRow.getInt("rank")
        val addressRank = if (placeAddressRank > 0) {
            placeAddressRank
        } else {
            100 // TODO Confirm is the data has null values or not
        }

        return Place(osmId = placeRow.getLong("osm_id"),
                osmType = placeRow.getString("osm_type"),
                name = name,
                address = placeRow.getString("en_label").trim(),
                classification = placeRow.getString(3),
                type = placeRow.getString(4),
                addressRank = addressRank,
                latlong = latlong,
                tags = extractTagsFromRow(),
                country = placeRow.getString("country"),
                adminLevel = placeRow.getInt("admin_level"))
    }

}
