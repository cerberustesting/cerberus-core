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
var nbRow = 0;

function openModalApplication(application, mode, page) {

    if ($('#editApplicationModal').data("initLabel") === undefined) {
        if (page === "applicationObject") {
            initModalApplication(application, mode, "application", );
        } else {
            initModalApplication(application, mode, "testCaseScript");
        }
        $('#editApplicationModal').data("initLabel", true);
    }

    // In Edit TestCase form, if we change the test, we get the latest testcase from that test.
    $('#editApplicationModalForm input[name="application"]').off("change");
    if (mode === "EDIT") {
        $('#editApplicationModalForm input[name="application"]').change(function () {
            // Compare with original value in order to display the warning message.
            displayWarningOnChangeApplicationKey();
        });
    }


    var doc = new Doc();
    if (mode === "EDIT") {
        editApplicationClick(application);
        $("#editApplicationModalLabel").html(doc.getDocLabel("page_application", "button_edit"));
    } else if (mode === "ADD") {
        $("#editApplicationModalLabel").html(doc.getDocLabel("page_application", "button_create"));
        addApplicationClick(application);
    } else if (mode === "DUPLICATE") {
        $("#editApplicationModalLabel").html(doc.getDocLabel("page_application", "button_create"));
        duplicateApplicationClick(application);
    }
}

function initModalApplication(application, mode, page) {
    var doc = new Doc();
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
    $("[name='buttonDuplicate']").html(doc.getDocLabel("page_global", "btn_duplicate"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_applicationObject", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_applicationObject", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_applicationObject", "button_edit"));
    $("[name='objectField']").html(doc.getDocOnline("page_applicationObject", "Object"));
    $("[name='applicationField']").html(doc.getDocOnline("page_applicationObject", "Application"));
    $("[name='screenshotfilenameField']").html(doc.getDocOnline("page_applicationObject", "ScreenshotFileName"));

    $("[name='bugtrackerurlField']").html(doc.getDocOnline("application", "bugtrackerurl"));
    $("[name='bugtrackernewurlField']").html(doc.getDocOnline("application", "bugtrackernewurl"));

    displayInvariantList("system", "SYSTEM", false, undefined, undefined, undefined, undefined, "editApplicationModal");
    displayInvariantList("type", "APPLITYPE", false, undefined, undefined, undefined, undefined, "editApplicationModal");
    displayDeployTypeList("deploytype");
    displayInvariantList("bugtrackerconnector", "BUGTRACKERCONNECTOR", false, undefined, undefined, undefined, undefined, "editApplicationModal");

    $("#editApplicationModalForm #type").off("change");
    $("#editApplicationModalForm #type").change(function () {
        $("#editApplicationModalForm #AppLogo").attr("src", "./images/logoapp-" + $(this).val() + ".png");
    });

    $("#editApplicationButton").off("click");
    $("#editApplicationButton").click(function () {
        confirmApplicationModalHandler("EDIT");
    });
    $("#addApplicationButton").off("click");
    $("#addApplicationButton").click(function () {
        confirmApplicationModalHandler("ADD");
    });
    $("#duplicateApplicationButton").off("click");
    $("#duplicateApplicationButton").click(function () {
        confirmApplicationModalHandler("DUPLICATE");
    });
    $("#bugtrackerconnector").off("change");
    $("#bugtrackerconnector").change(function () {
        updateBugTrackerConnector();
    });

    //clear the modals fields when closed
    $('#editApplicationModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    // Adding rows in edit Modal.
    $("#addEnvironment").off("click");
    $("#addEnvironment").click(addNewEnvironmentRow);

    // Load the select needed in localStorage cache.
    getSelectInvariant("ENVIRONMENT", true);
    getSelectInvariant("COUNTRY", true);

    // Redraw applicationObjects DataTable when Object tab is clicked (Alpine.js tabs)
    $(document).off('click.appObjectTab').on('click.appObjectTab', '#editApplicationModal button[name="tabsAppEdit3"]', function () {
        setTimeout(function () {
            if ($("#editApplicationModal #applicationObjectsTable_wrapper").length > 0) {
                $("#editApplicationModal #applicationObjectsTable").DataTable().draw();
            }
        }, 100);
    });

}



/***
 * Open the modal with queue information.
 * @param {String} application - robot selected
 * @returns {null}
 */
function editApplicationClick(application) {

    clearResponseMessage($('#editApplicationModal'));

    // When editing the execution queue, we can modify, modify and run or cancel.
    $('#editApplicationButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#duplicateApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#addApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');

    feedApplicationModal(application, "EDIT");
}

/***
 * Open the modal with queue information.
 * @param {String} application - name of the robot to duplicate.
 * @returns {null}
 */
function duplicateApplicationClick(application) {

    clearResponseMessage($('#editApplicationModal'));

    $('#duplicateApplicationButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#editApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#addApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');

    feedApplicationModal(application, "DUPLICATE");
}

/***
 * Open the modal with queue information.
 * @param {String} application - idname of the invariant to duplicate.
 * @returns {null}
 */
function addApplicationClick(application) {

    clearResponseMessage($('#editApplicationModal'));

    $('#addApplicationButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#editApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#duplicateApplicationButton').attr('class', 'hidden').attr('hidden', 'hidden');

    feedApplicationModal(application, "ADD");
}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editApplicationModal #editApplicationModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editApplicationModal'));
}

function confirmApplicationModalHandler(mode) {
    clearResponseMessage($('#editApplicationModal'));
    var formEdit = $('#editApplicationModal #editApplicationModalForm');

    // Calculate servlet name to call.
    var myServlet = "UpdateApplication";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateApplication";
    }


    // Getting Data from Application TAB
    var table1 = $("#environmentTableBody tr");
    var table_environment = [];
    for (var i = 0; i < table1.length; i++) {
        table_environment.push($(table1[i]).data("environment"));
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editApplicationModal');
    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {application: data.application,
            originalApplication: data.originalApplication,
            description: data.description,
            sort: data.sort,
            type: data.type,
            system: data.system,
            poolSize: data.poolSize,
            subsystem: data.subsystem,
            repourl: data.repourl,
            bugtrackerconnector: data.bugtrackerconnector,
            bugtrackerparam1: data.bugtrackerparam1,
            bugtrackerparam2: data.bugtrackerparam2,
            bugtrackerparam3: data.bugtrackerparam3,
            bugtrackerurl: data.bugtrackerurl,
            bugtrackernewurl: data.bugtrackernewurl,
            deploytype: data.deploytype,
            mavengroupid: data.mavengroupid,
            environmentList: JSON.stringify(table_environment)},
        success: function (data) {
            hideLoaderInModal('#editApplicationModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#applicationsTable").dataTable();
                oTable.fnDraw(false);
                $('#editApplicationModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editApplicationModal'));
            }
        },
        error: showUnexpectedError
    });

}

