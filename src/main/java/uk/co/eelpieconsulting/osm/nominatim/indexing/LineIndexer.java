package uk.co.eelpieconsulting.osm.nominatim.indexing;

import uk.co.eelpieconsulting.osm.nominatim.parsing.PlacesDumpParser;

public interface LineIndexer {

	public void indexLines(final PlacesDumpParser parser);

}