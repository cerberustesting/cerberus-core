-- Purge 

-- 10 years = 3650 days
-- 3 years = 1100 days
-- 2 years = 730 days
-- 1 year = 365 days


-- ENVIRONMENT Purges.
-- -------------------

-- Batch/Event execution in every Environment History
DELETE FROM buildrevisionbatch
-- SELECT count(*) FROM buildrevisionbatch
where TO_DAYS(NOW()) - TO_DAYS(DateBatch) >= 1100 ;

-- Release Content History
DELETE FROM buildrevisionparameters
-- SELECT count(*) FROM buildrevisionparameters
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 3650 ;

-- Environment status log History (new build/Revision and disable events)
DELETE FROM countryenvparam_log
-- SELECT count(*) FROM countryenvparam_log
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 1100 ;

-- LOG Purges.
-- -----------

-- User action Log History
DELETE FROM logevent
-- SELECT count(*) FROM logevent
where TO_DAYS(NOW()) - TO_DAYS(Time) >= 365 ;

-- Further purge on DEPRECATED messages.
DELETE FROM logevent
-- select count(*) from logevent 
where TO_DAYS(NOW()) - TO_DAYS(Time) >= 30 
and Log like '[DEPRECATED]%' ;



-- EXECUTION Purges.
-- -----------------

SELECT @ID2 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 2 ;
SELECT @ID60 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 60 ;
SELECT @ID100 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 ;
SELECT @ID200 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 ;
SELECT @ID400 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 400 ;
SELECT @ID730 := IFNULL(MAX(ID), 0) from testcaseexecution where TO_DAYS(NOW()) - TO_DAYS(Start) >= 730 ;


-- Test Execution Control History
DELETE FROM testcaseexecutionfile
-- SELECT count(*) FROM testcaseexecutionfile
where ExeID < @ID60 ;

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where ID < @ID200 ;

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where ID < @ID200 ;

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where ID < @ID200 ;

-- Test Execution Property History
DELETE FROM testcaseexecutiondata
-- SELECT count(*) FROM testcaseexecutiondata
where ID < @ID200 ;

-- Test Execution www det
DELETE FROM testcaseexecutionwwwdet
-- SELECT count(*) FROM testcaseexecutionwwwdet
where ID < @ID60 ;

-- Test Execution History
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where ID < @ID730 ;

-- Test Execution System versionning History
DELETE FROM testcaseexecutionsysver
-- SELECT count(*) FROM testcaseexecutionsysver
where ID < @ID200 ;



-- EXECUTION Purges (BAM Test Cases).
-- -----------------------

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where ID < @ID100 and test = 'Business Activity Monitor';

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where ID < @ID100 and test = 'Business Activity Monitor';

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where ID < @ID100 and test = 'Business Activity Monitor';

-- Test Execution History
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  ID < @ID400 and test = 'Business Activity Monitor';

-- Test Execution History for BAM tests that reported no result.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  ID < @ID2 and test = 'Business Activity Monitor' and ControlStatus in ('NA','FA','KO','PE','CA');


-- EXECUTION Purges (Performance Monitor Test Cases).
-- -----------------------

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where ID < @ID100 and test = 'Performance Monitor';

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where ID < @ID100 and test = 'Performance Monitor';

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where ID < @ID100 and test = 'Performance Monitor';

-- Test Execution History for BAM tests.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  ID < @ID200 and test = 'Performance Monitor';



-- EXECUTION QUEUE Purges.
-- -----------------------

-- Test Execution Queue History
DELETE FROM testcaseexecutionqueue
-- SELECT count(*) FROM testcaseexecutionqueue
where TO_DAYS(NOW()) - TO_DAYS(DateCreated) >= 100 ;

-- Test Execution Queue History for queue entries that never generated any execution
DELETE FROM testcaseexecutionqueue
-- SELECT count(*) FROM testcaseexecutionqueue
where TO_DAYS(NOW()) - TO_DAYS(DateCreated) >= 10 and ExeId is null ;

-- Test Execution Queue History for queue entries that are DONE
DELETE FROM testcaseexecutionqueue
-- SELECT count(*) FROM testcaseexecutionqueue
where TO_DAYS(NOW()) - TO_DAYS(DateCreated) >= 10 and state = 'DONE' ;

