# Cerberus release processes

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This folder is the entry point to release Cerberus project.

## Get started

This folder defines a set of release processes, each of them represented by a `.cmds` file.

A `release.cmds` file gathers all necessary commands to be executed in order to apply a release process. That file can be executed thanks to the [runcmds](https://github.com/abourdon/runcmds) command execution tool.
 
Finally, the `release.cmds` file contains a documentation header part to describe how to use it.

Prerequisits are 

* Have a installed version of maven :

      sudo apt install maven


* JDK installation.

      export JAVA_HOME=/opt/jdk1.8.0_251/
      export PATH=$JAVA_HOME/bin:$PATH

### Cerberus github release


### Step 1 : Run the script that perform the release

Go to your cerberus/release folder


    cd <path_to_cerberusclone>/release/

And run the release cmd :

    ./runcmds.sh \
       -e RELEASE_VERSION <release version> \
       -e NEXT_DEVELOPMENT_VERSION <next development version> \
       -e DATABASE_VERSION <current database version> \
       -s ./release.cmds

NB : If under Windows, you can submit the command from docker bash.

### Step 2 : Copy paste changelog on github

* Click on 'Draft new release'.
* Choose 'cerberus-testing-XNEW.YNEW' tag
* Put in title : vXNEW.YNEW
* copy/paste adoc file under source/src/main/resources/documentation/D2/include/en/changelog_xdev_ydev.adoc to content.
* Upload Cerberus-xnew.ynew.zip from source/target/
* Press 'Publish Release'

### Cerberus docker release

### Step 1 : Docker Login

You need to be logged in to docker registry to perform the docker's release

    docker login -p <password> -u <username>

Where:
 - <password> is your docker hub password
 - <username> is your docker hub username
 
 **/!\ you need to have the right on cerberus repository for docker hub**

### Step 2 : Run the script that perform the release

Go to your cerberus/docker folder

    cd <path_to_cerberusclone>/release/cerberus-source/docker/images/cerberus-as-tomcat

And run the release cmd :

    ../../../../runcmds.sh \
       -e RELEASE_VERSION <release version> \
       -s ./release.cmds

Go to the other docker image in order to perform the same release command.

    cd <path_to_cerberusclone>/release/cerberus-source/docker/images/cerberus-as-tomcat-keycloak

NB : If under Windows, you can submit the command from docker bash.


## List of available release processes

Hereafter the list of available release processes:

File                            | Description                        
--------------------------------|---------------------------------------------------------------------
[common.cmds](./common.cmds)    | Release all necessary Cerberus project modules for a common release which are [cerberus-source](https://github.com/cerberustesting/cerberus-source) and [cerberus-as-tomcat](https://github.com/cerberustesting/cerberus-source/tree/master/docker/images/cerberus-as-tomcat).
  
## License

Cerberus Copyright (C) 2013 - 2019 cerberustesting

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
