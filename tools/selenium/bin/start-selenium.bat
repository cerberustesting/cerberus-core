@echo off

:This is a sample script to start a selenium server.

SET JAVA_HOME=C:\Users\gumartin\Desktop\Dev\Tools\jdk-8u101\
SET CD_PATH=C:\Users\gumartin\Desktop\Dev\Tools\SeleniumDriver\

:Start Selenium.
%JAVA_HOME%\bin\java -jar -Dwebdriver.chrome.driver=%CD_PATH%chromedriver.exe selenium-server-standalone-3.4.0.jar -port 5555 


pause