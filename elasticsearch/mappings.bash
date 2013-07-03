curl -XDELETE 'http://172.16.123.129:9200/osm/places/_mapping'
curl -XPUT 'http://172.16.123.129:9200/osm/places/_mapping' -d '
{ "places" : { "properties" : { 
	"address" : {"type" : "string", "index" : "not_analyzed" }
	} } }
'
