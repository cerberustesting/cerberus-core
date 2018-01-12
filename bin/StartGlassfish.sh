#!/bin/bash

#########################################################
#          Cerberus Application Deploy Script           #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH

### Starting instance.
$GLASSFISHPATH/asadmin start-domain
