@echo off

rem #########################################################
rem #          Cerberus Application Deploy Script           #
rem #########################################################

CALL %CD%\00Config.bat

rem ###### Script start here ######

cd %MYPATH%
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-0.9.0
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-0.9.1
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.0.0
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.0.1
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.0.2
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.0
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.1
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.1
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.2
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.3
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.4
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.5
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.6
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.7
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.8
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.9
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.10
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.10.1
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.11
CALL %GLASSFISHPATH%asadmin.bat undeploy --target server --cascade=true Cerberus-1.1.12
CALL %GLASSFISHPATH%asadmin.bat deploy --target server --contextroot Cerberus --availabilityenabled=true %MYPATH%\..\source\target\Cerberus-1.1.12.war
