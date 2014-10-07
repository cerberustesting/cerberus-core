#!/bin/bash
#
# Cerberus Release Process.
#
# This script prepare release environment and do the release.
#
# Do not forget to fill your ~.m2/settings.xml file with the following information:
#   <profiles>
#       <profile>
#          <id>default</id>
#          <properties>
#            <sourceforge.username>USERNAME,cerberus-source</sourceforge.username>
#            <sourceforge.password>PASSWORD</sourceforge.password>
#            <sourceforge.url>frs.sourceforge.net:/home/frs/project/cerberus-source/</sourceforge.url>
#          </properties>
#       </profile>
#   </profiles>
#    
#   <activeProfiles>
#       <activeProfile>default</activeProfile>
#   </activeProfiles>

MAVENBIN=mvn
GITBIN=git

nextReleaseVersion=""
nextDevelopmentVersion=""

# Initializes variables from user
function initVariables {
    read -p "Next release version? " nextReleaseVersion
    read -p "Next development version? " nextDevelopmentVersion
}

# Update a bin/02DeployApp file by adding a new undeploy line and setting the deploy line with the next release version
function updateDeployAppFile {
    deployLineAndNumber=`grep -n " deploy " $1`
    deployLineNumber=`echo $deployLineAndNumber | cut -d: -f1`
    deployLine=`echo $deployLineAndNumber | cut -d: -f2`
    undeployLine=`grep " undeploy " $1 | tail -1 | sed "s/Cerberus-[^ ]*/Cerberus-${nextReleaseVersion}/"`

    sed -i "${deployLineNumber}i $undeployLine" $1
    sed -i "/ deploy /s/Cerberus-.*.war/Cerberus-${nextReleaseVersion}.war/" $1
}

# Updates the bin/02DeployApp.* files by adding a new undeploy line and setting the deploy line with the next release version
function updateDeployAppFiles {
    updateDeployAppFile "bin/02DeployApp.sh"
    updateDeployAppFile "bin/02DeployApp.bat"
}

# Commits changes
function commitChanges {
    $GITBIN commit -a -m "Update deploy app files for the next release"
}

# Prepare environment by:
# - Initializing variables
# - Updating bin/02DeployApp files
# - Commiting changes
function prepareEnvironment {
    initVariables
    updateDeployAppFiles
    commitChanges    
}

# Do the release by:
# - Executing the Maven release:prepare goal
# - Executing the Maven release:perform goal
# - Optionally executing a git push
function doRelease {
    $MAVENBIN --batch-mode release:prepare \
        -Dtag=cerberus-testing-$nextReleaseVersion \
        -DreleaseVersion=$nextReleaseVersion \
        -DdevelopmentVersion=$nextDevelopmentVersion \
    && $MAVENBIN release:perform && $GITBIN push    
}

# Main entry point
function main {
    prepareEnvironment
    doRelease
}

main
