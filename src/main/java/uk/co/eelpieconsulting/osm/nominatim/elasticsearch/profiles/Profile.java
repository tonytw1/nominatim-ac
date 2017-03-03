package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder;

public interface Profile {

    public String getName();
    public BoolQueryBuilder getQuery();

}
