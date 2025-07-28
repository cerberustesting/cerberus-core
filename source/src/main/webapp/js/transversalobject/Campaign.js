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
tinymce.init({
    selector: ".wysiwyg"
});


function renderOptionsForCampaign_Label(tableId) {
    var doc = new Doc();
    var data = getSelectInvariant("SYSTEM", false, false, "");
    $("#" + tableId + "_wrapper #addLabelTestcampaign").remove();
    var contentToAdd =
            "<div class='marginBottom10 form-inline' id='addLabelTestcampaign'>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='labelSelect' class='form-control' style='width:100%;' onchange='updateSelectLabel(\"" + tableId + "\")'>";
    for (var i = 0; i < data.find("option").length; i++) {
        contentToAdd +=
                "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    contentToAdd +=
            "</select>" +
            "</div>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='labelSelect2' class='form-control' style='width:100%;'>" +
            "</select>" +
            "</div>" +
            "<div class='form-group'>" +
            "<button type='button' id='addLabelTestcampaignButton' disabled='true' class='btn btn-primary' name='ButtonEdit' onclick='addLabelEntryClick(\"" + tableId + "\")'>" + doc.getDocLabel("page_testcampaign", "add_btn") + "</button>" +
            "</div>" +
            "</div>";
    $("#" + tableId + "_wrapper div#" + tableId + "_length").before(contentToAdd);
    $("#" + tableId + "_wrapper #labelSelect").select2({
        placeholder: "System",
        allowClear: true
    });

    $("#" + tableId + "_wrapper #labelSelect2").select2(camp_getComboConfigLabel("", "", tableId));
    $('#labelSelect2').on('select2:select', function (e) {
        $('#addLabelTestcampaignButton').prop("disabled", false);
    });

    updateSelectLabel(tableId);

}

function camp_getComboConfigLabel(labelType, system, tableId) {

    var config =
            {
                placeholder: {
                    id: '-1', // the value of the option
                    description: 'Select a Label',
                    label: 'Select a Label',
                    color: '#8b8b8c'
                },
                allowClear: true,
                ajax: {
                    url: "ReadLabel?bStrictSystemFilter=Y&iSortCol_0=0&sSortDir_0=desc&sColumns=type&iDisplayLength=30&sSearch_0=" + labelType + "&system=" + system,
                    dataType: 'json',
                    delay: 250,
                    data: function (params) {
                        params.page = params.page || 1;
                        return {
                            sSearch: params.term, // search term
                            iDisplayStart: (params.page * 30) - 30
                        };
                    },
                    processResults: function (data, params) {
                        params.page = params.page || 1;
                        return {
                            results: $.map(data.contentTable, function (obj) {
                                if (!(findValueTableDataByCol(tableId, 2, obj.id))) {
                                    return {
                                        id: obj.id,
                                        label: obj.label,
                                        color: obj.color,
                                        description: obj.description,
                                        system: obj.system,
                                        type: obj.type
                                    };
                                }
                            }),
                            pagination: {
                                more: (params.page * 30) < data.iTotalRecords
                            }
                        };
                    },
                    cache: true,
                    allowClear: true
                },
                width: "100%",
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                minimumInputLength: 0,
                templateResult: camp_comboConfigLabel_format, // omitted for brevity, see the source of this page
                templateSelection: camp_comboConfigLabel_formatSelection // omitted for brevity, see the source of this page
            };

    return config;
}

function camp_comboConfigLabel_format(label) {
    var markup = "<div class='select2-result-tag clearfix'>" +
            "<div style='float:left;'><span class='label label-primary' style='background-color:"
            + label.color + "' data-toggle='tooltip' data-labelid='"
            + label.id + "' title='"
            + label.description + "'>"
            + label.label + "</span> ["
            + label.system + "]</div>";

    markup += "</div>";

    return markup;
}

function camp_comboConfigLabel_formatSelection(label) {
    var result = label.id;
    if (!isEmpty(label.label)) {
        result = "<div style='float:left;height: 34px'><span class='label label-primary' style='background-color:"
                + label.color + "' data-toggle='tooltip' data-labelid='"
                + label.id + "' title='"
                + label.description + "'>"
                + label.label + "</span></div>";
    }
    return result;
}

function renderOptionsForCampaign_Parameter(id) {
    var doc = new Doc();
    var data = getSelectInvariant("CAMPAIGN_PARAMETER", false, false);
    $("#" + id + "_wrapper #addParameterTestcampaign").remove();
    var contentToAdd =
            "<div class='marginBottom10 form-inline' id='addParameterTestcampaign'>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='parameterTestSelect' class='form-control' style='width:100%;' onchange='updateSelectParameter(\"" + id + "\")'>";
    for (var i = 0; i < data.find("option").length; i++) {
        contentToAdd +=
                "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    contentToAdd +=
            "</select>" +
            "</div>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='parameterTestSelect2' class='form-control' style='width:100%;'>" +
            "</select>" +
            "</div>" +
            "<div class='form-group'>" +
            "<button type='button' id='addParameterTestcampaignButton' class='btn btn-primary' name='ButtonEdit' onclick='addParameterEntryClick(\"" + id + "\")'>" + doc.getDocLabel("page_testcampaign", "add_btn") + "</button>" +
            "</div>" +
            "</div>";
    $("#" + id + "_wrapper div#" + id + "_length").before(contentToAdd);
    $("#" + id + "_wrapper #parameterTestSelect").select2();
    $("#" + id + "_wrapper #parameterTestSelect2").select2();
    updateSelectParameter(id);

}

function renderOptionsForCampaign_TestcaseCriterias(id) {
    var doc = new Doc();
    var data = getSelectInvariant("CAMPAIGN_TCCRITERIA", false, false);
    $("#" + id + "_wrapper #addParameterTestcase").remove();
    var contentToAdd =
            "<div class='marginBottom10 form-inline' id='addParameterTestcase'>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='criteriaTestSelect' class='form-control' style='width:100%;' onchange='updateSelectCriteria(\"" + id + "\")'>";
    for (var i = 0; i < data.find("option").length; i++) {
        contentToAdd +=
                "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    contentToAdd +=
            "</select>" +
            "</div>" +
            "<div class='form-group marginRight10 col-sm-3' style='padding-right: 0px; padding-left: 0px;'>" +
            "<select id='criteriaTestSelect2' class='form-control' style='width:100%;'>" +
            "</select>" +
            "</div>" +
            "<div class='form-group'>" +
            "<button type='button' id='addCriteriaTestcampaignButton' class='btn btn-primary' name='ButtonEdit' onclick='addCriteriaEntryClick(\"" + id + "\")'>" + doc.getDocLabel("page_testcampaign", "add_btn") + "</button>" +
            "</div>" +
            "</div>";
    $("#" + id + "_wrapper div#" + id + "_length").before(contentToAdd);
    $("#" + id + "_wrapper #criteriaTestSelect").select2();
    $("#" + id + "_wrapper #criteriaTestSelect2").select2();
    updateSelectCriteria(id);

}

function renderOptionsForCampaign_TestCase(data) {
}

function viewEntryClick(param) {
    clearResponseMessageMainPage();

    var doc = new Doc();
    var formEdit = $('#viewTestcampaignModal');

    $("[name='viewTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "viewtestcampaign_field") + " : " + param);

    $("#campaignKey").val(param);

    showLoader("#testcampaignList");


    var jqxhr = $.getJSON("ReadCampaign?testcases=true&", "campaign=" + encodeURIComponent(param));
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        /* TESTCASE */

        var array = [];

        $.each(obj.testcase, function (e) {
            array.push(
                    [obj.testcase[e].test, obj.testcase[e].testCase, obj.testcase[e].application, obj.testcase[e].description, obj.testcase[e].status]
                    );
        });

        if ($("#viewTestcampaignModal #viewTestcampaignsTable_wrapper").length > 0) {
            $("#viewTestcampaignModal #viewTestcampaignsTable").DataTable().clear();
            $("#viewTestcampaignModal #viewTestcampaignsTable").DataTable().rows.add(array).draw();

        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("viewTestcampaignsTable", array, aoColumnsFunc_TestCase(), true);
            createDataTableWithPermissions(configurations, renderOptionsForCampaign_TestCase, "#viewTestcampaignList", undefined, true);
        }

        hideLoader("#testcampaignList");

        formEdit.modal('show');

    });
    refreshPopoverDocumentation("editTestcampaignModal");
}

function viewStatEntryClick(param) {
    clearResponseMessageMainPage();

    var doc = new Doc();
    var formEdit = $('#viewStatcampaignModal');

    $("[name='viewStatcampaignField']").html(doc.getDocLabel("page_testcampaign", "viewstatcampaign_field") + " : " + param);

    $("#campaignKey").val(param);

    showLoader("#testcampaignList");


    var jqxhr = $.getJSON("ReadCampaign?tags=true&", "campaign=" + encodeURIComponent(param));
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        /* TAG */

        var array = [];

        $.each(obj.tags, function (e) {
            array.push(
                    [obj.tags[e], obj.tags[e].tag, obj.tags[e].nbExe, obj.tags[e].nbExeUsefull, obj.tags[e].DateCreated, obj.tags[e].DateEndQueue, obj.tags[e].ciResult, obj.tags[e].ciScore]
                    );
        });

        if ($("#viewStatcampaignModal #viewTagcampaignsTable_wrapper").length > 0) {
            $("#viewStatcampaignModal #viewTagcampaignsTable").DataTable().clear();
            $("#viewStatcampaignModal #viewTagcampaignsTable").DataTable().rows.add(array).draw();

        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("viewTagcampaignsTable", array, aoColumnsFunc_Tag(), true, [1, 'desc']);
            createDataTableWithPermissions(configurations, renderOptionsForCampaign_TestCase, "#viewTagcampaignList", undefined, true);
        }


        hideLoader("#testcampaignList");

        formEdit.modal('show');

    });

}

