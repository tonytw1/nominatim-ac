package uk.co.eelpieconsulting.osm.nominatim.psql

import org.springframework.beans.factory.FactoryBean

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// Used by Spring as the factory for OsmDAO beans
@Component
class OSMDAOFactory(@param:Value("\${database.username}") private val username: String,
                    @param:Value("\${database.password}") private val password: String,
                    @param:Value("\${database.host}") private val host: String) : FactoryBean<OsmDAO> {

    @Throws(Exception::class)
    override fun getObject(): OsmDAO {
        return OsmDAO(username, password, host)
    }

    override fun getObjectType(): Class<*>? {
        return OsmDAO::class.java
    }
}
