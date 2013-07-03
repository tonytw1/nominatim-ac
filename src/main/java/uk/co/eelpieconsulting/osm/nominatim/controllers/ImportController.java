package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.solr.IndexUpdater;

@Controller
public class ImportController {
	
	private final IndexUpdater indexUpdater;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ImportController(IndexUpdater indexUpdater, ViewFactory viewFactory) {
		this.indexUpdater = indexUpdater;
		this.viewFactory = viewFactory;
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
