# Cerberus Glassfish images

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This Docker image run a Cerberus instance into a [Glassfish](https://glassfish.java.net/) application server.

## Tags

Hereafter list of available tags:

Tag     | Description                        | Source
--------|------------------------------------|-------------------------------
latest  | Use the latest Cerberus version    | [latest/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.5/Dockerfile)
1.1.5   | Use the 1.1.5   Cerberus version   | [1.1.5/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.5/Dockerfile)
1.1.4   | Use the 1.1.4   Cerberus version   | [1.1.4/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.4/Dockerfile)
1.1.3.1 | Use the 1.1.3.1 Cerberus version   | [1.1.3.1/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.3.1/Dockerfile)
1.1.3   | Use the 1.1.3 Cerberus version     | [1.1.3/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.3/Dockerfile)

## Prerequisites

This image needs to be linked to a Cerberus database image, e.g., `cerberus-db-mysql`.

See the `DATABASE_*` environment variables on the Dockerfile for more information.

## How to run this image

This image can simply be run by using the following command:

    docker run -d -P cerberus/cerberus-as-glassfish:latest

Note the use of the `-d` and `-P` arguments to let image be run as deamon and open ports outside container which is the common use.

To run image by connecting to a MySQL Cerberus database located at `<database_host>:<database_port>` you could run (assume we are using default values for database type, name, username, and password):

    docker run -d -P -e DATABASE_HOST='<database_host>' -e DATABASE_PORT='<database_port>' cerberus/cerberus-as-glassfish:latest

## Environment variables

Hereafter list of environment variables that could be overridden when starting the image.

Environment variable        | Definition                    | Default value
----------------------------|-------------------------------|--------------------------
`DATABASE_TYPE`             | Cerberus database type        | `mysql` or `mariadb` only
`DATABASE_HOST`             | Cerberus database host        | `localhost`
`DATABASE_PORT`             | Cerberus database port        | `3306`
`DATABASE_NAME`             | Cerberus database name        | `cerberus`
`DATABASE_USER`             | Cerberus database user        | `cerberus`
`DATABASE_PASSWORD`         | Cerberus database password    | `toto`

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

From the [1.1.3.1](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.3.1/Dockerfile) version, the new `/opt/cerberus-screenshots` directory is created to store Cerberus execution screenshots.
Don't forget to map it to an existing folder out of your Docker host in order to make them persistent. Example:

    docker run [...] -v /your/cerberus/screenshots/directory:/opt/cerberus-screenshots cerberus/cerberus-as-glassfish:latest

Where `/your/cerberus/screenshots/directory` is the directory to store the Cerberus execution screenshots out of your Docker host.

From the [1.1.5](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-as-glassfish/1.1.5/Dockerfile) version, the new `/opt/glassfish` directory is created to store the Glassfish home directory and let you map it to an existing folder out of your Docker host. Example:

    docker run [...] -v /your/glassfish/home/directory:/opt/glassfish cerberus/cerberus-as-glassfish:latest

Where `/your/glassfish/home/directory` is the directory to store the Glassfish home directory out of your Docker host.

Note: The setup process put the `.cerberus` file to the `${GLASSFISH_HOME}` root directory. This marker file is used to know if setup has already be done and then to not setup Glassfish twice. In case of using an existing Cerberus configured Glassfish instance, beware to put this file to your Glassfish home directory in order to by-pass the setup process.

## License

Cerberus Copyright (C) 2016 Cerberus Testing

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
