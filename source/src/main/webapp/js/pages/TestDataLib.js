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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        //correct the overflow display when everything is loaded (workaround)
        $('.dataTable').DataTable().draw();
        //open navbar menu Data
        openNavbarMenu("navMenuData");

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {

    displayPageLabel();

    /**
     * Handles the click to save the test data lib entry
     */
    $("#saveTestDataLib").on("click", editTestDataLibModalSaveHandler);
    $("#addTestDataLibButton").click(addTestDataLibModalSaveHandler);
    $("#saveDuplicateTestDataLib").click(duplicateTestDataLibModalSaveHandler);

    /*
     * Specification of the methods that handle the bs.modal close.
     */
    $('#editTestDataLibModal').on('hidden.bs.modal', editTestDataLibModalCloseHandler);
    $('#addTestDataLibModal').on('hidden.bs.modal', addTestDataLibModalCloseHandler);
    $('#duplicateTestDataLibModal').on('hidden.bs.modal', duplicateTestDataLibModalCloseHandler);
    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);

    /*
     * Handles the change of the type when adding a new test data lib entry
     */
    $('#addTestDataLibModal #type').change(function () {
        console.debug("expand");
        if ($(this).val() === "SQL") {
            $("#panelSQL").collapse("show");
            $("#panelSERVICE").collapse("hide");
            $("#panelCSV").collapse("hide");
        } else if ($(this).val() === "SERVICE") {
            $("#panelSQL").collapse("hide");
            $("#panelSERVICE").collapse("show");
            $("#panelCSV").collapse("hide");
        } else if ($(this).val() === "CSV") {
            $("#panelSQL").collapse("hide");
            $("#panelSERVICE").collapse("hide");
            $("#panelCSV").collapse("show");
        } else {
            $("#panelSQL").collapse("hide");
            $("#panelSERVICE").collapse("hide");
            $("#panelCSV").collapse("hide");
        }
    });
    $('#editTestDataLibModal #type').change(function () {
        if ($(this).val() === "SQL") {
            $("#panelSQLEdit").collapse("show");
            $("#panelSERVICEEdit").collapse("hide");
            $("#panelCSVEdit").collapse("hide");
        } else if ($(this).val() === "SERVICE") {
            $("#panelSQLEdit").collapse("hide");
            $("#panelSERVICEEdit").collapse("show");
            $("#panelCSVEdit").collapse("hide");
        } else if ($(this).val() === "CSV") {
            $("#panelSQLEdit").collapse("hide");
            $("#panelSERVICEEdit").collapse("hide");
            $("#panelCSVEdit").collapse("show");
        } else {
            $("#panelSQLEdit").collapse("hide");
            $("#panelSERVICEEdit").collapse("hide");
            $("#panelCSVEdit").collapse("hide");
        }
    });
    $('#duplicateTestDataLibModal #type').change(function () {
        if ($(this).val() === "SQL") {
            $("#panelSQLDuplicate").collapse("show");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("hide");
        } else if ($(this).val() === "SERVICE") {
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("show");
            $("#panelCSVDuplicate").collapse("hide");
        } else if ($(this).val() === "CSV") {
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("show");
        } else {
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("hide");
        }
    });

    // Invariant Combo loading.
    displayInvariantList("system", "SYSTEM", false, "", "");
    displayInvariantList("environment", "ENVIRONMENT", false, "", "");
    displayInvariantList("country", "COUNTRY", false, "", "");
    displayInvariantList("database", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("databaseUrl", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("databaseCsv", "PROPERTYDATABASE", false, "", "");
    displayInvariantList("type", "TESTDATATYPE", false, "INTERNAL");

    $("select[id='service']").append($('<option></option>').text("").val(""));
    displayAppServiceList("service", "");

    $("#addTestDataLibModal #service").change(function () {
        activateSOAPServiceFields("#addTestDataLibModal", $(this).val());
    });
    $("#editTestDataLibModal #service").change(function () {
        activateSOAPServiceFields("#editTestDataLibModal", $(this).val());
    });
    $("#duplicateTestDataLibModal #service").change(function () {
        activateSOAPServiceFields("#duplicateTestDataLibModal", $(this).val());
    });


    // Click on add row button adds a Subdata entry.
    $("#addSubData").click(function () {
        addNewSubDataRow("subdataTableBody");
    });
    $("#addSubData_edit").click(function () {
        addNewSubDataRow("subdataTableBody_edit")
    });
    $("#dupSubData").click(function () {
        addNewSubDataRow("subdataTableBody_dup")
    });

    var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "contentTable", aoColumnsFuncTestDataLib("listOfTestDataLib"), [2, 'asc']);

    //creates the main table and draws the management buttons if the user has the permissions
    $.when(createDataTableWithPermissions(configurations, renderOptionsForTestDataLib, "#testdatalib", undefined, true)).then(function () {
        $("#listOfTestDataLib_wrapper div.ColVis .ColVis_MasterButton").addClass("btn btn-default");
    });

}

