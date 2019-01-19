# In case you want to make a call to external SSL ressources, here is the procedure to follow :

## Introduction

Sometimes when you want to perform some SOAP or rest calls to external ressources that are managed by SSL, you may face the following error :

`
Failed to call the SOAP Operation 'myOperation' on Service Path https://api.toto.com/services/soap/ ! Caused by : com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl: Message send failed.
`

On the application server, an exception will raize with the following message :

`
javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to reques ted target
`

That probably means that the external URL you try to access require to import a certificate on the Cerberus application side.

The following chapters will guide you on how to perform that task and fix that issue.

## Step 1 Getting the crt file from external ressource

With your favorite browser get the the targeted URL (ex : https://api.toto.com/).
Then download the crt file from the side from certificate menu.

It will look like :

`
BEGIN CERTIFICATE                                               
MIIMNTCCCx2gAwIBAgIQAssfuP/rdS5GZdAl+BCnbzANBgkqhkiG9w0BAQsFADBw
MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3
d3cuZGlnaWNlcnQuY29tMS8wLQYDVQQDEyZEaWdpQ2VydCBTSEEyIEhpZ2ggQXNz
dXJhbmNlIFNlcnZlciBDQTAeFw0xOTAxMDgwMDAwMDBaFw0xOTAyMTQxMjAwMDBa
MF8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRIwEAYDVQQHEwlT
KlwDVgbflp+HNEjqpe5j5rfUlIk0Celii4xT5nqZ/57ZxH6Ym2zGtN6orMvu8Ukn
49b1j7Wus5XLF74zNZlW5fxjp6xQR0vb+zR61pCi7g2nRWSSuBX0jQ/C9CFmyX3B
gn2/3E+pNGgQsR8ttQNO/QcXTyIJZOiPwXaU1gqr/Jx4Ceg9f/hsoIsIcvAXWKi4
a81xPnn5mveD                                                    
END CERTIFICATE                                                 
`

Upload the toto.crt file to the Cerberus server.

## Step 2 import the certificate to your keystore file

Once the crt file on your server you can use the following command in order to import it to your keystore file. 

`
keytool -import -noprompt -trustcacerts -alias <AliasName> -file   <certificate> -keystore <KeystoreFile> -storepass <Password>
`

ex :

`
keytool -import -noprompt -trustcacerts -alias totocom -file /opt/apache-tomcat-8.5.35-QA/conf/toto.crt -keystore /opt/apache-tomcat-8.5.35-QA/conf/keystore.jks
`

You can check the alias has been added to your keystore file by using command :

`
keytool -list -keystore /opt/apache-tomcat-8.5.35-QA/conf/keystore.jks
`

Will produce the following output :

`
Your keystore contains 1 entry
toto.org, Jan 1, 2019, PrivateKeyEntry, 
Certificate fingerprint (SHA1): 54:69:62:35:EC:46:98:70:3C:XX:XX:6E:88:59:F1:9F:2E:4C:A6:EE
`

In case you never created any keystore file you can create it with the following command :

`
keytool -genkey -alias server -keyalg RSA -keystore keystore.jks -validity 10950
`

## Step 3 Configure Tomcat to use the keystore file.

in order to have tomcat using the keystore file, you need to change the setenv.sh file on bin folder adding or changing the line :

`
CATALINA_OPTS="-Djavax.net.ssl.trustStore=/opt/apache-tomcat-8.5.35-QA/conf/keystore.jks"
`

NOTE : Any change on the keystore file needs a tomcat restart in order to apply.


