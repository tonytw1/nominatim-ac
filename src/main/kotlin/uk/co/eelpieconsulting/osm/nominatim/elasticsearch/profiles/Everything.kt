package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class Everything : Profile {

    private val tags = "tags"

    override fun getName(): String {
        return "everything"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isBoundaryHistoric = QueryBuilders.termQuery(tags, "boundary|historic")
        val isRailwayStop = QueryBuilders.termQuery(tags, "railway|stop")

        return QueryBuilders.boolQuery().mustNot(isBoundaryHistoric).mustNot(isRailwayStop)
    }

}
