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

$(document).ready(function () {

    if(getUrlParameter("tutorielId") !== undefined) {
        let tutorielId = getUrlParameter("tutorielId");
        console.log(tutorielId);
        let startStep = getUrlParameter("startStep");
        interractiveTutorial(startStep);
    }

});

function interractiveTutorial(startStep=1) {console.log("coucou");
    let cerberusTuto = new CerberusTuto("firstStepAdmin");

    // Page d'accueil
    cerberusTuto.addGeneralMessage("Bienvenue dans Cerberus ! Je vois que c'est ta première connexion, veux tu que je te guide dans tes premiers pas ?");
    cerberusTuto.addGeneralMessage("Bienvenue sur la page d'accueil de cerberus ! <b>Sur cette page d’accueil, tu trouveras des informations sur</b>" +
        "<ul>" +
        "   <li>les cas de tests par application regroupés par status</li>" +
        "   <li>les dernières executions par tag</li>" +
        "   <li>les versions des applications déployées par environnement</li>" +
        "</ul>");

    cerberusTuto.addGeneralMessage("TODO, 2 choix = rediriger sur tuto admin ou tuto création dun cas de test. Pour le moment juste l'admin est créé");

    cerberusTuto.addMessageAndChangePageAfterClick("#sidebar", "Vous êtes administrateur ! La 1ere étape de configuration est de créer un système. " +
        "Un <strong>système</strong> est une application métier ou CI. Rendez vous dans le menu <b>Administration/Invariants.</b>","#menuInvariants");

    // Page invariant
    cerberusTuto.addMessage("#createInvariantButton", "Clique sur Creer un invariant");
    cerberusTuto.addMessage("#idname", "Selectionne SYSTEM- dans la liste");
    cerberusTuto.addMessage("#value", "Donne un nom à ton systeme");
    cerberusTuto.addMessage("#addInvariantButton", "Et valide ton nouveau systeme");
    cerberusTuto.addMessageAndChangePageAfterClick("#sidebar", "Voilà, ton sytème est créé ! Prochaine étape : il faut créez un environnement. L'<b>environement</b>" +
        " represente une plateforme de test, ex : INTEGRATION ou PREPRODUCTION." +
        " Rendez-vous dans le menu <b>Integration/Environment</b>", "#menuEnvironments");

    // page environement
    cerberusTuto.addMessage("#createEnvButton", "Clique sur <b>Creer un environement</b>");
    cerberusTuto.addMessage("#system", "Selectionne le système que tu viens de creer");
    cerberusTuto.addMessage("#country", "Selectionne le pays - TODO recuperer l'aide du pays");
    cerberusTuto.addMessage("#environment", "Selectionne l'environnement sur lequel tu veux faire tourner le système");
    cerberusTuto.addMessage("#description", "Ajoute une description de l'environnement");
    cerberusTuto.addMessage("#addEnvButton", "Et valide l'environement");
    cerberusTuto.addMessageAndChangePageAfterClick("#sidebar", "Prochaine étape : vérifier qu'un type de déploiement existe. Rendez vous dans <b>Application/Type de deploiement</b>", "#menuDeployType");

    // type de deploiement
    cerberusTuto.addMessage("#createDeployTypeButton", "Creer un nouveau type de deploiement");
    cerberusTuto.addMessage("#deployType", "Renseigne le nom du type de deploiement");
    cerberusTuto.addMessage("#Description", "Decrit le type de deploiement");
    cerberusTuto.addMessage("#addEntryButton", "Et sauvegarde le type de deploiment");
    cerberusTuto.addMessageAndChangePageAfterClick("#sidebar", "Dernière étape : créer une application. Rendez vous dans <b>Application/Application</b>", "a#menuApplications");


    // page application
    cerberusTuto.addMessage("#createApplicationButton", "Clique sur <b>Creer une application</b>");
    cerberusTuto.addMessage("#application", "Entre le nom de l'application que tu veux tester");
    cerberusTuto.addMessage("#description", "Entre une description");
    cerberusTuto.addMessage("#type", "Entre le type d'application (web, mobile, client riche etc...)");
    cerberusTuto.addMessage("#system", "Selectionne le système");
    cerberusTuto.addMessage("#deploytype", "Selectionne le type de deploiement prevu pour l'application");
    cerberusTuto.addMessage("#addApplicationButton", "Et valide l'application !");

    cerberusTuto.addGeneralMessage("Bravo! Tu es prêt à creer tes premiers cas de test sur l'application que tu viens de créer. >>> Passer au tuto suivant");


    cerberusTuto.start(startStep);
}

