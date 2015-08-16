#!/bin/bash
#-------------------------------------------------------------------------------
#
# Objet : Generate the lines of code inside class DatabaseVersioningService.java 
#           in order to provide the delete and insert SQL instructions in order
#           to refresh the documentation table from the existing entries in 
#           the database.
#		Step 1 : Update and tune the documentation directly inside your database
#               Step 2 : Once clean, execute the script and copy the output at the end 
#                                           of the DatabaseVersioningService.java class
#               Step 3 : Replace all previous INSERT or UPDATE instructions on documentation 
#                           table to : SELECT 1 FROM dual;
#			    (that will speed up the execution of the SQL in case of 
#                               1st execution without changing the version number)
#         
#-------------------------------------------------------------------------------

# Database parameters.
DATABASE_NAME=cerberus
DATABASE_USER=root
# Outputfile where the result will be sent.
TMP_DOCUMENTATION_OUTPUT=/home/vertigo/Documentation.result
# Temporary area where log and temporary files will be saved
TMP_DIR=/home/vertigo/tmp
# Debug mode. If ="1" activate the debug mode. Any other value desactivate the debug mode.
DEBUG="1"

SCRIPTNAME=`basename $0`
LOG_FILE=${TMP_DIR}/${SCRIPTNAME}.log

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


# Static code before the table.
#-------------------------------------------------------------------------------

echo // New updated Documentation. > $TMP_DOCUMENTATION_OUTPUT
echo //-- ------------------------ 000-000 >> $TMP_DOCUMENTATION_OUTPUT
echo SQLS = new StringBuilder\(\)\; >> $TMP_DOCUMENTATION_OUTPUT
echo SQLS.append\(\"DELETE FROM \`documentation\`\;\"\)\; >> $TMP_DOCUMENTATION_OUTPUT
echo SQLInstruction.add\(SQLS.toString\(\)\)\; >> $TMP_DOCUMENTATION_OUTPUT
echo SQLS = new StringBuilder\(\)\; >> $TMP_DOCUMENTATION_OUTPUT
echo -n SQLS.append\(\" >> $TMP_DOCUMENTATION_OUTPUT


# Insert instructions from the table dump.
# 
#   ,(' is replaced by ");\nSQLS.append(",('
#   \' is replaced by \\'
#   on the last line we replace '); by ')");
# 
#-------------------------------------------------------------------------------
mysqldump -u $DATABASE_USER -q $DATABASE_NAME documentation | grep INSERT | sed s/,\(\'/\"\)\;\\nSQLS.append\(\"\,\(\'/g | sed s/\\\\\'/\\\\\\\\\'/g > $TMP_DOCUMENTATION_OUTPUT.tmp

cat $TMP_DOCUMENTATION_OUTPUT.tmp | sed -r s/\'\\\)\;$/\'\\\)\"\)\;/ >> $TMP_DOCUMENTATION_OUTPUT

# Static code after the table.
#-------------------------------------------------------------------------------
echo SQLInstruction.add\(SQLS.toString\(\)\)\; >> $TMP_DOCUMENTATION_OUTPUT


# Clean temporary files after execution.
#-------------------------------------------------------------------------------
rm -f $TMP_DOCUMENTATION_OUTPUT.tmp
