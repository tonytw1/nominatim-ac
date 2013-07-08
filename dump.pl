#!/usr/bin/perl

use DBI;

my $dbh = DBI->connect("DBI:Pg:dbname=nominatim", "osm", "", {'RaiseError' => 1});

my $limits_sth = $dbh->prepare("select min(place_id) as start, max(place_id) as end from placex");
$limits_sth->execute();
my $limits_ref = $limits_sth->fetchrow_hashref();
my $start = $limits_ref->{'start'};
my $end = $limits_ref->{'end'};

$limits_sth->finish();

my $sth = $dbh->prepare("select osm_id, osm_type, class, type, housenumber, 
	get_address_by_language(place_id,  ARRAY['name']) AS label from placex WHERE place_id >= ? AND place_id < ?");
for ($i = $start; $i < $end; $i = $i + 1000) {
	warn "$i/$end\n";
	$sth->execute($i, $i + 1000);      
	while(my $ref = $sth->fetchrow_hashref()) {
	    print "$ref->{'osm_id'}|$ref->{'osm_type'}|$ref->{'housenumber'}|$ref->{'label'}|$ref->{'class'}|$ref->{'type'}\n";
	}
}

$sth->finish();
$dbh->disconnect();
