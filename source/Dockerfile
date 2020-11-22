# Cerberus Copyright (C) 2016 Cerberus Testing
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This file is part of Cerberus.
#
# Cerberus is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Cerberus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Cerberus. If not, see <http://www.gnu.org/licenses/>.

#####################
# MAVEN BUILD STAGE #
#####################
FROM maven:3.6.3-jdk-8-slim AS build
# copy src folder (repo) to container
COPY src /home/app/src
# copy pom.xml file to container
COPY pom.xml /home/app
# copy the license header to container
COPY licenseheader.txt /home/app
# copy the INSTALL & LICENSE files (mandatory since present in local-assembly.xml)
COPY INSTALL /home
COPY LICENSE /home
# run the maven build command
RUN mvn -f /home/app/pom.xml clean install

#######################
# TOMCAT DEPLOY STAGE #
#######################
FROM bitnami/tomcat:9.0.39-debian-10-r28
# user privilegied user to bypass security
USER root
# copy mysql connector and keycloak adapter .jar files to tomcat
COPY /lib/mysql-connector-java-8.0.21.jar /opt/bitnami/tomcat/lib/
COPY /lib/bcpkix-jdk15on-1.65.jar /opt/bitnami/tomcat/lib/
COPY /lib/bcprov-jdk15on-1.65.jar /opt/bitnami/tomcat/lib/
COPY /lib/commons-codec-1.11.jar /opt/bitnami/tomcat/lib/
COPY /lib/commons-logging-1.2.jar /opt/bitnami/tomcat/lib/
COPY /lib/httpclient-4.5.12.jar /opt/bitnami/tomcat/lib/
COPY /lib/httpcore-4.4.13.jar /opt/bitnami/tomcat/lib/
COPY /lib/jackson-annotations-2.10.4.jar /opt/bitnami/tomcat/lib/
COPY /lib/jackson-core-2.10.4.jar /opt/bitnami/tomcat/lib/
COPY /lib/jackson-databind-2.10.4.jar /opt/bitnami/tomcat/lib/
COPY /lib/jakarta.activation-1.2.1.jar /opt/bitnami/tomcat/lib/
COPY /lib/jboss-logging-3.4.1.Final.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-adapter-core-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-adapter-spi-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-authz-client-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-common-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-core-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-tomcat-adapter-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-tomcat-adapter-spi-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/keycloak-tomcat-core-adapter-11.0.3.jar /opt/bitnami/tomcat/lib/
COPY /lib/mysql-connector-java-8.0.21.jar /opt/bitnami/tomcat/lib/
# set the environment variable for tomcat
# NOTE FOR: -Dorg.cerberus.keycloak.url: you have to use this URL in your local machine (in this configuration: kubernetes.docker.internal)
# It is also mandatory to check in your hosts file to have an entry such as : 127.0.0.1 kubernetes.docker.internal (should be done by Docker Desktop)
ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=keycloak -Dorg.cerberus.keycloak.realm=Cerberus -Dorg.cerberus.keycloak.client=cerberus -Dorg.cerberus.keycloak.url=http://kubernetes.docker.internal:38080/auth -Xmx1024m"
# copy .war file to deploy folder
COPY --from=build /home/app/target/Cerberus-4.11-SNAPSHOT.war /opt/bitnami/tomcat/webapps_default
# expose the 8080 port according to server.xml configuration
EXPOSE 8080