function activateSOAPServiceFields(modal, serviceValue) {
    if (serviceValue === "") {
        $(modal + " #servicepath").prop("readonly", false);
        $(modal + " #method").prop("readonly", false);
        var editor = ace.edit($(modal + " #envelope")[0]);
        editor.container.style.opacity = 1;
        editor.renderer.setStyle("disabled", false);
    } else {
        $(modal + " #servicepath").prop("readonly", true);
        $(modal + " #method").prop("readonly", true);
        var editor = ace.edit($(modal + " #envelope")[0]);
        editor.container.style.opacity = 0.5;
        editor.renderer.setStyle("disabled", true);
    }
}

/**
 * After table feeds, 
 * @returns {undefined}
 */
function afterTableLoad() {
    $.each($("pre[name='envelopeField']"), function (i, e) {
        //Highlight envelop on modal loading
        var editor = ace.edit($(e).get(0));
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/xml");
        editor.setOptions({
            maxLines: 1,
            showLineNumbers: false,
            showGutter: false,
            highlightActiveLine: false,
            highlightGutterLine: false,
            readOnly: true
        });
        editor.renderer.$cursorLayer.element.style.opacity = 0;
    });
    $.each($("pre[name='scriptField']"), function (i, e) {
        //Highlight envelop on modal loading
        var editor = ace.edit($(e).get(0));
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/sql");
        editor.setOptions({
            maxLines: 1,
            showLineNumbers: false,
            showGutter: false,
            highlightActiveLine: false,
            highlightGutterLine: false,
            readOnly: true
        });
        editor.renderer.$cursorLayer.element.style.opacity = 0;
    });

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);

    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));

    //set translations for all modals required by TestDataLib page

    //title 
    $("#editTestDataLibTitle").text(doc.getDocLabel("page_testdatalib_m_updatelib", "title"));
    $("#duplicateTestDataLibTitle").text(doc.getDocLabel("page_testdatalib_m_duplicatelib", "title"));
    $("#addTestDataLibModalLabel").text(doc.getDocLabel("page_testdatalib_m_createlib", "title"));
    $("#testCaseListModalLabel").text(doc.getDocLabel("page_testdatalib_m_gettestcases", "title"));
    // TestDataLib content
    $("[name='lbl_name']").html(doc.getDocOnline("testdatalib", "name"));
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
    // Sub Data content
    $("[name='subdataHeader']").html(doc.getDocOnline("testdatalibdata", "subData"));
    $("[name='valueHeader']").html(doc.getDocOnline("testdatalibdata", "value"));
    $("[name='columnHeader']").html(doc.getDocOnline("testdatalibdata", "column"));
    $("[name='parsingAnswerHeader']").html(doc.getDocOnline("testdatalibdata", "parsingAnswer"));
    $("[name='columnPositionHeader']").html(doc.getDocOnline("testdatalibdata", "columnPosition"));
    $("[name='descriptionHeader']").html(doc.getDocOnline("testdatalibdata", "description"));
    // Tracability
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
    $("#saveTestDataLib").text(doc.getDocLabel("page_global", "buttonAdd"));
    //buttons    
    $("#cancelDuplicateTestDataLib").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#saveDuplicateTestDataLib").text(doc.getDocLabel("page_global", "btn_duplicate"));
    //cancel + add buttons
    $("#addTestDataLibButton").text(doc.getDocLabel("page_global", "btn_add"));
    $("#cancelTestDataLibButton").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#closeButton").text(doc.getDocLabel("page_global", "buttonClose"));
    //tabs, tab2 is updated when the entries are managed
    $("[name='tab1Text']").text(doc.getDocLabel("page_testdatalib", "m_tab1_text"));
    $("[name='tab2Text']").text(doc.getDocLabel("page_testdatalib", "m_tab2_text"));
    $("[name='tab3Text']").text(doc.getDocLabel("page_testdatalib", "m_tab3_text"));

    displayFooter(doc);
}

