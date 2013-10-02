curl -XDELETE 'http://localhost:9200/osm/places/_mapping'
curl -XPUT 'http://localhost:9200/osm/places/_mapping' -d '
{ "places" : { "properties" : { 
	"address" : {"type" : "string", "index" : "not_analyzed" }
	} } }
'
