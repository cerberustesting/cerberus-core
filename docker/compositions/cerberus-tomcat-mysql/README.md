# cerberus-tomcat-mysql Cerberus Docker compositions

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

Here you will find information about the `cerberus-tomcat-mysql` Docker composition

## cerberus-tomcat-mysql

The `cerberus-tomcat-mysql` Docker composition runs Cerberus under the Tomcat application server and a [MySQL](https://www.mysql.com/) database.

### Get started

 1. Download the default Docker composition
 
_Note: Optionaly adapt it according to your local config._

 2. Execute at the root path

```
wget https://raw.githubusercontent.com/cerberustesting/cerberus-source/master/docker/compositions/cerberus-tomcat-mysql/docker-compose.yml
docker-compose -f docker-compose.yml up
```

 3. Wait for the images to startup

 4. Open your favorite browser and go to `<docker_host>:8080`, where `<docker_host>` is your Docker host

 5. If this is your first time with Cerberus, the GUI will ask for database initialization. Please read the message on the page and click on the button `Initialize Database`, wait for all queries to execute, then scroll down and click on `Apply Next SQL`.

 6. Go to `<docker_host>:8080` again, it should ask for user credentials. Two default users are created:

   * User `admin`, password `admin`
   * User `cerberus`, password `cerberus`
   
 7. Set runtime configuration, as explained from the version-related [`cerberus-as-tomcat` README](https://github.com/cerberustesting/cerberus-source/tree/master/docker/images/cerberus-as-tomcat/README.md) under section 'Configure the running Cerberus instance'.

### How to run Web Application tests

#### Configure Cerberus to access to the Selenium Grid

By default, the `docker-compose.yml` composition is just executing a Cerberus instance, without any external tool.
 
To start Cerberus with a ready-to-use Selenium Grid, you can run the `docker-compose-with-selenium.yml`
### Associated images

Image                                                                                                           | Description
----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------
[`cerberustesting/cerberus-db-mysql`](https://hub.docker.com/r/cerberustesting/cerberus-db-mysql/)              | Run a Cerberus dedicated MySQL database instance
[`cerberustesting/cerberus-as-tomcat`](https://hub.docker.com/r/cerberustesting/cerberus-as-tomcat/)      | Run a Cerberus instance into a Tomcat application server
[`selenium/hub`](https://hub.docker.com/r/selenium/hub/)                                                        | Run a Selenium Grid instance
[`selenium/node-firefox-debug`](https://hub.docker.com/r/selenium/node-firefox-debug/)                          | Run a Selenium node with Mozilla Firefox and a VNC server installed
[`selenium/node-chrome-debug`](https://hub.docker.com/r/selenium/node-chrome-debug/)                            | Run a Selenium node with Google Chrome and a VNC server installed

### Ports

Hereafter list of reachable ports from your Docker host:

Port             | Description
-----------------|---------------------------------------------------------------------------------
`8080`          | the Cerberus HTTP access port
`3306`          | the MySQL database access port

### Mapped volumes

Hereafter list of mapped volumes:

Service                 | Data volume (Source)                                                          | Host volume (Destination, default values)     | Description
------------------------|-------------------------------------------------------------------------------|-----------------------------------------------| -----------------------------------------------
`cerberus-db-mysql`     | `/var/lib/mysql`                                                              | `./localdata/mysql-db`                        | The MySQL local database directory
`cerberus-as-tomcat`    | `/opt/CerberusMedias/`                                                  | `./localdata/cerberusmedia`                     | The Cerberus media directory (hosting execution screenshot for ex.)


Don't forget to change host volume default values to fit to your need.

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
