import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseTest {

    private static final String DATABASE_HOST = "localhost:6432";  // TODO inject
    private static final String DATABASE_USER = "www-data";
    private static final String DATABASE_PASSWORD = "";

    private OsmDAO osmDAO;
    private PlaceRowParser placeRowParser;

    @BeforeAll
    public void setup() throws SQLException {
        placeRowParser = new PlaceRowParser();
        osmDAO = new OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST);
    }

    @Test
    public void canExtractDisplayAddressForPlace() throws Exception {
        final ResultSet placeRow = osmDAO.getPlace(284926920, "W");
        placeRow.next();

        final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);

        assertEquals("Twickenham Rowing Club, Church Lane, Twickenham, London Borough of Richmond upon Thames, London, Greater London, England, TW1 3DY, United Kingdom", place.getAddress());
    }

}