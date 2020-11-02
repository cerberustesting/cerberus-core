/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

var canUpdate = false;
var allDelete = false;
var loadedPropertiesNumber = -1;
var Tags = [];

var actionUIList = {
    "fr": [
        {"type": "Unknown", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "click", "aval1": "Chemin vers l'élement à cliquer", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "longPress", "aval1": "Chemin vers l'élement à cliquer", "acol1": "col-lg-7", "aval2": "[opt] Valeur (ms) : 8000 par défaut", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonPress", "aval1": "Chemin vers l'élement à cibler", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonRelease", "aval1": "Chemin vers l'élement", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doubleClick", "aval1": "Chemin vers l'élement à double-cliquer", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "rightClick", "aval1": "Chemin vers l'élement à clicker avec le bouton droit", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOver", "aval1": "Chemin vers l'élement", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusToIframe", "aval1": "Chemin vers l'élement de l'iFrame à cibler", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusDefaultIframe", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "switchToWindow", "aval1": "Titre ou url de la fenêtre", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialog", "aval1": "ok ou cancel", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialogKeypress", "aval1": "Touches à appuyer.", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlWithBase", "aval1": "URI à appeler (ex : /index.html)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlLogin", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrl", "aval1": "URL à appeler (ex : http://www.domain.com)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "refreshCurrentPage", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeJS", "aval1": "JavaScript à executer", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCommand", "aval1": "Commande Appium (ex : \"mobile:deepLink\")", "acol1": "col-lg-4", "aval2": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCerberusCommand", "aval1": "Commande (ex : \"grep\")", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openApp", "aval1": "Nom ou chemin de l'application, package pour android", "acol1": "col-lg-6", "aval2": "[Optionnel, obligatoire pour Android] Activity", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "closeApp", "aval1": "Nom ou chemin de l'application", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "dragAndDrop", "aval1": "Chemin de l'élement", "acol1": "col-lg-5", "aval2": "Destination de l'élément", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "select", "aval1": "Chemin vers l'élement", "acol1": "col-lg-5", "aval2": "Chemin vers l'option", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "keypress", "aval1": "[opt] Chemin vers l'élement à cibler", "acol1": "col-lg-7", "aval2": "Touche à appuyer", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "type", "aval1": "Chemin vers l'élement", "acol1": "col-lg-5", "aval2": "Texte à entrer", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "hideKeyboard", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "clearField", "aval1": "Chemin vers l'élement à effacer", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "swipe", "aval1": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "acol1": "col-lg-5", "aval2": "Direction x;y;z;y", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "wait", "aval1": "Valeur (ms) ou élement", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitVanish", "aval1": "Element", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitNetworkTrafficIdle", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "callService", "aval1": "Nom du Service", "acol1": "col-lg-5", "aval2": "Nb Evt à attendre (Kafka)", "acol2": "col-lg-2", "aval3": "Tps d'attente en sec (Kafka)", "acol3": "col-lg-2"},
        {"type": "executeSqlUpdate", "aval1": "Nom de Base de donnée", "acol1": "col-lg-3", "aval2": "Script à executer", "acol2": "col-lg-6", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeSqlStoredProcedure", "aval1": "Nom de Base de donnée", "acol1": "col-lg-3", "aval2": "Procedure Stoquée à executer", "acol2": "col-lg-6", "aval3": null, "acol3": "col-lg-5"},
        {"type": "calculateProperty", "aval1": "Nom d'une Proprieté", "acol1": "col-lg-5", "aval2": "[opt] Nom d'une autre propriété", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setNetworkTrafficContent", "aval1": "URL à filtrer", "acol1": "col-lg-7", "aval2": "Activation du contenu des reponses http (Y/N)", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "indexNetworkTraffic", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setServiceCallContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setConsoleContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setContent", "aval1": "Valeur", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "scrollTo", "aval1": "element (id, xpath, ..., et text=)", "acol1": "col-lg-5", "aval2": "Nombre maximum de scroll vers le bas (8 par defaut)", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "installApp", "aval1": "Chemin vers l'application (ex : /root/toto.apk)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeApp", "aval1": "Package de l'application (ex : com.cerberus.appmobile)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doNothing", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOverAndWait", "aval1": "Action Depreciée", "acol1": "col-lg-5", "aval2": "Action Depreciée", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeDifference", "aval1": "Action Depreciée", "acol1": "col-lg-5", "aval2": "Action Depreciée", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"}
    ], "en": [
        {"type": "Unknown", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "click", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "", "aval3": null, "acol3": ""},
        {"type": "longPress", "aval1": "Element path", "acol1": "col-lg-7", "aval2": "[opt] Duration (ms) : 8000 by default", "acol2": "col-lg-2", "aval3": null, "acol3": ""},
        {"type": "mouseLeftButtonPress", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonRelease", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doubleClick", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "rightClick", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOver", "aval1": "Element path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusToIframe", "aval1": "Element path of the target iFrame", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusDefaultIframe", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "switchToWindow", "aval1": "Window title or url", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialog", "aval1": "ok or cancel", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialogKeypress", "aval1": "keys to press.", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlWithBase", "aval1": "URI to call  (ex : /index.html)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlLogin", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrl", "aval1": "URL to call (ex : http://www.domain.com)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "refreshCurrentPage", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeJS", "aval1": "JavaScript to execute", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCommand", "aval1": "Appium Command (ex : \"mobile:deepLink\")", "acol1": "col-lg-4", "aval2": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCerberusCommand", "aval1": "Command (ex : \"grep\")", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openApp", "aval1": "Application name or path or package for Android", "acol1": "col-lg-6", "aval2": "[Optional, required for Android] Activity", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "closeApp", "aval1": "Application name or path", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "dragAndDrop", "aval1": "Element path", "acol1": "col-lg-5", "aval2": "Destination Element Path", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "select", "aval1": "Element path", "acol1": "col-lg-5", "aval2": "Option value", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "keypress", "aval1": "[opt] Target element path", "acol1": "col-lg-7", "aval2": "Key to press", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "type", "aval1": "Element path", "acol1": "col-lg-5", "aval2": "Text to type", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "hideKeyboard", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "clearField", "aval1": "Element path to Clear", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "swipe", "aval1": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "acol1": "col-lg-5", "aval2": "Direction x;y;z;y", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "wait", "aval1": "Duration(ms) or Element", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitVanish", "aval1": "Element", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitNetworkTrafficIdle", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "callService", "aval1": "Service Name", "acol1": "col-lg-5", "aval2": "Nb Evt (Kafka)", "acol2": "col-lg-2", "aval3": "Evt Wait sec (Kafka)", "acol3": "col-lg-2"},
        {"type": "executeSqlUpdate", "aval1": "Database Name", "acol1": "col-lg-3", "aval2": "Script", "acol2": "col-lg-6", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeSqlStoredProcedure", "aval1": "Database Name", "acol1": "col-lg-3", "aval2": "Stored Procedure", "acol2": "col-lg-6", "aval3": null, "acol3": "col-lg-5"},
        {"type": "calculateProperty", "aval1": "Property Name", "acol1": "col-lg-5", "aval2": "[opt] Name of an other property", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setNetworkTrafficContent", "aval1": "url to filter", "acol1": "col-lg-7", "aval2": "Activate http response content (Y/N)", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "indexNetworkTraffic", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setServiceCallContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setConsoleContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setContent", "aval1": "Value to Set", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "scrollTo", "aval1": "element ('id=ressource-id'. Empty if you want use text)", "acol1": "col-lg-5", "aval2": "text (empty if you want use element)", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "installApp", "aval1": "Application path (ex : /root/toto.apk)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeApp", "aval1": "Application package (ex : com.cerberus.appmobile)", "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doNothing", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOverAndWait", "aval1": "[Deprecated]", "acol1": "col-lg-5", "aval2": "[Deprecated]", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeDifference", "aval1": "[Deprecated]", "acol1": "col-lg-5", "aval2": "[Deprecated]", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"}
    ]
};

var propertyUIList = {
    "fr": [
        {
            "type": "text",
            "value1": "Valeur",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": "[opt] Length",
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": "Nature",
            "retry": null,
            "period": null
        },
        {
            "type": "getFromSql",
            "value1": "Requete SQL",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/sql",
            "value2": null,
            "database": "Database",
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": "Row Limit",
            "nature": "Nature",
            "retry": "Number of retry (until non-empty result)",
            "period": "Retry period (ms)"
        },
        {
            "type": "getFromDataLib",
            "value1": "Nom de la DataLib",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": "Length (nb of rows)",
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": "[opt] Row Limit",
            "nature": "Nature",
            "retry": "Number of retry (until non-empty result)",
            "period": "Retry period (ms)"
        },
        {
            "type": "getFromHtml",
            "value1": "Chemin vers l'élément",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/xquery",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromHtmlVisible",
            "value1": "Chemin vers l'élément",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/xquery",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromJS",
            "value1": "Commande Javascript",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/javascript",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getAttributeFromHtml",
            "value1": "Chemin vers l'élément",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "Attribute name",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromCookie",
            "value1": "Nom du Cookie",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": "Cookie attribute",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromXml",
            "value1": "Xpath",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "[opt] Contenu XML ou URL vers un fichier XML",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getRawFromXml",
            "value1": "Xpath",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "[opt] XML or URL to XML file",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getDifferencesFromXml",
            "value1": "value1",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "value2",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromJson",
            "value1": "JSON Path",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/json",
            "value2": "[opt] Contenu JSON ou URL vers un fichier JSON",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromNetworkTraffic",
            "value1": "filtre d'URL",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/json",
            "value2": "JSON Path",
            "value3": "Activate HTTP response content (Y/N)",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromGroovy",
            "value1": "Groovy command",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/groovy",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromCommand",
            "value1": "Commande Appium (ex : \"mobile:deviceInfo\")",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/shell",
            "value2": "Arguments (ex : {param1: \"val1\", param2: \"val2\"})",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getElementPosition",
            "value1": "Récupérer la position en Pixel d'un élement (use id=/xpath=/etc syntax - return \"px;py\")",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "executeSoapFromLib",
            "value1": "Service lib name",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "executeSqlFromLib",
            "value1": "SQL Lib name",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        }
    ], "en": [
        {
            "type": "text",
            "value1": "Value",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": "[opt] Length",
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": "Nature",
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromSql",
            "value1": "SQL Query",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/sql",
            "value2": null,
            "database": "Database",
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": "Row Limit",
            "nature": "Nature",
            "retry": "Number of retry (until non-empty result)",
            "period": "Retry period (ms)",
            "rank": "Rank"
        },
        {
            "type": "getFromDataLib",
            "value1": "DataLib name",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": "Length (nb of rows)",
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": "[opt] Row Limit",
            "nature": "Nature",
            "retry": "Number of retry (until non-empty result)",
            "period": "Retry period (ms)",
            "rank": "Rank"
        },
        {
            "type": "getFromHtml",
            "value1": "Element path",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/xquery",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromHtmlVisible",
            "value1": "Element path",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/xquery",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromJS",
            "value1": "Javascript command",
            "value1Class": "col-sm-10",
            "value1EditorMode": "ace/mode/javascript",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getAttributeFromHtml",
            "value1": "Element path",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "Attribute name",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromCookie",
            "value1": "Cookie name",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": "Cookie attribute",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromXml",
            "value1": "Xpath",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "[opt] XML or URL to XML file",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getRawFromXml",
            "value1": "Xpath",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "[opt] XML or URL to XML file",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getDifferencesFromXml",
            "value1": "value1",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/xquery",
            "value2": "value2",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromJson",
            "value1": "JSONPath",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/json",
            "value2": "[opt] JSON or URL to JSON file",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromNetworkTraffic",
            "value1": "URL filter",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/json",
            "value2": "JSON Path",
            "value3": "Activate HTTP response content (Y/N)",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "getFromGroovy",
            "value1": "Groovy command",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/groovy",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getFromCommand",
            "value1": "Appium Command (ex : \"mobile:deviceInfo\")",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/shell",
            "value2": "Arguments (ex : {param1: \"val1\", param2: \"val2\"})",
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "getElementPosition",
            "value1": "Get an element pixel position (use id=/xpath=/etc syntax - return \"px;py\")",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null
        },
        {
            "type": "executeSoapFromLib",
            "value1": "Service lib name",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        },
        {
            "type": "executeSqlFromLib",
            "value1": "SQL Lib name",
            "value1Class": "col-sm-8",
            "value1EditorMode": "ace/mode/cerberus",
            "value2": null,
            "database": null,
            "length": null,
            "cacheExpire": "[opt] cache Expire (s)",
            "rowLimit": null,
            "nature": null,
            "retry": null,
            "period": null,
            "rank": "Rank"
        }
    ]
};

var conditionUIList = {
    "fr": [
        {"type": "always", "object": null, "property": null, "condValue3": null},
        {"type": "ifPropertyExist", "object": "Propriété (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifPropertyNotExist", "object": "Propriété (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifElementPresent", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotPresent", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifElementVisible", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotVisible", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifTextInElement", "object": "Element", "property": "Texte", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifTextNotInElement", "object": "Element", "property": "Texte", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifNumericEqual", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericDifferent", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericGreater", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericGreaterOrEqual", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericMinor", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifNumericMinorOrEqual", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifStringEqual", "object": "String1", "property": "String2", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringDifferent", "object": "String1", "property": "String2", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringGreater", "object": "String1 (ex : ZZZ)", "property": "String2 (ex : AAA)", "condValue3": null},
        {"type": "ifStringMinor", "object": "String2 (ex : AAA)", "property": "String2 (ex : ZZZ)", "condValue3": null},
        {"type": "ifStringContains", "object": "String1 (ex : toto)", "property": "String2 (ex : ot)", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringNotContains", "object": "String1 (ex : toto)", "property": "String2 (ex : zot)", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "Never", "object": null, "property": null, "condValue3": null}
    ], "en": [
        {"type": "always", "object": null, "property": null, "condValue3": null},
        {"type": "ifPropertyExist", "object": "Property name  (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifPropertyNotExist", "object": "Property name  (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifElementPresent", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotPresent", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifElementVisible", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotVisible", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifTextInElement", "object": "Element", "property": "Text", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifTextNotInElement", "object": "Element", "property": "Text", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifNumericEqual", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericDifferent", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericGreater", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericGreaterOrEqual", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericMinor", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifNumericMinorOrEqual", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifStringEqual", "object": "String1", "property": "String2", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringDifferent", "object": "String1", "property": "String2", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringGreater", "object": "String1 (ex : ZZZ)", "property": "String2 (ex : AAA)", "condValue3": null},
        {"type": "ifStringMinor", "object": "String1 (ex : AAA)", "property": "String (ex : ZZZ)", "condValue3": null},
        {"type": "ifStringContains", "object": "String1 (ex : toto)", "property": "String2 (ex : ot)", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringNotContains", "object": "String1 (ex : toto)", "property": "String2 (ex : zot)", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "never", "object": null, "property": null, "condValue3": null}
    ]
};

var controlUIList = {
    "fr": [
        {
            "type": "Unknown",
            "value1": null,
            "value2": null,
            "value3": null,
            "fatal": null
        },
        {
            "type": "verifyStringEqual",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringDifferent",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringGreater",
            "value1": "String1 (ex: ZZZ)", "acol1": "col-lg-3",
            "value2": "String2 (ex: AAA)", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringMinor",
            "value1": "String1 (ex: AAA)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ZZZ)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ot)", "acol2": "col-lg-2",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringNotContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : zot)", "acol2": "col-lg-2",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyNumericEquals",
            "value1": "Integer1", "acol1": "col-lg-3",
            "value2": "Integer2", "acol2": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericDifferent",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreater",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreaterOrEqual",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinor",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinorOrEqual",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementEquals",
            "value1": "XPath of the element", "acol1": "col-lg-3",
            "value2": "Expected element", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementDifferent",
            "value1": "XPath of the element", "acol1": "col-lg-3",
            "value2": "Not Expected element", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementInElement",
            "value1": "Master Element", "acol1": "col-lg-4",
            "value2": "Sub Element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {"type": "verifyElementTextEqual", "value2": "Texte", "acol2": "col-lg-3", "value1": "Chemin vers l'Element", "acol1": "col-lg-3", "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyElementTextDifferent", "value2": "Texte", "acol2": "col-lg-3", "value1": "Chemin vers l'Element", "acol1": "col-lg-3", "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyElementTextMatchRegex", "value2": "Regex", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericEqual", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericDifferent", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericGreater", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericGreaterOrEqual", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericMinor", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericMinorOrEqual", "value2": "Valeur numerique", "acol2": "col-lg-4", "value1": "Chemin vers l'Element", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "verifyTextInPage", "value2": null, "value1": "Regex", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyTextNotInPage", "value2": null, "value1": "Regex", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyTitle", "value2": null, "value1": "Title", "acol1": "col-lg-6", "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyUrl", "value2": null, "value1": "URL", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyTextInDialog", "value2": null, "value1": "Text", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyXmlTreeStructure", "value2": "Tree", "acol2": "col-lg-4", "value1": "XPath", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "takeScreenshot", "value2": null, "value1": "[opt] Cadrer en pixel (gauche,droite,haut,bas)", "acol1": "col-lg-8", "value3": null, "fatal": null},
        {"type": "getPageSource", "value2": null, "value1": null, "value3": null, "fatal": null}
    ], "en": [
        {
            "type": "Unknown",
            "value1": null,
            "value2": null,
            "value3": null,
            "fatal": null
        },
        {
            "type": "verifyStringEqual",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringDifferent",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringGreater",
            "value1": "String1 (ex: ZZZ)", "acol1": "col-lg-4",
            "value2": "String2 (ex : AAA)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringMinor",
            "value1": "String1 (ex: AAA)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ZZZ)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ot)", "acol2": "col-lg-2",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringNotContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : zot)", "acol2": "col-lg-2",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyNumericEquals",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericDifferent",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreater",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreaterOrEqual",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinor",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinorOrEqual",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementEquals",
            "value1": "XPath of the element", "acol1": "col-lg-4",
            "value2": "Expected element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementDifferent",
            "value1": "XPath of the element", "acol1": "col-lg-4",
            "value2": "Not Expected element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementInElement",
            "value1": "Master Element", "acol1": "col-lg-4",
            "value2": "Sub Element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {"type": "verifyElementTextEqual", "value2": "Text", "acol2": "col-lg-3", "value1": "Element Path", "acol1": "col-lg-3", "value3": "[opt] Case Sensitive (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyElementTextDifferent", "value2": "Text", "acol2": "col-lg-3", "value1": "Element Path", "acol1": "col-lg-3", "value3": "[opt] Case Sensitive (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyElementTextMatchRegex", "value2": "Regex", "acol2": "col-lg-3", "value1": "Element Path", "acol1": "col-lg-5", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericEqual", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericDifferent", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericGreater", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericGreaterOrEqual", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericMinor", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyElementNumericMinorOrEqual", "value2": "Numeric value", "acol2": "col-lg-2", "value1": "Element Path", "acol1": "col-lg-6", "value3": null, "fatal": ""},
        {"type": "verifyTextInPage", "value2": null, "value1": "Regex", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyTextNotInPage", "value2": null, "value1": "Regex", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyTitle", "value2": null, "value1": "Title", "acol1": "col-lg-6", "value3": "[opt] Case Sensitive (Y/N)", "acol3": "col-lg-2", "fatal": ""},
        {"type": "verifyUrl", "value2": null, "acol2": null, "value1": "URL", "acol1": "col-lg-8", "value3": null, "acol2": null, "fatal": ""},
        {"type": "verifyTextInDialog", "value2": null, "value1": "Text", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {"type": "verifyXmlTreeStructure", "value2": "Tree", "acol2": "col-lg-4", "value1": "XPath", "acol1": "col-lg-4", "value3": null, "fatal": ""},
        {"type": "takeScreenshot", "value2": null, "value1": "[opt] Crop values in pixels (left,right,top,bottom)", "acol1": "col-lg-8", "value3": null, "fatal": null},
        {"type": "getPageSource", "value2": null, "value1": null, "value3": null, "fatal": null}
    ]
};

$.when($.getScript("js/global/global.js"), $.getScript("js/global/autocomplete.js")).then(function () {
    $(document).ready(function () {
        loadedPropertiesNumber = -1;
        initModalDataLib();
        $("#nav-property").on('mouseenter', 'a', function (ev) {
            try {
                $(this).find("button").show();
            } catch (e) {
            }

        }).on('mouseleave', 'a', function (ev) {
            try {
                $(this).find("button").hide();

            } catch (e) {
            }
        });
        $('#propName').trigger("change");

        $('#propName').change(function () {
            openModalAppServiceFromHere();
        });
        $('#createApplicationObjectButton').click(function () {
            openModalApplicationObject(undefined, undefined, "ADD", "testCaseScript");

        });
        $(window).bind('beforeunload', function () {
            if (getModif()) {
                return true; // Display alert Message that a modification has
                // been done
            }
        });

        var doc = new Doc();
        var steps = [];

        // Load invariant list into local storage.
        getSelectInvariant("SRVTYPE", false, true);
        getSelectInvariant("SRVMETHOD", false, true);
        getSelectInvariant("ACTION", false, true);
        getSelectInvariant("CONTROL", false, true);
        getSelectInvariant("CTRLFATAL", false, true);
        getSelectInvariant("PROPERTYTYPE", false, true);
        getSelectInvariant("PROPERTYDATABASE", false, true);
        getSelectInvariant("PROPERTYNATURE", false, true);
        getSelectInvariant("ACTIONFORCEEXESTATUS", false, true);
        getSelectInvariant("STEPLOOP", false, true);
        getSelectInvariant("STEPCONDITIONOPERATOR", false, true);
        bindToggleCollapse();
        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var step = GetURLParameter("step");
        var property = GetURLParameter("property");
        var tabactive = GetURLParameter("tabactive");
        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);
        displayPageLabel(doc);
        $("#addStepModal [name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));


        fillTestAndTestCaseSelect(".testTestCase #test", "#testCaseSelect", test, testcase, false)

        $("#testCaseSelect").bind("change", function (event) {
            window.location.href = "./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI($(this).val());
        });
        $(".testTestCase #test").bind("change", function (event) {
            window.location.href = "./TestCaseScript.jsp?test=" + encodeURI($(this).val());
        });

        if (test !== null && testcase !== null) {
            // Edit TestCase open the TestCase Modal
            $("#editTcInfo").click(function () {
                openModalTestCase(test, testcase, "EDIT");
                $('#editTestCaseModal').on("hidden.bs.modal", function (e) {
                    $('#editTestCaseModal').unbind("hidden.bs.modal");
                    var t = $('#editTestCaseModal').find("#test option:selected");
                    var tc = $('#editTestCaseModal').find("#testCase");
                    if (!((t.val() === test) && (tc.val() === testcase))) {
                        // Key was modified.
                        if ($('#editTestCaseModal').data("Saved")) {
                            // Modal confirm that change was OK.
                            $('#editTestCaseModal').data("Saved", undefined);
                            window.location = "./TestCaseScript.jsp?test=" + encodeURI(t.val()) + "&testcase=" + encodeURI(tc.val());
                        }

                    }
                });
            });

            $("#TestCaseButton").show();
            $("#tcBody").show();

            var stepsObject;
            var testcaseObject;
            $.ajax({
                url: "ReadTestCase",
                data: {test: test, testCase: testcase, withSteps: true, system: getSys()},
                dataType: "json",
                success: function (data) {

                    // manage error
                    if (data.messageType !== undefined && data.messageType === "KO") {
                        showUnexpectedError(null, "ERROR", data.message);
                        return;
                    }

                    canUpdate = data.hasPermissionsUpdate;
                    loadLibraryStep(undefined, data.contentTable[0].system);

                    testcaseObject = data.contentTable[0];
                    loadTestCaseInfo(testcaseObject);
                    stepsObject = testcaseObject.steps;
                    sortData(stepsObject);
                    testcaseObject.properties.inheritedProperties.sort(function (a, b) {
                        return compareStrings(a.property, b.property);
                    });
                    createSteps(stepsObject, steps, step, data.hasPermissionsUpdate, data.hasPermissionsStepLibrary);
                    drawInheritedProperty(data.contentTable[0].properties.inheritedProperties);

                    var configs = {
                        'system': true,
                        'object': testcaseObject.application,
                        'property': data,
                        'identifier': true
                    };
                    var context = data;
                    initTags(configs, context).then(function (tags) {
                        autocompleteAllFields(configs, context, tags);
                    });
                    loadPropertiesAndDraw(test, testcase, testcaseObject, property, data.hasPermissionsUpdate);

                    // Manage authorities when data is fully loadable.
                    $("#deleteTestCase").attr("disabled", !data.hasPermissionsDelete);
                    $("#addStep").attr("disabled", !data.hasPermissionsUpdate);
                    $("#deleteStep").attr("disabled", !data.hasPermissionsUpdate);
                    $("#saveScript").attr("disabled", !data.hasPermissionsUpdate);
                    $("#addActionBottom").attr("disabled", !data.hasPermissionsUpdate);
                    $("#addProperty").attr("disabled", !data.hasPermissionsUpdate);
                    // $("#saveProperty1").attr("disabled",
                    // !data.hasPermissionsUpdate);
                    // $("#saveProperty2").attr("disabled",
                    // !data.hasPermissionsUpdate);

                    // Building full list of country from testcase.
                    var myCountry = [];
                    $.each(testcaseObject.countries, function (index) {
                        myCountry.push(testcaseObject.countries[index].value);
                    });

                    // Button Add Property insert a new Property
                    $("#addProperty").click(function () {

                        if (myCountry.length <= 0) {
                            showMessageMainPage("danger", doc.getDocLabel("page_testcasescript", "warning_nocountry"), false);

                        } else {


                            // Store the current saveScript button status and
                            // disable it
                            var saveScriptOldStatus = $("#saveScript").attr("disabled");
                            $("#saveScript").attr("disabled", true);
                            // clone the country list
                            var newCountryList = myCountry.slice(0);

                            let propIndex = $("#propTable #masterProp").length;
                            var newProperty = {
                                property: "PROP-" + propIndex,
                                description: "",
                                country: newCountryList,
                                type: "text",
                                database: "",
                                value1: "",
                                value2: "",
                                length: 0,
                                rowLimit: 0,
                                cacheExpire: 0,
                                nature: "STATIC",
                                retryNb: "",
                                retryPeriod: "",
                                rank: 1,
                                toDelete: false
                            };

                            var prop = drawProperty(newProperty, testcaseObject, true, document.getElementsByClassName("property").length);
                            setPlaceholderProperty(prop[0], prop[1]);

                            $(prop[0]).find("#propName").focus();
                            // autocompleteAllFields();
                            setModif(true);

                            // Restore the saveScript button status
                            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
                        }

                    });

                    $('[data-toggle="tooltip"]').tooltip();

                    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                        initModification();
                    });
                },
                error: showUnexpectedError
            });

//            $("#propertiesModal [name='buttonSave']").click(editPropertiesModalSaveHandler);

            $("#addStep").click({steps: steps}, function (event) {
                // Store the current saveScript button status and disable it
                var saveScriptOldStatus = $("#saveScript").attr("disabled");
                $("#saveScript").attr("disabled", true);

                // Really do add step action
                addStep(event);

                // Restore the saveScript button status
                $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
            });
            $('#addStepModal').on('hidden.bs.modal', function () {
                $("#importDetail").find("[name='importInfo']").removeData("stepInfo");
                $("[name='importInfo']").empty();
                $("#addStepModal #description").val("");
                $("#useStep").prop("checked", false);
                $("#importDetail").hide();
                $("#importDetail div.row").remove();
                $(".sub-sub-item.selected").each(function (idx, element) {
                    $(element).removeClass("selected");
                    $(element).find("[name='idx']").remove();
                });
                importInfoIdx = 0;
            });

            $("#deleteStep").click(function () {

                var step = $("#steps .active").data("item");

                if (step.isStepInUseByOtherTestCase) {
                    showStepUsesLibraryInConfirmationModal(step);
                } else {
                    setModif(true);
                    step.setDelete();
                }
            });

            $("#addAction").click(function () {
                addActionAndFocus()
            });

            // CONTEXT SAVE MENU
            $("#saveScript").click(saveScript);
            $("#saveScriptAs").click(function () {
                openModalTestCase(test, testcase, "DUPLICATE");
                $('#editTestCaseModal').on("hidden.bs.modal", function (e) {
                    $('#editTestCaseModal').unbind("hidden.bs.modal");
                    var t = $('#editTestCaseModal').find("#test option:selected");
                    var tc = $('#editTestCaseModal').find("#testCase");
                    if ($('#editTestCaseModal').data("Saved")) {
                        $('#editTestCaseModal').data("Saved", undefined);
                        window.location = "./TestCaseScript.jsp?test=" + t.val() + "&testcase=" + tc.val();
                    }
                });
            });
            $("#deleteTestCase").click(function () {
                removeTestCaseClick(test, testcase);
            });

            // CONTEXT GOTO & RUN MENU
            $("#seeLogs").parent().attr("href", "./LogEvent.jsp?Test=" + encodeURI(test) + "&TestCase=" + encodeURI(testcase));
            $("#seeTest").parent().attr("href", "./TestCaseList.jsp?test=" + encodeURI(test));
            $("#runTestCase").parent().attr("href", "./RunTests.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase));

            $.ajax({
                url: "ReadTestCaseExecution",
                data: {test: test, testCase: testcase, system: getSys()},
                dataType: "json",
                success: function (data) {
                    if (!jQuery.isEmptyObject(data.contentTable)) {
                        $("#seeLastExecUniq").parent().attr("href", "./TestCaseExecution.jsp?executionId=" + encodeURI(data.contentTable.id));
                        $("#seeLastExec").parent().attr("href", "./TestCaseExecutionList.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase));
                        $("#rerunTestCase").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end)
                        $("#rerunTestCase").parent().attr("href", "./RunTests.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&country=" + encodeURI(data.contentTable.country) + "&environment=" + encodeURI(data.contentTable.env));
                        $("#rerunFromQueue").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end)
                        if (data.contentTable.queueId > 0) {
                            $("#rerunFromQueue").click(function () {
                                openModalTestCaseExecutionQueue(data.contentTable.queueId, "DUPLICATE");
                            });
                        } else {
                            $("#rerunFromQueue").attr("disabled", true);
                        }
                        $("#rerunFromQueueandSee").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end)
                        if (data.contentTable.queueId > 0) {
                            $("#rerunFromQueueandSee").click(function () {
                                triggerTestCaseExecutionQueueandSeeFromTC(data.contentTable.queueId);
                            });
                        } else {
                            $("#rerunFromQueueandSee").attr("disabled", true);
                        }
                    } else {
                        $("#seeLastExecUniq").attr("disabled", true);
                        $("#seeLastExec").attr("disabled", true);
                        $("#rerunTestCase").attr("disabled", true);
                        $("#rerunFromQueue").attr("disabled", true);
                        $("#rerunFromQueueandSee").attr("disabled", true);
                    }
                },
                error: showUnexpectedError
            });
            var height = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $(".page-title-line").outerHeight(true) - 10;

            $("#divPanelDefault").affix({offset: {top: height}});

            var wrap = $(window);

            wrap.on("scroll", function (e) {
                $(".affix").width($("#page-layout").width() - 3);
                // $(".affix-top").width($("#divPanelDefault").width());
            });

            if (tabactive !== null) {
                $("a[name='" + tabactive + "']").click();
            }
        }
        // close all Navbar menu
        closeEveryNavbarMenu();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'
        }
        );

        // open Run navbar Menu
        openNavbarMenu("navMenuTest");
    });
});



function displayPageLabel(doc) {
    $("h1.page-title-line").html(doc.getDocLabel("page_testcasescript", "testcasescript_title"));
    $("#pageTitle").html(doc.getDocLabel("page_testcasescript", "testcasescript_title"));
    $("#nav-execution #list-wrapper #stepsWrapper h3").html(doc.getDocLabel("page_testcasescript", "steps_title"));
    $("#nav-execution #list-wrapper #tcButton h3").html(doc.getDocLabel("page_global", "columnAction"));
    $("#nav-execution #list-wrapper #deleteButton h3").html(doc.getDocLabel("page_global", "columnAction") + " " + doc.getDocLabel("page_header", "menuTestCase"));

    // CONTEXT MENU
    $("#btnGroupDrop1").html(doc.getDocLabel("page_testcasescript", "goto") + " <span class='caret'></span>");
    $("#seeLastExecUniq").html("<span class='glyphicon glyphicon-saved'></span> " + doc.getDocLabel("page_testcasescript", "see_lastexecuniq"));
    $("#seeLastExec").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_lastexec"));
    $("#seeTest").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_test"));
    $("#seeLogs").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_logs"));
    $("#btnGroupDrop2").html(doc.getDocLabel("page_testcasescript", "run") + " <span class='caret'></span>");
    $("#runTestCase").html("<span class='glyphicon glyphicon-play'></span> " + doc.getDocLabel("page_testcasescript", "run_testcase"));
    $("#rerunTestCase").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerun_testcase"));
    $("#rerunFromQueue").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerunqueue_testcase"));
    $("#rerunFromQueueandSee").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerunqueueandsee_testcase"));
    $("#editTcInfo").html("<span class='glyphicon glyphicon-pencil'></span> " + doc.getDocLabel("page_testcasescript", "edit_testcase"));
    $("#saveScript").html("<span class='glyphicon glyphicon-floppy-disk'></span> " + doc.getDocLabel("page_testcasescript", "save_script"));
    $("#saveScriptAs").html("<span class='glyphicon glyphicon-floppy-disk'></span> " + doc.getDocLabel("page_testcasescript", "saveas_script"));
    $("#deleteTestCase").html("<span class='glyphicon glyphicon-trash'></span> " + doc.getDocLabel("page_testcasescript", "delete"));

    $("#addStep").html(doc.getDocLabel("page_testcasescript", "add_step"));
    $("#addActionBottomBtn button").html(doc.getDocLabel("page_testcasescript", "add_action"));
    $("#stepConditionOperator").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_operation"));
    $("#stepConditionVal1").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_value1"));

    // TestCase
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocOnline("testcase", "TestCase"));
    $("[name='lastModifierField']").html(doc.getDocOnline("testcase", "LastModifier"));
    $("[name='originField']").html(doc.getDocOnline("testcase", "Origine"));
    $("[name='refOriginField']").html(doc.getDocOnline("testcase", "RefOrigine"));
    $("[name='projectField']").html(doc.getDocOnline("project", "idproject"));
    $("[name='ticketField']").html(doc.getDocOnline("testcase", "ticket"));
    $("[name='functionField']").html(doc.getDocOnline("testcase", "Function"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='statusField']").html(doc.getDocOnline("testcase", "Status"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='isActiveQAField']").html(doc.getDocOnline("testcase", "IsActiveQA"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActivePRODField']").html(doc.getDocOnline("testcase", "IsActivePROD"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='detailedDescriptionField']").html(doc.getDocOnline("testcase", "detailedDescription"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
    $("[name='creatorField']").html(doc.getDocOnline("testcase", "Creator"));
    $("[name='implementerField']").html(doc.getDocOnline("testcase", "Implementer"));
    $("[name='groupField']").html(doc.getDocOnline("invariant", "Type"));
    $("[name='priorityField']").html(doc.getDocOnline("invariant", "PRIORITY"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='tcDateCreaField']").html(doc.getDocOnline("testcase", "TCDateCrea"));
    $("[name='isActiveField']").html(doc.getDocOnline("testcase", "IsActive"));
    $("[name='fromMajorField']").html(doc.getDocOnline("testcase", "FromMajor"));
    $("[name='fromMinorField']").html(doc.getDocOnline("testcase", "FromMinor"));
    $("[name='toMajorField']").html(doc.getDocOnline("testcase", "ToMajor"));
    $("[name='toMinorField']").html(doc.getDocOnline("testcase", "ToMinor"));
    $("[name='targetMajorField']").html(doc.getDocOnline("testcase", "TargetMajor"));
    $("[name='targetMinorField']").html(doc.getDocOnline("testcase", "TargetMinor"));
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    // PREPARE MASS ACTION
    $("[name='massActionTestCaseField']").html(doc.getDocOnline("page_testcaselist", "massAction"));
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='testCaseParameterField']").html(doc.getDocLabel("page_testcaselist", "testCaseParameter"));
    $("[name='activationCriteriaField']").html(doc.getDocLabel("page_testcaselist", "activationCriteria"));
    // Traceability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));
}

function triggerTestCaseExecutionQueueandSeeFromTC(queueId) {
    $.ajax({
        url: "CreateTestCaseExecutionQueue",
        async: true,
        method: "POST",
        data: {
            id: queueId,
            actionState: "toQUEUED",
            actionSave: "save"
        },
        success: function (data) {
            if (getAlertType(data.messageType) === "success") {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
                var url = "./TestCaseExecution.jsp?executionQueueId=" + encodeURI(data.testCaseExecutionQueueList[0].id);
                console.info("redir : " + url);
                window.location.replace(url);
            } else {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
            }
        },
        error: showUnexpectedError
    });
}

function addAction(action) {
    setModif(true);
    var step = $("#steps li.active").data("item");
    var act = new Action(null, step, true);
    step.setAction(act, action);
    setAllSort();
    return act;
}

function addActionAndFocus(action) {
    $.when(addAction(action)).then(function (action) {
        listenEnterKeypressWhenFocusingOnDescription();
        $($(action.html[0]).find(".description")[0]).focus();
    });
}

function getTestCase(test, testcase, step) {
    window.location.href = "./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&step=" + encodeURI(step);
}

function setAllSort() {
    var steps = $("#steps li");
    var stepArr = [];

    // Construct the step/action/control list:
    // Iterate over steps
    for (var i = 0; i < steps.length; i++) {
        var step = $(steps[i]).data("item");
        var actionArr = [];

        if (!step.toDelete) {
            // Set the step's sort
            step.setSort(i + 1);

            // Get step's actions
            var actions = step.stepActionContainer.children(".action-group").children(".action");
            // Iterate over actions
            for (var j = 0; j < actions.length; j++) {
                var action = $(actions[j]).data("item");
                var controlArr = [];

                if (!action.toDelete) {
                    // Set the action's sort
                    action.setSort(j + 1);

                    // Set the action's step
                    action.setStep(i + 1);

                    // Get action's controls
                    var controls = action.html.children(".control");

                    // Iterate over controls
                    for (var k = 0; k < controls.length; k++) {
                        var control = $(controls[k]).data("item");

                        if (!control.toDelete) {
                            // Set the control's sort
                            control.setParentActionSort(j + 1);
                            control.setSort(k + 1);
                            control.setStep(i + 1);
                            control.setControlSequence(k + i)

                            // Then push control into result array
                            controlArr.push(control.getJsonData());
                        }
                    }
                }
                var actionJson = action.getJsonData();
                actionJson.controlArr = controlArr;
                actionArr.push(actionJson);
            }
            var stepJson = step.getJsonData();
            stepJson.actionArr = actionArr;
            stepArr.push(stepJson);
        }
    }

    return stepArr;
}


function saveScript(property) {

    if (!isPropertyListDisplayed()) {
        return;
    }

    // Disable the save button to avoid double click.
    $("#saveScript").attr("disabled", true);

    var stepArr = setAllSort();
    var doc = new Doc();

    var properties = $("#propTable #masterProp");
    var propArr = [];
    var propertyWithoutCountry = false;
    var propertyWithoutName = false;
    for (var i = 0; i < properties.length; i++) {
        if (($(properties[i]).data("property").country.length <= 0) && ($(properties[i]).data("property").toDelete === false)) {
            propertyWithoutCountry = true;
        }
        if (($(properties[i]).data("property").property === "") && ($(properties[i]).data("property").toDelete === false)) {
            propertyWithoutName = true;
        }
        if (!$.isNumeric($(properties[i]).data("property").rank)) {
            $(properties[i]).data("property").rank = 1;
        }
        propArr.push($(properties[i]).data("property"));
    }


    if (property !== undefined) {
        for (i in propArr) {
            if (propArr[i].property === property) {
                propArr[i].toDelete = true
            }
        }
    }

    var saveProp = function () {
        showLoaderInModal('#propertiesModal');
        $.ajax({
            url: "UpdateTestCaseWithDependencies",
            async: true,
            method: "POST",
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify({
                informationInitialTest: GetURLParameter("test"),
                informationInitialTestCase: GetURLParameter("testcase"),
                informationTest: GetURLParameter("test"),
                informationTestCase: GetURLParameter("testcase"),
                stepArray: stepArr,
                propArr: propArr
            }),
            success: function () {

                var stepHtml = $("#steps li.active");
                var stepData = stepHtml.data("item");

                var tabActive = $("#tabsScriptEdit li.active a").attr("name");

                var parser = document.createElement('a');
                parser.href = window.location.href;

                var tutorielId = GetURLParameter("tutorielId", null);
                var startStep = GetURLParameter("startStep", null);

                var tutorialParameters = "";
                if (tutorielId != null && startStep != null) {
                    tutorialParameters = "&tutorielId=" + tutorielId + "&startStep=" + startStep;
                }

                var url_sort = "";
                if (!(isEmpty(stepData))) {
                    url_sort = "&step=" + encodeURI(stepData.sort);
                }
                var new_uri = parser.pathname + "?test=" + encodeURI(GetURLParameter("test")) + "&testcase=" + encodeURI(GetURLParameter("testcase")) + url_sort + tutorialParameters + "&tabactive=" + tabActive;

                setModif(false);

                window.location.href = new_uri;
            },
            error: showUnexpectedError
        });
    };


    if (propertyWithoutCountry) {
        showModalConfirmation(function () {
            $('#confirmationModal').modal('hide');
            saveProp();
        }, function () {
            $("#saveScript").attr("disabled", false);
        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_no_country"), "", "", "", "");
    } else if (propertyWithoutName) {
        showModalConfirmation(function () {
            $('#confirmationModal').modal('hide');
            saveProp();
        }, function () {
            $("#saveScript").attr("disabled", false);
        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_one_empty_prop"), "", "", "", "");
    } else {
        saveProp();
    }

}

function isPropertyListDisplayed() {

    //var displayedPropertiesNumber = document.getElementById('propList').getElementsByTagName('li').length;
    if (loadedPropertiesNumber === -1) {
        return false;
    }

    return true;

}

function deleteFnct(property) {
    var linkToProperty = null;

    // go though every link and look for the right one
    $("#propListWrapper li a").each(function () {
        if ($(this).text() === property)
            linkToProperty = $(this).parent();
    });
    if (linkToProperty !== null) {
        if (property.toDelete) {
            linkToProperty.addClass("list-group-item-danger");
        } else {
            linkToProperty.removeClass("list-group-item-danger");
        }
    }
}

function prevent(e) {
    e.preventDefault();
}

function drawPropertyList(property, index, isSecondary) {
    var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");

    $(htmlElement).append($("<a ></a>").attr("href", "#propertyLine" + property).text(property));

    var deleteBtn = $("<button style='padding:0px;float:right;display:none' class='btn btn-danger add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    deleteBtn.attr("disabled", !canUpdate);
    $(htmlElement).find("a").append(deleteBtn);

    // add the color for secondary properties
    // TO DO: create a new CSS class
    if (isSecondary === true) {
        htmlElement.css("background-color", "#dfe4e9");
        htmlElement.children("a").append("<span class='secondaryproptext'>secondary</span>");
        htmlElement.find("span.secondaryproptext").css("padding", "0px 10px 0px 10px");
        htmlElement.find("span.secondaryproptext").css("float", "right");
        htmlElement.find("span.secondaryproptext").css("display", "block");
        htmlElement.find("span.secondaryproptext").css("color", "#636e72");
        // <span style="padding: 0px 10px 0px 10px;float: right;display:
        // block;color: #636e72;">secondary</span>
    }

    deleteBtn.click(function (ev) {

        if (allDelete !== true) {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    if (!$(this).hasClass("list-group-item-danger")) {
                        $(this).find("button.add-btn.btn-danger").trigger("click");
                    }
                }
            })
        } else {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    $(this).find("button.add-btn.btn-danger").trigger("click");
                }
            })
        }
    })

    $("#propList").append(htmlElement);
}

function drawProperty(property, testcaseinfo, canUpdate, index) {
    var doc = new Doc();
    var selectType = getSelectInvariant("PROPERTYTYPE", false, true);
    selectType.attr("name", "propertyType");
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true);
    var deleteBtn = $("<button class='btn btn-danger add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var moreBtn = $("<button class='btn btn-default add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));

    var propertyInput = $("<input onkeypress='return tec_keyispressed(event);' id='propName' style='width: 100%; font-size: 16px; font-weight: 600;' name='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "'>").addClass("form-control input-sm").val(property.property);
    var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "'>").addClass("form-control input-sm").val(property.description);
    var valueInput = $("<pre name='propertyValue' id='propertyValue" + index + "' style='min-height:150px' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></pre>").addClass("form-control input-sm").val(property.value1);
    var value2Input = $("<textarea name='propertyValue2' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></textarea>").addClass("form-control input-sm").val(property.value2);
    var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "'>").addClass("form-control input-sm").val(property.length);
    var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "'>").addClass("form-control input-sm").val(property.rowLimit);
    var cacheExpireInput = $("<input type='number' placeholder=''>").addClass("form-control input-sm").val(property.cacheExpire);
    var retryNbInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryNb") + "'>").addClass("form-control input-sm").val(property.retryNb);
    var retryPeriodInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryPeriod") + "'>").addClass("form-control input-sm").val(property.retryPeriod);
    var rankInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "Rank") + "'>").addClass("form-control input-sm").val(property.rank);
    var table = $("#propTable");

    selectType.attr("disabled", !canUpdate);
    selectDB.attr("disabled", !canUpdate);
    selectNature.attr("disabled", !canUpdate);
    deleteBtn.attr("disabled", !canUpdate);
    propertyInput.prop("readonly", !canUpdate);
    descriptionInput.prop("readonly", !canUpdate);
    valueInput.prop("readonly", !canUpdate);
    value2Input.prop("readonly", !canUpdate);
    lengthInput.prop("readonly", !canUpdate);
    rowLimitInput.prop("readonly", !canUpdate);
    retryNbInput.prop("readonly", !canUpdate);
    retryPeriodInput.prop("readonly", !canUpdate);
    cacheExpireInput.prop("readonly", !canUpdate);
    rankInput.prop("readonly", !canUpdate);

    // if the property is secondary
    //var isSecondary = property.description.indexOf("[secondary]") >= 0;
    var isSecondary = property.rank === 2;
    if (isSecondary) {
        var content = $("<div class='row secondaryProperty list-group-item list-group-item-secondary'></div>");
    } else {
        var content = $("<div class='row property list-group-item'></div>");
    }
    var props = $("<div class='col-sm-11' name='propertyLine' id='propertyLine" + property.property + "'></div>");
    var right = $("<div class='col-sm-1 propertyButtons'></div>");

    var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
    var row2 = $("<div class='row' name='masterProp'></div>");
    var row3 = $("<div class='row' style='display:none;'></div>");
    var row4 = $("<div class='row' name='masterProp'></div>");
    var row5 = $("<div class='row'></div>");
    var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);
    var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);
    var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(testcaseinfo.countries, property.country, !canUpdate));
    var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.val(property.type));
    var db = $("<div class='col-sm-2 form-group' name='fieldDatabase'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
    var value = $("<div class='col-sm-8 form-group' name='fieldValue1'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
    var value2 = $("<div class='col-sm-6 form-group' name='fieldValue2'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
    var length = $("<div class='col-sm-2 form-group' name='fieldLength'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
    var rowLimit = $("<div class='col-sm-2 form-group' name='fieldRowLimit'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
    var cacheExpire = $("<div class='col-sm-2 form-group' name='fieldExpire'></div>").append($("<label></label>").text("cacheExpire")).append(cacheExpireInput);

    var nature = $("<div class='col-sm-2 form-group' name='fieldNature'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.val(property.nature));
    var retryNb = $("<div class='col-sm-2 form-group' name='fieldRetryNb'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryNb"))).append(retryNbInput);
    var retryPeriod = $("<div class='col-sm-1 form-group' name='fieldRetryPeriod'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryPeriod"))).append(retryPeriodInput);
    var rank = $("<div class='col-sm-1 form-group' name='rank'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(rankInput);

    var selectAllBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function () {
        country.find("input[type='checkbox']").prop('checked', true).trigger("change");
    });
    selectAllBtn.attr("disabled", !canUpdate);
    var selectNoneBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function () {
        country.find("input[type='checkbox']").prop('checked', false).trigger("change");
    });
    selectNoneBtn.attr("disabled", !canUpdate);
    var btnRow = $("<div class='col-sm-2'></div>").css("margin-top", "5px").css("margin-bottom", "5px").append(selectAllBtn).append(selectNoneBtn);

    deleteBtn.click(function () {
        // trigger when any deleteBtn is clicked
        var stopAllDelete = false;
        var stopNothing = false;
        var linkToProperty = null;
        var nothing = false;
        property.toDelete = (property.toDelete) ? false : true;

        if (property.toDelete) {
            if (isSecondary) {
                content.removeClass("list-group-item-secondary");
            }
            content.addClass("list-group-item-danger");
        } else {
            content.removeClass("list-group-item-danger");
            if (isSecondary) {
                content.addClass("list-group-item-secondary");
            }
        }

        $(table).find("div.list-group-item").each(function () {
            if ($(this).find("#propName").val() === property.property) {
                if ($(this).hasClass("list-group-item-danger")) {
                    if (stopAllDelete !== true) {
                        allDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = false
                        stopNothing = true;
                    }
                } else {
                    if (stopAllDelete !== true) {
                        allDelete = false;
                        stopAllDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = true;
                    }
                }
            }
        })

        $("#propListWrapper li a").each(function () {
            if ($(this).text() === property.property)
                linkToProperty = $(this).parent();
        });


        // set the property in red (or remove the red color)

        // set the link to the property in red (or remove the red color)
        var propertyName = property.property;

        // go though every link and look for the right one
        if (linkToProperty !== null) {
            if (allDelete === true && nothing === false) {
                // set color to red
                linkToProperty.css("background-color", "#c94350");
            } else if (nothing === true) {
                // set color to white
                linkToProperty.css("background-color", "#fff");
            } else {
                // set color to pink
                linkToProperty.css("background-color", "#f2dede");
            }

        }
    });

    moreBtn.click(function () {
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down");
            $(this).find("span").addClass("glyphicon-chevron-up");
        } else {
            $(this).find("span").removeClass("glyphicon-chevron-up");
            $(this).find("span").addClass("glyphicon-chevron-down");
        }
        $(this).parent().parent().find(".row:not([name='masterProp'])").toggle();
    });

    propertyInput.change(function () {
        property.property = $(this).val();
    });

    descriptionInput.change(function () {
        property.description = $(this).val();
    });

    selectType.change(function () {
        property.type = $(this).val();
        setPlaceholderProperty($(this).parents(".property"), property);
    });

    selectDB.change(function () {
        property.database = $(this).val();
    });

    valueInput.change(function () {
        property.value1 = $(this).val();
    });

    value2Input.change(function () {
        property.value2 = $(this).val();
    });

    lengthInput.change(function () {
        property.length = $(this).val();
    });

    rowLimitInput.change(function () {
        property.rowLimit = $(this).val();
    });

    cacheExpireInput.change(function () {
        property.cacheExpire = parseInt($(this).val());
    });

    selectNature.change(function () {
        property.nature = $(this).val();
    });

    retryNbInput.change(function () {
        property.retryNb = $(this).val();
    });

    retryPeriodInput.change(function () {
        property.retryPeriod = $(this).val();
    });

    rankInput.change(function () {
        property.rank = $(this).val();
    });

    row1.data("property", property);
    row1.append(propertyName);
    row1.append(description);
    props.append(row1);

    row4.append(btnRow);
    row4.append(country);
    props.append(row4);

    row2.append(type);
    row2.append(db);
    row2.append(value);
    row2.append(value2);
    props.append(row2);


    row3.append(length);
    row3.append(rowLimit);
    row3.append(nature);
    row3.append(retryNb);
    row3.append(retryPeriod);
    row3.append(cacheExpire);
    row3.append(rank);
    props.append(row3);

    right.append(moreBtn).append(deleteBtn);

    content.append(props).append(right);
    table.append(content);
    return [props, property];
}

