package uk.co.eelpieconsulting.osm.nominatim.model;

import java.util.List;
import java.util.Map;

public class Place {

	private long osmId;
	private String osmType;
	private String housenumber;
	private String address;
	private String classification;
	private String type;
	private int rank;
	private Map<String, Double> latlong;
	private List<String> tags;
	private String country;
	private int adminLevel;
	
	public Place() {
	}
	
	public Place(long osmId, String osmType, String houseNumber, String address, String classification, String type, int rank, Map<String, Double> latlong, List<String> tags, String country, int adminLevel) {
		this.osmId = osmId;
		this.osmType = osmType;
		this.housenumber = houseNumber;
		this.address = address;
		this.classification = classification;
		this.type = type;
		this.rank = rank;
		this.latlong = latlong;
		this.tags = tags;
		this.country = country;
		this.adminLevel = adminLevel;
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

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getCountry() {
		return country;
	}
	
	public Integer getAdminLevel() {
		return adminLevel;
	}

	public String getDisplayType() {
		if (tags.contains("place|country")) {
			return "country";
		}
		if (tags.contains("place|county")) {
			return "county";
		}
		if (tags.contains("place|city")) {
			return "city";
		}
		if (tags.contains("place|town")) {
			return "town";
		}
		if (tags.contains("place|suburb")) {
			return "suburb";
		}
		return classification.toUpperCase() + "/" + type.toUpperCase();
	}

	@Override
	public String toString() {
		return "Place [address=" + address + ", adminLevel=" + adminLevel
				+ ", classification=" + classification + ", country=" + country
				+ ", housenumber=" + housenumber + ", latlong=" + latlong
				+ ", osmId=" + osmId + ", osmType=" + osmType + ", rank="
				+ rank + ", tags=" + tags + ", type=" + type + "]";
	}
	
}