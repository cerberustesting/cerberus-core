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
FROM mysql:5.6
MAINTAINER cerberustesting

# MySQL image prerequisity for database creation
ENV MYSQL_ROOT_PASSWORD root

# Copy the database initialization file
COPY ./db-init.sh /docker-entrypoint-initdb.d

# Copy the bootstrap file that will be launched as entrypoint
# Do not name it as entrypoint.sh to not interfere with the MySQL one
COPY ./bootstrap.sh /
RUN chmod u+x /bootstrap.sh

# Execute the bootstrap file
ENTRYPOINT ["/bootstrap.sh"]