function viewEntryModalCloseHandler() {
    // reset form values
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#viewTestcampaignModal'));
}

function editEntryClick(param) {
    clearResponseMessageMainPage();

    //Store the campaign name
    $("#campaignKey").val(param);

    $('#addTestcampaignButton').attr('class', '');
    $('#addTestcampaignButton').attr('hidden', 'hidden');
    $('#editTestcampaignButton').attr('class', 'btn btn-primary');
    $('#editTestcampaignButton').removeAttr('hidden');

    // handle the click for specific action buttons
    $("#editTestcampaignButton").off("click");
    $("#editTestcampaignButton").click(editEntryModalSaveHandler);
    $("#addTestcampaignButton").off("click");
    $("#addTestcampaignButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editTestcampaignModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addTestcampaignModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#viewTestcampaignModal').on('hidden.bs.modal', viewEntryModalCloseHandler);

    var formEdit = $('#editTestcampaignModal');

    showLoader("#testcampaignList");

    var jqxhr = $.getJSON("ReadCampaign?parameters=true&labels=true&eventHooks=true&scheduledEntries=true", "campaign=" + encodeURIComponent(param));
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];
        var parameters = [];
        var criterias = [];
        for (var i = 0; i < obj.parameters.length; i++) {
            if ((obj.parameters[i].parameter === "BROWSER")
                    || (obj.parameters[i].parameter === "COUNTRY")
                    || (obj.parameters[i].parameter === "ENVIRONMENT")
                    || (obj.parameters[i].parameter === "ROBOT")) {
                parameters.push(obj.parameters[i]);
            } else {
                criterias.push(obj.parameters[i]);
            }
        }

        formEdit.find("#campaign").prop("value", obj["campaign"]);
        formEdit.find("#originalCampaign").prop("value", obj["campaign"]);
        formEdit.find("#notifystart").val(obj["notifyStartTagExecution"]);
        formEdit.find("#notifyend").val(obj["notifyEndTagExecution"]);
        formEdit.find("#distriblist").prop("value", obj["distribList"]);
        formEdit.find("#notifySlackstart").val(obj["SlackNotifyStartTagExecution"]);
        formEdit.find("#notifySlackend").val(obj["SlackNotifyEndTagExecution"]);
        formEdit.find("#webhook").val(obj["SlackWebhook"]);
        formEdit.find("#channel").val(obj["SlackChannel"]);
        formEdit.find("#cIScoreThreshold").val(obj["CIScoreThreshold"]);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#group1").prop("value", obj["group1"]);
        formEdit.find("#group2").prop("value", obj["group2"]);
        formEdit.find("#group3").prop("value", obj["group3"]);
        if (tinyMCE.get('longDescription') !== null)
            tinyMCE.get('longDescription').setContent(obj["longDescription"]);

//        formEdit.find("#longDescription").prop("value", obj["longDescription"]);
        formEdit.find("#id").prop("value", obj["campaignID"]);

        formEdit.find("#tag").prop("value", obj["Tag"]);
        formEdit.find("#verbose").prop("value", obj["Verbose"]);
        formEdit.find("#screenshot").prop("value", obj["Screenshot"]);
        formEdit.find("#video").prop("value", obj["Video"]);
        formEdit.find("#pageSource").prop("value", obj["PageSource"]);
        formEdit.find("#robotLog").prop("value", obj["RobotLog"]);
        formEdit.find("#consoleLog").prop("value", obj["ConsoleLog"]);
        formEdit.find("#retries").prop("value", obj["Retries"]);
        formEdit.find("#timeout").prop("value", obj["Timeout"]);
        formEdit.find("#priority").prop("value", obj["Priority"]);
        formEdit.find("#manualExecution").prop("value", obj["ManualExecution"]);

        formEdit.find("#usrcreated").prop("value", obj["UsrCreated"]);
        formEdit.find("#datecreated").prop("value", getDate(obj["DateCreated"]));
        formEdit.find("#usrmodif").prop("value", obj["UsrModif"]);
        formEdit.find("#datemodif").prop("value", getDate(obj["DateModif"]));

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#campaign").prop("readonly", "readonly");
            formEdit.find("#description").prop("readonly", "readonly");
            formEdit.find("#group1").prop("readonly", "readonly");
            formEdit.find("#group2").prop("readonly", "readonly");
            formEdit.find("#group3").prop("readonly", "readonly");
            formEdit.find("#id").prop("readonly", "readonly");

            $('#editTestcampaignButton').attr('class', '');
            $('#editTestcampaignButton').attr('hidden', 'hidden');
        }

        /* LABEL */

        var array = [];

        $.each(obj.labels, function (e) {
            array.push(
                    [obj.labels[e].campaign, obj.labels[e].campaignLabelID, obj.labels[e].LabelId, obj.labels[e].label.system, obj.labels[e].label.label, obj.labels[e].label.color, obj.labels[e].label.description, obj.labels[e].label.type]
                    );
        });

        if ($("#editTestcampaignModal #labelTestcampaignsTable_wrapper").length > 0) {
            $("#editTestcampaignModal #labelTestcampaignsTable").DataTable().clear();
            $("#editTestcampaignModal #labelTestcampaignsTable").DataTable().rows.add(array).draw();
        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("labelTestcampaignsTable", array, aoColumnsFunc_Label("labelTestcampaignsTable"), true);
            createDataTableWithPermissions(configurations, null, "#labelTestcampaignList", undefined, true);
        }
        renderOptionsForCampaign_Label("labelTestcampaignsTable");

        /* PARAMETERS */

        var array = [];

        $.each(parameters, function (e) {
            array.push([parameters[e].campaign, parameters[e].campaignparameterID, parameters[e].parameter, parameters[e].value]);
        });

        if ($("#editTestcampaignModal #parameterTestcampaignsTable_wrapper").length > 0) {
            $("#editTestcampaignModal #parameterTestcampaignsTable").DataTable().clear();
            $("#editTestcampaignModal #parameterTestcampaignsTable").DataTable().rows.add(array).draw();
        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("parameterTestcampaignsTable", array, aoColumnsFunc_Parameter("parameterTestcampaignsTable"), true);
            createDataTableWithPermissions(configurations, null, "#parameterTestcampaignList", undefined, true);
        }
        renderOptionsForCampaign_Parameter("parameterTestcampaignsTable");
        hideLoader("#testcampaignList");

        $('#editTestcampaignModal .nav-tabs a[href="#tabsCreate-1"]').tab('show');
        formEdit.modal('show');

        /* CRITERIAS */

        var array = [];

        $.each(criterias, function (e) {
            array.push([criterias[e].campaign, criterias[e].campaignparameterID, criterias[e].parameter, criterias[e].value]);
        });

        if ($("#editTestcampaignModal #parameterTestcaseTable_wrapper").length > 0) {
            $("#editTestcampaignModal #parameterTestcaseTable").DataTable().clear();
            $("#editTestcampaignModal #parameterTestcaseTable").DataTable().rows.add(array).draw();
        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("parameterTestcaseTable", array, aoColumnsFunc_Parameter("parameterTestcaseTable"), true);
            createDataTableWithPermissions(configurations, null, "#parameterTestcaseTable", undefined, true);
        }

        renderOptionsForCampaign_TestcaseCriterias("parameterTestcaseTable");

        /* SCHEDULER */
        var doc = new Doc();
        $("[name='lbl_cronexp']").html(doc.getDocOnline("scheduler", "cronexp"));

        $('#editTestcampaignModal .nav-tabs a[href="#tabsCreate-1"]').tab('show');
        $('#addScheduleEntry').off('click');
        $('#addScheduleEntry').click(addNewSchedulerRow);

        loadSchedulerTable(obj);

        /* EVENTHOOK */
        $('#addEventHook').off('click');
        $('#addEventHook').click(addNewEventHookRow);
        loadEventHookTable(obj);


        hideLoader("#testcampaignList");
        formEdit.modal('show');

    });

}

