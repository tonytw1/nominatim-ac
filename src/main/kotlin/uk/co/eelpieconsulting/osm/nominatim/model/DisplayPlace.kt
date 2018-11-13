package uk.co.eelpieconsulting.osm.nominatim.model

class DisplayPlace(
        val osmId: Long,
        val osmType: String,
        val address: String,
        val classification: String,
        val type: String,
        val latlong: Map<String, Double>,
        val country: String,
        val displayType: String
)