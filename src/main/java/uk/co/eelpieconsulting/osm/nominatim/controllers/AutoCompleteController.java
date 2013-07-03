package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.AutoCompleteService;

@Controller
public class AutoCompleteController {
	
	private final AutoCompleteService solrDAO;
	private final ViewFactory viewFactory;
	
	@Autowired
	public AutoCompleteController(AutoCompleteService solrDAO, ViewFactory viewFactory) {
		this.solrDAO = solrDAO;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/suggest")
	public ModelAndView suggestions(@RequestParam(value="term",required=true) String term,
			@RequestParam(value="callback", required=false) String callback) throws MalformedURLException, SolrServerException {

		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", solrDAO.getSuggestionsFor(term));
		if (callback != null) {
			mv.addObject("callback", callback);
		}
		return mv;
	}
	
}
