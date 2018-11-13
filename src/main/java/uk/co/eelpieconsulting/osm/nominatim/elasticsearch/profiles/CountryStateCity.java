package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class CountryStateCity implements Profile {

    public static final String COUNTRY_STATE_CITY = "countryStateCity";
    private static final String TAGS = "tags";

    public String getName() {
        return COUNTRY_STATE_CITY;
    }

    public BoolQueryBuilder getQuery() {
        QueryBuilder isCountry = termQuery(TAGS, "place|country");
        QueryBuilder isCity = termQuery(TAGS, "place|city");
        QueryBuilder isBoundary = termQuery(TAGS, "boundary|administrative");
        QueryBuilder isAdminLevelSix = termQuery("adminLevel", "6");
        QueryBuilder isAdminLevelFour = termQuery("adminLevel", "4");
        QueryBuilder isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix);
        QueryBuilder isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour);

        return boolQuery().minimumShouldMatch(1).
                should(isCountry).boost(10).
                should(isCity).boost(8).
                should(isAdminLevelFourBoundary).boost(6).
                should(isAdminLevelSixBoundary).boost(5);
    }

}
