# Cerberus Docker compositions

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

Here you will find Cerberus Docker compositions

## How to run a composition

Assume the Docker composition file is located to ./my-cerberus-compose.yml, you can run this composition as the following:

    docker-compose -f ./my-cerberus-compose.yml up

Then the Docker composition is running and exposed ports can be reached.

## Available compositions

Hereafter the set of available Cerberus Docker compositions:

Name                                                                        | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------
[`cerberus-glassfish-mysql-compose`](#cerberus-glassfish-mysql-compose)     | Run Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database

### cerberus-glassfish-mysql-compose

The `cerberus-glassfish-mysql-compose` run Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database.

#### How to run

 1. Execute the following command :

    `docker-compose -f ./cerberus-glassfish-mysql-compose.yml up`

 2. Waiting for images startup

 3. Open your browser and go to `<docker_host>:18080/Cerberus`, where `<docker_host>` is your Docker host

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