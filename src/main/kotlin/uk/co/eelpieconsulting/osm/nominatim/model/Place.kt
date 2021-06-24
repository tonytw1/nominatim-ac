package uk.co.eelpieconsulting.osm.nominatim.model

data class Place(
        val place_id: Long,
        val osmId: Long,
        val osmType: String,
        val housenumber: String? = null,
        val address: String,
        val classification: String,
        val type: String,
        val addressRank: Int,
        val latlong: LatLong,
        var tags: List<String>,
        val country: String?,
        val adminLevel: Int,
        val name: String?) {
}

