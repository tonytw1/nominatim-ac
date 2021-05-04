curl -XPUT -H "Content-Type: application/json" 'http://localhost:9200/nominatimac/_mapping' -d '
{
		"properties" : { 
			"address" : {
				"type":"text",
				"search_analyzer":"analyzer_startswith",
				"analyzer":"analyzer_startswith"		
			},
			"addressRank" : {"type" : "integer" },
			"adminLevel" : {"type" : "integer"},
			"tags" : {"type" : "keyword" },
			"latlong" : {"type" : "geo_point"},
			"country" : {"type" : "keyword" }
		}
}
'
