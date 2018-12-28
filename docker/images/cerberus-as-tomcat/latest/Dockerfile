FROM tomcat:8-jre8-alpine

ENV LOG_ROOT_PATH /usr/local/tomcat/logs/

#ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=none -Xmx1024m"
ENV CATALINA_OPTS="-Dorg.cerberus.environment=prd -Dorg.cerberus.authentification=keycloak -Dorg.cerberus.keycloak.realm=Cerberus -Dorg.cerberus.keycloak.client=cerberus -Dorg.cerberus.keycloak.url=http://localhost:8180/auth -Xmx1024m"


ENV DATABASE_HOST cerberus-db-mysql
ENV DATABASE_PORT 3306
ENV DATABASE_NAME cerberus
ENV DATABASE_USER cerberus
ENV DATABASE_PASSWORD toto

ENV CERBERUS_NAME Cerberus
ENV CERBERUS_VERSION 3.11-SNAPSHOT
ENV CERBERUS_PICTURES_PATH ${GLASSFISH_HOME}/glassfish/domains/${GLASSFISH_DOMAIN}/docroot/CerberusPictures
ENV CERBERUS_PACKAGE_NAME ${CERBERUS_NAME}-${CERBERUS_VERSION}

ENV MYSQL_JAVA_CONNECTOR_VERSION 5.1.20
ENV MYSQL_JAVA_CONNECTOR_NAME mysql-connector-java-${MYSQL_JAVA_CONNECTOR_VERSION}

ENV KEYCLOACK_VERSION 4.8.0.Final
ENV KEYCLOACK_NAME keycloak-${KEYCLOACK_VERSION}
ENV KEYCLOACK_TOMCAT8_ADAPTER_NAME keycloak-tomcat8-adapter-dist-${KEYCLOACK_VERSION}


# COPY JDBC
RUN wget -P /tmp/ https://downloads.mysql.com/archives/get/file/${MYSQL_JAVA_CONNECTOR_NAME}.zip && \
    unzip -q -d /usr/local/tomcat/lib/ /tmp/${MYSQL_JAVA_CONNECTOR_NAME}.zip

# TODO copy maria jdbc

# install KeyCloak to manage authentification with tomcat
RUN mkdir /keylock && echo https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/adapters/keycloak-oidc/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip  && wget -P /keylock https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/adapters/keycloak-oidc/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip && \
    unzip -q -d /usr/local/tomcat/lib/ /keylock/${KEYCLOACK_TOMCAT8_ADAPTER_NAME}.zip
RUN wget -P /keylock https://downloads.jboss.org/keycloak/${KEYCLOACK_VERSION}/${KEYCLOACK_NAME}.zip && \
    unzip -q -d /keylock /keylock/${KEYCLOACK_NAME}.zip



# download cerberus zip
#RUN wget -P /tmp/ https://github.com/cerberustesting/cerberus-source/releases/download/cerberus-testing-${CERBERUS_VERSION}/Cerberus-${CERBERUS_VERSION}.zip
COPY Cerberus.zip /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN  unzip -q -d /tmp /tmp/${CERBERUS_PACKAGE_NAME}.zip
RUN cp /tmp/${CERBERUS_PACKAGE_NAME}/${CERBERUS_PACKAGE_NAME}.war /usr/local/tomcat/webapps/Cerberus.war
RUN cd /usr/local/tomcat/webapps/ && mkdir Cerberus && unzip -q -d Cerberus Cerberus.war && ls -al Cerberus


# DELETE current <login-config> on web.xml and the last line
RUN sed -i "$(grep  -n '<login-config>'  /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml | awk -F: '{print $1; exit}'),$(grep  -n '</login-config>'  /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml| awk -F: '{print $1; exit}')d" /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml &&\
    sed -i "$(grep  -n '</web-app>'  /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml | awk -F: '{print $1}'),\$d" /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml &&\
    # and annd the new <login-config> settings
    echo "<login-config>"  \
    "        <auth-method>BASIC</auth-method>"  \
    "        <realm-name>Enable this in order to activate KeyCloak</realm-name>"  \
    "    </login-config>" \
    "</web-app>" >> /usr/local/tomcat/webapps/Cerberus/WEB-INF/web.xml


RUN mv /usr/local/tomcat/lib/${MYSQL_JAVA_CONNECTOR_NAME}/${MYSQL_JAVA_CONNECTOR_NAME}-bin.jar  /usr/local/tomcat/lib/

# On Cerberus source, uncomment following line from META-INF/context.xml
COPY context-cerberus.xml  /usr/local/tomcat/webapp
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY server.xml /usr/local/tomcat/conf/server.xml


COPY entrypoint.sh /entrypoint.sh
RUN dos2unix /entrypoint.sh && chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
