# Cerberus Docker compositions

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

Here you will find Cerberus Docker compositions

## Available compositions

Hereafter the set of available Cerberus Docker compositions:

Name                                                                        | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------
[`cerberus-glassfish-mysql-compose`](#cerberus-glassfish-mysql-compose)     | Run Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database

### cerberus-glassfish-mysql-compose

The `cerberus-glassfish-mysql-compose` run Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database.

#### How to run it

 1. Clone the [Cerberus Docker files repository](https://github.com/cerberustesting/cerberus-docker):

    `git clone https://github.com/cerberustesting/cerberus-docker.git`

 2. Go to the `compositions` directory

 3. Execute the following command:

    `docker-compose -f ./cerberus-glassfish-mysql-compose.yml up`

 4. Waiting for images startup

 5. Open your favorite browser and go to `<docker_host>:18080/Cerberus`, where `<docker_host>` is your Docker host

#### Associated images

Image                                                                                                                                       | Description
--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------
[`cerberustesting/cerberus-db-mysql`](https://github.com/cerberustesting/cerberus-docker/tree/master/images/cerberus-db-mysql)              | Run a Cerberus dedicated MySQL database instance
[`cerberustesting/cerberus-as-glassfish`](https://github.com/cerberustesting/cerberus-docker/tree/master/images/cerberus-as-glassfish)      | Run a Cerberus instance into a Glassfish application server

#### Ports

Hereafter list of reachable ports from your Docker host:

Port             | Description
-----------------|---------------------------------------------------------------------------------
`18080`          | the Glassfish HTTP access port
`14848`          | the Glassfish administration console access port
`13306`          | the MySQL database access port

#### Mapped volumes

Hereafter list of mapped volumes:

Data volume (Source)        | Host volume (Destination). Default values             | Description
----------------------------|-------------------------------------------------------| ---------------------
`/var/lib/mysql`            | `../localdata/mysql-db`                               | The MySQL local database directory
`/opt/cerberus-screenshots` | `../localdata/screenshots`                            | The Cerberus execution screenshots directory

Don't forget to change host volume default values to fit to your need.

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