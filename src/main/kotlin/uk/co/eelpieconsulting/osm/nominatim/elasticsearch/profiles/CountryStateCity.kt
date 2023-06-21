package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class CountryStateCity : Profile {

    private val tags = "tags"

    override fun getName(): String {
        return "countryStateCity"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(tags, "place|country")
        val isCity = termQuery(tags, "place|city")
        val isBoundary = termQuery(tags, "boundary|administrative")
        val isAdminLevelSix = termQuery("adminLevel", "6")
        val isAdminLevelFour = termQuery("adminLevel", "4")
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix)
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour)

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f).should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary).boost(5f)
    }

}