function propertiesToArray(propList) {
    var propertyArray = [];
    for (var index = 0; index < propList.length; index++) {
        propertyArray.push(propList[index].property);
    }
    return propertyArray;
}

function drawInheritedProperty(propList) {
    var doc = new Doc();
    var selectType = getSelectInvariant("PROPERTYTYPE", false, true).attr("disabled", true);
    selectType.attr("name", "inheritPropertyType");
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true).attr("disabled", true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true).attr("disabled", true);
    var table = $("#inheritedPropPanel");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];
        var test = property.fromTest;
        var testcase = property.fromTestCase;

        var moreBtn = $("<button class='btn btn-default add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
        var editBtn = $("<a href='./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&property=" + encodeURI(property.property) + "' class='btn btn-primary add-btn'></a>").append($("<span></span>").addClass("glyphicon glyphicon-pencil"));

        var propertyInput = $("<input id='propName' name='propName' style='width: 100%; font-size: 16px; font-weight: 600;' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<pre id='inheritPropertyValue" + index + "' style='min-height:150px'  rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control input-sm").text(property.value1);
        var value2Input = $("<textarea name='inheritPropertyValue2' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control input-sm").val(property.value2);
        var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.length);
        var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.rowLimit);
        var cacheExpireInput = $("<input placeholder='0' readonly='readonly'>").addClass("form-control input-sm").val(property.cacheExpire);
        var retryNbInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryNb") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.retryNb);
        var rankInput = $("<input type='number' placeholder='" + doc.getDocLabel("testcasecountryproperties", "Rank") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.rank);

        var retryPeriodInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryPeriod") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.retryPeriod);

        var content = $("<div class='row property list-group-item disabled'></div>");
        var props = $("<div class='col-sm-11' name='inheritPropertyLine' id='inheritPropertyLine" + property.property + "'></div>");
        var right = $("<div class='col-sm-1 propertyButtons'></div>");

        var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row' name='masterProp'></div>");
        var row3 = $("<div class='row' style='display:none;'></div>");
        var row4 = $("<div class='row' name='masterProp'></div>");
        var row5 = $("<div class='row'></div>");
        var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);
        var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);
        var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(property.country, property.country, true));
        var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.clone().val(property.type));
        var db = $("<div class='col-sm-2 form-group' name='fieldDatabase'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
        var value = $("<div class='col-sm-8 form-group' name='fieldValue1'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
        var value2 = $("<div class='col-sm-6 form-group' name='fieldValue2'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
        var length = $("<div class='col-sm-2 form-group' name='fieldLength'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
        var rowLimit = $("<div class='col-sm-2 form-group' name='fieldRowLimit'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
        var nature = $("<div class='col-sm-2 form-group' name='fieldNature'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.val(property.nature));
        var cacheExpire = $("<div class='col-sm-2 form-group' name='fieldExpire'></div>").append($("<label></label>").text("cacheExpire")).append(cacheExpireInput);
        var retryNb = $("<div class='col-sm-2 form-group' name='fieldRetryNb'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryNb"))).append(retryNbInput);
        var retryPeriod = $("<div class='col-sm-1 form-group' name='fieldRetryPeriod'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryPeriod"))).append(retryPeriodInput);
        var rank = $("<div class='col-sm-1 form-group' name='Rank'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(rankInput);


        var selectAllBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function () {
            country.find("input[type='checkbox']").prop('checked', true);
        });
        var selectNoneBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function () {
            country.find("input[type='checkbox']").prop('checked', false);
        });
        var btnRow = $("<div class='col-sm-2'></div>").css("margin-top", "5px").css("margin-bottom", "5px").append(selectAllBtn).append(selectNoneBtn);

        moreBtn.click(function () {
            if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
                $(this).find("span").removeClass("glyphicon-chevron-down");
                $(this).find("span").addClass("glyphicon-chevron-up");
            } else {
                $(this).find("span").removeClass("glyphicon-chevron-up");
                $(this).find("span").addClass("glyphicon-chevron-down");
            }
            $(this).parent().parent().find(".row:not([name='masterProp'])").toggle();
        });

        row1.data("property", property);
        row1.append(propertyName);
        row1.append(description);
        props.append(row1);

        row4.append(btnRow);
        row4.append(country);
        props.append(row4);

        row2.append(type);
        row2.append(db);
        row2.append(value);
        row2.append(value2);
        props.append(row2);

        row3.append(db);
        row3.append(length);
        row3.append(cacheExpire);
        row3.append(rowLimit);
        row3.append(nature);
        row3.append(retryNb);
        row3.append(retryPeriod);
        row3.append(rank);
        props.append(row3);

        right.append(moreBtn);
        right.append(editBtn);

        content.append(props).append(right);
        table.append(content);

        var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
        $(htmlElement).append($("<a></a>").attr("href", "#inheritPropertyLine" + property.property).text(property.property));

        $("#inheritPropList").append(htmlElement);
    }

    sortProperties("#inheritedPropPanel");
}

