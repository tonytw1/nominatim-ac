package uk.co.eelpieconsulting.osm.nominatim.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.AutoCompleteService;
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchAutoCompleteService;

@Controller
public class AutoCompleteController {
	
	private final AutoCompleteService autoCompleteService;
	private final ViewFactory viewFactory;
	
	@Autowired
	public AutoCompleteController(ElasticSearchAutoCompleteService autoCompleteService, ViewFactory viewFactory) {
		this.autoCompleteService = autoCompleteService;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/suggest")
	public ModelAndView suggestions(@RequestParam(value="q",required=true) String q,
			@RequestParam(value="callback", required=false) String callback) {

		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", autoCompleteService.getSuggestionsFor(q));
		if (callback != null) {
			mv.addObject("callback", callback);
		}
		return mv;
	}
	
}
