## Cerberus Source
    
### How to install my development environment ?

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

#### Install Glassfish

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

