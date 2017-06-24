#!/bin/bash
#
# Travis job configuration when checking a commit from a Cerberus development branch

set -ev

cd source
mvn -Dcerberus.demo.qa.username=${CERBERUS_QA_DEMO_USERNAME} -Dcerberus.demo.qa.password=${CERBERUS_QA_DEMO_PASSWORD} clean deploy -Pqa-demo-deploy