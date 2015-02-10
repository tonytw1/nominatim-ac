package uk.co.eelpieconsulting.osm.nominatim.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService;

@Controller
public class AutoCompleteController {
	
	private final ElasticSearchAutoCompleteService autoCompleteService;
	private final ViewFactory viewFactory;
	
	@Autowired
	public AutoCompleteController(ElasticSearchAutoCompleteService autoCompleteService, ViewFactory viewFactory) {
		this.autoCompleteService = autoCompleteService;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/suggest")
	public ModelAndView suggestions(@RequestParam(value = "q", required = true) String q,
			@RequestParam(value="callback", required=false) String callback) {

		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", autoCompleteService.getSuggestionsFor(q));
		if (callback != null) {
			mv.addObject("callback", callback);
		}
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
			@RequestParam(value = "callback", required=false) String callback) {
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", autoCompleteService.search(q, tag, lat, lon, radius, rank, country));
		if (callback != null) {
			mv.addObject("callback", callback);
		}
		return mv;
	}
	
}