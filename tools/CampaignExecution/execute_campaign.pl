#!/usr/bin/perl
## Author: Memiks
## Date: 2014-06-06
## Version: 0.3a
## Execution of tests from a campaign on X queue.
## TODO: Test result of test
## TODO: Get time and statistics on all threads
## TODO: Try to use Thread::Thread_Share

my %parameters = ('campaign'=>2,
	'environment'=>'QA',
	'on' => 3,
	'robot'=>'MyRobot',
	'cerberus'=> 'http://localhost:8080/Cerberus-0.9.2-SNAPSHOT/',
	'servlet'=> 'GetCampaignExecutionsCommand',
	'timeout'=> 150000
);

use strict;
use warnings;
use LWP::Simple qw($ua get);

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

		# display some information in the terminal
		print "Execute Test $i/$numberOfTests of from=".$args{'from'}." on=".$args{'on'}."\n";
		# display URL currently executed by the tread
		print $TestURL."\n\n";

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

