package uk.co.eelpieconsulting.osm.nominatim.psql

import org.apache.log4j.Logger
import org.joda.time.DateTime

import java.sql.*
import java.util.Properties

class OsmDAO(val username: String, val password: String, val host: String) {

    private val log = Logger.getLogger(OsmDAO::class.java)

    val conn: Connection by lazy {  // This lazy init is a hack to allow startup of a read only node with no Postgres
        // TODO make this formal by making the indexing code aware that the Postgres source is optional
        getConnection()
    }

    private val placesIndexedFrom: PreparedStatement by lazy {
        conn.prepareStatement("SELECT place_id, osm_id, osm_type, class, type, housenumber, "
                + "get_address_by_language(place_id, NULL ARRAY['name:en', 'name']) AS en_label,"
                + "name,"
                + "country_code AS country,"
                + "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
                + "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
                + "rank_address AS rank, "
                + "admin_level AS admin_level, "
                + "indexed_date AS indexed_date, "
                + "extratags "
                + "FROM placex "
                + "WHERE indexed_date > ? "
                + "ORDER BY indexed_date "
                + "LIMIT ?")
    }

    // When construction this query we need to be mindful of the indexes available.
    // This is why we can't sub order by place_id for pagination.
    private val places: PreparedStatement by lazy {
        conn.prepareStatement("SELECT place_id, osm_id, osm_type, class, type, housenumber, "
                //+ "get_address_by_language(place_id,  ARRAY['']) AS label,"
                + "get_address_by_language(place_id, NULL, ARRAY['name:en', 'name']) AS en_label,"
                + "name,"
                + "country_code AS country,"
                + "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
                + "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
                + "rank_address AS rank, "
                + "admin_level AS admin_level, "
                + "extratags "
                + "FROM placex "
                + "WHERE osm_id >= ? AND osm_type=? AND name IS NOT NULL "
                + "ORDER by osm_type, osm_id "
                + "LIMIT ?")
    }

    private val place by lazy {
        conn.prepareStatement("SELECT place_id, osm_id, osm_type, class, type, housenumber, "
                + "get_address_by_language(place_id, NULL,  ARRAY['name:en', 'name']) AS en_label,"
                + "name,"
                + "country_code AS country,"
                + "case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,"
                + "case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,"
                + "rank_address AS rank, "
                + "admin_level AS admin_level, "
                + "extratags "
                + "FROM placex "
                + "WHERE osm_id = ? AND osm_type = ? "
                + "LIMIT ?")
    }

    fun getMax(type: String): Long {
        val prepareStatement = conn.prepareStatement("SELECT MAX(osm_id) AS end from placex WHERE osm_type=?")
        prepareStatement.setString(1, type)
        val rs = prepareStatement.executeQuery()
        rs.next()
        val max = rs.getLong(1)
        rs.close()
        prepareStatement.close()
        return max
    }

    fun getLastImportDate(): DateTime {
        val prepareStatement = conn.prepareStatement("SELECT lastimportdate AS lastimportdate FROM import_status")
        val rs = prepareStatement.executeQuery()
        rs.next()
        val latest = rs.getTimestamp(1)
        rs.close()
        prepareStatement.close()
        return DateTime(latest.time)
    }

    fun getPlaces(start: Long, limit: Long, type: String): ResultSet {
        log.info("Get places: $start, $type, $limit")
        places.setLong(1, start)
        places.setString(2, type)
        places.setLong(3, limit)
        return places.executeQuery()
    }

    fun getPlacesIndexedAfter(start: DateTime, limit: Long): ResultSet {
        placesIndexedFrom.setTimestamp(1, java.sql.Timestamp(start.millis))
        placesIndexedFrom.setLong(2, limit.toLong())
        return placesIndexedFrom.executeQuery()
    }

    // TODO make visible to tests only
    fun getPlace(id: Long, type: String, limit: Long): ResultSet {
        place.setLong(1, id)  // TODO not thread safe
        place.setString(2, type)
        place.setLong(3, limit)
        return place.executeQuery()
    }

    private fun getConnection(): Connection {
        val url = "jdbc:postgresql://$host/nominatim"
        val props = Properties()
        props.setProperty("user", username)
        props.setProperty("password", password)
        log.info("Connecting to JDBC: $url")
        return DriverManager.getConnection(url, props)
    }
}