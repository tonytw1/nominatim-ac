package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class Everything: Profile {

    private val TAGS = "tags"

    override fun getName(): String {
       return "everything"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isBoundaryHistoric = QueryBuilders.termQuery(TAGS, "boundary|historic");

        return QueryBuilders.boolQuery().mustNot(isBoundaryHistoric)
    }

}
