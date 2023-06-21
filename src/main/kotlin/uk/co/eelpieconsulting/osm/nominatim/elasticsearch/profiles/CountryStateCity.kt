package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery

class CountryStateCity : Profile {

    override fun getName(): String {
        return "countryStateCity"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = requiringTag("place|country")
        val isCity = requiringTag("place|city")
        val isBoundary = requiringTag("boundary|administrative")
        val isAdminLevelSix = requiringAdminLevel("6")
        val isAdminLevelFour = requiringAdminLevel("4")
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix)
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour)

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f)
            .should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary).boost(5f)
    }

}
