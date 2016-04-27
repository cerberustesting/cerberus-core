Cerberus MySQL images
====================

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This Docker image run a Cerberus dedicated [MySQL](https://www.mysql.com/) database instance.

Tags
-------------

Hereafter list of available tags:

Tag     | Description                                   | Source
--------|-----------------------------------------------|-------------------------------
latest  | Use the latest compatible MySQL version       | [latest/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-db-mysql/5.6/Dockerfile)
5.6     | Use the 5.6 MySQL version                     | [1.1.3/Dockerfile](https://github.com/cerberustesting/cerberus-docker/blob/master/images/cerberus-db-mysql/5.6/Dockerfile)

How to run this image
-------------

This image can simply be run by using the following command:

    docker run -d -P cerberus/cerberus-db-mysql:latest

Note the use of the `-d` and `-P` arguments to let image be run as deamon and open ports outside container which is the common use.

A common use is to map the `/var/lib/mysql` volume outside container. To do this, simply add the following argument to the previous command :

    -v <path_to_your_data_directory>:/var/lib/mysql

Environment variables
-------------

All the environment variables are inherited from the MySQL image. Look at the [documentation](https://hub.docker.com/_/mysql/) for more details.

Exposed ports
-------------

All the exposed ports are inherited from the MySQL image. Look at the [documentation](https://hub.docker.com/_/mysql/) for more details.