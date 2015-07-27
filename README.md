An experimental Open Street Map (OSM) / Nominatim place name auto-complete service.

Motivation:
Allowing users to tag content with a latitude/longitude point loses alot of the context.
Was the user referring to a country, a city or a specific building when they applied the tag?

Can OSM data be used to provide a user friendly auto complete place name service which preserves context by assigning a repeatable location id to each result?

Background:
Many existing location lookup services resolve to a point location only.
The Google Maps geocoding service does provide some context information, but lacks ids which can be advertised and referred back to at a later date.

Some services such as Twitter and Instagram expose their own in-house ids for locations.
The data sets which these ids refer to aren't available to 3rd party developers.

OSM does provide publicly accessible ids for locations.
Resolving the fairly basic OSM schema into a hierarchy is a fairly complicated task.
This task has been tackled by the Nominatim project (http://wiki.openstreetmap.org/wiki/Nominatim).

This means we can infer things like 'if this content is tagged with that street, then it must also be relevant to this city and this country'.
OSM ids are public so we can advertise them and be confident that other applications can used that information.

Nominatims' terms and conditions state that it shouldn't be used
as an auto complete service. This is fair. Using Nominatim in this manner would result in alot of expensive database queries.

This code base dumps out the contents of a populated local Nominatim postgres instance into Elasticsearch which is then exposed as a JSON web service.
This service is able to quickly respond to key stokes as the Nominatim output has now been indexed and does not need to be derived in real time.
The Elasticsearch index can be distrubed to smaller machines than those need to run an entire Nominatim instance.

The Nominatim place name and OSM id is made available in the JSON returned to clients.
The calling application can now persist the OSM id of the selected result for future reference.

Implementation:
Written in Java, backed by Elasticsearch.

An example install containing the whole world dataset is available at http://nominatim-ac.eelpieconsulting.co.uk

Also see:

photon (http://photon.komoot.de/) takes a similar approach and is production ready.
Nominatim AC will be maintained to further investigate the use of search 'profiles' to improve the user experience.
ie. selectively excluding things like post boxes and bus stops.


Installation:
The Elasticsearch index is populated by reading from the Postgres database of a locally running Nominatim instance.

Installing a whole world Nominatim instances is a fairly large undertaking.
From experience a full world Nominatim instance requires a machine with at least 32GB of RAM available.
I struggled to build a consumer machine which could complete a full install.
A whole world install was finally sucessful using a HP DL360 server with 32Gb of RAM (single Xeon processor).

The initial Nominatim import of planet.osm took approimately 6 weeks to complete and consumes around 1TB of disk.
The indexing the full Nominatim install into Elasticsearch takes aroung 48 hours.
The Elasticsearch index is then topped up ever 5 minutes to capture updates feed into Nominatim via Osmosis (as per Nominatims' install instructions).

TODO local compile instructions