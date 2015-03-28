import org.junit.Test;

import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

public class OsmPlacesSourceIT {
	
	@Test
	public void testname() throws Exception {
		OsmPlacesSource osmPlacesSource = new OsmPlacesSource(new OsmDAO("", "", "localhost"), new PlaceRowParser(), "R");
		while(osmPlacesSource.hasNext()) {
			System.out.println(osmPlacesSource.next());
		}
	}

}
