package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.solr.IndexUpdater;
import uk.co.eelpieconsulting.osm.nominatim.solr.SolrDAO;

@Controller
public class AutoCompleteController {
	
	private final SolrDAO solrDAO;
	private final IndexUpdater indexUpdater;
	private final ViewFactory viewFactory;
	
	@Autowired
	public AutoCompleteController(SolrDAO solrDAO, IndexUpdater indexUpdater, ViewFactory viewFactory) {
		this.solrDAO = solrDAO;
		this.indexUpdater = indexUpdater;
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
	
	@RequestMapping("/import")
	public ModelAndView inputIndex() throws SolrServerException, IOException {
		indexUpdater.buildIndex("uk-all.txt");
		indexUpdater.buildIndex("nz.txt");
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", "ok");
		return mv;
	}
	
}
