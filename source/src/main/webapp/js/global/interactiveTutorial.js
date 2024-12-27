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

$(document).ready(function () {

    if (document.title !== "Login") {
        let displayWelcomeTuto = localStorage.getItem("displayWelcomeTuto");

        if (displayWelcomeTuto === null) {
            displayWelcomeTuto = "true";
            localStorage.setItem("displayWelcomeTuto", "true");
        }

        var doc = new Doc();
        if ((displayWelcomeTuto === "true") && (GetURLParameter("tutorielId") == null)) {
            if ((window.location.href.includes("Homepage.jsp")) || (window.location.href.slice(-1) === "/")) {
                displayTuto(doc);
            }
        }

        $("#byPassTuto").off("click");
        $("#byPassTuto").click(function () {
            localStorage.setItem("displayWelcomeTuto", "false");
            $('#interactiveTutoModal').modal('hide')
        });


        $("#openInteractiveTutoModal").off("click");
        $("#openInteractiveTutoModal").click(function () {
            displayTuto(doc);
        });

        if (getUrlParameter("tutorielId") !== undefined) {
            let tutorielId = getUrlParameter("tutorielId");
            let startStep = getUrlParameter("startStep");
            interractiveTutorial(tutorielId, startStep);
        }
    }
});

function displayTuto(doc) {

    // remove all into the modal
    $("#interactiveTutoList").html("");
    $('#interactiveTutoList').append("<div class='row' id='link-line'>");
    $('#link-line').append("<div class='col-lg-6 panel' id='createTest-line'>");
    $('#link-line').append("<div class='col-lg-6 panel' id='tutolist-line'>");

    $('#createTest-line').append("<div id='tutoWelcome6' class='panel-heading bold text-center'>CREATE AND EXECUTE TESTCASE IN MINUTES</div>");
    $('#createTest-line').append(
            "<div class='marginTop10 marginBottom10 text-center'  data-dismiss=\"modal\" id=createTestcaseTutorial>" +
            "  <a href='#' class='btn btn-success'>" +
            "  <div class='card' style='width: 18rem;'>" +
            "  <span class=\"card-img-top glyphicon glyphicon-pencil marginBottom20 marginTop20\" style=\"font-size:70px;\"></span>" +
            "  <div class='card-body'>" +
            "  <p id='tutoCreateTestcaseLabel' class='card-text'>Create Testcase</p>" +
            "  </div>" +
            "  </div>" +
            "    </a>" +
            "</div>");

    $('#tutolist-line').append("<div id='tutoWelcome7' class='panel-heading bold text-center'>FOLLOW ONE OF OUR INTERACTIVE TUTORIAL</div>");
    $('#tutolist-line').append("<div class='panel-body' id='tuto-line'></div>");

    $("#tutoWelcome1").html(doc.getDocDescription("transversal", "tuto_line1"));
    $("#tutoWelcome2").html(doc.getDocDescription("transversal", "tuto_line2"));
    $("#tutoWelcome3").html(doc.getDocDescription("transversal", "tuto_line3"));
    $("#tutoWelcome4").html(doc.getDocDescription("transversal", "tuto_line4"));
    $("#byPassTuto").html(doc.getDocDescription("transversal", "tuto_line5"));
    $("#tutoWelcome6").html(doc.getDocDescription("transversal", "tuto_line6"));
    $("#tutoWelcome7").html(doc.getDocDescription("transversal", "tuto_line7"));
    $("#tutoCreateTestcaseLabel").html(doc.getDocDescription("transversal", "tuto_createTestCaseButton"));

    $.get('api/interactiveTuto/list',
            function (data, status) {
                if (status === 'success') {
                    data.forEach(function (data) {
                        createNewButtonOnTutoShowroom(data.id, data.title, data.description, data.role, data.level);
                    });


                } else {
                    console.error('api/interactiveTuto/list respond with error' + status);
                }
            });

    $('#interactiveTutoModal').modal();

    $("#createTestcaseTutorial").click(function () {
        openModalTestCaseSimple();
    });

}

function createNewButtonOnTutoShowroom(id, title, description, role, level) {


    let levelstr = "easy";
    let badgecolor = "success";
    if (level === 2) {
        levelstr = "medium";
        badgecolor = "warning";
    }
    if (level === 3) {
        levelstr = "hard";
        badgecolor = "danger";
    }


    // populate the modal
    $('#tuto-line').append(
            "<div class='col-sm-4 marginTop10 marginBottom10 text-center'  data-dismiss=\"modal\" id=tuto-" + id + ">" +
            "  <a href='#' class='btn btn-primary'>" +
            "  <div class='card' style='width: 8rem;'>" +
            "  <span class=\"card-img-top glyphicon glyphicon-" + title + " marginBottom20 marginTop20\" style=\"font-size:20px;\"></span>" +
            "  <div class='card-body'>" +
            "  <p class='card-text'>" + description + "</p>" +
            "  </div>" +
            "  </div>" +
            "    </a>" +
            "</div>"
            );



    $('#tuto-' + id).click(function () {
        interractiveTutorial(id);
    })

}

