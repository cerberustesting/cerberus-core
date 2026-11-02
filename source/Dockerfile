# DOCKERFILE FOR LOCAL DEVELOPMENT AND CI
# this dockerfile creates an tomcat image packaged with all the necessary configuration for Cerberus.
# AUTH_MODE selects the setenv.sh : local (cerberus-as-tomcat) or keycloak (cerberus-as-tomcat-keycloak).

FROM tomcat:11-jre17-temurin

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ARG CERBERUS_NAME=cerberus-core
ARG CERBERUS_VERSION=6.0-SNAPSHOT
ARG CERBERUS_PACKAGE_NAME=${CERBERUS_NAME}-${CERBERUS_VERSION}

ARG AUTH_MODE=local

# Install required tools
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

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

# JVM and Cerberus options (local or keycloak authentication). Read by catalina.sh at startup.
COPY configuration/tomcat/setenv-${AUTH_MODE}.sh /usr/local/tomcat/bin/setenv.sh
RUN sed -i 's/\r$//' /usr/local/tomcat/bin/setenv.sh && chmod +x /usr/local/tomcat/bin/setenv.sh

COPY entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
