package uk.co.eelpieconsulting.osm.nominatim.model;

public class Place {

	private long osmId;
	private String osmType;
	private String housenumber;
	private String address;
	private String classification;
	private String type;
	
	public Place() {
	}
	
	public Place(long osmId, String osmType, String houseNumber, String address, String classification, String type) {
		this.osmId = osmId;
		this.osmType = osmType;
		this.housenumber = houseNumber;
		this.address = address;
		this.classification = classification;
		this.type = type;
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

	@Override
	public String toString() {
		return "Place [osmId=" + osmId + ", osmType=" + osmType
				+ ", housenumber=" + housenumber + ", address=" + address
				+ ", classification=" + classification + ", type=" + type + "]";
	}
	
}
