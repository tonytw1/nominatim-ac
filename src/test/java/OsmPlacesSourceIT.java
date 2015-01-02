import org.junit.Test;

import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmPlacesSource;

public class OsmPlacesSourceIT {
	
	@Test
	public void testname() throws Exception {
		OsmPlacesSource osmPlacesSource = new OsmPlacesSource(new OsmDAO(), "R", 16);
		while(osmPlacesSource.hasNext()) {
			System.out.println(osmPlacesSource.next());
		}
	}

}
