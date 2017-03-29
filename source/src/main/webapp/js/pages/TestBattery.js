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
$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    displayPageLabel();

    var doc = new Doc();

    // handle the click for specific action buttons
    $("#editTestbatteryButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editTestbatteryModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    $('#editTestbatteryModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if (target == "#tabsCreate-1" || target == "#tabsCreate-2") {
            // Tabs looks like we can click on it
            $("#createTab1Text").parent().removeClass("disabled");
            $("#createTab2Text").parent().removeClass("disabled");
            // Remove the click function that prevent us to click on it if it exist. Now we can click on tabs.
            $("#createTab1Text").unbind("click");
            $("#createTab2Text").unbind("click");
            // Hide Add Test Case Tab
            $("#createTab3Text").parent().hide();
            // Set the click event of save button to Save the Battery
            $("#editTestbatteryButton").unbind("click");
            $("#editTestbatteryButton").text(doc.getDocLabel("page_testbattery", "save_btn")).click(editEntryModalSaveHandler);
            // Set the click event of close button to Close the modal
            $("button[name='buttonClose']").attr("data-dismiss", "modal");
            $("button[name='buttonClose']").text(doc.getDocLabel("page_testbattery", "close_btn")).unbind("click");
            if (target == "#tabsCreate-2") {
                // Reload Battery's Case's table
                $("#batteryTestcasesTable").DataTable().draw();
            }
        } else if (target == "#tabsCreate-3") {
            // Tabs look like we can't click on it
            $("#createTab1Text").parent().addClass("disabled");
            $("#createTab2Text").parent().addClass("disabled");
            // Disable all click event of Tabs so we can't click on it
            $("#createTab1Text").click(function (e) {
                return false;
            });
            $("#createTab2Text").click(function (e) {
                return false;
            });
            // Change Save button purpose to Add Case to the battery
            $("#editTestbatteryButton").unbind("click");
            $("#editTestbatteryButton").text(doc.getDocLabel("page_testbattery", "add_btn")).click(function () {
                addTestCaseEntryClick();
                $('#editTestbatteryModal .nav-tabs a[href="#tabsCreate-2"]').tab('show');
            });
            // Change Close button purpose to Cancel the add and return to the cases tab
            $("button[name='buttonClose']").removeAttr("data-dismiss");
            $("button[name='buttonClose']").text(doc.getDocLabel("page_testbattery", "back_btn")).click(function () {
                $('#editTestbatteryModal .nav-tabs a[href="#tabsCreate-2"]').tab('show');
            });
            // Show Add Test Case Tab
            $("#createTab3Text").parent().show();
            // Reload Case's table
            $("#batteryTestcases2Table").DataTable().draw();
        }
    });

    $("#createTab3Text").parent().hide();

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testbatterysTable", "ReadTestBattery", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForBattery, "#testbatteryList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testbattery", "allTestbatterys"));
    $("[name='batteryField']").html(doc.getDocLabel("page_testbattery", "battery_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_testbattery", "description_field"));
    $("[name='tabDescription']").html(doc.getDocLabel("page_testbattery", "description_tab"));
    $("[name='tabTestCase']").html(doc.getDocLabel("page_testbattery", "testcase_tab"));
    $("[name='tabAddTestCase']").html(doc.getDocLabel("page_testbattery", "addtestcase_tab"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_testbattery", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_testbattery", "save_btn"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForBattery(data) {
    var doc = new Doc();
    if ($("#createTestbatteryButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createTestbatteryButton' type='button' class='btn btn-default' onclick='editEntryClick()'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testbattery", "button_create") + "</button></div>";
        $("#testbatterysTable_wrapper div#testbatterysTable_length").before(contentToAdd);
    }
}

function renderOptionsForBattery2(id) {
    var doc = new Doc();
    if ($("#createTestcaseButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createTestcaseButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testbattery", "button_add") + "</button></div>";
        $("div#batteryTestcasesTable_length").before(contentToAdd);
        $('#createTestcaseButton').click(function () {
            $('#editTestbatteryModal .nav-tabs a[href="#tabsCreate-3"]').tab('show');
        });
    }
}

function renderOptionsForBattery3(id) {
    var doc = new Doc();
    if ($("#addTestCase3").length <= 0) {
        var contentToAdd = "<div class='marginBottom10' style='float:left;' id='addTestCase3'>" +
            "<button id='checkall' type='button' class='btn btn-default margin-right5'><span class='glyphicon glyphicon-check' aria-hidden='true'></spanclass></button>" +
            "<button id='uncheckall' type='button' class='btn btn-default margin-right5'><span class='glyphicon glyphicon-unchecked' aria-hidden='true'></spanclass></button>" +
            "</div>";
        $("#batteryTestcases2Table_paginate").parent().css("clear", "both");
        $("#batteryTestcases2Table_wrapper div#batteryTestcases2Table_paginate").parent().after(contentToAdd);
        $("#uncheckall").click(function () {
            $("#editTestbatteryModal input[type='checkbox']").prop("checked", false).trigger("change");
        });
        $("#checkall").click(function () {
            $("#editTestbatteryModal input[type='checkbox']").prop("checked", true).trigger("change");
        });
        $("#editTestbatteryModal #addBatteryContentButton").click(addTestCaseEntryClick);
    }
}

function editEntryClick(param) {
    clearResponseMessageMainPage();

    var doc = new Doc();

    //Store the battery name, we need it if we want to add him a battery test
    $("#batteryKey").val(param);

    var formEdit = $('#editTestbatteryModal');

    resetBatteries();

    if (param != undefined) {
        $("[name='editTestbatteryField']").html(doc.getDocLabel("page_testbattery", "edittestbattery_field"));
        formEdit.find("#battery").prop("readonly", "readonly");
        var jqxhr = $.getJSON("ReadTestBatteryContent?test=true", "param=" + param);
        $.when(jqxhr).then(function (data) {
            var obj = data["contentTable"];


            formEdit.find("#battery").prop("value", obj["testbattery"]);
            formEdit.find("#description").prop("value", obj["description"]);
            formEdit.find("#id").prop("value", obj["batteryID"]);

            if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
                formEdit.find("#description").prop("readonly", "readonly");
                formEdit.find("#id").prop("readonly", "readonly");

                $('#editTestbatteryButton').attr('class', '');
                $('#editTestbatteryButton').attr('hidden', 'hidden');
            }

            if ($("#editTestbatteryModal #batteryTestcasesTable_wrapper").length > 0) {
                $("#editTestbatteryModal #batteryTestcasesTable").DataTable().clear();
                $("#editTestbatteryModal #batteryTestcasesTable").DataTable().rows.add(data.contentTable.battery).draw();
            } else {
                var configurations = new TableConfigurationsClientSide("batteryTestcasesTable", data.contentTable.battery, aoColumnsFunc2("batteryTestcasesTable"), true);
                createDataTableWithPermissions(configurations, renderOptionsForBattery2, "#batteryTestcaseList", undefined, true);
            }
            renderOptionsForBattery2("batteryTestcasesTable");

        });
    } else {
        $("[name='editTestbatteryField']").html(doc.getDocLabel("page_testbattery", "addtestbattery_field"));
        formEdit.find("#battery").attr("readonly", false);
        if ($("#editTestbatteryModal #batteryTestcasesTable_wrapper").length > 0) {
            $("#editTestbatteryModal #batteryTestcasesTable").DataTable().clear().draw();
        } else {
            var configurations = new TableConfigurationsClientSide("batteryTestcasesTable", [], aoColumnsFunc2("batteryTestcasesTable"), true);
            createDataTableWithPermissions(configurations, renderOptionsForBattery2, "#batteryTestcaseList", undefined, true);
        }
        renderOptionsForBattery2("batteryTestcasesTable");
    }

    if ($("#editTestbatteryModal #batteryTestcases2Table_wrapper").length > 0) {
        $("#editTestbatteryModal #batteryTestcases2Table").DataTable().draw();
    } else {
        var configurations = new TableConfigurationsServerSide("batteryTestcases2Table", "ReadTestCase", "contentTable", aoColumnsFunc3("batteryTestcases2Table"), [1, 'asc']);
        var table = createDataTableWithPermissions(configurations, renderOptionsForBattery3, "#batteryTestcase3List", undefined, true);
    }

    $('#editTestbatteryModal .nav-tabs a[href="#tabsCreate-1"]').tab('show');

    formEdit.modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editTestbatteryModal'));
    var formEdit = $('#editTestbatteryModal #editTestbatteryModalForm :input');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var batteries = null;
    if ($("#batteryTestcasesTable_wrapper").length > 0) {
        batteries = $("#batteryTestcasesTable").DataTable().data().toArray();
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestbatteryModal');

    if ($("#batteryKey").val() != "") {
        $.ajax({
            url: "UpdateTestBattery2",
            async: true,
            method: "POST",
            data: {
                testBattery: data.battery,
                testbatteryID: data.id,
                description: data.description,
                batteryContent: JSON.stringify(batteries)
            },
            success: function (data) {
                data = JSON.parse(data);
                hideLoaderInModal('#editTestbatteryModal');
                if (getAlertType(data.messageType) === 'success') {
                    var oTable = $("#testbatterysTable").dataTable();
                    oTable.fnDraw(true);
                    $('#editTestbatteryModal').modal('hide');
                    showMessage(data);
                } else {
                    showMessage(data, $('#editTestbatteryModal'));
                }
            },
            error: showUnexpectedError
        });
    } else {
        $.ajax({
            url: "CreateTestBattery",
            async: true,
            method: "POST",
            data: {
                testBattery: data.battery,
                testbatteryID: data.id,
                description: data.description,
                batteryContent: JSON.stringify(batteries)
            },
            success: function (data) {
                data = JSON.parse(data);
                hideLoaderInModal('#editTestbatteryModal');
                if (getAlertType(data.messageType) === 'success') {
                    var oTable = $("#testbatterysTable").dataTable();
                    oTable.fnDraw(true);
                    $('#editTestbatteryModal').modal('hide');
                    showMessage(data);
                } else {
                    showMessage(data, $('#editTestbatteryModal'));
                }
            },
            error: showUnexpectedError
        });
    }
}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editTestbatteryModal #editTestbatteryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editTestbatteryModal'));
}

function removeEntryClick(batteryTest) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var param = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteTestBattery2?key=" + param,
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#confirmationModal');
                var oTable = $("#testbatterysTable").dataTable();
                oTable.fnDraw(true);
                $('#confirmationModal').modal('hide');
                showMessage(data);
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_testbattery", "title_remove"), doc.getDocLabel("page_testbattery", "message_remove"), batteryTest, undefined, undefined, undefined);

}