function loadCronList(field) {
    var cronList = [];
    cronList.push("0 0 12 1/1 * ? *");
    cronList.push("0 0 12 ? * MON-FRI *");
    cronList.push("0 0/10 * 1/1 * ? *");
    field.autocomplete({
        source: cronList,
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

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editTestcampaignModal'));
    var formEdit = $('#editTestcampaignModal #editTestcampaignModalForm');
    tinyMCE.triggerSave();

    var sa = formEdit.serializeArray();
    var data = {};
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    // Getting Data from Scheduler TAB
    var table1 = $("#schedulerTableBody tr.dataField");
    var table_scheduler = [];
    for (var i = 0; i < table1.length; i++) {
        table_scheduler.push($(table1[i]).data("scheduler"));
    }
    // Getting Data from Notifications TAB
    var table1 = $("#eventHookTableBody tr");
    var table_events = [];
    for (var i = 0; i < table1.length; i++) {
        table_events.push($(table1[i]).data("eventHook"));
    }

    var labels = null;
    if ($("#labelTestcampaignsTable_wrapper").length > 0) {
        labels = $("#labelTestcampaignsTable").DataTable().data().toArray();
    }

    var parameters = null;
    if ($("#parameterTestcampaignsTable_wrapper").length > 0) {
        parameters = $("#parameterTestcampaignsTable").DataTable().data().toArray();
    }

    var criterias = null;
    if ($("#parameterTestcaseTable_wrapper").length > 0) {
        criterias = $("#parameterTestcaseTable").DataTable().data();
        for (let i = 0; i < criterias.length; i++) {
            parameters.push(criterias[i]);
        }
    }

// Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestcampaignModal');
    $.ajax({
        url: "UpdateCampaign",
        async: true,
        method: "POST",
        data: {
            Campaign: data.campaign,
            OriginalCampaign: data.originalCampaign,
            CampaignID: data.id,
            CIScoreThreshold: data.cIScoreThreshold,
            Description: data.description,
            Group1: data.group1,
            Group2: data.group2,
            Group3: data.group3,
            LongDescription: data.longDescription,
            Labels: JSON.stringify(labels),
            Parameters: JSON.stringify(parameters),
            ScheduledEntries: JSON.stringify(table_scheduler),
            EventEntries: JSON.stringify(table_events),
            Tag: data.tags,
            Screenshot: data.screenshot,
            Video: data.video,
            Verbose: data.verbose,
            PageSource: data.pageSource,
            RobotLog: data.robotLog,
            ConsoleLog: data.consoleLog,
            Timeout: data.timeout,
            Retries: data.retries,
            Priority: data.priority,
            ManualExecution: data.manualExecution
        },
        success: function (data) {
//            data = JSON.parse(data);
            hideLoaderInModal('#editTestcampaignModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(false);
                $('#editTestcampaignModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editTestcampaignModal'));
            }
        },
        error: showUnexpectedError
    });


}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editTestcampaignModal #editTestcampaignModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editTestcampaignModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();

    $('#editTestcampaignButton').attr('class', '');
    $('#editTestcampaignButton').attr('hidden', 'hidden');
    $('#addTestcampaignButton').attr('class', 'btn btn-primary');
    $('#addTestcampaignButton').removeAttr('hidden');

    // handle the click for specific action buttons
    $("#editTestcampaignButton").off("click");
    $("#editTestcampaignButton").click(editEntryModalSaveHandler);
    $("#addTestcampaignButton").off("click");
    $("#addTestcampaignButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editTestcampaignModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addTestcampaignModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#viewTestcampaignModal').on('hidden.bs.modal', viewEntryModalCloseHandler);

    $("#editTestcampaignModal #campaign").empty();
    $("#editTestcampaignModal #originalCampaign").empty();

    $("#editTestcampaignModal #campaign").removeAttr("readonly");

    // LABEL
    if ($("#labelTestcampaignsTable_wrapper").length > 0) {
        $("#labelTestcampaignsTable").DataTable().clear().draw();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("labelTestcampaignsTable", null, aoColumnsFunc_Label("labelTestcampaignsTable"), true);
        createDataTableWithPermissions(configurations, null, "#labelTestcampaignList", undefined, true);
    }
    renderOptionsForCampaign_Label("labelTestcampaignsTable");

    // PARAMETER
    if ($("#parameterTestcampaignsTable_wrapper").length > 0) {
        $("#parameterTestcampaignsTable").DataTable().clear().draw();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("parameterTestcampaignsTable", null, aoColumnsFunc_Parameter("parameterTestcampaignsTable"), true);
        createDataTableWithPermissions(configurations, null, "#parameterTestcampaignList", undefined, true);
    }
    renderOptionsForCampaign_Parameter("parameterTestcampaignsTable");

    // CRITERIA
    if ($("#parameterTestcaseTable_wrapper").length > 0) {
        $("#parameterTestcaseTable").DataTable().clear().draw();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("parameterTestcaseTable", null, aoColumnsFunc_Parameter("parameterTestcaseTable"), true);
        createDataTableWithPermissions(configurations, null, "#parameterTestcaseList", undefined, true);
    }
    renderOptionsForCampaign_TestcaseCriterias("parameterTestcaseTable");

    $('#editTestcampaignModal .nav-tabs a[href="#tabsCreate-1"]').tab('show');
    $('#editTestcampaignModal').modal('show');

    /* SCHEDULER */
    var doc = new Doc();
    $('#schedulerTableBody tr').remove();
    $('#addScheduleEntry').off('click');
    $('#addScheduleEntry').click(addNewSchedulerRow);
    //    
    loadSchedulerTable("");

    /* EVENTHOOK */
    $('#eventHookTableBody tr').remove();
    $('#addEventHook').off('click');
    $('#addEventHook').click(addNewEventHookRow);
    loadEventHookTable("");

}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#editTestcampaignModal'));
    var formEdit = $('#editTestcampaignModal #editTestcampaignModalForm');

    var sa = formEdit.serializeArray();
    var data = {};
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    // Getting Data from Scheduler TAB
    var table1 = $("#schedulerTableBody tr.dataField");
    var table_scheduler = [];
    for (var i = 0; i < table1.length; i++) {
        table_scheduler.push($(table1[i]).data("scheduler"));
    }
    // Getting Data from Notifications TAB
    var table1 = $("#eventHookTableBody tr");
    var table_events = [];
    for (var i = 0; i < table1.length; i++) {
        table_events.push($(table1[i]).data("eventHook"));
    }


    var labels = null;
    if ($("#labelTestcampaignsTable_wrapper").length > 0) {
        labels = $("#labelTestcampaignsTable").DataTable().data().toArray();
    }
    for (var i = 0; i < labels.length; i++) {
        labels[i][0] = data.campaign;
    }

    var parameters = null;
    if ($("#parameterTestcampaignsTable_wrapper").length > 0) {
        parameters = $("#parameterTestcampaignsTable").DataTable().data().toArray();
    }

    var criterias = null;
    if ($("#parameterTestcaseTable").length > 0) {
        criterias = $("#parameterTestcaseTable").DataTable().data();
        for (let i = 0; i < criterias.length; i++) {
            parameters.push(criterias[i]);
        }
    }
    for (var i = 0; i < parameters.length; i++) {
        parameters[i][0] = data.campaign;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestcampaignModal');
    $.ajax({
        url: "CreateCampaign",
        async: true,
        method: "POST",
        data: {
            Campaign: data.campaign,
            CIScoreThreshold: data.cIScoreThreshold,
            Description: data.description,
            Group1: data.group1,
            Group2: data.group2,
            Group3: data.group3,
            LongDescription: data.longDescription,
            Labels: JSON.stringify(labels),
            Parameters: JSON.stringify(parameters),
            Tag: data.tags,
            ScheduledEntries: JSON.stringify(table_scheduler),
            EventEntries: JSON.stringify(table_events),
            Screenshot: data.screenshot,
            Video: data.video,
            Verbose: data.verbose,
            PageSource: data.pageSource,
            RobotLog: data.robotLog,
            ConsoleLog: data.consoleLog,
            Timeout: data.timeout,
            Retries: data.retries,
            Priority: data.priority,
            ManualExecution: data.manualExecution
        },
        success: function (data) {
//            data = JSON.parse(data);
            hideLoaderInModal('#editTestcampaignModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(false);
                $('#editTestcampaignModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editTestcampaignModal'));
            }
        },
        error: showUnexpectedError
    });

}

