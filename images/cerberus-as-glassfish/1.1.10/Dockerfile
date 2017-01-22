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
FROM glassfish:4.1
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
ENV CERBERUS_VERSION 1.1.10
ENV CERBERUS_PICTURES_PATH ${GLASSFISH_HOME}/glassfish/domains/domain1/docroot/CerberusPictures
ENV CERBERUS_PACKAGE_NAME ${CERBERUS_NAME}-${CERBERUS_VERSION}
ENV GLASSFISH_ADMIN_USER admin
ENV GLASSFISH_DOMAIN domain1
ENV MYSQL_JAVA_CONNECTOR_VERSION 5.1.20
ENV MYSQL_JAVA_CONNECTOR_NAME mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}
ENV MYSQL_JAVA_CONNECTOR_LIB_PATH /tmp/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar

# Get and extract the Cerberus package
# Use RUN instead of ADD to avoid re-download (see https://github.com/docker/docker/issues/15717)
RUN curl -L -o /tmp/${CERBERUS_PACKAGE_NAME}.zip https://sourceforge.net/projects/cerberus-source/files/${CERBERUS_PACKAGE_NAME}.zip/download && \
    unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip

# Get and extract the MySQL connector library
# Use RUN instead of ADD to avoid re-download (see https://github.com/docker/docker/issues/15717)
RUN curl -L -o /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip https://downloads.mysql.com/archives/get/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /tmp /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip

# Create the screenshot directory
RUN mkdir ${CERBERUS_PICTURES_PATH} && \
    chmod u+wx ${CERBERUS_PICTURES_PATH}

# Glassfish credentials files
COPY glassfish_admin_set_password.txt /tmp/glassfish_admin_set_password.txt
COPY glassfish_admin_password.txt /tmp/glassfish_admin_password.txt

# Start Glassfish initialization and execution
COPY entrypoint.sh /entrypoint.sh
RUN chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

# Start Glassfish instance with the verbose option to remain in the foreground so that docker can track it
CMD asadmin --user ${GLASSFISH_ADMIN_USER} --passwordfile /tmp/glassfish_admin_password.txt start-domain --verbose ${GLASSFISH_DOMAIN}