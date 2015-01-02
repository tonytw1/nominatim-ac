package uk.co.eelpieconsulting.osm.nominatim.parsing;

import java.util.Iterator;
import java.util.Map;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class PlacesDumpLineParser {

	private static final char COLUMN_SEPERATOR = '|';

	private final Splitter onColumnSeperator;
	private final Map<String, String> osmTypeLabels;
	
	public PlacesDumpLineParser() {
		onColumnSeperator = Splitter.on(COLUMN_SEPERATOR);
		osmTypeLabels = Maps.newHashMap();
		osmTypeLabels.put("R", "relation");
		osmTypeLabels.put("N", "node");
		osmTypeLabels.put("W", "way");
	}
	
	public Place parse(String line) {
		Iterable<String> split = onColumnSeperator.split(line);
		Iterator<String> fields = split.iterator();
		return new Place(Long.parseLong(fields.next()), osmTypeLabels.get(fields.next()), fields.next(), fields.next(), fields.next(), fields.next(), Integer.parseInt(fields.next()), null);
	}
	
}