function addEntryModalCloseHandler() {
    // reset form values
    $('#editTestcampaignModal #editTestcampaignModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editTestcampaignModal'));
}

function addLabelEntryClick(tableId) {

    var v = $('#labelSelect2').select2('data')[0];

    $("#" + tableId).DataTable().row.add([
        $("#campaignKey").val(),
        0,
        v.id,
        v.system,
        v.label,
        v.color,
        v.description,
        v.type
    ]).draw();
    $('#labelSelect2').val("").trigger('change');
    $('#addLabelTestcampaignButton').prop("disabled", true);
}

function removeLabelEntryClick(tableId, key) {
    $('#' + tableId + '_wrapper #removeLabel').filter(function (i, e) {
        return $(e).attr("key") == key;
    }).off().prop("disabled", true);
    $("#" + tableId).DataTable().rows(function (i, d, n) {
        return d[2] == key;
    }).remove().draw();
    updateSelectLabel(tableId);
}

function updateSelectLabel(tableId) {
    var val = $("#" + tableId + '_wrapper #labelSelect').find(":selected").val();

    $("#" + tableId + "_wrapper #labelSelect2").val(null).trigger('change');
    $("#" + tableId + "_wrapper #labelSelect2").select2(camp_getComboConfigLabel("", val, tableId));

    $("#addLabelTestcampaignButton").prop("disabled", true);

    $("#" + tableId + '_wrapper #addLabelTestcampaignButton').prop("disabled", false);
}

