package uk.co.eelpieconsulting.osm.nominatim.psql;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FormattedAddressCorrection {

    private static Logger log = Logger.getLogger(FormattedAddressCorrection.class);

    private static final String NAME_EN = "name:en";

    public String appendName(String address, Map<String, String> name) {
        if (name != null && name.containsKey(NAME_EN)) {
            String n = name.get(NAME_EN);
            if (!address.startsWith(n)) {
                String c = n + ", " + address;
                log.info("Address corrected from '" + address + "' to '" + c + "'");
                return c;
            }
        }
        return address;
    }

}
