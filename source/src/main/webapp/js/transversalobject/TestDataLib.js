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
var initMode = "";


$(function () {
    $('[data-toggle="popover"]').popover()
    $('#editTestDataLibModal #types').change(function () {
        collapseOrExpandTypes();
    });

    $('#editTestDataLibModal #service').change(function () {
        changeAppServiceFromHere();
    });

})

function collapseOrExpandTypes() {

    var typesVal = $('#editTestDataLibModal #types').val();
    if (typesVal === "SQL") {
        $("#panelSQLEdit").collapse("show");
        $("#panelSERVICEEdit").collapse("hide");
        $("#panelCSVEdit").collapse("hide");
    } else if (typesVal === "SERVICE") {
        $("#panelSQLEdit").collapse("hide");
        $("#panelSERVICEEdit").collapse("show");
        $("#panelCSVEdit").collapse("hide");
    } else if (typesVal === "FILE") {
        $("#panelSQLEdit").collapse("hide");
        $("#panelSERVICEEdit").collapse("hide");
        $("#panelCSVEdit").collapse("show");
    } else {
        $("#panelSQLEdit").collapse("hide");
        $("#panelSERVICEEdit").collapse("hide");
        $("#panelCSVEdit").collapse("hide");
    }

}
function changeAppServiceFromHere() {

    var doc = new Doc();

    $('#editTestDataLibModal #service').parent().find(".input-group-btn").remove();

    var formEdit = $("#editTestDataLibModal #editTestLibData");

    if (!isEmpty($('#editTestDataLibModal #service').val())) {

//        activateSOAPServiceFields("#editTestDataLibModal #editTestLibData", $('#editTestDataLibModal #service').val());

        var editEntry = '<span class="input-group-btn" style="vertical-align:bottom!important"><button id="editEntry" onclick="openModalAppService(\'' + $('#editTestDataLibModal #service').val() + '\',\'EDIT\' );"\n\
            class="buttonObject btn btn-sm btn-default " \n\
           title="' + doc.getDocLabel("page_applicationObject", "button_create") + '" type="button">\n\
            <span class="glyphicon glyphicon-pencil"></span></button></span>';
//        var emptyEntry = '<span class="input-group-btn" style="vertical-align:bottom!important"><button id="emptyEntry" onclick="emptyService();"\n\
//            class="buttonObject btn btn-default " \n\
//           title="Empty" type="button">\n\
//            <span class="glyphicon glyphicon-remove"></span></button></span>';
        $('#editTestDataLibModal #service').parent().append(editEntry);

//    } else {

//        activateSOAPServiceFields("#editTestDataLibModal #editTestLibData", $('#editTestDataLibModal #service').val());

    }

}


function emptyService() {
    $("#editTestDataLibModal #service").val("").trigger('change');
}


/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the DeployType list
 * @param {String} dataLibEntryId Datalib ID. If defined, the modal will appear directly on that entry
 * @param {String} dataLibName  Name of the datalib.
 * @param {String} mode of the modal to open (either EDIT, ADD, DUPLICATE)
 * @param {String} page
 * @param {String} aceElementId
 * @returns {void}
 */
