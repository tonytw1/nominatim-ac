package uk.co.eelpieconsulting.osm.nominatim.psql

import junit.framework.Assert.assertEquals
import org.junit.Test

class OsmDAOTest {

    // Default credentials for Nominatim 3.7 Docker image; nothing to see here
    private val DATABASE_HOST = "localhost:5432"  // TODO inject
    private val DATABASE_USER = "nominatim"
    private val DATABASE_PASSWORD = "qaIACxO6wMR3"

    private val osmDAO = OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST)

    private val boscanova_osm_id = 742231354L

    @Test
    fun canRetrieveAddressForPlace() {
        val placesResultSet = osmDAO.getPlace(boscanova_osm_id, "N", 1)
        placesResultSet.next()

        val address = placesResultSet.getString("en_label")

        assertEquals("Cafe Boscanova, 650, Christchurch Road, Boscombe, Bournemouth, Bournemouth, Christchurch and Poole, England, BH1 4BP, United Kingdom", address)
    }

}