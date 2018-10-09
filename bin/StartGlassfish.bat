@echo off

rem #########################################################
rem #          Cerberus Application Stop                    #
rem #########################################################

CALL %CD%\00Config.bat

rem ###### Script start here ######

cd %MYPATH%

rem ### Starting instance.
CALL %GLASSFISHPATH%asadmin.bat start-domain
