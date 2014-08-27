@echo off

rem ####################################################
rem #              Cerberus Release process            #
rem ####################################################

SET MAVENBIN=mvn
SET GITBIN=git

CALL %MAVENBIN% release:prepare && %MAVENBIN% release:perform && %GITBIN% push
