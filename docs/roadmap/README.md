1. Queue Dependency improvements. 
1. Add har support
1. Improve email notification layout
1. Improve requirements
1. Improve system management
1. Move to Tomcat
1. Move to latest MariaDb
1. Manage better Authentification
1. pdf connector

DONE
1. Add robotexecutor

# 1. Queue Dependency improvements.

## Description

Dependency on :
* other testcase
 I can link a testcase B with a testcase A so that incase they are executed on the same tag, B will not trigger until A is finished.
 Also, when puting a dependency between 2 testcases, properties will be shared. That means that Execution of the second testcase will get the property list and value from the first execution.
* wait for event
 I can link a testcase with a specific 'event' so that the testcase will not be triggered until the event is produced. Event can be produced by calling a public service sending the event name + the corresponding tag.
 
## Benefit
* Allow single campaign for batch executions.
* Allow multi application tests scenarii.
 
# 2. Add har support

## Description

## Benefit
* 

# 3. Improve email notification layout

## Description

## Benefit
* 

# 4. Improve requirements

## Description

## Benefit
* 

# 5. Improve system management

## Description

## Benefit
* 

# 6. Move to Tomcat

## Description

## Benefit
* 

# 7. Move to latest MariaDb

## Description

## Benefit
* 

# 8. Manage better Authentification

## Description

Integrate [Keycloak](https://www.keycloak.org/)
Issue : #1180
 
## Benefit
* allow centralised authenticitation and better integration (google auth or Office 365 authen)

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
	
