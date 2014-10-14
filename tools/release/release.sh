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

APP=`basename $0`
MAVEN_SETTINGS_PATH=~/.m2/settings.xml

nextReleaseVersion=""
nextDevelopmentVersion=""

# Log a message according to the log level given in parameters
function log {
    level=$1
    shift
    echo "$APP [$level] $*"
}

# Log an error message
function error {
    log ERROR $*
}

# Light check on the Maven user settings file if SourceForge variables have been set
function checkSettingsFile {
    grep "sourceforge.username" $MAVEN_SETTINGS_PATH >> /dev/null && \
    grep "sourceforge.password" $MAVEN_SETTINGS_PATH >> /dev/null && \
    grep "sourceforge.url" $MAVEN_SETTINGS_PATH >> /dev/null
    
    if [[ $? != 0 ]]; then
        error "Your Maven user settings file is not set to be used with the SourceForge account. Please correctly set it and try again."
        return 1
    fi
    return 0
}

# Check if there are not commited changes in the local repository
function checkLocalChanges {
    localChanges=`git status --untracked-files=no --porcelain`
    if [ ! -z "$localChanges" ]; then
        error "Your local repository contains local changes. Please clean it and try again."
        return 1
    fi
    return 0
}

# Check prerequisities before to do the release
function checkPrerequisities {
    checkLocalChanges && \
    checkSettingsFile
    return $?
}

# Initializes variables from user
function initVersions {
    read -p "Next release version? " nextReleaseVersion
    read -p "Next development version? " nextDevelopmentVersion
}

# Update a bin/02DeployApp file by adding a new undeploy line and setting the deploy line with the next release version
function updateDeployAppFile {
    deployLineAndNumber=`grep -n " deploy " $1`
    deployLineNumber=`echo $deployLineAndNumber | cut -d: -f1`
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
    git commit -a -m "Update deploy app files for the next release."
}

# Prepare environment by:
# - Initializing variables
# - Updating bin/02DeployApp files
# - Commiting changes
function prepareEnvironment {
    initVersions
    updateDeployAppFiles
    commitChanges    
}

# Do the release by:
# - Executing the Maven release:prepare goal
# - Executing the Maven release:perform goal
# - Optionally executing a git push
function doRelease {
    mvn --batch-mode release:prepare \
        -Dtag=cerberus-testing-$nextReleaseVersion \
        -DreleaseVersion=$nextReleaseVersion \
        -DdevelopmentVersion=$nextDevelopmentVersion \
    && mvn release:perform && git push    
}

# Main entry point
function main {
    if ! checkPrerequisities; then
        error "Release aborted."
        exit 1
    fi
    
    prepareEnvironment
    doRelease
}

main
