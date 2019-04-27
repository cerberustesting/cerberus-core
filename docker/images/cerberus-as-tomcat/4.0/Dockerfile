FROM tomcat:8-jre8-alpine

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=none -Xmx1024m"

ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ENV CERBERUS_NAME Cerberus
ENV CERBERUS_VERSION 4.0
ENV CERBERUS_PACKAGE_NAME ${CERBERUS_NAME}-${CERBERUS_VERSION}

ENV MYSQL_JAVA_CONNECTOR_VERSION 5.1.20
ENV MYSQL_JAVA_CONNECTOR_NAME mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}

# Download and install MySQL JDBC Drivers
RUN wget -P /tmp/ https://downloads.mysql.com/archives/get/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip
RUN unzip -q -d /tmp/ /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip
RUN mv /tmp/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar  /usr/local/tomcat/lib/
#COPY mysql-connector-java-5.1.20-bin.jar /usr/local/tomcat/lib/

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/lib/

# Download & install Cerberus Application
RUN wget -P /tmp/ https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-${CERBERUS_VERSION}/Cerberus-${CERBERUS_VERSION}.zip
RUN unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN rm -rf /usr/local/tomcat/webapps/*
RUN cp /tmp/${CERBERUS_PACKAGE_NAME}/${CERBERUS_PACKAGE_NAME}.war /usr/local/tomcat/webapps/ROOT.war
#COPY Cerberus-3.12-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Only for debug purpose
#RUN ls -al /usr/local/tomcat/webapps/

# Configure Tomcat for Cerberus need.
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY server.xml /usr/local/tomcat/conf/server.xml

# Only for debug purpose
#RUN echo ${CATALINA_OPTS}

COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
