An experimental Open Street Map (OSM) / Nominatim place name auto-complete service.

Indexes the contents of a Nominatim 3.7 database into Elasticsearch version 7.8.

An example containing the whole planet data set is available at (https://nominatim-ac.eelpieconsulting.co.uk).


### Motivation

Allowing users to tag content with just a latitude/longitude point loses a lot of the context.
Was the user referring to a country, a city or a specific building when they applied the tag?

Can OSM data be used to provide a user friendly auto complete place name service which preserves context by assigning a repeatable location id to each result?

### Background (circa 2013)

Many existing location lookup services resolve to a point location only.
The Google Maps geocoding service provided some context information but lacked persistent ids which can be advertised and referred back to at a later date
(it now does).

Some services such as Twitter and Instagram exposed their own in-house location ids
but data ids referred to wasn't available to 3rd party developers.

OSM does provide publicly accessible ids for locations.
Resolving the fairly basic OSM schema into a hierarchy is a fairly complicated task.
This has been tackled by the Nominatim project (http://wiki.openstreetmap.org/wiki/Nominatim).

This means we can infer things like 'if this content is tagged with that street, then it must also be relevant to this city and this country'.
OSM ids are public so we can advertise them and be confident that other applications can make use of that information.

The Nominatim terms and conditions state that it shouldn't be used as an auto complete service.
This is fair. Using Nominatim in this manner would result in a lot of expensive database queries.



### Implementation

Indexes the contents of a populated Nominatim postgres instance into Elasticsearch and exposes it as a JSON web service.

Pages through the entire `placex` table and indexes the output of the `get_address_by_language` function by osm id.

This is a very naive approach which has some pros and cons (see below).
There is some useful intuition here around reliably paging through the entire places table.

The service is able to quickly respond to key stokes as the Nominatim output has been pre-rendered and indexed and does not need to be calculated in real time.
The Elasticsearch index can be distributed to smaller machines than those needed to run a whole planet Nominatim instance.

The Nominatim place name and OSM id is made available in the JSON returned to clients.
The calling application can now persist the OSM id of the selected result for future reference.


#### Does well

Very fast response time when running on a modest machine.

#### Does not do well

Does not deal with look aheads.

These examples do not work well:

`Boscanova` does not match for `Cafe Boscanova`.
`Dublin Ireland` does not match for `Dublin, County Dublin, Leinster, Ireland`

Does not offer short names:

`Dublin, County Dublin, Leinster, Ireland` should also be offered as `Dublin, Ireland`

An improvement would probably involve indexing the more structured output of the `get_addressdata` function.


### End points

The application exposes these JSON end points which client applications can call:

#### /search

| Parameter          | Description                                                   |
|--------------------|---------------------------------------------------------------|
| q	                 | 	The user query. ie. London                                   |
| tag	               | Restrict results to a given OSM tag.                          |
| lat / lon / radius | Restrict results to a given geocircle                         |
| country            | Restrict results to a given country code                      |
| callback           | The name of an optional JSONP callback to wrap the results in |

#### /places/{id}

Retrieve a single place by the `id` provided in search results.

Potentially useful for applications which have identified an OSM id using autocomplete
and need to retrieve it's details later in a flow but do not have access to a Nominatim API.


### Also see

photon (https://photon.komoot.io/) takes a similar approach and is production ready.

Nominatim AC will be maintained to further investigate the use of search 'profiles' to improve the user experience.
ie. selectively excluding things like post boxes and bus stops.



### Local development

This is a Spring Boot / Kotlin project with a Gradle build.

Review the configuration
Configuration is in the file named `application.properties`.

Start Elasticsearch:
```
docker-compose -f docker-compose/docker-compose.yml up
```

Build locally with:
```
./gradlew clean build -x test
```

Start locally

```
mv build/libs/nominatim-ac-0.1.0.jar .
java -jar nominatim-ac-0.1.0.jar 
```
The Elastic index will be created automatically if it does not already exist.


Populate index

```
curl http://localhost:8080/import
```

A full index is around 16Gb in size.
It takes 18 hours to index; this could probably be improved with threading.


The tests are expecting to see a Postgres Nominatim 3.7 schema containing a June 2021 Great Britain import
on localhost port 5432.

Mediagis have published a Nominatim Docker build which can be used for local development (https://github.com/mediagis/nominatim-docker).


### Cloud build

We use Google Cloud Build to produce a container image.

```
gcloud components install cloud-build-local
cloud-build-local --config=cloudbuild.yaml --dryrun=false --push=false .
```


### Production installation

The Elasticsearch index is populated by reading from the Postgres database of a locally running Nominatim instance.

Installing a whole world Nominatim instances is a fairly large undertaking.
From experience a full world Nominatim instance requires a machine with at least 32GB of RAM available.
I struggled to build a consumer machine which could complete ea full install. A full install was

Back in 2013 the Nominatim import of planet.osm took approximately 6 weeks to complete and consumed around 1TB of disk
(single CPU HP DL360 server with 48Gb of RAM; single SATA SSD drive).

In 2021 this import takes around 3 days (HP z620; dual CPU; 96Gb RAM; 1.5TB of NVME SSD).




