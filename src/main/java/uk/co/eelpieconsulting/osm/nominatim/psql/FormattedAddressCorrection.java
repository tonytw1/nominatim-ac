package uk.co.eelpieconsulting.osm.nominatim.psql;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FormattedAddressCorrection {

    private static Logger log = Logger.getLogger(FormattedAddressCorrection.class);

    public String appendName(String address, Map<String, String> name) {
        final String trimmedAddress = address.trim();
        return trimmedAddress;
    }

}