function renderOptionsForTestDataLib(data) {
    //check if user has permissions to perform the add and import operations
    var doc = new Doc();

    if (data["hasPermissions"]) {
        if ($("#createLibButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createLibButton' type='bytton' class='btn btn-default'><span class='glyphicon glyphicon-plus-sign'></span> ";
            contentToAdd += doc.getDocLabel("page_testdatalib", "btn_create"); //translation for the create button;
            contentToAdd += "</button></div>";

            $("#listOfTestDataLib_wrapper #listOfTestDataLib_length").before(contentToAdd);
            $('#createLibButton').click(addTestDataLibClick);

        }
    } else {
        $("#testdatalibFirstColumnHeader").html(doc.getDocLabel("page_global", "columnAction"));
    }
}

function deleteTestDataLibHandlerClick() {
    var testDataLibID = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteTestDataLib", {testdatalibid: testDataLibID}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#listOfTestDataLib").dataTable();
            oTable.fnDraw(true);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteTestDataLibClick(testDataLibID, name, system, environment, country, type) {
    var doc = new Doc();

    var systemLabel = system === '' ? doc.getDocLabel("page_global", "lbl_all") : system;
    var environmentLabel = environment === '' ? doc.getDocLabel("page_global", "lbl_all") : environment;
    var countryLabel = country === '' ? doc.getDocLabel("page_global", "lbl_all") : country;

    var messageComplete = doc.getDocLabel("page_testdatalib", "message_delete").replace("%ENTRY%", name).replace("%ID%", testDataLibID).replace("%SYSTEM%", systemLabel)
            .replace("%ENVIRONMENT%", environmentLabel).replace("%COUNTRY%", countryLabel);
    showModalConfirmation(deleteTestDataLibHandlerClick, undefined, doc.getDocLabel("page_testdatalib_delete", "title"), messageComplete, testDataLibID, "", "", "");

}

function addTestDataLibModalCloseHandler() {
    var doc = getDoc();
    var docMultiSelect = doc.multiselect;

    $('#addTestDataLibModal #addTestDataLibModalForm')[0].reset();
    $('#addSubDataTableBody tr[class="trData"]').remove(); // removes all rows except the first one


    //clears all filters
    $(this).find("li[class='multiselect-item filter'] button").trigger("click");

    //removes the active styles
    $(this).find("li.multiselect-item").children().removeClass("active");
    $(this).find("li").removeClass("active");

    $(this).find("li input[type='checkbox']").prop("checked", false);

    //changes the title labels
    var nonSelectedText = docMultiSelect.none_selected.docLabel;
    $(this).find("button[class='multiselect dropdown-toggle btn btn-default']").prop("title", nonSelectedText);
    $(this).find("span[class='multiselect-selected-text']").text(nonSelectedText);


    //selects the first option for group
    $(this).find('#group option:first').prop("selected", "selected");
    //selects the first option for type
    $(this).find('#type option:first').prop("selected", "selected");
    $(this).find('div.has-error').removeClass("has-error");
    //clears the response messages
    clearResponseMessage($('#addTestDataLibModal'));
    $(this).find("#tab1Text").tab('show'); //shows the first tab as the default
}

function addTestDataLibModalSaveHandler() {
    //shows the modal that allows the creation of test data lib 
    var formAdd = $("#addTestDataLibModal #addTestDataLibModalForm");

    //START client-side validation
    //validates if the property name is not empty
    var nameElement = formAdd.find("#name");
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

    showLoaderInModal('#addTestDataLibModal');

    // Getting Data from Database TAB
    var table1 = $("#subdataTableBody tr");
    var table_subdata = [];
    for (var i = 0; i < table1.length; i++) {
        table_subdata.push($(table1[i]).data("subdata"));
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formAdd.serialize());
    //Add envelope and script, not in the form
    var editorEnv = ace.edit($("#addTestDataLibModalForm #envelope")[0]);
    data.envelope = encodeURIComponent(editorEnv.getSession().getDocument().getValue());
    var editorScr = ace.edit($("#addTestDataLibModalForm #script")[0]);
    data.script = encodeURIComponent(editorScr.getSession().getDocument().getValue());

    $.ajax({
        url: "CreateTestDataLib",
        async: true,
        method: "POST",
        data: {system: data.system,
            country: data.country,
            environment: data.environment,
            libdescription: data.libdescription,
            csvUrl: data.csvUrl,
            database: data.database,
            databaseCsv: data.databaseCsv,
            databaseUrl: data.databaseUrl,
            envelope: data.envelope,
            group: data.group,
            method: data.method,
            name: data.name,
            script: data.script,
            separator: data.separator,
            servicepath: data.servicepath,
            service: data.service,
            testdatalibid: data.testdatalibid,
            type: data.type,
            subDataList: JSON.stringify(table_subdata)},
        success: function (data) {
            hideLoaderInModal('#addTestDataLibModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#listOfTestDataLib").dataTable();
                oTable.fnDraw(true);
                $('#addTestDataLibModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#addTestDataLibModal'));
            }
        },
        error: showUnexpectedError
    });
}

