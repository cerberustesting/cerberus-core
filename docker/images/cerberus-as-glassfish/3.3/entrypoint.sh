#!/bin/bash
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

# Cerberus Glassfish configuration

# Fail on any error
set -e

# Use asadmin with credentials
ASADMIN="asadmin --user ${GLASSFISH_ADMIN_USER} --passwordfile /tmp/glassfish_admin_password.txt"

# Initialization marker file
INIT_MARKER_FILE=${GLASSFISH_HOME}/glassfish/domains/${GLASSFISH_DOMAIN}/.cerberus
INIT_MARKER_DEPLOY=${GLASSFISH_HOME}/glassfish/domains/${GLASSFISH_DOMAIN}/.cerberus.deploy

# Deploy the installed Cerberus instance
function deploy() {
    echo "* Starting Cerberus Glassfish deployment..."
    ${ASADMIN} start-domain ${GLASSFISH_DOMAIN}
    ${ASADMIN} deploy --target server --contextroot ${CERBERUS_NAME} --availabilityenabled=true /tmp/${CERBERUS_PACKAGE_NAME}/${CERBERUS_PACKAGE_NAME}.war
    ${ASADMIN} stop-domain ${GLASSFISH_DOMAIN}
    touch ${INIT_MARKER_DEPLOY}
echo "* Starting Cerberus Glassfish deployment... Done."
}

# Setup Glassfish to the Cerberus needs
function setup() {
    echo "* Starting Cerberus Glassfish setup..."

    # Create the screenshot directory
    mkdir -p ${CERBERUS_PICTURES_PATH}
    chmod u+wx ${CERBERUS_PICTURES_PATH}

    # Copy the MySQL Java connector to Glassfish global libraries folder
    cp ${MYSQL_JAVA_CONNECTOR_LIB_PATH} ${GLASSFISH_HOME}/glassfish/lib

    # Set the admin password
    local ASADMIN_DEFAULT=asadmin
    ${ASADMIN_DEFAULT} start-domain ${GLASSFISH_DOMAIN}

    cat /tmp/glassfish_admin_set_password.txt > /tmp/glassfishpwd
    ${ASADMIN_DEFAULT} --user ${GLASSFISH_ADMIN_USER} --passwordfile /tmp/glassfishpwd change-admin-password --domain_name ${GLASSFISH_DOMAIN}
    rm /tmp/glassfishpwd
    echo "AS_ADMIN_PASSWORD=${GLASSFISH_ADMIN_PASSWORD}" > /tmp/glassfishpwd

    # Configure Glassfish to the Cerberus needs
    ${ASADMIN} restart-domain ${GLASSFISH_DOMAIN}
    ${ASADMIN} enable-secure-admin
    ${ASADMIN} create-jvm-options --target server "-Dorg.cerberus.environment=prd"
    ${ASADMIN} create-jdbc-connection-pool --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource --restype javax.sql.ConnectionPoolDataSource --steadypoolsize 2 --property user=${DATABASE_USER}:password=${DATABASE_PASSWORD}:ServerName=${DATABASE_HOST}:DatabaseName=${DATABASE_NAME}:portNumber=${DATABASE_PORT} cerberus
    ${ASADMIN} create-jdbc-resource --connectionpoolid cerberus jdbc/cerberusprd
    ${ASADMIN} create-auth-realm  --target server --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=jdbc/cerberusprd:user-table=user:user-name-column=Login:password-column=Password:group-table=usergroup:group-name-column=GroupName:digest-algorithm=SHA-1 securityCerberus
    ${ASADMIN} set server-config.security-service.default-realm=securityCerberus
    ${ASADMIN} set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=${GLASSFISH_HTTP_THREADPOOL_MAX_SIZE}
    ${ASADMIN} stop-domain ${GLASSFISH_DOMAIN}

    # Persist setup execution to avoid it to be re-run
    touch ${INIT_MARKER_FILE}
    echo "* Starting Cerberus Glassfish setup... Done."
}

# Main entry point
function main() {
    if [ ! -f ${GLASSFISH_HOME}/glassfish/domains/${GLASSFISH_DOMAIN}/config/domain.xml ]; then
        echo "AS_ADMIN_PASSWORD=" > /tmp/glassfishpwd
        ${ASADMIN} create-domain --adminport 4848 ${GLASSFISH_DOMAIN}
    fi
    # Check if setup has already been done, and if not, then execute it
    if [ ! -f ${INIT_MARKER_FILE} ]; then
        setup
    else
        echo "* Glassfish domain already deployed. Skip installation."
    fi

    echo "AS_ADMIN_PASSWORD=${GLASSFISH_ADMIN_PASSWORD}" > /tmp/glassfishpwd

    if [ ! -f ${INIT_MARKER_DEPLOY} ]; then
        deploy
    else
        echo "* Cerberus is already deployed to the Glassfish instance. Skip installation."
    fi
}

# Execute the main entry point
main

# Finally continue execution
${ASADMIN} start-domain --verbose ${GLASSFISH_DOMAIN}