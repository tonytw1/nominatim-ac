package uk.co.eelpieconsulting.osm.nominatim;

import java.util.List;

import uk.co.eelpieconsulting.osm.nominatim.model.Place;

public interface AutoCompleteService {

	public List<Place> getSuggestionsFor(String q);
	public List<Place> search(String q, String tag);

}