function addTestDataLibClick() {
    console.debug("addTest");
    clearResponseMessageMainPage();

    var doc = new Doc();

    //when creating the testdatalibrary entry the INTERNAL is the default select
    $('#addTestDataLibModal #type option[value="INTERNAL"]').attr("selected", "selected");

// Cleaning the Subdata Table.
    $('#subdataTableBody tr').remove();
// Adding the key subdata line.
    var newSubData = {
        subData: "",
        value: "",
        column: "",
        parsingAnswer: "",
        columnPosition: "",
        description: "",
        testDataLibDataID: -1,
        toDelete: false
    };
    appendSubDataRow(newSubData, "subdataTableBody");

//Destroy the previous Ace object.
    //ace.edit($("#addTestDataLibModal #envelope")[0]).destroy();
    //ace.edit($("#addTestDataLibModal #script")[0]).destroy();

//Highlight envelop on modal loading
    var editor = ace.edit($("#addTestDataLibModal #envelope")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/xml");
    editor.setOptions({
        maxLines: Infinity
    });

//Highlight envelop on modal loading
    var editor = ace.edit($("#addTestDataLibModal #script")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/sql");
    editor.setOptions({
        maxLines: Infinity
    });

    activateSOAPServiceFields("#addTestDataLibModal", "");

    $('#addTestDataLibModal').modal('show');
}

function duplicateTestDataLibModalCloseHandler() {
    $('#duplicateTestDataLibModal #duplicateTestLibData')[0].reset();
    clearResponseMessage($('#duplicateTestDataLibModal'));
}

function duplicateTestDataLibModalSaveHandler() {
    var formAdd = $('#duplicateTestDataLibModal').find('form#duplicateTestLibData');
    showLoaderInModal('#duplicateTestDataLibModal');

    //START client-side validation
    //validates if the property name is not empty
    var nameElement = formAdd.find("#name");
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
    var noRepeated = validateSubDataEntriesRepeated($("#duplicateTestDataLibModal"), "duplicateSubDataTableBody", false);
    if (nameElementEmpty || !noRepeated) {
        return;
    }
    //END client-side validation

    showLoaderInModal('#duplicateTestDataLibModal');

    // Getting Data from Database TAB
    var table1 = $("#subdataTableBody_dup tr");
    var table_subdata = [];
    for (var i = 0; i < table1.length; i++) {
        table_subdata.push($(table1[i]).data("subdata"));
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formAdd.serialize());
    //Add envelope and script, not in the form
    var editorEnv = ace.edit($("#duplicateTestDataLibModal #envelope")[0]);
    data.envelope = encodeURIComponent(editorEnv.getSession().getDocument().getValue());
    var editorScr = ace.edit($("#duplicateTestDataLibModal #script")[0]);
    data.script = encodeURIComponent(editorScr.getSession().getDocument().getValue());

    $.ajax({
        url: "CreateTestDataLib",
        async: true,
        method: "POST",
        data: {system: data.system,
            country: data.country,
            environment: data.environment,
            libdescription: data.libdescription,
            csvUrl: data.csvUrl,
            database: data.database,
            databaseCsv: data.databaseCsv,
            databaseUrl: data.databaseUrl,
            envelope: data.envelope,
            group: data.group,
            method: data.method,
            name: data.name,
            script: data.script,
            separator: data.separator,
            servicepath: data.servicepath,
            service: data.service,
            testdatalibid: data.testdatalibid,
            type: data.type,
            subDataList: JSON.stringify(table_subdata)},
        success: function (data) {
            hideLoaderInModal('#duplicateTestDataLibModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#listOfTestDataLib").dataTable();
                oTable.fnDraw(true);
                $('#duplicateTestDataLibModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#duplicateTestDataLibModal'));
            }
        },
        error: showUnexpectedError
    });
}

function duplicateTestDataLibClick(testDataLibID) {

    clearResponseMessageMainPage();
    //load the data from the row 
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID);

    $.when(jqxhr).then(function (data) {

        //Destroy the previous Ace object.
        ace.edit($("#duplicateTestDataLibModal #envelope")[0]).destroy();
        ace.edit($("#duplicateTestDataLibModal #script")[0]).destroy();

        var obj = data["testDataLib"];

        $('#duplicateTestDataLibModal #testdatalibid').prop("value", testDataLibID);
        $('#duplicateTestDataLibModal #name').prop("value", obj.name);

        $('#duplicateTestDataLibModal #type option[value="' + obj.type + '"]').attr("selected", "selected");
        $('#duplicateTestDataLibModal #system').find('option[value="' + obj.system + '"]').prop("selected", true);
        $('#duplicateTestDataLibModal #environment').find('option[value="' + obj.environment + '"]').prop("selected", true);
        $('#duplicateTestDataLibModal #country').find('option[value="' + obj.country + '"]').prop("selected", true);

        //loads the information for entries
        $('#duplicateTestDataLibModal #databaseUrl').find('option[value="' + obj.databaseUrl + '"]:first').prop("selected", "selected");
        $('#duplicateTestDataLibModal #service').find('option[value="' + obj.service + '"]:first').prop("selected", "selected");
        $('#duplicateTestDataLibModal #servicepath').prop("value", obj.servicePath);
        $('#duplicateTestDataLibModal #method').prop("value", obj.method);
        $('#duplicateTestDataLibModal #envelope').text(obj.envelope);
        activateSOAPServiceFields("#duplicateTestDataLibModal", obj.service);
        $('#duplicateTestDataLibModal #databaseCsv').find('option[value="' + obj.databaseCsv + '"]:first').prop("selected", "selected");
        $('#duplicateTestDataLibModal #csvUrl').prop("value", obj.csvUrl);
        $('#duplicateTestDataLibModal #separator').prop("value", obj.separator);
        $('#duplicateTestDataLibModal #database').find('option[value="' + obj.database + '"]:first').prop("selected", "selected");
        $('#duplicateTestDataLibModal #script').text(obj.script);

        //load TYPE
        $('#duplicateTestDataLibModal #libdescription').prop("value", obj.description);
        $('#duplicateTestDataLibModal #group').prop("value", obj.group);

        if (obj.type === "SQL") {
            $("#panelSQLDuplicate").collapse("show");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("hide");
        } else if (obj.type === "SERVICE") {
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("show");
            $("#panelCSVDuplicate").collapse("hide");
        } else if (obj.type === "CSV") {
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("show");
        } else {
            //hide all if the type is internal
            $("#panelSQLDuplicate").collapse("hide");
            $("#panelSERVICEDuplicate").collapse("hide");
            $("#panelCSVDuplicate").collapse("hide");
        }

        //loads groups from database
        var jqxhrGroups = $.getJSON("ReadTestDataLib", "groups");
        $.when(jqxhrGroups).then(function (groupsData) {
            //load distinct groups
            var doc = new Doc();
            loadSelectElement(groupsData["contentTable"], $('#duplicateTestDataLibModal #groupduplicate'), true,
                    doc.getDocLabel("page_testdatalib_m_createlib", "lbl_dropdown_help"));
            //selects the group entered by the user

            $('#duplicateTestDataLibModal #groupduplicate').find('option[value="' + obj.group + '"]:first').prop("selected", "selected");
            $('#duplicateTestDataLibModal #groupduplicate').find('option:first').addClass("emptySelectOption");
            $('#duplicateTestDataLibModal #groupduplicate').change();
        });

        loadTestDataLibSubdataTable(testDataLibID, "subdataTableBody_dup");



//Highlight envelop on modal loading
        var editor = ace.edit($("#duplicateTestDataLibModal #envelope")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/xml");
        editor.setOptions({
            maxLines: Infinity
        });

//Highlight envelop on modal loading
        var editor = ace.edit($("#duplicateTestDataLibModal #script")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/sql");
        editor.setOptions({
            maxLines: Infinity
        });

        //after everything. then shows the modal
        $("#duplicateTestDataLibModal").modal("show");

    }).fail(handleErrorAjaxAfterTimeout);

}

function editTestDataLibModalCloseHandler() {
    $('#editTestDataLibModal #editTestLibData')[0].reset();
    clearResponseMessage($('#editTestDataLibModal'));
}

function editTestDataLibModalSaveHandler() {
    var formEdit = $('#editTestDataLibModal').find('form#editTestLibData');
    showLoaderInModal('#editTestDataLibModal');

    // Getting Data from Database TAB
    var table1 = $("#subdataTableBody_edit tr");
    var table_subdata = [];
    for (var i = 0; i < table1.length; i++) {
        table_subdata.push($(table1[i]).data("subdata"));
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    //Add envelope and script, not in the form
    var editorEnv = ace.edit($("form#editTestLibData #envelope")[0]);
    data.envelope = encodeURIComponent(editorEnv.getSession().getDocument().getValue());
    var editorScr = ace.edit($("form#editTestLibData #script")[0]);
    data.script = encodeURIComponent(editorScr.getSession().getDocument().getValue());

    $.ajax({
        url: "UpdateTestDataLib",
        async: true,
        method: "POST",
        data: {system: data.system,
            country: data.country,
            environment: data.environment,
            libdescription: data.libdescription,
            csvUrl: data.csvUrl,
            database: data.database,
            databaseCsv: data.databaseCsv,
            databaseUrl: data.databaseUrl,
            envelope: data.envelope,
            group: data.group,
            method: data.method,
            name: data.name,
            script: data.script,
            separator: data.separator,
            servicepath: data.servicepath,
            service: data.service,
            testdatalibid: data.testdatalibid,
            type: data.type,
            subDataList: JSON.stringify(table_subdata)},
        success: function (data) {
            hideLoaderInModal('#editTestDataLibModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#listOfTestDataLib").dataTable();
                oTable.fnDraw(true);
                $('#editTestDataLibModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editTestDataLibModal'));
            }
        },
        error: showUnexpectedError
    });

}

function editTestDataLibClick(testDataLibID) {

    clearResponseMessageMainPage();
    //load the data from the row 
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID);

    $.when(jqxhr).then(function (data) {

        //Destroy the previous Ace object.
        ace.edit($("#editTestDataLibModal #envelope")[0]).destroy();
        ace.edit($("#editTestDataLibModal #script")[0]).destroy();

        var obj = data["testDataLib"];

        $('#editTestDataLibModal #testdatalibid').prop("value", testDataLibID);
        $('#editTestDataLibModal #name').prop("value", obj.name);

//        $('#editTestDataLibModal #type option[value="' + obj.type + '"]').attr("selected", "selected");
        $('#editTestDataLibModal #type').prop("value", obj.type);
        $('#editTestDataLibModal #system').find('option[value="' + obj.system + '"]').prop("selected", true);
        $('#editTestDataLibModal #environment').find('option[value="' + obj.environment + '"]').prop("selected", true);
        $('#editTestDataLibModal #country').find('option[value="' + obj.country + '"]').prop("selected", true);

        //loads the information for the entries
        $('#editTestDataLibModal #databaseUrl').find('option[value="' + obj.databaseUrl + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #service').find('option[value="' + obj.service + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #servicepath').prop("value", obj.servicePath);
        $('#editTestDataLibModal #method').prop("value", obj.method);
        $('#editTestDataLibModal #envelope').text(obj.envelope);
        activateSOAPServiceFields("#editTestDataLibModal", obj.service);
        $('#editTestDataLibModal #databaseCsv').find('option[value="' + obj.databaseCsv + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #csvUrl').prop("value", obj.csvUrl);
        $('#editTestDataLibModal #separator').prop("value", obj.separator);
        $('#editTestDataLibModal #database').find('option[value="' + obj.database + '"]:first').prop("selected", "selected");
        $('#editTestDataLibModal #script').text(obj.script);

        $('#editTestDataLibModal #libdescription').prop("value", obj.description);
        $('#editTestDataLibModal #group').prop("value", obj.group);

        $('#editTestDataLibModal #created').prop("value", obj.created);
        $('#editTestDataLibModal #creator').prop("value", obj.creator);
        $('#editTestDataLibModal #lastModified').prop("value", obj.lastModified);
        $('#editTestDataLibModal #lastModifier').prop("value", obj.lastModifier);

        if (obj.type === "SQL") {
            $("#panelSQLEdit").collapse("show");
            $("#panelSERVICEEdit").collapse("hide");
            $("#panelCSVEdit").collapse("hide");
        } else if (obj.type === "SERVICE") {
            $("#panelSERVICEEdit").collapse("show");
            $("#panelSQLEdit").collapse("hide");
            $("#panelCSVEdit").collapse("hide");
        } else if (obj.type === "CSV") {
            $("#panelCSVEdit").collapse("show");
            $("#panelSQLEdit").collapse("hide");
            $("#panelSERVICEEdit").collapse("hide");
        } else {
            //hide all if the type is static
            $("#panelSQLEdit").collapse("hide");
            $("#panelSERVICEEdit").collapse("hide");
            $("#panelCSVEdit").collapse("hide");
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

        // Permission management.
        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            $('#saveTestDataLib').attr('class', '');
            $('#saveTestDataLib').attr('hidden', 'hidden');
        }

        // Loading the list of subdata.
        loadTestDataLibSubdataTable(testDataLibID, "subdataTableBody_edit");

//Highlight envelop on modal loading
        var editor = ace.edit($("#editTestDataLibModal #envelope")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/xml");
        editor.setOptions({
            maxLines: Infinity
        });

//Highlight envelop on modal loading
        var editor = ace.edit($("#editTestDataLibModal #script")[0]);
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/sql");
        editor.setOptions({
            maxLines: Infinity
        });

        //after everything. then shows the modal
        $('#editTestDataLibModal').modal('show');

    }).fail(handleErrorAjaxAfterTimeout);


}

// Function to load the various SubData lists.
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

// Function to append 1 line of Subdata in the various SubData lists.
function appendSubDataRow(subdata, targetTableBody) {
    var doc = new Doc();
    var isKey = false;
    if (subdata.subData === "") {
        isKey = true;
    }
    if (isKey) {
        var deleteBtn = $("<button type=\"button\" disabled=\"disabled\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
        var subDataInput = $("<input  maxlength=\"200\" disabled=\"disabled\">").addClass("form-control input-sm").val(subdata.subData);
    } else {
        var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
        var subDataInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "subData") + " --\">").addClass("form-control input-sm").val(subdata.subData);
    }
    var valueInput = $("<input placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "value") + " --\">").addClass("form-control input-sm").val(subdata.value);
    var columnInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "column") + " --\">").addClass("form-control input-sm").val(subdata.column);
    var parsingAnswerInput = $("<input placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "parsingAnswer") + " --\">").addClass("form-control input-sm").val(subdata.parsingAnswer);
    var columnPositionInput = $("<input  maxlength=\"45\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "columnPosition") + " --\">").addClass("form-control input-sm").val(subdata.columnPosition);
    var descriptionInput = $("<input  maxlength=\"1000\" placeholder=\"-- " + doc.getDocLabel("testdatalibdata", "description") + " --\">").addClass("form-control input-sm").val(subdata.description);
    var table = $("#" + targetTableBody);


    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var subData = $("<td></td>").append(subDataInput);
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
    row.append(value);
    row.append(column);
    row.append(parsingAnswer);
    row.append(columnPosition);
    row.append(description);
    row.data("subdata", subdata);
    table.append(row);
}

// Function to append 1 new line of Subdata in the various SubData lists.
function addNewSubDataRow(dataTableBody) {
    var nbRows = $("#" + dataTableBody + " tr").size();
    var newSubData = {
        subData: "SUBDATA" + nbRows,
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
        oTable.fnDraw(true);
        $('#modalUpload').modal('hide');
        showMessageMainPage(getAlertType(data.messageType), data.message, false);
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
 * Function that loads all test cases that are associated with the selected entry 
 * @param {type} testDataLibID testdatalib id
 * @param {type} name entry name
 * @param {type} country where the entry is available
 */
function getTestCasesUsing(testDataLibID, name, country) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID + "&name=" + name + "&country=" + country);

    var doc = new Doc();

    $.when(jqxhr).then(function (result) {

        $('#testCaseListModal #totalTestCases').text(doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTests") + " " + result["TestCasesList"].length);
        var htmlContent = "";

        $.each(result["TestCasesList"], function (idx, obj) {

            var item = '<b><a class="list-group-item ListItem" data-remote="true" href="#sub_cat' + idx + '" id="cat' + idx + '" data-toggle="collapse" \n\
            data-parent="#sub_cat' + idx + '"><span class="pull-left">' + obj[0] + '</span>\n\
                                        <span style="margin-left: 25px;" class="pull-right">' + doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTestCases") + obj[2] + '</span>\n\
                                        <span class="menu-ico-collapse"><i class="fa fa-chevron-down"></i></span>\n\
                                    </a></b>';
            htmlContent += item;
            htmlContent += '<div class="collapse list-group-submenu" id="sub_cat' + idx + '">';


            $.each(obj[3], function (idx2, obj2) {
                var hrefTest = 'TestCaseScript.jsp?test=' + obj[0] + '&testcase=' + obj2.TestCaseNumber;
                htmlContent += '<span class="list-group-item sub-item ListItem" data-parent="#sub_cat' + idx + '" style="padding-left: 78px;height: 50px;">';
                htmlContent += '<span class="pull-left"><a href="' + hrefTest + '" target="_blank">' + obj2.TestCaseNumber + '- ' + obj2.TestCaseDescription + '</a></span>';
                htmlContent += '<span class="pull-right">' + doc.getDocLabel("page_testdatalib_m_gettestcases", "nrProperties") + " " + obj2.NrProperties + '</span><br/>';
                htmlContent += '<span class="pull-left"> ' + doc.getDocLabel("testcase", "Creator") + ": " + obj2.Creator + ' | '
                        + doc.getDocLabel("testcase", "TcActive") + ": " + obj2.Active + ' | ' + doc.getDocLabel("testcase", "Status") + ": " + obj2.Status + ' | ' +
                        doc.getDocLabel("invariant", "GROUP") + ": " + obj2.Group + ' | ' + doc.getDocLabel("application", "Application") + ": " + obj2.Application + '</span>';
                htmlContent += '</span>';
            });

            htmlContent += '</div>';

        });
        if (htmlContent !== '') {
            $('#testCaseListModal #testCaseListGroup').append(htmlContent);
        }
        hideLoaderInModal('#testCaseListModal');
        $('#testCaseListModal').modal('show');

    }).fail(handleErrorAjaxAfterTimeout);

}

/**
 * Handler that cleans the test case list modal when it is closed
 */
function getTestCasesUsingModalCloseHandler() {
    //we need to clear the item-groups that were inserted
    $('#testCaseListModal #testCaseListGroup a[id*="cat"]').remove();
    $('#testCaseListModal #testCaseListGroup div[id*="sub_cat"]').remove();
}

function aoColumnsFuncTestDataLib(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {
            "sName": "tdl.TestDataLibID",
            "data": "testDataLibID",
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "150px",
            "title": doc.getDocLabel("testdatalib", "actions"),
            "mRender": function (data, type, oObj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var editElement = '<button id="editTestDataLib' + data + '"  onclick="editTestDataLibClick(' + data + ');" \n\
                                class="editTestDataLib btn btn-default btn-xs margin-right5" \n\
                            name="editTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewElement = '<button id="editTestDataLib' + data + '"  onclick="editTestDataLibClick(' + data + ');" \n\
                                class="editTestDataLib btn btn-default btn-xs margin-right25" \n\
                            name="editTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteElement = '<button onclick="deleteTestDataLibClick(' + oObj.testDataLibID + ',\'' + oObj.name
                        + '\', ' + '\'' + oObj.system + '\', ' + '\'' + oObj.environment + '\', ' + '\'' + oObj.country + '\', '
                        + '\'' + oObj.type + '\');" class="btn btn-default btn-xs margin-right25 " \n\
                            name="deleteTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_delete") + '" type="button">\n\
                            <span class="glyphicon glyphicon-trash"></span></button>';
                var duplicateEntryElement = '<button class="btn btn-default btn-xs margin-right5" \n\
                            name="duplicateTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry") + '"\n\
                                 type="button" onclick="duplicateTestDataLibClick(' + data + ')">\n\
                                <span class="glyphicon glyphicon-duplicate"></span></button>'; //TODO check if we can add this glyphicon glyphicon-duplicate
                var viewTestCase = '<button class="getTestCasesUsing btn  btn-default btn-xs margin-right5" \n\
                            name="getTestCasesUsing" title="' + doc.getDocLabel("page_testdatalib", "tooltip_gettestcases") + '" type="button" \n\
                            onclick="getTestCasesUsing(' + data + ', \'' + oObj.name + '\', \'' + oObj.country + '\')"><span class="glyphicon glyphicon-list"></span></button>';

                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width250">' + editElement + duplicateEntryElement + deleteElement + viewTestCase + '</div>';
                } else {
                    return '<div class="center btn-group width250">' + viewElement + viewTestCase + '</div>';
                }
            }
        },
        {
            "sName": "tdl.TestDataLibID",
            "data": "testDataLibID",
            "sWidth": "50px",
            "title": doc.getDocOnline("testdatalib", "testdatalibid")
        },
        {
            "sName": "tdl.Name",
            "data": "name",
            "sWidth": "200px",
            "title": doc.getDocOnline("testdatalib", "name")
        },
        {
            "sName": "tdl.System",
            "data": "system",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "system")
        },
        {
            "sName": "tdl.Environment",
            "data": "environment",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "environment")
        },
        {
            "sName": "tdl.Country",
            "data": "country",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "country")
        },
        {
            "sName": "tdl.Group",
            "data": "group",
            "sWidth": "100px",
            "title": doc.getDocOnline("testdatalib", "group")
        },
        {
            "sName": "tdl.Description",
            "data": "description",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "description")
        },
        {
            "sName": "tdl.Type",
            "data": "type",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "type")
        },
        {
            "sName": "tdd.value",
            "data": "subDataValue",
            "sWidth": "150px",
            "bSortable": false,
            "title": doc.getDocOnline("testdatalibdata", "value")
        },
        {
            "sName": "tdl.Database",
            "data": "database",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "database")
        },
        {"data": "script", "sName": "tdl.Script", "sWidth": "450px", "title": doc.getDocLabel("testdatalib", "script"),
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre name='scriptField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['script'])).html();
            }},
        {
            "sName": "tdl.DatabaseUrl",
            "data": "databaseUrl",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "databaseUrl")
        },
        {
            "sName": "tdl.Service",
            "data": "service",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "service")
        },
        {
            "sName": "tdl.ServicePath",
            "data": "servicePath",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "servicepath")
        },
        {
            "sName": "tdl.method",
            "data": "method",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "method")
        },
        {
            "data": "envelope", "sName": "tdl.envelope", "title": doc.getDocLabel("testdatalib", "envelope"), "sWidth": "350px",
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre name='envelopeField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['envelope'])).html();
            }
        },
        {
            "sName": "tdl.DatabaseCsv",
            "data": "databaseCsv",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "databaseCsv")
        },
        {
            "sName": "tdl.csvUrl",
            "data": "csvUrl",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "csvUrl")
        },
        {
            "sName": "tdl.separator",
            "data": "separator",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "separator")
        },
        {
            "sName": "tdl.Created",
            "data": "created",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "created")
        },
        {
            "sName": "tdl.Creator",
            "data": "creator",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "creator")
        },
        {
            "sName": "tdl.LastModified",
            "data": "lastModified",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "lastmodified")
        },
        {
            "sName": "tdl.LastModifier",
            "data": "lastModifier",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "lastmodifier")
        },
        {
            "sName": "tdd.column",
            "data": "subDataColumn",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "column")
        },
        {
            "sName": "tdd.ParsingAnswer",
            "data": "subDataParsingAnswer",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "parsingAnswer")
        },
        {
            "sName": "tdd.ColumnPosition",
            "data": "subDataColumnPosition",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "columnPosition")
        }
    ];

    return aoColumns;

}

