#!/bin/bash
#-------------------------------------------------------------------------------
#
# Objet : Secure that all servlet and jsp are configured inside web.xml 
#  The script output all servlet and jsp that are not defined the 
#  normal amount of times inside the web.xml file.
#         
#-------------------------------------------------------------------------------

# Project Root.
PROJECTROOT=/home/vertigo/dev/ProjectsNetBeans/CerberusGit/
# Web.xml file to check.
WEBXML=/home/vertigo/dev/ProjectsNetBeans/CerberusGit/source/src/main/webapp/WEB-INF/web.xml
# Temporary area where log and temporary files will be saved
TMP_DIR=/home/vertigo/tmp
# Debug mode. If ="1" activate the debug mode. Any other value desactivate the debug mode.
DEBUG="1"

SCRIPTNAME=`basename $0`
LOG_FILE=${TMP_DIR}/${SCRIPTNAME}.log
TMP_SERVLET=${TMP_DIR}/${SCRIPTNAME}.tmp.listservlet
TMP_SERVLET2=${TMP_DIR}/${SCRIPTNAME}.tmp.listservlet2
TMP_SERVLET3=${TMP_DIR}/${SCRIPTNAME}.tmp.listservlet3
TMP_JSP=${TMP_DIR}/${SCRIPTNAME}.tmp.listjsp
TMP_JSP2=${TMP_DIR}/${SCRIPTNAME}.tmp.listjsp2

# Log functions
#-------------------------------------------------------------------------------

function log
{
message="$1"
HOUR=$(date '+%y%m%d-%H:%M:%S')
echo "$HOUR $$ $message"  
# >> $LOG_FILE
}

function log_debug
{
if [ $DEBUG -eq "1" ]
then
	message="$1"
	HOUR=$(date '+%y%m%d-%H:%M:%S')
	echo "$HOUR $$ $message"  >> $LOG_FILE
fi
}


# Servlet
#-------------------------------------------------------------------------------

log "----------   List of servlet that exist in the project source servlet"
log "----------              folder that does not appear (or appear twice)"
log "----------                 in <security-constraint> in web.xml file :"
log "---------------------------------------------------------------------"
find $PROJECTROOT -type f -wholename */main/java/org/cerberus/servlet/*java -exec basename {} \; > $TMP_SERVLET
#cat $TMP_SERVLET
servlet_to_fix=0
for j in `cat $TMP_SERVLET`
do
  log_debug "Processing $j"
  j=${j/.java/}
  log_debug "Counting $j"
  cntwebxml=`grep \/${j%}\<\/url $WEBXML | wc -l`
  log_debug "Found $cntwebxml"
  if [ ! $cntwebxml -eq 2 ]
    then
      echo "${j} $cntwebxml"
      servlet_to_fix=`expr $servlet_to_fix + 1`
  fi
done
if [ $servlet_to_fix -eq 0 ]
  then
    log "--> No Servlet to fix."
fi

#-------

log "----------  List of Servlet defined in the web.xml file that does not"
log "----------          appear (or appear twice) in <security-constraint>"
log "----------                                    still in web.xml file :"
log "---------------------------------------------------------------------"
grep servlet-name $WEBXML | sort | uniq > $TMP_SERVLET2
#cat $TMP_SERVLET2
servlet_to_fix=0
for j in `cat $TMP_SERVLET2`
do
  log_debug "Processing $j"
  j=${j/<servlet-name>/}
  j=${j/<\/servlet-name>/}
  log_debug "Counting $j"
  cntwebxml=`grep \/${j%}\<\/url $WEBXML | wc -l`
  log_debug "Found $cntwebxml"
  if [ ! $cntwebxml -eq 2 ]
    then
      echo "${j} $cntwebxml"
      servlet_to_fix=`expr $servlet_to_fix + 1`
  fi
done
if [ $servlet_to_fix -eq 0 ]
  then
    log "--> No Servlet to fix."
fi

#------- TO BE IMPLEMETED

#log "----------   List of servlet that exist in the web.xml file and does "
#log "----------                           not exist in the project source "
#log "----------                                            servlet folder :"
#log "---------------------------------------------------------------------"
#grep "\.jsp</url" $WEBXML > $TMP_SERVLET3
#cat $TMP_SERVLET3


# jsp
#-------------------------------------------------------------------------------
log "---------- List of jsp that exist in the project source webapp folder"
log "----------                     that does not appear (or appear twice)"
log "----------                 in <security-constraint> in web.xml file :"
log "---------------------------------------------------------------------"
find $PROJECTROOT -type f -wholename *src*main*webapp*.jsp -exec basename {} \; | sort > $TMP_JSP
#cat $TMP_JSP
jsp_to_fix=0
for j in `cat $TMP_JSP`
do
  log_debug "Processing ${j}"
  cntwebxml=`grep \/${j}\<\/url $WEBXML | wc -l`
  if [ ! $cntwebxml -eq 1 ]
    then
      echo "${j} $cntwebxml"
      jsp_to_fix=`expr $jsp_to_fix + 1`
  fi
done
if [ $jsp_to_fix -eq 0 ]
  then
    log "--> No jsp to fix."
fi

log "----------       List of jsp that exist in the web.xml file and does "
log "----------                           not exist in the project source "
log "----------                                            webapp folder :"
log "---------------------------------------------------------------------"
jsp_to_fix=0
grep "\.jsp</url" $WEBXML > $TMP_JSP2
#cat $TMP_JSP2
for j in `cat $TMP_JSP2`
do
  log_debug "Processing ${j}"
  j=${j/<url-pattern>/}
  j=${j/<\/url-pattern>/}
  log_debug "Counting ${j}"
  count_jsp=`find $PROJECTROOT -type f -wholename "*src*main*webapp*${j}*" -exec basename {} \; | wc -l`
  if [ ! $count_jsp -eq 1 ]
    then
      echo "${j} $count_jsp"
      find $PROJECTROOT -type f -wholename "*src*main*webapp*${j}*"
      jsp_to_fix=`expr $jsp_to_fix + 1`
  fi
done
if [ $jsp_to_fix -eq 0 ]
  then
    log "--> No jsp to fix."
fi


# Clean temporary files.
#-------------------------------------------------------------------------------
rm $TMP_SERVLET
rm $TMP_SERVLET2
#rm $TMP_SERVLET3
rm $TMP_JSP
rm $TMP_JSP2


