#!/bin/bash

# This is a sample script to start a selenium server.

JAVA_HOME=/home/user/local/jdk1.6.0_38/

# Start Selenium.
$JAVA_HOME/bin/java -jar `dirname $0`/selenium-server-standalone-2.35.0.jar -port 5555


# Start Selenium with proxy config and trusting all SSLCertificates.
#$JAVA_HOME/bin/java -Dhttp.proxyHost=HOSTNAME -Dhttp.proxyPort=PORT -Dhttp.proxyUser=USER -Dhttp.proxyPassword=PASSWORD -jar selenium-server-standalone-2.35.0.jar -port 5555 -trustAllSSLCertificates


