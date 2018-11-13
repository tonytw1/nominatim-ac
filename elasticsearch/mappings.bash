curl -XPUT -H "Content-Type: application/json" 'http://10.0.45.11:32400/nominatimac/places/_mapping' -d '
{
	"places" : { 
		"properties" : { 
			"address" : {
				"type":"text",
				"search_analyzer":"analyzer_startswith",
				"analyzer":"analyzer_startswith"		
			},
			"rank" : {"type" : "integer" },
			"adminLevel" : {"type" : "integer"},
			"tags" : {"type" : "keyword" },
			"latlong" : {"type" : "geo_point"},
			"country" : {"type" : "keyword" }
		} 
	}
}
'