function feedApplicationModal(application, mode, system) {
    clearResponseMessageMainPage();

    var formEdit = $('#editApplicationModal');

    if (mode === "DUPLICATE" || mode === "EDIT") {

        var jqxhr = $.getJSON("ReadApplication", "application=" + application);
        $.when(jqxhr).then(function (data) {

            if (data.messageType === "OK") {

                var appObj = data.contentTable;
                var hasPermissions = data.hasPermissions;

                feedApplicationModalData(appObj, mode, hasPermissions);

                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }

        });
    } else {

        var appObj1 = {};
        appObj1.application = "";
        appObj1.description = "";
        appObj1.type = "NONE";
        appObj1.system = getUser().defaultSystem;
        appObj1.subsystem = "";
        appObj1.poolSize = 10;
        appObj1.repourl = "";
        appObj1.bugTrackerUrl = "";
        appObj1.bugTrackerConnector = "";
        appObj1.bugTrackerParam1 = "";
        appObj1.bugTrackerParam2 = "";
        appObj1.bugTrackerParam3 = "";
        appObj1.bugTrackerNewUrl = "";
        appObj1.deploytype = "NONE";
        appObj1.mavengroupid = "";
        var hasPermissions = true;
        feedApplicationModalData(appObj1, mode, hasPermissions);

        formEdit.modal('show');

    }
}


