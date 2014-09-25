#!/usr/bin/perl

use DBI;

my $dbh = DBI->connect("DBI:Pg:dbname=nominatim;port=5432", "tony", "", {'RaiseError' => 1});

my $limits_sth = $dbh->prepare("select min(place_id) as start, max(place_id) as end from placex");
$limits_sth->execute();
my $limits_ref = $limits_sth->fetchrow_hashref();
my $start = $limits_ref->{'start'};
my $end = $limits_ref->{'end'};

$limits_sth->finish();

my $sth = $dbh->prepare("select osm_id, osm_type, class, type, housenumber, 
	get_address_by_language(place_id,  ARRAY['name:en']) AS label, 
	calculated_country_code AS country,
	case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,
        case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude
	FROM placex
	WHERE place_id >= ? AND place_id < ?
	ORDER by place_id
	");

for ($i = $start; $i < $end; $i = $i + 1000) {
	#warn "$i/$end\n";
	$sth->execute($i, $i + 1000);      
	while(my $ref = $sth->fetchrow_hashref()) {
	    my $line = "$ref->{'osm_id'}|$ref->{'osm_type'}|$ref->{'housenumber'}|$ref->{'label'}|$ref->{'class'}|$ref->{'type'}|$ref->{'country'}|$ref->{'latitude'}|$ref->{'longitude'}";
            $line =~ s/\s/ /g;
            print "$line\n";
	}
}

$sth->finish();
$dbh->disconnect();
