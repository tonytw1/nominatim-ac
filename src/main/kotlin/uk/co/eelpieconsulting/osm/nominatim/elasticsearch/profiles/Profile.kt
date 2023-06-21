package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.TermQueryBuilder

interface Profile {

    fun getName(): String
    fun getQuery(): BoolQueryBuilder

    fun requiringTag(tag: String): TermQueryBuilder {
        val tags = "tags"
        return QueryBuilders.termQuery(tags, tag)
    }

    fun requiringAdminLevel(tag: String): TermQueryBuilder {
        val tags = "adminLevel"
        return QueryBuilders.termQuery(tags, tag)
    }

}
