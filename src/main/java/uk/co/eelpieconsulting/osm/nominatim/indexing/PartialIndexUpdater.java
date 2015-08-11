package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.ElasticSearchIndexer;
import uk.co.eelpieconsulting.osm.nominatim.model.Place;
import uk.co.eelpieconsulting.osm.nominatim.psql.OSMDAOFactory;
import uk.co.eelpieconsulting.osm.nominatim.psql.OsmDAO;
import uk.co.eelpieconsulting.osm.nominatim.psql.PlaceRowParser;

import com.google.common.collect.Lists;

@Component
public class PartialIndexUpdater {
	
	private static Logger log = Logger.getLogger(PartialIndexUpdater.class);
	
	private final OsmDAO osmDAO;
	private final PlaceRowParser placeRowParser;
	private final ElasticSearchIndexer elasticSearchIndexer;
	private final PartialIndexWatermarkService partialIndexWatermarkService;
	
	@Autowired
	public PartialIndexUpdater(OSMDAOFactory osmDaoFactory, PlaceRowParser placeRowParser, ElasticSearchIndexer elasticSearchIndexer, PartialIndexWatermarkService partialIndexWatermarkService) throws SQLException {
		this.placeRowParser = placeRowParser;
		this.elasticSearchIndexer = elasticSearchIndexer;
		this.partialIndexWatermarkService = partialIndexWatermarkService;
		this.osmDAO = osmDaoFactory.build();	
	}
	
	@Scheduled(fixedRate=60000)
	public void update() throws SQLException {
		final DateTime persistedWaterMark = partialIndexWatermarkService.getWatermark();
		final DateTime watermark = persistedWaterMark != null ? persistedWaterMark : new DateTime(0L);
		
		log.info("Updating indexed after: " + watermark);
		final ResultSet places = osmDAO.getPlacesIndexedAfter(watermark, 10000);
		
		
		DateTime highWater = watermark;		
		List<Place> updates = Lists.newArrayList();
		while (!places.isAfterLast()) {
			boolean next = places.next();
			if (next) {
				Place place = placeRowParser.buildPlaceFromCurrentRow(places);
				updates.add(place);	
				highWater = new DateTime(places.getTimestamp("indexed_date"));
			}
		}
		
		elasticSearchIndexer.index(updates);
		log.info("Submitted updates: " + updates.size());				
	
		log.info("Setting watermark to: " + highWater);
		partialIndexWatermarkService.setWatermark(highWater);
	}
	
}