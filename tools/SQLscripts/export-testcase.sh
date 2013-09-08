#!/bin/bash

######################################################################
# Script that export testcase information from the Cerberus database #
######################################################################
 
# Test and TestCase as the 2 parameters.
export TEST=$1
export TESTCASE=$2

# Local parameters Do Not Modify there but copy paste into the "import-dump.variable.sh" Script
# Path to Script that updates database with test data.
export DTBTODUMP=cerberus
export MYSQLOPTS="--user=root --password=tototata"
export WHERE="Test like '$TEST' and Testcase like '$TESTCASE'"

# File that will store the result of the script execution.
export MYLOG=/home/vertigo/dev/database/log/export.testcase.log


#$TXTEDITOR $MYLOG &

echo "-- Starting export of Test and TestCase from $DTBTODUMP database ... " > $MYLOG
date >> $MYLOG


mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcase
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasecountry
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasecountryproperties
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasestep
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasestepaction
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasestepactioncontrol
mysqldump $MYSQLOPTS --no-create-info --skip-opt --where="$WHERE" $DTBTODUMP testcasestepbatch

