import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

public class OsmPlacesSourceIT {
	
	private static final String DATABASE_HOST = "localhost";
	private static final String DATABASE_PASSWORD = "";
	private static final String DATABASE_USER = "";

	private OsmDAO osmDAO;
	private PlaceRowParser placeRowParser;

	@Before
	public void setup() throws SQLException {
		placeRowParser = new PlaceRowParser();
		osmDAO = new OsmDAO(DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST);
	}
	
	//@Test
	@Test
	public void canIterateThroughPlaces() throws Exception {
		OsmPlacesSource osmPlacesSource = new OsmPlacesSource(osmDAO, placeRowParser, "R");
		while(osmPlacesSource.hasNext()) {
			System.out.println(osmPlacesSource.next());
		}
	}
	
	@Test
	public void canExtractDisplayAddressForPlace() throws Exception {
		final ResultSet placeRow = osmDAO.getPlace(284926920, "W");
		placeRow.next();

		final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);
		
		assertEquals("Twickenham Rowing Club, Church Lane, Cole Park, St Margarets, London Borough of Richmond upon Thames, London, Greater London, England, TW1 3DU, United Kingdom", place.getAddress());
	}
	
	@Test
	public void nameShouldUseNameInPreferenceToRefField() throws Exception {
		final ResultSet placeRow = osmDAO.getPlace(202880711, "W");
		placeRow.next();
		
		final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);

		assertEquals("Arras Tunnel, SH 1, Mount Cook, Wellington, Wellington City, WGN, 6011, New Zealand/Aotearoa", place.getAddress());
	}
	
}