function loadPropertiesAndDraw(test, testcase, testcaseinfo, propertyToFocus, canUpdate) {

    return new Promise(function (resolve, reject) {
        var array = [];
        var secondaryPropertiesArray = [];

        var propertyList = [];
        var secondaryPropertyList = [];

        $.ajax({
            url: "GetPropertiesForTestCase",
            data: {test: test, testcase: testcase},
            async: true,
            success: function (data) {

                data.sort(function (a, b) {
                    return compareStrings(a.property, b.property);
                })

                for (var index = 0; index < data.length; index++) {
                    var property = data[index];
                    // check if the property is secondary
                    var isSecondary = property.rank === 2;
                    //var isSecondary = property.description.indexOf("[secondary]") >= 0;

                    if (isSecondary) {
                        secondaryPropertiesArray.push(data[index].property);
                    } else {
                        array.push(data[index].property);
                    }
                    property.toDelete = false;
                    var prop = drawProperty(property, testcaseinfo, canUpdate, index);
                    setPlaceholderProperty(prop[0], prop[1]);

                    if (isSecondary) {
                        secondaryPropertyList.push(property.property);
                    } else {
                        propertyList.push(property.property);
                    }
                }
                loadedPropertiesNumber = propertyList.length;
                localStorage.setItem("properties", JSON.stringify(propertyList));
                localStorage.setItem("secondaryProperties", JSON.stringify(propertyList));
                sortProperties("#propTable");
                sortSecondaryProperties("#propTable");

                var scope = undefined;
                if (propertyToFocus != undefined && propertyToFocus != null) {
                    $("#propTable #propName").each(function (i) {
                        if ($(this).val() == propertyToFocus) {
                            scope = this;
                            $("#propertiesModal").on("shown.bs.modal", function (e) {
                                $(scope).focus();
                                $(scope).click();
                            });
                        }
                    });
                }

                var propertyListUnique = Array.from(new Set(propertyList));
                var secondaryPropertyListUnique = Array.from(new Set(secondaryPropertyList));

                for (var index = 0; index < propertyListUnique.length; index++) {
                    drawPropertyList(propertyListUnique[index], index, false);
                }

                for (var index = 0; index < secondaryPropertyListUnique.length; index++) {
                    drawPropertyList(secondaryPropertyListUnique[index], index, true);
                }

                array.sort(function (a, b) {
                    return compareStrings(a, b);
                })


                resolve(propertyListUnique);

            },
            error: showUnexpectedError
        });
    });
}

function sortProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".property");
    list.sort(function (a, b) {

        var aProp = $(a).find("#masterProp").data("property").property.toLowerCase(),
                bProp = $(b).find("#masterProp").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

// Temporary function: can be merged with sortProperties by adding one parameter
// to call the children() function differently
function sortSecondaryProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".secondaryProperty");
    list.sort(function (a, b) {

        var aProp = $(a).find("#masterProp").data("property").property.toLowerCase(),
                bProp = $(b).find("#masterProp").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

function getTestCaseCountry(countries, countryToCheck, isDisabled) {
    var html = [];
    var cpt = 0;
    var div = $("<div></div>").addClass("checkbox");

    $.each(countries, function (index) {
        var country;
        if (typeof index === "number") {
            country = countries[index].value;
        } else if (typeof index === "string") {
            country = index;
        }
        var input = $("<input>").attr("type", "checkbox").attr("name", country);

        if ((countryToCheck.indexOf(country) !== -1)) {
            input.prop("checked", true).trigger("change");
        }
        if (isDisabled) {
            input.prop("disabled", "disabled");
        } else {
            input.change(function () {
                var country = $(this).prop("name");
                var checked = $(this).prop("checked");
                var index = countryToCheck.indexOf(country);

                if (checked && index === -1) {
                    countryToCheck.push(country);
                } else if (!checked && index !== -1) {
                    countryToCheck.splice(index, 1);
                }
            });
        }

        div.append($("<label></label>").addClass("checkbox-inline")
                .append(input)
                .append(country));

        cpt++;
        html.push(div);
    });

    return html;
}

function loadTestCaseInfo(info) {
    $(".testTestCase #description").text(info.description);
}

function changeLib() {
    setModif(true);
    var stepHtml = $("#steps li.active");
    var stepData = stepHtml.data("item");
    if (stepData.isLibraryStep === "Y") {
        stepData.isLibraryStep = "N";
        $(this).removeClass("btn-dark");
    } else {
        stepData.isLibraryStep = "Y";
        $(this).addClass("btn-dark");
    }
}

function generateImportInfoId(stepInfo) {
    var hash = 0;
    if (stepInfo.description.length === 0)
        return hash;
    for (i = 0; i < stepInfo.description.length; i++) {
        char = stepInfo.description.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

var importInfoIdx = 0;

function showImportStepDetail(element) {

    var stepInfo = $(element).data("stepInfo");

    if ($(element).hasClass("selected")) {
        $(element).removeClass("selected");
        $(element).find("[name='idx']").remove();
        $("#" + generateImportInfoId(stepInfo)).remove();
    } else {
        importInfoIdx++;
        $(element).addClass("selected");
        $(element).append('<span class="badge" name="idx">' + importInfoIdx + ' </span>');
        var importInfoId = generateImportInfoId(stepInfo);

        var importInfo =
                '<div id="' + importInfoId + '" class="row">' +
                '   <div class="col-sm-5"><span class="badge">' + importInfoIdx + ' </span>&nbsp;' + stepInfo.description + '</div>' +
                '   <div name="importInfo" class="col-sm-5"></div>' +
                '   <div class="col-sm-2">' +
                '    <label class="checkbox-inline">' +
                '        <input type="checkbox" name="useStep" checked> Use Step' +
                '    </label>' +
                '   </div>' +
                '</div>';

        $("#importDetail").append(importInfo);
        $("#" + importInfoId).find("[name='importInfo']").text("Imported from " + stepInfo.test + " - " + stepInfo.testCase + " - " + stepInfo.sort + ")").data("stepInfo", stepInfo);

        $("#importDetail[name='useStep']").prop("checked", true);

        $("#importDetail").show();
    }
}

function initStep() {
    return {
        "isLibraryStep": "N",
        "objType": "step",
        "libraryStepTest": "",
        "libraryStepTestCase": "",
        "isUsedStep": "N",
        "description": "",
        "libraryStepStepId": -1,
        "actions": [],
        "loop": "onceIfConditionTrue",
        "conditionOperator": "always",
        "conditionVal1": "",
        "conditionVal2": "",
        "conditionVal3": "",
        "isExecutionForced": "N"
    };
}

function addStep(event) {
    var steps = event.data.steps;
    $("#addStepModal").modal('show');

    // Setting the focus on the Description of the step.
    $('#addStepModal').on('shown.bs.modal', function () {
        $('#description').focus();
    });

    $("#addStepConfirm").unbind("click").click(function (event) {
        setModif(true);


        if ($("[name='importInfo']").length === 0) { // added a new step
            var step = initStep();
            step.description = $("#addStepModal #description").val();
            var stepObj = new Step(step, steps, true);
            stepObj.draw();
            steps.push(stepObj);
            stepObj.html.trigger("click");
        } else {
            // added a library step
            $("[name='importInfo']").each(function (idx, importInfo) {
                var step = initStep();

                if ($(importInfo).data("stepInfo")) {
                    var useStep = $(importInfo).data("stepInfo");

                    step.description = useStep.description;

                    $.ajax({
                        url: "ReadTestCaseStep",
                        data: {test: useStep.test, testcase: useStep.testCase, step: useStep.step},
                        async: false,
                        success: function (data) {
                            step.actions = data.tcsActions;
                            for (var index = 0; index < data.tcsActionControls.length; index++) {
                                var control = data.tcsActionControls[index];

                                for (var i = 0; i < step.actions.length; i++) {
                                    if (step.actions[i].actionId === control.actionId) {
                                        step.actions[i].controls.push(control);
                                        break;
                                    }
                                }
                            }
                            sortStep(step);
                        }
                    });
                    if ($("#" + generateImportInfoId(useStep)).find("[name='useStep']").prop("checked")) {
                        step.isUsedStep = "Y";
                        step.libraryStepTest = useStep.test;
                        step.libraryStepTestCase = useStep.testCase;
                        step.libraryStepStepId = useStep.step;
                        step.useStepStepSort = useStep.sort;
                    }
                }
                var stepObj = new Step(step, steps, true);

                stepObj.draw();
                steps.push(stepObj);
                stepObj.html.trigger("click");
            });
        }
    });
}

function createSteps(data, steps, stepIndex, canUpdate, hasPermissionsStepLibrary) {
    // If the testcase has no steps, we create an empty one.
    if (data.length === 0) {
        var step = initStep();
        var stepObj = new Step(step, steps, canUpdate, hasPermissionsStepLibrary);

        stepObj.draw();
        steps.push(stepObj);
//        setModif(true);
    }
    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, steps, canUpdate, hasPermissionsStepLibrary);

        stepObj.draw();
        steps.push(stepObj);
    }

    if (stepIndex !== undefined) {
        var find = false;
        for (var i = 0; i < steps.length; i++) {
            if (steps[i].sort == stepIndex) {
                find = true;
                $(steps[i].html[0]).click();
            }
        }
        if ((!find) && (steps.length > 0)) {
            $(steps[0].html[0]).click();
        }
    } else if (steps.length > 0) {
        $(steps[0].html[0]).click();
    } else {
        $("#stepHeader").hide();
        $("#addActionBottomBtn").hide();
        $("#addAction").attr("disabled", true);
    }
}

/** Modification Status * */

var getModif, setModif, initModification;
(function () {
    var isModif = false;
    getModif = function () {
        return isModif;
    };
    setModif = function (val) {
        isModif = val;
        if (isModif === true && $("#saveScript").hasClass("btn-default")) {
            $("#saveScript").removeClass("btn-default").addClass("btn-primary");
        } else if (isModif === false && $("#saveScript").hasClass("btn-primary")) {
            $("#saveScript").removeClass("btn-primary").addClass("btn-default");
        }

    };
    initModification = function () {
        $(".panel-body input, .panel-body select, .panel-body textarea").change(function () {
            setModif(true);
        })
    };
})();

/** LIBRARY STEP UTILY FUNCTIONS * */

function loadLibraryStep(search, system) {
    var search_lower = "";
    if (search !== undefined) {
        search_lower = search.toLowerCase();
    }
    $("#lib").empty();
    showLoaderInModal("#addStepModal");
    $.ajax({
        url: "GetStepInLibrary",
        data: {system: system},
        async: true,
        success: function (data) {
            var test = {};

            for (var index = 0; index < data.testCaseSteps.length; index++) {
                var step = data.testCaseSteps[index];

                if (search === undefined || search === "" || step.description.toLowerCase().indexOf(search_lower) > -1 || step.testCase.toLowerCase().indexOf(search_lower) > -1 || step.test.toLowerCase().indexOf(search_lower) > -1) {
                    if (!test.hasOwnProperty(step.test)) {
                        $("#lib").append($("<a></a>").addClass("list-group-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "']")
                                .text(step.test).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listGr = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test);
                        $("#lib").append(listGr);

                        test[step.test] = {content: listGr, testCase: {}};
                    }
                    if ((!test[step.test].testCase.hasOwnProperty(step.testCase))) {
                        var listGrp = test[step.test].content;
                        listGrp.append($("<a></a>").addClass("list-group-item sub-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "'][data-testCase='" + step.testCase + "']")
                                .text(step.testCase + " - " + step.tcdesc).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listCaseGr = $("<div></div>").addClass("list-group collapse in").attr("data-test", step.test).attr("data-testCase", step.testCase);
                        listGrp.append(listCaseGr);

                        test[step.test].testCase[step.testCase] = {content: listCaseGr, step: {}};
                    }
                    var listCaseGrp = test[step.test].testCase[step.testCase].content;
                    var listStepGrp = $("<a></a>").addClass("list-group-item sub-sub-item").attr("href", "#").text(step.description).data("stepInfo", step);
                    listStepGrp.attr("onclick", "javascript:showImportStepDetail($(this))");
                    listCaseGrp.append(listStepGrp);
                    test[step.test].testCase[step.testCase].step[step.description] = listStepGrp;
                }
            }

            if (search !== undefined && search !== "") {
                $('#lib').find("div").toggleClass('in');
            }

            $('#addStepModal > .list-group-item').unbind("click").on('click', function () {
                $('.glyphicon', this)
                        .toggleClass('glyphicon-chevron-right')
                        .toggleClass('glyphicon-chevron-down');
            });

            $("#addStepModal #search").unbind("input").on("input", function (e) {
                var search = $(this).val();
                // Clear any previously set timer before setting a fresh one
                window.clearTimeout($(this).data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    loadLibraryStep(search, system);
                }, 500));
            });

            hideLoaderInModal("#addStepModal");
        }
    });
}

function loadApplicationObject(application) {
    return new Promise(function (resolve, reject) {
        var array = [];
        $.ajax({
            url: "ReadApplicationObject?application=" + application,
            dataType: "json",
            success: function (data) {
                for (var i = 0; i < data.contentTable.length; i++) {
                    array.push(data.contentTable[i]);
                }
                resolve(array);
            }
        });
    });
}

function showStepUsesLibraryInConfirmationModal(object) {
    var doc = new Doc();
    $("#confirmationModal [name='buttonConfirm']").text("OK");
    $("#confirmationModal [name='buttonDismiss']").hide();
    $("#confirmationModal").on("hidden.bs.modal", function () {
        $("#confirmationModal [name='buttonConfirm']").text(doc.getDocLabel("page_global", "buttonConfirm"));
        $("#confirmationModal [name='buttonDismiss']").show();
        $("#confirmationModal").unbind("hidden.bs.modal");
    });

    $.ajax({
        url: "ReadTestCaseStep",
        dataType: "json",
        data: {
            test: object.test,
            testcase: object.testcase,
            step: object.stepId,
            getUses: true
        },
        success: function (data) {
            var content = "";
            for (var i = 0; i < data.step.length; i++) {
                content += "<a target='_blank' href='./TestCaseScript.jsp?test=" + encodeURI(data.step[i].test) + "&testcase=" + encodeURI(data.step[i].testcase) + "&step=" + encodeURI(data.step[i].sort) + "'>" + data.step[i].test + " - " + data.step[i].testcase + " - " + data.step[i].sort + " - " + data.step[i].description + "</a><br/>"
            }
            $("#confirmationModal #otherStepThatUseIt").empty().append(content);
        }
    });
    showModalConfirmation(function () {
        $('#confirmationModal').modal('hide');
    }, undefined, doc.getDocLabel("page_global", "warning"),
            doc.getDocLabel("page_testcasescript", "cant_detach_library") +
            "<br/>" +
            "<div id='otherStepThatUseIt' style='width:100%;'>" +
            "<div style='width:30px; margin-left: auto; margin-right: auto;'>" +
            "<span class='glyphicon glyphicon-refresh spin'></span>" +
            "</div>" +
            "</div>", "", "", "", "");
}


/** DRAG AND DROP HANDLERS * */

var source;

function isBefore(a, b) {
    if (a !== b && a.parentNode === b.parentNode) {
        for (var cur = a; cur; cur = cur.nextSibling) {
            if (cur === b) {
                return true;
            }
        }
    }
    return false;
}

function handleDragStart(event) {
    var dataTransfer = event.originalEvent.dataTransfer;
    var obj = this.parentNode;
    var offsetX = 50;
    var offsetY = 50;
    var img;

    if ($(obj).data("item") instanceof Action) {
        img = obj.parentNode;
    } else if ($(obj).data("item") instanceof Control) {
        img = obj;
    } else {
        img = obj;
        offsetX = 15;
        offsetY = 15;
    }

    source = obj;
    obj.style.opacity = '0.4';
    dataTransfer.effectAllowed = 'move';
    dataTransfer.setData('text/html', img.innerHTML);
    dataTransfer.setDragImage(img, offsetX, offsetY);
}

function handleDragEnter(event) {
    setModif(true);
    var target = this.parentNode;
    var sourceData = $(source).data("item");
    var targetData = $(target).data("item");

    if (sourceData instanceof Action && targetData instanceof Action) {
        if (isBefore(source.parentNode, target.parentNode)) {
            $(target).parent(".action-group").after(source.parentNode);
        } else {
            $(target).parent(".action-group").before(source.parentNode);
        }
    } else if (sourceData instanceof Control &&
            (targetData instanceof Action || targetData instanceof Control)) {
        if (isBefore(source, target) || targetData instanceof Action) {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    } else if (sourceData instanceof Step && targetData instanceof Step) {
        if (isBefore(source, target)) {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    } else if (sourceData instanceof Action && targetData instanceof Step) {
        $(target).click();
    } else if (sourceData instanceof Control && targetData instanceof Step) {
        $(target).click();
    }
}

function handleDragOver(event) {

    var e = event.originalEvent;

    if (e.preventDefault) {
        e.preventDefault(); // Necessary. Allows us to drop.
    }
    e.dataTransfer.dropEffect = 'move';

    return false;
}

function handleDragLeave(event) {

}

function handleDrop(event) {
    var e = event.originalEvent;

    if (e.stopPropagation) {
        e.stopPropagation(); // stops the browser from redirecting.
    }

    return false;
}

function handleDragEnd(event) {
    this.parentNode.style.opacity = '1';
    setAllSort();
}

/** DATA AGREGATION * */

function sortStep(step) {
    for (var j = 0; j < step.actions.length; j++) {
        var action = step.actions[j];

        action.controls.sort(function (a, b) {
            return a.sort - b.sort;
        });
    }

    step.actions.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function compareStrings(a, b) {
    a = a.toLowerCase();
    b = b.toLowerCase();

    return (a < b) ? -1 : (a > b) ? 1 : 0;
}

/** JAVASCRIPT OBJECT * */

function Step(json, steps, canUpdate, hasPermissionsStepLibrary) {
    this.stepActionContainer = $("<div></div>").addClass("step-container").css("display", "none");
    this.sort = json.sort;
    this.stepId = json.stepId;
    this.description = json.description;
    this.isExecutionForced = json.isExecutionForced;
    this.loop = json.loop;
    this.conditionOperator = json.conditionOperator;
    this.conditionVal1 = json.conditionVal1;
    this.conditionVal2 = json.conditionVal2;
    this.conditionVal3 = json.conditionVal3;
    this.isUsedStep = json.isUsedStep;
    this.isLibraryStep = json.isLibraryStep;
    this.libraryStepTest = json.libraryStepTest;
    this.libraryStepTestCase = json.libraryStepTestCase;
    this.libraryStepStepId = json.libraryStepStepId;
    this.useStepStepSort = json.useStepStepSort;
    this.test = json.test;
    this.testcase = json.testcase;
    this.isStepInUseByOtherTestCase = json.isStepInUseByOtherTestCase;
    this.actions = [];
    if (canUpdate) {
        // If we can update the testcase we check whether we can still modify
        // following the TestStepLibrary group.
        if (!hasPermissionsStepLibrary && json.isLibraryStep === "Y") {
            canUpdate = false;
        }
    }
    this.setActions(json.actions, canUpdate);

    this.steps = steps;
    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;
    this.hasPermissionsStepLibrary = hasPermissionsStepLibrary;

    this.html = $("<li style='padding-right:5px'></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-sm-8 textArea").addClass("step-description").text(this.description);
}

Step.prototype.draw = function () {
    var htmlElement = this.html;
    var drag = $("<div></div>").addClass("col-sm-1 drag-step").css("padding-left", "5px").css("padding-right", "2px").prop("draggable", true)
            .append($("<span></span>").addClass("fa fa-ellipsis-v"));

    var loopIcon = $("<div></div>").addClass("col-sm-1 loop-Icon")
    var libraryIcon = $("<div></div>").addClass("col-sm-1 library-Icon")

    if (this.loop != "onceIfConditionTrue" && this.loop != "onceIfConditionFalse") {
        loopIcon = $("<span class='loopIcon'></span>").addClass("glyphicon glyphicon-refresh loop-Icon");
    }

    if (this.isLibraryStep == "Y") {
        libraryIcon = $("<span class='libraryIcon'></span>").addClass("glyphicon glyphicon-book library-Icon");
    }

    if (this.isUsedStep == "Y") {
        libraryIcon = $("<span class='libraryIcon'></span>").addClass("glyphicon glyphicon-lock library-Icon");
    }

    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);

    // htmlElement.append(badge);
    htmlElement.append(drag);
    htmlElement.append(this.textArea);
    htmlElement.append(loopIcon);
    htmlElement.append(libraryIcon);
    htmlElement.data("item", this);
    htmlElement.click(this.show);

    $("#stepPlus").unbind("click").click(function () {
        $("#stepHiddenRow").toggle();
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    $("#steps").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
    this.refreshSort();
};

Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    $("#stepHeader").show();
    $("#addActionBottomBtn").show();

    for (var i = 0; i < object.steps.length; i++) {
        var step = object.steps[i];

        step.stepActionContainer.hide();
        step.stepActionContainer.find("[data-toggle='tooltip']").tooltip("hide");
        step.html.removeClass("active");
    }

    $(this).addClass("active");
    if (object.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    $("#isLib").unbind("click");
    if (object.isLibraryStep === "Y") {
        $("#isLib").addClass("btn-dark");
        if (object.isStepInUseByOtherTestCase) {
            $("#isLib").click(function () {

                showStepUsesLibraryInConfirmationModal(object);

            });
        } else {
            $("#isLib").click(changeLib);
        }
    } else {
        $("#isLib").removeClass("btn-dark");
        $("#isLib").click(changeLib);
    }

    if (object.isUsedStep === "Y") {
        $("#isLib").hide();
        $("#UseStepRow").html("(" + doc.getDocLabel("page_testcasescript", "imported_from") + " <a href='./TestCaseScript.jsp?test=" + encodeURI(object.libraryStepTest) + "&testcase=" + encodeURI(object.libraryStepTestCase) + "&step=" + encodeURI(object.useStepStepSort) + "' >" + object.libraryStepTest + " - " + object.libraryStepTestCase + " - " + object.useStepStepSort + "</a>)").show();
        $("#UseStepRowButton").html("|").show();
        $("#addAction").prop("disabled", true);
        $("#addActionBottomBtn").hide();
        $("#isUseStep").show();
    } else {
        $("#isLib").show();
        $("#UseStepRow").html("").hide();
        $("#UseStepRowButton").html("").hide();
        $("#addAction").prop("disabled", false);
        $("#addActionBottomBtn").show();
        $("#isUseStep").hide();
    }

    if (object.toDelete) {
        $("#contentWrapper").addClass("list-group-item-danger");
    } else {
        $("#contentWrapper").removeClass("list-group-item-danger");
    }


    $("#stepLoop").replaceWith(getSelectInvariant("STEPLOOP", false, true).css("width", "100%").addClass("form-control input-sm").attr("id", "stepLoop"));
    $("#stepLoop").unbind("change").change(function () {
        setModif(true);
        object.loop = $(this).val();
    });

    $("#stepForceExe").replaceWith(getSelectInvariant("STEPFORCEEXE", true, true).css("width", "100%").addClass("form-control input-sm").attr("id", "stepForceExe"));
    $("#stepForceExe").unbind("change").change(function () {
        setModif(true);
        object.isExecutionForced = $(this).val();
    });

    $("#stepConditionVal1").unbind("change").change(function () {
        setModif(true);
        object.conditionVal1 = $(this).val();
    });

    $("#stepConditionVal2").unbind("change").change(function () {
        setModif(true);
        object.conditionVal2 = $(this).val();
    });

    $("#stepConditionVal3").unbind("change").change(function () {
        setModif(true);
        object.conditionVal3 = $(this).val();
    });

    $("#stepConditionOperator").replaceWith(getSelectInvariant("STEPCONDITIONOPERATOR", false, true).css("width", "100%").addClass("form-control input-sm").attr("id", "stepConditionOperator"));
    $("#stepConditionOperator").unbind("change").change(function () {
        setModif(true);
        object.conditionOperator = $(this).val();
        setPlaceholderCondition($("#stepConditionOperator").parent().parent(".row"));
    });


    object.stepActionContainer.show();
    $("#stepDescription").unbind("change").change(function () {
        setModif(true);
        object.description = $(this).val();
    });

    $("#isUseStep").unbind("click").click(function () {
        setModif(true);
        if (object.isUsedStep === "Y") {
            showModalConfirmation(function () {
                object.isUsedStep = "N";
                object.libraryStepStepId = -1;
                object.libraryStepTest = "";
                object.libraryStepTestCase = "";
                saveScript();
            }, undefined, doc.getDocLabel("page_testcasescript", "unlink_useStep"), doc.getDocLabel("page_testcasescript", "unlink_useStep_warning"), "", "", "", "");
        }
    });

    $("#stepLoop").val(object.loop);
    $("#stepConditionOperator").val(object.conditionOperator);
    $("#stepConditionVal1").val(object.conditionVal1);
    $("#stepConditionVal2").val(object.conditionVal2);
    $("#stepConditionVal3").val(object.conditionVal3);
    $("#stepDescription").val(object.description);
    $("#stepForceExe").val(object.isExecutionForced);
    $("#stepId").text(object.sort);
    $("#stepInfo").show();
    $("#addActionContainer").show();
    $("#stepHeader").show()
    setPlaceholderCondition($("#stepConditionOperator").parent().parent(".row"));

    // Disable fields if Permission not allowing.
    // Description and unlink the step with UseStep of the Step can be modified
    // if hasPermitionUpdate is true.
    var activateDisable = !object.hasPermissionsUpdate;
    $("#stepDescription").attr("disabled", activateDisable);
    $("#isUseStep").attr("disabled", activateDisable);
    // Flag the Step as a library if hasPermissionsUpdate and
    // hasPermissionsStepLibrary is true.
    var activateIsLib = !(object.hasPermissionsUpdate && object.hasPermissionsStepLibrary)
    $("#isLib").attr("disabled", activateIsLib);
    // Detail of the Step can be modified if hasPermitionUpdate is true and Step
    // is not a useStep.
    var activateDisableWithUseStep = !(object.hasPermissionsUpdate && !(object.isUsedStep === "Y"));
    $("#stepLoop").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionOperator").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal1").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal2").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal3").attr("disabled", activateDisableWithUseStep);
};

Step.prototype.setActions = function (actions, canUpdate) {
    for (var i = 0; i < actions.length; i++) {
        this.setAction(actions[i], undefined, canUpdate);
    }
};

Step.prototype.setAction = function (action, afterAction, canUpdate) {
    if (action instanceof Action) {
        action.draw(afterAction);
        this.actions.push(action);
    } else {
        var actionObj = new Action(action, this, canUpdate);

        actionObj.draw(afterAction);
        this.actions.push(actionObj);
    }
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepDescription").val(description);
};

Step.prototype.setDelete = function () {
    this.toDelete = (this.toDelete) ? false : true;

    if ($("#contentWrapper").hasClass("list-group-item-danger")) {
        $("#contentWrapper").removeClass("list-group-item-danger");
    } else {
        $("#contentWrapper").removeClass("well").addClass("list-group-item-danger well")
    }

    if (this.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    for (var i = 0; i < this.steps.length; i++) {
        var step = this.steps[i];

        if (step.toDelete) {
            step.html.addClass("list-group-item-danger");
            step.html.removeClass("list-group-item-calm");
        } else {
            step.html.addClass("list-group-item-calm");
            step.html.removeClass("list-group-item-danger");
        }
    }
};

Step.prototype.setStep = function (stepId) {
    this.stepId = stepId;
};

Step.prototype.getStep = function () {
    return this.stepId;
};

Step.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Step.prototype.refreshSort = function () {
    this.html.find("#labelDiv").empty().text(this.sort);
};

Step.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.sort = this.sort;
    json.description = this.description;
    json.isUsedStep = this.isUsedStep;
    json.libraryStepTest = this.libraryStepTest;
    json.libraryStepTestCase = this.libraryStepTestCase;
    json.libraryStepStepId = this.libraryStepStepId;
    json.isLibraryStep = this.isLibraryStep;
    json.loop = this.loop;
    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal3 = this.conditionVal3;
    json.isExecutionForced = this.isExecutionForced;

    return json;
};

function Action(json, parentStep, canUpdate) {
    this.html = $("<div></div>").addClass("action-group");
    this.parentStep = parentStep;

    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testcase;
        this.stepId = json.stepId;
        this.sequence = json.actionId;
        this.sort = json.sort;
        this.description = json.description;
        this.action = json.action;
        // A SUPPRIMER
        //this.object = json.value1;
        //this.property = json.value2;
        // FIN SUPPR
        this.forceExeStatus = json.isFatal;
        this.conditionOperator = json.conditionOperator;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
        this.conditionVal3 = json.conditionVal3;
        this.screenshotFileName = json.screenshotFileName;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.value3 = json.value3;
        this.controls = [];
        this.setControls(json.controls, canUpdate);
    } else {
        this.test = "";
        this.testcase = "";
        this.stepId = parentStep.stepId;
        this.description = "";
        this.action = "doNothing";
        this.object = "";
        this.property = "";
        this.forceExeStatus = "";
        this.conditionOperator = "always";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
        this.conditionVal3 = "";
        this.screenshotFileName = "";
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.controls = [];
    }

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;
}

Action.prototype.draw = function (afterAction) {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("step-action row").addClass("action");
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true);
    var plusBtn = $("<button></button>").addClass("btn btn-default add-btn").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
    var addBtn = $("<button></button>").addClass("btn btn-success add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var addABtn = $("<button></button>").addClass("btn btn-primary add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn btn-danger add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-1").css("padding", "0px").append($("<div>").addClass("boutonGroup").append(addABtn).append(supprBtn).append(addBtn).append(plusBtn));
    var imgGrp = $("<div></div>").addClass("col-lg-1").css("height", "100%").append($("<div style='margin-top:40px;'></div>").append($("<img>").attr("id", "ApplicationObjectImg").css("width", "100%")));

    if ((this.parentStep.isUsedStep === "N") && (action.hasPermissionsUpdate)) {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    } else {
        addBtn.prop("disabled", true);
        addABtn.prop("disabled", true);
        supprBtn.prop("disabled", true);
    }

    plusBtn.click(function () {
        var container = $(this).parent().parent().parent();
        container.find(".fieldRow:eq(2)").toggle();
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    var scope = this;
    addBtn.click(function () {
        addControlAndFocus(scope);
    });
    addABtn.click(function () {
        addActionAndFocus(scope);
    });

    supprBtn.click(function () {
        setModif(true);
        action.toDelete = (action.toDelete) ? false : true;

        if (action.toDelete) {
            action.html.find(".step-action").addClass("danger");
        } else {
            action.html.find(".step-action").removeClass("danger");
        }
    });
    row.append(drag);
    row.append(this.generateContent());
    row.append(imgGrp);
    row.append(btnGrp);
    row.data("item", this);
    htmlElement.prepend(row);

    setPlaceholderAction(htmlElement);
    setPlaceholderCondition(htmlElement);

    listenEnterKeypressWhenFocusingOnDescription(htmlElement);

    if (afterAction === undefined) {
        this.parentStep.stepActionContainer.append(htmlElement);
    } else {
        afterAction.html.after(htmlElement);
    }
    this.refreshSort();
};

Action.prototype.setControls = function (controls, canUpdate) {
    for (var i = 0; i < controls.length; i++) {
        this.setControl(controls[i], undefined, canUpdate);
    }
};

Action.prototype.setControl = function (control, afterControl, canUpdate) {
    if (control instanceof Control) {
        control.draw(afterControl);
        this.controls.push(control);
    } else {
        var controlObj = new Control(control, this, canUpdate);

        controlObj.draw(afterControl);
        this.controls.push(controlObj);
    }
};

Action.prototype.setStep = function (stepId) {
    this.stepId = stepId;
};

Action.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Action.prototype.getSequence = function () {
    return this.sequence;
};

Action.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Action.prototype.refreshSort = function () {
    this.html.find(".action #labelDiv").text(this.sort);
};

Action.prototype.generateContent = function () {
    var obj = this;
    var doc = new Doc();
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group");
    var secondRow = $("<div></div>").addClass("fieldRow row secondRow input-group").css("width", "100%");
    var thirdRow = $("<div></div>").addClass("fieldRow row thirdRow").hide();

    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input class='description form-control' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_action") + "'>");
    descContainer.append($("<span class='input-group-addon' style='font-weight: 700;' id='labelDiv'></span>"));
    descContainer.append(descriptionField);

    var actions = $("<select></select>").addClass("form-control input-sm");

    var value1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v1");
    var value2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v2");
    var value3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v3");

    var actionconditionoperator = $("<select></select>").addClass("form-control input-sm");
    var actionconditionval1 = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm");
    var actionconditionval2 = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm");
    var actionconditionval3 = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm");

    var forceExeStatusList = $("<select></select>").addClass("form-control input-sm");

    descriptionField.val(this.description);
    descriptionField.css("width", "100%");
    descriptionField.on("change", function () {
        setModif(true);
        obj.description = descriptionField.val();
    });

    actionconditionoperator = getSelectInvariant("ACTIONCONDITIONOPERATOR", false, true).css("width", "100%");
    actionconditionoperator.on("change", function () {
        if (obj.conditionOperator !== actionconditionoperator.val()) {
            setModif(true);
        }
        obj.conditionOperator = actionconditionoperator.val();
        if ((obj.conditionOperator === "always") || (obj.conditionOperator === "never")) {
            actionconditionval1.parent().hide();
            actionconditionval2.parent().hide();
            actionconditionval3.parent().hide();
        } else {
            actionconditionval1.parent().show();
            actionconditionval2.parent().show();
            actionconditionval3.parent().show();
        }
        setPlaceholderCondition($(this).parents(".action"));
    });
    actionconditionoperator.val(this.conditionOperator).trigger("change");
    actionconditionoperator.attr("id", "conditionSelect");
    actionconditionval1.css("width", "100%");
    actionconditionval1.on("change", function () {
        setModif(true);
        obj.conditionVal1 = actionconditionval1.val();
    });
    actionconditionval1.val(this.conditionVal1);

    actionconditionval2.css("width", "100%");
    actionconditionval2.on("change", function () {
        setModif(true);
        obj.conditionVal2 = actionconditionval2.val();
    });
    actionconditionval2.val(this.conditionVal2);

    actionconditionval3.css("width", "100%");
    actionconditionval3.on("change", function () {
        setModif(true);
        obj.conditionVal3 = actionconditionval3.val();
    });
    actionconditionval3.val(this.conditionVal3);

    actions = getSelectInvariant("ACTION", false, true).css("width", "100%").attr("id", "actionSelect");
    actions.val(this.action);
    actions.off("change").on("change", function () {
        setModif(true);
        obj.action = actions.val();
        setPlaceholderAction($(this).parents(".action"));
        $(actions).parent().parent().find(".input-group-btn").remove();
    });

    forceExeStatusList = getSelectInvariant("ACTIONFORCEEXESTATUS", false, true).css("width", "100%");
    forceExeStatusList.val(this.forceExeStatus);
    forceExeStatusList.on("change", function () {
        setModif(true);
        obj.forceExeStatus = forceExeStatusList.val();
    });

    value1Field.val(this.value1);
    value1Field.css("width", "100%");
    value1Field.on("change", function () {
        setModif(true);
        obj.value1 = value1Field.val();
    });

    value2Field.val(this.value2);
    value2Field.css("width", "100%");
    value2Field.on("change", function () {
        setModif(true);
        obj.value2 = value2Field.val();
    });

    value3Field.val(this.value3);
    value3Field.css("width", "100%");
    value3Field.on("change", function () {
        setModif(true);
        obj.value3 = value3Field.val();
    });

    firstRow.append(descContainer);
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "action_field"))).append(actions));
    secondRow.append($("<div></div>").addClass("v1 col-lg-5").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(value1Field));
    /*
     * if(secondRow.find("col-lg-6").find("label").text() === "Chemin vers
     * l'élement" ){ console.log(".append(choiceField)") }
     */
    secondRow.append($("<div></div>").addClass("v2 col-lg-2 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Field));
    secondRow.append($("<div></div>").addClass("v3 col-lg-2 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value3_field"))).append(value3Field));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_operation_field"))).append(actionconditionoperator));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval1));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval2));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval3));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "force_execution_field"))).append(forceExeStatusList));

    actionconditionoperator.trigger("change");

    if ((this.parentStep.isUsedStep === "Y") || (!obj.hasPermissionsUpdate)) {
        descriptionField.prop("readonly", true);
        value1Field.prop("readonly", true);
        value2Field.prop("readonly", true);
        value3Field.prop("readonly", true);
        actions.prop("disabled", "disabled");
        forceExeStatusList.prop("disabled", "disabled");
        actionconditionoperator.prop("disabled", "disabled");
        actionconditionval1.prop("readonly", true);
        actionconditionval2.prop("readonly", true);
        actionconditionval3.prop("readonly", true);
    }

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);

    return content;
};

Action.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.sequence = this.sequence;
    json.sort = this.sort;
    json.description = this.description;
    json.action = this.action;
    json.object = this.value1;
    json.property = this.value2;
    json.value3 = this.value3;
    json.forceExeStatus = this.forceExeStatus;
    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal3 = this.conditionVal3;
    json.screenshotFileName = "";

    return json;
};

function Control(json, parentAction, canUpdate) {
    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testcase;
        this.stepId = json.stepId;
        this.sequence = json.actionId;
        this.control = json.control;
        this.sort = json.sort;
        this.description = json.description;
        //this.objType = json.objType;
        this.controlSequence = json.controlId;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.value3 = json.value3;
        this.fatal = json.isFatal;
        this.conditionOperator = json.conditionOperator;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
        this.conditionVal3 = json.conditionVal3;
        this.screenshotFileName = "";
    } else {
        this.test = "";
        this.testcase = "";
        this.stepId = parentAction.stepId;
        this.sequence = parentAction.actionId;
        this.control = "Unknown";
        this.description = "";
        this.objType = "Unknown";
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.fatal = "N";
        this.conditionOperator = "always";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
        this.conditionVal3 = "";
        this.screenshotFileName = "";
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;
    this.parentActionSort = parentAction.sort;

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;

    this.html = $("<div></div>").addClass("step-action row").addClass("control");
}

Control.prototype.draw = function (afterControl) {
    var htmlElement = this.html;
    var control = this;
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true);
    var plusBtn = $("<button></button>").addClass("btn btn-default add-btn").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
    var addBtn = $("<button></button>").addClass("btn btn-success add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var addABtn = $("<button></button>").addClass("btn btn-primary add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn btn-danger add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-1").css("padding", "0px").append($("<div>").addClass("boutonGroup").append(addABtn).append(supprBtn).append(addBtn).append(plusBtn));
    var imgGrp = $("<div></div>").addClass("col-lg-1").css("height", "100%").append($("<span style='display: inline-block; height: 100%; vertical-align: middle;'></span>")).append($("<img>").attr("id", "ApplicationObjectImg").css("width", "100%"));

    var content = this.generateContent();

    if ((this.parentAction.parentStep.isUsedStep === "N") && (control.hasPermissionsUpdate)) {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    }

    supprBtn.click(function () {
        setModif(true);
        control.toDelete = (control.toDelete) ? false : true;

        if (control.toDelete) {
            control.html.addClass("danger");
        } else {
            control.html.removeClass("danger");
        }
    });

    plusBtn.click(function () {
        var container = $(this).parent().parent().parent();
        container.find(".fieldRow:eq(2)").toggle();
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    if ((this.parentStep.isUsedStep === "Y") || (!control.hasPermissionsUpdate)) {

        supprBtn.attr("disabled", true);
        addBtn.attr("disabled", true);
        addABtn.attr("disabled", true);
    }

    var scope = this;

    addABtn.click(function () {
        addActionAndFocus(scope.parentAction);
    });

    addBtn.click(function () {
        addControlAndFocus(scope.parentAction, scope);
    });

    htmlElement.append(drag);
    htmlElement.append(content);
    htmlElement.append(imgGrp);
    htmlElement.append(btnGrp);
    htmlElement.data("item", this);

    setPlaceholderControl(htmlElement);
    setPlaceholderCondition(htmlElement);
    listenEnterKeypressWhenFocusingOnDescription(htmlElement);

    if (afterControl == undefined) {
        this.parentAction.html.append(htmlElement);
    } else {
        afterControl.html.after(htmlElement);
    }
    this.refreshSort();
};

Control.prototype.setStep = function (stepId) {
    this.stepId = stepId;
};

Control.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Control.prototype.getControl = function () {
    return this.control;
}

Control.prototype.setControlSequence = function (controlSequence) {
    this.controlSequence = controlSequence;
}

Control.prototype.setControl = function (control) {
    this.control = control;
};

Control.prototype.setParentActionSort = function (parentActionSort) {
    this.parentActionSort = parentActionSort;
};

Control.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Control.prototype.refreshSort = function () {
    this.html.find("#labelDiv").text(this.parentActionSort);
    this.html.find("#labelControlDiv").text(this.sort);
};

Control.prototype.generateContent = function () {
    var obj = this;
    var doc = new Doc();
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group");
    var secondRow = $("<div></div>").addClass("fieldRow row secondRow input-group");
    secondRow.css("width", "120%");
    var thirdRow = $("<div></div>").addClass("fieldRow row").hide();

    var controls = $("<select></select>").addClass("form-control input-sm").css("width", "100%");
    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input class='description form-control' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_control") + "'>");
    descContainer.append($("<span class='input-group-addon' style='font-weight: 700;' id='labelDiv'></span>"));
    descContainer.append($("<span class='input-group-addon' style='font-weight: 700;' id='labelControlDiv'></span>"));
    descContainer.append(descriptionField);
    var controlValue1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm").css("width", "100%");
    var controlValue2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm").css("width", "100%");
    var controlValue3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm").css("width", "100%");

    var controlconditionoperator = $("<select></select>").addClass("form-control input-sm");
    var controlconditionval1 = $("<input>").attr("type", "text").addClass("form-control input-sm");
    var controlconditionval2 = $("<input>").attr("type", "text").addClass("form-control input-sm");
    var controlconditionval3 = $("<input>").attr("type", "text").addClass("form-control input-sm");
    var fatalList = $("<select></select>").addClass("form-control input-sm");

    descriptionField.val(this.description);
    descriptionField.css("width", "100%");
    descriptionField.on("change", function () {
        setModif(true);
        obj.description = descriptionField.val();
    });

    controlconditionoperator = getSelectInvariant("CONTROLCONDITIONOPERATOR", false, true).css("width", "100%").attr("id", "controlConditionSelect");
    controlconditionoperator.on("change", function () {
        if (obj.conditionOperator !== controlconditionoperator.val()) {
            setModif(true);
        }
        obj.conditionOperator = controlconditionoperator.val();
        setPlaceholderCondition($(this).parents(".control"));

    });
    controlconditionoperator.val(this.conditionOperator).trigger("change");

    controlconditionval1.val(this.conditionVal1);
    controlconditionval1.css("width", "100%");
    controlconditionval1.on("change", function () {
        setModif(true);
        obj.conditionVal1 = controlconditionval1.val();
    });

    controlconditionval2.val(this.conditionVal2);
    controlconditionval2.css("width", "100%");
    controlconditionval2.on("change", function () {
        setModif(true);
        obj.conditionVal2 = controlconditionval2.val();
    });

    controlconditionval3.val(this.conditionVal3);
    controlconditionval3.css("width", "100%");
    controlconditionval3.on("change", function () {
        setModif(true);
        obj.conditionVal3 = controlconditionval3.val();
    });


    controls = getSelectInvariant("CONTROL", false, true).attr("id", "controlSelect");
    controls.val(this.control);
    controls.css("width", "100%");
    controls.on("change", function () {
        setModif(true);
        obj.control = controls.val();
        setPlaceholderControl($(this).parents(".control"));
    });

    controlValue1Field.val(this.value1);
    controlValue1Field.css("width", "84%")
    controlValue1Field.on("change", function () {
        setModif(true);
        obj.value1 = controlValue1Field.val();
    });

    controlValue2Field.val(this.value2);
    controlValue2Field.css("width", "84%");
    controlValue2Field.on("change", function () {
        setModif(true);
        obj.value2 = controlValue2Field.val();
    });

    controlValue3Field.val(this.value3);
    controlValue3Field.css("width", "84%");
    controlValue3Field.on("change", function () {
        setModif(true);
        obj.value3 = controlValue3Field.val();
    });

    fatalList = getSelectInvariant("CTRLFATAL", false, true);
    fatalList.val(this.fatal);
    fatalList.css("width", "100%");
    fatalList.on("change", function () {
        setModif(true);
        obj.fatal = fatalList.val();
    });

    firstRow.append(descContainer);

    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "control_field"))).append(controls));
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(controlValue1Field));
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(controlValue2Field));
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value3_field"))).append(controlValue3Field));

    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(controlconditionval1));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(controlconditionval2));
    thirdRow.append($("<div></div>").addClass("col-lg-2 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(controlconditionval3));
    thirdRow.append($("<div></div>").addClass("col-lg-1 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "fatal_field"))).append(fatalList));

    thirdRow.prepend($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_operation_field"))).append(controlconditionoperator));


    if ((this.parentStep.isUsedStep === "Y") || (!obj.hasPermissionsUpdate)) {
        descriptionField.prop("readonly", true);
        controlValue1Field.prop("readonly", true);
        controlValue2Field.prop("readonly", true);
        controlValue3Field.prop("readonly", true);
        controls.prop("disabled", "disabled");
        fatalList.prop("disabled", "disabled");
        controlconditionoperator.prop("disabled", "disabled");
        controlconditionval1.prop("readonly", true);
        controlconditionval2.prop("readonly", true);
        controlconditionval3.prop("readonly", true);
    }

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);

    return content;
};

Control.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.sequence = this.sequence;
    json.control = this.control;
    json.sort = this.sort;
    json.description = this.description;
    json.controlSequence = this.controlSequence;
    json.value1 = this.value1;
    json.value2 = this.value2;
    json.value3 = this.value3;
    json.fatal = this.fatal;
    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal3 = this.conditionVal3;

    json.screenshotFileName = this.screenshotFileName;

    return json;
};

/**
 * Call Add Action and focus to next description when focusing on description
 * and clicking on enter
 *
 * @returns {undefined}
 */
function listenEnterKeypressWhenFocusingOnDescription(element) {
    $(element).find("input[class='description form-control']").each(function (index, field) {
        $(field).off('keydown');
        $(field).on('keydown', function (e) {
            if (e.which === 13) {
                // if description is not empty, create new action
                if ($(field)[0].value.length !== 0) {
                    addActionAndFocus();
                } else {
                    // if description is empty, create action or control
                    // depending on field
                    if ($(field).closest(".step-action").hasClass("action")) {
                        var newAction = $(field).closest(".action-group");
                        var oldAction = newAction.prev().find(".step-action.row.action").last();
                        newAction.remove();
                        addControlAndFocus(oldAction);
                    } else {
                        var newAction = $(field).closest(".step-action");
                        newAction.remove();
                        addActionAndFocus();
                    }
                }
            }
        });
    });
}

function addControl(action, control) {
    setModif(true);
    var act;
    if (action instanceof Action) {
        act = action;
    } else {
        act = action.data("item");
    }

    var ctrl = new Control(null, act, true);
    act.setControl(ctrl, control, true);
    setAllSort();
    return ctrl;
}

function addControlAndFocus(oldAction, control) {
    $.when(addControl(oldAction, control)).then(function (action) {
        $($(action.html[0]).find(".description")[0]).focus();
    });
}

/**
 * Find into tag array if object exist
 *
 * @param tagToUse
 * @param label
 *            string to search
 *
 * @return a boolean : true if exist, false if not exist
 */
function objectIntoTagToUseExist(tagToUse, label) {
    for (var i = 0; i < tagToUse.array.length; i++) {
        var data = tagToUse.array[i];

        if (data === undefined) {
            continue;
        }

        if (typeof data === "string") {
            if (data === label)
                return true;
        } else {
            if (data.object === label) {
                return true;
            } else if (data.service === label) {
                return true;
            }
        }
    }
    return false;
}

function loadGuiProperties() {

    let propArr = new Object();

    $("div.list-group-item").each(function () {
        var editor = ace.edit($(this).find("pre").attr("id"));
        let info = new Object();
        info["name"] = $(this).find("#propName").val()
        info["type"] = $(this).find("select").val();
        info["value"] = editor.getValue();
        if (!($(this).find("#propName").val() in propArr)) {
            propArr[$(this).find("#propName").val()] = info;
        }
    })
    return propArr;
}

var autocompleteAllFields, getTags, setTags, handlerToDeleteOnStepChange = [];

(function () {
    // var accessible only in closure
    var TagsToUse = [];
    var tcInfo = [];
    var contextInfo = [];

    getTags = function () {
        return TagsToUse;
    };
    setTags = function (tags) {
        TagsToUse = tags;
    };

    // function accessible everywhere that has access to TagsToUse
    autocompleteAllFields = function (configs, context, Tags) {
        if (Tags !== undefined) {
            TagsToUse = Tags;
        }

        if (configs !== undefined) {
            tcInfo = configs.property.contentTable[0];
        }

        if (context !== undefined) {
            contextInfo = context;
        }

        $(document).on('focus', ".content div.fieldRow input:not('.description')", function (e) {
            let currentAction = $(this).parent().parent().find("#actionSelect").val();
            if (currentAction === "callService" || currentAction === "calculateProperty") {
                initAutocompleteforSpecificFields($(this));
            } else {
                initAutocompleteWithTags($(this), configs, contextInfo);
            }
        })

        $(document).on('settingsButton', ".content div.fieldRow input:not('.description')", function (e) {
            var doc = new Doc();
            let currentAction = $(this).parent().parent().find("#actionSelect").val();
            let htmlElement = $(this);
            $(htmlElement).parent().find(".input-group-btn").remove();
            switch (currentAction) {
                case 'callService':
                    if (htmlElement.val()) {
                        $.ajax({
                            url: "ReadAppService?service=" + encodeURI(htmlElement.val()),
                            dataType: "json",
                            success: function (data) {
                                var dataContent = data.contentTable;
                                if ($(htmlElement).parent().find(".v1").val() !== undefined) {
                                    if (dataContent !== undefined) {
                                        var editEntry = $('<span class="input-group-btn ' + encodeURIComponent(htmlElement.val()) + '"><button id="editEntry" onclick="openModalAppService(\'' + encodeURIComponent(htmlElement.val()) + '\',\'EDIT\'  ,\'TestCase\' );"\n\
        								class="buttonObject btn btn-default input-sm " \n\
        								title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
        						<span class="glyphicon glyphicon-pencil"></span></button></span>');
                                        $(htmlElement).parent().append(editEntry);
                                    } else {
                                        var addEntry = '<span class="input-group-btn ' + encodeURIComponent(htmlElement.val()) + '"><button id="editEntry" onclick="openModalAppService(\'' + encodeURIComponent(htmlElement.val()) + '\',\'ADD\'  ,\'TestCase\' );"\n\
        						class="buttonObject btn btn-default input-sm " \n\
        						title="' + doc.getDocLabel("page_applicationObject", "button_create") + '" type="button">\n\
        						<span class="glyphicon glyphicon-plus"></span></button></span>';
                                        $(htmlElement).parent().append(addEntry);
                                    }
                                }
                            }
                        });
                    }
                    break;
                case 'calculateProperty':
                    let data = loadGuiProperties()
                    var viewEntry = $('<span class="input-group-btn ' + $(htmlElement).val() + '"><button id="editEntry" data-toggle="modal" data-target="#modalProperty" "\n\
        				class="buttonObject btn btn-default input-sm " \n\
        				title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
        				<span class="glyphicon glyphicon-eye-open"></span></button></span>');
                    if (data[$(htmlElement).val()]) {
                        viewEntry.find("button").off("click").on("click", function () {
                            let firstRow = $('<p style="text-align:center" > Type : ' + data[$(htmlElement).val()].type + '</p>');
                            let secondRow = $('<p style="text-align:center"> Value : ' + data[$(htmlElement).val()].value + '</p>');
                            $("#modalProperty").find("h5").text("test");
                            $("#modalProperty").find("#firstRowProperty").find("p").remove();
                            $("#modalProperty").find("#secondRowProperty").find("p").remove();
                            $("#modalProperty").find("#firstRowProperty").append(firstRow);
                            $("#modalProperty").find("#secondRowProperty").append(secondRow);
                        });
                        $(htmlElement).parent().append(viewEntry);
                    }
                    break;
                default:
                    var name = undefined;
                    var nameNotExist = undefined;
                    var objectNotExist = false;
                    var typeNotExist = undefined;
                    var doc = new Doc();
                    var checkObject = [];
                    var betweenPercent = $(htmlElement).val().match(new RegExp(/%[^%]*%/g));
                    if (betweenPercent !== null && betweenPercent.length > 0) {
                        var i = betweenPercent.length - 1;
                        while (i >= 0) {
                            var findname = betweenPercent[i].match(/\.[^\.]*(\.|.$)/g);
                            if (betweenPercent[i].startsWith("%object.") && findname !== null && findname.length > 0) {
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                $(htmlElement).parent().parent().parent().parent().find("#ApplicationObjectImg").attr("src", "ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name + "&time=" + new Date().getTime());
                                if (!objectIntoTagToUseExist(TagsToUse[1], name)) {
                                    var addEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'ADD\'  ,\'testCaseScript\' );"\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-plus"></span></button></span>');
                                    objectNotExist = true;
                                    nameNotExist = name;
                                    typeNotExist = "applicationObject";
                                    $(htmlElement).parent().append(addEntry);
                                } else if (objectIntoTagToUseExist(TagsToUse[1], name)) {
                                    var editEntry = '<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'EDIT\'  ,\'testCaseScript\' );"\n\
	                                class="buttonObject btn btn-default input-sm " \n\
	                                title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-pencil"></span></button></span>';
                                    $(htmlElement).parent().append(editEntry);
                                }
                            } else if (betweenPercent[i].startsWith("%property.") && findname !== null && findname.length > 0) {
                                let data = loadGuiProperties();
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                if (objectIntoTagToUseExist(TagsToUse[2], name)) {
                                    var viewEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" data-toggle="modal" data-target="#modalProperty" "\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-eye-open"></span></button></span>');
                                    if (data[name]) {
                                        let property = name;
                                        viewEntry.find("button").on("click", function () {
                                            let firstRow = $('<p style="text-align:center" > Type : ' + data[property].type + '</p>');
                                            let secondRow = $('<p style="text-align:center"> Value : ' + data[property].value + '</p>');
                                            $("#modalProperty").find("#firstRowProperty").find("p").remove();
                                            $("#modalProperty").find("#secondRowProperty").find("p").remove();
                                            $("#modalProperty").find("#firstRowProperty").append(firstRow);
                                            $("#modalProperty").find("#secondRowProperty").append(secondRow);
                                            $("#modalProperty").find(".modal-title").html(property);
                                        });
                                        $(htmlElement).parent().append(viewEntry);
                                    }
                                }
                            }
                            i--;
                        }
                    }
            }
        });

        $(document).on('input', ".content div.fieldRow input:not('.description')", function (e) {
            let data = loadGuiProperties();
            if ($(this).parent().parent().find("select").val() === "callService") {
                let url = "ReadAppService?service=" + encodeURI($(this).val()) + "&limit=15";
                modifyAutocompleteSource($(this), url);
            } else if ($(this).parent().parent().find("select").val() === "calculateProperty") {
                modifyAutocompleteSource($(this), null, data);
            }
            $(this).trigger("settingsButton");
        });
        $("div.step-action .content div.fieldRow:nth-child(2) input").trigger("settingsButton");
    };
})();


function removeTestCaseClick(test, testCase) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcase", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", test + " / " + testCase);
    showModalConfirmation(deleteTestCaseHandlerClick, undefined, "Delete", messageComplete, test, testCase, "", "");
}

/*
 * Function called when confirmation button pressed @returns {undefined}
 */
function deleteTestCaseHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var testCase = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteTestCase", {test: test, testCase: testCase}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            window.location = "./TestCaseScript.jsp?test=" + encodeURI(test);
        }
        // show message in the main page
        showMessageMainPage(messageType, data.message, false);
        // close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function setPlaceholderAction(actionElement) {

    var user = getUser();
    var placeHolders = actionUIList[user.language];

    $(actionElement).find('select#actionSelect option:selected').each(function (i, e) {
        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].aval1 !== null) {
                    $(e).parent().parent().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol1);
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('label').text(placeHolders[i].aval1);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].aval2 !== null) {
                    $(e).parent().parent().next().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol2);
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('label').text(placeHolders[i].aval2);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
                if (placeHolders[i].aval3 !== null) {
                    $(e).parent().parent().next().next().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol3);
                    $(e).parent().parent().next().next().next().show();
                    $(e).parent().parent().next().next().next().find('label').text(placeHolders[i].aval3);
                } else {
                    $(e).parent().parent().next().next().next().hide();
                }
            }
        }
    });
}