function feedApplicationModalData(application, mode, hasPermissionsUpdate) {

    var formEdit = $('#editApplicationModal');

    formEdit.find("#originalApplication").prop("value", application["application"]);
    formEdit.find("#application").prop("value", application["application"]);
    formEdit.find("#description").prop("value", application["description"]);
    //formEdit.find("#sort").prop("value", obj["sort"]);
    formEdit.find("#type").prop("value", application["type"]);
    formEdit.find("#AppLogo").attr("src", "./images/logoapp-" + application["type"] + ".png");
    formEdit.find("#system").prop("value", application["system"]);
    formEdit.find("#subsystem").prop("value", application["subsystem"]);
    formEdit.find("#poolSize").prop("value", application["poolSize"]);
    formEdit.find("#repourl").prop("value", application["repoUrl"]);
    formEdit.find("#bugtrackerurl").prop("value", application["bugTrackerUrl"]);
    formEdit.find("#bugtrackerconnector").prop("value", application["bugTrackerConnector"]);
    formEdit.find("#bugtrackerparam1").prop("value", application["bugTrackerParam1"]);
    formEdit.find("#bugtrackerparam2").prop("value", application["bugTrackerParam2"]);
    formEdit.find("#bugtrackerparam3").prop("value", application["bugTrackerParam3"]);
    formEdit.find("#bugtrackernewurl").prop("value", application["bugTrackerNewUrl"]);
    formEdit.find("#deploytype").prop("value", application["deploytype"]);
    formEdit.find("#mavengroupid").prop("value", application["mavengroupid"]);

    if (!hasPermissionsUpdate) { // If readonly, we only readonly all fields
        formEdit.find("#application").prop("readonly", "readonly");
        formEdit.find("#description").prop("readonly", "readonly");
        //formEdit.find("#sort").prop("readonly", "readonly");
        formEdit.find("#type").prop("disabled", "disabled");
        formEdit.find("#system").prop("disabled", "disabled");
        formEdit.find("#subsystem").prop("readonly", "readonly");
        formEdit.find("#poolSize").prop("readonly", "readonly");
        formEdit.find("#repourl").prop("readonly", "readonly");
        formEdit.find("#bugtrackerurl").prop("readonly", "readonly");
        formEdit.find("#bugtrackernewurl").prop("readonly", "readonly");
        formEdit.find("#deploytype").prop("disabled", "disabled");
        formEdit.find("#mavengroupid").prop("readonly", "readonly");

        $('#editApplicationButton').attr('class', '');
        $('#editApplicationButton').attr('hidden', 'hidden');
    }

    if ($("#editApplicationModal #applicationObjectsTable_wrapper").length > 0) {
        var oTable = $("#editApplicationModal #applicationObjectsTable").dataTable();
        oTable.fnSettings().sAjaxSource = "ReadApplicationObject?application=" + encodeURIComponent(application.application);
        oTable.fnDraw();

    } else {
        var configurations = new TableConfigurationsServerSide("applicationObjectsTable", "ReadApplicationObject?application=" + encodeURIComponent(application.application), "contentTable", aoColumnsFunc_object("applicationObjectsTable"), [1, 'asc']);
        var table = createDataTableWithPermissions(configurations, function (data) {
            renderOptionsForApplicationObject(application.application, data);
        }, "#applicationObjectList", undefined, true);
    }

    if (mode !== "ADD") {
        loadEnvironmentTable(application.system, application.application);
    } else {
        $('#environmentTableBody tr').remove();
    }
    updateBugTrackerConnector();

    refreshPopoverDocumentation("editApplicationModalForm");

}

