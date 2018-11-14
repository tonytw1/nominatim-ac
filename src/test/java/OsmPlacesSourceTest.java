import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OsmPlacesSourceTest {

    private static final String DATABASE_HOST = "10.0.45.11";  // TODO inject
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
    public void somePlacesSpanMultipleRowsWithTypeAndCategories() throws SQLException {
        ResultSet r = osmDAO.getPlace(4599, "R");

        List<String> types = Lists.newArrayList();
        List<String> categories = Lists.newArrayList();
        while (r.next()) {
            Place place = placeRowParser.buildPlaceFromCurrentRow(r);
            types.add(place.getType());
            categories.add(place.getClassification());
        }

        assertEquals(2, types.size());
        assertEquals( Lists.newArrayList("attraction" , "government"), types);
        assertEquals( Lists.newArrayList("tourism" , "office"), categories);
    }

    @Test
    public void canIterateThroughPlaces() throws Exception {
        OsmPlacesSource osmPlacesSource = new OsmPlacesSource(osmDAO, placeRowParser, "R");
        int rowsInterated = 0;
        while (osmPlacesSource.hasNext() && rowsInterated < 1000) {
            osmPlacesSource.next();
            rowsInterated++;
        }

        assertEquals(1000, rowsInterated);
    }

    @Test
    public void canExtractDisplayAddressForPlace() throws Exception {
        final ResultSet placeRow = osmDAO.getPlace(284926920, "W");
        placeRow.next();

        final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);

        assertEquals("Twickenham Rowing Club, Church Lane, Cole Park, Twickenham, London Borough of Richmond upon Thames, London, Greater London, England, TW1 3DY, United Kingdom", place.getAddress());
    }

    @Test
    public void placeTagsShouldIncludeTheClassificationTypeAndExtraTags() throws Exception {
        final ResultSet placeRow = osmDAO.getPlace(284926920, "W");
        placeRow.next();

        final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);

        assertTrue(place.getTags().contains("leisure|sports_centre"));
        assertTrue(place.getTags().contains("sport|rowing"));
    }

}