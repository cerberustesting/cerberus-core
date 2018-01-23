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
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.1
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.1
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.2
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.3
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.4
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.5
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.6
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.7
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.8
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.9
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.10
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.10.1
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.11
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.12
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.13
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.13
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-1.1.14
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-2.0.0
$GLASSFISHPATH/asadmin undeploy --target server --cascade=true Cerberus-3.0.0
$GLASSFISHPATH/asadmin deploy --target server --contextroot Cerberus --availabilityenabled=true $MYPATH/../Cerberus-3.0.0.war
