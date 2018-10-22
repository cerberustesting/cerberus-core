# Cerberus MySQL image

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This Docker image run a Cerberus dedicated [MySQL](https://www.mysql.com/) database instance.

## Tags

Hereafter list of available tags:

Tag     | Description                                   | Source
--------|-----------------------------------------------|-------------------------------
latest  | Use the latest compatible MySQL version       | [latest/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-db-mysql/5.6/Dockerfile)
5.6     | Use the 5.6 MySQL version                     | [5.6/Dockerfile](https://github.com/cerberustesting/cerberus-source/blob/master/docker/images/cerberus-db-mysql/5.6/Dockerfile)

## How to run this image

This image can simply be run by using the following command:

    docker run -d -P cerberustesting/cerberus-db-mysql:latest

Note the use of the `-d` and `-P` arguments to let image be run as deamon and open ports outside container which is the common use.

A common use is to map the `/var/lib/mysql` volume outside container. To do this, simply add the following argument to the previous command :

    -v <path_to_your_data_directory>:/var/lib/mysql

## Full Example

    docker run -d -P -p 13306:3306 -v <path_to_your_data_directory>:/var/lib/mysql cerberustesting/cerberus-db-mysql:latest

## Environment variables

All the environment variables are inherited from the MySQL image. Look at the [documentation](https://hub.docker.com/_/mysql/) for more details.

## Exposed ports

All the exposed ports are inherited from the MySQL image. Look at the [documentation](https://hub.docker.com/_/mysql/) for more details.

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
