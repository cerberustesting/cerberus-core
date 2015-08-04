-- Purge 

-- 10 years = 3650 days
-- 3 years = 1100 days
-- 2 years = 730 days
-- 1 year = 365 days

-- Batch execution History
DELETE FROM buildrevisionbatch
-- SELECT count(*) FROM buildrevisionbatch
where TO_DAYS(NOW()) - TO_DAYS(DateBatch) >= 1100 ;

-- Release History
DELETE FROM buildrevisionparameters
-- SELECT count(*) FROM buildrevisionparameters
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 3650 ;

-- Environment status log History
DELETE FROM countryenvparam_log
-- SELECT count(*) FROM countryenvparam_log
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 1100 ;

-- Log History
DELETE FROM log
-- SELECT count(*) FROM log
where TO_DAYS(NOW()) - TO_DAYS(datecre) >= 365 ;

-- User action Log History
DELETE FROM logevent
-- SELECT count(*) FROM logevent
where TO_DAYS(NOW()) - TO_DAYS(Time) >= 365 ;

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
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 190 ;


-- Test Execution www det
DELETE FROM testcaseexecutionwwwdet
-- SELECT count(*) FROM testcaseexecutionwwwdet
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 60 ;

-- Test Execution History
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where TO_DAYS(NOW()) - TO_DAYS(Start) >= 700 ;



-- More drastic Execution purge for BAM tests.

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

-- Test Execution History for BAM tests.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  TO_DAYS(NOW()) - TO_DAYS(Start) >= 400 and test = 'Business Activity Monitor';

-- Test Execution History for BAM tests that reported no result.
DELETE FROM testcaseexecution
-- SELECT count(*) FROM testcaseexecution
where  TO_DAYS(NOW()) - TO_DAYS(Start) >= 2 and test = 'Business Activity Monitor' and ControlStatus in ('NA','FA','KO','PE','CA');

-- More drastic Execution purge for BAM tests.

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

