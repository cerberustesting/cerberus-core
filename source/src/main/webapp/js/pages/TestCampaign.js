/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    displayPageLabel();

    // handle the click for specific action buttons
    $("#editTestcampaignButton").click(editEntryModalSaveHandler);
    $("#addTestcampaignButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editTestcampaignModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addTestcampaignModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#viewTestcampaignModal').on('hidden.bs.modal', viewEntryModalCloseHandler);

    $('#editTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if (target == "#tabsCreate-1") {

        } else if (target == "#tabsCreate-2") {
            $("#batteryTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-3") {
            $("#parameterTestcampaignsTable").DataTable().draw();
        }
    });

    $('#addTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if (target == "#tabsCreate-11") {

        } else if (target == "#tabsCreate-12") {
            $("#addModalBatteryTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-13") {
            $("#addModalParameterTestcampaignsTable").DataTable().draw();
        }
    });

    $("#viewTestcampaignModal").on('shown.bs.modal', function (e) {
        $("#viewTestcampaignsTable").DataTable().columns.adjust();
    })

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testcampaignsTable", "ReadCampaign", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForCampaign, "#testcampaignList");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcampaign", "allTestcampaigns"));
    $("[name='editTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "edittestcampaign_field"));
    $("[name='addTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "addtestcampaign_field"));
    $("[name='campaignField']").html(doc.getDocLabel("page_testcampaign", "campaign_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_testcampaign", "description_field"));
    $("[name='tabDescription']").html(doc.getDocLabel("page_testcampaign", "description_tab"));
    $("[name='tabBatteries']").html(doc.getDocLabel("page_testcampaign", "battery_tab"));
    $("[name='tabParameters']").html(doc.getDocLabel("page_testcampaign", "parameter_tab"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_testcampaign", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_testcampaign", "save_btn"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForCampaign(data) {
    var doc = new Doc();
    if ($("#createTestcampaignButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createTestcampaignButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testcampaign", "button_create") + "</button></div>";
        $("#testcampaignsTable_wrapper div#testcampaignsTable_length").before(contentToAdd);
        $('#testcampaignList #createTestcampaignButton').click(addEntryClick);
    }
}

function renderOptionsForCampaign2(id) {
    var doc = new Doc();
    var data = getSelectTestBattery(false, true);
    $("#" + id + "_wrapper #addBatteryTestcampaign").remove();
    var contentToAdd =
        "<div class='marginBottom10 form-inline' id='addBatteryTestcampaign'>" +
        "<div class='form-group marginRight10'>" +
        "<select id='batteryTestSelect' class='form-control' style='width:200px;'>";
    for (var i = 0; i < data.find("option").length; i++) {
        if (!findValueTableDataByCol(id, 0, data.find("option")[i].value)) {
            contentToAdd +=
                "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].text + "</option>";
        }
    }
    contentToAdd +=
        "</select>" +
        "</div>" +
        "<div class='form-group'>" +
        "<button type='button' id='addBatteryTestcampaignButton' class='btn btn-primary' name='ButtonEdit' onclick='addBatteryEntryClick(\"" + id + "\")'>" + doc.getDocLabel("page_testcampaign", "add_btn") + "</button>" +
        "</div>" +
        "</div>";
    $("#" + id + "_wrapper div#" + id + "_length").before(contentToAdd);
    $("#" + id + "_wrapper #batteryTestSelect").select2();
    if ($("#" + id + '_wrapper #batteryTestSelect option').size() <= 0) {
        $("#" + id + '_wrapper #batteryTestSelect').parent().hide();
        $("#" + id + '_wrapper #addBatteryTestcampaignButton').off().prop("disabled", true);
    }

}

function renderOptionsForCampaign3(id) {
    var doc = new Doc();
    var data = getSelectInvariant("CAMPAIGN_PARAMETER", false, true);
    $("#" + id + "_wrapper #addParameterTestcampaign").remove();
    var contentToAdd =
        "<div class='marginBottom10 form-inline' id='addParameterTestcampaign'>" +
        "<div class='form-group marginRight10'>" +
        "<select id='parameterTestSelect' class='form-control' style='width:200px;' onchange='updateSelectParameter(\"" + id + "\")'>";
    for (var i = 0; i < data.find("option").length; i++) {
        contentToAdd +=
            "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
    }
    contentToAdd +=
        "</select>" +
        "</div>" +
        "<div class='form-group marginRight10'>" +
        "<select id='parameterTestSelect2' class='form-control' style='width:200px;'>" +
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

function renderOptionsForCampaign4(data) {
    if ($("#blankSpaceBattery").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpaceBattery'></div>";
        $("#viewTestcampaignsTable_wrapper div#viewTestcampaignsTable_length").before(contentToAdd);
    }
}

function viewEntryClick(param) {
    clearResponseMessageMainPage();

    var doc = new Doc();

    $("[name='viewTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "viewtestcampaign_field") + " " + param);

    //Store the campaign name, we need it if we want to add him a battery test
    $("#campaignKey").val(param);

    if ($("#viewTestcampaignModal #viewTestcampaignsTable_wrapper").length > 0) {
        $("#viewTestcampaignModal #viewTestcampaignsTable").DataTable().destroy();
        $("#viewTestcampaignModal #viewTestcampaignsTable").empty();
    }
    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("viewTestcampaignsTable", "ReadTestBatteryContent?campaign=" + param, "contentTable", aoColumnsFunc4(), [0, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForCampaign4, "#viewTestcampaignList");

    var formEdit = $('#viewTestcampaignModal');

    formEdit.modal('show');
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

    //Store the campaign name, we need it if we want to add him a battery test
    $("#campaignKey").val(param);

    var formEdit = $('#editTestcampaignModal');

    var jqxhr = $.getJSON("ReadCampaign?battery=true&parameter=true", "param=" + param);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];


        formEdit.find("#campaign").prop("value", obj["campaign"]);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#id").prop("value", obj["campaignID"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#campaign").prop("readonly", "readonly");
            formEdit.find("#description").prop("readonly", "readonly");
            formEdit.find("#id").prop("readonly", "readonly");

            $('#editTestcampaignButton').attr('class', '');
            $('#editTestcampaignButton').attr('hidden', 'hidden');
        }

        /* BATTERIES */

        var array = [];

        $.each(obj.battery, function (e) {
            array.push(
                [obj.battery[e].testbattery,obj.battery[e].campaign,obj.battery[e].campaigncontentID]
            );
        });

        if ($("#editTestcampaignModal #batteryTestcampaignsTable_wrapper").length > 0) {
            $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().clear();
            $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().rows.add(array).draw();
        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("batteryTestcampaignsTable", array, aoColumnsFunc2("batteryTestcampaignsTable"), true);
            createDataTableWithPermissions(configurations, null, "#batteryTestcampaignList");
        }
        renderOptionsForCampaign2("batteryTestcampaignsTable");

        /* PARAMETERS */

        var array = [];

        $.each(obj.parameter, function (e) {
            array.push([obj.parameter[e].campaignparameterID,obj.parameter[e].parameter,obj.parameter[e].campaign,obj.parameter[e].value])
        });

        if ($("#editTestcampaignModal #parameterTestcampaignsTable_wrapper").length > 0) {
            $("#editTestcampaignModal #parameterTestcampaignsTable").DataTable().clear();
            $("#editTestcampaignModal #parameterTestcampaignsTable").DataTable().rows.add(array).draw();
        } else {
            //configure and create the dataTable
            var configurations = new TableConfigurationsClientSide("parameterTestcampaignsTable", array, aoColumnsFunc3("parameterTestcampaignsTable"), true);
            createDataTableWithPermissions(configurations, null, "#parameterTestcampaignList");
        }
        renderOptionsForCampaign3("parameterTestcampaignsTable");

    });

    $('#editTestcampaignModal .nav-tabs a[href="#tabsCreate-1"]').tab('show');

    formEdit.modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editTestcampaignModal'));
    var formEdit = $('#editTestcampaignModal #editTestcampaignModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var batteries = null;
    if ($("#batteryTestcampaignsTable_wrapper").length > 0) {
        batteries = $("#batteryTestcampaignsTable").DataTable().data().toArray();
    }

    var parameters = null;
    if ($("#parameterTestcampaignsTable_wrapper").length > 0) {
        parameters = $("#parameterTestcampaignsTable").DataTable().data().toArray();
    }

    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestcampaignModal');
    $.ajax({
        url: "UpdateCampaign2",
        async: true,
        method: "POST",
        data: {
            Campaign: data.campaign,
            CampaignID: data.id,
            Description: data.description,
            Batteries: JSON.stringify(batteries),
            Parameters: JSON.stringify(parameters)
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#editTestcampaignModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(true);
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
    $("#addTestcampaignModal #campaign").empty();

    if ($("#addModalBatteryTestcampaignsTable_wrapper").length > 0) {
        $("#addModalBatteryTestcampaignsTable").DataTable().clear().draw();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("addModalBatteryTestcampaignsTable", null, aoColumnsFunc2("addModalBatteryTestcampaignsTable"), true);
        createDataTableWithPermissions(configurations, null, "#addModalBatteryTestcampaignList");
    }
    renderOptionsForCampaign2("addModalBatteryTestcampaignsTable");

    if ($("#addModalParameterTestcampaignsTable_wrapper").length > 0) {
        $("#addModalParameterTestcampaignsTable").DataTable().clear().draw();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("addModalParameterTestcampaignsTable", null, aoColumnsFunc3("addModalParameterTestcampaignsTable"), true);
        createDataTableWithPermissions(configurations, null, "#addModalParameterTestcampaignList");
    }
    renderOptionsForCampaign3("addModalParameterTestcampaignsTable");

    $('#addTestcampaignModal .nav-tabs a[href="#tabsCreate-11"]').tab('show');

    $('#addTestcampaignModal').modal('show');
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addTestcampaignModal'));
    var formEdit = $('#addTestcampaignModal #addTestcampaignModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var batteries = null;
    if ($("#addModalBatteryTestcampaignsTable_wrapper").length > 0) {
        batteries = $("#addModalBatteryTestcampaignsTable").DataTable().data().toArray();
    }
    for (var i = 0; i < batteries.length; i++) {
        batteries[i][1] = data.campaign;
    }

    var parameters = null;
    if ($("#addModalParameterTestcampaignsTable_wrapper").length > 0) {
        parameters = $("#addModalParameterTestcampaignsTable").DataTable().data().toArray();
    }
    for (var i = 0; i < parameters.length; i++) {
        parameters[i][2] = data.campaign;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#addTestcampaignModal');
    $.ajax({
        url: "CreateCampaign",
        async: true,
        method: "POST",
        data: {
            Campaign: data.campaign,
            Description: data.description,
            Batteries: JSON.stringify(batteries),
            Parameters: JSON.stringify(parameters)
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#addTestcampaignModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(true);
                $('#addTestcampaignModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#addTestcampaignModal'));
            }
        },
        error: showUnexpectedError
    });

}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addTestcampaignModal #addTestcampaignModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addTestcampaignModal'));
}

function removeEntryClick(key) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var id = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteCampaign2?key=" + key,
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#removeTestampaignModal');
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(true);
                $('#removeTestcampaignModal').modal('hide');
                showMessage(data);
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_testcampaign", "title_remove"), doc.getDocLabel("page_testcampaign", "message_remove"), id, undefined, undefined, undefined);
}

function addBatteryEntryClick(tableId) {
    $("#" + tableId + '_wrapper #addBatteryTestcampaignButton').off().prop("disabled", true);
    $("#" + tableId).DataTable().row.add([$("#" + tableId + '_wrapper #batteryTestSelect').find(":selected").val(), $("#campaignKey").val()]).draw();
    renderOptionsForCampaign2(tableId);
}

function removeBatteryEntryClick(tableId, key) {
    $('#' + tableId + '_wrapper #removeTestbattery').filter(function (i, e) {
        return $(e).attr("key") == key;
    }).off().prop("disabled", true);
    $("#" + tableId).DataTable().rows(function (i, d, n) {
        return d[0] == key;
    }).remove().draw();
    renderOptionsForCampaign2(tableId);
}

function addParameterEntryClick(tableId) {
    $("#" + tableId + '_wrapper #addParameterTestcampaignButton').off().prop("disabled", true);
    $("#" + tableId).DataTable().row.add([0, $("#" + tableId + '_wrapper #parameterTestSelect').find(":selected").val(), $("#campaignKey").val(), $("#" + tableId + '_wrapper #parameterTestSelect2').find(":selected").val()]).draw();
    updateSelectParameter(tableId);
}

function removeParameterEntryClick(tableId, key, key1) {
    $('#' + tableId + '_wrapper #removeTestbattery').filter(function (i, e) {
        return $(e).attr("key") == key && $(e).attr("key1") == key1;
    }).off().prop("disabled", true);
    $("#" + tableId).DataTable().rows(function (i, d, n) {
        return d[1] == key && d[3] == key1;
    }).remove().draw()
    updateSelectParameter(tableId);
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function updateSelectParameter(id) {
    var val = $("#" + id + '_wrapper #parameterTestSelect').find(":selected").val();
    var data = getSelectInvariant(val, false, true);
    $("#" + id + "_wrapper #parameterTestSelect2").empty();
    var optionList = "";
    for (var i = 0; i < data.find("option").length; i++) {
        if (!(findValueTableDataByCol(id, 1, val) && findValueTableDataByCol(id, 3, data.find("option")[i].value)))
            optionList +=
                "<option value='" + data.find("option")[i].value + "'>" + data.find("option")[i].value + "</option>";
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

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editTestcampaign = '<button id="editTestcampaign" onclick="editEntryClick(\'' + obj["campaign"] + '\');"\n\
                                        class="editCampaign btn btn-default btn-xs margin-right5" \n\
                                        name="editTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var removeTestcampaign = '<button id="removeTestcampaign" onclick="removeEntryClick(\'' + obj["campaign"] + '\');"\n\
                                        class="removeTestcampaign btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';
                var viewTestcampaign = '<button id="viewTestcampaign" onclick="viewEntryClick(\'' + obj["campaign"] + '\');"\n\
                                        class="viewTestcampaign btn btn-default btn-xs margin-right5" \n\
                                        name="viewTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_view") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-eye-open"></span></button>';

                return '<div class="center btn-group width150">' + editTestcampaign + removeTestcampaign + viewTestcampaign + '</div>';

            },
            "width": "100px"
        },
        {"data": "campaign", "sName": "campaign", "title": doc.getDocLabel("page_testcampaign", "testcampaign_col")},
        {
            "data": "description",
            "sName": "description",
            "title": doc.getDocLabel("page_testcampaign", "description_col")
        }
    ];
    return aoColumns;
}

function aoColumnsFunc2(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var removeTestcampaign = '<button id="removeTestbattery" key="' + obj[0] + '" onclick="removeBatteryEntryClick(\'' + tableId + '\',\'' + obj[0] + '\');"\n\
                                        class="removeTestbattery btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestbattery" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + removeTestcampaign + '</div>';

            },
            "width": "100px"
        },
        {"data": "0", "sName": "testbattery", "title": doc.getDocLabel("page_testcampaign", "testbattery_col")}
    ];
    return aoColumns;
}

function aoColumnsFunc3(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var removeTestcampaign = '<button id="removeTestparameter" key="' + obj[1] + '" key1="' + obj[3] + '" onclick="removeParameterEntryClick(\'' + tableId + '\',\'' + obj[1] + '\',\'' + obj[3] + '\');"\n\
                                        class="removeTestparameter btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestparameter" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + removeTestcampaign + '</div>';

            },
            "width": "100px"
        },
        {"data": "1", "sName": "parameter", "title": doc.getDocLabel("page_testcampaign", "parameter_col")},
        {"data": "3", "sName": "value", "title": doc.getDocLabel("page_testcampaign", "value_col")}
    ];
    return aoColumns;
}

function aoColumnsFunc4() {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": "testbattery",
            "sName": "tbc.testbattery",
            "title": doc.getDocLabel("page_testcampaign", "testbattery_col")
        },
        {"data": "test", "sName": "tbc.Test", "title": doc.getDocLabel("page_testcampaign", "test_col")},
        {
            "data": "testCase", "sName": "tbc.Testcase", "title": doc.getDocLabel("page_testcampaign", "testcase_col"),
            "mRender": function (data, type, obj) {
                return "<a target=\"_blank\" href='TestCaseScript.jsp?test=" + obj["test"] + "&testcase=" + obj["testCase"] + "'>" + obj["testCase"] + "</a>";
            }
        }
    ];
    return aoColumns;
}
