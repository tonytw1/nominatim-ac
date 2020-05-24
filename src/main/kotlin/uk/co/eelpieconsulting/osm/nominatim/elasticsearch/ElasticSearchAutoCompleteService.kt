package uk.co.eelpieconsulting.osm.nominatim.elasticsearch

import com.google.common.base.Strings
import com.google.common.collect.Lists
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.index.query.PrefixQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.osm.nominatim.elasticsearch.profiles.*
import uk.co.eelpieconsulting.osm.nominatim.json.JsonDeserializer
import uk.co.eelpieconsulting.osm.nominatim.model.DisplayPlace
import java.io.IOException

@Component
class ElasticSearchAutoCompleteService @Autowired constructor(private val elasticSearchClientFactory: ElasticSearchClientFactory, private val jsonDeserializer: JsonDeserializer,
                                                              @param:Value("\${elasticsearch.index.read}") private val readIndex: String) {

    private val SEARCH_TYPE = "places" // TODO ElasticSearchIndexer.TYPE;
    private val ADDRESS = "address"
    private val DEFAULT_RADIUS = "100km"
    private val LATLONG = "latlong"
    private val TAGS = "tags"

    private val availableProfiles: MutableList<Profile>

    init {
        availableProfiles = Lists.newArrayList()
        availableProfiles.add(Country())
        availableProfiles.add(CountryCityTownSuburb())
        availableProfiles.add(CountryStateCity())
        availableProfiles.add(Everything())
    }

    fun getAvailableProfiles(): List<Profile> {
        return availableProfiles
    }

    @Throws(IOException::class)
    fun search(q: String?, tag: String?, lat: Double?, lon: Double?, radius: Double?, rank: Int?, country: String?, profileName: String?): List<DisplayPlace> {
        if (q.isNullOrEmpty()) {
            return Lists.newArrayList()
        }

        var profile: Profile = Everything()

        profileName.let { pn ->
            for (p in availableProfiles) {  // TODO find
                if (p.getName().equals(pn)) {
                    profile = p
                }
            }
        }

        var query = profile.getQuery()
        query = query.must(startsWith(q))
        if (!Strings.isNullOrEmpty(tag)) {
            query = query.must(QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TAGS, tag)))
        }
        if (rank != null) {
            query = query.must(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("rank", rank)))
        }
        if (!Strings.isNullOrEmpty(country)) {
            query = query.must(QueryBuilders.termQuery("country", country))
        }
        if (lat != null && lon != null) {
            val distance = if (radius != null) java.lang.Double.toString(radius) + "km" else DEFAULT_RADIUS
            val geoCircle = QueryBuilders.geoDistanceQuery(LATLONG).point(lat, lon).distance(distance)
            query = query.must(QueryBuilders.boolQuery().must(geoCircle))
        }
        return executeAndParse(query)
    }

    @Throws(IOException::class)
    fun indexedItemsCount(): Long {
        val all = QueryBuilders.boolQuery()
        val searchRequest = SearchRequest(readIndex)
        searchRequest.types(SEARCH_TYPE)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(all)
        searchSourceBuilder.size(0)
        searchRequest.source(searchSourceBuilder)

        val client = elasticSearchClientFactory.getClient()
        val response = client.search(searchRequest, RequestOptions.DEFAULT)
        return response.hits.totalHits
    }

    @Throws(IOException::class)
    private fun executeAndParse(query: QueryBuilder): List<DisplayPlace> {
        val client = elasticSearchClientFactory.getClient()
        val searchRequest = SearchRequest(readIndex)
        searchRequest.types(SEARCH_TYPE)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(query)
        searchSourceBuilder.sort(SortBuilders.fieldSort("adminLevel").order(SortOrder.ASC)).sort(SortBuilders.fieldSort("addressRank").order(SortOrder.ASC))
        searchRequest.source(searchSourceBuilder)
        val response = client.search(searchRequest, RequestOptions.DEFAULT)
        val places: MutableList<DisplayPlace> = Lists.newArrayList()
        for (i in response.hits.hits.indices) {
            val searchHit = response.hits.hits[i]
            val (osmId, osmType, _, address, classification, type, addressRank, latlong, _, country, adminLevel) = jsonDeserializer.deserializePlace(searchHit.sourceAsString)
            places.add(DisplayPlace(osmId, osmType, address, classification,
                    type, latlong, country!!, type,
                    adminLevel, addressRank))
        }
        return places
    }

    private fun startsWith(q: String): PrefixQueryBuilder {
        return QueryBuilders.prefixQuery(ADDRESS, q.toLowerCase())
    }

}