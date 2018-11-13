package uk.co.eelpieconsulting.osm.nominatim.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;

import java.io.IOException;

@Component
public class JsonDeserializer {

  private final ObjectMapper mapper;

  {
    this.mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public Place deserializePlace(String sourceAsString) throws IOException {
    return mapper.readValue(sourceAsString, Place.class);
  }

}