var currentInteractiveTuto;

function interractiveTutorial(id, startStep = 1) {

    $.get('api/interactiveTuto/get', {
        id: id
    }, function (data, status) {
        if (status !== 'success') {
            console.error('api/interactiveTuto/get respond with error' + status);
            return;
        }
        let cerberusTuto = new CerberusTuto(data.id);
        currentInteractiveTuto = cerberusTuto;
        if (data.steps == null || data.steps.length <= 0) {
            cerberusTuto.addGeneralMessage("Tutoriel is being written  ...");
        } else {
            data.steps.forEach(function (step) {
                switch (step.type) {
                    case 'GENERAL' :
                        cerberusTuto.addGeneralMessage(step.text);
                        break;
                    case 'CHANGE_PAGE_AFTER_CLICK' :
                        cerberusTuto.addMessageAndChangePageAfterClick(step.selectorJquery, step.text, step.attr1);
                        break;
                    default :
                        cerberusTuto.addMessage(step.selectorJquery, step.text);
                        break;
                }
            });
        }

        cerberusTuto.start(startStep);
    });
}



class CerberusTuto {
    constructor(tutorialId) {
        this.tutorialId = tutorialId;
        this.listMessage = new Array();
        this.cpt = 1;
        this.working = false;
        this.startStep = 0;
    }

    addGeneralMessage(messageStr) {
        let message = {
            intro: messageStr,
            step: this.cpt,
            type: 'general'
        };
        this.listMessage.push(message);

        this.cpt++;
    }

    addMessage(jqueryId, messageStr) {
        let message = {
            element: jqueryId,
            elementStr: jqueryId,
            intro: messageStr,
            step: this.cpt,
            type: 'default'
        };


        this.listMessage.push(message);
        this.cpt++;
    }

    addMessageAndChangePageAfterClick(jqueryId, messageStr, idLink) {
        this.addMessage(jqueryId, messageStr);
        this.listMessage[this.listMessage.length - 1].type = 'changeAfterClick';
        this.listMessage[this.listMessage.length - 1].idLink = idLink;
    }

    isWorking() {
        return this.working;
    }

    getUrlParamter() {
        return "tutorielId=" + this.getTutorialId() + "&startStep=" + this.intro.getNextStep();
    }

    getTutorialId() {
        return this.tutorialId;
    }

    getCurrentStep() {
        return parseInt(this.currentStep) + parseInt(this.startStep);
    }
    getNextStep() {
        return this.getCurrentStep() + parseInt(this.startStep) + 1;
    }
    isLastStep() {
        return this.currentStep === this.intro._options.steps.length - 1;
    }

