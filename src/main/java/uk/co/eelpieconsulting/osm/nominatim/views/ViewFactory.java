package uk.co.eelpieconsulting.osm.nominatim.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViewFactory {

	private final EtagGenerator etagGenerator;
	
	@Autowired
	public ViewFactory(EtagGenerator etagGenerator) {
		this.etagGenerator = etagGenerator;
	}

	public JsonView getJsonView() {
		return new JsonView(new WebStyleJsonSerializer(), etagGenerator, null);
	}
	
	public JsonView getJsonView(int maxAge) {
		return new JsonView(new WebStyleJsonSerializer(), etagGenerator, maxAge);
	}

}
