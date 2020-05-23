package uk.co.eelpieconsulting.osm.nominatim.model

class DisplayPlace(
        val osmId: Long,
        val osmType: String,
        val address: String,
        val classification: String,
        val type: String,
        val latlong: LatLong,
        val country: String,
        val displayType: String,
        val adminLevel: Int,
        val addressRank: Int
)