function openModalDataLib(dataLibEntryId, dataLibName, mode, page, aceElementId) {

//    Modal is now init on master page load #1748
//    if ($('#editTestDataLibModal').data("initLabel") === undefined) {
//        initModalDataLib();
//        $('#editTestDataLibModal').data("initLabel", true);
//    }
    initMode = mode;

    $('[data-toggle="popover"]').popover()

    console.info("libName " + dataLibName)
    let firstEntry = feedSelect(dataLibName, "datalibEntry", dataLibEntryId);

    if (dataLibEntryId === null) {
        console.info("empty Modal and select entry " + firstEntry)
        if (mode !== "ADD") {
            dataLibEntryId = firstEntry;
        } else {
            // hide id=selectDatalibID
        }
    }

    if (mode === "EDIT") {
        editDataLibClick(dataLibEntryId);
    } else if (mode === "ADD") {
        addDataLibClick(dataLibEntryId, dataLibName);
    } else {
        duplicateDataLibClick(dataLibEntryId);
    }


    var availableDataLibName = [];
    $.ajax({
        url: "ReadTestDataLib?columnName=tdl.Name",
        dataType: "json",
        success: function (data) {
            for (var i = 0; i < data.distinctValues.length; i++) {
                availableDataLibName.push(data.distinctValues[i]);
            }
        }
    });
    $('#editTestLibData input#tdlname').autocomplete({
        source: availableDataLibName,
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

    $("#datalibEntry").off("change");
    $("#datalibEntry").change(function () {
        console.info("Change " + mode + "  " + dataLibEntryId + $(this).val());
        if ($(this).val() === "ZZNEW-ENTRYZZ") {
            mode = "ADD";
        } else {
            mode = initMode;
        }
        if (mode === "EDIT") {
            editDataLibClick($(this).val());
        } else if (mode === "ADD") {
            addDataLibClick($(this).val());
        } else {
            duplicateDataLibClick($(this).val());
        }


    });


    $("#editDataLibButton").off("click");
    $("#editDataLibButton").click(function () {
        $("#SubdataTable_edit").find("button").click;
        confirmDataLibModalHandler("EDIT", page, aceElementId, dataLibName);
    });

    $("#addDataLibButton").off("click");
    $("#addDataLibButton").click(function () {
        confirmDataLibModalHandler("ADD", page, aceElementId, dataLibName);
    });

    $("#duplicateDataLibButton").off("click");
    $("#duplicateDataLibButton").click(function () {
        confirmDataLibModalHandler("DUPLICATE", page, aceElementId, dataLibName);
    });

    bindToggleFullscreen();

}

function initModalDataLib() {

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'z-index': 1060,
        'container': 'body'}
    )

    var doc = new Doc();

    // Click on add row button adds a Subdata entry.
    $("#addSubData_edit").off("click");
    $("#addSubData_edit").click(function () {
        addNewSubDataRow("SubdataTable_edit");
    });

    displayInvariantList("system", "SYSTEM", false, "", "");
    displayInvariantList("environment", "ENVIRONMENT", false, "", "");
    displayInvariantList("country", "COUNTRY", false, "", "");
    displayInvariantList("database", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("databaseUrl", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("databaseCsv", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("types", "TESTDATATYPE", false, "INTERNAL");

    displayAppServiceList("service", "", "", "");


    $("#testCaseListModalLabel").text(doc.getDocLabel("page_testdatalib_m_gettestcases", "title"));
    // TestDataLib content
    $("[name='lbl_tdlname']").html(doc.getDocOnline("testdatalib", "name"));
    $("[name='lbl_type']").html(doc.getDocOnline("testdatalib", "type"));
    $("[name='lbl_system']").html(doc.getDocOnline("testdatalib", "system"));
    $("[name='lbl_environment']").html(doc.getDocOnline("testdatalib", "environment"));
    $("[name='lbl_country']").html(doc.getDocOnline("testdatalib", "country"));
    $("[name='lbl_description']").html(doc.getDocOnline("testdatalib", "description"));
    $("[name='lbl_database']").html(doc.getDocOnline("testdatalib", "database"));
    $("[name='lbl_script']").html(doc.getDocOnline("testdatalib", "script"));
    $("[name='lbl_databaseUrl']").html(doc.getDocOnline("testdatalib", "databaseUrl"));
    $("[name='lbl_service_path']").html(doc.getDocOnline("testdatalib", "servicepath"));
    $("[name='lbl_service']").html(doc.getDocOnline("testdatalib", "service"));
    $("[name='lbl_method']").html(doc.getDocOnline("testdatalib", "method"));
    $("[name='lbl_envelope']").html(doc.getDocOnline("testdatalib", "envelope"));
    $("[name='lbl_databaseCsv']").html(doc.getDocOnline("testdatalib", "databaseCsv"));
    $("[name='lbl_csvUrl']").html(doc.getDocOnline("testdatalib", "csvUrl"));
    $("[name='lbl_separator']").html(doc.getDocOnline("testdatalib", "separator"));
    $("[name='lbl_group']").html(doc.getDocOnline("testdatalib", "group"));
    $("[name='lbl_privateData']").html(doc.getDocOnline("testdatalib", "privateData"));
    // Sub Data content
    $("[name='subdataHeader']").html(doc.getDocOnline("testdatalibdata", "subData"));
    $("[name='encryptHeader']").html(doc.getDocOnline("testdatalibdata", "encrypt"));
    $("[name='valueHeader']").html(doc.getDocOnline("testdatalibdata", "value"));
    $("[name='columnHeader']").html(doc.getDocOnline("testdatalibdata", "column"));
    $("[name='parsingAnswerHeader']").html(doc.getDocOnline("testdatalibdata", "parsingAnswer"));
    $("[name='columnPositionHeader']").html(doc.getDocOnline("testdatalibdata", "columnPosition"));
    $("[name='descriptionHeader']").html(doc.getDocOnline("testdatalibdata", "description"));
    // Traceability
    $("[name='lbl_created']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_creator']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_lastModified']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_lastModifier']").html(doc.getDocOnline("transversal", "UsrModif"));

    //service and sql specific configurations
    $("[name='sqlConfigurationsLbl']").html(doc.getDocOnline("page_testdatalib", "title_sql_configurations"));
    $("[name='serviceConfigurationsLbl']").html(doc.getDocOnline("page_testdatalib", "title_service_configurations"));
    $("[name='csvConfigurationsLbl']").html(doc.getDocOnline("page_testdatalib", "title_csv_configurations"));

    //buttons
    $("#cancelTestDataLib").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#addDataLibButton").text(doc.getDocLabel("page_global", "btn_add"));
    //buttons    
    $("#cancelDuplicateTestDataLib").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#duplicateDataLibButton").text(doc.getDocLabel("page_global", "btn_duplicate"));
    //cancel + add buttons
    $("#editDataLibButton").text(doc.getDocLabel("page_global", "buttonEdit"));
    $("#cancelTestDataLibButton").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#closeButton").text(doc.getDocLabel("page_global", "buttonClose"));
    //tabs, tab2 is updated when the entries are managed
    $("[name='tab1Text']").text(doc.getDocLabel("page_testdatalib", "m_tab1_text"));
    $("[name='tab2Text']").text(doc.getDocLabel("page_testdatalib", "m_tab2_text"));
    $("[name='tab3Text']").text(doc.getDocLabel("page_testdatalib", "m_tab3_text"));

}

/***
 * Open the modal with testcase information.
 * @param {String} dataLibEntry - type selected
 * @returns {null}
 */
function editDataLibClick(dataLibEntry) {

    clearResponseMessage($('#editApplicationObjectModal'));

    var doc = new Doc();
    $("[name='editSoapLibraryField']").html(doc.getDocLabel("page_appservice", "editSoapLibrary_field"));


    $('#editDataLibButton').attr('class', 'btn btn-primary');
    $('#editDataLibButton').removeProp('hidden');
    $('#duplicateDataLibButton').attr('class', '');
    $('#duplicateDataLibButton').attr('hidden', 'hidden');
    $('#addDataLibButton').attr('class', '');
    $('#addDataLibButton').attr('hidden', 'hidden');

    feedDataLibModal(dataLibEntry, "editTestDataLibModal", "EDIT");
}

/***
 * Open the modal with testcase information.
 * @param {String} dataLibEntry - type selected
 * @returns {null}
 */
function duplicateDataLibClick(dataLibEntry) {

    $('#editDataLibButton').attr('class', '');
    $('#editDataLibButton').attr('hidden', 'hidden');
    $('#duplicateDataLibButton').attr('class', 'btn btn-primary');
    $('#duplicateDataLibButton').removeProp('hidden');
    $('#addDataLibButton').attr('class', '');
    $('#addDataLibButton').attr('hidden', 'hidden');

    feedDataLibModal(dataLibEntry, "editTestDataLibModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @param {String} dataLibEntry - type selected
 * @returns {null}
 */
function addDataLibClick(dataLibEntry, dataLibName) {

    // Prepare all Events handler of the modal.

    $('#editDataLibButton').attr('class', '');
    $('#editDataLibButton').attr('hidden', 'hidden');

    $('#addDataLibButton').attr('class', 'btn btn-primary');
    $('#addDataLibButton').removeProp('hidden');

    $('#duplicateDataLibButton').attr('class', '');
    $('#duplicateDataLibButton').attr('hidden', 'hidden');

    feedDataLibModal(dataLibEntry, "editTestDataLibModal", "ADD", dataLibName);
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} page - page where the modal is open.
 * @param {String} aceElementId - id of the ace element when page if TestCaseScript_Props.
 * @returns {null}
 */
function confirmDataLibModalHandler(mode, page, aceElementId, dataLibName) {
    //shows the modal that allows the creation of test data lib 
    var formEdit = $("#editTestDataLibModal #editTestLibData");

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#service").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateTestDataLib";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateTestDataLib";
    }

    //START client-side validation
    //validates if the property name is not empty
    var nameElement = formEdit.find("#tdlname");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_testdatalib", "empty_name_message"));
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addTestDataLibModal'));
        //return ;
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }
    //check if entries have repeated names
    var noRepeated = validateSubDataEntriesRepeated($("#addTestDataLibModal"), "addSubDataTableBody", false);
    if (nameElementEmpty || !noRepeated) {
        return;
    }
    //END client-side validation

    showLoaderInModal('#editTestDataLibModal');

    // Getting Data from Database TAB
    var table1 = $("#subdataTableBody_edit tr");
    var table_subdata = [];

    for (var i = 0; i < table1.length; i++) {
        table_subdata.push($(table1[i]).data("subdata"));
    }

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formEdit.serialize());
    dataForm = JSON.parse(JSON.stringify(dataForm).split('"types":').join('"type":'));
    dataForm = JSON.parse(JSON.stringify(dataForm).split('"servicepaths":').join('"servicepath":'));
    dataForm = JSON.parse(JSON.stringify(dataForm).split('"methods":').join('"method":'));
    //Add envelope and script, not in the form
    var editorEnv = ace.edit($("#editTestLibData #envelope")[0]);
    dataForm.envelope = encodeURIComponent(editorEnv.getSession().getDocument().getValue());
    var editorScr = ace.edit($("#editTestLibData #script")[0]);
    dataForm.script = encodeURIComponent(editorScr.getSession().getDocument().getValue());

    var file = $("#editTestDataLibModal input[type=file]");
    files = file.prop("files")[0]

    dataForm.privateData = $('#editTestDataLibModal #privateData').prop("checked");
    dataForm.ignoreFirstLine = $('#editTestDataLibModal #ignoreFirstLine').prop("checked");

    var sa = formEdit.serializeArray();
    var formData = new FormData();


    for (var i in dataForm) {
        formData.append(i, dataForm[i]);
    }

    formData.append("file", file.prop("files")[0]);
    formData.append("name", nameElement.prop("value"));

    formData.append("subDataList", encodeURIComponent(JSON.stringify(table_subdata)));

    $.ajax({
        async: true,
        url: myServlet,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            hideLoaderInModal('#editTestDataLibModal');
            if (getAlertType(data.messageType) === "success") {
                if (page === "TestDataLibList") {
                    var oTable = $("#listOfTestDataLib").dataTable();
                    oTable.fnDraw(false);
                } else {
                    if (page === "TestCaseScript_Props") {
                        var editor = ace.edit($("#" + aceElementId)[0])
                        editor.setValue($("#tdlname").val());
                    } else if (page === "TestCaseScript_Steps") {
                        var Tags = getTags();
                        for (var i = 0; i < Tags.length; i++) {
                            if (Tags[i].name === "datalib") {
                                Tags[i].array.push(formData.get("name"));
                            }
                        }
                        $("div.step-action .content div.fieldRow div:nth-child(n+2) input").trigger("input");
                    }
                }

                $('#editTestDataLibModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editTestDataLibModal'));
            }
        },
        error: showUnexpectedError
    });
}

