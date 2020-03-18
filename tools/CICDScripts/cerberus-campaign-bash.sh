#!/bin/bash

################################################
# @author acraske
# Cerberus CI/CD script
# launches the campaign with parameters
# exit with 0 (success) with OK result
# exit with 1 (failure) with KO result
# parameters:
# - mandatory: host to call, with HTTP/HTTPS
# - mandatory: campaign to trigger
# - optional : timeout in seconds, default 300
################################################

set -e

# set parameters related to environment
HOST_URL=https://atale.cerberus-testing.com
TRIGGER_URL=/AddToExecutionQueueV003?
RESULTCI_URL=/ResultCIV003?
TAGREPORT_URL=/ReportingExecutionByTag.jsp?Tag=

# set parameters related to execution
PARAM_CAMPAIGN='campaign='
PARAM_TAG='tag='
CAMPAIGN=atale-website-monitoring
TIMESTAMP=$( date "+%Y%m%d-%H%M%S" )
TAG=$CAMPAIGN.$TIMESTAMP

# set urls combining the various parameters
LAUNCH_CALL=$HOST_URL$TRIGGER_URL$PARAM_CAMPAIGN$CAMPAIGN'&'$PARAM_TAG$TAG
RESULTCI_CALL=$HOST_URL$RESULTCI_URL$PARAM_CAMPAIGN$CAMPAIGN'&'$PARAM_TAG$TAG
REPORT_CALL=$HOST_URL$TAGREPORT_URL$TAG

# set parameters for the internal plugin logic
LOOP_OUT='status_QU_nbOfExecution":0'
RESULT_KO='result":"KO'

#set parameters related to timing, in seconds
timeout_default=300
WAIT_PERIOD=$CERB_ENV_VAR_TIMEOUT
check_default=5
CHECK_INTERVAL=$CERB_ENV_VAR_CHECK

# check if mandatory parameters are set, if not exit
if [ -z "$HOST_URL" ]; then
	printf "\nMandatory parameter not set HOST_URL "
	printf  "\nExiting..."
	exit 1
fi

if [ -z "$CAMPAIGN" ]; then
	printf "\nMandatory parameter not set CAMPAIGN "
	printf  "\nExiting..."
	exit 1
fi

# overwrite optional parameters if not set
if [ -z "$WAIT_PERIOD" ]; then
	WAIT_PERIOD=$timeout_default
	printf "\nTIMEOUT not defined, setting default value of "
	printf  $WAIT_PERIOD
	printf "\n"
fi

if [ -z "$CHECK_INTERVAL" ]; then
	CHECK_INTERVAL=$check_default
	printf "\nCheck interval not defined, setting default value of "
	printf  $CHECK_INTERVAL
	printf "\n"
fi

# display parameters
echo "#############################################################"
echo "###############  CERBERUS CI/CD SCRIPT  #####################"
echo "# HOST     : " $HOST_URL
echo "# CAMPAIGN : " $CAMPAIGN
echo "# TAG      : " $TAG
echo "# TIMEOUT  : " $WAIT_PERIOD " seconds"
echo "# CHECK    : " $CHECK_INTERVAL " seconds"
echo "#############################################################"

# set algorithm variables default value
counter=1
elapsed=0

# start the campaign
printf "\n\nLaunching campaign: \n"
printf $LAUNCH_CALL

curl -s $LAUNCH_CALL > /dev/null


# loop while the campaign has not met the end criteria
while [ $counter == 1 ]; do

    printf "\n\nPerforming call to: \n"
    printf $RESULTCI_CALL

    # get out of the loop if campaign ended
	if $( curl -s $RESULTCI_CALL | grep -q $LOOP_OUT )
	then
		printf "\n\nCampaign finished"
		counter=0
		continue
	fi
	
	printf "\n\nCampaign not yet finished, trying again in 5 seconds..."
	sleep $CHECK_INTERVAL
	
	# logic for timeout
	elapsed=$((elapsed+CHECK_INTERVAL))
	
	# get out of the loop if reached timeout 
	if [ $elapsed -gt $WAIT_PERIOD ];then
       echo "\n\nThe script successfully ran for" $WAIT_PERIOD " seconds, exiting now..."
       exit 1
    fi

done


# output the campaign result based on the verification message
if $( curl -s $RESULTCI_CALL | grep -q $RESULT_KO )
then
	printf "\n\nCampaign failed, see results at: \n"
	printf $REPORT_CALL
	exit 1
else
	printf "\n\nCampaign successfully finished, see results at: \n"
	printf $REPORT_CALL
	exit 0
fi