function removeTestCaseEntryClick(tableId, key, key2) {
    $('#' + tableId + '_wrapper #removeTestbattery').filter(function (i, e) {
        return $(e).attr("key") == key && $(e).attr("key2") == key2;
    }).off().prop("disabled", true);
    $("#" + tableId).DataTable().rows(function (i, d, n) {
        return d["test"] == key && d["testCase"] == key2;
    }).remove().draw();
    renderOptionsForBattery2(tableId);
}

function addTestCaseEntryClick() {
    var checked = getBatteries();
    var rows = [];
    for (var i = 0; i < checked.length; i++) {
        var test = checked[i].test;
        var testcase = checked[i].testCase;
        if (!$("#editTestbatteryModal #batteryTestcasesTable").DataTable().data().toArray().some(function (e) {
                return e.test == test && e.testCase == testcase
            })) {
            rows.push({test: test, testCase: testcase});
        }
    }
    $("#editTestbatteryModal #batteryTestcasesTable").DataTable().rows.add(rows);
    $('#editTestbatteryModal .nav-tabs a[href="#tabsCreate-2"]').tab('show');
    resetBatteries();
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_testbattery", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editTestbattery = '<button id="editTestbattery" onclick="editEntryClick(\'' + obj["testbattery"] + '\');"\n\
                                        class="editBattery btn btn-default btn-xs margin-right5" \n\
                                        name="editTestbattery" title="' + doc.getDocLabel("page_testbattery", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var removeTestbattery = '<button id="removeTestbattery" onclick="removeEntryClick(\'' + obj["testbattery"] + '\');"\n\
                                        class="removeTestbattery btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestbattery" title="' + doc.getDocLabel("page_testbattery", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editTestbattery + removeTestbattery + '</div>';

            },
            "width": "100px"
        },
        {
            "data": "testbattery",
            "sName": "testbattery",
            "title": doc.getDocLabel("page_testbattery", "testbattery_col")
        },
        {
            "data": "description",
            "sName": "description",
            "title": doc.getDocLabel("page_testbattery", "description_col")
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
            "title": doc.getDocLabel("page_testbattery", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var removeTestbattery = '<button id="removeTestbattery" key="' + obj["test"] + '" key2="' + obj["testCase"] + '" onclick="removeTestCaseEntryClick(\'' + tableId + '\',\'' + obj["test"] + '\',\'' + obj["testCase"] + '\');"\n\
                                        class="removeTestbattery btn btn-default btn-xs margin-right5" \n\
                                        name="removeTestbattery" title="' + doc.getDocLabel("page_testbattery", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + removeTestbattery + '</div>';

            },
            "width": "100px"
        },
        {"data": "test", "sName": "test", "title": doc.getDocLabel("page_testbattery", "test_col")},
        {"data": "testCase", "sName": "testcase", "title": doc.getDocLabel("page_testbattery", "testcase_col")}
    ];
    return aoColumns;
}

//Public functions
var aoColumnsFunc3, updateBatteries, resetBatteries, getBatteries;

(function() {
    //Private field
    var batteries = [];

    updateBatteries = function(scope, test, testcase){
        if ($(scope).is(":checked")){
            batteries.push({test : test, testCase : testcase})
        }else{
            var index = batteries.map(function(e) { return e.test == test && e.testCase == testcase; }).indexOf(true);
            if (index > -1) {
                batteries.splice(index, 1);
            }
        }
        console.log(batteries);
    };

    resetBatteries = function(){
        batteries = [];
    };

    getBatteries = function(){
        return batteries;
    };

    aoColumnsFunc3 = function(tableId) {
        var doc = new Doc();
        var aoColumns = [
            {
                "data": null,
                "bSortable": false,
                "bSearchable": false,
                "title": doc.getDocOnline("page_global", "columnAction"),
                "sDefaultContent": "",
                "sWidth": "50px",
                "mRender": function (data, type, obj) {
                    var hasPermissions = $("#" + tableId).attr("hasPermissions");


                    var checked = "";
                    var index = batteries.map(function(e) { return e.test == obj["test"] && e.testCase == obj["testCase"]; }).indexOf(true);
                    if (index > -1) {
                        checked = "checked";
                    }
                    var check = '<input type="checkbox" value="" data-test="' + obj["test"] + '" data-testcase="' + obj["testCase"] + '" onchange="updateBatteries(this, \'' + obj["test"] + '\', \'' + obj["testCase"] + '\')" ' + checked + '>';

                    return '<div class="center btn-group width150">' + check + '</div>';

                }
            },
            {
                "data": "test",
                "sName": "tec.test",
                "title": doc.getDocOnline("test", "Test"),
                "sWidth": "120px",
                "sDefaultContent": ""
            },
            {
                "data": "testCase",
                "sName": "tec.testCase",
                "title": doc.getDocOnline("testcase", "TestCase"),
                "sWidth": "70px",
                "sDefaultContent": ""
            },
            {
                "data": "labels",
                "sName": "lab.label",
                "title": doc.getDocOnline("label", "label"),
                "sWidth": "170px",
                "sDefaultContent": "",
                "render": function (data, type, full, meta) {
                    var labelValue = '';
                    $.each(data, function (i, e) {
                        labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '">' + e.name + '</span></div> ';
                    });
                    return labelValue;
                }
            },
            {
                "data": "application",
                "sName": "tec.application",
                "title": doc.getDocOnline("application", "Application"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "project",
                "sName": "tec.project",
                "title": doc.getDocOnline("project", "idproject"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "usrCreated",
                "sName": "tec.usrCreated",
                "title": doc.getDocOnline("testcase", "Creator"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "usrModif",
                "sName": "tec.usrModif",
                "title": doc.getDocOnline("testcase", "LastModifier"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "tcActive",
                "sName": "tec.tcactive",
                "title": doc.getDocOnline("testcase", "TcActive"),
                "sDefaultContent": "",
                "sWidth": "70px"
            },
            {
                "data": "status",
                "sName": "tec.status",
                "title": doc.getDocOnline("testcase", "Status"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "priority",
                "sName": "tec.priority",
                "title": doc.getDocOnline("invariant", "PRIORITY"),
                "sWidth": "70px",
                "sDefaultContent": ""
            },
            {
                "data": "origine",
                "sName": "tec.origine",
                "title": doc.getDocOnline("testcase", "Origine"),
                "sWidth": "70px",
                "sDefaultContent": ""
            },
            {
                "data": "refOrigine",
                "sName": "tec.refOrigine",
                "title": doc.getDocOnline("testcase", "RefOrigine"),
                "sWidth": "80px",
                "sDefaultContent": ""
            },
            {
                "data": "group",
                "sName": "tec.group",
                "title": doc.getDocOnline("invariant", "GROUP"),
                "sWidth": "100px",
                "sDefaultContent": ""
            },
            {
                "data": "description",
                "sName": "tec.description",
                "title": doc.getDocOnline("testcase", "Description"),
                "sWidth": "300px",
                "sDefaultContent": ""
            },
            {
                "data": "dateCreated",
                "sName": "tec.dateCreated",
                "title": doc.getDocOnline("testcase", "TCDateCrea"),
                "sWidth": "150px",
                "sDefaultContent": ""
            }
        ];
        return aoColumns;
    };
})();