    start(startStep = 1) {
        var doc = new Doc();
        console.info("start");

        if (startStep <= 0)
            startStep = 0;
        this.startStep = startStep;
        this.currentStep = startStep;
        this.intro = introJs();
        this.listMessageToUse = this.listMessage.slice(startStep - 1);
        this.intro.setOptions({steps: this.listMessageToUse});

        let _this = this;

        // correct a bug into introJs. If element use "nth-child" selector,  we have to
        // initialize and find it manually it before a change
        this.intro.onbeforechange(function (targetElement) {
            console.info("onbeforechange");
            console.info(_this);

            if (this._options.steps[this._currentStep].element != undefined && this._options.steps[this._currentStep].element.indexOf("nth-child") !== -1 ||
                    this._introItems[this._currentStep].element === document.querySelector(".introjsFloatingElement") && typeof (this._introItems[this._currentStep].elementStr) === 'string') {
                let elmt = $(this._options.steps[this._currentStep].elementStr);
                if (elmt != undefined) {
                    this._introItems[this._currentStep].position = null;
                    this._introItems[this._currentStep].element = document.querySelector(this._options.steps[this._currentStep].element);
                }
            }
        });

        this.intro.onbeforeexit(function () {
            console.info("onbeforeexit");
            console.info(_this);
            if (modalConfirmationIsVisible()) {
                hideModalConfirmationIsVisible();
                return true;
            }

            if (!_this.isLastStep()) {
                showModalConfirmation(function () {
                    console.log("ok");
                    _this.intro.exit(true);
                    this.working = false;
                }, function () {
                }, "Warning", doc.getDocDescription("transversal", "tuto_exit"));
                return false;
            }

            return true;
        });


        this.intro.onchange(function (targetElement) {
            console.info("onchange");
            console.info(_this);
            let intro = this;
            _this.currentStep = intro._currentStep;
            var clickOnNextStep = function (targetElement) {
                if (intro != undefined && intro._options.steps[intro._currentStep + 1] != undefined && intro._options.steps[intro._currentStep + 1].element != undefined) {
                    waitForElementToDisplay(intro._options.steps[intro._currentStep + 1].element, 100, function () {
                        intro.nextStep();
                    });
                } else if (intro != undefined) {
                    intro.nextStep();
                }
            }

            if ($(targetElement).is("button")) { // if current element is a button
                $(targetElement).unbind("click.clickOnNextStep");
                $(targetElement).bind("click.clickOnNextStep", clickOnNextStep);
            } else { // else, for each button into the element
                $(targetElement).find("button").each(function (index, value) {
                    $(value).unbind("click.clickOnNextStep");
                    $(value).bind("click.clickOnNextStep", clickOnNextStep);
                });

                //  ecouter les autre bouton qui appariasserait pour ajouter l'action click
                $(document).on('DOMNodeInserted', function (e) {
                    $(e.target).find("button").unbind("click.clickOnNextStep");
                    $(e.target).find("button").bind("click.clickOnNextStep", clickOnNextStep);
                });

            }

            // add the step and tutorial number on link to follow the tutorial throw web pages
            let message = _this.listMessage[intro._currentStep + parseInt(startStep) - 1];
            // if we want change page after the click, we have to added
            if (message.type === 'changeAfterClick') {
                if ($(message.idLink) === undefined) {
                    console.log("Element " + message.idLink + " is undefined");
                } else {
                    let typeObj = $(message.idLink).prop('nodeName');

                    // by default, we add action on dom
                    if (typeObj != undefined) {
                        prepareChangeAfterClick(typeObj, message, _this);
                    }

                    $(message.element).on('DOMNodeInserted', function (e) {
                        let typeObj = $(e.target).find(message.idLink).prop('nodeName');
                        if (typeObj != undefined) {
                            prepareChangeAfterClick(typeObj, message, _this);
                        }
                    });
                }
            }

        });

        this.intro.onafterchange(function (targetElement) {
            console.info("onafterchange");
            console.info(_this);
            var intro = this;

            // Bug introjs with modal bootstrat, we move introjs directly into the modal to correct it (bug with fix position)
            // by default introjs element on body
            $('.introjs-overlay, .introjs-helperLayer, .introjs-tooltipReferenceLayer').appendTo("body");
            if (intro._options.steps[intro._currentStep ] !== undefined && intro._options.steps[intro._currentStep ].element !== undefined) {
                waitForElementToDisplay(intro._options.steps[intro._currentStep].element, 100, function () {
                    if ($("div.modal.introjs-fixParent").length == 1) {
                        $('.introjs-overlay, .introjs-helperLayer, .introjs-tooltipReferenceLayer').appendTo("div.modal.introjs-fixParent");
                        $('.introjs-overlay').css("position", "absolute");
                        $('.introjs-overlay').css("height", $(document).height() + "px");
                    }
                    $('.introjs-helperLayer, .introjs-tooltipReferenceLayer').removeClass("introjs-fixedTooltip");
                });
            }

        });

        // wait for the first element
        if (this.listMessage[startStep - 1] != undefined && this.listMessage[startStep - 1].element != undefined) {
            waitForElementToDisplay(this.listMessage[startStep - 1].element, 100, function () {
                _this.intro.start();
                _this.working = true;
            });
        } else {
            this.intro.start();
            this.working = true;
    }
    }

}



function prepareChangeAfterClick(typeObj, message, _this) {
    switch (typeObj.toLowerCase()) {
        case "button" :
        case "form" : // cas du form sans action, ne marchera pas avec un action
            let url = window.location.href;

            // get part after ?
            let getterParams = url.substr(url.indexOf("?") + 1, url.length - 1).split("&");

            // construct new url
            let newurl = "?";
            getterParams.forEach(function (param) {
                let paramTab = param.split("=");
                switch (paramTab[0]) {
                    case 'tutorielId' :
                        newurl += "&tutorielId=" + _this.tutorialId;
                        break;
                    case 'startStep' :
                        newurl += "&startStep=" + (message.step + 1);
                        break;
                    default :
                        newurl += "&" + param;
                        break;
                }
            });

            window.history.pushState(null, "", newurl);

            break;
        case "a" :

            $(message.idLink).each(function (index, data) {
                let symboleAdd = $(data).attr("href").includes("?") ? "&" : "?";
                $(data).attr("href", $(data).attr("href") + symboleAdd + "tutorielId=" + _this.tutorialId + "&startStep=" + (message.step + 1));
            });
            break;
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
}
;


function waitForElementToDisplay(selector, time, callback) {
    if ($(selector).is(":visible")) {
        callback();
        return;
    } else {
        setTimeout(function () {
            waitForElementToDisplay(selector, time, callback);
        }, time);
    }
}

