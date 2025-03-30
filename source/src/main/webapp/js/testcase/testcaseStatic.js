/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */

// ACTION
var actionOptGroupList = [
    {"name": "access_application", "label": {"en": "Application Access", "fr": "Accès à l'Application"}, "picto": "<img width='20px' height='20px' src='images/action-website.png'/>"},
    {"name": "mouse_action", "label": {"en": "Mouse Action", "fr": "Action à la souris"}, "picto": "<img width='20px' height='20px' src='images/action-mouse.png'/>"},
    {"name": "finger_action", "label": {"en": "Finger Action", "fr": "Action au doigt"}, "picto": "<img width='20px' height='20px' src='images/action-tap.png'/>"},
    {"name": "context_action", "label": {"en": "Context Action", "fr": "Action de Contexte"}, "picto": "<img width='20px' height='20px' src='images/action-settings.png'/>"},
    {"name": "keyboard_action", "label": {"en": "Keyboard Action", "fr": "Action au Clavier"}, "picto": "<img width='20px' height='20px' src='images/action-keyboard.png'/>"},
    {"name": "command", "label": {"en": "Execute Command", "fr": "Execution de Commande"}, "picto": "<img width='20px' height='20px' src='images/action-command-line.png'/>"},
    {"name": "wait", "label": {"en": "Wait", "fr": "Attendre"}, "picto": "<img width='20px' height='20px' src='images/action-time-left.png'/>"},
    {"name": "file", "label": {"en": "File", "fr": "Fichier"}, "picto": "<img width='20px' height='20px' src='images/action-file.png'/>"},
    {"name": "device", "label": {"en": "Mobile Device", "fr": "Appareil Mobile"}, "picto": "<img width='20px' height='20px' src='images/action-mobile-application.png'/>"},
    {"name": "context_control", "label": {"en": "Context Controls", "fr": "Contexte des Contrôles"}, "picto": "<img width='20px' height='20px' src='images/action-share.png'/>"}

];

/*
 
 ###   available class for autocomplete tuning 
 #############################################

 crb-autocomplete-variable          (include Variables ONLY) #####
 crb-autocomplete-element           (include Elements+Variables) #####
 crb-autocomplete-service           (include Services+Variables) #####
 crb-autocomplete-property          (include Properties+Variables) #####
 crb-autocomplete-switch            (include Switch Elements+Variables (aka url= regexurl=, title=, etc) ) #####
 crb-autocomplete-select            (include Select Option Elements+Variables) #####
 crb-autocomplete-boolean           (include Boolean+Variables) #####
 crb-autocomplete-fileuploadflag    (include File Flags+Variables) #####
 crb-autocomplete-filesortflag      (include File Flags+Variables) #####
 
 */

