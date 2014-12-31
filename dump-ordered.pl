#!/usr/bin/perl

use strict;
use DBI;

my $dbh = DBI->connect("DBI:Pg:dbname=nominatim;port=5432", "tony", "", {'RaiseError' => 1});

my $limits_sth = $dbh->prepare("select min(osm_id) as start, max(osm_id) as end from placex WHERE osm_type='N'");
$limits_sth->execute();
my $limits_ref = $limits_sth->fetchrow_hashref();
my $start = $limits_ref->{'start'};
my $end = $limits_ref->{'end'};
$limits_sth->finish();

print "N: $start -> $end\n";

my $sth = $dbh->prepare("select osm_id, osm_type, class, type, housenumber,
 	get_address_by_language(place_id,  ARRAY['']) AS label,
        get_address_by_language(place_id, ARRAY['name:en']) AS en_label, 
        calculated_country_code AS country,
        case when GeometryType(geometry) = 'POINT' then ST_Y(geometry) else ST_Y(centroid) end as latitude,
        case when GeometryType(geometry) = 'POINT' then ST_X(geometry) else ST_X(centroid) end as longitude,
	rank_address
        FROM placex
        WHERE osm_id >= ? AND osm_id < ? AND osm_type='N'
	AND rank_address > 0 and rank_address <= 16
	ORDER by osm_id, osm_type
	"
);

my $chunksize = 1000;
for (my $i = 0; $i < $end; $i = $i + $chunksize) {
	$sth->execute($i, $i + $chunksize);
	while(my $ref = $sth->fetchrow_hashref()) {
	    my $line = "$ref->{'osm_id'}|$ref->{'osm_type'}|$ref->{'housenumber'}|$ref->{'label'}|$ref->{'en_label'}|$ref->{'class'}|$ref->{'type'}|$ref->{'country'}|$ref->{'latitude'}|$ref->{'longitude'}|$ref->{'rank_address'}";
            $line =~ s/\s/ /g;
            print "$line\n";
	}
}

$sth->finish();
$dbh->disconnect();
