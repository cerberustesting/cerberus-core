1. Move to latest MariaDb
1. More reporting dashboard at campaign level.
1. Add Webperf KPI (SpeedIndex)
1. Improve requirements and test folder hierarchy
1. Queue Dependency improvements. 
1. Improve email notification layout
1. Integrate more CICD tools
1. pdf connector

DONE
1. Add robotexecutor
1. Move to Tomcat
1. Manage better Authentification
1. Improve system management
1. Add har and network traffic support

# 1. Move to latest MariaDb

## Description

Latest Version allow timestamps at millisecond level + JSON format. That is an oportunity to clean all timestamps columns in execution, step, action and control level.
To check as well the effort of moving to flyway removing constrain of SQL Script List size.

## Benefit
* Up to date Mariadb version.

# 7. More reporting and dashboard at campaign level

## Description

## Benefit
* 

# 7. Add more webperf KPI

## Description

## Benefit
* 


# 7. Improve requirements and test folder hierarchy

## Description

## Benefit
* 


# 2. Queue Dependency improvements.

## Description

Dependency on :
* other testcase [DONE]
 I can link a testcase B with a testcase A so that incase they are executed on the same tag, B will not trigger until A is finished.
 Also, when puting a dependency between 2 testcases, properties will be shared. That means that Execution of the second testcase will get the property list and value from the first execution.
* wait for event
 I can link a testcase with a specific 'event' so that the testcase will not be triggered until the event is produced. Event can be produced by calling a public service sending the event name + the corresponding tag.
 
## Benefit
* Allow single campaign for batch executions.
* Allow multi application tests scenarii. [DONE]
 
# 3. Improve email notification layout

## Description

## Benefit
* 

# 9. integrate more CICD Tools.

## Description
Such as Bitrise

## Benefit
* 

# 9. pdf connector

## Description

## Benefit
* 

# DONE SECTION

# 1. Add robotexecutor

## Description

Allow to have several host/port/user/pass for a given Robot.
 
## Benefit
* Speedup Appium & Sikuli testcase executions.
* potencially by pass Selenium hub and have robot-extention on a selenium farm.

# 2. Move to Tomcat

## Description

## Benefit
* 

# 3. Manage better Authentification

## Description

Integrate [Keycloak](https://www.keycloak.org/)
Issue : #1180
 
## Benefit
* allow centralised authenticitation and better integration (google auth or Office 365 authen)

# 4. Improve system management

## Description

Allow to isolate testcases, and executions per system and filter them per user depending on access right definition.

## Benefit
* clearer UI for users focussing on their scope of tests/executions

# 5. Add har support

## Description

Collect har file during and after execution and create specific action and property type in order to manipulate that data inside tests.
Also agregate stats in order to make controls easier and build dedicated graph at execution level + followup http perf graphs over time.

## Benefit
* Web perf management.
* Better analysis of execution over time.