function setPlaceholderCondition(conditionElement) {

    var user = getUser();
    var placeHolders = conditionUIList[user.language];


    if ($(conditionElement).find('select#conditionSelect option:selected').length) {
        $(conditionElement).find('select#conditionSelect option:selected').each(function (i, e) {
            for (var i = 0; i < placeHolders.length; i++) {
                if (placeHolders[i].type === e.value) {
                    if (placeHolders[i].object !== null) {
                        $(e).parent().parent().next().show();
                        $(e).parent().parent().next().find('label').text(placeHolders[i].object);
                    } else {
                        $(e).parent().parent().next().hide();
                    }
                    if (placeHolders[i].property !== null) {
                        $(e).parent().parent().next().next().show();
                        $(e).parent().parent().next().next().find('label').text(placeHolders[i].property);
                    } else {
                        $(e).parent().parent().next().next().hide();
                    }
                    if (placeHolders[i].condValue3 !== null) {
                        $(e).parent().parent().next().next().next().show();
                        $(e).parent().parent().next().next().next().find('label').text(placeHolders[i].condValue3);
                    } else {
                        $(e).parent().parent().next().next().next().hide();
                    }
                }
            }
        });
    } else if ($(conditionElement).children().find('select#stepConditionOperator option:selected').length) {
        $(conditionElement).children().find('select#stepConditionOperator option:selected').each(function (i, e) {
            for (var i = 0; i < placeHolders.length; i++) {
                if (placeHolders[i].type === e.value) {
                    if (placeHolders[i].object !== null) {
                        $(e).parent().parent().next().show();
                        $(e).parent().parent().next().find('label').text(placeHolders[i].object);
                    } else {
                        $(e).parent().parent().next().hide();
                    }
                    if (placeHolders[i].property !== null) {
                        $(e).parent().parent().next().next().show();
                        $(e).parent().parent().next().next().find('label').text(placeHolders[i].property);
                    } else {
                        $(e).parent().parent().next().next().hide();
                    }
                    if (placeHolders[i].condValue3 !== null) {
                        $(e).parent().parent().next().next().next().show();
                        $(e).parent().parent().next().next().next().find('label').text(placeHolders[i].condValue3);
                    } else {
                        $(e).parent().parent().next().next().next().hide();
                    }
                }
            }
        });
    } else if ($(conditionElement).find('select#controlConditionSelect option:selected').length) {
        $(conditionElement).find('select#controlConditionSelect option:selected').each(function (i, e) {
            for (var i = 0; i < placeHolders.length; i++) {
                if (placeHolders[i].type === e.value) {
                    if (placeHolders[i].object !== null) {
                        $(e).parent().parent().next().show();
                        $(e).parent().parent().next().find('label').text(placeHolders[i].object);
                    } else {
                        $(e).parent().parent().next().hide();
                    }
                    if (placeHolders[i].property !== null) {
                        $(e).parent().parent().next().next().show();
                        $(e).parent().parent().next().next().find('label').text(placeHolders[i].property);
                    } else {
                        $(e).parent().parent().next().next().hide();
                    }
                    if (placeHolders[i].condValue3 !== null) {
                        $(e).parent().parent().next().next().next().show();
                        $(e).parent().parent().next().next().next().find('label').text(placeHolders[i].condValue3);
                    } else {
                        $(e).parent().parent().next().next().next().hide();
                    }
                }
            }
        });
    }
}