function addParameterEntryClick(tableId) {
    $("#" + tableId + '_wrapper #addParameterTestcampaignButton').off().prop("disabled", true);
    $("#" + tableId).DataTable().row.add([$("#campaignKey").val()
                , 0
                , $("#" + tableId + '_wrapper #parameterTestSelect').find(":selected").val()
                , $("#" + tableId + '_wrapper #parameterTestSelect2').find(":selected").val()]).draw();
    updateSelectParameter(tableId);
}

function addCriteriaEntryClick(tableId) {
    $("#" + tableId + '_wrapper #addCriteriaTestcampaignButton').off().prop("disabled", true);
    $("#" + tableId).DataTable().row.add([$("#campaignKey").val()
                , 0
                , $("#" + tableId + '_wrapper #criteriaTestSelect').find(":selected").val()
                , $("#" + tableId + '_wrapper #criteriaTestSelect2').find(":selected").val()]).draw();
    updateSelectCriteria(tableId);
}

function removeParameterEntryClick(tableId, key, key1) {
    $('#' + tableId + '_wrapper #removeTestbattery').filter(function (i, e) {
        return $(e).attr("key") == key && $(e).attr("key1") === key1;
    }).off().prop("disabled", true);
    $("#" + tableId).DataTable().rows(function (i, d, n) {
        return d[2] === key && d[3] === key1;
    }).remove().draw();
    updateSelectParameter(tableId);
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function updateSelectParameter(id) {
    var val = $("#" + id + '_wrapper #parameterTestSelect').find(":selected").val();
    var data = [];

    if (val === "ROBOT") {
        data = getSelectRobot(true, true);
    } else {
        data = getSelectInvariant(val, false, false);
    }
    $("#" + id + "_wrapper #parameterTestSelect2").empty();
    var optionList = "";
    for (var i = 0; i < data.find("option").length; i++) {
        if (!(findValueTableDataByCol(id, 2, val) && findValueTableDataByCol(id, 3, data.find("option")[i].value)))
            optionList += "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    $("#" + id + "_wrapper #parameterTestSelect2").append(optionList);
    if ($("#" + id + '_wrapper #parameterTestSelect2 option').size() <= 0) {
        $("#" + id + '_wrapper #parameterTestSelect2').parent().hide();
        $("#" + id + '_wrapper #addParameterTestcampaignButton').prop("disabled", true);
    } else {
        $("#" + id + '_wrapper #parameterTestSelect2').parent().show();
        $("#" + id + '_wrapper #addParameterTestcampaignButton').bind("click", function () {
            addParameterEntryClick(id);
        }).prop("disabled", false);
    }
}

function updateSelectCriteria(id) {
    var val = $("#" + id + '_wrapper #criteriaTestSelect').find(":selected").val();
    var data = [];
    if (val === "STATUS") {
        data = getSelectInvariant("TCSTATUS", false, false);
    } else if (val === "TYPE") {
        data = getSelectInvariant("TESTCASE_TYPE", false, false);
    } else if (val === "APPLICATION") {
        data = getSelectApplicationWithoutSystem();
    } else if (val === "TESTFOLDER") {
        data = getSelectFolder();
    } else {
        data = getSelectInvariant(val, false, false);
    }

    $("#" + id + "_wrapper #criteriaTestSelect2").empty();
    var optionList = "";
    for (var i = 0; i < data.find("option").length; i++) {
        if (!(findValueTableDataByCol(id, 2, val) && findValueTableDataByCol(id, 3, data.find("option")[i].value)))
            optionList +=
                    "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    $("#" + id + "_wrapper #criteriaTestSelect2").append(optionList);
    if ($("#" + id + '_wrapper #criteriaTestSelect2 option').size() <= 0) {
        $("#" + id + '_wrapper #criteriaTestSelect2').parent().hide();
        $("#" + id + '_wrapper #addCriteriaTestcampaignButton').prop("disabled", true);
    } else {
        $("#" + id + '_wrapper #criteriaTestSelect2').parent().show();
        $("#" + id + '_wrapper #addCriteriaTestcampaignButton').bind("click", function () {
            addCriteriaEntryClick(id);
        }).prop("disabled", false);
    }
}

function findValueTableDataByCol(tableId, colIndex, value) {
    var result = false;
    //Iterate all td's in second column
    $.each($("#" + tableId).DataTable().rows().data(), function (i, v) {
        if (v[colIndex] == value) {
            result = true;
        }
    });
    return result;
}

function aoColumnsFunc_Label(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var removeButton = '<button id="removeTestlabel" key="' + obj[2] + '" onclick="removeLabelEntryClick(\'' + tableId + '\',\'' + obj[2] + '\');"\n\
                                        class="removeTestlabel btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestlabel" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group">' + removeButton + '</div>';

            }
        },
        {"data": "2", "sName": "labelId", "title": doc.getDocLabel("label", "id")},
        {"data": "3", "sName": "system", "title": doc.getDocLabel("label", "system")},
        {
            "data": "4",
            "sName": "label",
            "title": doc.getDocLabel("label", "label"),
            "mRender": function (data, type, obj) {
                var result = "<div style='float:left;height: 34px'><span class='label label-primary' style='background-color:"
                        + obj[5] + "' data-toggle='tooltip' data-labelid='"
                        + obj[2] + "' title='"
                        + obj[6] + "'>"
                        + obj[4] + "</span></div>";
                return result;

            }
        },
        {"data": "7", "sName": "type", "title": doc.getDocLabel("label", "type")}
    ];
    return aoColumns;
}

