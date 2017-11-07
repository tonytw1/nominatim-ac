package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class CountryCityTownSuburb implements Profile {

    public static final String COUNTRY_CITY_TOWN_SUBURB = "countryCityTownSuburb";
    private static final String TAGS = "tags";

    public String getName() {
        return COUNTRY_CITY_TOWN_SUBURB;
    }

    public BoolQueryBuilder getQuery() {
        QueryBuilder isCountry = termQuery(TAGS, "place|country");
        QueryBuilder isCity = termQuery(TAGS, "place|city");
        QueryBuilder isCounty = termQuery(TAGS, "place|county");
        QueryBuilder isTown = termQuery(TAGS, "place|town");
        QueryBuilder isSuburb = termQuery(TAGS, "place|suburb");
        QueryBuilder isNationalPark = termQuery(TAGS, "boundary|national_park");
        QueryBuilder isLeisurePark = termQuery(TAGS, "leisure|park");
        QueryBuilder isLeisureCommon = termQuery(TAGS, "leisure|common");
        QueryBuilder isPeak = termQuery(TAGS, "natural|peak");
        QueryBuilder isIsland = termQuery(TAGS, "place|island");
        QueryBuilder isVillage = termQuery(TAGS, "place|village");
        QueryBuilder isBoundary = termQuery(TAGS, "boundary|administrative");
        QueryBuilder isAdminLevelFour = termQuery("adminLevel", "4");
        QueryBuilder isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour);
        QueryBuilder isAdminLevelSix = termQuery("adminLevel", "6");
        QueryBuilder isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix);

        return boolQuery().minimumNumberShouldMatch(1).
                should(isCountry).boost(10).
                should(isCity).boost(8).
                should(isNationalPark).boost(8).
                should(isAdminLevelFourBoundary).boost(6).
                should(isAdminLevelSixBoundary).boost(5).
                should(isCounty).boost(4).
                should(isTown).boost(3).
                should(isPeak).boost(3).
                should(isIsland).
                should(isLeisurePark).
                should(isLeisureCommon).
                should(isVillage).
                should(isSuburb);
    }

}
