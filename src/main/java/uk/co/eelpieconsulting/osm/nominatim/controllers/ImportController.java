package uk.co.eelpieconsulting.osm.nominatim.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.osm.nominatim.indexing.FullIndexBuilder;

@Controller
public class ImportController {
	
	private final FullIndexBuilder fullIndexBuilder;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ImportController(FullIndexBuilder fullIndexBuilder, ViewFactory viewFactory) {
		this.fullIndexBuilder = fullIndexBuilder;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/import")
	public ModelAndView inputIndex() throws FileNotFoundException, IOException, SQLException {
		fullIndexBuilder.buildFullIndex();
		return new ModelAndView(viewFactory.getJsonView()).addObject("data", "ok");
	}
	
}