function aoColumnsFunc_Parameter(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var removeButton = '<button id="removeTestparameter" key="' + obj[2] + '" key1="' + obj[3] + '" onclick="removeParameterEntryClick(\'' + tableId + '\',\'' + obj[2] + '\',\'' + obj[3] + '\');"\n\
                                        class="removeTestparameter btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestparameter" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group">' + removeButton + '</div>';

            }
        },
        {"data": "2", "sName": "parameter", "title": doc.getDocLabel("page_testcampaign", "parameter_col")},
        {"data": "3", "sName": "value", "title": doc.getDocLabel("page_testcampaign", "value_col")}
    ];
    return aoColumns;
}

function aoColumnsFunc_TestCase() {
    var doc = new Doc();
    var aoColumns = [
        {"data": "0", "sName": "tbc.Test", "title": doc.getDocLabel("test", "Test")},
        {
            "data": "1", "sName": "tbc.Testcase", "title": doc.getDocLabel("testcase", "TestCase"),
            "mRender": function (data, type, obj) {
                return "<a target=\"_blank\" href='TestCaseScript.jsp?test=" + obj[0] + "&testcase=" + obj[1] + "'>" + obj[1] + "</a>";
            }
        },
        {"data": "2", "sName": "tbc.application", "title": doc.getDocLabel("application", "Application")},
        {"data": "3", "sName": "tbc.description", "title": doc.getDocLabel("testcase", "Description")},
        {"data": "4", "sName": "tec.status", "title": doc.getDocLabel("testcase", "Status")}
    ];
    return aoColumns;
}

function aoColumnsFunc_Tag() {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": "1",
            "sName": "Tag",
            "sWidth": "250px",
            "title": doc.getDocOnline("tag", "tag")
        },
        {
            "data": "4",
            "sName": "DateCreated",
            "sWidth": "150px",
            "title": doc.getDocLabel("page_tag", "datecreated"),
            "mRender": function (data, type, oObj) {
                if (type === "display") {
                    return getDate(oObj["4"]);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "6",
            "sName": "ciresult",
            "sWidth": "40px",
            "mRender": function (data, type, obj) {
                if (isEmpty(obj[0].ciResult)) {
                    return "";
                } else {
                    return '<div class="progress-bar status' + obj[0].ciResult + '" role="progressbar" style="width:100%">' + obj[0].ciResult + '</div>';
                }
            },
            "title": doc.getDocOnline("tag", "ciresult")
        },
        {
            "data": null,
            "sName": "result",
            "sWidth": "150px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                if ((type === "display") || (type === "filter")) {
                    return result(obj[0]);
                } else {
                    if (obj[0].nbExeUsefull > 0) {
                        return ((obj[0].nbOK * 100 / obj[0].nbExeUsefull) * 1000000)
                                + ((obj[0].nbKO * 100 / obj[0].nbExeUsefull) * 10000)
                                + ((obj[0].nbFA * 100 / obj[0].nbExeUsefull) * 100)
                                + ((obj[0].nbNA * 100 / obj[0].nbExeUsefull));
                    } else {
                        return 0;
                    }
                }
            },
            "title": doc.getDocLabel("page_tag", "result")
        },
        {
            "data": null,
            "sName": "duration",
            "sWidth": "40px",
            "bSearchable": false,
            className: 'dt-body-right',
            "mRender": function (data, type, obj) {
                let tmp = getDuration(obj[0]);
                if (type === "display") {
                    return tmp.disp;
                } else {
                    return tmp.data;
                }
            },
            "title": doc.getDocOnline("page_tag", "duration")
        },
        {
            "data": "3",
            "sName": "nbExeUsefull",
            "sWidth": "40px",
            "bSearchable": false,
            "title": doc.getDocOnline("tag", "nbexeusefull")
        },
        {
            "data": null,
            "sName": "reliability",
            "sWidth": "150px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                return reliability(obj[0]);
            },
            "title": doc.getDocOnline("page_tag", "reliability")
        },
        {
            "data": null,
            "sName": "exepermin",
            "sWidth": "40px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var dur = getDuration(obj[0]);
                if (dur.data > 0) {
                    return Math.round((obj[0].nbExe / (dur.data / 60)) * 10) / 10;
                }
                return "";
            },
            "title": doc.getDocOnline("page_tag", "exepermin")
        },
        {
            "data": "2",
            "sName": "nbExe",
            "sWidth": "40px",
            "bSearchable": false,
            "title": doc.getDocOnline("tag", "nbexe")
        },
        {
            "data": "7",
            "sName": "ciscore",
            "sWidth": "40px",
            "bSearchable": false,
            "title": doc.getDocOnline("tag", "ciscore")
        },
        {
            "data": "5",
            "sName": "DateEndQueue",
            "sWidth": "150px",
            "mRender": function (data, type, obj) {
                if (type === "display") {
                    return getDate(obj[0].DateEndQueue);
                } else {
                    return obj[0].DateEndQueue;
                }
            },
            "title": doc.getDocOnline("tag", "dateendqueue")
        }
    ];
    return aoColumns;
}

