<?php

///////////////////////PARAMETRES A CONFIGURER //////
$campaign_list = [ "campaign_prod_monitoring"];
$destinataires = "email1@domain.com,email2@domain.com";
$monitoring = true;
///////////////////////////////////////////////

require_once 'lib.function.php';

//lance les campagne avec le parametre monitoring = true / envoie un email en cas d'erreur 
startCampaigns(); 
