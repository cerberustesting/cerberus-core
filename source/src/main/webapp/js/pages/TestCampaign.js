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

    $('#editTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if(target == "#tabsCreate-1"){

        }else if(target == "#tabsCreate-2"){
            $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().draw();
        }else if(target == "#tabsCreate-3"){

        }
    });

    $('#addTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if(target == "#tabsCreate-11"){

        }else if(target == "#tabsCreate-12"){
            $("#addTestcampaignModal #batteryTestcampaignsTable").DataTable().draw();
        }else if(target == "#tabsCreate-13"){

        }
    });

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testcampaignsTable", "ReadCampaign", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#testcampaignList");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcampaign", "allTestcampaigns"));
    $("[name='editTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "edittestcampaign_field"));
    $("[name='testcampaignField']").html(doc.getDocLabel("page_testcampaign", "testcampaign_field"));
    $("[name='cerberusField']").html(doc.getDocLabel("page_testcampaign", "cerberus_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_testcampaign", "description_field"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_testcampaign", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_testcampaign", "save_btn"));

    displayHeaderLabel(doc);

    $("[name='systemField']").html(doc.getDocLabel("page_testcampaign", "system_field") + " (" + getSys() + ")");

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();
    if ($("#createTestcampaignButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createTestcampaignButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testcampaign", "button_create") + "</button></div>";
        $("#testcampaignsTable_wrapper div#testcampaignsTable_length").before(contentToAdd);
        $('#testcampaignList #createTestcampaignButton').click(addEntryClick);
    }
}

function findValueTableDataByCol(tableId, colIndex, value){
    var result = false;
    //Iterate all td's in second column
    $.each($("#"+tableId).DataTable().rows().data(), function(i, v){
        if(v[colIndex] == value){
            result = true;
        }
    });
    return result;
}

function renderOptionsForApplication2(id) {

    $.ajax({
        url: "ReadTestBattery",
        async: true,
        method: "GET",
        success: function (data) {

            if(data.messageType == "OK"){
                $("#"+id+" #addBatteryTestcampaign").remove();
                var contentToAdd =
                    "<div class='marginBottom10 form-inline' id='addBatteryTestcampaign'>" +
                        "<div class='form-group marginRight10'>" +
                            "<select id='batteryTestSelect' class='form-control' style='width:200px;'>";
                for(var i = 0; i<data.contentTable.length; i++){
                    if(!findValueTableDataByCol(id+" #batteryTestcampaignsTable", 0, data.contentTable[i].testbattery)) {
                        contentToAdd +=
                            "<option value='" + data.contentTable[i].testbattery + "'>" + data.contentTable[i].testbattery + " - " + data.contentTable[i].description + "</option>";
                    }
                }
                contentToAdd +=
                            "</select>" +
                        "</div>" +
                        "<div class='form-group'>" +
                            "<button type='button' id='addBatteryTestcampaignButton' class='btn btn-primary' name='ButtonEdit' onclick='addBatteryEntryClick(\"" + id + "\")'>Add</button>" +
                        "</div>" +
                    "</div>";
                $("#"+id+" #batteryTestcampaignsTable_wrapper div#batteryTestcampaignsTable_length").before(contentToAdd);
                $("#"+id+" #batteryTestSelect").select2();
                if($("#"+id+' #batteryTestSelect option').size() <= 0){
                    $("#"+id+' #batteryTestSelect').parent().hide();
                    $("#"+id+' #addBatteryTestcampaignButton').off().prop("disabled",true);
                }
            }
        },
        error: showUnexpectedError
    });

}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addTestcampaignModal #campaign").empty();

    if($("#addTestcampaignModal #batteryTestcampaignsTable_wrapper").length > 0) {
        $("#addTestcampaignModal #batteryTestcampaignsTable").DataTable().clear().draw();
    }else{
        //configure and create the dataTable
        var configurations = new TableConfigurationsClientSide("addTestcampaignModal #batteryTestcampaignsTable", null, aoColumnsFunc2("addTestcampaignModal"), true);
        createDataTableWithPermissions(configurations, null, "#addTestcampaignModal #batteryTestcampaignList");
    }
    renderOptionsForApplication2("addTestcampaignModal");
    $('#addTestcampaignModal .nav-tabs a[href="#tabsCreate-11"]').tab('show');

    $('#addTestcampaignModal').modal('show');
}

function addBatteryEntryClick(tableId){
    $("#"+ tableId +" #batteryTestcampaignsTable").DataTable().row.add([$("#"+ tableId +' #batteryTestSelect').find(":selected").val(),$("#"+ tableId +" #campaignKey").val()]).draw();
    renderOptionsForApplication2(tableId);
}

