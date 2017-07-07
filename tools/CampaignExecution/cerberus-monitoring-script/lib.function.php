<?php

set_time_limit(3600);

ini_set('error_reporting', 'E_ALL & ~E_DEPRECATED & ~E_STRICT & ~E_WARNING');

$tag_list = [];
$cerberusUrl = 'http://ppicboappcerb01.fra.local:8080/Cerberus/';

function generate_url_list() {
    global $parameters, $cerberus, $servlet;
    # generate URL to retrieve list of t$servletests
    $URL = $cerberus . $servlet . '?';

    # concat all others parameters to the URL
    foreach ($parameters as $key => $value) {
        $URL .= "&$key=$value";
    }

    # return URL
    return $URL;
}

function executeACampaign($campaignName) {

    global $destinataires, $monitoring, $parameters, $cerberus, $cerberusUrl, $servlet, $cerberusUrl, $tag_list;

# Specify default parameters value
//    $help = 0;
    $campaign = 2;
//    $campaignName = $campaignName;
//    $environment = 'QA';
    $on = 20;
    $robot = 'robot_chrome';
//    $robot = 'vplancke_chrome';
    $screenshot = 1;
    if ($monitoring) {
        $tag = $campaignName;
    } else {
        $tag = date("Ymd_") . $campaignName;
    }

    array_push($tag_list, $tag);

    $cerberus = $cerberusUrl;
    $servlet = 'GetCampaignExecutionsCommand';

    # Set parameter values
    $parameters = array('campaign' => $campaign,
        'campaignname' => $campaignName,
        'environment' => $environment,
        'on' => $on,
        'robot' => $robot,
        'screenshot' => $screenshot,
        'tag' => $tag,
        'timeout' => 30000,
//        'retries' => 1,
        'outputformat' => 'compact'
    );

    $i = 0;

    # generate URL to retrieve list of tests for the queue
    $URL = generate_url_list();
    # get list of tests
    $content = file_get_contents($URL);
    # explode it to an Array of test URL
    $listOfTest = explode("\r\n", $content);

    # Retrieve number of tests for the queue
    $numberOfTests = count($listOfTest) - 1;

    # For each element of listOfTest
    for ($i = 0; $i < $numberOfTests; ++$i) {

        # Generate cerberus RunTest URL by added environment parameter
        $TestURL = $listOfTest[$i] . '&outputformat=compact';
        preg_match("#&Test=(.*)&TestCase#", $TestURL, $testResult);
        $testName = $testResult[1];
        preg_match("#&TestCase=(.*)&Browser#", $TestURL, $testCaseResult);
        $testCase = $testCaseResult[1];

        //lance le test ici
        echo $TestURL . "<br>\n";

        $result = file_get_contents($TestURL);

        //monitoring result
        if ($monitoring && !preg_match("#OK|not selected for country#", $result)) {
//            echo $result;

            preg_match("# - ([0-9]*) \[[a-zA-Z0-9]*\|([a-zA-Z0-9]*)\|#", $result, $matched);
            $executionLink = "See execution : $cerberusUrl/ExecutionDetail2.jsp?executionId=$matched[1] ";
            $testCase = $matched[2];
            if ($testCase != "") {
                preg_match("# -([0-9]+) [#", $result, $matched);
//                echo "\n" . "destinataire : $destinataires \n";
                //email le lien de lu repport on error
                $link = " Campaign : $cerberusUrl/ReportingExecutionByTag.jsp?Tag=$tag\n";
                $link_last_executions = "To see if this test often fails, you can see the history :    $cerberusUrl/ExecutionDetailList.jsp?test=$testName&testcase=$testCase\n";
                $resultat = getResult($tag);
                $message = htmlentities("The test n° $testCase failed ") . "<br><br>" . htmlentities("$result") . "<br>" . " <br /> " .
                        htmlentities($executionLink) . "<br> <br>" . htmlentities($link) . "<br> <br>" . htmlentities($link_last_executions)
                        . " <br /><br />" . htmlentities($resultat) . " <br /><br />";
                ;
                //envoie du mail
                $sujet = htmlentities("[Cerberus Monitoring] A test FAILED");
                email($destinataires, $sujet, $message);
            }
        }
    }
}

//démarre les campagnes passées en paramètre
function startCampaigns() {
    global $monitoring, $campaign_list;

    if (!$monitoring) { //si on est pas en monitoring on lance les CDT sans attendre la réponse de cerberus
        ini_set('default_socket_timeout', 1);
    } else {
        ini_set('default_socket_timeout', 600);
    }

    //boucle liste campagne
    foreach ($campaign_list as $campaignName) {
//        echo "go campaign $campaignName <br>";
        executeACampaign($campaignName, $monitoring);
    }

    if (!$monitoring) { //si c'est pas un monitoring on fait un mail récapitulatif
        //_TODO : result ci : return json % success
        reportByEmail();
    }
}

function email($destinataires, $sujet, $message) {

    $headers = 'MIME-Version: 1.0' . "\r\n";
    $headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";
    if (!mail($destinataires, $sujet, $message, $headers)) {
        echo "Error while sending email";
    }
}

//email la liste des campagnes
function reportByEmail() {
    //email avec une URL(tag) par campagne
    global $cerberusUrl, $tag_list, $campaign_list, $destinataires;

    foreach ($tag_list as $key => $tag) {
        if ($tag) {
            //get result
            $resultat = getFinalResult($tag);
            $link = "$cerberusUrl/ReportingExecutionByTag.jsp?Tag=$tag\n";
            $message .= "The campaign  <b> $campaign_list[$key] </b>" . htmlentities(" started, the result will be available here : ") . " <br /> " .
                    htmlentities($link) . " <br /><br />" . htmlentities($resultat) . " <br /><br />";
//            echo $message;
        }
    }
    //envoie du mail
    $sujet = htmlentities("[Cerberus] Execution of campaigns ");

    email($destinataires, $sujet, $message);

    $tag_list = [];
    exit;
}

function getFinalResult($tag) {
    global $getResult, $cerberusUrl;
    if (isset($getResult) && $getResult) {
        $resultUrl = $cerberusUrl . "ResultCIV001?tag=$tag";
        $resultJson = json_decode(file_get_contents($resultUrl), true);
        //print_r($resultJson);

        //tant que c'est pas démarré OU pas terminé, on attend
        while ($resultJson['TOTAL_nbOfExecution'] == 0 || $resultJson['status_PE_nbOfExecution'] != 0) {
            sleep(5);
            $resultJson = json_decode(file_get_contents($resultUrl), true);
            //print_r($resultJson);
        }
        //stats message
        $resultMessage = "Result : " . number_format($resultJson['status_OK_nbOfExecution'] / $resultJson['TOTAL_nbOfExecution'] * 100, 2)
                . " % success (" . $resultJson['status_OK_nbOfExecution'] . "/" . $resultJson['TOTAL_nbOfExecution'] . ") ";

        htmlentities($resultMessage);
        return $resultMessage;
    }
    return "";
}

function getResult($tag) {
    //get live result
    global $getResult, $cerberusUrl;
    $resultUrl = $cerberusUrl . "ResultCIV001?tag=$tag";
    $resultJson = json_decode(file_get_contents($resultUrl), true);
    //stats message
    $resultMessage = "Current result : " . number_format($resultJson['status_OK_nbOfExecution'] / $resultJson['TOTAL_nbOfExecution'] * 100, 2)
            . " % success (" . $resultJson['status_OK_nbOfExecution'] . "/" . $resultJson['TOTAL_nbOfExecution'] . ") ";

    htmlentities($resultMessage);
    return $resultMessage;
}
