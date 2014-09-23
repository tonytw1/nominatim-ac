package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.indexing.IndexUpdater;

@Controller
public class ImportController {
	
	private final IndexUpdater indexUpdater;
	private final ViewFactory viewFactory;
	private String importFile;
	
	@Autowired
	public ImportController(IndexUpdater indexUpdater, ViewFactory viewFactory, @Value("${import.file}") String importFile) {
		this.indexUpdater = indexUpdater;
		this.viewFactory = viewFactory;
		this.importFile = importFile;
	}
	
	@RequestMapping("/import")
	public ModelAndView inputIndex() throws FileNotFoundException, IOException {
		indexUpdater.buildIndex(importFile);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", "ok");
		return mv;
	}
	
}
