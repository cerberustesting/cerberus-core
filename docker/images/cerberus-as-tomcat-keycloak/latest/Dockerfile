FROM tomcat:8-jre8-alpine

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

ENV KEYCLOACK_REALM Cerberus
ENV KEYCLOACK_CLIENT cerberus
ENV KEYCLOACK_URL http://localhost:8180/auth
ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=keycloak -Dorg.cerberus.keycloak.realm=${KEYCLOACK_REALM} -Dorg.cerberus.keycloak.client=${KEYCLOACK_CLIENT} -Dorg.cerberus.keycloak.url=${KEYCLOACK_URL} -Xmx1024m"
ENV KEYCLOACK_VERSION 4.8.0.Final
ENV KEYCLOACK_TOMCAT8_ADAPTER_NAME keycloak-tomcat8-adapter-dist-${KEYCLOACK_VERSION}

ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ENV CERBERUS_NAME Cerberus
ENV CERBERUS_VERSION 3.11-SNAPSHOT
ENV CERBERUS_PACKAGE_NAME ${CERBERUS_NAME}-${CERBERUS_VERSION}

ENV MYSQL_JAVA_CONNECTOR_VERSION 5.1.20
ENV MYSQL_JAVA_CONNECTOR_NAME mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}

# Download and install MySQL JDBC Drivers
RUN wget -P /tmp/ https://downloads.mysql.com/archives/get/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /usr/local/tomcat/lib/ /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip

# Download & install KeyCloak adapter to manage authentification with Tomcat
RUN mkdir /keylock && echo https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/adapters/keycloak-oidc/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip  && wget -P /keylock https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/adapters/keycloak-oidc/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip && \
    unzip -q -d /usr/local/tomcat/lib/ /keylock/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip

# Download & install Cerberus Application
RUN wget -P /tmp/ https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-${CERBERUS_VERSION}/Cerberus-${CERBERUS_VERSION}.zip
#COPY Cerberus.zip /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN cp /tmp/${CERBERUS_PACKAGE_NAME}/${CERBERUS_PACKAGE_NAME}.war /usr/local/tomcat/webapps/Cerberus.war
#RUN cd /usr/local/tomcat/webapps/ && mkdir Cerberus && unzip -q -d Cerberus Cerberus.war && ls -al Cerberus


#RUN mv /usr/local/tomcat/lib/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar  /usr/local/tomcat/lib/

# Configure Tomcat for Cerberus need.
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY server.xml /usr/local/tomcat/conf/server.xml


COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
