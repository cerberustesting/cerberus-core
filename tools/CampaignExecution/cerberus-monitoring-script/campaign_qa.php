<?php

///////////////////////PARAMETRES A GERER //////

$campaign_list = [ "campaign_qa"];

if (isset($_GET['destinataires']) && $_GET['destinataires'] != '') {
    $destinataires = $_GET['destinataires']; 
} else {
    $destinataires = "email1@domain.com,email2@domain.com";
}
$monitoring = false;
$getResult = true;
require_once 'lib.function.php';
 
startCampaigns(); 