function setPlaceholderControl(controlElement) {

    var user = getUser();
    var placeHolders = controlUIList[user.language];

    $(controlElement).find('select#controlSelect option:selected').each(function (i, e) {

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].value1 !== null) {
                    $(e).parent().parent().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol1);
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('label').text(placeHolders[i].value1);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].value2 !== null) {
                    $(e).parent().parent().next().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol2);
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('label').text(placeHolders[i].value2);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
                if (placeHolders[i].value3 !== null) {
                    $(e).parent().parent().next().next().next().removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders[i].acol3);
                    $(e).parent().parent().next().next().next().show();
                    $(e).parent().parent().next().next().next().find('label').text(placeHolders[i].value3);
                } else {
                    $(e).parent().parent().next().next().next().hide();
                }
                if (placeHolders[i].fatal !== null) {
                    $(e).parent().parent().next().next().next().next().show();
                } else {
                    $(e).parent().parent().next().next().next().next().hide();
                }
            }
        }
    });
}


function setPlaceholderProperty(propertyElement, property) {
    /**
     * Todo : GetFromDatabase Translate for FR
     */

    var user = getUser();
    var placeHolders = propertyUIList[user.language];

    $(propertyElement).find('select[name="propertyType"] option:selected').each(function (i, e) {


        function initChange() {

            if ($("#" + editor.container.id).parent().parent().find("[name='propertyType']").val() === "getFromDataLib") {
                $("#" + editor.container.id).parent().find('.input-group').remove();
                var escaped = encodeURIComponent(editor.getValue());
                if (!isEmpty(escaped)) {
                    $.ajax({
                        url: "ReadTestDataLib",
                        data: {
                            name: escaped,
                            limit: 15,
                            like: "N"
                        },
                        async: true,
                        method: "GET",
                        success: function (data) {
                            if (data.messageType === "OK") {
                                // Feed the data to the screen and manage
                                // authorities.
                                var service = data.contentTable;
                                if (service.length >= 2) {

                                    $("#" + editor.container.id).parent().find('.input-group').remove();
                                    $("#" + editor.container.id).parent().parent().find('.col-btn').remove();

                                    var editEntry = $('<div class="input-group col-sm-5 col-sm-offset-3"><label>Choose one data library</label><select class="datalib  form-control"></select><span class="input-group-btn"  style="vertical-align:bottom"><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-pencil"></span></button></span></div>');
                                    $("#" + editor.container.id).parent().append(editEntry);

                                    displayDataLibList(editor.container.id, undefined, data);
                                    $("#" + editor.container.id).parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + $("#" + editor.container.id).parent().find("select").val() + "\','EDIT'," + "'" + escaped + "')");
                                    $("#" + editor.container.id).parent().find("select").unbind("change").change(function () {
                                        $("#" + editor.container.id).parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + $("#" + editor.container.id).parent().find("select").val() + "\','EDIT'," + "'" + escaped + "')");
                                    });


                                } else {
                                    $("#" + editor.container.id).parent().find('.input-group').remove();
                                    $("#" + editor.container.id).parent().parent().find('.col-btn').remove();
                                    if (service.length === 1) {
                                        var editEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Edit the DataLib</label><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-pencil"></span></button></div>');
                                        var addEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Add the DataLib</label><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-plus"></span></button></div>');
                                        $("#" + editor.container.id).parent().removeClass("col-sm-10").addClass("col-sm-8");
                                        $("#" + editor.container.id).parent().parent().append(editEntry);
                                        $("#" + editor.container.id).parent().parent().append(addEntry);
                                        $("#" + editor.container.id).parent().parent().find("button:eq(0)").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + service[0].testDataLibID + "\','EDIT'," + "'" + escaped + "')");
                                        $("#" + editor.container.id).parent().parent().find("button:eq(1)").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + escaped + "\','ADD'," + "'" + escaped + "')");
                                    } else {
                                        var addEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Add the DataLib</label><button class="btn btn-secondary ' + escaped + '" type="button"><span class="glyphicon glyphicon-plus"></span></button></div>');
                                        addEntry.find("button").attr("disabled", !canUpdate);
                                        $("#" + editor.container.id).parent().removeClass("col-sm-10").addClass("col-sm-8");
                                        $("#" + editor.container.id).parent().parent().append(addEntry);
                                        $("#" + editor.container.id).parent().parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + escaped + "\','ADD'," + "'" + escaped + "')");
                                    }
                                }
                            }
                        },
                        error: showUnexpectedError
                    });
                }
            }
        }

        var editor = ace.edit($($(e).parents("div[name='propertyLine']").find("pre[name='propertyValue']"))[0]);

        editor.removeAllListeners('change');

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].database !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase'] label").html(placeHolders[i].database);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase']").hide();
                }
                if (placeHolders[i].value1 !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1'] label").html(placeHolders[i].value1);
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").removeClass();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").addClass(placeHolders[i].value1Class);
                    // Ace module management
                    configureAceEditor(editor, placeHolders[i].value1EditorMode, property);

                    if (placeHolders[i].type === "getFromDataLib") {
                        if ((editor.getValue() !== null)) {
                            initChange();
                        }
                        editor.on('change', initChange);

                    } else {
                        $("#" + editor.container.id).parent().children('.input-group').remove();
                        $("#" + editor.container.id).parent().parent().find('.col-btn').remove();
                    }

                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").hide();
                }
                if (placeHolders[i].value2 !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2'] label").html(placeHolders[i].value2);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2']").hide();
                }
                if (placeHolders[i].length !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength'] label").html(placeHolders[i].length);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength']").hide();
                }
                if (placeHolders[i].rowLimit !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit'] label").html(placeHolders[i].rowLimit);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit']").hide();
                }
                if (placeHolders[i].nature !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature'] label").html(placeHolders[i].nature);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature']").hide();
                }
                if (placeHolders[i].cacheExpire !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire'] label").html(placeHolders[i].cacheExpire);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire']").hide();
                }
                if (placeHolders[i].retry !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb'] label").html(placeHolders[i].retry);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb']").hide();
                }
                if (placeHolders[i].period !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod'] label").html(placeHolders[i].period);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").hide();
                }
                if (placeHolders[i].rank !== null) {
                    // condition will always be true
                    $(e).parents("div[name='propertyLine']").find("div[name='rank']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='rank'] label").html(placeHolders[i].rank);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").hide();
                }


            }
        }
    });

}


