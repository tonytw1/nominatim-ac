curl -XDELETE 'http://localhost:9200/osm/places/_mapping'

curl -XPUT 'http://localhost:9200/osm/places/_mapping' -d '
{
	"places" : { 
		"properties" : { 
			"address" : {
				"type":"string",
				"search_analyzer":"analyzer_startswith",
				"index_analyzer":"analyzer_startswith"		
			},
			"classification" : {"type" : "string", "index" : "not_analyzed" },	
			"type" : {"type" : "string", "index" : "not_analyzed" },	
			"rank" : {"type" : "integer", "index" : "not_analyzed" },
			"tags" : {"type" : "string", "index" : "not_analyzed" },	
			"latlong" : {"type" : "geo_point"},
			"country" : {"type" : "string", "index" : "not_analyzed" }
		} 
	}
}
'
