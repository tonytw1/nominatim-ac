package uk.co.eelpieconsulting.osm.nominatim.model;

public class Place {

	private final long osmId;
	private final String osmType;
	private final String housenumber;
	private final String address;
	private final String classification;
	private final String type;
	
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
