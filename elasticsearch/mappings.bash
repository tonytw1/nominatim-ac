curl -XDELETE 'http://localhost:9200/osm20170104/places/_mapping'

curl -XPUT 'http://localhost:9200/osm20170104/places/_mapping' -d '
{
	"places" : { 
		"properties" : { 
			"address" : {
				"type":"string",
				"search_analyzer":"analyzer_startswith",
				"analyzer":"analyzer_startswith"		
			},
			"rank" : {"type" : "integer", "index" : "not_analyzed" },
			"adminLevel" : {"type" : "integer", "index" : "not_analyzed" },
			"tags" : {"type" : "string", "index" : "not_analyzed" },	
			"latlong" : {"type" : "geo_point"},
			"country" : {"type" : "string", "index" : "not_analyzed" }
		} 
	}
}
'
