# Setup Nominatim 3.7 Docker image and import Great Britain extract for local development
docker run -it --rm -e PBF_URL=file:///osm/great-britain-210607.osm.pbf -e REPLICATION_URL=https://download.geofabrik.de/europe/great-britain-updates/ -v /home/tony/nominatim37/main:/var/lib/postgresql/12/main -v /home/tony/osm/:/osm -p 8080:8080 -p 5432:5432 --name nominatim mediagis/nominatim:3.7

