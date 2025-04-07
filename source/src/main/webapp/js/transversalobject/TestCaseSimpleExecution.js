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

/***
 * Open the modal.
 * @returns {null}
 */
function openModalExecutionSimple(application, test, testcase, description, country, environment, robot) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#testCaseSimpleExecutionModal').data("initLabel") === undefined) {
        initModalTestCaseSimpleExecution(application, test, testcase, country, environment, robot);
        $('#testCaseSimpleExecutionModal').data("initLabel", true);
    }

    // Init the Saved data to false.
    $('#testCaseSimpleExecutionModal').data("Saved", false);
    $('#testCaseSimpleExecutionModal').data("testcase", undefined);

    //Add event on Save button.
    $("#executeTestCaseButton").off("click");
    $("#executeTestCaseButton").click(function () {
        if (checkFormBeforeSubmit() === true) {
            submitForm();
        }
    });
    $('#executeTestCaseButton').attr('class', 'btn btn-primary');
    $('#executeTestCaseButton').removeProp('hidden');

    //Feed the modal
    feedNewTestCaseModalSimple("testCaseSimpleExecutionModal");

    //Clean response messages
    $('#executeTestCaseModalForm #application').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#testCaseSimpleExecutionModal'));

    $('#testCaseToExecute').attr('data-test', test).attr('data-testcase', testcase).text(description);
}

