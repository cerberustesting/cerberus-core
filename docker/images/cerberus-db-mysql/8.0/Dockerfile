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
FROM mysql:8.0
MAINTAINER cerberustesting

# Set root password and host
ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_ROOT_HOST=%

# Your script to create DBs/users/etc. — runs *once* on first start
COPY ./db-init.sh /docker-entrypoint-initdb.d/
RUN chmod +x /docker-entrypoint-initdb.d/db-init.sh

# Optional UID/GID fix — move to docker-entrypoint-initdb.d too if it must be run once
COPY ./bootstrap.sh /docker-entrypoint-initdb.d/01-bootstrap.sh
RUN chmod +x /docker-entrypoint-initdb.d/01-bootstrap.sh
