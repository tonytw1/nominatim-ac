package uk.co.eelpieconsulting.osm.nominatim.psql

import org.apache.log4j.Logger
import uk.co.eelpieconsulting.osm.nominatim.model.Place

import java.sql.ResultSet
import java.sql.SQLException

class OsmPlacesSource @Throws(SQLException::class)
constructor(private val osmDAO: OsmDAO, private val placeRowParser: PlaceRowParser, private val type: String) {

    fun cursor(start: Long, stepSize: Long): ResultSet {
        return osmDAO.getPlaces(start, stepSize, type)
    }

    private var places: ResultSet? = null
    private var start: Long = 0

    private val max: Long

    private val log = Logger.getLogger(OsmPlacesSource::class.java)

    private val STEP_SIZE: Long = 1000

    init {
        start = 0
        this.max = osmDAO.getMax(type)
        this.places = prepare(start, STEP_SIZE)
    }

    private fun prepare(start: Long, stepSize: Long): ResultSet {
        log.info("Preparing type: $type $start")
        return cursor(start, stepSize)
    }

    fun hasNext(): Boolean {
        return start < max
    }

    fun next(): Place {
        try {
            if (places!!.isLast) {
                log.debug("After last; preparing again")
                this.places = prepare(start, STEP_SIZE)
            }
            places!!.next()

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }

        try {
            val place = placeRowParser.buildPlaceFromCurrentRow(places!!)
            start = place.osmId
            return place

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }

    }

}