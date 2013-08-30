#!/bin/bash

#########################################################
#          Cerberus Application Deploy Script           #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true GuiCerberus-1.1.0-SNAPSHOT.war
$GLASSFISHPATH/asadmin deploy --target server --availabilityenabled=true $MYPATH/../GuiCerberus-1.1.0-SNAPSHOT.war

