package uk.co.eelpieconsulting.osm.nominatim.indexing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
	
	private DateTime start;

	@Autowired
	public PartialIndexUpdater(OSMDAOFactory osmDaoFactory, PlaceRowParser placeRowParser, ElasticSearchIndexer elasticSearchIndexer) throws SQLException {
		this.placeRowParser = placeRowParser;
		this.elasticSearchIndexer = elasticSearchIndexer;
		this.osmDAO = osmDaoFactory.build();	
		this.start = new DateTime(2015, 2, 1, 0, 0, 0);	// TODO persist
	}
	
	@Scheduled(fixedRate=5000)
	public void update() throws SQLException {
		log.info("Updating indexed after: " + start);
		final ResultSet places = osmDAO.getPlacesIndexedAfter(start, 10000);
		
		List<Place> updates = Lists.newArrayList();
		while (!places.isAfterLast()) {
			boolean next = places.next();
			if (next) {
				updates.add(indexRow(places));
			}
		}
		
		elasticSearchIndexer.index(updates);
		log.info("Submitted updates: " + updates.size());
	}
	
	private Place indexRow(final ResultSet places) throws SQLException {
		Timestamp time = places.getTimestamp("indexed_date");
		DateTime indexedDate = new DateTime(time);
		
		Place place = placeRowParser.buildPlaceFromRow(places);
		start = indexedDate;	// TODO should be set after elasticsearch submit
		return place;
	}
	
}
