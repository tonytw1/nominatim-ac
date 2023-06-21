package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class CountryCityTownSuburb : Profile {

    private val tags = "tags"

    override fun getName(): String {
        return "countryCityTownSuburb"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(tags, "place|country")
        val isCity = termQuery(tags, "place|city")
        val isCounty = termQuery(tags, "place|county")
        val isTown = termQuery(tags, "place|town")
        val isSuburb = termQuery(tags, "place|suburb")
        val isNationalPark = termQuery(tags, "boundary|national_park")
        val isLeisurePark = termQuery(tags, "leisure|park")
        val isLeisureCommon = termQuery(tags, "leisure|common")
        val isPeak = termQuery(tags, "natural|peak")
        val isIsland = termQuery(tags, "place|island")
        val isVillage = termQuery(tags, "place|village")
        val isBoundary = termQuery(tags, "boundary|administrative")
        val isAdminLevelFour = termQuery("adminLevel", "4")
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour)
        val isAdminLevelSix = termQuery("adminLevel", "6")
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix)

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f)
            .should(isNationalPark).boost(8f).should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary)
            .boost(5f).should(isCounty).boost(4f).should(isTown).boost(3f).should(isPeak).boost(3f).should(isIsland)
            .should(isLeisurePark).should(isLeisureCommon).should(isVillage).should(isSuburb)
    }

}
