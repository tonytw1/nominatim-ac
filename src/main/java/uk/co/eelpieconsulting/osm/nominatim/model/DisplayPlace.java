package uk.co.eelpieconsulting.osm.nominatim.model;

import java.util.List;
import java.util.Map;

public class DisplayPlace {

	private final long osmId;
	private final String osmType;
	private final String address;
	private final String classification;
	private final String type;
	private final Map<String, Double> latlong;
	private final String country;
	private final String displayType;

	public DisplayPlace(long osmId, String osmType, String address, String classification, String type, Map<String, Double> latlong, String country, String displayType) {
		this.osmId = osmId;
		this.osmType = osmType;
		this.address = address;
		this.classification = classification;
		this.type = type;
		this.latlong = latlong;
		this.country = country;
		this.displayType = displayType;
	}

	public long getOsmId() {
		return osmId;
	}

	public String getOsmType() {
		return osmType;
	}

	public String getAddress() {
		return address;
	}

	public String getClassification() {
		return classification;
	}

	public String getType() {
		return type;
	}

	public Map<String, Double> getLatlong() {
		return latlong;
	}

	public String getCountry() {
		return country;
	}

	public String getDisplayType() {
		return displayType;
	}

	@Override
	public String toString() {
		return "DisplayPlace{" +
						"osmId=" + osmId +
						", osmType='" + osmType + '\'' +
						", address='" + address + '\'' +
						", classification='" + classification + '\'' +
						", type='" + type + '\'' +
						", latlong=" + latlong +
						", country='" + country + '\'' +
						", displayType='" + displayType + '\'' +
						'}';
	}

}