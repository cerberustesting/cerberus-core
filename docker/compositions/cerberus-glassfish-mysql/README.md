# cerberus-glassfish-mysql Cerberus Docker compositions

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

Here you will find information about the `cerberus-glassfish-mysql` Docker composition

## cerberus-glassfish-mysql

The `cerberus-glassfish-mysql` Docker composition runs Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database.

It runs also a [Selenium Grid](http://www.seleniumhq.org/docs/07_selenium_grid.jsp), initialized with two nodes with respectively [Mozilla Firefox](https://www.mozilla.org/en-US/firefox/products/) and [Google Chrome](https://www.google.com/chrome/browser/index.html) Web browsers to get ready to run Web Application tests.

### Get started

 1. [Download the default Docker composition](https://raw.githubusercontent.com/cerberustesting/cerberus-source/master/docker/compositions/cerberus-glassfish-mysql/default.yml).
 
_Note: Optionaly adapt it according to your local config._

 2. Execute at the root path
	
        docker-compose -f default.yml up

 3. Wait for the images to startup

 4. Open your favorite browser and go to `<docker_host>:18080/Cerberus`, where `<docker_host>` is your Docker host

 5. If this is your first time with Cerberus, the GUI will ask for database initialization. Please read the message on the page and click on the button `Initialize Database`, wait for all queries to execute, then scroll down and click on `Apply Next SQL`.

 6. Go to `<docker_host>:18080/Cerberus` again, it should ask for user credentials. Two default users are created:

   * User `admin`, password `admin`
   * User `cerberus`, password `cerberus`
   
 7. Set runtime configuration, as explained from the version-related [`cerberus-as-glassfish` README](https://github.com/cerberustesting/cerberus-source/tree/master/docker/images/cerberus-as-glassfish/README.md) under section 'Configure the running Cerberus instance'.

### How to run Web Application tests

#### Configure Cerberus to access to the Selenium Grid

By default, the `default.yml` composition is just executing a Cerberus instance, without any external tool.
 
To start Cerberus with a ready-to-use Selenium Grid, you can run the `default-with-selenium.yml` composition. Once started, a Selenium Grid will be reachable by applying the following configuration the Cerberus Execution or Robot page:

- Selenium ip/host: `selenium-hub`
- Selenium port: `4444`
- Platform: `LINUX`
- Browser: `firefox` or `chrome`

_Note: The Selenium Grid, Mozilla Firefox node and Google Chrome node are all under the **2.53.0** version._

#### Access to the Selenium Grid Console

The Selenium Grid Console can be reached at `<docker_host>:14444/grid/console`.

#### View running test on browser

The Selenium Grid is using the _debug_ version of the associated nodes.
This way, nodes integrate a [VNC server](https://fr.wikipedia.org/wiki/Virtual_Network_Computing) that can be reached through a VNC client.

If you use the default `default-with-selenium.yml` Docker composition configuration then you could connect to a Selenium node by: 

1. Getting the port from which the Selenium node you want to connect (`15900` for Mozilla Firefox, `15901` for Google Chrome). Let's name it `<selenium_node_port>`
2. Connect your VNC client by:
  * Using `<docker_host>:<selenium_node_port>` as URL
  * Using `secret` as prompted password

#### Selenium nodes scaling

By default, the `default-with-selenium.yml` Docker composition starts one [Mozilla Firefox Selenium node]((https://hub.docker.com/r/selenium/node-firefox-debug/)) and one [Google Chrome Selenium node]((https://hub.docker.com/r/selenium/node-chrome-debug/)).
Number of Selenium nodes can be scaled. For instance, by executing:

    docker-compose -f default-with-selenium.yml scale selenium-node-firefox=3 selenium-node-chrome=3

Then three Mozilla Firefox Selenium nodes and three Google Chrome Selenium nodes will be available.

**Warning**, the default `default-with-selenium.yml` Docker composition defines a given port for its single Google Chrome and Mozilla Firefox Selenium node. Beware to remove it in order to apply scale. 

_Note: The default configuration could not be efficient in production environment. You may want to remove the -debug suffix from Selenium node image names to get production ready Selenium nodes._

### Associated images

Image                                                                                                           | Description
----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------
[`cerberustesting/cerberus-db-mysql`](https://hub.docker.com/r/cerberustesting/cerberus-db-mysql/)              | Run a Cerberus dedicated MySQL database instance
[`cerberustesting/cerberus-as-glassfish`](https://hub.docker.com/r/cerberustesting/cerberus-as-glassfish/)      | Run a Cerberus instance into a Glassfish application server
[`selenium/hub`](https://hub.docker.com/r/selenium/hub/)                                                        | Run a Selenium Grid instance
[`selenium/node-firefox-debug`](https://hub.docker.com/r/selenium/node-firefox-debug/)                          | Run a Selenium node with Mozilla Firefox and a VNC server installed
[`selenium/node-chrome-debug`](https://hub.docker.com/r/selenium/node-chrome-debug/)                            | Run a Selenium node with Google Chrome and a VNC server installed

### Ports

Hereafter list of reachable ports from your Docker host:

Port             | Description
-----------------|---------------------------------------------------------------------------------
`18080`          | the Glassfish HTTP access port
`14848`          | the Glassfish administration console access port
`13306`          | the MySQL database access port
`14444`          | the Selenium Grid access port, in case of using the `default-with-selenium.yml` composition
`15900`          | the VNC port for the Mozilla Firefox Selenium node, in case of using the `default-with-selenium.yml` composition
`15901`          | the VNC port for the Google Chrome Selenium node, in case of using the `default-with-selenium.yml` composition

### Mapped volumes

Hereafter list of mapped volumes:

Service                 | Data volume (Source)                                                          | Host volume (Destination, default values)     | Description
------------------------|-------------------------------------------------------------------------------|-----------------------------------------------| -----------------------------------------------
`cerberus-db-mysql`     | `/var/lib/mysql`                                                              | `./localdata/mysql-db`                        | The MySQL local database directory
`cerberus-as-glassfish` | `/opt/CerberusMedias/`                                                  | `./localdata/cerberusmedia`                     | The Cerberus media directory (hosting execution screenshot for ex.)
`cerberus-as-glassfish` | `/tmp/lib/`                                  | `./localdata/lib`                            | Temporary folder in order to upload jdbc drivers for Glassfish
`cerberus-as-glassfish` | `/glassfish5/glassfish/domain/cerberus`                                       | `./localdata/server`                 | The Cerberus Glassfish domain. Allow to persist connection pool and custom glassfish seetings


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