function feedSelect(datalibName, selectId, defaultValue) {
    $("select[id='" + selectId + "']").empty();
    let firstEntry = null;

    $.ajax({
        async: false,
        url: "ReadTestDataLib?like=false&limit=10000&name=" + datalibName,
        method: "GET",
        success: function (data) {
            for (var option in data.contentTable) {
                let tdl = data.contentTable[option];

                if (firstEntry === null) {
                    firstEntry = tdl.testDataLibID;
                }

                $("select[id='" + selectId + "']").append($('<option></option>').text(tdl.testDataLibID + "   [  " + concatVariations(tdl.system, tdl.environment, tdl.country, "  |  ", "<<DEFAULT>>") + "  ] " + tdl.type).val(tdl.testDataLibID));
            }

            console.info(defaultValue);
            console.info(firstEntry);
            if (defaultValue !== null) {
                $("select[id='" + selectId + "']").val(defaultValue);
            } else {
                if (firstEntry !== null) {
                    $("select[id='" + selectId + "']").val(firstEntry);
                }
            }

            $("select[id='" + selectId + "']").append($('<option></option>').text("--- ADD NEW ENTRY ---").val("ZZNEW-ENTRYZZ"));

        },
        error: showUnexpectedError
    });

    return firstEntry;

}

function concatVariations(s1, s2, s3, sep, defaulWhenEmpty) {
    let f = "";
    if (s1 !== null && s1.length > 0)
        f += sep + s1;
    if (s2 !== null && s2.length > 0)
        f += sep + s2;
    if (s3 !== null && s3.length > 0)
        f += sep + s3;
    if (f.length > 1)
        f = f.substring(sep.length)
    if (f.length === 0)
        return defaulWhenEmpty;
    return f;
}
/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} datalibId - type selected
 * @param {String} modalId - type selected
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedDataLibModal(datalibId, modalId, mode, dataLibName) {
    clearResponseMessageMainPage();
    var formEdit = $('#' + modalId);

    if (mode === "DUPLICATE" || mode === "EDIT") {

        $.ajax({
            url: "ReadTestDataLib?testdatalibid=" + datalibId,
            async: true,
            method: "GET",
            success: function (data) {
                if (data.messageType === "OK") {
                    // Feed the data to the screen and manage authorities.
                    var service = data.testDataLib;
                    feedDataLibModalData(service, modalId, mode, data.hasPermissions);

                    formEdit.modal('show');
                } else {
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });

    } else {

        var DataObj1 = {};
        var hasPermissions = true;
        DataObj1.system = "";
        DataObj1.country = "";
        DataObj1.environment = "";
        DataObj1.libdescription = "";
        DataObj1.cvsUrl = "";
        DataObj1.database = "";
        DataObj1.databaseCsv = "";
        DataObj1.databaseUrl = "";
        DataObj1.envelope = "";
        DataObj1.privateData = false;
        DataObj1.group = "";
        DataObj1.method = "";
        DataObj1.name = dataLibName;
        DataObj1.script = "";
        DataObj1.separator = "";
        DataObj1.ignoreFirstLine = true;
        DataObj1.servicepath = "";
        DataObj1.service = "";
        DataObj1.testdatalibid = "";
        DataObj1.type = "";
        DataObj1.subDataList = "";

        feedDataLibModalData(DataObj1, modalId, mode, hasPermissions);
        formEdit.modal('show');
    }

}

//function activateSOAPServiceFields(modal, serviceValue) {
//    if (isEmpty(serviceValue)) {
//        $(modal + " #servicepaths").prop("readonly", false);
//        $(modal + " #methods").prop("readonly", false);
//        var editor = ace.edit($(modal + " #envelope")[0]);
//        editor.container.style.opacity = 1;
//        editor.renderer.setStyle("disabled", false);
//    } else {
//        $(modal + " #servicepaths").prop("readonly", true);
//        $(modal + " #methods").prop("readonly", true);
//        var editor = ace.edit($(modal + " #envelope")[0]);
//        editor.container.style.opacity = 0.5;
//        editor.renderer.setStyle("disabled", true);
//    }
//}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} testDataLib - service object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedDataLibModalData(testDataLib, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    //Destroy the previous Ace object.

    ace.edit($("#editTestDataLibModal #envelope")[0]).destroy();
    ace.edit($("#editTestDataLibModal #script")[0]).destroy();

    if (isEmpty(testDataLib)) {
//        activateSOAPServiceFields("#editTestDataLibModal", "");
        // Cleaning the Subdata Table.
        $('#subdataTableBody_edit tr').remove();
        addNewSubDataKeyRow("subdataTableBody_edit");

    } else {

        var obj = testDataLib;
        $('#editTestDataLibModal #testdatalibid').val(obj.testDataLibID);
        $('#editTestDataLibModal #tdlname').prop("value", obj.name);
        $("#buttonDownloadCsvFile").attr("href", "./api/testdatalibs/" + encodeURI(obj.testDataLibID) + "/csv/");

        $('#editTestDataLibModal #types').prop("value", obj.type);
        collapseOrExpandTypes();
        $('#editTestDataLibModal #system').find('option[value="' + obj.system + '"]').prop("selected", true);
        $('#editTestDataLibModal #environment').find('option[value="' + obj.environment + '"]').prop("selected", true);
        $('#editTestDataLibModal #country').find('option[value="' + obj.country + '"]').prop("selected", true);

        obj.privateData === "Y" ? obj.privateData = true : obj.privateData = false;
        var disabled = hasPermissionsUpdate ? "" : "disabled";
        $('#editTestDataLibModal #privateData').prop("checked", obj.privateData).prop("disabled", disabled);

        $('#editTestDataLibModal #messagePrivate').empty();
        if (obj.privateData) {
            $('#editTestDataLibModal #messagePrivate').html("This data is modifiable by " + obj.creator + " Only");
        }

        $('#editTestDataLibModal #privateData').off("click").click(function () {
            if ($('#editTestDataLibModal #privateData').prop("checked")) {
                if (obj.creator !== undefined) {
                    $('#messagePrivate').html("This data can only be modified by <b><i>" + obj.creator + "</i></b>");
                } else {
                    $('#messagePrivate').html("This data can only be modified by <b><i>" + getUser().login + "</i></b>  ");
                }
            } else {
                $('#messagePrivate').empty();
            }
        });

        //loads the information for the entries
        $('#editTestDataLibModal #databaseUrl').find('option[value="' + obj.databaseUrl + '"]:first').prop("selected", "selected");

//        $('#editTestDataLibModal #service').find('option[value="' + obj.service + '"]:first').prop("selected", "selected");
        // init the select2
//        $('#editTestDataLibModal #service').select2(getComboConfigService());
        $('#editTestDataLibModal #service').select2();
        // set it with the service value
        $("#editTestDataLibModal #service").val(obj.service).trigger('change');

        $('#editTestDataLibModal #servicepaths').prop("value", obj.servicePath);
        $('#editTestDataLibModal #methods').prop("value", obj.method);
        $('#editTestDataLibModal #envelope').text(obj.envelope);
//        activateSOAPServiceFields("#editTestDataLibModal", obj.service);
        $('#editTestDataLibModal #databaseCsv').find('option[value="' + obj.databaseCsv + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #csvUrl').prop("value", obj.csvUrl);
        $('#editTestDataLibModal #separator').prop("value", obj.separator);
        $('#editTestDataLibModal #ignoreFirstLine').prop("checked", obj.ignoreFirstLine);

        $('#editTestDataLibModal #database').find('option[value="' + obj.database + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #script').text(obj.script);

        $('#editTestDataLibModal #libdescription').prop("value", obj.description);
        $('#editTestDataLibModal #group').prop("value", obj.group);

        $('#editTestDataLibModal #created').prop("value", getDate(obj.created));
        $('#editTestDataLibModal #creator').prop("value", obj.creator);
        $('#editTestDataLibModal #lastModified').prop("value", getDate(obj.lastModified));
        $('#editTestDataLibModal #lastModifier').prop("value", obj.lastModifier);

        changeAppServiceFromHere();

        // Loading the list of subdata.       
        if ((mode === "EDIT") || (mode === "DUPLICATE")) {
            loadTestDataLibSubdataTable(obj.testDataLibID, "subdataTableBody_edit");
        } else {
            // Cleaning the Subdata Table.
            $('#subdataTableBody_edit tr').remove();
            addNewSubDataKeyRow("subdataTableBody_edit");
        }

        //loads groups from database
        var jqxhrGroups = $.getJSON("ReadTestDataLib", "groups");
        $.when(jqxhrGroups).then(function (groupsData) {
            //load distinct groups
            var doc = new Doc();
            loadSelectElement(groupsData["contentTable"], $('#editTestDataLibModal #groupedit'), true,
                    doc.getDocLabel("page_testdatalib_m_createlib", "lbl_dropdown_help"));
            //selects the group entered by the user
            $('#editTestDataLibModal #groupedit').find('option[value="' + obj.group + '"]:first').prop("selected", "selected");
            $('#editTestDataLibModal #groupedit').find('option:first').addClass("emptySelectOption");
            $('#editTestDataLibModal #groupedit').change();
        });

        if (!hasPermissionsUpdate) { // If readonly, we only readonly all fields
            $('#editDataLibButton').attr('class', '');
            $('#editDataLibButton').attr('hidden', 'hidden');
        }

        //Highlight envelop on modal loading
        var editor = ace.edit($("#editTestDataLibModal #envelope")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/xml");
        editor.setOptions({
            maxLines: Infinity
        });
        document.getElementById('envelope').style.fontSize = '16px';

        //Highlight envelop on modal loading
        var editor = ace.edit($("#editTestDataLibModal #script")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/sql");
        editor.setOptions({
            maxLines: Infinity
        });
        document.getElementById('script').style.fontSize = '16px';
    }

    formEdit.find("#testdatalibid").prop("readonly", "readonly");
    formEdit.find("#tdlname").prop("readonly", "readonly");

    // Authorities
    if (mode === "EDIT") {
        formEdit.find("#tdlname").prop("readonly", "");
        $("#editTestDataLibModalLabel").html(doc.getDocOnline("page_testdatalib", "title_edit"));
    } else {
        formEdit.find("#tdlname").prop("readonly", "");
        if (mode === "ADD") {
            $('#editTestDataLibModal #types option[value="INTERNAL"]').prop("selected", true);
            $("#editTestDataLibModalLabel").html(doc.getDocOnline("page_testdatalib", "title_create"));
        } else {
            $("#editTestDataLibModalLabel").html(doc.getDocOnline("page_testdatalib", "title_duplicate"));
        }

        formEdit.find("#testdatalibid").val("");

    }

}

//Function to append 1 line of Subdata in the various SubData lists.
function appendSubDataRow(subdata, targetTableBody) {
    var doc = new Doc();
    var isKey = false;
    if (subdata.subData === "") {
        isKey = true;
    }
    if (isKey) {
        var deleteBtn = $("<button type=\"button\" disabled=\"disabled\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
        var subDataInput = $("<input  maxlength=\"200\" disabled=\"disabled\">").addClass("form-control").val(subdata.subData);
    } else {
        var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
        var subDataInput = $("<input onkeydown=\"return dtl_keyispressed(event);\" maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "subData") + " --\">").addClass("form-control").val(subdata.subData);
    }
    subdata.encrypt === "Y" ? subdata.encrypt = true : subdata.encrypt = false;
    var encryptInput = $("<input type='checkbox' \">").prop("checked", subdata.encrypt).addClass("form-control input-sm");
    var typeStyle = subdata.encrypt === true ? "password" : "";
    var valueInput = $("<input type=\"" + typeStyle + "\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "value") + " --\">").addClass("form-control").val(subdata.value);
    var columnInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "column") + " --\">").addClass("form-control").val(subdata.column);
    var parsingAnswerInput = $("<input placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "parsingAnswer") + " --\">").addClass("form-control").val(subdata.parsingAnswer);
    var columnPositionInput = $("<input  maxlength=\"45\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "columnPosition") + " --\">").addClass("form-control").val(subdata.columnPosition);
    var descriptionInput = $("<input  maxlength=\"1000\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "description") + " --\">").addClass("form-control").val(subdata.description);
    var table = $("#" + targetTableBody);


    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var subData = $("<td></td>").append(subDataInput);
    var encrypt = $("<td></td>").append(encryptInput);
    var value = $("<td></td>").append(valueInput);
    var column = $("<td></td>").append(columnInput);
    var parsingAnswer = $("<td></td>").append(parsingAnswerInput);
    var columnPosition = $("<td></td>").append(columnPositionInput);
    var description = $("<td></td>").append(descriptionInput);
    deleteBtn.click(function () {
        subdata.toDelete = (subdata.toDelete) ? false : true;

        if (subdata.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    subDataInput.change(function () {
        subdata.subData = $(this).val();
    });
    encryptInput.change(function () {
        subdata.encrypt = $(this).prop("checked");
        if ($(this).prop("checked")) {
            $(this).parent().next().find("input").prop("type", "password");
        } else {
            $(this).parent().next().find("input").prop("type", "");
        }
    });
    valueInput.change(function () {
        subdata.value = $(this).val();
    });
    columnInput.change(function () {
        subdata.column = $(this).val();
    });
    parsingAnswerInput.change(function () {
        subdata.parsingAnswer = $(this).val();
    });
    columnPositionInput.change(function () {
        subdata.columnPosition = $(this).val();
    });
    descriptionInput.change(function () {
        subdata.description = $(this).val();
    });

    row.append(deleteBtnRow);
    row.append(subData);
    row.append(encrypt);
    row.append(value);
    row.append(column);
    row.append(parsingAnswer);
    row.append(columnPosition);
    row.append(description);
    row.data("subdata", subdata);
    table.append(row);
}

//Function to append 1 new line of Subdata in the various SubData lists.
function addNewSubDataRow(dataTableBody) {
    var nbRows = $("#" + dataTableBody + " tr").size();
    var newSubData = {
        subData: "SUBDATA" + nbRows,
        encrypt: false,
        value: "",
        column: "",
        parsingAnswer: "",
        columnPosition: "",
        description: "",
        testDataLibDataID: -1,
        toDelete: false
    };
    appendSubDataRow(newSubData, dataTableBody);
}

function addNewSubDataKeyRow(dataTableBody) {
    var newSubData = {
        subData: "",
        encrypt: false,
        value: "",
        column: "",
        parsingAnswer: "",
        columnPosition: "",
        description: "",
        testDataLibDataID: -1,
        toDelete: false
    };
    appendSubDataRow(newSubData, dataTableBody);
}

function loadTestDataLibSubdataTable(testDataLibID, subDataTableBody) {
    $('#' + subDataTableBody + ' tr').remove();
    var jqxhr = $.getJSON("ReadTestDataLibData", "testdatalibid=" + testDataLibID);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendSubDataRow(obj, subDataTableBody);
        });
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Auxiliary method that validates if there are subdata entries that are repeated
 * @param {type} dialog
 * @param {type} tableBody
 * @param {type} checkOnesMarkedToRemove
 * @returns {Boolean}
 */
function validateSubDataEntriesRepeated(dialog, tableBody, checkOnesMarkedToRemove) {
    var arrayValues = [];

    //client-side validation 
    var elementsWithRepeatedSubdata = $("#" + tableBody + " tr td:nth-child(2) input").filter(function () {
        var repeatedCount = 0;
        var parent = $(this).parents("div.form-group").addClass('has-error');

        //if empty we will check if there are any other row with the same value
        if ($.inArray(this.value, arrayValues) > -1) {
            $(parent).addClass('has-error');
            repeatedCount++;
        } else {
            if (checkOnesMarkedToRemove) {
                //if the operation is to remove, then we can ignore that item
                var parentTrOperation = $(this).parents("tr").attr("data-operation");
                if (parentTrOperation !== 'remove') {
                    arrayValues.push(this.value);
                }
            } else {
                arrayValues.push(this.value.trim());
            }
            //removes the error class if for some reason has it
            if ($(parent).hasClass('has-error')) {
                $(parent).removeClass('has-error');
            }

        }
        return repeatedCount !== 0;
    }).size();

    if (elementsWithRepeatedSubdata > 0) {
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_testdatalib", "duplicated_message") + elementsWithRepeatedSubdata);
        showMessage(localMessage, dialog);

        return false;
    }
    return true;

}

/**
 * Handler method that uploads a XML file
 */
function uploadTestDataLibFromXMLFile() {
    //gets the form and translates it in order to be uploadedshowModalUpload 
    var form = document.getElementById('formUpload');
    var formData = new FormData(form);
    showLoaderInModal("#modalUpload");
    var jqxhr = $.ajax({
        url: "ImportTestDataLib",
        type: "POST",
        data: formData,
        mimeType: "multipart/form-data",
        contentType: false,
        cache: false,
        processData: false,
        dataType: "json"

    });

    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#modalUpload");
        var oTable = $("#listOfTestDataLib").dataTable();
        oTable.fnDraw(false);
        $('#modalUpload').modal('hide');
        showMessageMainPage(getAlertType(data.messageType), data.message, false);
    }).fail(handleErrorAjaxAfterTimeout);
}

function dtl_keyispressed(e) {
    var toto = "|.| |(|)|%|";
    var charval = "|" + e.key + "|";
    if (toto.indexOf(charval) !== -1) {
        var localMessage = new Message("WARNING", "Character '" + e.key + "' is not allowed on subdata name. This is to avoid creating ambiguous syntax when using variabilization.");
        showMessage(localMessage, $('#editTestDataLibModal'), false, 1000);
        return false;
    }
    return true;
}
