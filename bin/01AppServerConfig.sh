#!/bin/bash

#########################################################
#          Glassfish Configuration for Cerberus         #
#########################################################

. `dirname $0`/00Config.sh

###### Script start here ######

cd $MYPATH

### Starting server.
$GLASSFISHPATH/asadmin start-domain

### Configuring server.
$GLASSFISHPATH/asadmin create-jvm-options --target server "-Dorg.cerberus.environment=prd"

### Ressources and Connection Pool.
echo Deleting Resources and Connection Pool...
$GLASSFISHPATH/asadmin delete-resource-ref --target server jdbc/cerberusprd
$GLASSFISHPATH/asadmin delete-jdbc-connection-pool cerberus
$GLASSFISHPATH/asadmin delete-jdbc-resource jdbc/cerberusprd
echo Creating Resources and Connection Pool
# MySQL
$GLASSFISHPATH/asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource --restype javax.sql.ConnectionPoolDataSource --steadypoolsize 2 --property user=$DTBUSER:password=$DTBPASSWD:ServerName=$DTBSRVHOST:DatabaseName=$DTBNAME:portNumber=$DTBSRVPORT cerberus
# Mariadb
#$GLASSFISHPATH/asadmin create-jdbc-connection-pool --datasourceclassname org.mariadb.jdbc.MariaDbDataSource --restype javax.sql.ConnectionPoolDataSource --steadypoolsize 2 --property user=$DTBUSER:password=$DTBPASSWD:serverName=$DTBSRVHOST:databaseName=$DTBNAME:portNumber=$DTBSRVPORT cerberus
$GLASSFISHPATH/asadmin create-jdbc-resource --connectionpoolid cerberus jdbc/cerberusprd
$GLASSFISHPATH/asadmin create-resource-ref --target server jdbc/cerberusprd

### Security.
echo Creating Authentication configuration...
$GLASSFISHPATH/asadmin delete-auth-realm securityCerberus
$GLASSFISHPATH/asadmin create-auth-realm  --target server --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=jdbc/cerberusprd:user-table=user:user-name-column=Login:password-column=Password:group-table=usergroup:group-name-column=GroupName:digest-algorithm=SHA-1 securityCerberus
$GLASSFISHPATH/asadmin set server-config.security-service.default-realm=securityCerberus

### Restarting instance.
$GLASSFISHPATH/asadmin stop-domain
$GLASSFISHPATH/asadmin start-domain