function updateBugTrackerConnector() {
    let connector = $('#editApplicationModal #bugtrackerconnector').val();
    if (connector === 'REDIRECT') {
        $('#editApplicationModal #TrackerLogo').attr("src", "./images/bt-REDIRECT.png");
        $('#editApplicationModal #btP1').hide();
        $('#editApplicationModal #btP2').hide();
        $('#editApplicationModal #btP3').hide();
        $('#editApplicationModal #btP3').hide();

    } else if (connector === 'JIRA') {
        $('#editApplicationModal #TrackerLogo').attr("src", "./images/bt-JIRA.png");
        $('#editApplicationModal #TrackerLogo').show();
        $('#editApplicationModal #btP1').show();
        $('#editApplicationModal #btP2').show();
        $('#editApplicationModal #btP3').hide();
        $("[name='bugtrackerparam1Field']").html("Project Code");
        $("[name='bugtrackerparam2Field']").html("Ticket Type");
        //<i class="fa fa-external-link" aria-hidden="true"></i>
        $('#editApplicationModal #bugtrackerparam2').autocomplete({
            source: ["Bug", "Task", "Story", "Epic"],
            minLength: 0,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        }).on("focus", function () {
            $(this).autocomplete("search", "");
        });

    } else if (connector === 'GITHUB') {
        $('#editApplicationModal #TrackerLogo').attr("src", "./images/bt-GITHUB.png");
        $('#editApplicationModal #TrackerLogo').show();
        $('#editApplicationModal #btP1').show();
        $('#editApplicationModal #btP2').show();
        $('#editApplicationModal #btP3').hide();
        $("[name='bugtrackerparam1Field']").html("organisation/repo");
        $("[name='bugtrackerparam2Field']").html("Label");
        //<i class="fa fa-external-link" aria-hidden="true"></i>
        $('#editApplicationModal #bugtrackerparam2').autocomplete({
            source: ["bug","cerberus"],
            minLength: 0,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        }).on("focus", function () {
            $(this).autocomplete("search", "");
        });

    } else if (connector === 'GITLAB') {
        $('#editApplicationModal #TrackerLogo').attr("src", "./images/bt-GITLAB.png");
        $('#editApplicationModal #TrackerLogo').show();
        $('#editApplicationModal #btP1').show();
        $('#editApplicationModal #btP2').show();
        $('#editApplicationModal #btP3').hide();
        $("[name='bugtrackerparam1Field']").html("organisation/repo");
        $("[name='bugtrackerparam2Field']").html("Label");
        //<i class="fa fa-external-link" aria-hidden="true"></i>
        $('#editApplicationModal #bugtrackerparam2').autocomplete({
            source: ["bug","cerberus"],
            minLength: 0,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        }).on("focus", function () {
            $(this).autocomplete("search", "");
        });

    } else if (connector === 'AZUREDEVOPS') {
        $('#editApplicationModal #TrackerLogo').attr("src", "./images/bt-AZUREDEVOPS.png");
        $('#editApplicationModal #TrackerLogo').show();
        $('#editApplicationModal #btP1').show();
        $('#editApplicationModal #btP2').hide();
        $('#editApplicationModal #btP3').hide();
        $("[name='bugtrackerparam1Field']").html("organisation/project");
        $("[name='bugtrackerparam2Field']").html("Label");
        //<i class="fa fa-external-link" aria-hidden="true"></i>
        $('#editApplicationModal #bugtrackerparam2').autocomplete({
            source: [""],
            minLength: 0,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        }).on("focus", function () {
            $(this).autocomplete("search", "");
        });

    } else {
        $('#editApplicationModal #TrackerLogo').attr("src", "");
        $('#editApplicationModal #TrackerLogo').hide();
        $('#editApplicationModal #btP1').hide();
        $('#editApplicationModal #btP2').hide();
        $('#editApplicationModal #btP3').hide();
        //<i class="fa fa-external-link" aria-hidden="true"></i>
        $('#editApplicationModal #bugtrackerparam2').autocomplete({
            source: [""],
            minLength: 0,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        }).on("focus", function () {
            $(this).autocomplete("search", "");
        });

    }
}

function renderOptionsForApplicationObject(id, data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if ($("#createApplicationObjectButton").length === 0) {
        var contentToAdd = "<div class='mb-3'><a href='ApplicationObjectList.jsp?application=" + id + "' target='_blank'><button id='createApplicationObjectButton' type='button' class='px-3 py-1.5 rounded-md border border-slate-300 dark:border-slate-600 text-sm text-slate-600 dark:text-slate-300 hover:border-sky-400 hover:text-sky-500 transition flex items-center gap-1.5'>" +
            "<i data-lucide='external-link' class='w-3.5 h-3.5'></i> " + doc.getDocLabel("page_application", "button_manage") + "</button></a></div>";

        $("#applicationObjectsTable_wrapper div#applicationObjectsTable_length").before(contentToAdd);
        if (window.lucide) lucide.createIcons();
    }
}

function loadEnvironmentTable(selectSystem, selectApplication) {

    $('#environmentTableBody tr').remove();
    var jqxhr = $.getJSON("ReadCountryEnvironmentParameters", "system=" + selectSystem + "&application=" + selectApplication + "&iSortCol_0=3");
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendEnvironmentRow(obj);
        });
        refreshPopoverDocumentation("editApplicationModalForm");

    }).fail(handleErrorAjaxAfterTimeout);

}

