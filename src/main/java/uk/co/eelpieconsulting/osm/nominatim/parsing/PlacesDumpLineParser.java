package uk.co.eelpieconsulting.osm.nominatim.parsing;

import java.util.Iterator;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.base.Splitter;

public class PlacesDumpLineParser {

	private static final char COLUMN_SEPERATOR = '|';

	private final Splitter onColumnSeperator;
	
	public PlacesDumpLineParser() {
		onColumnSeperator = Splitter.on(COLUMN_SEPERATOR);
	}
	
	public Place parse(String line) {
		Iterable<String> split = onColumnSeperator.split(line);
		Iterator<String> fields = split.iterator();
		return new Place(Long.parseLong(fields.next()), fields.next(), fields.next(), fields.next(), fields.next(), fields.next());
	}
	
}
