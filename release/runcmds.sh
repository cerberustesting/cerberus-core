#!/bin/bash
#
# Sequentially execute a list of commands by checking exit status.
#
# In case of failure, then display the failing command number
# and let user to start command list execution from a specific index.
#
# For more information, use the -h | --help option
#
# @author Aurelien Bourdon

#################################################
# User tunable variables                        #
#################################################

# List of command to execute
commands=()

# List of environment variables to use
environment=()

# Index to start from
functionalIndexToStart=1

# Source file
commandsFile=''

# Dry run
dryRun='false'

#################################################
# Internal variables                            #
#################################################

# Application name
APP=`basename $0`

# Log levels
INFO='INFO'
ERROR='ERROR'

#################################################
# Internal functions                            #
#################################################

# Print a log to console
#
# @param $1 log level
# @param $2 message content
# @return nothing
function log {
    level="$1"
    message="$2"
    echo "$APP [$level] $message"
}

# Display help message and exit
#
# @param nothing
# @return nothing
function help {
    echo "${APP}: Execute a list of commands"
    echo "Usage: ${APP} [OPTIONS] [COMMAND_1 COMMAND_2 ...]"
    echo 'OPTIONS:'
    echo '      -f | --from INDEX                       From which command number (INDEX) to start. Start from 1.'
    echo '      -h | --help                             Display this helper message.'
    echo '      -s | --source PATH                      The file PATH from which getting list of commands to execute.'
    echo '                                              File format: One line = One command. Empty line or line starting by the "#" character will be ignored.'
    echo '      -e | --environment ENV_KEY ENV_VALUE    Set the environment key variable ENV_KEY to the value ENV_VALUE within a command.'
    echo '                                              Once defined, environment variable can be used from command as the following: ${ENV_KEY}.'
    echo '                                              For instance, the command "echo ${foo}" will be interpreted as "echo bar" by using the option "-e foo bar", or "--environment foo bar".'
    echo '      -d | --dry-run                          List all commands which will be executed by interpreting environment variable if necessary.'
    echo '                                              Useful to see commands before really execute them.'
    echo 'COMMAND_1 COMMAND_2 ...:'
    echo '      List of commands to execute in order. If exists, then the -s | --source option will be disabled.'
    exit 0
}

# Parse user-given options
#
# @param $@ user options
# @return nothing
function parseOptions {
    while [[ $# -gt 0 ]]; do
        argument="$1"
        case $argument in
            -h|--help)
                help;
                ;;
            -f|--from)
                value="$2"
                if [[ $value -le 0 ]]; then
                    log ERROR 'Bad start index value. Value has to be greater or equal to 1.'
                    exit 1
                fi
                functionalIndexToStart=$value
                shift
                ;;
            -s|--source)
                value="$2"
                if [ ! -r $value ]; then
                    log ERROR "Unable to parse commands file '$value'. Exiting."
                    exit 1
                fi
                commandsFile=$value
                shift
                ;;
            -e|--environment)
                environment+=("$2")
                environment+=("$3")
                shift
                shift
                ;;
            -d|--dry-run)
                dryRun='true'
                ;;
            *)
                commands+=("$argument")
                ;;
        esac
        shift
    done
}

# Fill the $commands array from commands fetched from the associated file
#
# @param nothing
# @return nothing
function parseCommandsFile {
    while IFS='' read -r command || [[ -n "$command" ]]; do
        if [[ "$command" != "" &&  "$command" != \#* ]]; then
            commands+=("$command")
        fi
    done < $commandsFile
}

# Execute command list
#
# @param nothing
# @return noting
function runCommands {
    indexToStart=`expr $functionalIndexToStart - 1`
    commandsLength=${#commands[@]}

    if [ $commandsLength -eq 0 ]; then
        log INFO 'Nothing to execute. You can use the -h or --help option to display help message.'
        exit 0
    elif [ $indexToStart -ge $commandsLength ]; then
        log ERROR 'Index to start > commands length. Exiting.'
        exit 1
    fi

    startingMessage='Starting command execution flow'
    if [ $dryRun = 'true' ]; then
        startingMessage="$startingMessage in dry run mode..."
    fi
    log INFO "$startingMessage";

    indexToEnd=`expr $commandsLength - 1`
    for index in `seq $indexToStart $indexToEnd`; do
        functionalIndex=`expr $index + 1`
        command="${commands[$index]}"

        # Parse environment variables
        environmentLength=${#environment[@]}
        if [ ! $environmentLength -eq 0 ]; then
            for environmentIndex in `seq 0 2 $(expr ${environmentLength} - 1)`; do
                # Get environemnt key and value
                environmentKey=${environment[$environmentIndex]}
                environmentValue=${environment[$environmentIndex+1]}

                # Sanitize environment value to be used by sed
                environmentValue=`echo $environmentValue | sed -e 's/[\/&]/\\\&/g'`

                # Parse command line by substituting environment key and value if any
                command=`echo "$command" | sed "s/\\${${environmentKey}}/${environmentValue}/g"`
            done;
        fi

        # Execute command
        log INFO "----- #${functionalIndex}: $command"
        if [ $dryRun != 'true' ]; then
            eval "$command"
        fi

        # Check exit status
        if [ $? -ne 0 ]; then
            log ERROR "Command #${functionalIndex} failed"
            log ERROR "Please fix it and rerun by executing: ${APP} [...] --from ${functionalIndex}"
            exit 1
        fi
    done
    log INFO 'Command execution flow done.'
}

# Main entry point
#
# @param $@ the program arguments
# @return nothing
function main {
    parseOptions "$@"
    if [[ ${#commands[@]} -eq 0 && $commandsFile != '' ]]; then
        parseCommandsFile
    fi
    runCommands
}

#################################################
# Execution                                     #
#################################################

main "$@"
