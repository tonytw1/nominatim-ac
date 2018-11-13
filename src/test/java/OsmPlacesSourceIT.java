import org.junit.Before;
import org.junit.Test;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OsmPlacesSourceIT {

  private static final String DATABASE_HOST = "10.0.45.11";  // TODO inject
  private static final String DATABASE_USER = "www-data";
  private static final String DATABASE_PASSWORD = "";

  private OsmDAO osmDAO;
  private PlaceRowParser placeRowParser;

  @Before
  public void setup() throws SQLException {
    placeRowParser = new PlaceRowParser();
    osmDAO = new OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST);
  }

  @Test
  public void canIterateThroughPlaces() throws Exception {
    OsmPlacesSource osmPlacesSource = new OsmPlacesSource(osmDAO, placeRowParser, "R");
    int rowsInterated = 0;
    while (osmPlacesSource.hasNext() && rowsInterated < 1000) {
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