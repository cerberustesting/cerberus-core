# Steps in order to move from Cerberus User database to Keycloack Database.
That document will explain how to move to keycloak by creating roles and optionally the users from Cerberus.


## Get the json import file from Cerberus by calling the servlet.

The servlet :
`
http://localhost:8080/Cerberus/GetKeycloakImport?realm=Cerberus
`

Will provide in a json keycloak format the list of users and roles from Cerberus database. realm parameter has to be feed with the Cerberus realm name defined inside Keycloak.

  NB1 : Only users that have a defined email will be extracted. Please clean the database by adding the emails before the extracts.
  
  NB2 : Make sure that no dupliceted email are inside the database. That can be checked with 
`
  SELECT email, count(*) FROM user where email is not null and email != '' group by email having count(*)>1;
`
  
  NB3 : All users will have new password as forced to 'Cerberus2018'
  
  NB4 : For the 1st login, all users will be require to 1/ Reset their password, 2/ validate the account details (name, surname and email) 3/ validate the email by receiving an email and confirm it by a link.

## Import the json file to keycloak

Login to keycloak on master realm as administrator.
Go to import menu and select the json file extracted previously.
Import tool will allow you to import roles and/or users. As a minimum you need to import the roles.
If you decide to keep users inside Keycloak (Keycloak can also integrate the 3rd party LDAP such as Google or Facebook), also import the users.

## First connection for migrated users

Once connecting the Cerberus, the Keycloak login page should be displayed.


Specify your login and 'Cerberus2018' as a password

[![01](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/01%20Login.png)]()


Keycloak will ask you to change your password.

[![02](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/02%20Update%20Password.png)]()

Then, it will ask you to update your personal informations

[![03](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/03%20Update%20Account.png)]()

Then, it will send you an email that will contain a link that you need to click.

[![04](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/04%20Email%20Verification.png)]()

[![05](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/05%20Email%20Received.png)]()

Then you will be granted to access Cerberus.

[![07](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/07%20Cerberus%20Homepage.png)]()

In case you get an error page from Cerberus, just click on 'Back to Homepage'

[![06](https://github.com/cerberustesting/cerberus-source/raw/master/docs/KeycloakMigration/06%20Login%20to%20Cerberus.png)]()

