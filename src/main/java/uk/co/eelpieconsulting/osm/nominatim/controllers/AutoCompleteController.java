package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;

@Controller
public class AutoCompleteController {
	
	private final ElasticSearchAutoCompleteService autoCompleteService;
	private final ViewFactory viewFactory;
	private final OsmDAO osmDAO;
	
	@Autowired
	public AutoCompleteController(ElasticSearchAutoCompleteService autoCompleteService, ViewFactory viewFactory, OSMDAOFactory osmDAOFactory) throws SQLException {
		this.autoCompleteService = autoCompleteService;
		this.viewFactory = viewFactory;
		this.osmDAO = osmDAOFactory.build();
	}
	
	@RequestMapping("/status")
	public ModelAndView status() throws SQLException {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("nominatimLatestIndexedDate", osmDAO.getLatestIndexedDate());
		return mv;
	}
	
	@RequestMapping("/search")
	public ModelAndView search(
			@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "tag", required = false) String tag,
			@RequestParam(value = "lat", required = false) Double lat,
			@RequestParam(value = "lon", required = false) Double lon,
			@RequestParam(value = "radius", required = false) Double radius,
			@RequestParam(value = "rank", required = false) Integer rank,
			@RequestParam(value = "country", required = false) String country,
			@RequestParam(value = "callback", required=false) String callback,
			@RequestParam(required=false) String profile) {
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView(600));
		mv.addObject("data", autoCompleteService.search(q, tag, lat, lon, radius, rank, country, profile));
		if (callback != null) {
			mv.addObject("callback", callback);
		}
		return mv;
	}
	
	@Deprecated
	@RequestMapping("/suggest")
	public ModelAndView suggestions(@RequestParam(value = "q", required = true) String q, 
			@RequestParam(value = "callback", required=false) String callback) {
		return search(q, null, null, null, null, null, null, callback, ElasticSearchAutoCompleteService.COUNTRY_CITY_TOWN_SUBURB);
	}
	
}