var actionOptList = {
    "unknown": {"group": "none", "value": "Unknown", "label_en": "None", "label": {"en": "Define an action", "fr": "Choisir une action"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"]},
    "click": {"group": "mouse_action", "value": "click", "label": {"en": "Click", "fr": "Cliquer"}, "application_types": ["GUI", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à cliquer"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}, "documentation": {"en": "...", "fr": "..."},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100) [GUI,APK,IPA only]", "fr": "[Optionnel] Offset (ex : 50,100) [seulement GUI,APK,IPA]"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-variable crb-contextual-button"}},
    "longPress": {"group": "mouse_action", "value": "longPress", "label": {"en": "longPress", "fr": "Cliquer x secondes"}, "application_types": ["GUI", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à cliquer"}, "picto": "images/action-html.png", "class": "col-lg-9 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[opt] Duration (ms) : 8000 by default", "fr": "[opt] Valeur (ms) : 8000 par défaut"}, "picto": "images/action-time-left.png", "class": "col-lg-3 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "mouseLeftButtonPress": {"group": "mouse_action", "value": "mouseLeftButtonPress", "label": {"en": "Press and keep left button", "fr": "Presser et maintenir le bouton gauche"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à cibler"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100) [GUI only]", "fr": "[Optionnel] Offset (ex : 50,100) [seulement GUI]"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}},
    "mouseLeftButtonRelease": {"group": "mouse_action", "value": "mouseLeftButtonRelease", "label": {"en": "Release left button", "fr": "Relacher le bouton gauche"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100) [GUI only]", "fr": "[Optionnel] Offset (ex : 50,100) [seulement GUI]"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}},
    "doubleClick": {"group": "mouse_action", "value": "doubleClick", "label": {"en": "Double Click", "fr": "Double Clic"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à double-cliquer"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100)", "fr": "[Optionnel] Offset (ex : 50,100)"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}},
    "rightClick": {"group": "mouse_action", "value": "rightClick", "label": {"en": "Right Click", "fr": "Clic droit"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à clicker avec le bouton droit"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100) [GUI only]", "fr": "[Optionnel] Offset (ex : 50,100) [seulement GUI]"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}},
    "mouseOver": {"group": "mouse_action", "value": "mouseOver", "label": {"en": "Mouse Over", "fr": "Souris sur l'élément"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "[Optional] Offset. (ex : 50,100) [GUI, FAT only]", "fr": "[Optionnel] Offset (ex : 50,100) [seulement GUI,FAT]"}, "picto": "images/action-numeric.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "mouseMove": {"group": "mouse_action", "value": "mouseMove", "label": {"en": "Move Mouse", "fr": "Déplacer la souris"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Relative coord. (ex : 50,100 ; 200,50)", "fr": "Coordonnées relatives (ex : 50,100 ; 200,50)"}, "class": "col-lg-12 crb-autocomplete-variable"}},
    "openUrlWithBase": {"group": "access_application", "value": "openUrlWithBase", "label": {"en": "openUrlWithBase", "fr": "Appeler l'URI"}, "application_types": ["GUI", "IPA", "APK"],
        "field1": {"label": {"en": "URI to call  (ex : /index.html)", "fr": "URI à appeler (ex : /index.html)"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "openUrlLogin": {"group": "access_application", "value": "openUrlLogin", "label": {"en": "openUrlLogin", "fr": "Appeler l'URL de Login"}, "application_types": ["GUI", "IPA", "APK"]},
    "openUrl": {"group": "access_application", "value": "openUrl", "label": {"en": "Open Url", "fr": "Appeler l'URL"}, "application_types": ["GUI", "IPA", "APK"],
        "field1": {"label": {"en": "URL to call (ex : http://www.domain.com)", "fr": "URL à appeler (ex : http://www.domain.com)"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "focusToIframe": {"group": "context_action", "value": "focusToIframe", "label": {"en": "Focus to Iframe", "fr": "Switcher sur l'Iframe"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "Element path of the target iFrame", "fr": "Chemin vers l'élement de l'iFrame à cibler"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "focusDefaultIframe": {"group": "context_action", "value": "focusDefaultIframe", "label": {"en": "Focus to main context", "fr": "Switcher sur le context principal"}, "application_types": ["GUI"]},
    "switchToWindow": {"group": "context_action", "value": "switchToWindow", "label": {"en": "Switch to Window", "fr": "Switcher sur l'onglet"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "Window title or url (incl. regexTitle and regexUrl)", "fr": "Titre ou url de la fenêtre  (incl. regexTitle and regexUrl)"}, "picto": "images/action-website.png", "class": "col-lg-12 crb-autocomplete-switch"}},
    "switchToContext": {"group": "context_action", "value": "switchToContext", "label": {"en": "Switch to context", "fr": "Switcher sur le contexte"}, "application_types": ["IPA", "APK"],
        "field1": {"label": {"en": "[opt] Context name", "fr": "[opt] Nom du contexte"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "manageDialog": {"group": "context_action", "value": "manageDialog", "label": {"en": "Manage Dialog", "fr": "Gérer la popup"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "ok or cancel", "fr": "ok ou cancel"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "manageDialogKeypress": {"group": "context_action", "value": "manageDialogKeypress", "label": {"en": "Manage Dialog pressing key", "fr": "Switcher sur l'Iframe"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "keys to press", "fr": "Touches à appuyer"}, "picto": "images/action-keyboard.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "refreshCurrentPage": {"group": "context_action", "value": "refreshCurrentPage", "label": {"en": "Refresh Page", "fr": "Recharger la page"}, "application_types": ["GUI"]},
    "returnPreviousPage": {"group": "context_action", "value": "returnPreviousPage", "label": {"en": "Return Previous Page", "fr": "Retourner page précédente"}, "application_types": ["GUI"]},
    "forwardNextPage": {"group": "context_action", "value": "forwardNextPage", "label": {"en": "Forward Next Page", "fr": "Aller à la page suivante"}, "application_types": ["GUI"]},
    "executeJS": {"group": "command", "value": "executeJS", "label": {"en": "Execute Javascript Command", "fr": "Executer une commande Javascript"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "JavaScript to execute", "fr": "JavaScript à executer"}, "picto": "images/action-command-line.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "executeCommand": {"group": "command", "value": "executeCommand", "label": {"en": "Execute Appium Command", "fr": "Executer une commande Appium"}, "application_types": ["GUI", "IPA", "APK"],
        "field1": {"label": {"en": "Appium Command (ex : \"mobile:deepLink\")", "fr": "Commande Appium (ex : \"mobile:deepLink\")"}, "picto": "images/action-command-line.png", "class": "col-lg-6 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})", "fr": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})"}, "class": "col-lg-6 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "executeCerberusCommand": {"group": "command", "value": "executeCerberusCommand", "label": {"en": "Execute Cerberus Command", "fr": "Executer une commande Cerberus"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "Command (ex : \"grep\")", "fr": "Commande (ex : \"grep\")"}, "picto": "images/action-command-line.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "openApp": {"group": "access_application", "value": "openApp", "label": {"en": "Open Application", "fr": "Lancer l'application"}, "application_types": ["GUI", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Application name or path or package for Android", "fr": "Nom ou chemin de l'application, package pour android"}, "picto": "images/action-mobile-application.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "[Optional, required for Android] Activity", "fr": "[Optionnel, obligatoire pour Android] Activity"}, "class": "col-lg-4 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "closeApp": {"group": "command", "value": "closeApp", "label": {"en": "Close Application", "fr": "Fermer l'application"}, "application_types": ["GUI", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Application name or path", "fr": "Nom ou chemin de l'application"}, "picto": "images/action-mobile-application.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "dragAndDrop": {"group": "mouse_action", "value": "dragAndDrop", "label": {"en": "Drag And Drop", "fr": "Glisser Déposer"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin de l'élement"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Destination Element Path or offset (offset=xx;yy)", "fr": "Destination de l'élément ou offset (offset=xx;yy)"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}, "documentation": {"en": "...", "fr": "..."}},
    "select": {"group": "mouse_action", "value": "select", "label": {"en": "Choose option in select box", "fr": "Choisir une option dans un Select"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Option value", "fr": "Chemin vers l'option"}, "picto": "images/action-command-line.png", "class": "col-lg-12 crb-autocomplete-select"}, "documentation": {"en": "...", "fr": "..."}},
    "keypress": {"group": "keyboard_action", "value": "keypress", "label": {"en": "Press Key", "fr": "Appuyer sur une touche"}, "application_types": ["GUI"],
        "field1": {"label": {"en": "[opt] Target element path", "fr": "[opt] Chemin vers l'élement à cibler"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Key to Press", "fr": "Touche à appuyer"}, "picto": "images/action-keyboard.png", "class": "col-lg-6 crb-autocomplete-variable"},
        "field3": {"label": {"en": "[opt] Modifier to press", "fr": "[opt] Touche modificatrice"}, "picto": "images/action-keyboard.png", "class": "col-lg-6 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "type": {"group": "keyboard_action", "value": "type", "label": {"en": "Feed field", "fr": "Remplir le champs"}, "application_types": ["GUI", "APK", "IPA", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text to Type", "fr": "Texte à entrer"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "hideKeyboard": {"group": "keyboard_action", "value": "hideKeyboard", "label": {"en": "Hide Keyboard", "fr": "Cacher le Clavier"}, "application_types": ["APK", "IPA"], "documentation": {"en": "...", "fr": "..."}},
    "clearField": {"group": "keyboard_action", "value": "clearField", "label": {"en": "Clear Field", "fr": "Vider l'élément"}, "application_types": ["GUI", "APK", "IPA", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Chemin vers l'élement à effacer"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}, "documentation": {"en": "...", "fr": "..."}},
    "swipe": {"group": "finger_action", "value": "swipe", "label": {"en": "Swipe", "fr": "Swiper"}, "application_types": ["APK", "IPA"],
        "field1": {"label": {"en": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "fr": "Action (UP DOWN LEFT RIGHT CUSTOM...)"}, "picto": "images/action-font.png ", "class": "col-lg-6"},
        "field2": {"label": {"en": "Direction x;y;z;y", "fr": "Direction x;y;z;y"}, "picto": "images/action-settings.png", "class": "col-lg-6"}, "documentation": {"en": "...", "fr": "..."}},
    "wait": {"group": "wait", "value": "wait", "label": {"en": "Wait", "fr": "Attendre"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"],
        "field1": {"label": {"en": "Duration(ms) or Element", "fr": "Valeur (ms) ou élement"}, "picto": "images/action-time-left.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}, "documentation": {"en": "...", "fr": "..."}},
    "waitVanish": {"group": "wait", "value": "waitVanish", "label": {"en": "Wait Element Vanish", "fr": "Attendre la disparition de l'élément"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Element path", "fr": "Element"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"}, "documentation": {"en": "...", "fr": "..."}},
    "waitNetworkTrafficIdle": {"group": "wait", "value": "waitNetworkTrafficIdle", "label": {"en": "Wait Network traffic Idle", "fr": "Attendre la fin du chargement réseau"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "documentation": {"en": "...", "fr": "..."}},
    "callService": {"group": "access_application", "value": "callService", "label": {"en": "Call Service", "fr": "Appeler le Service"}, "application_types": ["GUI", "APK", "IPA", "FAT", "SRV"],
        "field1": {"label": {"en": "Service Name", "fr": "Nom du Service"}, "picto": "images/action-api.png", "class": "col-lg-12 crb-autocomplete-service crb-contextual-button"},
        "field2": {"label": {"en": "Nb Evt (Kafka)", "fr": "Nb Evt à attendre (Kafka)"}, "picto": "images/action-settings.png", "class": "col-lg-6 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Evt Wait sec (Kafka)", "fr": "Tps d'attente en sec (Kafka)"}, "picto": "images/action-time-left.png", "class": "col-lg-6 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "executeSqlUpdate": {"group": "command", "value": "executeSqlUpdate", "label": {"en": "Execute SQL script (insert/update)", "fr": "Executer un script SQL (insert/update)"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Database Name", "fr": "Nom de Base de donnée"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Script", "fr": "Script à executer"}, "picto": "images/action-script.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "executeSqlStoredProcedure": {"group": "command", "value": "executeSqlStoredProcedure", "label": {"en": "Execute SQL Stored Procedure", "fr": "Executer une procedure stoquée SQL"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Database Name", "fr": "Nom de Base de donnée"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Stored Procedure", "fr": "Procedure Stoquée à executer"}, "picto": "images/action-script.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "calculateProperty": {"group": "command", "value": "calculateProperty", "label": {"en": "Calculate Property", "fr": "Calculer la propriété"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Property Name", "fr": "Nom d'une Proprieté"}, "picto": "images/action-font.png", "class": "col-lg-6 crb-autocomplete-property crb-contextual-button"},
        "field2": {"label": {"en": "[opt] Name of an other property", "fr": "[opt] Nom d'une autre propriété"}, "picto": "images/action-font.png", "class": "col-lg-6 crb-autocomplete-property"}, "documentation": {"en": "...", "fr": "..."}},
    "setNetworkTrafficContent": {"group": "context_control", "value": "setNetworkTrafficContent", "label": {"en": "Switch context to network traffic content", "fr": "Passer au contenu du traffic réseau"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "url to filter", "fr": "URL à filtrer"}, "picto": "images/action-link.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Activate http response content (Y/N)", "fr": "Activation du contenu des reponses http (Y/N)"}, "picto": "images/action-settings.png", "class": "col-lg-4 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "indexNetworkTraffic": {"group": "command", "value": "indexNetworkTraffic", "label": {"en": "Index Network Traffic", "fr": "Indexer le contenu du traffic réseau"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "[opt] Index name", "fr": "[opt] Nom de l'index"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "setServiceCallContent": {"group": "context_control", "value": "setServiceCallContent", "label": {"en": "Switch context to service call content", "fr": "Passer au contenu du dernier service appelé"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"], "documentation": {"en": "...", "fr": "..."}},
    "setConsoleContent": {"group": "context_control", "value": "setConsoleContent", "label": {"en": "Switch context to web console content", "fr": "Passer au contenu de la console"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"], "documentation": {"en": "...", "fr": "..."}},
    "setContent": {"group": "context_control", "value": "setContent", "label": {"en": "Switch context to specific content", "fr": "Passer au contenu spécifique"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Value to Set", "fr": "Valeur"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "scrollTo": {"group": "mouse_action", "value": "scrollTo", "label": {"en": "Scroll to element", "fr": "Scroller jusqu'à l'élément"}, "application_types": ["GUI", "IPA", "APK", "FAT"],
        "field1": {"label": {"en": "Element path or Text to scroll to", "fr": "Chemin vers l'élement ou Texte à scroller"}, "picto": "images/action-html.png", "class": "col-lg-12 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Max scroll iteration [APK,IPA only]", "fr": "Nombre maximum de scroll vers le bas (8 par defaut) [seulement APK,IPA]"}, "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "offsets (h,v) [APK,IPA,GUI only]", "fr": "offsets (h,v) [seulement APK,IPA,GUI]"}, "class": "col-lg-6 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "installApp": {"group": "access_application", "value": "installApp", "label": {"en": "Install Application", "fr": "Installer l'Application"}, "application_types": ["IPA", "APK"],
        "field1": {"label": {"en": "Application path (ex : /root/toto.apk)", "fr": "Chemin vers l'application (ex : /root/toto.apk)"}, "picto": "images/action-mobile-application.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "removeApp": {"group": "access_application", "value": "removeApp", "label": {"en": "Uninstall Application", "fr": "Désinstaller l'Application"}, "application_types": ["IPA", "APK"],
        "field1": {"label": {"en": "Application package (ex : com.cerberus.appmobile)", "fr": "Package de l'application (ex : com.cerberus.appmobile)"}, "picto": "images/action-mobile-application.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "cleanRobotFile": {"group": "file", "value": "cleanRobotFile", "label": {"en": "Clean Robot File Folder", "fr": "Vider le dossier fichier du robot"}, "application_types": ["GUI", "FAT"],
        "field1": {"label": {"en": "Path/Pattern to empty. ex : /home/seluser/Downloads/", "fr": "Chemin du dossier à vider. ex : /home/seluser/Downloads/"}, "picto": "images/action-file.png", "class": "col-lg-12 crb-autocomplete-variable"}, "documentation": {"en": "...", "fr": "..."}},
    "uploadRobotFile": {"group": "file", "value": "uploadRobotFile", "label": {"en": "Upload File to Robot", "fr": "Upload un fichier vers le Robot"}, "application_types": ["GUI", "APK", "IPA", "FAT", "SRV"],
        "field1": {"label": {"en": "Filename to create. ex : /home/seluser/Downloads/test.json", "fr": "Nom du fichier à créer. ex : /home/seluser/Downloads/test.json"}, "picto": "images/action-file.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Content to upload (base64)", "fr": "Contenu à charger (base64)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable  crb-contextual-button"},
        "field3": {"label": {"en": "Option (EMPTYFOLDER)", "fr": "Option (EMPTYFOLDER)"}, "picto": "images/action-settings.png", "class": "col-lg-12 crb-autocomplete-fileuploadflag"}, "documentation": {"en": "...", "fr": "..."}},
    "getRobotFile": {"group": "file", "value": "getRobotFile", "label": {"en": "Download File from Robot", "fr": "Télécharger un fichier depuis le Robot"}, "application_types": ["GUI", "APK", "IPA", "FAT", "SRV"],
        "field1": {"label": {"en": "Path/Pattern to retrieved. ex : /home/seluser/Downloads/", "fr": "Nom du fichier à récupérer. ex : /home/seluser/Downloads/"}, "picto": "images/action-file.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Nb of files", "fr": "Nb de fichiers"}, "picto": "images/action-numeric.png", "class": "col-lg-6 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Sorting Option. (LASTMODIFIED/IGNORECASEDESC/IGNORECASEASC/DESC/ASC)", "fr": "Option de tri. (LASTMODIFIED/IGNORECASEDESC/IGNORECASEASC/DESC/ASC)"}, "picto": "images/action-settings.png", "class": "col-lg-6 crb-autocomplete-filesortflag"}, "documentation": {"en": "...", "fr": "..."}},
    "lockDevice": {"group": "device", "value": "lockDevice", "label": {"en": "Lock Device", "fr": "Verrouiller l'appareil"}, "application_types": ["APK", "IPA"], "documentation": {"en": "...", "fr": "..."}},
    "unlockDevice": {"group": "device", "value": "unlockDevice", "label": {"en": "Unlock Device", "fr": "Déverrouiller l'appareil"}, "application_types": ["APK", "IPA"], "documentation": {"en": "...", "fr": "..."}},
    "rotateDevice": {"group": "device", "value": "rotateDevice", "label": {"en": "Rotate Device", "fr": "Tourner l'appareil"}, "application_types": ["APK", "IPA"], "documentation": {"en": "...", "fr": "..."}},
    "doNothing": {"group": "none", "value": "doNothing", "label": {"en": "No action", "fr": "Pas d'action"}, "application_types": ["GUI", "SRV", "IPA", "APK", "FAT"], "documentation": {"en": "...", "fr": "..."}}
};

// CONTROL
var operatorOptList = {
    "unknown": {"value": "unknown", "label": {"en": "---", "fr": "---"}, "control_type": ["unknown", "text_in_page", "text_not_in_page", "text_in_dialog", "take_screenshot", "get_page_source"]},
    "equals": {"value": "equals", "label": {"en": "=", "fr": "="}, "control_type": ["string_comparison", "numeric_comparison", "element", "element_text", "element_numeric", "url", "title"]},
    "different": {"value": "different", "label": {"en": "!=", "fr": "!="}, "control_type": ["string_comparison", "numeric_comparison", "element", "element_numeric", "element_text", "url", "title"]},
    "greater": {"value": "greater", "label": {"en": ">", "fr": ">"}, "control_type": ["string_comparison", "numeric_comparison", "element_numeric"]},
    "greaterOrEqual": {"value": "greaterOrEqual", "label": {"en": ">=", "fr": ">="}, "control_type": ["numeric_comparison", "element_numeric"]},
    "minor": {"value": "minor", "label": {"en": "<", "fr": "<"}, "control_type": ["string_comparison", "numeric_comparison", "element_numeric"]},
    "minorOrEqual": {"value": "minorOrEqual", "label": {"en": "<=", "fr": "<="}, "control_type": ["numeric_comparison", "element_numeric"]},
    "contains": {"value": "contains", "label": {"en": "Contains", "fr": "Contient"}, "control_type": ["string_comparison", "element_text", "url", "title"]},
    "notContains": {"value": "notContains", "label": {"en": "Not Contains", "fr": "Ne Contient Pas"}, "control_type": ["string_comparison", "element_text", "url", "title"]},
    "matchRegex": {"value": "matchRegex", "label": {"en": "Match Regex", "fr": "Match Regex"}, "control_type": ["element_text", "url", "title"]},
    "isPresent": {"value": "isPresent", "label": {"en": "isPresent", "fr": "isPresent"}, "control_type": ["element"]},
    "isNotPresent": {"value": "isNotPresent", "label": {"en": "isNotPresent", "fr": "isNotPresent"}, "control_type": ["element"]},
    "isVisible": {"value": "isVisible", "label": {"en": "isVisible", "fr": "isVisible"}, "control_type": ["element"]},
    "isNotVisible": {"value": "isNotVisible", "label": {"en": "isNotVisible", "fr": "isNotVisible"}, "control_type": ["element"]},
    "isChecked": {"value": "isChecked", "label": {"en": "isChecked", "fr": "isChecked"}, "control_type": ["element"]},
    "isNotChecked": {"value": "isNotChecked", "label": {"en": "isNotChecked", "fr": "isNotChecked"}, "control_type": ["element"]},
    "isClickable": {"value": "isClickable", "label": {"en": "isClickable", "fr": "isClickable"}, "control_type": ["element"]},
    "isNotClickable": {"value": "isNotClickable", "label": {"en": "isNotClickable", "fr": "isNotClickable"}, "control_type": ["element"]},
    "inElement": {"value": "inElement", "label": {"en": "inElement", "fr": "inElement"}, "control_type": ["element"]},
    "isArrayString": {"value": "isArrayString", "label": {"en": "isArrayString", "fr": "isArrayString"}, "control_type": ["array"]},
    "isArrayNumeric": {"value": "isArrayNumeric", "label": {"en": "isArrayNumeric", "fr": "isArrayNumeric"}, "control_type": ["array"]},
    "isElementArrayString": {"value": "isElementArrayString", "label": {"en": "isElementArrayString", "fr": "isElementArrayString"}, "control_type": ["array"]},
    "isElementArrayNumeric": {"value": "isElementArrayNumeric", "label": {"en": "isElementArrayNumeric", "fr": "isElementArrayNumeric"}, "control_type": ["array"]}
};

var convertToGui = {
    "unknown": {"control": "unknown", "operator": "unknown"},

    "verifyStringEqual": {"control": "string_comparison", "operator": "equals",
        "field1": {"label": {"en": "String 1", "fr": "Chaine 1"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Chaine 2"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyStringDifferent": {"control": "string_comparison", "operator": "different",
        "field1": {"label": {"en": "String 1", "fr": "Chaine 1"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Chaine 2"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyStringGreater": {"control": "string_comparison", "operator": "greater",
        "field1": {"label": {"en": "String 1", "fr": "Chaine 1"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Chaine 2"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyStringMinor": {"control": "string_comparison", "operator": "minor",
        "field1": {"label": {"en": "String 1", "fr": "Chaine 1"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Chaine 2"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyStringContains": {"control": "string_comparison", "operator": "contains",
        "field1": {"label": {"en": "String 1 (ex : toto)", "fr": "Chaine 1 (ex : toto)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2 (ex : ot)", "fr": "Chaine 2 (ex : ot)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyStringNotContains": {"control": "string_comparison", "operator": "notContains",
        "field1": {"label": {"en": "String 1 (ex : toto)", "fr": "Chaine 1 (ex : toto)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2 (ex : zot)", "fr": "Chaine 2 (ex : zot)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyNumericEquals": {"control": "numeric_comparison", "operator": "equals",
        "field1": {"label": {"en": "Integer 1 (ex : 1234)", "fr": "Entier 1 (ex : 1234)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 1234)", "fr": "Entier 2 (ex : 1234)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyNumericDifferent": {"control": "numeric_comparison", "operator": "different",
        "field1": {"label": {"en": "Integer 1 (ex : 1234)", "fr": "Entier 1 (ex : 123)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 1234)", "fr": "Entier 2 (ex : 123)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyNumericGreater": {"control": "numeric_comparison", "operator": "greater",
        "field1": {"label": {"en": "Integer 1 (ex : 12)", "fr": "Entier 1 (ex : 12)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 10)", "fr": "Entier 2 (ex : 10)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyNumericGreaterOrEqual": {"control": "numeric_comparison", "operator": "greaterOrEqual",
        "field1": {"label": {"en": "Integer 1 (ex : 12)", "fr": "Entier 1 (ex : 12)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 10)", "fr": "Entier 2 (ex : 10)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyNumericMinor": {"control": "numeric_comparison", "operator": "minor",
        "field1": {"label": {"en": "Integer 1 (ex : 10)", "fr": "Entier 1 (ex : 10)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 12)", "fr": "Entier 2 (ex : 12)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyNumericMinorOrEqual": {"control": "numeric_comparison", "operator": "minorOrEqual",
        "field1": {"label": {"en": "Integer 1 (ex : 10)", "fr": "Entier 1 (ex : 10)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2 (ex : 12)", "fr": "Entier 2 (ex : 12)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyElementPresent": {"control": "element", "operator": "isPresent",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementNotPresent": {"control": "element", "operator": "isNotPresent",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementVisible": {"control": "element", "operator": "isVisible",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementNotVisible": {"control": "element", "operator": "isNotVisible",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementChecked": {"control": "element", "operator": "isChecked",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementNotChecked": {"control": "element", "operator": "isNotChecked",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementClickable": {"control": "element", "operator": "isClickable",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementNotClickable": {"control": "element", "operator": "isNotClickable",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"}},
    "verifyElementEquals": {"control": "element", "operator": "equals",
        "field1": {"label": {"en": "Path of the element", "fr": "Chemin de l'élément"}, "picto": "images/action-settings.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Expected element", "fr": "Element"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementDifferent": {"control": "element", "operator": "different",
        "field1": {"label": {"en": "Path of the element", "fr": "Chemin de l'élément"}, "picto": "images/action-settings.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Expected element", "fr": "Element"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementInElement": {"control": "element", "operator": "inElement",
        "field1": {"label": {"en": "Parent Element", "fr": "Élément parent"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Child Element", "fr": "Élément enfant"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyStringArrayContains": {"control": "array", "operator": "isArrayString",
        "field1": {"label": {"en": "Array (ex : [\"a\",\"b\",\"c\"])", "fr": "Tableau (ex : [\"a\",\"b\",\"c\"])"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Text (ex: a)", "fr": "Texte (ex: a)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyNumericArrayContains": {"control": "array", "operator": "isArrayNumeric",
        "field1": {"label": {"en": "Array (ex : [1,2,3])", "fr": "Tableau (ex : [1,2,3])"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer (ex: 1)", "fr": "Entier (ex: 1)"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementTextArrayContains": {"control": "array", "operator": "isElementArrayString",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text (ex: a)", "fr": "Texte (ex: a)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementNumericArrayContains": {"control": "array", "operator": "isElementArrayNumeric",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Integer (ex: 1)", "fr": "Entier (ex: 1)"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyElementTextEqual": {"control": "element_text", "operator": "equals",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementTextDifferent": {"control": "element_text", "operator": "different",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementTextContains": {"control": "element_text", "operator": "contains",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementTextNotContains": {"control": "element_text", "operator": "notContains",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyElementTextMatchRegex": {"control": "element_text", "operator": "matchRegex",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Regex", "fr": "Regex"}, "picto": "images/action-font.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyElementNumericEqual": {"control": "element_numeric", "operator": "equals",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementNumericDifferent": {"control": "element_numeric", "operator": "different",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementNumericGreater": {"control": "element_numeric", "operator": "greater",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementNumericGreaterOrEqual": {"control": "element_numeric", "operator": "greaterOrEqual",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementNumericMinor": {"control": "element_numeric", "operator": "minor",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},
    "verifyElementNumericMinorOrEqual": {"control": "element_numeric", "operator": "minorOrEqual",
        "field1": {"label": {"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"}, "picto": "images/action-html.png", "class": "col-lg-8 crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Numeric", "fr": "Valeur Numérique"}, "picto": "images/action-numeric.png", "class": "col-lg-8 crb-autocomplete-variable"}},

    "verifyTitleEqual": {"control": "title", "operator": "equals",
        "field1": {"label": {"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyTitleDifferent": {"control": "title", "operator": "different",
        "field1": {"label": {"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyTitleContains": {"control": "title", "operator": "contains",
        "field1": {"label": {"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyTitleNotContains": {"control": "title", "operator": "notContains",
        "field1": {"label": {"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyTitleMatchRegex": {"control": "title", "operator": "matchRegex",
        "field1": {"label": {"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},

    "verifyUrlEqual": {"control": "url", "operator": "equals",
        "field1": {"label": {"en": "URL", "fr": "URL"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyUrlDifferent": {"control": "url", "operator": "different",
        "field1": {"label": {"en": "URL", "fr": "URL"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyUrlContains": {"control": "url", "operator": "contains",
        "field1": {"label": {"en": "URL", "fr": "URL"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyUrlNotContains": {"control": "url", "operator": "notContains",
        "field1": {"label": {"en": "URL", "fr": "URL"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},
    "verifyUrlMatchRegex": {"control": "url", "operator": "matchRegex",
        "field1": {"label": {"en": "URL", "fr": "URL"}, "picto": "images/action-link.png", "class": "col-lg-12 crb-autocomplete-variable"},
        "field3": {"label": {"en": "Case sensitive (true/false)", "fr": "[opt] Sensible à la Casse (true/false)"}, "picto": "images/action-vote.png", "class": "col-lg-4 crb-autocomplete-boolean"}},

    "verifyTextInPage": {"control": "text_in_page", "operator": "unknown",
        "field1": {"label": {"en": "Text to find", "fr": "Texte à rechercher"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "verifyTextNotInPage": {"control": "text_not_in_page", "operator": "unknown",
        "field1": {"label": {"en": "Text not to find)", "fr": "Texte à ne pas trouver"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "verifyTextInDialog": {"control": "text_in_dialog", "operator": "unknown",
        "field1": {"label": {"en": "Text to find", "fr": "Texte à rechercher"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "takeScreenshot": {"control": "take_screenshot", "operator": "unknown",
        "field1": {"label": {"en": "Crop values in pixels (left,right,top,bottom)", "fr": "Cadrer en pixel (gauche,droite,haut,bas)"}, "picto": "images/action-font.png", "class": "col-lg-12 crb-autocomplete-variable"}},
    "getPageSource": {"control": "get_page_source", "operator": "unknown"}
};

var newControlOptList = {
    "unknown": {"value": "unknown", "label": {"en": "Define a control", "fr": "Choisir un control"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"]},
    "string_comparison": {"value": "string_comparison", "label": {"en": "String Comparison", "fr": "Comparaison de Textes"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "equals": "verifyStringEqual", "different": "verifyStringDifferent", "greater": "verifyStringGreater", "minor": "verifyStringMinor", "contains": "verifyStringContains", "notContains": "verifyStringNotContains"},
    "numeric_comparison": {"value": "numeric_comparison", "label": {"en": "Numeric Comparison", "fr": "Comparaison de Numériques"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "equals": "verifyNumericEquals", "different": "verifyNumericDifferent", "greater": "verifyNumericGreater", "greaterOrEqual": "verifyNumericGreaterOrEqual", "minor": "verifyNumericMinor", "minorOrEqual": "verifyNumericMinorOrEqual"},
    "element": {"value": "element", "label": {"en": "Verify Element", "fr": "Vérifier l'élément"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "isPresent": "verifyElementPresent", "isNotPresent": "verifyElementNotPresent", "isVisible": "verifyElementVisible", "isNotVisible": "verifyElementNotVisible", "isChecked": "verifyElementChecked", "isNotChecked": "verifyElementNotChecked", "isClickable": "verifyElementClickable", "isNotClickable": "verifyElementNotClickable", "equals": "verifyElementEquals", "different": "verifyElementDifferent", "inElement": "verifyElementInElement"},
    "element_text": {"value": "element_text", "label": {"en": "Verify Text in Element", "fr": "Vérifier le texte de l'élément"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "equals": "verifyElementTextEqual", "matchRegex": "verifyElementTextMatchRegex", "different": "verifyElementTextDifferent", "contains": "verifyElementTextContains", "notContains": "verifyElementTextNotContains"},
    "element_numeric": {"value": "element_numeric", "label": {"en": "Verify Numeric in Element", "fr": "Vérifier la valeur numérique de l'élément"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "equals": "verifyElementNumericEqual", "different": "verifyElementNumericDifferent", "greater": "verifyElementNumericGreater", "greaterOrEqual": "verifyElementNumericGreaterOrEqual", "minor": "verifyElementNumericMinor", "minorOrEqual": "verifyElementNumericMinorOrEqual"},
    "array": {"value": "array", "label": {"en": "Verify Array Content", "fr": "Vérifier le contenu d'un tableau"}, "application_types": ["GUI", "SRV", "IPA", "APK", "BAT", "FAT", "NONE"], "isArrayString": "verifyStringArrayContains", "isArrayNumeric": "verifyNumericArrayContains", "isElementArrayString": "verifyElementTextArrayContains", "isElementArrayNumeric": "verifyElementNumericArrayContains"},
    "title": {"value": "title", "label": {"en": "Verify Title", "fr": "Vérifier la balise title"}, "application_types": ["GUI"], "equals": "verifyTitleEqual", "matchRegex": "verifyTitleMatchRegex", "different": "verifyTitleDifferent", "contains": "verifyTitleContains", "notContains": "verifyTitleNotContains"},
    "url": {"value": "url", "label": {"en": "Verify URL", "fr": "Vérifier l'URL"}, "application_types": ["GUI"], "equals": "verifyUrlEqual", "matchRegex": "verifyUrlMatchRegex", "different": "verifyUrlDifferent", "contains": "verifyUrlContains", "notContains": "verifyUrlNotContains"},
    "text_in_page": {"value": "text_in_page", "label": {"en": "Verify Text in Page", "fr": "Vérifier que la page contient le texte"}, "application_types": ["GUI"], "unknown": "verifyTextInPage"},
    "text_not_in_page": {"value": "text_not_in_page", "label": {"en": "Verify Text not in Page", "fr": "Vérifier que la page ne contient pas le texte"}, "application_types": ["GUI"], "unknown": "verifyTextNotInPage"},
    "text_in_dialog": {"value": "text_in_dialog", "label": {"en": "Verify Text in Dialog", "fr": "Vérifier que la popup contient"}, "application_types": ["GUI"], "unknown": "verifyTextInDialog"},
    "take_screenshot": {"value": "take_screenshot", "label": {"en": "Take a Screenshot", "fr": "Prendre un Screenshot"}, "application_types": ["GUI"], "unknown": "takeScreenshot"},
    "get_page_source": {"value": "get_page_source", "label": {"en": "Record Page Source", "fr": "Enregistrer les sources de la page"}, "application_types": ["GUI"], "unknown": "getPageSource"}
};

//PROPERTY
//Structure :
//      > type of the property
//        > field
//          > label : text to display
//          > class : class to add (to determine length for example
//          > editorMode : Which ace editor choose (json, xml, html, cerberus)
//          > picto : Picto to display
//          > insertAfter : name of the previous field
//          > displayConditions : if present, condition of display regarding the value of defined field
var newPropertyPlaceholder = {
    "text": {
        "value1": {
            "label": {
                "en": "Value",
                "fr": "Value"
            },
            "class": "col-sm-9",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-font.png",
            "insertAfter": "length"
        },
        "length": {
            "label": {
                "en": "Length (nb of rows)",
                "fr": "Length (nb of rows)"
            },
            "class": "col-sm-3",
            "picto": "images/property-length.png",
            "insertAfter": "nature",
            "displayConditions": [{
                    "key": "nature",
                    "values": ["RANDOM", "RANDOMNEW"]
                }]
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                },
                {
                    "value": "RANDOMNEW",
                    "label": {
                        "en": "Random & New",
                        "fr": "Aléatoire & Nouvelle"
                    }
                }
            ],
            "insertAfter": "pType"
        },
        "helperInformation": ["nature", "length"],
        "helperMessages": {
            "helper-STATIC-x": "Return text",
            "helper-STATIC-0": "Return text",
            "helper-RANDOM-x": "Generate random string with defined length using characters present in value",
            "helper-RANDOM-0": "A length value is mandatory to Generate a random string. ",
            "helper-RANDOMNEW-x": "Generate random string with defined length using characters present in value",
            "helper-RANDOMNEW-0": "A length value is mandatory to Generate a random string.",
        }
    },
    "getFromSql": {
        "value1": {
            "label": {
                "en": "SQL Query",
                "fr": "Requête SQL"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/sql",
            "picto": "images/property-sql-query.png",
            "insertAfter": "database"
        },
        "database": {
            "label": {
                "en": "Database",
                "fr": "Base de données"
            },
            "class": "col-sm-4",
            "picto": "images/property-bdd.png",
            "insertAfter": "pType"
        },
        "rowLimit": {
            "label": {
                "en": "[opt] Row Limit",
                "fr": "[opt] Row Limit"
            },
            "class": "col-sm-3",
            "picto": "images/property-maxlength.png",
            "insertAfter": "nature"
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                },
                {
                    "value": "RANDOMNEW",
                    "label": {
                        "en": "Random & New",
                        "fr": "Aléatoire & Nouvelle"
                    }
                }
            ],
            "insertAfter": "value1"
        },
        "rank": {
            "label": {
                "en": "Rank",
                "fr": "Rang"
            },
            "class": "col-sm-3",
            "picto": "images/property-rank.png",
            "insertAfter": "rowLimit"
        },
        "helperInformation": ["rowLimit", "nature", "rank"],
        "helperMessages": {
            "helper-x-STATIC-0": "Get the first value from the result set retrieved from Database",
            "helper-1-STATIC-x": "To retrieve specific value on result set, Row Limit must be set and greater than 1",
            "helper-1-STATIC-0": "Get the first value retreived from Database",
            "helper-x-STATIC-x": "Get the specific value (based on rank defined) retreived from Database",
            "helper-1-RANDOM": "To retreive Random value, define the Row Limit. Default will return first value",
            "helper-x-RANDOM": "Get a random value from the result set retrieved from Database"
        }
    },
    "getFromDataLib": {
        "value1": {
            "label": {
                "en": "DataLib name",
                "fr": "DataLib name"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-html.png"
        },
        "length": {
            "label": {
                "en": "Length (nb of rows)",
                "fr": "Length (nb of rows)"
            },
            "class": "col-sm-3",
            "picto": "images/property-length.png"
        },
        "cacheExpire": {
            "label": {
                "en": "[opt] cache Expire (s)",
                "fr": "[opt] cache Expire (s)"
            },
            "class": "col-sm-3",
            "picto": "images/property-expire.png"
        },
        "rowLimit": {
            "label": {
                "en": "[opt] Row Limit",
                "fr": "[opt] Row Limit"
            },
            "class": "col-sm-3",
            "picto": "images/property-maxlength.png"
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                },
                {
                    "value": "RANDOMNEW",
                    "label": {
                        "en": "Random & New",
                        "fr": "Aléatoire & Nouvelle"
                    }
                },
                {
                    "value": "NOTINUSE",
                    "label": {
                        "en": "Not In Use",
                        "fr": "Non Utilisée"
                    }
                }
            ]
        },
        "retry": {
            "label": {
                "en": "Number of retry (until non-empty result)",
                "fr": "Number of retry (until non-empty result)"
            },
            "class": "col-sm-3",
            "picto": "images/property-retry.png"
        },
        "period": {
            "label": {
                "en": "Retry period (ms)",
                "fr": "Retry period (ms)"
            },
            "class": "col-sm-3",
            "picto": "images/property-period.png"
        },
        "rank": {
            "label": {
                "en": "Rank",
                "fr": "Rang"
            },
            "class": "col-sm-3",
            "picto": "images/property-rank.png"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Get Data from Cerberus Data Library"}
    },
    "getFromHtml": {
        "value1": {
            "label": {
                "en": "Element path",
                "fr": "Element path"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/xquery",
            "picto": "images/action-html.png",
            "insertAfter": "rank"
        },
        "value3": {
            "label": {
                "en": "Element type",
                "fr": "Type d'élément"
            },
            "class": "col-sm-3",
            "picto": "images/property-value3.png",
            "options": [
                {
                    "value": "value",
                    "label": {
                        "en": "Element Value",
                        "fr": "Valeur de l'élément"
                    }
                },
                {
                    "value": "count",
                    "label": {
                        "en": "Element Count",
                        "fr": "Nombre d'éléments"
                    }
                },
                {
                    "value": "raw",
                    "label": {
                        "en": "Raw Element",
                        "fr": "Elément brut"
                    }
                },
                {
                    "value": "coordinate",
                    "label": {
                        "en": "Coordinate",
                        "fr": "Coordonnées"
                    }
                },
                {
                    "value": "attribute",
                    "label": {
                        "en": "Attribute value",
                        "fr": "Valeur de l'attribut"
                    }
                },
                {
                    "value": "valueList",
                    "label": {
                        "en": "Value List",
                        "fr": "Liste de valeur"
                    }
                },
                {
                    "value": "valueSum",
                    "label": {
                        "en": "Value Sum",
                        "fr": "Somme des valeurs"
                    }
                },
                {
                    "value": "rawList",
                    "label": {
                        "en": "Raw List",
                        "fr": "Liste d'éléments brut"
                    }
                }
            ],
            "insertAfter": "pType"
        },
        "value2": {
            "label": {
                "en": "Attribute Name",
                "fr": "Nom de l'attribut"
            },
            "class": "col-sm-3",
            "picto": "images/property-value3.png",
            "insertAfter": "value3",
            "displayConditions": [{
                    "key": "value3",
                    "values": ["attribute"]
                }]
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                }
            ],
            "insertAfter": "value3",
            "displayConditions": [{
                    "key": "value3",
                    "values": ["value", "raw", "coordinate", "attribute"]
                }]
        },
        "rank": {
            "label": {
                "en": "Rank",
                "fr": "Rang"
            },
            "class": "col-sm-3",
            "picto": "images/property-rank.png",
            "insertAfter": "nature",
            "displayConditions": [{
                    "key": "nature",
                    "values": ["STATIC"]
                }, {
                    "key": "value3",
                    "values": ["value", "raw", "coordinate", "attribute"]
                }]
        },
        "helperInformation": ["value3", "nature", "rank"],
        "helperMessages": {
            "helper-value-STATIC-0": "Get value from first HTML Element found",
            "helper-value-STATIC-x": "Get value from specific HTML Element found",
            "helper-value-RANDOM": "Get value from random HTML Element found",
            "helper-raw-STATIC-0": "Get first HTML Element in raw format",
            "helper-raw-STATIC-x": "Get specific raw Element from HTML",
            "helper-raw-RANDOM": "Get random raw Element from HTML",
            "helper-coordinate-STATIC-0": "Get position of first HTML element",
            "helper-coordinate-STATIC-x": "Get position of specific HTML element",
            "helper-coordinate-RANDOM": "Get position of random HTML element",
            "helper-attribute-STATIC-0": "Get attribute of first HTML element",
            "helper-attribute-STATIC-x": "Get attribute of specific HTML element",
            "helper-attribute-RANDOM": "Get attribute of random HTML element",
            "helper-count": "Get number of HTML element found",
            "helper-valueList": "Get table of value of all HTML element found",
            "helper-valueSum": "Get sum of values of all elements found",
            "helper-rawList": "Get all HTML element found in raw format"
        }
    },
    "getFromJS": {
        "value1": {
            "label": {
                "en": "Javascript command",
                "fr": "Commande Javascript"
            },
            "class": "col-sm-9",
            "editorMode": "ace/mode/javascript",
            "picto": "images/property-javascript.png",
            "insertAfter": "pType"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Return result from the Javascript Command execution"}
    },
    "getFromCookie": {
        "value1": {
            "label": {
                "en": "Cookie name",
                "fr": "Nom du Cookie"
            },
            "class": "col-sm-5",
            "picto": "images/action-html.png",
            "insertAfter": "pType"
        },
        "value2": {
            "label": {
                "en": "Cookie attribute",
                "fr": ""
            },
            "class": "col-sm-4",
            "picto": "images/action-html.png",
            "insertAfter": "value1"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Return the value of cookie specific attribute"
        }
    },
    "getFromXml": {
        "value1": {
            "label": {
                "en": "Xpath",
                "fr": "Xpath"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/xquery",
            "picto": "images/action-html.png",
            "insertAfter": "pType"
        },
        "value2": {
            "label": {
                "en": "[opt] XML or URL to XML file",
                "fr": ""
            },
            "class": "col-sm-12",
            "picto": "images/action-html.png",
            "insertAfter": "value1"
        },
        "value3": {
            "label": {
                "en": "output",
                "fr": "output"
            },
            "class": "col-sm-3",
            "picto": "images/property-value3.png",
            "options": [
                {
                    "value": "value",
                    "label": {
                        "en": "Element Value",
                        "fr": "Valeur de l'élément"
                    }
                },
                {
                    "value": "raw",
                    "label": {
                        "en": "Raw Element",
                        "fr": "Elément brut"
                    }
                }
            ],
            "insertAfter": "value2"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Get value from XML"}
    },
    "getFromJson": {
        "value1": {
            "label": {
                "en": "JSON Path",
                "fr": "JSON Path"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/json",
            "picto": "images/action-html.png",
            "insertAfter": "rank"
        },
        "value2": {
            "label": {
                "en": "[opt] JSON or URL to JSON file",
                "fr": ""
            },
            "class": "col-sm-12",
            "picto": "images/property-json.png",
            "insertAfter": "value1"
        },
        "value3": {
            "label": {
                "en": "output",
                "fr": "output"
            },
            "class": "col-sm-3",
            "picto": "images/property-value3.png",
            "options": [
                {
                    "value": "value",
                    "label": {
                        "en": "Element Value",
                        "fr": "Valeur de l'élément"
                    }
                },
                {
                    "value": "count",
                    "label": {
                        "en": "Element Count",
                        "fr": "Nombre d'éléments"
                    }
                },
                {
                    "value": "valueSum",
                    "label": {
                        "en": "Value Sum",
                        "fr": "Somme des valeurs"
                    }
                },
                {
                    "value": "valueList",
                    "label": {
                        "en": "Value List",
                        "fr": "Liste de valeur"
                    }
                },
                {
                    "value": "rawList",
                    "label": {
                        "en": "Raw List",
                        "fr": "Liste d'éléments brut"
                    }
                }
            ],
            "insertAfter": "pType"
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                }
            ],
            "insertAfter": "value3",
            "displayConditions": [{
                    "key": "value3",
                    "values": ["value"]
                }]
        },
        "rank": {
            "label": {
                "en": "Rank",
                "fr": "Rang"
            },
            "class": "col-sm-3",
            "picto": "images/property-rank.png",
            "insertAfter": "nature",
            "displayConditions": [{
                    "key": "value3",
                    "values": ["value"]
                }]
        },
        "helperInformation": ["value3", "nature", "rank"],
        "helperMessages": {
            "helper-value-STATIC-0": "Get value from first Element found",
            "helper-value-STATIC-x": "Get value from specific Element found",
            "helper-value-RANDOM": "Get value from random Element found",
            "helper-raw-STATIC-0": "Get first Element in raw format",
            "helper-raw-STATIC-x": "Get specific raw Element",
            "helper-raw-RANDOM": "Get random raw Element",
            "helper-count": "Get number of element found",
            "helper-valueList": "Get table of values of all elements found",
            "helper-valueSum": "Get sum of values of all elements found",
            "helper-rawList": "Get all elements found in raw format",
        }
    },
    "getFromNetworkTraffic": {
        "value1": {
            "label": {
                "en": "URL filter",
                "fr": ""
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/json",
            "picto": "images/action-html.png"
        },
        "value2": {
            "label": {
                "en": "JSON Path",
                "fr": "JSON Path"
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        },
        "value3": {
            "label": {
                "en": "Output",
                "fr": "Output"
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png",
            "options": [
                {
                    "value": "value",
                    "label": {
                        "en": "Element Value",
                        "fr": "Valeur de l'élément"
                    }
                },
                {
                    "value": "count",
                    "label": {
                        "en": "Element Count",
                        "fr": "Nombre d'éléments"
                    }
                },
                {
                    "value": "valueList",
                    "label": {
                        "en": "Value List",
                        "fr": "Liste de valeur"
                    }
                }
            ],
        },
        "nature": {
            "label": {
                "en": "Nature",
                "fr": "Nature"
            },
            "class": "col-sm-3",
            "picto": "images/property-random.png",
            "options": [
                {
                    "value": "STATIC",
                    "label": {
                        "en": "Static",
                        "fr": "Statique"
                    }
                },
                {
                    "value": "RANDOM",
                    "label": {
                        "en": "Random",
                        "fr": "Aléatoire"
                    }
                }
            ],
            "insertAfter": "value3",
            "displayConditions": [{
                    "key": "value3",
                    "values": ["value"]
                }]
        },
        "rank": {
            "label": {
                "en": "Rank",
                "fr": "Rang"
            },
            "class": "col-sm-3",
            "picto": "images/property-rank.png",
            "insertAfter": "nature",
            "displayConditions": [{
                    "key": "nature",
                    "values": ["STATIC"]
                }]
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Get value from Network Traffic of current execution"}
    },
    "getFromGroovy": {
        "value1": {
            "label": {
                "en": "Groovy command",
                "fr": "Commande Groovy"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/groovy",
            "picto": "images/action-html.png"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Return result from the Groovy Command execution"}
    },
    "getFromCommand": {
        "value1": {
            "label": {
                "en": "Appium Command (ex : \"mobile:deviceInfo\")",
                "fr": "Commande Appium (ex : \"mobile:deviceInfo\")"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/sh",
            "picto": "images/action-html.png",
            "insertAfter": "pType"
        },
        "value2": {
            "label": {
                "en": "Arguments (ex : {param1: \"val1\", param2: \"val2\"})",
                "fr": "Arguments (ex : {param1: \"val1\", param2: \"val2\"})"
            },
            "class": "col-sm-12",
            "picto": "images/action-html.png",
            "insertAfter": "value1"
        },
        "helperInformation": [],
        "helperMessages": {"helper": "Return result from the Command execution"}
    },
    "getOTP": {
        "value1": {
            "label": {
                "en": "Get an OTP code from a secret key",
                "fr": "Get an OTP code from a secret key"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-html.png"
        },
        "cacheExpire": {
            "label": {
                "en": "[opt] cache Expire (s)",
                "fr": "[opt] cache Expire (s)"
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        },
        "helperInformation": [],
        "helperMessages": {"helper": ""}
    }
};

// CONDITION

var conditionNewUIList = {
    "always": {"value": "always", "label": {"en": "Always", "fr": "Toujours"}},
    "ifPropertyExist": {"value": "ifPropertyExist", "label": {"en": "If Property exists", "fr": "Si la Propriété existe"},
        "field1": {"label": {"en": "Property name  (ex : PROP1)", "fr": "Propriété (ex : PROP1)"}, "class": "crb-autocomplete-property"}},
    "ifPropertyNotExist": {"value": "ifPropertyNotExist", "label": {"en": "If Property not exists", "fr": "Si la Propriété n'existe pas"},
        "field1": {"label": {"en": "Property name  (ex : PROP1)", "fr": "Propriété (ex : PROP1)"}, "class": "crb-autocomplete-property"}},

    "ifElementPresent": {"value": "ifElementPresent", "label": {"en": "If Element Present", "fr": "Si l'élément est présent"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"}},
    "ifElementNotPresent": {"value": "ifElementNotPresent", "label": {"en": "If Element not Present", "fr": "Si l'élément n'est pas présent"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"}},
    "ifElementVisible": {"value": "ifElementVisible", "label": {"en": "If Element Visible", "fr": "Si l'élément est visible'"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"}},
    "ifElementNotVisible": {"value": "ifElementNotVisible", "label": {"en": "If Element not Visible", "fr": "Si l'élément n'est pas visible"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"},
        "field5": {"label": {"en": "true/false : Element must be present? Default : true", "fr": "true/false : L'élément doit-il être present? Par défault : true"}, "class": "crb-autocomplete-variable"}},
    "ifTextInElement": {"value": "ifTextInElement", "label": {"en": "If Text in Element", "fr": "Si l'élément contient le texte"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "class": "crb-autocomplete-variable"}},
    "ifTextNotInElement": {"value": "ifTextNotInElement", "label": {"en": "If Text not in Element", "fr": "Si l'élément ne contient pas le texte"},
        "field1": {"label": {"en": "Element Path", "fr": "Chemin de l'élément"}, "class": "crb-autocomplete-element crb-contextual-button"},
        "field2": {"label": {"en": "Text", "fr": "Texte"}, "class": "crb-autocomplete-variable"}},

    "ifNumericEqual": {"value": "ifNumericEqual", "label": {"en": "If Numeric Equals", "fr": "Si la valeur numérique égale"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},
    "ifNumericDifferent": {"value": "ifNumericDifferent", "label": {"en": "If Numeric Different", "fr": "Si la valeur numérique est différente"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},
    "ifNumericGreater": {"value": "ifNumericGreater", "label": {"en": "If Numeric Greater", "fr": "Si la valeur numérique est supérieure"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},
    "ifNumericGreaterOrEqual": {"value": "ifNumericGreaterOrEqual", "label": {"en": "If Numeric Greater or Equal", "fr": "Si la valeur numérique est supérieure ou égale"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},
    "ifNumericMinor": {"value": "ifNumericMinor", "label": {"en": "If Numeric Minor", "fr": "Si la valeur numérique est inférieure"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},
    "ifNumericMinorOrEqual": {"value": "ifNumericMinorOrEqual", "label": {"en": "If Numeric Minor or Equals", "fr": "Si la valeur numérique est inférieure ou égale"},
        "field1": {"label": {"en": "Integer 1", "fr": "Entier 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "Integer 2", "fr": "Entier 2"}, "class": "crb-autocomplete-variable"}},

    "ifStringEqual": {"value": "ifStringEqual", "label": {"en": "If String Equals", "fr": "Si le texte est egal"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStringDifferent": {"value": "ifStringDifferent", "label": {"en": "If String Different", "fr": "Si le texte est différent"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStringGreater": {"value": "ifStringGreater", "label": {"en": "If String Greater", "fr": "Si le texte est supérieur"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStringMinor": {"value": "ifStringMinor", "label": {"en": "If String Minor", "fr": "Si le texte est inférieur"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStringContains": {"value": "ifStringContains", "label": {"en": "If String Contains", "fr": "Si le texte contient"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStringNotContains": {"value": "ifStringNotContains", "label": {"en": "If String not Contains", "fr": "Si le texte ne contient pas"},
        "field1": {"label": {"en": "String 1", "fr": "Texte 1"}, "class": "crb-autocomplete-variable"},
        "field2": {"label": {"en": "String 2", "fr": "Texte 2"}, "class": "crb-autocomplete-variable"}},
    "ifStepStatusOK": {"value": "ifStepStatusOK", "label": {"en": "If step returned OK", "fr": "Si le step est OK'"},
        "field4": {"label": {"en": "Step", "fr": "Step"}}, "type": "combo", "level": "step"},
    "ifStepStatusNE": {"value": "ifStepStatusNE", "label": {"en": "If defined step not executed", "fr": "Si le step n'a pas été executé'"},
        "field4": {"label": {"en": "Step", "fr": "Step"}}, "type": "combo", "level": "step"},
    "ifActionStatusOK": {"value": "ifActionStatusOK", "label": {"en": "If action returned OK", "fr": "Si l'action est OK'"},
        "field4": {"label": {"en": "Action", "fr": "Action"}}, "type": "combo", "level": "action"},
    "ifActionStatusNE": {"value": "ifActionStatusNE", "label": {"en": "If defined Action not executed", "fr": "Si l'action n'a pas été executée'"},
        "field4": {"label": {"en": "Action", "fr": "Action"}}, "type": "combo", "level": "action"},
    "ifControlStatusOK": {"value": "ifControlStatusOK", "label": {"en": "If control returned OK", "fr": "Si le contrôle est OK'"},
        "field4": {"label": {"en": "Control", "fr": "Control"}}, "type": "combo", "level": "control"},
    "ifControlStatusNE": {"value": "ifControlStatusNE", "label": {"en": "If Control not executed", "fr": "Si le contrôle n'a pas été executé'"},
        "field4": {"label": {"en": "Control", "fr": "Control"}}, "type": "combo", "level": "control"},
    "never": {"value": "never", "label": {"en": "Never", "fr": "Jamais"}},

}

var propDeleted = {
    "getFromHtmlVisible": {
        "value1": {
            "label": {
                "en": "Element path",
                "fr": "Chemin de l'élément"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/xquery",
            "picto": "images/action-html.png"
        }
    },
    "getAttributeFromHtml": {
        "value1": {
            "label": {
                "en": "Element path",
                "fr": "Chemin de l'élément"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/xquery",
            "picto": "images/action-html.png"
        },
        "value2": {
            "label": {
                "en": "Attribute name",
                "fr": ""
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        }
    },
    "getRawFromJson": {
        "value1": {
            "label": {
                "en": "JSON Path",
                "fr": "JSON Path"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/json",
            "picto": "images/action-html.png"
        },
        "value2": {
            "label": {
                "en": "[opt] JSON or URL to JSON file",
                "fr": ""
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        },
        "value3": {
            "label": {
                "en": "output",
                "fr": ""
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        }
    },
    "getRawFromXml": {
        "value1": {
            "label": {
                "en": "Xpath",
                "fr": "Xpath"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/xquery",
            "picto": "images/action-html.png"
        },
        "value2": {
            "label": {
                "en": "[opt] XML or URL to XML file",
                "fr": ""
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        }
    },
    "getElementPosition": {
        "value1": {
            "label": {
                "en": "Get an element pixel position (use id=/xpath=/etc syntax - return \"px;py\")",
                "fr": ""
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-html.png"
        }
    },
    "executeSoapFromLib": {
        "value1": {
            "label": {
                "en": "Service Lib Name",
                "fr": "Nom Service Lib"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-html.png"
        },
        "cacheExpire": {
            "label": {
                "en": "[opt] cache Expire (s)",
                "fr": "[opt] cache Expire (s)"
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        }
    },
    "executeSqlFromLib": {
        "value1": {
            "label": {
                "en": "SQL Lib Name",
                "fr": "Nom SQL Lib"
            },
            "class": "col-sm-12",
            "editorMode": "ace/mode/cerberus",
            "picto": "images/action-html.png"
        },
        "cacheExpire": {
            "label": {
                "en": "[opt] cache Expire (s)",
                "fr": "[opt] cache Expire (s)"
            },
            "class": "col-sm-3",
            "picto": "images/action-html.png"
        }
    }
}
