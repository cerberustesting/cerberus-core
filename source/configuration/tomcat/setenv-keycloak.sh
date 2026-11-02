#!/bin/sh
# Tomcat setenv.sh - Keycloak authentication (image cerberus-as-tomcat-keycloak).
# Every value can be overridden with an environment variable (docker run -e ...).

CATALINA_OPTS="-Dspring.profiles.active=keycloak -Dorg.cerberus.environment=prd -Xmx${CERBERUS_XMX:-1024m} -Dorg.cerberus.authentification=keycloak"
CATALINA_OPTS="$CATALINA_OPTS -Dorg.cerberus.keycloak.realm=${KEYCLOAK_REALM:-R-preprod} -Dorg.cerberus.keycloak.client=${KEYCLOAK_CLIENT:-Cerberus}"
CATALINA_OPTS="$CATALINA_OPTS -Dorg.cerberus.keycloak.localrunnerclient=${KEYCLOAK_LOCALRUNNER_CLIENT:-cerberus-local-runner} -Dorg.cerberus.keycloak.mcpclient=${KEYCLOAK_MCP_CLIENT:-Cerberus-mcp}"
CATALINA_OPTS="$CATALINA_OPTS -Dorg.cerberus.keycloak.url=${KEYCLOAK_URL:-https://authent.cerberus-testing.org/auth/}"
CATALINA_OPTS="$CATALINA_OPTS -Dorg.cerberus.keycloak.mcp.audience=${KEYCLOAK_MCP_AUDIENCE:-cerberus-mcp-api}"
CATALINA_OPTS="$CATALINA_OPTS -DDATABASE_HOST=$DATABASE_HOST -DDATABASE_PORT=$DATABASE_PORT -DDATABASE_NAME=$DATABASE_NAME -DDATABASE_USER=$DATABASE_USER -DDATABASE_PASSWORD=$DATABASE_PASSWORD"
CATALINA_OPTS="$CATALINA_OPTS -Ddb.url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT/$DATABASE_NAME -Ddb.username=$DATABASE_USER -Ddb.password=$DATABASE_PASSWORD"
CATALINA_OPTS="$CATALINA_OPTS -Ddb.pool.maxTotal=${DB_POOL_MAXTOTAL:-100} -Ddb.pool.maxIdle=${DB_POOL_MAXIDLE:-30} -Ddb.pool.maxWaitMillis=${DB_POOL_MAXWAITMILLIS:-10000}"
export CATALINA_OPTS
