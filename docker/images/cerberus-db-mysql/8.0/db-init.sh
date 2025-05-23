#!/bin/bash
#
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

# Initialize the Cerberus database

# Fail on any error
set -e

echo "* Cerberus database initialization..."
# Create user - MySQL 8 syntax
mysql --user="root" --password="root" --execute="CREATE USER IF NOT EXISTS 'cerberus'@'%' IDENTIFIED WITH caching_sha2_password BY 'toto';"
mysql --user="root" --password="root" --execute="CREATE USER IF NOT EXISTS 'cerberus'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'toto';"
# Create database
mysql --user="root" --password="root" --execute="CREATE DATABASE IF NOT EXISTS cerberus;"
# Grant privileges 
mysql --user="root" --password="root" --execute="GRANT ALL PRIVILEGES ON cerberus.* TO 'cerberus'@'%';"
mysql --user="root" --password="root" --execute="GRANT ALL PRIVILEGES ON cerberus.* TO 'cerberus'@'localhost';"
# Apply privileges
mysql --user="root" --password="root" --execute="FLUSH PRIVILEGES;"
echo "* Cerberus database initialization... Done."
