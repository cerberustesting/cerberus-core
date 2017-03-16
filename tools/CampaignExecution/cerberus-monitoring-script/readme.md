# Introduction

This scripts executed by a cron task permits to launch with Cerberus.

- **Campaign** with a reporting email showing % of success
- **Monitoring** with an email when a fail occur


## Configuration 

### File lib.function
 
First we set Cerberus URL in the `lib.function.php` file

	$cerberusUrl = 'http://ppicboappcerb01.fra.local:8080/Cerberus/';

And the Cerberus robot name :

	$robot = 'robot_chrome';

### File campaign/monitoring 

Mail recipients in the campaign file (ex : `campaign_qa.php` ou `monitoring_prod.php`   )

	 $destinataires = "email1@domain.com,email2@domain.com";


And the campaign's name :

	$campaign_list = [ "NCAT_Dev"];

### php.ini Config 

Set in php.ini the smtp server of your company.

	SMTP = smtp.domain.com

## Cron tasks (example) : 

Execute daily campaigns from Monday to Friday at 8am :


    0 8 * * 1,2,3,4,5 /usr/bin/php -f /path/to/cerberus-monitoring/campaign_qa.php



Production monitoring every hour from Monday to Saturday :


    0 * * * 1,2,3,4,5,6 /usr/bin/php -f /path/to/cerberus-monitoring/monitoring_prod.php 

