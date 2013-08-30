@echo off

rem #########################################################
rem #          Cerberus Application Deploy Script           #
rem #########################################################

CALL %CD%00Config.bat

rem ###### Script start here ######

cd %MYPATH%
CALL %GLASSFISHPATH%asadmin undeploy --target server --cascade=true GuiCerberus-1.1.0-SNAPSHOT.war
CALL %GLASSFISHPATH%asadmin deploy --target server --availabilityenabled=true %MYPATH%\..\GuiCerberus-1.1.0-SNAPSHOT.war
