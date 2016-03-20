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

-- Log History [DEPRECATED]
DELETE FROM log
-- SELECT count(*) FROM log
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 365 ;

-- User action Log History
DELETE FROM logevent
-- SELECT count(*) FROM logevent
where TO_DAYS(NOW()) - TO_DAYS(Time) >= 365 ;

-- EXECUTION Purges.
-- -----------------

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 ;

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 ;

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 ;

-- Test Execution Property History
DELETE FROM testcaseexecutiondata
-- SELECT count(*) FROM testcaseexecutiondata
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 ;

-- Test Execution www det
DELETE FROM testcaseexecutionwwwdet
-- SELECT count(*) FROM testcaseexecutionwwwdet
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 60 ;

-- Test Execution History
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 730 ;


-- EXECUTION Purges (BAM Test Cases).
-- -----------------------

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Business Activity Monitor';

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Business Activity Monitor';

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Business Activity Monitor';

-- Test Execution History
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  TO_DAYS(NOW()) - TO_DAYS(Start) >= 400 and test = 'Business Activity Monitor';

-- Test Execution History for BAM tests that reported no result.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  TO_DAYS(NOW()) - TO_DAYS(Start) >= 2 and test = 'Business Activity Monitor' and ControlStatus in ('NA','FA','KO','PE','CA');

-- EXECUTION Purges (Performance Monitor Test Cases).
-- -----------------------

-- Test Execution Control History
DELETE FROM testcasestepactioncontrolexecution
-- SELECT count(*) FROM testcasestepactioncontrolexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Performance Monitor';

-- Test Execution Action History
DELETE FROM testcasestepactionexecution
-- SELECT count(*) FROM testcasestepactionexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Performance Monitor';

-- Test Execution Step History
DELETE FROM testcasestepexecution
-- SELECT count(*) FROM testcasestepexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 100 and test = 'Performance Monitor';

-- Test Execution History for BAM tests.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  TO_DAYS(NOW()) - TO_DAYS(Start) >= 200 and test = 'Performance Monitor';

