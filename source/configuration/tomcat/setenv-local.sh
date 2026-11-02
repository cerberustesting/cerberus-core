#!/bin/sh
# Tomcat setenv.sh - local authentication (image cerberus-as-tomcat).
# Every value can be overridden with an environment variable (docker run -e ...).

CATALINA_OPTS="-Dspring.profiles.active=local -Dorg.cerberus.environment=prd -Xmx${CERBERUS_XMX:-1024m} -Dorg.cerberus.authentification=local"
CATALINA_OPTS="$CATALINA_OPTS -DDATABASE_HOST=$DATABASE_HOST -DDATABASE_PORT=$DATABASE_PORT -DDATABASE_NAME=$DATABASE_NAME -DDATABASE_USER=$DATABASE_USER -DDATABASE_PASSWORD=$DATABASE_PASSWORD"
CATALINA_OPTS="$CATALINA_OPTS -Ddb.url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT/$DATABASE_NAME -Ddb.username=$DATABASE_USER -Ddb.password=$DATABASE_PASSWORD"
CATALINA_OPTS="$CATALINA_OPTS -Ddb.pool.maxTotal=${DB_POOL_MAXTOTAL:-100} -Ddb.pool.maxIdle=${DB_POOL_MAXIDLE:-30} -Ddb.pool.maxWaitMillis=${DB_POOL_MAXWAITMILLIS:-10000}"
CATALINA_OPTS="$CATALINA_OPTS -Dorg.cerberus.keycloak.mcp.audience=${KEYCLOAK_MCP_AUDIENCE:-cerberus-mcp-api}"
export CATALINA_OPTS
