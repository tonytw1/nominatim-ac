package uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles;

import org.elasticsearch.index.query.BoolQueryBuilder;

interface Profile {
    fun getName(): String
    fun getQuery(): BoolQueryBuilder
}
