-- Clean Database from HTML encoded caracters.



-- countryenvparam --
-- ------------------

select * from countryenvparam
where Description like '%&%;%'
or DistribList like '%&%;%'
or EMailBodyRevision like '%&%;%'
or EMailBodyChain like '%&%;%'
or EMailBodyDisableEnvironment like '%&%;%'
;

update countryenvparam 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

update countryenvparam 
set DistribList = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(DistribList,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where DistribList like '%&%;%';

update countryenvparam 
set EMailBodyRevision = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(EMailBodyRevision,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where EMailBodyRevision like '%&%;%';

update countryenvparam 
set EMailBodyChain = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(EMailBodyChain,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where EMailBodyChain like '%&%;%';

update countryenvparam 
set EMailBodyDisableEnvironment = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(EMailBodyDisableEnvironment,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where EMailBodyDisableEnvironment like '%&%;%';

-- testdatalib --
-- --------------

select * from testdatalib
where Script like '%&%;%'
or ServicePath like '%&%;%'
or Method like '%&%;%'
or Envelope like '%&%;%'
or Description like '%&%;%'
;

update testdatalib 
set Script = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Script,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Script like '%&%;%';

update testdatalib 
set ServicePath = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ServicePath,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where ServicePath like '%&%;%';

update testdatalib 
set Method = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Method,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Method like '%&%;%';

update testdatalib 
set Envelope = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Envelope,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Envelope like '%&%;%';

update testdatalib 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

-- testdatalibdata --
-- --------------

select * from testdatalibdata
where `Value` like '%&%;%'
or `Column` like '%&%;%'
or ParsingAnswer like '%&%;%'
or Description like '%&%;%'
;

update testdatalibdata 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

update testdatalibdata 
set ParsingAnswer = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ParsingAnswer,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where ParsingAnswer like '%&%;%';

update testdatalibdata 
set `Column` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`Column`,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where `Column` like '%&%;%';

update testdatalibdata 
set `Value` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`Value`,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where `Value` like '%&%;%';


-- application --
-- --------------

select * from application
where `svnurl` like '%&%;%'
or `BugTrackerUrl` like '%&%;%'
or BugTrackerNewUrl like '%&%;%'
or Description like '%&%;%'
;

update application 
set svnurl = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(svnurl,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where svnurl like '%&%;%';

update application 
set BugTrackerUrl = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(BugTrackerUrl,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where BugTrackerUrl like '%&%;%';

update application 
set BugTrackerNewUrl = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(BugTrackerNewUrl,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where BugTrackerNewUrl like '%&%;%';

update application 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

-- testcase --
-- -----------

select * from testcase
where `BehaviorOrValueExpected` like '%&%;%'
or `Comment` like '%&%;%'
or HowTo like '%&%;%'
or Description like '%&%;%'
;

update testcase 
set BehaviorOrValueExpected = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(BehaviorOrValueExpected,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where BehaviorOrValueExpected like '%&%;%';

update testcase 
set Comment = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Comment,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Comment like '%&%;%';

update testcase 
set HowTo = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(HowTo,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where HowTo like '%&%;%';

update testcase 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

-- testcasestep --
-- ---------------

select * from testcasestep
where Description like '%&%;%'
;

update testcasestep 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

-- testcasestepaction --
-- ---------------

select * from testcasestepaction
where Description like '%&%;%'
or Object like '%&%;%'
or Property like '%&%;%'
;

update testcasestepaction 
set Description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Description,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where Description like '%&%;%';

-- testcasestepactioncontrol --
-- ---------------

select * from testcasestepactioncontrol
where ControlValue like '%&%;%'
or ControlProperty like '%&%;%'
or ControlDescription like '%&%;%'
;

update testcasestepactioncontrol 
set ControlDescription = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlDescription,'&#61;', '='),'&nbsp;', ' '),'&ccedil;', 'Ç'),'&middot;', '·'),'&rsquo;', '\’'),'&aacute;', 'Á'),'&ndash;', '‒'),'&amp;', '&'),'&laquo;', '«'),'&rdquo;', '”'),'&Eacute;', 'É'),'&acirc;', 'â'),'&pound;', '£'),'&Ucirc;', 'Û'),'&raquo;', '»'),'&ldquo;', '“'),'&oacute;', 'ó'),'&oelig;', 'Œ'),'&atilde;', 'Ã'),'&uacute;', 'Ú'),'&lsquo;', '‘'),'&hellip;', '…'),'&ucirc;', 'Û'),'&ugrave;', 'Ù'),'&Agrave;', 'à'),'&icirc;', 'î'),'&ocirc;', 'ô'),'&ecirc;', 'ê'),'&deg;', '°'),'&euro;', '€'),'&lt;', '<'),'&egrave;', 'è'),'&quot;', '\"'),'&gt;', '>'),'\\&#39;', '\''),'&eacute;', 'é'),'&agrave;', 'à'),'&#39;', '\''),'&#43;', '+'), '&#64;', '@') 
where ControlDescription like '%&%;%';



