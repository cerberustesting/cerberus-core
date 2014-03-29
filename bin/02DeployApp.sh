#!/bin/bash

#########################################################
#          Cerberus Application Deploy Script           #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-0.9.0.war
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-0.9.1.war
$GLASSFISHPATH/asadmin deploy --target server --contextroot Cerberus --availabilityenabled=true $MYPATH/../Cerberus-0.9.1.war