function reliability(tag) {
    if (tag.nbExe > 0) {
        var per = (tag.nbExeUsefull / tag.nbExe) * 100;
        var roundPercent = Math.round(per * 10) / 10;
        var satcolor = getGreenToRed(per);
        return "<div class=\"progress-bar\" role=\"progressbar\" style=\"width:" + per + "%; background-color: " + satcolor + "\">" + roundPercent + "%</div>";
    } else {
        return "";
    }
}

function getGreenToRed(percent) {
    r = percent < 50 ? 255 : Math.floor(255 - (percent * 2 - 100) * 255 / 100);
    g = percent > 50 ? 255 : Math.floor((percent * 2) * 255 / 100);
    return 'rgb(' + r + ',' + g + ',0)';
}

function result(tag) {
    var progress = "";
    if (tag.nbExeUsefull > 0) {
        progress += "<div class=\"progress-bar statusOK\" role=\"progressbar\" style=\"width:" + (tag.nbOK * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbOK * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusKO\" role=\"progressbar\" style=\"width:" + (tag.nbKO * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbKO * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusFA\" role=\"progressbar\" style=\"width:" + (tag.nbFA * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbFA * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusNA\" role=\"progressbar\" style=\"width:" + (tag.nbNA * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbNA * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusNE\" role=\"progressbar\" style=\"width:" + (tag.nbNE * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbNE * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusWE\" role=\"progressbar\" style=\"width:" + (tag.nbWE * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbWE * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusPE\" role=\"progressbar\" style=\"width:" + (tag.nbPE * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbPE * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusQU\" role=\"progressbar\" style=\"width:" + (tag.nbQU * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbQU * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusQE\" role=\"progressbar\" style=\"width:" + (tag.nbQE * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbQE * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
        progress += "<div class=\"progress-bar statusCA\" role=\"progressbar\" style=\"width:" + (tag.nbCA * 100 / tag.nbExeUsefull) + "%;\">" + Math.round((tag.nbCA * 100 / tag.nbExeUsefull) * 10) / 10 + "%</div>";
    }
    return progress;
}

function getDuration(tag) {
    let res = {};
    var startTime = new Date(tag.DateStartExe);
    var endTime = new Date(tag.DateEndQueue);
    var diff = (endTime - startTime) / 1000;
    res.disp = getHumanReadableDuration(diff);
    res.data = diff;
    return res;
}

function loadSchedulerTable(name) {
    $('#schedulerTableBody tr').remove();

    if (name !== "") {
        name.scheduledEntries.forEach(function (obj, idx) {
            obj.toDelete = false;
            appendSchedulerRow(obj);
        });
    }
    refreshPopoverDocumentation("schedulerTable");
}

function loadEventHookTable(name) {
    $('#eventHookTableBody tr').remove();

    if (name !== "") {
        name.eventHooks.forEach(function (obj, idx) {
            obj.toDelete = false;
            appendEventHookRow(obj);
        });
    }
    refreshPopoverDocumentation("eventHookTable");
}


function appendSchedulerRow(scheduler) {

    var activebool = true;
    if (scheduler.isActive === "Y") {
        activebool = true;
    } else {
        activebool = false;
    }

    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-s").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var cronInput = $("<input name=\"cronDefinition\" maxlength=\"200\">").addClass("form-control").val(scheduler.cronDefinition);
    loadCronList(cronInput);
    var descInput = $("<input name=\"cronDescription\" maxlength=\"200\">").addClass("form-control").val(scheduler.description);
    var activeInput = $("<input name=\"cronActive\" type='checkbox'>").addClass("form-control").prop("checked", activebool);
    var lastExecInput = $("<input name=\"cronLastExecInput\" readonly>").addClass("form-control input-sm").val(getDate(scheduler.lastExecution));
    var userCreateInput = $("<input name=\"cronUserCreateInput\" readonly>").addClass("form-control input-sm").val(scheduler.usrCreated);
    var userModifInput = $("<input name=\"cronUserModifInput\" readonly>").addClass("form-control input-sm").val(scheduler.usrModif);

    var cron = $("<div class='form-group col-sm-10'></div>").append("<label for='cronDefinition'>" + doc.getDocOnline("scheduleentry", "cronDefinition") + "</label>").append(cronInput);
    var active = $("<div class='form-group col-sm-2'></div>").append("<label for='cronActive'>" + doc.getDocOnline("scheduleentry", "active") + "</label>").append(activeInput);
    var lastExec = $("<div class='form-group col-sm-4'></div>").append("<label for='cronLastExecInput'>" + doc.getDocOnline("scheduleentry", "lastexecution") + "</label>").append(lastExecInput);
    var description = $("<div class='form-group col-sm-12'></div>").append("<label for='cronDescription'>" + doc.getDocOnline("scheduleentry", "description") + "</label>").append(descInput);
    var userCreate = $("<div class='form-group col-sm-4'></div>").append("<label for='cronUserCreateInput'>" + doc.getDocOnline("transversal", "UsrCreated") + "</label>").append(userCreateInput);
    var userModif = $("<div class='form-group col-sm-4'></div>").append("<label for='cronUserModifInput'>" + doc.getDocOnline("transversal", "UsrModif") + "</label>").append(userModifInput);


    var table = $('#schedulerTableBody');
    var row = $("<tr class='dataField'></tr>");

    var drow1 = $("<div class='row'></div>").append(cron).append(active).append(description).append(lastExec).append(userCreate).append(userModif);

    var td1 = $("<td class='row form-group'></td>").append(deleteBtn);
    var td2 = $("<td class='row form-group'></td>").append(drow1);

    deleteBtn.click(function () {
        scheduler.toDelete = (scheduler.toDelete) ? false : true;
        if (scheduler.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });

    cronInput.change(function () {
        scheduler.cronDefinition = $(this).val();
    });

    descInput.change(function () {
        scheduler.description = $(this).val();
    });

    activeInput.change(function () {
        if ($(this).prop("checked")) {
            scheduler.isActive = "Y";
        } else {
            scheduler.isActive = "N";
        }
    });

    row.append(td1);
    row.append(td2);
    row.data("scheduler", scheduler);
    table.append(row);
}

function appendEventHookRow(eventHook) {

    var doc = new Doc();

    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs col-sm-12 marginBottom20").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var eventRefInput = $("<select></select>").addClass("form-control");
    eventRefInput.append($("<option></option>").text("CAMPAIGN_START").val("CAMPAIGN_START"));
    eventRefInput.append($("<option></option>").text("CAMPAIGN_END").val("CAMPAIGN_END"));
    eventRefInput.append($("<option></option>").text("CAMPAIGN_END_CIKO").val("CAMPAIGN_END_CIKO"));
    eventRefInput.val(eventHook.eventReference);
    var activeInput = $("<input name=\"eventActive\" type='checkbox'>").addClass("form-control").prop("checked", eventHook.isActive);
    var descInput = $("<input name=\"eventDescription\" maxlength=\"200\">").addClass("form-control").val(eventHook.description);
    var hookConnectorInput = getSelectInvariant("EVENTCONNECTOR", false);
    hookConnectorInput.val(eventHook.hookConnector);
    var hookRecipientInput = $("<input name=\"hookRecipient\">").addClass("form-control").val(eventHook.hookRecipient);
    var hookChannelInput = $("<input name=\"hookChannel\">").addClass("form-control").val(eventHook.hookChannel);
    var userCreateInput = $("<input name=\"eventUserCreateInput\" readonly>").addClass("form-control input-sm").val(eventHook.usrCreated);
    var userModifInput = $("<input name=\"eventUserModifInput\" readonly>").addClass("form-control input-sm").val(eventHook.usrModif);

    var eventRef = $("<div class='form-group col-sm-4'></div>").append("<label for='eventReference'>" + doc.getDocOnline("eventhook", "EventReference") + "</label>").append(eventRefInput);
    var description = $("<div class='form-group col-sm-6'></div>").append("<label for='eventDescription'>" + doc.getDocOnline("eventhook", "Description") + "</label>").append(descInput);
    var active = $("<div class='form-group col-sm-2'></div>").append("<label for='eventActive'>" + doc.getDocOnline("eventhook", "IsActive") + "</label>").append(activeInput);
    var hookConnector = $("<div class='form-group col-sm-3'></div>").append("<label for='hookConnector'>" + doc.getDocOnline("eventhook", "HookConnector") + "</label>").append(hookConnectorInput);
    var hookRecipient = $("<div class='form-group col-sm-5'></div>").append("<label for='hookRecipient'>" + doc.getDocOnline("eventhook", "HookRecipient") + "</label>").append(hookRecipientInput);
    var hookChannel = $("<div class='form-group col-sm-4'></div>").append("<label for='hookChannel'>" + doc.getDocOnline("eventhook", "HookChannel") + "</label>").append(hookChannelInput);
    var userCreate = $("<div class='form-group col-sm-4'></div>").append("<label for='cronUserCreateInput'>" + doc.getDocOnline("eventhook", "UsrCreated") + "</label>").append(userCreateInput);
    var userModif = $("<div class='form-group col-sm-4'></div>").append("<label for='cronUserModifInput'>" + doc.getDocOnline("eventhook", "UsrModif") + "</label>").append(userModifInput);
    var logo = $("<span class='fa fa-fw col-sm-12' style='font-size:50px;'></span>");
    logo.addClass(getClass(eventHook.hookConnector));

    if (eventHook.hookConnector === "SLACK") {
        hookChannel.show();
        hookRecipient.removeClass("col-sm-5 col-sm-9");
        hookRecipient.addClass("col-sm-5");
    } else {
        hookChannel.hide();
        hookRecipient.removeClass("col-sm-5 col-sm-9");
        hookRecipient.addClass("col-sm-9");
    }

//
    var table = $('#eventHookTableBody');
    var row = $("<tr></tr>");

    var drow01 = $("<div class='row'></div>").append(deleteBtn);
    var drow02 = $("<div class='row'></div>").append(logo);

    var drow1 = $("<div class='row'></div>").append(eventRef).append(description).append(active);
    var drow2 = $("<div class='row'></div>").append(hookConnector).append(hookRecipient).append(hookChannel);

    var td1 = $("<td></td>").append(drow01).append(drow02);
    var td2 = $("<td></td>").append(drow1).append(drow2);

    deleteBtn.click(function () {
        eventHook.toDelete = (eventHook.toDelete) ? false : true;
        if (eventHook.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });

    hookConnectorInput.change(function () {
        eventHook.hookConnector = $(this).val();

        logo.removeClass("fa-slack fa-windows fa-at fa-external-link fa-google");
        logo.addClass(getClass($(this).val()));
        if ($(this).val() === "SLACK") {
            hookChannel.show();
            hookRecipient.removeClass("col-sm-5 col-sm-9");
            hookRecipient.addClass("col-sm-5");
        } else {
            hookChannel.hide();
            hookRecipient.removeClass("col-sm-5 col-sm-9");
            hookRecipient.addClass("col-sm-9");
        }

    });

    eventRefInput.change(function () {
        eventHook.eventReference = $(this).val();
    });

    descInput.change(function () {
        eventHook.description = $(this).val();
    });

    hookRecipientInput.change(function () {
        eventHook.hookRecipient = $(this).val();
    });

    hookChannelInput.change(function () {
        eventHook.hookChannel = $(this).val();
    });

    activeInput.change(function () {
        console.info($(this).prop("checked"));
        console.info($(this).val());
        eventHook.isActive = $(this).prop("checked");
    });

    row.append(td1);
    row.append(td2);
    row.data("eventHook", eventHook);
    table.append(row);
}

function getClass(connector) {
    if (connector === "TEAMS") {
        return "fa-windows";
    } else if (connector === "SLACK") {
        return "fa-slack";
    } else if (connector === "EMAIL") {
        return "fa-at";
    } else if (connector === "GOOGLE-CHAT") {
        return "fa-google";
    } else if (connector === "GENERIC") {
        return "fa-external-link";
    }
    return "fa-external-link";
}

function addNewSchedulerRow() {
    var newScheduler = {
        cronDefinition: "",
        isActive: "Y",
        lastExecution: "",
        description: "",
        id: "0",
        toDelete: false
    };
    appendSchedulerRow(newScheduler);
    refreshPopoverDocumentation("schedulerTable");

}

function addNewEventHookRow() {
    var newEventHook = {
        eventReference: "CAMPAIGN_START",
        isActive: true,
        hookConnector: "EMAIL",
        hookRecipient: "",
        hookChannel: "",
        objectKey1: "",
        objectKey2: "",
        description: "",
        id: 0,
        toDelete: false
    };
    appendEventHookRow(newEventHook);
    refreshPopoverDocumentation("eventHookTable");

}
