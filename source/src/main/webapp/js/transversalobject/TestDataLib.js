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
    // changeAppServiceFromHere not needed with crbDropdown
})


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

    // crbDropdown loaders handle these now (no more displayInvariantList / displayAppServiceList)


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


    $('#editDataLibButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#duplicateDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#addDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');

    feedDataLibModal(dataLibEntry, "editTestDataLibModal", "EDIT");
}

/***
 * Open the modal with testcase information.
 * @param {String} dataLibEntry - type selected
 * @returns {null}
 */
function duplicateDataLibClick(dataLibEntry) {

    $('#duplicateDataLibButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition')
        .removeAttr('hidden');
    $('#editDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#addDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');

    feedDataLibModal(dataLibEntry, "editTestDataLibModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @param {String} dataLibEntry - type selected
 * @returns {null}
 */
function addDataLibClick(dataLibEntry, dataLibName) {

    // Prepare all Events handler of the modal.

    $('#addDataLibButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition')
        .removeAttr('hidden');
    $('#editDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');
    $('#duplicateDataLibButton').attr('class', 'hidden').attr('hidden', 'hidden');

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

                $('#editTestDataLibModal').find('[x-data]').each(function() { if (this.__x) this.__x.$data.open = false; });
                window.dispatchEvent(new CustomEvent('testdatalib-modal-close'));
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

                    window.dispatchEvent(new CustomEvent('testdatalib-modal-open'));
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
        window.dispatchEvent(new CustomEvent('testdatalib-modal-open'));
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

        $('#editTestDataLibModal #types').val(obj.type);
        window.dispatchEvent(new CustomEvent('dlTypes-preselect', {detail: obj.type}));
        window.dispatchEvent(new CustomEvent('dl-type-changed', {detail: obj.type}));
        window.dispatchEvent(new CustomEvent('dlSystem-preselect', {detail: obj.system}));
        window.dispatchEvent(new CustomEvent('dlEnvironment-preselect', {detail: obj.environment}));
        window.dispatchEvent(new CustomEvent('dlCountry-preselect', {detail: obj.country}));

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
        window.dispatchEvent(new CustomEvent('dlDatabaseUrl-preselect', {detail: obj.databaseUrl}));

        // Service - use crbDropdown preselect instead of select2
        window.dispatchEvent(new CustomEvent('dlService-preselect', {detail: obj.service}));

        $('#editTestDataLibModal #servicepaths').prop("value", obj.servicePath);
        $('#editTestDataLibModal #methods').prop("value", obj.method);
        $('#editTestDataLibModal #envelope').text(obj.envelope);
//        activateSOAPServiceFields("#editTestDataLibModal", obj.service);
        window.dispatchEvent(new CustomEvent('dlDatabaseCsv-preselect', {detail: obj.databaseCsv}));
        $('#editTestDataLibModal #csvUrl').prop("value", obj.csvUrl);
        $('#editTestDataLibModal #separator').prop("value", obj.separator);
        $('#editTestDataLibModal #ignoreFirstLine').prop("checked", obj.ignoreFirstLine);

        window.dispatchEvent(new CustomEvent('dlDatabase-preselect', {detail: obj.database}));
        $('#editTestDataLibModal #script').text(obj.script);

        $('#editTestDataLibModal #libdescription').prop("value", obj.description);
        $('#editTestDataLibModal #group').prop("value", obj.group);

        $('#editTestDataLibModal #created').prop("value", getDate(obj.created));
        $('#editTestDataLibModal #creator').prop("value", obj.creator);
        $('#editTestDataLibModal #lastModified').prop("value", getDate(obj.lastModified));
        $('#editTestDataLibModal #lastModifier').prop("value", obj.lastModifier);

        // changeAppServiceFromHere not needed with crbDropdown

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
        editor.setTheme(getAceTheme());
        editor.getSession().setMode("ace/mode/xml");
        editor.setOptions({
            maxLines: Infinity
        });
        document.getElementById('envelope').style.fontSize = '16px';

        //Highlight envelop on modal loading
        var editor = ace.edit($("#editTestDataLibModal #script")[0]);
        editor.setTheme(getAceTheme());
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
            window.dispatchEvent(new CustomEvent('dlTypes-preselect', {detail: 'INTERNAL'}));
            window.dispatchEvent(new CustomEvent('dl-type-changed', {detail: 'INTERNAL'}));
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

    var inputClasses = "w-full h-8 border rounded-md px-3 py-1 text-xs bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-200 transition-all focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500";
    var tdStyle = { "padding": "10px 8px", "verticalAlign": "middle" };

    if (isKey) {
        var deleteBtn = $("<button type='button' disabled='disabled'></button>")
            .css({ "padding": "6px 8px", "border-radius": "8px", "border": "1px solid #e2e8f0", "background": "#f8fafc", "color": "#94a3b8", "cursor": "not-allowed", "display": "flex", "alignItems": "center", "justifyContent": "center", "fontSize": "12px", "width": "34px", "height": "34px" })
            .html('<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>');
        var subDataInput = $("<input maxlength='200' disabled='disabled'>").addClass(inputClasses).css({"background": "#f8fafc", "color": "#94a3b8", "borderColor": "#e2e8f0"}).val(subdata.subData);
    } else {
        var deleteBtn = $("<button type='button'></button>")
            .css({ "padding": "6px 8px", "border-radius": "8px", "border": "1px solid #fca5a5", "background": "#fef2f2", "color": "#dc2626", "cursor": "pointer", "display": "flex", "alignItems": "center", "justifyContent": "center", "fontSize": "12px", "transition": "all 0.2s", "width": "34px", "height": "34px" })
            .html('<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>')
            .on("mouseenter", function() { $(this).css({ "background": "#fecaca", "borderColor": "#f87171" }); })
            .on("mouseleave", function() { if (!subdata.toDelete) $(this).css({ "background": "#fef2f2", "borderColor": "#fca5a5" }); });
        var subDataInput = $("<input onkeydown='return dtl_keyispressed(event);' maxlength='200' placeholder='Sub data name'>").addClass(inputClasses).val(subdata.subData);
    }
    subdata.encrypt === "Y" ? subdata.encrypt = true : subdata.encrypt = false;
    var encryptInput = $("<input type='checkbox'>").css({ "width": "18px", "height": "18px", "accentColor": "#3b82f6", "cursor": "pointer" }).prop("checked", subdata.encrypt);
    var typeStyle = subdata.encrypt === true ? "password" : "";
    var valueInput = $("<input type='" + typeStyle + "' placeholder='Value'>").addClass(inputClasses).val(subdata.value);
    var columnInput = $("<input maxlength='255' placeholder='Column'>").addClass(inputClasses).val(subdata.column);
    var parsingAnswerInput = $("<input placeholder='XPath / JSONPath'>").addClass(inputClasses).val(subdata.parsingAnswer);
    var columnPositionInput = $("<input maxlength='45' placeholder='Position'>").addClass(inputClasses).val(subdata.columnPosition);
    var descriptionInput = $("<input maxlength='1000' placeholder='Description'>").addClass(inputClasses).val(subdata.description);
    var table = $("#" + targetTableBody);
    var isDark = document.documentElement.classList.contains('dark');
    var hoverBg = isDark ? '#1e293b' : '#f8fafc';

    var row = $("<tr></tr>").css({ "transition": "all 0.2s" })
        .on("mouseenter", function() { if (!subdata.toDelete) $(this).css("background", hoverBg); })
        .on("mouseleave", function() { if (!subdata.toDelete) $(this).css("background", ""); });
    var deleteBtnRow = $("<td></td>").css($.extend({}, tdStyle, { "width": "50px", "textAlign": "center" })).append(deleteBtn);
    var subData = $("<td></td>").css($.extend({}, tdStyle, { "minWidth": "120px" })).append(subDataInput);
    var encrypt = $("<td></td>").css($.extend({}, tdStyle, { "textAlign": "center", "width": "70px" })).append(encryptInput);
    var value = $("<td></td>").css($.extend({}, tdStyle, { "minWidth": "120px" })).append(valueInput);
    var column = $("<td></td>").css($.extend({}, tdStyle, { "minWidth": "100px" })).append(columnInput);
    var parsingAnswer = $("<td></td>").css($.extend({}, tdStyle, { "minWidth": "120px" })).append(parsingAnswerInput);
    var columnPosition = $("<td></td>").css($.extend({}, tdStyle, { "width": "100px" })).append(columnPositionInput);
    var description = $("<td></td>").css($.extend({}, tdStyle, { "minWidth": "140px" })).append(descriptionInput);
    deleteBtn.click(function () {
        subdata.toDelete = (subdata.toDelete) ? false : true;

        if (subdata.toDelete) {
            row.css({ "background": "#fef2f2", "opacity": "0.5" });
            row.find("input[type='text'], input[type='password']").css({ "textDecoration": "line-through", "color": "#94a3b8", "background": "#fef2f2" });
            deleteBtn.css({ "background": "#dc2626", "borderColor": "#dc2626", "color": "white" });
            deleteBtn.html('<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 15l3-3m0 0l3-3m-3 3l-3-3m3 3l3 3"/><circle cx="12" cy="12" r="10"/></svg>');
        } else {
            row.css({ "background": "", "opacity": "1" });
            row.find("input[type='text'], input[type='password']").css({ "textDecoration": "none", "color": "#0f172a", "background": "white" });
            deleteBtn.css({ "background": "#fef2f2", "borderColor": "#fca5a5", "color": "#dc2626" });
            deleteBtn.html('<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>');
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
