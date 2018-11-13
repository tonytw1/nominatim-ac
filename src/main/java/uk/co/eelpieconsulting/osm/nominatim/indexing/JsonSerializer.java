package uk.co.eelpieconsulting.osm.nominatim.indexing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class JsonSerializer {

	private ObjectMapper mapper;

	public JsonSerializer() {
		mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	}
	
	public String serialize(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
