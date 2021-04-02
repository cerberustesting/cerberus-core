# Cerberus Copyright (C) 2016 Cerberus Testing
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This file is part of Cerberus.
#
# Cerberus is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Cerberus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Cerberus. If not, see <http://www.gnu.org/licenses/>.
# Cerberus Glassfish MySQL composition.

####################################################################
# VARIABLES DEFINITION
####################################################################
MAVEN_CACHE?=localdata/maven-cache

####################################################################
# MAVEN BUILD
# use the docker maven service (docker-compose.yml) to build the app
# dependencies are cached locally
####################################################################
package:
	@mkdir -p $(MAVEN_CACHE)
	@export MAVEN_CACHE=$(MAVEN_CACHE) && \
		export USER_ID=$(shell id -u) && \
		export GROUP_ID=$(shell id -g) && \
		docker-compose run maven

####################################################################
# TOMCAT IMAGE BUILD
# build a new docker tomcat image with current source files
####################################################################
build:
	docker-compose build tomcat

####################################################################
# TOMCAT IMAGE RUN
# run the previsouly built docker image in the composition
####################################################################
run:
	docker-compose up -d 

####################################################################
# REFRESH
# rebuild from scratch the app and runs it.
####################################################################
refresh: package
	@$(MAKE) destroy
	@$(MAKE) build
	@$(MAKE) run

####################################################################
# DESTROY
# properly remove the Docker composition
####################################################################
destroy: 
	docker-compose down --remove-orphans

####################################################################
# LOG
# to be used in a command to check passed variables
####################################################################
.PHONY: log
log:
	@echo "#################################"
	@echo "MAKEFILE ARGS:"
	@echo "MAVEN_CACHE=$(MAVEN_CACHE)"
	@echo "#################################"

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
