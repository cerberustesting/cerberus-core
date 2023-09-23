# Cerberus Source

## Run a local environment

### Using Docker
Cerberus is a Java project build with Maven and recommended to use with Tomcat.
It rely on a MySQL (or MariaDB) databases.

All you need to build and launch Cerberus locally is Docker, in this folder you will find
- `Dockerfile` building the tomcat image
- `docker-compose.yml` running the maven build and running the tomcat & DB instance

#### Step 1: Maven

Run `docker-compose up maven`

This will build Cerberus and provides `.war` and `.zip` file in the `target` folder.

If launched for the first time, dependencies will be downloaded and cached (in `localdata/maven-cache`) for the next build.

#### Step 2: DB

Run `docker-compose run -d database`

This will run the MySQL database and use the cached data in `localdata` folder.

If run for the first time, the DB will empty.

#### Step 3: Tomcat

Run `docker-compose build`

This will launch the `Dockerfile` instructions and build on your machine a tomcat image packaged with the built artifact from step #1 (mandatory)

On completion, run `docker-compose run -d tomcat`

You should be able to access Cerberus on [localhost:8080](http://localhost:8080).

#### Makefile

All above commands are encapuslated in an Makefile.

Make sure to add the `make` command on your CLI first.

Simply run `make refresh` to test your new code.

`make destroy` will shutdown every docker instances once you're done.
    
### Manual installation

#### Install mysql
```
docker run --name mysql-cerberus -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:5.6
```
#### Launch the mysql console (via Docker)
```
docker exec -ti mysql-cerberus bash
mysql --password
```
Enter the root password ('root')
#### Create the Cerberus DB
Create a database `cerberus` and a user `cerberus`

```
CREATE USER 'cerberus'@'%' IDENTIFIED BY 'cerberus';
UPDATE mysql.user SET Password=PASSWORD('cerberus') WHERE User='cerberus' AND Host='localhost';
FLUSH PRIVILEGES;
GRANT USAGE ON * . * TO 'cerberus'@'%' IDENTIFIED BY 'cerberus';
GRANT USAGE ON * . * TO 'cerberus'@'localhost' IDENTIFIED BY 'cerberus';
CREATE DATABASE IF NOT EXISTS `cerberus` ;
GRANT ALL PRIVILEGES ON `cerberus` . * TO 'cerberus'@'%';
```

#### Install Glassfish (deprecated)

https://javaee.github.io/glassfish/download


#### Use Cerberus environment variable on your server 

Use bin/ script to configure your glassfish server. Just replace your mysql information on 00Config and run 00Config & 01AppServerConfig

##### Eclipse problem
If you use eclipse glassfish plugin, change the admin glassfish password because it doesn't work with an empty password.

```
asadmin change-admin-password #(Default password is empty.)
```  

On eclipse, Check `Use jar archives for deployment` on `server properties page > Glassfish`

##### stop server
```
asadmin stop-domain
```



problem : impossible to login : password incorrect,

Verify realm-name is fill into web.xml :

```
 <login-config>
        <auth-method>FORM</auth-method>
        <!--        Default Realm defined on Cluster->Security-->
        <realm-name>securityCerberus</realm-name>
        <form-login-config>
            <form-login-page>/Login.jsp</form-login-page>
            <form-error-page>/Login.jsp?error=1</form-error-page>
        </form-login-config>
    </login-config>
```

