package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class Everything : Profile {

    override fun getName(): String {
        return "everything"
    }

    override fun getQuery(): BoolQueryBuilder {
        val isBoundaryHistoric = requiringTag("boundary|historic")
        val isRailwayStop = requiringTag("railway|stop")
        return QueryBuilders.boolQuery().mustNot(isBoundaryHistoric).mustNot(isRailwayStop)
    }

}