function removeBatteryEntryClick(tableId, key){
    $("#"+ tableId +" #batteryTestcampaignsTable").DataTable().rows(function(i,d,n){return d[0] == key}).remove().draw()
    renderOptionsForApplication2(tableId);
}

function editEntryClick(param) {
    clearResponseMessageMainPage();

    //Store the campaign name, we need it if we want to add him a battery test
    $("#campaignKey").val(param);

    var formEdit = $('#editTestcampaignModal');

    var jqxhr = $.getJSON("ReadCampaign", "param=" + param);
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

    });

    $.ajax({
        url: "ReadCampaignContent?key=" + param,
        async: true,
        method: "GET",
        success: function (data) {
            var array = [];

            $.each(data.contentTable, function(e){
                array.push(
                    $.map(data.contentTable[e], function(value, index) {
                        return [value];
                    })
                );
            });

            if($("#editTestcampaignModal #batteryTestcampaignsTable_wrapper").length > 0) {
                $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().clear();
                $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().rows.add(array).draw();
            }else{
                //configure and create the dataTable
                var configurations = new TableConfigurationsClientSide("editTestcampaignModal #batteryTestcampaignsTable", array, aoColumnsFunc2("editTestcampaignModal"), true);
                createDataTableWithPermissions(configurations, null, "#editTestcampaignModal #batteryTestcampaignList");
            }
            renderOptionsForApplication2("editTestcampaignModal");

        },
        error: showUnexpectedError
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
    if($("#editTestcampaignModal #batteryTestcampaignsTable_wrapper").length > 0) {
        batteries = $("#editTestcampaignModal #batteryTestcampaignsTable").DataTable().data().toArray();
    }

    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestcampaignModal');
    $.ajax({
        url: "UpdateCampaign2",
        async: true,
        method: "POST",
        data: {Campaign: data.campaign,
            CampaignID: data.id,
            Description: data.description,
            Batteries: JSON.stringify(batteries)},
        success: function (data) {
            hideLoaderInModal('#editTestcampaignModal');
            var oTable = $("#testcampaignsTable").dataTable();
            oTable.fnDraw(true);
            $('#editTestcampaignModal').modal('hide');
            showMessage(data);
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

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addTestcampaignModal'));
    var formEdit = $('#addTestcampaignModal #addTestcampaignModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#addTestcampaignModal');
    $.ajax({
        url: "CreateCampaign",
        async: true,
        method: "POST",
        data: {Campaign: data.campaign,
            Description: data.description},
        success: function (data) {
            hideLoaderInModal('#addTestcampaignModal');
            var oTable = $("#testcampaignsTable").dataTable();
            oTable.fnDraw(true);
            $('#addTestcampaignModal').modal('hide');
            showMessage(data);
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

function removeEntryClick(id) {
    var doc = new Doc();
    showModalConfirmation(function(ev){
        var id = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteCampaign2?key="+id,
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
    }, doc.getDocLabel("page_testcampaign", "title_remove") , doc.getDocLabel("page_testcampaign", "message_remove"), id, undefined, undefined, undefined);
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editTestcampaign = '<button id="editTestcampaign" onclick="editEntryClick(\'' + obj["campaign"] + '\');"\n\
                                        class="editApplication btn btn-default btn-xs margin-right5" \n\
                                        name="editTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var removeTestcampaign = '<button id="removeTestcampaign" onclick="removeEntryClick(\'' + obj["campaignID"] + '\');"\n\
                                        class="removeTestcampaign btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editTestcampaign + removeTestcampaign + '</div>';

            },
            "width": "100px"
        },
        {"data": "campaign", "sName": "campaign", "title": doc.getDocLabel("page_testcampaign", "testcampaign_col")},
        {"data": "description", "sName": "description", "title": doc.getDocLabel("page_testcampaign", "description_col")}
    ];
    return aoColumns;
}

function aoColumnsFunc2(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var removeTestcampaign = '<button id="removeTestbattery" onclick="removeBatteryEntryClick(\'' + tableId + '\',\'' + obj[0] + '\');"\n\
                                        class="removeTestbattery btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestbattery" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + removeTestcampaign + '</div>';

            },
            "width": "100px"
        },
        {"data": "0", "sName": "testbattery", "title": doc.getDocLabel("page_testcampaign", "testcampaign_col")}
    ];
    return aoColumns;
}
