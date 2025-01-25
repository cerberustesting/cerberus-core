# Cerberus tomcat image

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This Docker image run a Cerberus instance into a [Tomcat](http://tomcat.apache.org/) application server.

## Tags

Hereafter list of available tags:

Tag     | Description                        | Source
--------|------------------------------------|-------------------------------
latest  | Use the latest Cerberus version    | [latest/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/latest/Dockerfile)
4.19   | Use the 4.19 Cerberus version     | [4.19/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.19/Dockerfile)
4.18   | Use the 4.18 Cerberus version     | [4.18/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.18/Dockerfile)
4.17   | Use the 4.17 Cerberus version     | [4.17/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.17/Dockerfile)
4.16   | Use the 4.16 Cerberus version     | [4.16/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.16/Dockerfile)
4.15   | Use the 4.15 Cerberus version     | [4.15/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.15/Dockerfile)
4.14   | Use the 4.14 Cerberus version     | [4.14/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.14/Dockerfile)
4.13.1   | Use the 4.13.1 Cerberus version     | [4.13.1/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.13.1/Dockerfile)
4.13   | Use the 4.13 Cerberus version     | [4.13/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.13/Dockerfile)
4.12   | Use the 4.12 Cerberus version     | [4.12/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.12/Dockerfile)
4.11.2   | Use the 4.11.2 Cerberus version     | [4.11.2/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.11.2/Dockerfile)
4.11.1   | Use the 4.11.1 Cerberus version     | [4.11.1/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.11.1/Dockerfile)
4.11   | Use the 4.11 Cerberus version     | [4.11/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.11/Dockerfile)
4.10   | Use the 4.10 Cerberus version     | [4.10/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.10/Dockerfile)
4.9   | Use the 4.9 Cerberus version     | [4.9/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.9/Dockerfile)
4.8   | Use the 4.8 Cerberus version     | [4.8/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.8/Dockerfile)
4.7   | Use the 4.7 Cerberus version     | [4.7/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.7/Dockerfile)
4.6   | Use the 4.6 Cerberus version     | [4.6/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.6/Dockerfile)
4.5   | Use the 4.5 Cerberus version     | [4.5/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.5/Dockerfile)
4.4   | Use the 4.4 Cerberus version     | [4.4/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.4/Dockerfile)
4.3   | Use the 4.3 Cerberus version     | [4.3/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.3/Dockerfile)
4.2   | Use the 4.2 Cerberus version     | [4.2/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.2/Dockerfile)
4.1   | Use the 4.1 Cerberus version     | [4.1/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.1/Dockerfile)
4.0   | Use the 4.0 Cerberus version     | [4.0/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.0/Dockerfile)
4.0-beta  | Use the 4.0-beta Cerberus version    | [4.0-beta/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-as-tomcat/4.0-beta/Dockerfile)

## Prerequisites

This image needs to be linked to a Cerberus database image, as [`cerberus-db-mysql`](https://github.com/cerberustesting/cerberus-source/tree/master/docker/images/cerberus-db-mysql).
See the `DATABASE_*` environment variables bellow for more details.

## How to run this image

### Run the image

This image can simply be run by using the following command:

    docker run -d -P cerberustesting/cerberus-as-tomcat:latest

Note the use of the `-d` and `-P` arguments to let image be run as deamon and open ports outside container which is the common use.

### Image Environment variables

Hereafter list of environment variables that could be overridden when starting the image.

Environment variable                    | Definition                                | Default value
----------------------------------------|-------------------------------------------|--------------------------
`DATABASE_HOST`                         | Cerberus database host                    | `localhost`
`DATABASE_PORT`                         | Cerberus database port                    | `3306`
`DATABASE_NAME`                         | Cerberus database name                    | `cerberus`
`DATABASE_USER`                         | Cerberus database user                    | `cerberus`
`DATABASE_PASSWORD`                     | Cerberus database password                | `toto`


To run image by connecting to a MySQL Cerberus database located at `<database_host>:<database_port>` you could run (assume we are using default values for name, username, and password):

    docker run -d -P -e DATABASE_HOST='<database_host>' -e DATABASE_PORT='<database_port>' cerberustesting/cerberus-as-tomcat:latest

### Image Exposed ports

Hereafter list of exposed ports when image is running (inherited from the [tomcat image](https://hub.docker.com/_/tomcat/)).

Exposed port            | Definition
------------------------|---------------------------------------------------------
`8080`                  | The tomcat HTTP access port

Assume you want to reach to the Cerberus instance, then you would run:

    docker run -d -p 18080:8080 cerberustesting/cerberus-as-tomcat:latest

And you could access to the Cerberus instance by reaching the following URL:

    <docker_host>:18080

Where `<docker_host>` is your Docker host URL.

### Image Volumes

#### Cerberus server logs

#### Cerberus medias

Don't forget to map Cerberus Media Folder to an existing folder out of your Docker host in order to make them persistent. Example:

    docker run [...] -v /your/local/cerberus/medias/directory:/usr/local/tomcat/tomcat/domains/domain1/docroot/CerberusMedias cerberus/cerberus-as-tomcat:latest

Where `/your/local/cerberus/medias/directory` is the directory to store the Cerberus media files out of your Docker host.

#### tomcat data
You can share this volume to persist tomcat settings : 
*  <local_path>/medias:/usr/local/tomcat5/tomcat/domains/<domain_name>
*  <local_path>/lib:/usr/local/tomcat5/tomcat/lib


## Full Example

    docker run -d -P -e DATABASE_HOST='192.168.1.1' -e DATABASE_PORT='13306' -p 18080:8080 -v <path_to_your_cerberus_media_directory>:/opt/CerberusMedias cerberustesting/cerberus-as-tomcat:latest


## Configure the running Cerberus instance

**Important**: Additional runtime configuration has to be made:

### Set the Cerberus base URL

The Cerberus base URL has to be known by Cerberus. To do so, Cerberus has to be configured as the following:

1. Open your favorite web browser to the Cerberus base URL (`<docker_host>:8080` by default)
2. Go to _Administration_ -> _Parameters_
3. Search the `cerberus_url` parameter
4. Set to the Cerberus base URL (`<docker_host>:8080` by default)
5. Save changes 

Note that specific configuration could be made if using [Volumes](#volumes) mapping.

### Set the Media Paths

To apply this runtime configuration to Cerberus instance, then:

1. Open your favorite web browser to the Cerberus base URL (`<docker_host>:18080` by default)
2. Go to _Administration_ -> _Parameters_
3. Search the following parameters and set the corresponding values : 

Parameter            | Value
------------------------|---------------------------------------------------------
`cerberus_applicationobject_path`                  | `/opt/CerberusMedias/objects/`
`cerberus_exeautomedia_path`                  | `/opt/CerberusMedias/executions/`
`cerberus_exemanualmedia_path`                  | `/opt/CerberusMedias/executions-manual/`
`cerberus_ftpfile_path`                  | `/opt/CerberusMedias/ftpfiles/`
`cerberus_testdatalibcsv_path`                  | `/opt/CerberusMedias/csvdata/`

4. Save changes



## Docker compose
An example of docker-compose file is available [here](https://github.com/cerberustesting/cerberus-source/tree/master/docker/compositions/cerberus-tomcat-mysql)


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
