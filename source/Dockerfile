# DOCKERFILE FOR LOCAL DEVELOPMENT
# this dockerfile creates an tomcat image packaged with all the necessary configuration for Cerberus

FROM tomcat:8-jre8-alpine

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

# NO KEYCLOAK AUTHENT
ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=none -Xmx1024m"

ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ARG CERBERUS_NAME=Cerberus
ARG CERBERUS_VERSION=4.14-SNAPSHOT
ARG CERBERUS_PACKAGE_NAME=${CERBERUS_NAME}-${CERBERUS_VERSION}

ARG MYSQL_JAVA_CONNECTOR_VERSION=5.1.47
ARG MYSQL_JAVA_CONNECTOR_NAME=mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}

# Download & install MySQL JDBC Drivers
RUN echo "Download & install MySQL JDBC Drivers" && \
    wget -P /tmp/ https://downloads.mysql.com/archives/get/p/3/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /tmp/ /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    mv /tmp/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar /usr/local/tomcat/lib/ && \
    echo "Clean temp directory" && \
    rm /tmp/* -rf

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/lib/

COPY target/${CERBERUS_PACKAGE_NAME}.zip /tmp

RUN echo "Unzip & Install Cerberus Application" && \
    # wget -P /tmp/ https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-${CERBERUS_VERSION}/Cerberus-${CERBERUS_VERSION}.zip && \
    unzip -q /tmp/${CERBERUS_PACKAGE_NAME}.zip -d /tmp && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cp /tmp/${CERBERUS_PACKAGE_NAME}/${CERBERUS_PACKAGE_NAME}.war /usr/local/tomcat/webapps/ROOT.war && \
    echo "Clean temp directory" && \
    rm /tmp/* -rf

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/webapps/

# Configure Tomcat for Cerberus need.
COPY configuration/tomcat/*.xml /usr/local/tomcat/conf/

# Only for debug purpose
#RUN echo ${CATALINA_OPTS}

COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