class CerberusTuto {

    constructor(tutorialId) {
        this.tutorialId=tutorialId;
        this.listMessage = new Array();
        this.cpt=1;
    }

    addGeneralMessage(messageStr) {
        let message = {
            intro : messageStr,
            step : this.cpt
        };
        this.listMessage.push(message);
        this.cpt++;
    }

    addMessage(jqueryId, messageStr) {
        let message = {
            element : jqueryId,
            intro : messageStr,
            step : this.cpt,
        };


        this.listMessage.push(message);
        this.cpt++;
    }

    addMessageAndChangePageAfterClick(jqueryId, messageStr, idLink) {
        this.addMessage(jqueryId, messageStr);

        if($(idLink) === undefined && $(idLink).attr("href") === undefined) {
            console.log("Element " + idLink + " is undefined or is not a link");
        } else {
            let symboleAdd = $(idLink).attr("href").includes("?") ? "&" : "?";
            $(idLink).attr("href", $(idLink).attr("href") + symboleAdd + "tutorielId=" + this.tutorialId + "&startStep="+this.cpt);
        }
    }

    start(startStep=1) {
        if(startStep<=0)startStep=0;

        this.intro = introJs();
        this.listMessageToUse = this.listMessage.slice(startStep-1);
        this.intro.setOptions({steps:this.listMessageToUse});

        this.intro.onchange(this.goToNextStepAfterClick);

        this.intro.onafterchange(function(targetElement) {
            var intro=this;

            // Bug introjs with modal bootstrat, we move introjs directly into the modal to cerrect it (bug with fix position)
            // by default introjs element on body
            $('.introjs-overlay, .introjs-helperLayer, .introjs-tooltipReferenceLayer').appendTo("body");
            waitForElementToDisplay(intro._options.steps[intro._currentStep + 1].element, 100, function () {
                if($("div.modal.introjs-fixParent").length==1) {
                    $('.introjs-overlay, .introjs-helperLayer, .introjs-tooltipReferenceLayer').appendTo("div.modal.introjs-fixParent");
                    $('.introjs-overlay').css("position","absolute");
                    $('.introjs-overlay').css("height", $(document).height() + "px");
                }
                $('.introjs-helperLayer, .introjs-tooltipReferenceLayer').removeClass("introjs-fixedTooltip");
            });
        });
        var _this = this;
        // wait for the first element
        if(this.listMessage[startStep-1] != undefined && this.listMessage[startStep-1].element != undefined) {
            waitForElementToDisplay(this.listMessage[startStep - 1].element, 100, function() {
                _this.intro.start();
            });
        } else {
            this.intro.start();
        }
    }


    goToNextStepAfterClick(targetElement) {
        let intro = this;

        var clickOnNextStep = function () {
            if (intro != undefined) {
                waitForElementToDisplay(intro._options.steps[intro._currentStep + 1].element, 100, function () {
                    intro.nextStep();
                });
            }
        }

        if ($(targetElement).is("button")) { // if current element is a button
            $(targetElement).click(clickOnNextStep);
        } else { // else, for each button into the element
            $(targetElement).find("button").each(function (index, value) {
                $(value).click(clickOnNextStep);
            });
        }
    }
}




function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};


function waitForElementToDisplay(selector, time, callback) {
    if($(selector).is(":visible")) {
        callback();
        return;
    }
    else {
        setTimeout(function() {
            waitForElementToDisplay(selector, time, callback);
        }, time);
    }
}

