# Cerberus Glassfish image

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This Docker image run a Cerberus instance into a [Glassfish](https://glassfish.java.net/) application server.

## Tags

Hereafter list of available tags:

Tag     | Description                        | Source
--------|------------------------------------|-------------------------------
latest  | Use the latest Cerberus version    | [latest/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/latest/Dockerfile)
1.1.12   | Use the 1.1.12 Cerberus version     | [1.1.12/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.12/Dockerfile)
1.1.10   | Use the 1.1.10   Cerberus version   | [1.1.10/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.10/Dockerfile)
1.1.9   | Use the 1.1.9   Cerberus version   | [1.1.9/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.9/Dockerfile)
1.1.8   | Use the 1.1.8   Cerberus version   | [1.1.8/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.8/Dockerfile)
1.1.7   | Use the 1.1.7   Cerberus version   | [1.1.7/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.7/Dockerfile)
1.1.6   | Use the 1.1.6   Cerberus version   | [1.1.6/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.6/Dockerfile)
1.1.5   | Use the 1.1.5   Cerberus version   | [1.1.5/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.5/Dockerfile)
1.1.4   | Use the 1.1.4   Cerberus version   | [1.1.4/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.4/Dockerfile)
1.1.3.1 | Use the 1.1.3.1 Cerberus version   | [1.1.3.1/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.3.1/Dockerfile)
1.1.3   | Use the 1.1.3 Cerberus version     | [1.1.3/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.3/Dockerfile)

## Prerequisites

This image needs to be linked to a Cerberus database image, as [`cerberus-db-mysql`](https://github.com/cerberustesting/cerberus-docker/tree/master/images/cerberus-db-mysql).
See the `DATABASE_*` environment variables bellow for more details.

Note, from the [1.1.9](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.9/Dockerfile) version, the `cerberus-as-glassfish` is only compatible with the [MySQL](http://www.mysql.com/) dialect, so only usable by linking to the [`cerberus-db-mysql`](https://github.com/cerberustesting/cerberus-docker/tree/master/images/cerberus-db-mysql) Cerberus database image.

## How to run this image

### Run the image

This image can simply be run by using the following command:

    docker run -d -P cerberus/cerberus-as-glassfish:latest

Note the use of the `-d` and `-P` arguments to let image be run as deamon and open ports outside container which is the common use.

To run image by connecting to a MySQL Cerberus database located at `<database_host>:<database_port>` you could run (assume we are using default values for name, username, and password):

    docker run -d -P -e DATABASE_HOST='<database_host>' -e DATABASE_PORT='<database_port>' cerberus/cerberus-as-glassfish:latest

### Configure the running Cerberus instance

**Important**: Additional runtime configuration has to be made:

#### Set the Cerberus base URL

The Cerberus base URL has to be known by Cerberus. To do so, Cerberus has to be configured as the following:

1. Open your favorite web browser to the Cerberus base URL (`<docker_host>:18080/Cerberus` by default)
2. Go to _Administration_ -> _Parameters_
3. Search the `cerberus_url` parameter
4. Set to the Cerberus base URL (`<docker_host>:18080/Cerberus` by default)
5. Save changes 

Note that specific configuration could be made if using [Volumes](#volumes) mapping.

## Environment variables

Hereafter list of environment variables that could be overridden when starting the image.

Environment variable                    | Definition                                | Default value
----------------------------------------|-------------------------------------------|--------------------------
`DATABASE_HOST`                         | Cerberus database host                    | `localhost`
`DATABASE_PORT`                         | Cerberus database port                    | `3306`
`DATABASE_NAME`                         | Cerberus database name                    | `cerberus`
`DATABASE_USER`                         | Cerberus database user                    | `cerberus`
`DATABASE_PASSWORD`                     | Cerberus database password                | `toto`
`GLASSFISH_HTTP_THREADPOOL_MAX_SIZE`    | Glassfish HTTP thread pool maximum size   | `500`

## Exposed ports

Hereafter list of exposed ports when image is running (inherited from the [Glassfish image](https://hub.docker.com/_/glassfish/)).

Exposed port            | Definition
------------------------|---------------------------------------------------------
`8080`                  | The Glassfish HTTP access port
`4848`                  | The Glassfish HTTP access port

Assume you want to reach to the Cerberus instance, then you would run:

    docker run -d -p 18080:8080 -p 14848:4848 cerberus/cerberus-as-glassfish:latest

And you could access to the Cerberus instance by reaching the following URL:

    <docker_host>:18080/Cerberus

Where `<docker_host>` is your Docker host URL.

With this example, you could access to the Glassfish administration console by reaching the following URL:

    <docker_host>:14848

## Volumes

### Cerberus server logs

Cerberus server logs can be persisted by using the following command:

    docker run [...] -v /your/local/cerberus/server/logs/directory:/usr/local/glassfish4/glassfish/domains/domain1/logs cerberus/cerberus-as-glassfish:latest

Where `/your/local/cerberus/screenshots/directory` is the directory to store the Cerberus execution screenshots out of your Docker host.

### Cerberus screenshots

From the [1.1.5](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.5/Dockerfile) version, the new `/usr/local/glassfish4/glassfish/domains/domain1/docroot/CerberusPictures` directory is created to store Cerberus execution screenshots.
Don't forget to map it to an existing folder out of your Docker host in order to make them persistent. Example:

    docker run [...] -v /your/local/cerberus/screenshots/directory:/usr/local/glassfish4/glassfish/domains/domain1/docroot/CerberusPictures cerberus/cerberus-as-glassfish:latest

Where `/your/local/cerberus/screenshots/directory` is the directory to store the Cerberus execution screenshots out of your Docker host.

To apply this runtime configuration to Cerberus instance, then:

1. Open your favorite web browser to the Cerberus base URL (`<docker_host>:18080/Cerberus` by default)
2. Go to _Administration_ -> _Parameters_
3. Search the `cerberus_picture_path` parameter
4. Set to the `/usr/local/glassfish4/glassfish/domains/domain1/docroot/CerberusPictures/` value (note the important trailing `/`)
5. Save changes
3. Search the `cerberus_picture_url` parameter
4. Set to the Cerberus base URL value (`<docker_host>:18080/CerberusPictures/` by default, note the important trailing `/`)
5. Save changes

From the [1.1.6](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.6/Dockerfile) version, `cerberus_picture_path` and `cerberus_picture_url` parameters have been respectively renamed `cerberus_mediastorage_path` and `cerberus_mediastorage_path`.

## License

Cerberus Copyright (C) 2013 - 2017 cerberustesting

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.