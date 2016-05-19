#!/usr/bin/perl
## Author: Memiks
## Date: 2014-06-06
## Version: 0.3a
## Execution of tests from a campaign on X queue.
## TODO: Test result of test
## TODO: Get time and statistics on all threads
## TODO: Try to use Thread::Thread_Share

use strict;
use warnings;
use LWP::Simple qw($ua get);
use Getopt::Long;

# Autoflush error and standard outputs.
select(STDERR);
$| = 1;
select(STDOUT); # default
$| = 1;

my $debug = 0;

# Specify default parameters value
my $help = 0;
my $campaign = 2;
my $campaignName = '';
my $environment = '';
my $on = 3;
my $robot = 'MyRobot';
my $screenshot = 1;
my $tag = $campaignName . '-' . time();
my $cerberusUrl = 'http://localhost:8080/Cerberus/';

# Retrieve value of parameters
GetOptions(
	'campaign=i'	=> \$campaign,
	'campaignName=s'	=> \$campaignName,
	'environment=s'	=> \$environment,
	'on=i'		=> \$on,
	'robot=s'		=> \$robot,
	'screenshot=i'	=>\$screenshot,
	'tag=s'		=> \$tag,
	'cerberusUrl=s' => \$cerberusUrl,
	'help!'		=> \$help,
) or die "Usage incorrect!\n";

# Set parameter values
my %parameters = ('campaign'=>$campaign,
	'campaignName'=>$campaignName,
	'environment'=>$environment,
	'on' => $on,
	'robot'=>$robot,
	'screenshot'=>$screenshot,
	'tag'=>$tag,
	'cerberus'=> $cerberusUrl,
	'servlet'=> 'GetCampaignExecutionsCommand',
	'timeout'=> 150000
);

# if -help is specified, print help and exit
if( $help ) {
	print " -campaign 2 -environment QA\n";
	print " -on 3 -robot GRID\n";
	exit;
} elsif( $debug ) {
	# if not AND debug to 1, display parameters
	my $param;
	# concat all parameters to print them
	foreach $param (sort keys %parameters) {
		print "$param: $parameters{$param}\n";
	}
}

# Add timeout on get function (lot of seconds !!)
$ua->timeout($parameters{'timeout'});

# Define size of memory allocated, exit only thread on error, result retrieve by string
 use threads ('yield',
'stack_size' => 64*4096,
'exit' => 'threads_only',
'stringify');

# Method to retrieve list of tests
sub generate_url_list {
	my %params = @_;
	my $param;
	# generate URL to retrieve list of tests
	my $URL=$params{'cerberus'}.$params{'servlet'}.'?';

	# delete these two parameters in the array of parameters
	delete $params{'cerberus'};
	delete $params{'servlet'};

	# concat all others parameters to the URL
	foreach $param (sort keys %params) {
		$URL .= "&$param=$params{$param}";
	}
	
	# return URL
	return $URL;
}

# Method executed has thread (below) to retrieve list of test for a queue
# and execute them one by one
sub execute_tests {
	my %args = @_;
	my $i=0;

	# generate URL to retrieve list of tests for the queue
	my $URL=generate_url_list(%args);

	# get list of tests
	my $content = get($URL);
	# split it to an Array of test URL
	my @listOfTest = split('\r\n',$content);

	# Retrieve number of tests for the queue
	my $numberOfTests = @listOfTest;

	# For each element of listOfTest
	for ($i = 0; $i < @listOfTest; ++$i) {
		# Generate cerberus RunTest URL by added environment parameter
		my $TestURL = $listOfTest[$i].'&Environment='.$args{'environment'};

		# display URL currently executed by the tread
		$TestURL =~ s/Browser/browser/;

		$TestURL =~ /&Test=([^&]+).*&TestCase=([^&]+).*&Country=([^&]+)/;
		print "Executing test case '$2', from test '$1' on '$3' environment...\n";

		# retrieve the content of the URL (nothing done with it for the moment)
		$content = get("$TestURL");
	}
}

# Array of threads created
my @threads=();

# Instanciate from variable
my $from;
# List of arguments for threads
my @argument = ();

# For each queue, create a thread to retrieve list of tests and execute it
for ($from = 1; $from <= $parameters{'on'}; $from++) {
	# Add from parameter on thread parameters
	@argument = (%parameters, ('from'=>$from));

	# Start thread and push it in threads array.
	push(@threads, threads->create('execute_tests', @argument));
}

my $index = 0;
# For all threads
while ($threads[$index]) {
	# Wait end of Thread execution
	$threads[$index]->join();
	# When Thread is ended wait the next one
	$index++;
}

if($tag) {
	my $resultOfCampaign = get($parameters{'cerberus'}."/ResultCI?tag=".$parameters{'tag'});
    print "Campaign result: $resultOfCampaign\n";
	print "Campaign report: $cerberusUrl/ReportingExecutionByTag.jsp?Tag=$tag\n";

	if($resultOfCampaign eq "OK") {
		exit 0;
	} else {
		exit 1;
	}
}

