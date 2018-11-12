package uk.co.eelpieconsulting.osm.nominatim.controllers;

import com.google.common.collect.Maps;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.Profile;
import uk.co.eelpieconsulting.osm.nominatim.indexing.PartialIndexWatermarkService;
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.views.ViewFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@Controller
public class AutoCompleteController {

  private static final DateTimeFormatter BASIC_DATE_TIME = ISODateTimeFormat.basicDateTime();
  private final ElasticSearchAutoCompleteService autoCompleteService;
  private final ViewFactory viewFactory;
  private final OsmDAO osmDAO;
  private final PartialIndexWatermarkService partialIndexWatermarkService;

  @Autowired
  public AutoCompleteController(ElasticSearchAutoCompleteService autoCompleteService,
                                ViewFactory viewFactory, OSMDAOFactory osmDAOFactory,
                                PartialIndexWatermarkService partialIndexWatermarkService) throws SQLException {
    this.autoCompleteService = autoCompleteService;
    this.viewFactory = viewFactory;
    this.partialIndexWatermarkService = partialIndexWatermarkService;
    this.osmDAO = osmDAOFactory.build();
  }

  @RequestMapping("/status")
  public ModelAndView status() throws SQLException, IOException {
    Map<String, String> data = Maps.newHashMap();
    data.put("lastImportDate", BASIC_DATE_TIME.print(osmDAO.getLastImportDate()));
    data.put("indexedTo", BASIC_DATE_TIME.print(partialIndexWatermarkService.getWatermark()));
    data.put("indexedItems", Long.toString(autoCompleteService.indexedItemsCount()));
    return new ModelAndView(viewFactory.getJsonView()).addObject("data", data);
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public ModelAndView search(
          @RequestParam(value = "q", required = false) String q,
          @RequestParam(value = "tag", required = false) String tag,
          @RequestParam(value = "lat", required = false) Double lat,
          @RequestParam(value = "lon", required = false) Double lon,
          @RequestParam(value = "radius", required = false) Double radius,
          @RequestParam(value = "rank", required = false) Integer rank,
          @RequestParam(value = "country", required = false) String country,
          @RequestParam(value = "callback", required = false) String callback,
          @RequestParam(required = false) String profile) throws IOException {

    final ModelAndView mv = new ModelAndView(viewFactory.getJsonView(600));
    mv.addObject("data", autoCompleteService.search(q, tag, lat, lon, radius, rank, country, profile));
    if (callback != null) {
      mv.addObject("callback", callback);
    }
    return mv;
  }

  @RequestMapping(value = "/search", method = RequestMethod.OPTIONS)
  public ModelAndView searchOptions() {
    return new ModelAndView();
  }

  @RequestMapping(value = "/profiles", method = RequestMethod.GET)
  public ModelAndView profiles() throws SQLException {
    Map<String, String> data = Maps.newHashMap();
    for (Profile p : autoCompleteService.getAvailableProfiles()) {
      data.put(p.getName(), p.getName());
    }
    return new ModelAndView(viewFactory.getJsonView()).addObject("data", data);
  }

}