//Init Modal label & combo
function initModalTestCaseSimpleExecution(application, test, testcase, country, environment, robot) {
    var doc = new Doc();

    $("[name='robotField']").html(doc.getDocOnline("testcaseexecution", "Robot"));
    $("[name='environmentField']").html(doc.getDocOnline("testcaseexecution", "Environment"));

    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_create"));

//Create link to Run page to display on the footer of the modal
    $("#linkToRunPageWithInformation").attr("href", "./RunTests.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase));

//Feed ApplicationEnvironment and Robot List
    displayRobotList("robotList", "ROBOTLIST", true, robot);
    displayApplicationIpList("environmentList", "", application, country, environment);

    $("#testToExecuteLabel").html('<span class="card-img-top glyphicon glyphicon-edit" style="font-size:15px;"></span>  ' + doc.getDocLabel("page_testcaseexecutionmodal", "testToExecuteLabel"));
    $("#chooseEnvLabel").html('<span class="card-img-top glyphicon glyphicon-list" style="font-size:15px;"></span>  ' + doc.getDocLabel("page_testcaseexecutionmodal", "chooseEnvLabel"));
    $("#chooseRobotLabel").html('<span class="card-img-top glyphicon glyphicon-road" style="font-size:15px;"></span>  ' + doc.getDocLabel("page_testcaseexecutionmodal", "chooseRobotLabel"));
    $("#customizeExecutionSettings").html('<span class="card-img-top glyphicon glyphicon-cog" style="font-size:15px;"></span>  ' + doc.getDocLabel("page_testcaseexecutionmodal", "customizeExecutionSettings"));


//Activate popover for interactive tutorial
    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

//if only 1 application or 1 env, selct them by default
    if ($("[name='applicationIpItem']").size() === 1) {
        $("[name='applicationIpItem']").addClass("active");
    }
    if ($("[name='robotItem']").size() === 1) {
        $("[name='robotItem']").addClass("active");
    }

    $("#filterRobot").keyup(function () {
        $("[name='robotItem']").show();

        $(this).val().toLowerCase().split(" ").forEach(function (item) {
            $("[name='robotItem']").each(function () {
                if (JSON.stringify($(this).data('item')).toLowerCase().indexOf(item) <= -1) {
                    $(this).hide();
                }
            });
        });
    });

    $("#filterEnvironment").keyup(function () {
        $("[name='applicationIpItem']").show();

        $(this).val().toLowerCase().split(" ").forEach(function (item) {
            $("[name='applicationIpItem']").each(function () {
                if (JSON.stringify($(this).data('item')).toLowerCase().indexOf(item) <= -1) {
                    $(this).hide();
                }
            });
        });
    });


}


function checkFormBeforeSubmit() {

    var doc = new Doc();
    clearResponseMessage($('#testCaseSimpleExecutionModal'));

    var hasNoEnvSelected = $('#environmentList').find('button[class*="active"]').size() === 0;

    var localMessage = new Message("danger", "Unexpected Error!");
    if (hasNoEnvSelected) {
        localMessage = new Message("danger", doc.getDocLabel("page_runtest", "select_one_env"));
        showMessage(localMessage, $('#testCaseSimpleExecutionModal'));
    }

    // verify if all mandatory fields are not empty and valid
    if (hasNoEnvSelected) {
        return false;
    }
    return true;
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String}
 * @returns {null}
 */
function submitForm() {

    clearResponseMessage($('#testCaseSimpleExecutionModal'));

    showLoaderInModal('#testCaseSimpleExecutionModal');

    var paramSerialized = "e=1";
    paramSerialized += "&tag=" + encodeURIComponent($("#executionSettings #tag").val());
    paramSerialized += "&screenshot=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&video=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&verbose=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&pagesource=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&seleniumlog=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&consolelog=" + $("#verbose > label[class*='active'] > input").attr('data-verbose');
    paramSerialized += "&timeout=" + $("#executionSettings #timeout").val();
    paramSerialized += "&manualexecution=" + $("#manual > label[class*='active'] > input").attr('data-manual');
    paramSerialized += "&retries=" + $("#retries > label[class*='active'] > input").attr('data-retries');
    paramSerialized += "&priority=" + $("#executionSettings #priority").val();
    paramSerialized += "&outputformat=json";

    let teststring = "";
    let test = $('#testCaseToExecute').attr('data-test');
    let testcase = $('#testCaseToExecute').attr('data-testcase');
    teststring += "&test=" + test + "&testcase=" + testcase;

    let countryenvironmentstring = "";
    $('#environmentList').find('button[class*="active"]').each(function () {
        if (!countryenvironmentstring.includes("&country=" + $(this).attr('data-country'))) {
            countryenvironmentstring += "&country=" + $(this).attr('data-country');
        }
        if (!countryenvironmentstring.includes("&environment=" + $(this).attr('data-environment'))) {
            countryenvironmentstring += "&environment=" + $(this).attr('data-environment');
        }
    });

    let robotsstring = "";
    $('#robotList').find('button[class*="active"]').each(function () {
        robotsstring += "&robot=" + $(this).attr('data-robot');
    });

    var jqxhr = $.post("AddToExecutionQueuePrivate", paramSerialized + teststring + countryenvironmentstring + robotsstring);
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns
        hideLoaderInModal('#testCaseSimpleExecutionModal');

        data.message = data.message.replace(/\n/g, '<br>');
        if (getAlertType(data.messageType) === "success") {
            handleAddToQueueResponse(data, true);
        } else {
            let localMessage = {};
            localMessage.message = data.message;
            localMessage.messageType = getAlertType(data.messageType);
            showMessage(localMessage, $('#testCaseSimpleExecutionModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);


}

function handleAddToQueueResponse(data, doRedirect) {
    if (data.nbErrorRobotMissing > 0) {
        data.message = data.message + "<br>" + data.nbErrorRobotMissing + " Executions not added due to <b>Empty Robot</b>.";
    }
    if (data.nbErrorTCNotActive > 0) {
        data.message = data.message + "<br>" + data.nbErrorTCNotActive + " Executions not added due to <b>Test Case not active</b>.";
    }
    if (data.nbErrorTCNotAllowedOnEnv > 0) {
        data.message = data.message + "<br>" + data.nbErrorTCNotAllowedOnEnv + " Executions not added due to <b>Test Case not beeing allowed to run on the corresponding group of environment</b>.";
    }
    if (data.nbErrorEnvNotExistOrNotActive > 0) {
        data.message = data.message + "<br>" + data.nbErrorEnvNotExistOrNotActive + " Executions not added due to <b>Environment/Country not active or don't exist</b>.";
    }
    if (data.nbExe === 1) {
        data.message = data.message + "<br><a href='TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId + "'><button class='btn btn-primary' id='goToExecution'>Open Execution</button></a>";
    }
    if (data.nbExe > 1) {
        data.message = data.message + "<br><a href='ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(data.tag) + "'><button class='btn btn-primary' id='goToTagReport'>Report by Tag</button></a>"
    }

    if ((data.nbExe === 1) && doRedirect) {
        window.location.href = "TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId;
    }
    if ((data.nbExe > 1) && doRedirect) {
        window.location.href = "ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(data.tag);
    }

    var rc = getAlertType(data.messageType);
    if ((rc === "success") && (data.nbExe === 0)) {
        data.messageType = "KO";
    }
    let localMessage = {};
    localMessage.message = data.message;
    localMessage.messageType = data.messageType;

    showMessage(localMessage, $('#testCaseSimpleExecutionModal'));

}

function afterSuccessSubmit(data, dataMessage) {
    var doc = new Doc();

    var code = getAlertType(dataMessage.messageType);

    var elementAlert = $('#editTestCaseSimpleCreationModal').find("div[id*='DialogMessagesAlert']");
    var elementAlertDescription = $('#editTestCaseSimpleCreationModal').find("span[id*='DialogAlertDescription']");

    elementAlertDescription.html(dataMessage.message);
    elementAlert.addClass("alert-success");
    elementAlert.fadeIn();

    elementAlert.fadeTo(2000, 1, function () {
        elementAlert.slideUp(500);
    });

    $('editTestCaseButton').off("click").click(function () {
        window.location.href = "TestCaseScript.jsp?test=" + encodeURI(data.test.replace(/\+/g, ' ')) + "&testcase=" + encodeURI(data.testCase.replace(/\+/g, ' '));
    });

    $('#editTestCaseModalForm').hide();
    $('#afterTestCaseCreationModalForm').show();

}



/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} modalId - Id of the modal to feed.
 * @param {String} defaultTest - default test to selected.
 * @returns {null}
 */
function feedNewTestCaseModalSimple(modalId) {
    clearResponseMessageMainPage();
    $('#editTestCaseModalForm').show();
    $('#afterTestCaseCreationModalForm').hide();

    var formEdit = $('#' + modalId);


    $('#editTestCaseSimpleCreationModal [name="application"]').change(function () {
        if ($('#editTestCaseSimpleCreationModal [name="application"] option[data-select2-tag=true]')[0] !== undefined) {
            $("#newApplication").attr('style', 'display:block');
        } else {
            $("#newApplication").attr('style', 'display:none');
        }
    });

    formEdit.modal('show');
}




