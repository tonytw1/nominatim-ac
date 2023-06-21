package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery

class Country : Profile {

    override fun getName(): String {
        return "country"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = requiringTag("place|country")
        return boolQuery().minimumShouldMatch(1).should(isCountry)
    }

}
