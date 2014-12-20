#!/bin/bash

#########################################################
#          Cerberus Application Deploy Script           #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-0.9.0
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-0.9.1
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.0.0
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.0.1
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.0.2
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.0
$GLASSFISHPATH/asadmin deploy --target server --contextroot Cerberus --availabilityenabled=true $MYPATH/../Cerberus-1.1.0.war

