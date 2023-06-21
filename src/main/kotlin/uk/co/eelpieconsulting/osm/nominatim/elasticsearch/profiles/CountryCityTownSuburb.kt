package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery

class CountryCityTownSuburb : Profile {

    override fun getName(): String {
        return "countryCityTownSuburb"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = requiringTag("place|country")
        val isCity = requiringTag("place|city")
        val isCounty = requiringTag("place|county")
        val isTown = requiringTag("place|town")
        val isSuburb = requiringTag("place|suburb")
        val isNationalPark = requiringTag("boundary|national_park")
        val isLeisurePark = requiringTag("leisure|park")
        val isLeisureCommon = requiringTag("leisure|common")
        val isPeak = requiringTag("natural|peak")
        val isIsland = requiringTag("place|island")
        val isVillage = requiringTag("place|village")
        val isBoundary = requiringTag("boundary|administrative")
        val isAdminLevelFour = requiringAdminLevel("4")
        val isAdminLevelFourBoundary = boolQuery().must(isBoundary).must(isAdminLevelFour)
        val isAdminLevelSix = requiringAdminLevel("6")
        val isAdminLevelSixBoundary = boolQuery().must(isBoundary).must(isAdminLevelSix)

        return boolQuery().minimumShouldMatch(1).should(isCountry).boost(10f).should(isCity).boost(8f)
            .should(isNationalPark).boost(8f).should(isAdminLevelFourBoundary).boost(6f).should(isAdminLevelSixBoundary)
            .boost(5f).should(isCounty).boost(4f).should(isTown).boost(3f).should(isPeak).boost(3f).should(isIsland)
            .should(isLeisurePark).should(isLeisureCommon).should(isVillage).should(isSuburb)
    }

}
