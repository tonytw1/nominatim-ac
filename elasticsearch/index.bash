curl -XDELETE 'http://localhost:9200/osm20170104/'

curl -XPUT http://localhost:9200/osm20170104/ -d '
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
