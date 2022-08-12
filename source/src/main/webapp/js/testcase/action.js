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

var actionUIList = {
    "fr": [
        {"type": "Unknown", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "click", "aval1": "Chemin vers l'élement à cliquer", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "longPress", "aval1": "Chemin vers l'élement à cliquer", "acol1": "col-lg-6", "aval2": "[opt] Valeur (ms) : 8000 par défaut", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonPress", "aval1": "Chemin vers l'élement à cibler", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonRelease", "aval1": "Chemin vers l'élement", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doubleClick", "aval1": "Chemin vers l'élement à double-cliquer", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "rightClick", "aval1": "Chemin vers l'élement à clicker avec le bouton droit", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOver", "aval1": "Chemin vers l'élement", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseMove", "aval1": "Coordonnées relatives (ex : 50,100 ; 200,50)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusToIframe", "aval1": "Chemin vers l'élement de l'iFrame à cibler", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusDefaultIframe", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "switchToWindow", "aval1": "Titre ou url de la fenêtre", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialog", "aval1": "ok ou cancel", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialogKeypress", "aval1": "Touches à appuyer.", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlWithBase", "aval1": "URI à appeler (ex : /index.html)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlLogin", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrl", "aval1": "URL à appeler (ex : http://www.domain.com)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "refreshCurrentPage", "aval1": null, "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeJS", "aval1": "JavaScript à executer", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCommand", "aval1": "Commande Appium (ex : \"mobile:deepLink\")", "acol1": "col-lg-4", "aval2": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCerberusCommand", "aval1": "Commande (ex : \"grep\")", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openApp", "aval1": "Nom ou chemin de l'application, package pour android", "acol1": "col-lg-5", "aval2": "[Optionnel, obligatoire pour Android] Activity", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "closeApp", "aval1": "Nom ou chemin de l'application", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "dragAndDrop", "aval1": "Chemin de l'élement", "acol1": "col-lg-4", "aval2": "Destination de l'élément", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "select", "aval1": "Chemin vers l'élement", "acol1": "col-lg-4", "aval2": "Chemin vers l'option", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "keypress", "aval1": "[opt] Chemin vers l'élement à cibler", "acol1": "col-lg-6", "aval2": "Touche à appuyer", "acol2": "col-lg-5", "aval3": "[opt] Touche modificatrice", "acol3": "col-lg-3"},
        {"type": "type", "aval1": "Chemin vers l'élement", "acol1": "col-lg-4", "aval2": "Texte à entrer", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "hideKeyboard", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "clearField", "aval1": "Chemin vers l'élement à effacer", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "swipe", "aval1": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "acol1": "col-lg-4", "aval2": "Direction x;y;z;y", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "wait", "aval1": "Valeur (ms) ou élement", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitVanish", "aval1": "Element", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitNetworkTrafficIdle", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "callService", "aval1": "Nom du Service", "acol1": "col-lg-4", "aval2": "Nb Evt à attendre (Kafka)", "acol2": "col-lg-2", "aval3": "Tps d'attente en sec (Kafka)", "acol3": "col-lg-2"},
        {"type": "executeSqlUpdate", "aval1": "Nom de Base de donnée", "acol1": "col-lg-3", "aval2": "Script à executer", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeSqlStoredProcedure", "aval1": "Nom de Base de donnée", "acol1": "col-lg-3", "aval2": "Procedure Stoquée à executer", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "cleanRobotFile", "aval1": "Chemin/Fichers à vider", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "uploadRobotFile", "aval1": "Nom du ficher à créer", "acol1": "col-lg-3", "aval2": "Contenu à uploader (base64)", "acol2": "col-lg-3", "aval3": "Option", "acol3": "col-lg-2"},
        {"type": "getRobotFile", "aval1": "Chemin/Fichers à recuperer", "acol1": "col-lg-5", "aval2": "Nb de fichiers", "acol2": "col-lg-2", "aval3": "Option", "acol3": "col-lg-3"},
        {"type": "calculateProperty", "aval1": "Nom d'une Proprieté", "acol1": "col-lg-4", "aval2": "[opt] Nom d'une autre propriété", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setNetworkTrafficContent", "aval1": "URL à filtrer", "acol1": "col-lg-6", "aval2": "Activation du contenu des reponses http (Y/N)", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "indexNetworkTraffic", "aval1": "[opt] Nom de l'index", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setServiceCallContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setConsoleContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setContent", "aval1": "Valeur", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "scrollTo", "aval1": "element (id, xpath, ..., et text=)", "acol1": "col-lg-5", "aval2": "Nombre maximum de scroll vers le bas (8 par defaut)", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "installApp", "aval1": "Chemin vers l'application (ex : /root/toto.apk)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeApp", "aval1": "Package de l'application (ex : com.cerberus.appmobile)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doNothing", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOverAndWait", "aval1": "Action Depreciée", "acol1": "col-lg-4", "aval2": "Action Depreciée", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeDifference", "aval1": "Action Depreciée", "acol1": "col-lg-4", "aval2": "Action Depreciée", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"}
    ], "en": [
        {"type": "Unknown", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "click", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "", "aval3": null, "acol3": ""},
        {"type": "longPress", "aval1": "Element path", "acol1": "col-lg-6", "aval2": "[opt] Duration (ms) : 8000 by default", "acol2": "col-lg-2", "aval3": null, "acol3": ""},
        {"type": "mouseLeftButtonPress", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseLeftButtonRelease", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doubleClick", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "rightClick", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOver", "aval1": "Element path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseMove", "aval1": "Relative coord. (ex : 50,100 ; 200,50)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusToIframe", "aval1": "Element path of the target iFrame", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "focusDefaultIframe", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "switchToWindow", "aval1": "Window title or url", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialog", "aval1": "ok or cancel", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "manageDialogKeypress", "aval1": "keys to press.", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlWithBase", "aval1": "URI to call  (ex : /index.html)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrlLogin", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openUrl", "aval1": "URL to call (ex : http://www.domain.com)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "refreshCurrentPage", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeJS", "aval1": "JavaScript to execute", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCommand", "aval1": "Appium Command (ex : \"mobile:deepLink\")", "acol1": "col-lg-4", "aval2": "Arguments (ex : {url: \"www.site.com\", package: \"com.Package\"})", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeCerberusCommand", "aval1": "Command (ex : \"grep\")", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "openApp", "aval1": "Application name or path or package for Android", "acol1": "col-lg-5", "aval2": "[Optional, required for Android] Activity", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "closeApp", "aval1": "Application name or path", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "dragAndDrop", "aval1": "Element path", "acol1": "col-lg-4", "aval2": "Destination Element Path", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "select", "aval1": "Element path", "acol1": "col-lg-5", "aval2": "Option value", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "keypress", "aval1": "[opt] Target element path", "acol1": "col-lg-6", "aval2": "Key to press", "acol2": "col-lg-5", "aval3": "[opt] Modifier to press", "acol3": "col-lg-3"},
        {"type": "type", "aval1": "Element path", "acol1": "col-lg-5", "aval2": "Text to type", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "hideKeyboard", "aval1": null, "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "clearField", "aval1": "Element path to Clear", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "swipe", "aval1": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "acol1": "col-lg-5", "aval2": "Direction x;y;z;y", "acol2": "col-lg-3", "aval3": null, "acol3": "col-lg-5"},
        {"type": "wait", "aval1": "Duration(ms) or Element", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitVanish", "aval1": "Element", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "waitNetworkTrafficIdle", "aval1": null, "acol1": "col-lg-9", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "callService", "aval1": "Service Name", "acol1": "col-lg-4", "aval2": "Nb Evt (Kafka)", "acol2": "col-lg-2", "aval3": "Evt Wait sec (Kafka)", "acol3": "col-lg-2"},
        {"type": "executeSqlUpdate", "aval1": "Database Name", "acol1": "col-lg-3", "aval2": "Script", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "executeSqlStoredProcedure", "aval1": "Database Name", "acol1": "col-lg-3", "aval2": "Stored Procedure", "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "cleanRobotFile", "aval1": "Path/Pattern to empty", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "uploadRobotFile", "aval1": "Filename to create", "acol1": "col-lg-3", "aval2": "Content to upload (base64)", "acol2": "col-lg-3", "aval3": "Option", "acol3": "col-lg-2"},
        {"type": "getRobotFile", "aval1": "Path/Pattern to retrieved", "acol1": "col-lg-5", "aval2": "Nb of files", "acol2": "col-lg-2", "aval3": "Option", "acol3": "col-lg-3"},
        {"type": "calculateProperty", "aval1": "Property Name", "acol1": "col-lg-4", "aval2": "[opt] Name of an other property", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setNetworkTrafficContent", "aval1": "url to filter", "acol1": "col-lg-6", "aval2": "Activate http response content (Y/N)", "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "indexNetworkTraffic", "aval1": "[opt] Index name", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setServiceCallContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setConsoleContent", "aval1": null, "acol1": "col-lg-7", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "setContent", "aval1": "Value to Set", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-2", "aval3": null, "acol3": "col-lg-5"},
        {"type": "scrollTo", "aval1": "element ('id=ressource-id'. Empty if you want use text)", "acol1": "col-lg-4", "aval2": "text (empty if you want use element)", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "installApp", "aval1": "Application path (ex : /root/toto.apk)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeApp", "aval1": "Application package (ex : com.cerberus.appmobile)", "acol1": "col-lg-8", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "doNothing", "aval1": null, "acol1": "col-lg-5", "aval2": null, "acol2": "col-lg-5", "aval3": null, "acol3": "col-lg-5"},
        {"type": "mouseOverAndWait", "aval1": "[Deprecated]", "acol1": "col-lg-4", "aval2": "[Deprecated]", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"},
        {"type": "removeDifference", "aval1": "[Deprecated]", "acol1": "col-lg-4", "aval2": "[Deprecated]", "acol2": "col-lg-4", "aval3": null, "acol3": "col-lg-5"}
    ]
};
