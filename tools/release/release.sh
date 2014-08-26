#!/bin/bash

####################################################
#              Cerberus Release Process            #
####################################################

MAVENBIN=mvn
GITBIN=git

$MAVENBIN release:prepare && $MAVENBIN release:perform && $GITBIN push
