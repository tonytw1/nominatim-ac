package uk.co.eelpieconsulting.osm.nominatim.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class PlaceController {

    Pattern OSM_IDENTIFIER_FORMAT = Pattern.compile("^(\\d+)(R|W|N)$");

    private final ViewFactory viewFactory;
    private final OsmDAO osmDAO;
    private final PlaceRowParser placeRowParser;

    @Autowired
    public PlaceController(
            ViewFactory viewFactory, OSMDAOFactory osmDAOFactory,
            PlaceRowParser placeRowParser) throws SQLException {
        this.viewFactory = viewFactory;
        this.placeRowParser = placeRowParser;
        this.osmDAO = osmDAOFactory.build();
    }

    @RequestMapping("/places/{p}")
    public ModelAndView place(@PathVariable String p) throws SQLException {
        Matcher matcher = OSM_IDENTIFIER_FORMAT.matcher(p);
        if (matcher.matches()) {
            long osmId = Long.parseLong(matcher.group(1));
            String osmType = matcher.group(2);

            ResultSet placeRow = osmDAO.getPlace(osmId, osmType);
            placeRow.next();

            final Place place = placeRowParser.buildPlaceFromCurrentRow(placeRow);
            return new ModelAndView(viewFactory.getJsonView()).addObject("data", place);

        } else {
            throw new IllegalArgumentException();
        }
    }

}