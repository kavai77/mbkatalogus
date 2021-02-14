# PDF Catalog generator for Madal Bal

## Building
`mvn package`

## Running locally with docker
```
docker run -it --rm -p 8080:8080 --name mbkatalogus -e SPRING_PROFILES_ACTIVE=szerb \
--mount type=bind,source=<IMAGE_DIR_ON_HOST>,target=/usr/share/cikkek \
--mount type=bind,source=<LOGO_DIR_ON_HOST>,target=/usr/share/logok \
--mount type=volume,source=mbkatalogusdb,target=/usr/share/db \
docker.himadri.eu:5000/kavai77/mbkatalogus:1.5
```
Then visit: http://localhost:8080

`IMAGE_DIR_ON_HOST` and `LOGO_DIR_ON_HOST` must point to existing directories on the host where the images and logos are found.
They can be also the same directory.

Available profiles:
* `szerszam`
* `bio`
* `szerb`

### Example Image and Logo directory setup
```
docker run -it --rm -p 8080:8080 --name mbkatalogus -e SPRING_PROFILES_ACTIVE=szerb \
--mount type=bind,source="$(pwd)"/project-files/cikkek/,target=/usr/share/cikkek \
--mount type=bind,source="$(pwd)"/project-files/logok/,target=/usr/share/logok \
--mount type=volume,source=mbkatalogusdb,target=/usr/share/db \
docker.himadri.eu:5000/kavai77/mbkatalogus:1.5
```