function appendEnvironmentRow(env) {
    nbRow++;
    var doc = new Doc();
    var n = nbRow;

    // Inputs keep form-control class — CSS in Application.html overrides it
    var selectEnvironment = getSelectInvariant("ENVIRONMENT", false);
    var selectCountry = getSelectInvariant("COUNTRY", false);
    var ipInput = $('<input class="form-control">').attr({maxlength: 150, placeholder: doc.getDocLabel("countryenvironmentparameters", "IP") || "Host"}).val(env.ip);
    var urlInput = $('<input class="form-control">').attr({maxlength: 150, placeholder: doc.getDocLabel("countryenvironmentparameters", "URL") || "/"}).val(env.url);
    var poolSizeInput = $('<input class="form-control">').attr({maxlength: 10, placeholder: "0"}).val(env.poolSize);
    var var1Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Variable 1"}).val(env.var1);
    var var2Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Variable 2"}).val(env.var2);
    var secret1Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Secret 1"}).val(env.secret1);
    var domainInput = $('<input class="form-control">').attr({maxlength: 150, placeholder: "Domain"}).val(env.domain);
    var urlLoginInput = $('<input class="form-control">').attr({maxlength: 300, placeholder: "URL Login"}).val(env.urlLogin);
    var var3Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Variable 3"}).val(env.var3);
    var var4Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Variable 4"}).val(env.var4);
    var secret2Input = $('<input class="form-control">').attr({maxlength: 200, placeholder: "Secret 2"}).val(env.secret2);
    var mobileActivity = $('<input class="form-control">').attr({maxlength: 254, placeholder: "Mobile Activity"}).val(env.mobileActivity);
    var mobilePackage = $('<input class="form-control">').attr({maxlength: 254, placeholder: "Mobile Package"}).val(env.mobilePackage);
    var activeInput = $('<input type="checkbox" class="env-check">').prop("checked", env.isActive);

    // Helper: label (plain text, no doc popover noise) + input
    function F(labelText, input) {
        var w = $('<div></div>');
        w.append($('<span class="env-label"></span>').text(labelText));
        w.append(input);
        return w;
    }

    var table = $("#environmentTableBody");
    var card = $('<tr></tr>');
    var cardTd = $('<td></td>');
    var cardDiv = $('<div class="env-card"></div>');

    // ── ROW 1: Environment · Country · Active · Delete ──
    var row1 = $('<div class="env-grid" style="grid-template-columns: 1fr 1fr 44px 28px; margin-bottom:6px;"></div>');
    row1.append(F("Environment", selectEnvironment.val(env.environment)));
    row1.append(F("Country", selectCountry.val(env.country)));
    // Active checkbox
    var actWrap = $('<div style="text-align:center;"></div>');
    actWrap.append($('<span class="env-label">Active</span>'));
    actWrap.append($('<div style="display:flex;align-items:center;justify-content:center;height:30px;"></div>').append(activeInput));
    row1.append(actWrap);
    // Delete button
    var deleteBtn = $('<button type="button" class="env-btn env-btn-del" title="Delete"><i data-lucide="trash-2" style="width:14px;height:14px;"></i></button>');
    row1.append($('<div style="display:flex;align-items:end;"></div>').append(deleteBtn));
    cardDiv.append(row1);

    // ── ROW 2: Host · Context Root · Pool Size ──
    var row2 = $('<div class="env-grid" style="grid-template-columns: 5fr 3fr 60px; margin-bottom:6px;"></div>');
    row2.append(F("Host", ipInput));
    row2.append(F("Context Root", urlInput));
    row2.append(F("Pool", poolSizeInput));
    cardDiv.append(row2);

    // ── ROW 3: Var1 · Var2 · Secret1 · Expand ──
    var row3 = $('<div class="env-grid" style="grid-template-columns: 1fr 1fr 1fr 28px;"></div>');
    row3.append(F("Variable 1", var1Input));
    row3.append(F("Variable 2", var2Input));
    row3.append(F("Secret 1", secret1Input));
    var expandBtn = $('<button type="button" class="env-btn env-btn-expand" data-toggle="collapse" data-target="#envX' + n + '" title="More"><i data-lucide="chevron-down" style="width:14px;height:14px;"></i></button>');
    row3.append($('<div style="display:flex;align-items:end;"></div>').append(expandBtn));
    cardDiv.append(row3);

    // ── COLLAPSED SECTION ──
    var extra = $('<div class="collapse" id="envX' + n + '"></div>');
    var inner = $('<div class="env-separator"></div>');

    var er1 = $('<div class="env-grid" style="grid-template-columns: 1fr 1fr; margin-bottom:6px;"></div>');
    er1.append(F("URL Login", urlLoginInput));
    er1.append(F("Domain", domainInput));
    inner.append(er1);

    var er2 = $('<div class="env-grid" style="grid-template-columns: 1fr 1fr 1fr; margin-bottom:6px;"></div>');
    er2.append(F("Variable 3", var3Input));
    er2.append(F("Variable 4", var4Input));
    er2.append(F("Secret 2", secret2Input));
    inner.append(er2);

    var er3 = $('<div class="env-grid" style="grid-template-columns: 1fr 1fr;"></div>');
    er3.append(F("Mobile Activity", mobileActivity));
    er3.append(F("Mobile Package", mobilePackage));
    inner.append(er3);

    extra.append(inner);
    cardDiv.append(extra);

    cardTd.append(cardDiv);
    card.append(cardTd);

    // ── Events ──
    deleteBtn.click(function () {
        env.toDelete = !env.toDelete;
        cardDiv.toggleClass("env-deleted", env.toDelete);
    });
    selectEnvironment.change(function () { env.environment = $(this).val(); });
    selectCountry.change(function () { env.country = $(this).val(); });
    activeInput.change(function () { env.isActive = $(this).prop("checked"); });
    ipInput.change(function () { env.ip = $(this).val(); });
    domainInput.change(function () { env.domain = $(this).val(); });
    urlInput.change(function () { env.url = $(this).val(); });
    urlLoginInput.change(function () { env.urlLogin = $(this).val(); });
    var1Input.change(function () { env.var1 = $(this).val(); });
    var2Input.change(function () { env.var2 = $(this).val(); });
    var3Input.change(function () { env.var3 = $(this).val(); });
    var4Input.change(function () { env.var4 = $(this).val(); });
    secret1Input.change(function () { env.secret1 = $(this).val(); });
    secret2Input.change(function () { env.secret2 = $(this).val(); });
    poolSizeInput.change(function () { env.poolSize = $(this).val(); });
    mobileActivity.change(function () { env.mobileActivity = $(this).val(); });
    mobilePackage.change(function () { env.mobilePackage = $(this).val(); });

    env.environment = selectEnvironment.prop("value");
    env.country = selectCountry.prop("value");
    card.data("environment", env);
    table.append(card);

    if (window.lucide) lucide.createIcons();
}

