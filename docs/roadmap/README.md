1. Add Webperf KPI (SpeedIndex)
1. Clean the structure of API.
1. Angular Version of the frontend.
1. Improve requirements and test folder hierarchy
1. Integrate more CICD tools

DONE
1. Kafka Avro connector
1. pdf connector
1. Improve Notification system
1. Queue Dependency improvements. 
1. Move to latest MariaDb
1. Add robotexecutor
1. Move to Tomcat
1. Manage better Authentification
1. Improve system management
1. Add har and network traffic support
1. More reporting and dashboard at campaign level

# 1. Move to latest MariaDb

## Description

Latest Version allow timestamps at millisecond level + JSON format. That is an oportunity to clean all timestamps columns in execution, step, action and control level.
To check as well the effort of moving to flyway removing constrain of SQL Script List size.

## Benefit
* Up to date Mariadb version.

# 2. Add more webperf KPI

## Description
Including SpeedIndex

## Benefit
* 
# 3. Clean API

## Description
Clean some structure (using more Array than Objects) and making some obect name less verbose and moving some String to boolean.

## Benefit
* Easier to integrate and maintain.

# 4. Angular Version

## Description
Faster UI and better ergonomy

## Benefit
* Better enjoy to use the tools and improve productivity.

# 5. Improve requirements and test folder hierarchy

## Description

## Benefit
* 

# 6. Queue Dependency improvements.

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
 
# 7. Improve Notification system

## Description
* Centralize notification system.
* Make notification system asynchroneus.
* Allow user GUI in order to activate or not notifications and webhook.
* Allow various connector such as email, slack, app server log, Kafka producer or even webhook.
* Event referencial such as execution start / end campaign start / end, create/update/delete any object. 

## Benefit
* Faster notification
* more flexible integration to external systems.

# 8. integrate more CICD Tools.

## Description
Such as Bitrise

## Benefit
* 

# 9. pdf connector

## Description

## Benefit
* 

# 10. KAFKA Avro connector

## Description
Support produce and consume event with Avro Schema registry.
https://aseigneurin.github.io/2018/08/02/kafka-tutorial-4-avro-and-schema-registry.html

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

# 6. More reporting and dashboard at campaign level

## Description

## Benefit
* 


