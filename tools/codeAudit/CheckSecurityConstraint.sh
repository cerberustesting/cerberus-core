#!/bin/bash
#-------------------------------------------------------------------------------
#
# Objet : Secure that all servlet and jsp are configured inside web.xml 
#  The script output all servlet and jsp that are not defined the 
#  normal amount of times inside the web.xml file.
#         
#-------------------------------------------------------------------------------

# Web.xml file to check.
WEBXML=/home/vertigo/dev/git/Cerberus/source/src/main/webapp/WEB-INF/web.xml
# Temporary area where log and temporary files will be saved
TMP_DIR=/home/vertigo/tmp

SCRIPTNAME=`basename $0`
LOG_FILE=${TMP_DIR}/${SCRIPTNAME}.log
TMP_XML=${TMP_DIR}/${SCRIPTNAME}.tmp.list

# Log functions
#-------------------------------------------------------------------------------

function log
{
message="$1"
HOUR=$(date '+%y%m%d-%H:%M:%S')
echo "$HOUR $$ $message"  
# >> $LOG_FILE
}


# Servlet
#-------------------------------------------------------------------------------

find . -type f -wholename *servlet*java -exec basename {} \; > $TMP_XML

#cat $TMP_XML

log "----------        List of Servlet :"
log "-----------------------------------"
for j in `cat $TMP_XML`
do
#  log "Processing ${j}"
  cntwebxml=`grep ${j%.java}\<\/url $WEBXML | wc -l`
  if [ ! $cntwebxml -eq 2 ]
    then
#      log ""
#    else
      echo "${j} $cntwebxml"
  fi
done


# jsp
#-------------------------------------------------------------------------------

find . -type f -wholename *.jsp -exec basename {} \; > $TMP_XML

#cat $TMP_XML

log "----------            List of jsp :"
log "-----------------------------------"
for j in `cat $TMP_XML`
do
#  log "Processing ${j}"
  cntwebxml=`grep \/${j%}\<\/url $WEBXML | wc -l`
  if [ ! $cntwebxml -eq 1 ]
    then
#      log ""
#    else
      echo "${j} $cntwebxml"
  fi
done
