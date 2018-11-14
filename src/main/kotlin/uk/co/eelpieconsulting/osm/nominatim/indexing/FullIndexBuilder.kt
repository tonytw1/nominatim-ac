package uk.co.eelpieconsulting.osm.nominatim.indexing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser

@Component
class FullIndexBuilder @Autowired
constructor(private val indexer: ElasticSearchIndexer, private val osmDaoFactory: OSMDAOFactory, private val placeRowParser: PlaceRowParser) {

    val osmDAO = osmDaoFactory.build()

    fun buildFullIndex() {
        //indexer.deleteAll();

        fun relations(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "R")
        fun ways(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "W")
        fun nodes(start: Long, pageSize: Long) = osmDAO.getPlaces(start, pageSize, "N")

        indexer.indexLines( OsmPlacesSource(osmDAO, placeRowParser, ::relations))
        indexer.indexLines( OsmPlacesSource(osmDAO, placeRowParser, ::ways))
        indexer.indexLines( OsmPlacesSource(osmDAO, placeRowParser, ::nodes))
    }

}