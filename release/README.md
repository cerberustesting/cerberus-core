# Cerberus release processes

[Cerberus](http://www.cerberus-testing.org/) is an user-friendly automated testing framework.

This project is the entry point to release Cerberus project modules.

## Get started

This project defines a set of release processes, each of them represented by a `.cmds` file.

A `.cmds` file gathers all necessary commands to be executed in order to apply a release process. These commands can be executed thanks to the [runcmds](https://github.com/abourdon/runcmds) command execution tool.
 
Finally, each `.cmds` file contains a documentation header part to describe how to use it.

### step by step

Clone runcmds.sh somewhere on your computer :
`
git clone https://github.com/abourdon/runcmds
`

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