function CompleterForAllDataLib() {

    var langTools = ace.require("ace/ext/language_tools");
    langTools.setCompleters([]);// clear the autocompleter list

    var staticWordCompleter = {

        getCompletions: function (editor, session, pos, prefix, callback) {
            var escaped = encodeURIComponent(editor.getValue());
            $.getJSON("ReadTestDataLib?name=" + escaped + "&limit=15&like=Y", function (wordList) {
                callback(null, wordList.contentTable.map(function (ea) {
                    return {name: ea.name, value: ea.name, meta: "DataLib"};
                }));
            });
        }
    };

    langTools.addCompleter(staticWordCompleter);
}


var oldCompleters = null;

/*
 * main function of ace editor
 */
function configureAceEditor(editor, mode, property) {
    // command Name
    var commandNameForAutoCompletePopup = "cerberusPopup";
    var commandNameForIssueDetection = "cerberusIssueDetection";
    // event listenner
    editor.commands.on("afterExec", function (e) {
        var langTools = ace.require('ace/ext/language_tools');


        if (e.command.name === "insertstring" || e.command.name === "paste" || e.command.name === "backspace") {
            // recreate the array at each loop

            if (property.type === "getFromDataLib") {
                CompleterForAllDataLib();
                $("pre").off("input").on("input", function (e) {
                    editor.execCommand("startAutocomplete");
                });
                editor.setOptions({maxLines: 15, enableBasicAutocompletion: true, enableLiveAutocompletion: false});
            } else {
                editor.setOptions({maxLines: 15, enableBasicAutocompletion: true, enableLiveAutocompletion: false});
                var allKeyword = createAllKeywordList(getKeywordList("object"), getKeywordList("property"));
                // editor.completers = [allKeyword]
                if (e.command.name !== "backspace") {
                    addCommandForCustomAutoCompletePopup(editor, allKeyword, commandNameForAutoCompletePopup);
                    editor.commands.exec(commandNameForAutoCompletePopup);// set
                    // autocomplete
                    // popup*/
                } else {
                    addCommandToDetectKeywordIssue(editor, allKeyword, commandNameForIssueDetection);
                    editor.commands.exec(commandNameForIssueDetection);// set
                    // annotation
                }
            }
        }
        createGuterCellListenner(editor);
        property.value1 = editor.session.getValue();

    });

    // editor option
    editor.getSession().setMode(mode);
    editor.off('change');
    editor.setTheme("ace/theme/chrome");
    editor.$blockScrolling = "Infinity";// disable error message

    // set text previously input
    editor.setValue(property.value1);
    // lose focus when loaded
    var count = editor.getSession().getLength();
    editor.gotoLine(count, editor.getSession().getLine(count - 1).length);
}

