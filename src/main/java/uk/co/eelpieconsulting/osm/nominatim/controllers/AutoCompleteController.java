package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.solr.SolrDAO;

@Controller
public class AutoCompleteController {
	
	private final SolrDAO solrDAO;
	private final ViewFactory viewFactory;
	
	@Autowired
	public AutoCompleteController(SolrDAO solrDAO, ViewFactory viewFactory) {
		this.solrDAO = solrDAO;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/suggest")
	public ModelAndView suggestions(@RequestParam("term") String term) throws MalformedURLException, SolrServerException {
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());		
		mv.addObject("data", solrDAO.getSuggestionsFor(term));
		return mv;
	}
	
}
