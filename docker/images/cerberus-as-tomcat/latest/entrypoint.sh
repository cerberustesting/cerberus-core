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


# test add realm


export CATALINA_OPTS="$CATALINA_OPTS -DDATABASE_HOST=$DATABASE_HOST -DDATABASE_PORT=$DATABASE_PORT -DDATABASE_NAME=$DATABASE_NAME -DDATABASE_USER=$DATABASE_USER -DDATABASE_PASSWORD=$DATABASE_PASSWORD"

/usr/local/tomcat/bin/catalina.sh start
sh /keylock/${KEYCLOACK_NAME}/bin/standalone.sh -b 0.0.0.0 > /var/log/keylock.log &
bash /keylock/${KEYCLOACK_NAME}/bin/add-user-keycloak.sh -u cerberus -p cerberus
/keylock/keycloak-4.8.0.Final/bin/jboss-cli.sh --connect command=:reload

#bash /keylock/${KEYCLOACK_NAME}/bin/kcadm.sh config credentials --server http://localhost:8080/auth --realm Cerberus --user cerberus --password cerberus --client cerberus
#bash /keylock/${KEYCLOACK_NAME}/bin/kcadm.sh create realms -s --server http://localhost:8080/auth realm=Cerberus -s enabled=true -o --server http://localhost:8080/auth --realm Cerberus --user cerberus --password cerberus

#CID=$(bash /keylock/${KEYCLOACK_NAME}/bin/kcadm.sh create clients -r Cerberus -s clientId=cerberus -s 'redirectUris=["http://localhost:8180/Cerberus/*"]' -i)
#bash /keylock/${KEYCLOACK_NAME}/bin/kcadm.sh get clients/$CID/installation/providers/keycloak-oidc-keycloak-json

tail -F /var/log/keylock.log /usr/local/tomcat/logs/catalina.out
