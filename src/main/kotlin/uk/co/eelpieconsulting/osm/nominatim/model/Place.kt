package uk.co.eelpieconsulting.osm.nominatim.model

class Place(
        val osmId: Long,
        val osmType: String,
        val housenumber: String,
        val address: String,
        val classification: String,
        val type: String,
        val rank: Int,
        val latlong: Map<String, Double>,
        var tags: List<String>,
        val country: String,
        val adminLevel: Int,
        val name: String) {


    fun getDisplayType(): String {
        if (tags.contains("place|country")) {
            return "country"
        }
        if (tags.contains("place|county")) {
            return "county"
        }
        if (tags.contains("place|city")) {
            return "city"
        }
        if (tags.contains("place|town")) {
            return "town"
        }
        return if (tags.contains("place|suburb")) {
            "suburb"
        } else classification.toUpperCase() + "/" + type.toUpperCase()
    }

}


