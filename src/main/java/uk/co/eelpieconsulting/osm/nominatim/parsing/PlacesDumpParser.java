package uk.co.eelpieconsulting.osm.nominatim.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.NotImplementedException;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

public class PlacesDumpParser implements Iterator<Place> {

	private PlacesDumpLineParser placesDumpLineParser;

	final private LineIterator lines;
	
	public PlacesDumpParser(File file) throws FileNotFoundException, IOException {
		placesDumpLineParser = new PlacesDumpLineParser();
		lines = IOUtils.lineIterator(new FileInputStream(file), "UTF-8");		
	}
	
	@Override
	public boolean hasNext() {
		return lines.hasNext();
	}

	@Override
	public Place next() {
		return placesDumpLineParser.parse(lines.nextLine());
	}

	@Override
	public void remove() {
		throw new NotImplementedException();		
	}

}
