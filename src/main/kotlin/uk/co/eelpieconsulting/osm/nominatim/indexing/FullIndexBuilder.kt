package uk.co.eelpieconsulting.osm.nominatim.indexing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser

@Component
class FullIndexBuilder @Autowired
constructor(private val indexer: ElasticSearchIndexer, private val osmDAO: OsmDAO, private val placeRowParser: PlaceRowParser) {

    fun buildFullIndex() {
        //indexer.deleteAll();

        fun relations(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "R")
        fun ways(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "W")
        fun nodes(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "N")

        val pageSize = 1000L
        indexer.indexLines(OsmPlacesSource(placeRowParser, ::relations, pageSize))
        indexer.indexLines(OsmPlacesSource(placeRowParser, ::ways, pageSize))
        indexer.indexLines(OsmPlacesSource(placeRowParser, ::nodes, pageSize))
    }

}
