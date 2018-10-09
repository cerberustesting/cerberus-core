#!/bin/bash

#########################################################
#          Cerberus Application Start                   #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH

### Starting instance.
$GLASSFISHPATH/asadmin start-domain

cd -
