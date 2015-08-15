curl -XDELETE 'http://localhost:9200/osm20150815/'

curl -XPUT http://localhost:9200/osm20150815/ -d '
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