/*
 * create an array of the current keyword with the keyword that precede them
 */
function createAllKeywordList(objectList, propertyList) {
    var availableObjectProperties = [
        "value",
        "picturepath",
        "pictureurl"
    ];
    var availableSystemValues = [
        "SYSTEM",
        "APPLI",
        "BROWSER", "ROBOT", "ROBOTDECLI", "SCREENSIZE",
        "APP_DOMAIN", "APP_HOST", "APP_CONTEXTROOT", "EXEURL", "APP_VAR1", "APP_VAR2", "APP_VAR3", "APP_VAR4",
        "ENV", "ENVGP",
        "COUNTRY", "COUNTRYGP1", "COUNTRYGP2", "COUNTRYGP3", "COUNTRYGP4", "COUNTRYGP5", "COUNTRYGP6", "COUNTRYGP7", "COUNTRYGP8", "COUNTRYGP9",
        "TEST",
        "TESTCASE",
        "SSIP", "SSPORT",
        "TAG",
        "EXECUTIONID",
        "EXESTART", "EXEELAPSEDMS",
        "EXESTORAGEURL",
        "STEP.n.n.RETURNCODE", "CURRENTSTEP_INDEX", "CURRENTSTEP_STARTISO", "CURRENTSTEP_ELAPSEDMS", "CURRENTSTEP_SORT",
        "LASTSERVICE_HTTPCODE",
        "TODAY-yyyy", "TODAY-MM", "TODAY-dd", "TODAY-doy", "TODAY-HH", "TODAY-mm", "TODAY-ss",
        "YESTERDAY-yyyy", "YESTERDAY-MM", "YESTERDAY-dd", "YESTERDAY-doy", "YESTERDAY-HH", "YESTERDAY-mm", "YESTERDAY-ss",
        "TOMORROW-yyyy", "TOMORROW-MM", "TOMORROW-dd", "TOMORROW-doy"
    ];
    var availableTags = [
        "property", // 0
        "object", // 1
        "system"    // 2
    ];

    var allKeyword = [];
    allKeyword.push({"motherKeyword": null, "listKeyword": availableTags});
    // property
    allKeyword.push({"motherKeyword": availableTags["0"], "listKeyword": propertyList});
    // object
    allKeyword.push({"motherKeyword": availableTags["1"], "listKeyword": objectList});
    // system
    allKeyword.push({"motherKeyword": availableTags["2"], "listKeyword": availableSystemValues});
    // object tag
    for (var i in objectList) {
        allKeyword.push({"motherKeyword": objectList[i], "listKeyword": availableObjectProperties});
    }
    return allKeyword;
}

/*
 * add an ace command to display autocomplete popup
 */
function addCommandForCustomAutoCompletePopup(editor, allKeyword, commandName) {


    editor.commands.addCommand({
        name: commandName,
        exec: function () {
            var cursorPositionY = editor.getCursorPosition().row;
            var editorValueAtTheLine = editor.session.getLine(cursorPositionY);
            // value on the line the cursor is currently in
            var numberOfPercentCaractereAtLine = (editorValueAtTheLine.match(/\%/g) || []).length;
            // start autocomplete when there is an odd number of %

            if (numberOfPercentCaractereAtLine !== 0 && numberOfPercentCaractereAtLine % 2 === 1) {
                var cursorPositionX = editor.getCursorPosition().column;
                var subStringCursorOn = editorValueAtTheLine.slice(editorValueAtTheLine.lastIndexOf('%', cursorPositionX) + 1, cursorPositionX);
                // Create an array of all the word separated by "." contain
                // between "%" caractere
                var keywordInputList = subStringCursorOn.split(".");


                var potentiallyNeddApoint = true;
                var allKeywordCorrect = true;
                // Check all the keywordInput
                for (var idKeywordToCheck in keywordInputList) {

                    var keywordInputByUserExist;
                    // Just after a "." or a blank line
                    if (keywordInputList[idKeywordToCheck] === "") {
                        keywordInputByUserExist = true;// blank is a valid
                        // keyword
                        keywordInputList.pop();// remove blank caractere
                        potentiallyNeddApoint = false;
                    } else {
                        keywordInputByUserExist = checkIfTheKeywordIsCorrect(allKeyword, keywordInputList, idKeywordToCheck);
                    }
                    // if at least on keyword between the "%" by default
                    // autocompletion is diable
                    if (keywordInputByUserExist === false)
                        allKeywordCorrect = false;

                }

                var currentKeyword = keywordInputList[keywordInputList.length - 1];
                // All the keyword are correct set autocompletion
                if (allKeywordCorrect) {
                    var idNextKeyword = getNextKeywordId(currentKeyword, allKeyword, keywordInputList);
                    // add the special caractere
                    if (potentiallyNeddApoint && currentKeyword !== undefined && idNextKeyword !== -1) {
                        editor.session.insert(editor.getCursorPosition(), ".");
                    }
                    if (currentKeyword !== undefined && idNextKeyword === -1) {
                        editor.session.insert(editor.getCursorPosition(), "%");
                    }
                    // change the autocompletionList

                    if (currentKeyword === undefined) {
                        changeAceCompletionList(allKeyword[0]["listKeyword"], "", editor);
                        editor.execCommand("startAutocomplete");
                    }
                    if (idNextKeyword !== -1 && currentKeyword !== undefined) {
                        changeAceCompletionList(allKeyword[idNextKeyword]["listKeyword"], allKeyword[idNextKeyword]["motherKeyword"], editor);
                        editor.execCommand("startAutocomplete");
                    }
                }
                // The user tryed to add an new object set autocompletion for
                // this specifique part
                if (!allKeywordCorrect && keywordInputList[0] === "object" && keywordInputList.length < 4) {
                    var availableObjectProperties = [
                        "value",
                        "picturepath",
                        "pictureurl"
                    ];
                    // if the user want to defined a new object
                    if (keywordInputList.length === 2 && potentiallyNeddApoint === false) {
                        changeAceCompletionList(availableObjectProperties, keywordInputList[1], editor);
                        editor.execCommand("startAutocomplete");
                    }
                    // add '%' when an availableObjectProperties was selected
                    if (keywordInputList.length === 3 && availableObjectProperties.indexOf(keywordInputList[2]) !== -1) {
                        editor.session.insert(editor.getCursorPosition(), "%");
                    }
                }
            }
        }
    });

}

/*
 * check if the keywordInputByUser and the keyword designated by the
 * idKeywordToCheck share the same motherKeyword (resolve issue with duplicate)
 */
function checkIfTheKeywordIsCorrect(allKeyword, keywordInputByUser, idKeywordToCheck) {

    for (var y in allKeyword) {
        for (var n in allKeyword[y]["listKeyword"]) {
            if (allKeyword[y]["listKeyword"][n] === keywordInputByUser[idKeywordToCheck]) {
                // check if the keyword matching posses the same mother keyword
                var listMotherKeywordPossible = getPossibleMotherKeyword(allKeyword[y]["listKeyword"][n], allKeyword);
                if (!(idKeywordToCheck >= 1 && listMotherKeywordPossible[0] !== null && getPossibleMotherKeyword(allKeyword[y]["listKeyword"][n], allKeyword).indexOf(keywordInputByUser[idKeywordToCheck - 1]) === -1)) {
                    return true;
                }
            }
        }
    }
    return false;
}

/*
 * Get the list of all the previous keyword possible for this keyword
 */
function getPossibleMotherKeyword(keyword, allKeyword) {
    var idmotherKeyword = [];
    for (var i in allKeyword) {
        for (var y in allKeyword[i]["listKeyword"]) {
            if (allKeyword[i]["listKeyword"][y] === keyword) {
                idmotherKeyword.push(allKeyword[i]["motherKeyword"]);
            }
        }
    }
    if (idmotherKeyword.length === 0)
        return -1;
    else
        return idmotherKeyword;
}

/*
 * Get the id of the next list of keyword by finding which one has keyword as a
 * motherKeyword
 */
function getNextKeywordId(keyword, allKeyword, keywordInputList) {
    // resolve issue with duplicate
    if (keywordInputList[0] !== "object" && keywordInputList.length === 2) {
        return -1;
    }
    // no duplicate
    else {
        var idCurrentKeyword = -1;
        for (i in allKeyword) {
            if (allKeyword[i]["motherKeyword"] === keyword) {
                idCurrentKeyword = i;
            }
        }
        return idCurrentKeyword;
    }
}

/*
 * Replace the autocompletion list of ace editor
 */
function changeAceCompletionList(keywordList, label, editor) {
    var langTools = ace.require("ace/ext/language_tools");
    langTools.setCompleters([]);// clear the autocompleter list
    var completer = {
        getCompletions: function (editor, session, pos, prefix, callback) {
            var completions = [];
            for (var i in keywordList) {
                completions.push({name: "default_name", value: keywordList[i], meta: label});
            }
            callback(null, completions);
        }
    };

    langTools.addCompleter(completer);
}

/*
 * Create a command to find and display (with annotation) the issue in ace
 */
function addCommandToDetectKeywordIssue(editor, allKeyword, commandName) {

    editor.commands.addCommand({
        name: commandName,
        exec: function () {
            var numberOfLine = editor.session.getLength();
            var annotationObjectList = [];
            // var warningKeywordList =[];
            for (var line = 0; line < numberOfLine; line++) {
                var editorValueAtTheLine = editor.session.getLine(line);
                var numberOfPercentCaractereAtLine = (editorValueAtTheLine.match(/\%/g) || []).length;
                if (numberOfPercentCaractereAtLine !== 0 && numberOfPercentCaractereAtLine % 2 === 0) {
                    var editorValueSplit = editorValueAtTheLine.split("%");
                    var cerberusVarAtLine = [];
                    for (var i = 0; i < editorValueSplit.length; i++) {
                        if (i % 2 === 1)
                            cerberusVarAtLine.push(editorValueSplit[i]);
                    }
                    // Check if each cerberus var is correct
                    for (var i in cerberusVarAtLine) {
                        var cerberusVarCurrentlyCheck = cerberusVarAtLine[i];
                        var keywordsListCurrentlyCheck = cerberusVarCurrentlyCheck.split(".");

                        var issueWithKeyword = "none";

                        if (keywordsListCurrentlyCheck.length >= 2) {
                            var startKeyword = keywordsListCurrentlyCheck[0];
                            var secondKeyword = keywordsListCurrentlyCheck[1];

                            if (startKeyword === "property" || startKeyword === "system" && keywordsListCurrentlyCheck.length === 2) {
                                if (getPossibleMotherKeyword(secondKeyword, allKeyword) === -1) {
                                    issueWithKeyword = "warning";
                                } else {
                                    if (getPossibleMotherKeyword(secondKeyword, allKeyword).indexOf(startKeyword) === -1)
                                        issueWithKeyword = "warning";
                                    // keyword exist but not correct
                                }
                            } else if (startKeyword === "object" && keywordsListCurrentlyCheck.length === 3) {

                                if (getPossibleMotherKeyword(secondKeyword, allKeyword) === -1) {
                                    issueWithKeyword = "warning";
                                } else {
                                    if (getPossibleMotherKeyword(secondKeyword, allKeyword).indexOf(startKeyword) === -1)
                                        issueWithKeyword = "warning";
                                    // keyword exist but not correct
                                }
                                var thirdKeyword = keywordsListCurrentlyCheck[2];
                                var availableObjectProperties = [
                                    "value",
                                    "picturepath",
                                    "pictureurl"
                                ];
                                if (availableObjectProperties.indexOf(thirdKeyword) === -1) {
                                    issueWithKeyword = "error";
                                }
                            } else {
                                issueWithKeyword = "error";
                            }
                        } else {
                            issueWithKeyword = "error";
                        }
                        if (issueWithKeyword === "error") {
                            var messageOfAnnotion = "error invalid keyword";
                            annotationObjectList.push(createAceAnnotationObject(line, messageOfAnnotion, "error", null, null));
                        }
                        if (issueWithKeyword === "warning") {
                            var messageOfAnnotion = "warning the " + keywordsListCurrentlyCheck[0] + " : " + keywordsListCurrentlyCheck[1] + " don't exist";
                            annotationObjectList.push(createAceAnnotationObject(line, messageOfAnnotion, "warning", keywordsListCurrentlyCheck[0], keywordsListCurrentlyCheck[1]));
                        }
                    }
                }
            }
            setAceAnnotation(editor, annotationObjectList);
        }
    });

}

/*
 * object use to highlight line
 */
function createAceAnnotationObject(lineNumber, annotationText, annotationType, keywordTypeVar, keywordValueVar) {

    return {
        row: lineNumber,
        column: 0,
        text: annotationText,
        type: annotationType,
        lineNumber: lineNumber,
        keywordType: keywordTypeVar,
        keywordValue: keywordValueVar
    };
}

// set the list of ace annotion object as annotation
function setAceAnnotation(editor, annotationObjectList) {
    // Set annotation replace all the annotation so if you use it you need to
    // resend every annotation for each change
    editor.getSession().setAnnotations(annotationObjectList);
}

/*
 * Set a listenner for every left part of ace's lines in each line that will
 * resolve issue
 */
function createGuterCellListenner(editor) {

    var currentEditorGutter = editor.container.getElementsByClassName("ace_gutter")[0];
    var cellList = currentEditorGutter.getElementsByClassName("ace_gutter-cell");
    for (var i = 0; i < cellList.length; i++) {

        cellList[i].setAttribute("style", "cursor: pointer");
        cellList[i].onclick = function () {

            var lineClickedId = this.innerHTML - 1;// start at 1
            var annotationObjectList = editor.getSession().getAnnotations();

            for (var y = 0; y < annotationObjectList.length; y++) {
                if (annotationObjectList[y].lineNumber === lineClickedId && annotationObjectList[y].type === "warning") {

                    var keywordType = annotationObjectList[y].keywordType;
                    var keywordValue = annotationObjectList[y].keywordValue;
                    if (keywordType === "property") {
                        addPropertyWithAce(keywordValue);
                    }
                    if (keywordType === "object") {
                        addObjectWithAce(keywordValue);
                    }
                }
            }
            this.className = "ace_gutter-cell";// Remove the warning annotation
        };
    }
}

// Add keywordValue as a new property
function addPropertyWithAce(keywordValue) {

    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    var info = GetURLParameter("testcase");
    var property = GetURLParameter("property");

    $.ajax({
        url: "ReadTestCase",
        data: {test: test, testCase: testcase, withSteps: true},
        dataType: "json",
        success: function (data) {

            testCaseObject = data.contentTable[0];
            loadTestCaseInfo(testCaseObject);

            var myCountry = [];
            $.each(testCaseObject.countries, function (index) {
                myCountry.push(index);
            });
            // Store the current saveScript button status and disable it
            var saveScriptOldStatus = $("#saveScript").attr("disabled");
            $("#saveScript").attr("disabled", true);

            var newProperty = {
                property: keywordValue,
                description: "",
                country: myCountry,
                type: "text",
                database: "",
                value1: "",
                value2: "",
                length: 0,
                rowLimit: 0,
                nature: "STATIC",
                retryNb: "",
                retryPeriod: "",
                toDelete: false,
                rank: 1
            };

            var prop = drawProperty(newProperty, testCaseObject, true, $("div[name='propertyLine']").length);
            setPlaceholderProperty(prop[0], prop[1]);

            // Restore the saveScript button status
            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
        }
    });
    getKeywordList("property").push(keywordValue);
}

// Add keywordValue as a new object
function addObjectWithAce(keywordValue) {

    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    var info = GetURLParameter("testcase");

    $.ajax({
        url: "ReadTestCase",
        data: {test: test, testCase: testcase, withSteps: true},
        dataType: "json",
        success: function (data) {
            // Store the current saveScript button status and disable it
            var saveScriptOldStatus = $("#saveScript").attr("disabled");
            $("#saveScript").attr("disabled", true);

            var applicationName = data.contentTable[0].application;
            addApplicationObjectModalClick(undefined, keywordValue, applicationName);

            // Restore the saveScript button status
            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
        }
    });
}

// Get the CURRENT list of keyword for each type
function getKeywordList(type) {
    if (getTags() !== undefined && getTags().length > 0) {
        var idType = -1;
        switch (type) {
            case "object":
                return getTags()[1].array;
            case "property":
                return getTags()[2].array;
            case "system":
                return getTags()[3].array;
                break;
            default:
                return null;
        }
    } else {
        return null;
    }
}

function tec_keyispressed(e) {
    var toto = "|.| |(|)|%|";
    var charval = "|" + e.key + "|";
    if (toto.indexOf(charval) !== -1) {
        showMessageMainPage("warning", "Character '" + e.key + "' is not allowed on subdata name. This is to avoid creating ambiguous syntax when using variabilization.", false, 4000);
        return false;
    }
    return true;
}
