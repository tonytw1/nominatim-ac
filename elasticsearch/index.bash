curl -XDELETE 'http://localhost:9200/osm20160806/'

curl -XPUT http://localhost:9200/osm20160806/ -d '
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
