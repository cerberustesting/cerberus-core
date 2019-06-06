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

# GlassFish Docker image oracle/glassfish has been removed from the Oracle docker-images repository as part of the donation process to the Eclipse Foundation.
# Eclipse will provide a glassfish docker image only when releasing GlassFish 5.0.1
# https://github.com/eclipse-ee4j/glassfish/issues/22598
# Until there glassfish is provided by nicodeur/glassfish:5.0-web
#FROM oracle/glassfish:5.0-web
FROM nicodeur/glassfish:5.0-web
MAINTAINER cerberustesting

# Database related environment variables
# Could be overridden by user
ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto
ENV GLASSFISH_HTTP_THREADPOOL_MAX_SIZE 500

# Internal environment variables
# Should not be overridden by user
ENV CERBERUS_NAME Cerberus
ENV CERBERUS_VERSION 4.1-SNAPSHOT
ENV GLASSFISH_DOMAIN cerberus
ENV GLASSFISH_ADMIN_PASSWORD admin
ENV CERBERUS_PICTURES_PATH ${GLASSFISH_HOME}/glassfish/domains/${GLASSFISH_DOMAIN}/docroot/CerberusPictures
ENV CERBERUS_PACKAGE_NAME ${CERBERUS_NAME}-${CERBERUS_VERSION}
ENV GLASSFISH_ADMIN_USER admin
ENV MYSQL_JAVA_CONNECTOR_VERSION 5.1.20
ENV MYSQL_JAVA_CONNECTOR_NAME mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}
ENV MYSQL_JAVA_CONNECTOR_LIB_PATH /tmp/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar

# Oracle do not install JDK on Glassfish... So we get it from Cerberus.
# That will have to be cleaned with Glassfish 5.0.1 release
RUN yum remove -y java-1.8.0-openjdk && yum install -y unzip wget && yum clean all && rm -rf /var/cache/yum
RUN wget -P /tmp/ https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-2.0.0/jdk-8u152-linux-x64.rpm
RUN rpm -ivh /tmp/jdk-8u152-linux-x64.rpm
ENV JAVA_HOME /usr/java/jdk1.8.0_152/

# For windows user
RUN yum install -y dos2unix

# Get and extract the Cerberus package
# Use RUN instead of ADD to avoid re-download (see https://github.com/docker/docker/issues/15717)

#RUN curl -L -o /tmp/${CERBERUS_PACKAGE_NAME}.zip https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-${CERBERUS_VERSION}/Cerberus-${CERBERUS_VERSION}.zip && \
#    unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip

COPY Cerberus.zip /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip

# Get and extract the MySQL connector library
# Use RUN instead of ADD to avoid re-download (see https://github.com/docker/docker/issues/15717)
RUN curl -L -o /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip https://downloads.mysql.com/archives/get/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /tmp /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip

# Glassfish credentials files
COPY glassfish_admin_set_password.txt /tmp/glassfish_admin_set_password.txt
COPY glassfish_admin_password.txt /tmp/glassfish_admin_password.txt

# Start Glassfish initialization and execution
COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
