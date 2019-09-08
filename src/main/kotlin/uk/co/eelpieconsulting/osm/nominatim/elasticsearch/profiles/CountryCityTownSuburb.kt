package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class CountryCityTownSuburb : Profile {

    private val COUNTRY_CITY_TOWN_SUBURB = "countryCityTownSuburb"
    private val TAGS = "tags"

    override fun getName(): String {
        return COUNTRY_CITY_TOWN_SUBURB
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(TAGS, "place|country")
        val isCity = termQuery(TAGS, "place|city");
        val isCounty = termQuery(TAGS, "place|county");
        val isTown = termQuery(TAGS, "place|town");
        val isSuburb = termQuery(TAGS, "place|suburb");
        val isNationalPark = termQuery(TAGS, "boundary|national_park");
        val isLeisurePark = termQuery(TAGS, "leisure|park");
        val isLeisureCommon = termQuery(TAGS, "leisure|common");
        val isPeak = termQuery(TAGS, "natural|peak");
        val isIsland = termQuery(TAGS, "place|island");
        val isVillage = termQuery(TAGS, "place|village");
        val isBoundary = termQuery(TAGS, "boundary|administrative");
        val isAdminLevelFour = termQuery("adminLevel", "4");
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour);
        val isAdminLevelSix = termQuery("adminLevel", "6");
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix);

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f).should(isNationalPark).boost(8f).should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary).boost(5f).should(isCounty).boost(4f).should(isTown).boost(3f).should(isPeak).boost(3f).should(isIsland).should(isLeisurePark).should(isLeisureCommon).should(isVillage).should(isSuburb);
    }

}
