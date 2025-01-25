FROM tomcat:9-jre11-temurin

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

ENV KEYCLOACK_REALM Cerberus
ENV KEYCLOACK_CLIENT cerberus
ENV KEYCLOACK_URL http://192.168.1.1:8080/auth
ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=keycloak -Xmx1024m"
ENV KEYCLOACK_VERSION 18.0.1
ENV KEYCLOACK_TOMCAT8_ADAPTER_NAME keycloak-oidc-tomcat-adapter-${KEYCLOACK_VERSION}

ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ARG CERBERUS_NAME=cerberus-core
ARG CERBERUS_VERSION=SNAPSHOT
ARG CERBERUS_PACKAGE_NAME=${CERBERUS_NAME}-${CERBERUS_VERSION}

ARG MYSQL_JAVA_CONNECTOR_VERSION=9.0.0
ARG MYSQL_JAVA_CONNECTOR_NAME=mysql-connector-j-${MYSQL_JAVA_CONNECTOR_VERSION}


RUN apt-get update -qqy \
  && apt-get -qqy install \
  unzip dos2unix \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

# Download and install MySQL JDBC Drivers
RUN echo "Download & install MySQL JDBC Drivers" && \
    wget -P /tmp/ https://downloads.mysql.com/archives/get/p/3/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /tmp/ /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    mv /tmp/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}.jar  /usr/local/tomcat/lib/ && \
    echo "Clean temp directory" && \
    rm /tmp/* -rf
#COPY mysql-connector-java-5.1.20-bin.jar /usr/local/tomcat/lib/

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/lib/
# https://github.com/keycloak/keycloak/releases/download/${KEYCLOACK_VERSION}/keycloak-oidc-tomcat-adapter-${KEYCLOACK_VERSION}.zip
# https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/adapters/keycloak-oidc/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip
# Download & install KeyCloak adapter to manage authentification with Tomcat
RUN wget -P /tmp/ https://github.com/keycloak/keycloak/releases/download/${KEYCLOACK_VERSION}/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip && \
    unzip -q -d /usr/local/tomcat/lib/ /tmp/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip && \
    echo "Clean temp directory" && \
    rm /tmp/* -rf
#COPY ${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip /tmp/

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/lib/

# Download & install Cerberus Application
RUN echo "Download & install Cerberus Application" && \
    wget -P /tmp/ https://vm.cerberus-testing.org/delivery/${CERBERUS_PACKAGE_NAME}.war && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cp /tmp/${CERBERUS_PACKAGE_NAME}.war /usr/local/tomcat/webapps/ROOT.war && \
    echo "Clean temp directory" && \
    rm /tmp/* -rf

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/webapps/

# Configure Tomcat for Cerberus need.
COPY *.xml /usr/local/tomcat/conf/

# Only for debug purpose
#RUN echo ${CATALINA_OPTS}

COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
