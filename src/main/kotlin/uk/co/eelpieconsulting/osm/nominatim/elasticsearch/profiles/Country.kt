package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder

import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery

class Country : Profile {

    private val tags = "tags"

    override fun getName(): String {
        return "country"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isCountry = termQuery(tags, "place|country")
        return boolQuery().minimumShouldMatch(1).should(isCountry)
    }
}
