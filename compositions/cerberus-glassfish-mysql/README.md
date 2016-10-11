# cerberus-glassfish-mysql Cerberus Docker compositions

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

Here you will find information about the `cerberus-glassfish-mysql` Docker composition

## cerberus-glassfish-mysql

The `cerberus-glassfish-mysql` Docker composition runs Cerberus under the [Glassfish](https://glassfish.java.net/) application server and a [MySQL](https://www.mysql.com/) database.

It runs also a [Selenium Grid](http://www.seleniumhq.org/docs/07_selenium_grid.jsp), initialized with two nodes with respectively [Mozilla Firefox](https://www.mozilla.org/en-US/firefox/products/) and [Google Chrome](https://www.google.com/chrome/browser/index.html) Web browsers to get ready to run Web Application tests.

### Get started

 1. Clone the [Cerberus Docker files repository](https://github.com/cerberustesting/cerberus-docker):

    `git clone https://github.com/cerberustesting/cerberus-docker.git`

 2. Go to the `compositions/cerberus-glassfish-mysql` directory

 3. Execute the following command:

	To run the docker-compose normally
	
    `docker-compose up`
	
	To run the docker-compose asynchronously
	
	`docker-compose up -d`
	
	To run the docker-compose and remove already existing images
	
	`docker-compose up -d --remove-orphans`
	
	[Optional] Adding more nodes to your Selenium Grid:

	`docker-compose scale selenium-node-firefox=3 selenium-node-chrome=3`

 4. Wait for the images to startup

 5. Open your favorite browser and go to `<docker_host>:18080/Cerberus`, where `<docker_host>` is your Docker host

 6. If this is your first time with Cerberus, the GUI will ask for database initialization. Please read the message on the page and click on the button `Initialize Database`, wait for all queries to execute, then scroll down and click on `Apply Next SQL`.

 7. Go to `<docker_host>:18080/Cerberus` again, it should ask for user credentials. Two default users are created:

   * User `admin`, password `admin`
   * User `cerberus`, password `cerberus`
   
 8. Set runtime configuration, as explained from the version-related [`cerberus-as-glassfish` README](https://github.com/cerberustesting/cerberus-docker/tree/master/images/cerberus-as-glassfish/README.md).

_Note: It is not yet possible to change a user's password, except by modifying its value directly in the database._

### How to run Web Application tests

#### Configure Cerberus to access to the Selenium Grid

This composition provide a ready-to-use Selenium Grid to run Web Application tests easier. To do so, simply feed the Cerberus Robot settings by the following information:

- Selenium ip/host: `selenium-hub`
- Selenium port: `4444`
- Browser: `firefox` or `chrome`

_Note: The Selenium Grid, Mozilla Firefox node and Google Chrome node are all under the **2.53.0** version._

#### Access to the Selenium Grid Console

The Selenium Grid Console can be reached from `<docker_host>:14444/grid/console`.

#### View running test on browser

The Selenium Grid is using the _debug_ version of the associated nodes.
This way, nodes integrate a [VNC server](https://fr.wikipedia.org/wiki/Virtual_Network_Computing) that can be reached through a VNC client.

Assume you already installed your own VNC client, then viewing running test on browser can be simply done by connecting your VNC client to:

- `vnc://<docker_host>:15900`, for the Mozilla Firefox Selenium node 
- `vnc://<docker_host>:25900`, for the Google Chrome Selenium node 
    
Where `<docker_host>` is your Docker host.

### Associated images

Image                                                                                                           | Description
----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------
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
`14444`          | the Selenium Grid access port
`15900`          | the VNC server access to the Mozilla Firefox Selenium node
`25900`          | the VNC server access to the Google Chrome Selenium node

### Mapped volumes

Hereafter list of mapped volumes:

Service                 | Data volume (Source)        | Host volume (Destination, default values)   | Description
------------------------|-----------------------------|---------------------------------------------| ---------------------
`cerberus-db-mysql`     | `/var/lib/mysql`            | `./localdata/mysql-db`                      | The MySQL local database directory
`cerberus-as-glassfish` | `/opt/cerberus-screenshots` | `./localdata/screenshots`                   | The Cerberus execution screenshots directory

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
