curl -XDELETE 'http://localhost:9200/osm/'

curl -XPUT http://localhost:9200/osm/ -d '
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

