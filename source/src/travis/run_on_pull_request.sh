#!/bin/bash
#
# Travis job configuration when checking a commit from a pull request

set -ev

cd source
mvn clean package