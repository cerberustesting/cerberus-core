---- Test Case Creation statistics.
-----------------------------------

-- Nb of test case created per month
SELECT date_format(TCDateCrea,"%Y%m"), count(*) FROM testcase
where TCDateCrea <> '0000-00-00 00:00:00'
GROUP BY date_format(TCDateCrea,"%Y%m")
ORDER BY TCDateCrea desc;


---- Regression Testing statistics.
-----------------------------------

-- Nb of Regression test case Execution per IP
SELECT distinct IP, count(*) from testcaseexecution
Where Test not in ('Business Activity Monitor', 'Performance Monitor')
group by IP;

-- Nb of Regression test case Executed per month (BAM Excluded)
SELECT date_format(Start,"%Y%m"), count(*) FROM testcaseexecution
Where Test not in ('Business Activity Monitor', 'Performance Monitor')
GROUP BY date_format(Start,"%Y%m")
ORDER BY Start desc;


---- Performance Testing statistics.
-----------------------------------

-- Number of Performance Test case Execution per IP. This is to verify that the tests are correctly balanced accross al VMs.
SELECT date_format(Start,"%Y%m"), IP, count(*) , sum(UNIX_TIMESTAMP(`End`)-UNIX_TIMESTAMP(`Start`))
FROM cerberus.testcaseexecution
WHERE test = 'Performance Monitor' and ControlStatus != 'PE'
GROUP BY date_format(Start,"%Y%m"), IP;

-- Number of Performance Test case Execution per IP for every status. This is to verify that the tests are correctly balanced.
-- -> issue on the engine side
SELECT tce.IP, count(*) PE  FROM testcaseexecution tce
WHERE tce.ID > ( SELECT max(ID) - 1000 idmax FROM testcaseexecution )
and ControlStatus = 'PE'
and environment='PROD' and status='WORKING' and Test='Performance Monitor'
and tce.IP in ('192.168.125.177','192.168.125.178','192.168.125.179','192.168.125.180')
GROUP BY tce.IP ;

-- -> issue on the test implementation side.
SELECT tce.IP, count(*) FA  FROM testcaseexecution tce
WHERE tce.ID > ( SELECT max(ID) - 1000 idmax FROM testcaseexecution )
and ControlStatus = 'FA'
and environment='PROD' and status='WORKING' and Test='Performance Monitor'
and tce.IP in ('192.168.125.177','192.168.125.178','192.168.125.179','192.168.125.180')
GROUP BY tce.IP ;

-- -> issue on the test implementation side or environment side (data to test not available).
SELECT tce.IP, count(*) NA  FROM testcaseexecution tce
WHERE tce.ID > ( SELECT max(ID) - 1000 idmax FROM testcaseexecution )
and ControlStatus = 'NA'
and environment='PROD' and status='WORKING' and Test='Performance Monitor'
and tce.IP in ('192.168.125.177','192.168.125.178','192.168.125.179','192.168.125.180')
GROUP BY tce.IP ;

-- -> issue system beeing tested. The incident is to open.
SELECT tce.IP, count(*) KO  FROM testcaseexecution tce
WHERE tce.ID > ( SELECT max(ID) - 1000 idmax FROM testcaseexecution )
and ControlStatus = 'KO' 
and environment='PROD' and status='WORKING' and Test='Performance Monitor'
and tce.IP in ('192.168.125.177','192.168.125.178','192.168.125.179','192.168.125.180')
GROUP BY tce.IP ;


---- DEPRECATED statistics.
-----------------------------------
SELECT `Action`, date_format(`Time`,"%Y%m%d"), count(*) FROM logevent
where `Time` > '2016-02-01 00:00:00' and `Log` like '[DEPRECATED]%'
GROUP BY `Action`, date_format(`Time`,"%Y%m%d")
ORDER BY `Action`, date_format(`Time`,"%Y%m%d");

