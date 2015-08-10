package uk.co.eelpieconsulting.osm.nominatim.indexing;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
public class PartialIndexWatermarkService {
	
	private DateTime watermark;
	
	public PartialIndexWatermarkService() {
		this.watermark = new DateTime(2015, 3, 29, 0, 0, 0);	// TODO persist
	}

	public DateTime getWatermark() {
		return watermark;
	}
	
	public void setWatermark(DateTime watermark) {
		this.watermark = watermark;		
	}
	
}