function addNewEnvironmentRow() {
    var newEnvironment = {
        environment: "",
        country: "",
        isActive: true,
        ip: "",
        domain: "",
        url: "",
        urlLogin: "",
        var1: "",
        var2: "",
        var3: "",
        var4: "",
        secret1: "",
        secret2: "",
        poolSize: 10,
        mobileActivity: "",
        mobilePackage: "",
        toDelete: false
    };
    appendEnvironmentRow(newEnvironment);
    refreshPopoverDocumentation("editApplicationModalForm");

}

function displayWarningOnChangeApplicationKey() {
    // Compare with original value in order to display the warning message.
    let old1 = $("#originalApplication").val();
    let new1 = $('#editApplicationModal input[name="application"]').val();
    if (old1 !== new1) {
        var localMessage = new Message("WARNING", "If you rename that application, All the corresponding execution historic will stay on old application name.");
        showMessage(localMessage, $('#editApplicationModal'));
    } else {
        clearResponseMessage($('#editApplicationModal'));
    }
}

function aoColumnsFunc_object(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": "application",
            "sName": "application",
            "visible": false,
            "title": doc.getDocOnline("application", "Application")
        },
        {
            "data": "object",
            "sName": "object",
            "title": doc.getDocOnline("applicationObject", "Object")
        },
        {
            "data": "value",
            "sName": "value",
            "title": doc.getDocOnline("applicationObject", "Value")
        },
        {
            "data": "screenshotFilename",
            "sName": "screenshotFilename",
            "title": doc.getDocOnline("applicationObject", "ScreenshotFileName")
        },
        {
            "data": "usrCreated",
            "sName": "usrCreated",
            "visible": false,
            "title": doc.getDocOnline("transversal", "UsrCreated")
        },
        {
            "data": "dateCreated",
            "like": true,
            "visible": false,
            "sName": "dateCreated",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {
            "data": "dateModif",
            "like": true,
            "visible": false,
            "sName": "dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        }
    ];
    return aoColumns;
}

function app_keyispressed(e, modalName) {
    var idname = $('#' + modalName + 'ApplicationModal #application').val();
    var toto = "|\"|'|&|";
    var charval = "|" + e.key + "|";
    if (toto.indexOf(charval) !== -1) {
        var localMessage = new Message("WARNING", "Character '" + e.key + "' is not allowed for application names.");
        showMessage(localMessage, $('#' + modalName + 'ApplicationModal'), false, 1000);
        return false;
    }
    return true;
}

