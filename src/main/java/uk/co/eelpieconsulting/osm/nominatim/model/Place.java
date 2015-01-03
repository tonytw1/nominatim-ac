package uk.co.eelpieconsulting.osm.nominatim.model;

import java.util.Map;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

public class Place {

	private long osmId;
	private String osmType;
	private String housenumber;
	private String address;
	private String classification;
	private String type;
	private int rank;
	private Map<String, Double> latlong;
	
	public Place() {
	}
	
	public Place(long osmId, String osmType, String houseNumber, String address, String classification, String type, int rank, Map<String, Double> latlong) {
		this.osmId = osmId;
		this.osmType = osmType;
		this.housenumber = houseNumber;
		this.address = address;
		this.classification = classification;
		this.type = type;
		this.rank = rank;
		this.latlong = latlong;
	}

	public long getOsmId() {
		return osmId;
	}

	public String getOsmType() {
		return osmType;
	}

	public String getHousenumber() {
		return housenumber;
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
	
	public int getRank() {
		return rank;
	}
	
	public Map<String, Double> getLatlong() {
		return latlong;
	}

	@Override
	public String toString() {
		return "Place [address=" + address + ", classification="
				+ classification + ", housenumber=" + housenumber
				+ ", latlong=" + latlong + ", osmId=" + osmId + ", osmType="
				+ osmType + ", rank=" + rank + ", type=" + type + "]";
	}
	
}