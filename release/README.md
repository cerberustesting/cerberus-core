# Cerberus release processes

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This project is the entry point to release Cerberus project modules.

## Get started

This project defines a set of release processes, each of them represented by a `.cmds` file.

A `.cmds` file gathers all necessary commands to be executed in order to apply a release process. These commands can be executed thanks to the [runcmds](https://github.com/abourdon/runcmds) command execution tool.
 
Finally, each `.cmds` file contains a documentation header part to describe how to use it.

### Step 1 : Database Version update

Go to your cerberus/source/src/main/webapp folder and modify the file DatabaseMaintenance.jsp. The variable `Integer SQLLimit` must be set to the current version of database + 1 :

 * `cd <path_to_cerberusclone>/source/src/main/webapp`
 * `vim DatabaseMaintenance.jsp (modify the file)`
 * `git commit -m "change database version to <database_version>`
 * `git push origin master`
 
 Where:
  - <database_version> is the current database version

### Step 2 : Maven config update for SourceForge upload

You need to be able to push cerberus on sourceforge
To do that, fill your Maven's settings file with the following information:

    <profiles>
        <profile>
            <id>default</id>
            <properties>
                <cerberus.sourceforge.username>USERNAME,cerberus-source</cerberus.sourceforge.username>
                <cerberus.sourceforge.password>PASSWORD</cerberus.sourceforge.password>
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>default</activeProfile>
    </activeProfiles>

Where:
 - USERNAME is your Cerberus' Sourceforge username
 - PASSWORD is your Cerberus' Sourceforge password
    
Note that user Maven's settings file is usually located at ~/.m2/settings.xml

### Step 3 : Docker Login

You need to be logged in to docker registry to perform the docker's release
`
    docker login -p <password> -u <username>
`

Where:
 - <password> is your docker hub password
 - <username> is your docker hub username
 
**/!\ you need to have the right on cerberus repository for sourceforge and docker hub**

### Step 4 : Get runcmds.sh Utility

Clone runcmds.sh somewhere on your computer : `git clone https://github.com/abourdon/runcmds`

### Step 5 : Run the script that perform the release

Go to your cerberus/release folder
`
    cd <path_to_cerberusclone>/release/
`

And run the release cmd :
`
 <path_to_runcmds>/runcmds.sh
       -e RELEASE_VERSION <release version> \
       -e NEXT_DEVELOPMENT_VERSION <next development version> \
       -e RUNCMDS_PATH <runcmds command path> \
       -s ./common.cmds
`

`common.cmds` will clone a cerberus on release/cerberus-testing, change some version on bin/*.sh script and make a `mvn release`.
After that, common.cmds will wait new version of cerberus is available on sourceforge, and create new docker version.

### Step 6 : Create a new changelog entry file and make is displayed in homepage for next developpement version

 * `cd <path_to_cerberusclone>/source/source/src/main/resources/documentation/include/en/`
 * `cp changelog_template_en.adoc changelog_xnew.ynew_en.adoc`
 * `cd <path_to_cerberusclone>/source/source/src/main/webapp/js/pages/`
 * `vim Homepage.js`

change

`
        $("#documentationFrame").attr("src", "./documentation/changelog_xold.yold_en.html");
`

to
`
        $("#documentationFrame").attr("src", "./documentation/changelog_xnew.ynew_en.html");
`

and

`
        $("#changelogLabel").html("Changelog xold.yold");
`

to
`
        $("#changelogLabel").html("Changelog xnew.ynew");
`
 
 
 * `cd <path_to_cerberusclone>/source/source/src/main/webapp/`
 * `vim Homepage.jsp`
 
change

`
                            <div class="panel-body collapse in" id="Changelogxoldyold">
`

to
`
                            <div class="panel-body collapse in" id="Changelogxnewynew">
`
 * `git commit -m "Added New Changelog`
 * `git push origin master`


## List of available release processes

Hereafter the list of available release processes:

File                            | Description                        
--------------------------------|---------------------------------------------------------------------
[common.cmds](./common.cmds)    | Release all necessary Cerberus project modules for a common release which are [cerberus-source](https://github.com/cerberustesting/cerberus-source) and [cerberus-as-glassfish](https://github.com/cerberustesting/cerberus-source/tree/master/docker/images/cerberus-as-glassfish).
  
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
