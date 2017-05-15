INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Type`, `Description`, `Envelope`) 
 SELECT '', `Country`, `Environment`, `key`, 'STATIC', IFNULL(td.`Description`,''), '' from testdata td
 ON DUPLICATE KEY UPDATE Description = IFNULL(td.`Description`,'');

INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Value`, `Description`, `ParsingAnswer`) 
 SELECT '', `Country`, `Environment`, `key`, '', IFNULL(td.`value`,''), IFNULL(td.`Description`,''), '' from testdata td
 ON DUPLICATE KEY UPDATE `Value` = IFNULL(td.`value`,''), Description = IFNULL(td.`Description`,'');
