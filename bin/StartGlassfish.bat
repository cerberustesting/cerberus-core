@echo off

rem #########################################################
rem #          Cerberus Application Deploy Script           #
rem #########################################################

CALL %CD%\00Config.bat

rem ###### Script start here ######

cd %MYPATH%

rem ### Starting instance.
CALL %GLASSFISHPATH%asadmin.bat start-domain
