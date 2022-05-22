#!/bin/bash

################################################
# @author acraske, vertigo17
# Cerberus CI/CD script
# launches the campaign with parameters
# exit with 0 (success) with OK result
# exit with 1 (failure) with KO result
# parameters:
# - mandatory: CRBURL host to call, with HTTP/HTTPS
# - mandatory: CAMPAIGN campaign to trigger
# - optional : MAXDURATION timeout in seconds, default 300
# - optional : CHECKPERIOD time in seconds between 2 checks
# - optional : APIKEY API Key in order to call Cerberus public API
# - optional : PARAMEXTRA Extra parameters that can be tuned to trigger the campaign execution
################################################

set -e

# set system parameters
PID=$$
TIMESTAMP=$( date "+%Y%m%d-%H%M%S" )
MYSCRIPTFILE=`basename $0`

# set Cerberus parameters
TRIGGER_URL=/AddToExecutionQueueV003?
RESULTCI_URL=/ResultCIV003?
TAGREPORT_URL=/ReportingExecutionByTag.jsp?Tag=
PARAM_CAMPAIGN='campaign='
PARAM_TAG='tag='


#set parameters related to timing, in seconds
timeout_default=300
check_default=5

if [ "$3" == "" ]; then
    echo Missing parameters !!
    echo Usage : $0 CRBURL= CAMPAIGN= PARAMEXTRA= APIKEY= MAXDURATION= CHECKPERIOD=
    echo Example : $0 CRBURL=https://atale.cerberus-testing.com CAMPAIGN=monitoringCampaign PARAMEXTRA="\&country=ssid\&country=atale\&priority=5000" APIKEY=16c2e3badb17d1eb453beb7c6a65aafb MAXDURATION=300 CHECKPERIOD=5
    exit 1;
fi

# Parsing Arguments.
####################

for argument; do #syntactic sugar for: for argument in "$@"; do
    key=${argument%%=*}
    value=${argument#*=}
    case "$key" in
            CRBURL)           CRBURL=$value;;
            CAMPAIGN)         CAMPAIGN=$value;;
            PARAMEXTRA)       PARAMEXTRA=$value;;
            APIKEY)           APIKEY=$value;;
            MAXDURATION)      MAXDURATION=$value;;
            CHECKPERIOD)      CHECKPERIOD=$value;;
    esac
done

# set dynamic parameters
TAG=$CAMPAIGN.$TIMESTAMP
LAUNCH_CALL=$CRBURL$TRIGGER_URL$PARAM_CAMPAIGN$CAMPAIGN'&'$PARAM_TAG$TAG'&'outputformat=json$PARAMEXTRA
RESULTCI_CALL=$CRBURL$RESULTCI_URL$PARAM_CAMPAIGN$CAMPAIGN'&'$PARAM_TAG$TAG
REPORT_CALL=$CRBURL$TAGREPORT_URL$TAG


# check if mandatory parameters are set, if not exit
if [ -z "$CRBURL" ]; then
	printf "\nMandatory parameter not set CRBURL "
	printf  "\nExiting..."
	exit 1
fi

if [ -z "$CAMPAIGN" ]; then
	printf "\nMandatory parameter not set CAMPAIGN "
	printf  "\nExiting..."
	exit 1
fi

# overwrite optional parameters if not set
if [ -z "$MAXDURATION" ]; then
	MAXDURATION=$timeout_default
#	printf "\nTIMEOUT not defined, setting default value of "
#	printf  $MAXDURATION
#	printf "\n"
fi

if [ -z "$CHECKPERIOD" ]; then
	CHECKPERIOD=$check_default
#	printf "\nCheck interval not defined, setting default value of "
#	printf  $CHECKPERIOD
#	printf "\n"
fi

OUTPUTFILE=/tmp/$MYSCRIPTFILE-$PID

# display parameters
echo "#############################################################"
echo "###############  CERBERUS CI/CD SCRIPT  #####################"
echo "# HOST     : " $CRBURL
echo "# CAMPAIGN : " $CAMPAIGN
echo "# PARAMEXTRA : " $PARAMEXTRA
echo "#############################################################"
echo "# LOG            : " $OUTPUTFILE
echo "# TAG            : " $TAG
echo "# Max check      : " $MAXDURATION " seconds"
echo "# Period Check   : " $CHECKPERIOD " seconds"
echo "#############################################################"

# set algorithm variables default value
counter=1
elapsed=0

# start the campaign
printf "\nLaunching campaign: \n"
printf $LAUNCH_CALL

OUTPUTFILE1=$OUTPUTFILE.startcampaign

curl -s --header "apikey: $APIKEY" -o $OUTPUTFILE1 $LAUNCH_CALL

RESULT=`cat $OUTPUTFILE1 | jq -r '.returnCode'`

if [ "$RESULT" == "KO" ]; then
    printf "\n\nFailed to trigger Campaign !!\n"
    echo `cat $OUTPUTFILE1 | jq -r '.message'`
    printf "\n"
    exit 1
else
    printf "\n\n"
    echo `cat $OUTPUTFILE1 | jq -r '.message'`
fi


printf "\nPerforming check calls to: \n"
printf $RESULTCI_CALL

# loop while the campaign has not met the end criteria
while [ $counter == 1 ]; do


    # get out of the loop if campaign ended

    OUTPUTFILE2=$OUTPUTFILE.check
    curl -s --header "apikey: $APIKEY" -o $OUTPUTFILE2 $RESULTCI_CALL
    RESULT=`cat $OUTPUTFILE2 | jq -r '.result'`

    RESULTQU=`cat $OUTPUTFILE2 | jq -r '.status_QU_nbOfExecution'`
    RESULTPE=`cat $OUTPUTFILE2 | jq -r '.status_PE_nbOfExecution'`
    RESULTOK=`cat $OUTPUTFILE2 | jq -r '.status_OK_nbOfExecution'`
    RESULTKO=`cat $OUTPUTFILE2 | jq -r '.status_KO_nbOfExecution'`
    RESULTFA=`cat $OUTPUTFILE2 | jq -r '.status_FA_nbOfExecution'`

    printf "\nQU : "
    printf '%-3s' "$RESULTQU"
    printf "| PE : "
    printf '%-3s' "$RESULTPE"
    printf "| OK : "
    printf '%-3s' "$RESULTOK"
    printf "| KO : "
    printf '%-3s' "$RESULTKO"
    printf "| FA : "
    printf '%-3s' "$RESULTFA"

#    printf "\nQU : $RESULTQU | PE : $RESULTPE | OK : $RESULTOK | KO : $RESULTKO | FA : $RESULTFA"

    if [ "$RESULT" != "PE" ]; then
		printf "\n\nCampaign finished with final result : $RESULT"
		counter=0
		continue
	fi
	
	# logic for timeout
	elapsed=$((elapsed+CHECKPERIOD))

	printf " (Campaign not yet finished, maybe trying again in 5 seconds... Elapsed : $elapsed/$MAXDURATION)"
	
	
	# get out of the loop if reached timeout 
	if [ $elapsed -gt $MAXDURATION ];then
       printf "\n\nThe script ran for more than $MAXDURATION seconds, exiting now...\n"
       exit 1
    fi

	sleep $CHECKPERIOD
done


# output the campaign result based on the verification message
if [ "$RESULT" != "OK" ]; then
	printf "\n\nCampaign failed, see results at: \n"
	printf $REPORT_CALL
	printf "\n"
	exit 1
else
	printf "\n\nCampaign successfully finished, see results at: \n"
	printf $REPORT_CALL
	printf "\n"
	exit 0
fi
