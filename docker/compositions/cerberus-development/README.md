# Cerberus development with Docker

This docker composition helps volunteer to deploy a development environment using Docker in order to simplify the installation process (maven, tomcat, keycloak).

Run `docker-compose up -d` to launch the env.

Run `docker-compose up down` to launch the env.

Run `docker-compose up build` to rebuild the cerberus app.

Note: the first time you launch the composition, you might get 404 error on `localhost:8080`. This a known issue not solved yet, just restart the env. 
