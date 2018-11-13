curl -XPUT -H "Content-Type: application/json" http://10.0.45.11:32400/nominatimac -d '
{
	"settings":{
		"index":{
			"analysis":{
				"analyzer":{
					"analyzer_startswith":{
						"tokenizer":"keyword",
						"filter":"lowercase"
					}
				}
			}
		}
	}
}
'
