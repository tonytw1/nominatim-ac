package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder

import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class Country : Profile {

    private val COUNTRY = "country"
    private val TAGS = "tags"

    override fun getName(): String {
        return COUNTRY
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(TAGS, "place|country")
        return boolQuery().minimumShouldMatch(1).should(isCountry)
    }
}
