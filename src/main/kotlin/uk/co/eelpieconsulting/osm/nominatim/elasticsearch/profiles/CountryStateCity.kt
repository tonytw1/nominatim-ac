package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder

import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class CountryStateCity : Profile {

    private val COUNTRY_STATE_CITY = "countryStateCity"
    private val TAGS = "tags"

    override fun getName(): String {
        return COUNTRY_STATE_CITY
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(TAGS, "place|country")
        val isCity = termQuery(TAGS, "place|city")
        val isBoundary = termQuery(TAGS, "boundary|administrative")
        val isAdminLevelSix = termQuery("adminLevel", "6")
        val isAdminLevelFour = termQuery("adminLevel", "4")
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix)
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour)

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f).should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary).boost(5f)
    }

}
