package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class Country implements Profile {

    private static final String COUNTRY = "country";
    private static final String TAGS = "tags";

    public String getName() {
        return COUNTRY;
    }

    public BoolQueryBuilder getQuery() {
        QueryBuilder isCountry = termQuery(TAGS, "place|country");
        return boolQuery().minimumShouldMatch(1).should(isCountry);
    }

}
