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

$.when($.getScript("js/pages/global/global.js")).then(function() {
    /**
     * Document ready methods
     */

    displayPageLabel();

    $(function() {


        var i = 0;
        var j = 0;
        /*****************************************************************************/
        //adds new rows to the subdata table
        $("#newSubData_addRow").click(function() {

            $('#addSubDataTableBody').append('<tr class="trData" id="row' + (i + 1) + '">\n\\n\
                <td ><div class="nomarginbottom marginTop5"> <button onclick="deleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"  maxlength="200"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="value" type="text" class="dataClass form-control input-xs" maxlength="1000"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="column" value="" type="text" class="dataClass form-control input-xs" maxlength="1000"  /></div></td>\n\\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="parsinganswer" value="" type="text" class="dataClass form-control input-xs" maxlength="1000"  /></div></td>\n\\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs" maxlength="1000"  /></div></td>\n\
                \n\
            </tr>');
            i++;

            $("#addTestDataLibModal #addSubDataTableBody tr td:nth-child(2) input:last").change(subdataNameOnChangeHandler);
            updateSubDataTabLabel();
        });
        /*****************************************************************************/
        //adds a new run in the edit window
        $("#editSubData_addRow").click(function() {
            //gets the id from the first row
            var testdatalibid = $("#editSubDataTableBody tr[data-operation='update']:first").attr("testdatalibid");

            $('#editSubDataTableBody').append('<tr class="trData" id="row' + (j + 1) + '" testdatalibid="' + testdatalibid + '" data-operation="insert" >\n\\n\
                <td ><div class="nomarginbottom marginTop5"> <button onclick="editDeleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"   /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="value" type="text" class="dataClass form-control input-xs"  /></div></td>\n\\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="column" type="text" class="dataClass form-control input-xs"  /></div></td>\n\\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="parsinganswer" type="text" class="dataClass form-control input-xs"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs"  /></div></td>\n\
                \n\
            </tr>');
            $("#manageTestDataLibDataModal #editSubDataTableBody tr td:nth-child(2) input:last").change(subdataNameOnChangeHandler);
            j++;
        });

        /*****************************************************************************/
        //delete all subdata rows     
        $("#newSubData_deleteAll").click(function() {
            removeAllEntries("addSubDataTable");
            updateSubDataTabLabel();
        });


        /*****************************************************************************/
        /**
         * Handles the click to save the test data lib entry
         */
        $("#saveTestDataLib").on("click", saveTestDataLibClickHandler);
        /*****************************************************************************/
        /**
         * Disables the group text box when the users selects an existing group
         */
        $("#group").change(groupChangeHandler);
        /**
         * Disables the group text box when the users selects an existing group
         */
        $("#groupedit").change(groupChangeHandler);

        /*****************************************************************************/
        /*
         * Handles the change of the type when adding a new test data lib entry
         */
        $('#addTestDataLibModal #type').change(function() {
            if ($(this).val() === "SQL") {
                $("#panelSQL").collapse("show");
            } else if ($(this).val() === "SOAP") {
                $("#panelSOAP").collapse("show");
            }
        });

        /*
         * Handles the change of the type select  when editing a test data lib entry
         */
        $('#editTestDataLibModal #type').change(function() {
            if ($(this).val() === "SQL") {
                $("#panelSQLEdit").collapse("show");
            } else if ($(this).val() === "SOAP") {
                $("#panelSOAPEdit").collapse("show");
            }
        });

        /**
         * Method that saves new test data lib entry
         */
        $("#addTestDataLibButton").click(saveNewTestDataLibHandler);


        /**
         * Save changes performed in the subdata list
         */
        $("#saveChangesSubData").click(saveChangesSubDataClickHandler);

        /*
         * Specification of the methods that handle the bs.modal close.
         */
        $('#testCaseListModal').on('hidden.bs.modal', testCaseListModalCloseHandler);
        $('#manageTestDataLibDataModal').on('hidden.bs.modal', editTestDataLibDataModalCloseHandler);
        $('#editTestDataLibModal').on('hidden.bs.modal', editTestDataLibModalCloseHandler);
        $('#addTestDataLibModal').on('hidden.bs.modal', addTestDataLibModalCloseHandler);
        $('#testCaseListModal').on('hidden.bs.modal', testCaseListModalCloseHandler);

        $("#addTestDataLibModal #addSubDataTableBody tr td:nth-child(2) input").change(subdataNameOnChangeHandler);

        var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "contentTable", aoColumnsFuncTestDataLib("listOfTestDataLib"));


        //creates the main table and draws the management buttons if the user has the permissions
        $.when(createDataTableWithPermissions(configurations, renderOptionsForTestDataManager)).then(function() {
            $("#listOfTestDataLib_wrapper div.ColVis .ColVis_MasterButton").addClass("btn btn-default");
        });
    })
});

/**
 * Method that translates the content of the pages with base on the user language.
 */
function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);

    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));
//    
//    //set translations for create testdatalib modal
    displayCreateTestDataLibLabels(doc);
    displayUpdateTestDataLibLabels(doc);
    displayManageTestDataLibDataLabels(doc);
    displayListTestCasesLabels(doc);
    displayListTestDataLibDataLabels(doc);
    displayFooter(doc);
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayUpdateTestDataLibLabels(doc) {

    //title 
    $("#editTestDataLibTitle").text(doc.getDocLabel("page_testdatalib_m_updatelib", "title"));
    //content
    
    $("#lbl_id_edit").html(doc.getDocOnline("testdatalib", "testdatalibid")); //id
    $("#lbl_name_edit").html(doc.getDocOnline("testdatalib", "name")); //name
    
    $("#lbl_type_edit").html(doc.getDocOnline("testdatalib", "type"));
    $("#lbl_system_edit").html(doc.getDocOnline("testdatalib", "system"));
    $("#lbl_environment_edit").html(doc.getDocOnline("testdatalib", "environment"));
    $("#lbl_country_edit").html(doc.getDocOnline("testdatalib", "country"));
    //panels
    //soap and sql specific configurations
    $("#sqlConfigurationsLbl_edit").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "title_sql_configurations"));
    $("#soapConfigurationsLbl_edit").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "title_soap_configurations"));

    $("#lbl_description_edit").html(doc.getDocOnline("testdatalib", "description"));
    $("#lbl_database_edit").html(doc.getDocOnline("testdatalib", "database"));
    $("#lbl_script_edit").html(doc.getDocOnline("testdatalib", "script"));
    $("#lbl_service_path_edit").html(doc.getDocOnline("testdatalib", "servicepath"));
    $("#lbl_method_edit").html(doc.getDocOnline("testdatalib", "method"));
    $("#lbl_envelope_edit").html(doc.getDocOnline("testdatalib", "envelope"));
    //buttons    
    $("#cancelTestDataLib").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#saveTestDataLib").text(doc.getDocLabel("page_global", "buttonAdd"));

    //auxiliar for group edition
    $("#lbl_choose_group_edit").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "lbl_choose_group"));
    $("#lbl_enter_group_edit").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "lbl_enter_group"));
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayManageTestDataLibDataLabels(doc) {
    $("#manageTestDataLibDataModalLabel").html(doc.getDocLabel("page_testdatalib_m_managetestdatalibdata", "title"));
    $("#subdataActionsHeader").html(doc.getDocOnline("page_testdatalib_m_managetestdatalibdata", "actions"));
    $("#subdataHeaderManage").html(doc.getDocOnline("testdatalibdata", "subData"));
    $("#valueHeaderManage").html(doc.getDocOnline("testdatalibdata", "value"));
    $("#columnHeaderManage").html(doc.getDocOnline("testdatalibdata", "column"));
    $("#parsingAnswerHeaderManage").html(doc.getDocOnline("testdatalibdata", "parsingAnswer"));
    $("#descriptionHeaderManage").html(doc.getDocOnline("testdatalibdata", "description"));

    //subdataLabelManage will be filled depending on the type of the library entry
    $("#editSubData_addRow").text(doc.getDocLabel("page_testdatalib_m_managetestdatalibdata", "link_add_new"));
    $("#editSubData_addRow").prop("title", doc.getDocLabel("page_testdatalib_m_managetestdatalibdata", "link_add_new_title"));
    //buttons    
    $("#cancelSubDataManage").text(doc.getDocLabel("page_global", "btn_cancel"));
    $("#saveChangesSubData").text(doc.getDocLabel("page_global", "buttonAdd"));
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayListTestDataLibDataLabels(doc) {
    //title
    $("#viewTestDataLibDataModalLabel").html(doc.getDocLabel("page_testdatalib_m_listtestdatalibdata", "title"));
    //table headers
    $("#viewTestDataLibDataID").html(doc.getDocOnline("testdatalib", "testdatalibid"));
    $("#viewTestDataLibDataSubData").html(doc.getDocOnline("testdatalibdata", "subData"));
    $("#viewTestDataLibValue").html(doc.getDocOnline("testdatalibdata", "value"));
    $("#viewTestDataLibColumn").html(doc.getDocOnline("testdatalibdata", "column"));
    $("#viewTestDataLibParsingAnswer").html(doc.getDocOnline("testdatalibdata", "parsinganswer"));
    $("#viewTestDataLibDescription").html(doc.getDocOnline("testdatalibdata", "description"));

    $("#closeSubDataManage").text(doc.getDocLabel("page_global", "buttonClose"));
}
/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 * 
 */
function displayListTestCasesLabels(doc) {
    //title
    $("#testCaseListModalLabel").text(doc.getDocLabel("page_testdatalib_m_gettestcases", "title"));
    //button
    $("#closeButton").text(doc.getDocLabel("page_global", "buttonClose"));
}

/**
 * Applies the translations for the create new library modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayCreateTestDataLibLabels(doc) {

    //title
    $("#addTestDataLibModalLabel").text(doc.getDocLabel("page_testdatalib_m_createlib", "title"));//docCreate.title.docLabel
    //cancel + add buttons
    $("#addTestDataLibButton").text(doc.getDocLabel("page_global", "btn_add"));
    $("#cancelTestDataLibButton").text(doc.getDocLabel("page_global", "btn_cancel"));
    //tabs, tab2 is updated when the entries are managed
    $("#tab1Text").text(doc.getDocLabel("page_testdatalib_m_createlib", "m_tab1_text"));

    //soap and sql specific configurations
    $("#sqlConfigurationsLbl").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "title_sql_configurations"));
    $("#soapConfigurationsLbl").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "title_soap_configurations"));

    //group information 
    $("#lbl_choose_group").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "lbl_choose_group"));
    $("#lbl_enter_group").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "lbl_enter_group"));

    //common information
    $("#lbl_name").html(doc.getDocOnline("testdatalib", "name"));
    $("#lbl_type").html(doc.getDocOnline("testdatalib", "type"));
    $("#lbl_system").html(doc.getDocOnline("testdatalib", "system"));
    $("#lbl_environment").html(doc.getDocOnline("testdatalib", "environment"));
    $("#lbl_country").html(doc.getDocOnline("testdatalib", "country"));
    $("#lbl_description").html(doc.getDocOnline("testdatalib", "description"));
    $("#lbl_database").html(doc.getDocOnline("testdatalib", "database"));
    $("#lbl_script").html(doc.getDocOnline("testdatalib", "script"));
    $("#lbl_service_path").html(doc.getDocOnline("testdatalib", "servicepath"));
    $("#lbl_method").html(doc.getDocOnline("testdatalib", "method"));
    $("#lbl_envelope").html(doc.getDocOnline("testdatalib", "envelope"));

    //documentation for sub-data entries
    //total number of entries
    updateSubDataTabLabel();
    $("#subdataHeader").html(doc.getDocOnline("testdatalibdata", "subData"));
    $("#valueHeader").html(doc.getDocOnline("testdatalibdata", "value"));
    $("#columnHeader").html(doc.getDocOnline("testdatalibdata", "column"));
    $("#parsingAnswerHeader").html(doc.getDocOnline("testdatalibdata", "parsingAnswer"));
    $("#descriptionHeader").html(doc.getDocOnline("testdatalibdata", "description"));
    $("#unmutableRowNotEditable").prop("title", doc.getDocOnline("page_testdatalib_m_createlib", "tooltip_defaultsubdata")); //tooltip for row that is not editable or removable


    //links for managing the subdata information
    $("#link_add_new").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "link_add_new"));
    $("#link_delete_all").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "link_delete_all"));
    $("#link_add_new_title").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "link_add_new_title"));
    $("#link_delete_all_title").html(doc.getDocOnline("page_testdatalib_m_createupdatelib", "link_delete_all_title"));


    //tab2 - links to edit table
    $("#newSubData_addRow").text(doc.getDocLabel("page_testdatalib_m_createlib", "link_add_new"));
    $("#newSubData_addRow").prop("title", doc.getDocLabel("page_testdatalib_m_createlib", "link_add_new_title"));
    $("#newSubData_deleteAll").text(doc.getDocLabel("page_testdatalib_m_createlib", "link_delete_all"));
    $("#newSubData_deleteAll").prop("title", doc.getDocLabel("page_testdatalib_m_createlib", "link_delete_all_title"));

}

/**
 * Auxiliary method that adds options to the testdatalib table, when the user has permissions for management operation
 * @param {type} data  sent from user 
 */
function renderOptionsForTestDataManager(data) {
    //check if user has permissions to perform the add and import operations
    var doc = new Doc();

    if (data["hasPermissions"]) {
        if ($("#createLibButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createLibButton' type='bytton' class='btn btn-default'>";
            contentToAdd += doc.getDocLabel("page_testdatalib", "btn_create"); //translation for the create button;
            contentToAdd += "</button><button id='importDataButton' type='bytton' class='btn btn-default marginLeft5'>";
            contentToAdd += doc.getDocLabel("page_testdatalib", "btn_import"); //translation for the create button;
            contentToAdd += "</button></div>";

            $("#listOfTestDataLib_wrapper div.ColVis").before(contentToAdd);
            $('#createLibButton').click(createLibButtonClickHandler);

            $('#importDataButton').click(function() {
                var translations = {};
                //I defined specific translations for this upload modal
                translations["modalUploadLabel"] = doc.getDocLabel("page_testdatalib_m_upload", "title");
                translations["choseFileLabel"] = doc.getDocLabel("page_testdatalib_m_upload", "btn_choose");
                translations["cancelButton"] = doc.getDocLabel("page_testdatalib_m_upload", "btn_cancel").docLabel;
                translations["uploadOk"] = doc.getDocLabel("page_testdatalib_m_upload", "btn_upload");

                showModalUpload(uploadTestDataLibFromXMLFile, "XML", translations);
            });
        }
    } else {
        $("#testdatalibFirstColumnHeader").html(doc.getDocLabel("testdatalib", "actions_nopermissions"));
    }
}
/**
 * Handler that cleans the test case list modal when it is closed
 */
function testCaseListModalCloseHandler() {
    //we need to clear the item-groups that were inserted
    $('#testCaseListModal #testCaseListGroup a[id*="cat"]').remove();
    $('#testCaseListModal #testCaseListGroup div[id*="sub_cat"]').remove();
}
/**
 * Handler that removes the css error style when a field changes
 */
function subdataNameOnChangeHandler() {
    var parent = $(this).parents("div.form-group");
    if ($(parent).hasClass('has-error')) {
        $(parent).removeClass('has-error');
    }
}
/**
 * Handler Method responsible for saving a new test data lib entry.
 */
function saveNewTestDataLibHandler() {
    //shows the modal that allows the creation of test data lib 
    var formAdd = $("#addTestDataLibModal #addTestDataLibModalForm");

    var nameElement = formAdd.find("#name");
    //validates if the property name is not empty
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
    //end client-side validation        


    
    showLoaderInModal('#addTestDataLibModal');
    var jqxhr = $.post("CreateTestDataLib", formAdd.serialize());
    $.when(jqxhr).then(function(data) {
        hideLoaderInModal('#addTestDataLibModal');

        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#listOfTestDataLib").dataTable();
            //redraws table and goes to last page
            //It is possible to go directly to the last page because that is order by id
            //oTable.fnPageChange( 'last' );
            oTable.fnDraw(true);
            showMessage(data);
            $('#addTestDataLibModal').modal('hide');
        } else {
            showMessage(data, $('#addTestDataLibModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout).then(function() {
        hideLoaderInModal('#addTestDataLibModal');
    });

}
/**
 * Handler that cleans the modal for editing subdata when it is closed.
 */
function editTestDataLibDataModalCloseHandler() {
    $('#editSubDataTableBody tr').remove();
    clearResponseMessage($('#manageTestDataLibDataModal'));

}
/**
 * Handler that cleans the modal for editing a testdatalib entry when it is closed
 */
function editTestDataLibModalCloseHandler() {
    $('#editTestDataLibModal #editTestLibData')[0].reset();
    //resets the hidden value
    $('#editTestDataLibModal').find("#databaseedithidden").prop("value", "");
    clearResponseMessage($('#editTestDataLibModal'));
}

/**
 * Handler method that deletes a test data lib
 */
function deleteTestDataLibHandlerClick() {
    var testDataLibID = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteTestDataLib", {testdatalibid: testDataLibID}, "json");
    $.when(jqxhr).then(function(data) {
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
        showMessageMainPage(messageType, data.message);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
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

    $.when(jqxhr).then(function(data) {
        hideLoaderInModal("#modalUpload");
        var oTable = $("#listOfTestDataLib").dataTable();
        oTable.fnDraw(true);
        $('#modalUpload').modal('hide');
        showMessageMainPage(getAlertType(data.messageType), data.message);
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Handles the click on the create new lib
 */
function createLibButtonClickHandler() {
    //clearResponseMessageMainPage();
    //showLoaderInModal("#addTestDataLibModal");

    //retrieves the data from the server that allows the creation of a new library (groups, database,...)
    var jqxhr = $.getJSON("ReadInvariant", "");

    $.when(jqxhr).then(function(data) {
        var systemsList = [];
        var environmentList = [];
        var countryList = [];
        var databaseList = [];
        var testDataTypeList = [];


        $.each(data["contentTable"], function(idx, obj) {
            //extract all invariants that are needed for the page
            if (obj.idName === 'SYSTEM') {
                systemsList.push(obj.value);
            } else if (obj.idName === 'ENVIRONMENT') {
                environmentList.push(obj.value);
            } else if (obj.idName === 'COUNTRY') {
                countryList.push(obj.value);
            } else if (obj.idName === 'PROPERTYDATABASE') {
                databaseList.push(obj.value);
            } else if (obj.idName === 'TESTDATATYPE') {
                testDataTypeList.push(obj.value);
            }
        });


        var doc = new Doc();
        

        //when creating the testdatalibrary entry the static is the default select
        loadSelectElement(testDataTypeList, $('#addTestDataLibModal #type'));
        $('#addTestDataLibModal #type option[value="STATIC"]').attr("selected", "selected");

        //ensure that the panels are collapsed
        $('#addTestDataLibModal #panelSOAP').collapse("hide");
        $('#addTestDataLibModal #panelSQL').collapse("hide");


        loadSelectElement(systemsList, $('#addTestDataLibModal #system'), true, '');
        //ENVIRONMENT
        loadSelectElement(environmentList, $('#addTestDataLibModal #environment'), true, '');
        //Country
        loadSelectElement(countryList, $('#addTestDataLibModal #country'), true, '');
        //database
        loadSelectElement(databaseList, $('#addTestDataLibModal #database'), true, '');

        var jqxhrGroups = $.getJSON("ReadTestDataLib", "groups");

        $.when(jqxhrGroups).then(function(groupsData) {
            //loads the distinct groups
            var groupList = groupsData["contentTable"]
            console.log(groupList);
            loadSelectElement(groupList, $('#addTestDataLibModal #group'), true,
                    doc.getDocLabel("page_testdatalib_m_createlib", "lbl_dropdown_help"));

            $('#addTestDataLibModal #group option:first-child').attr("selected", "selected");
            $('#addTestDataLibModal #group option:first').addClass("emptySelectOption");
            $('#addTestDataLibModal #group').change();
            
            $('#addTestDataLibModal').modal('show');
        });


    }).fail(handleErrorAjaxAfterTimeout);


}
/**
 * Handler that cleans the modal for adding a testdatalib entry when it is closed
 */
function addTestDataLibModalCloseHandler() {
    var doc = getDoc();
    var docMultiSelect = doc.multiselect;

    $('#addTestDataLibModal #addTestDataLibModalForm')[0].reset();
    $('#addSubDataTableBody tr[class="trData"]').remove(); // removes all rows except the first one
    updateSubDataTabLabel();


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

function saveChangesSubDataClickHandler() {
    //clears the current messages before each click on save
    clearResponseMessage($("#manageTestDataLibDataModal"));

    var dataArray = {};
    var removeObjects = [];
    var updateObjects = [];
    var insertObjects = [];

    var hasRepeatedNames = validateSubDataEntriesRepeated($("#manageTestDataLibDataModal"), "editSubDataTableBody", true);

    if (!hasRepeatedNames) {
        return;
    }

    //selects the elements that were marked as to remove
    $("#editSubDataTableBody tr[data-operation='remove']").each(function() {
        var item = {};
        item ["testdatalibdataid"] = $(this).prop("id");
        item ["testdatalibid"] = $(this).attr("testdatalibid");
        item ["subdata"] = $(this).find("td:nth-child(2) input").prop("value");
        item ["value"] = $(this).find("td:nth-child(3) input").prop("value");
        item ["column"] = $(this).find("td:nth-child(4) input").prop("value");
        item ["parsinganswer"] = $(this).find("td:nth-child(5) input").prop("value");
        item ["description"] = $(this).find("td:nth-child(6) input").prop("value");
        removeObjects.push(item);
    });
    //gets the elements that will be updated
    $("#editSubDataTableBody tr[data-operation='update']").each(function() {
        var item = {};
        item ["testdatalibdataid"] = $(this).prop("id");
        item ["testdatalibid"] = $(this).attr("testdatalibid");
        item ["subdata"] = $(this).find("td:nth-child(2) input").prop("value");
        item ["value"] = $(this).find("td:nth-child(3) input").prop("value");
        item ["column"] = $(this).find("td:nth-child(4) input").prop("value");
        item ["parsinganswer"] = $(this).find("td:nth-child(5) input").prop("value");
        item ["description"] = $(this).find("td:nth-child(6) input").prop("value");
        updateObjects.push(item);
    });

    var resultInsert = true;
    //gets the elements that should be inserted
    $("#editSubDataTableBody tr[data-operation='insert']").each(function() {
        var item = {};
        item ["testdatalibdataid"] = -1;
        item ["testdatalibid"] =$(this).attr("testdatalibid");
        item ["subdata"] = $(this).find("td:nth-child(2) input").prop("value");
        item ["value"] = $(this).find("td:nth-child(3) input").prop("value");
        item ["column"] = $(this).find("td:nth-child(4) input").prop("value");
        item ["parsinganswer"] = $(this).find("td:nth-child(5) input").prop("value");
        item ["description"] = $(this).find("td:nth-child(6) input").prop("value");
        var foundRepeated = false;

        //check if is defined in the insert objects
        for (var j in insertObjects) {
            if (insertObjects[j]["subdata"] === item ["subdata"]) {
                //the user is trying to insert entries with the same name
                $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                foundRepeated = true;
            }
        }

        //check if is defined in the edit objects
        for (var i in updateObjects) {
            if (updateObjects[i]["subdata"] === item ["subdata"]) {
                //the user is trying to insert entries with the same name
                $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                foundRepeated = true;
            }
        }

        if (foundRepeated) {
            return resultInsert = false;
        }
        insertObjects.push(item);
    });

    if (!resultInsert) {
        return;
    }

    if (removeObjects.length > 0) {
        dataArray["remove"] = removeObjects;
    }
    if (updateObjects.length > 0) {
        dataArray["update"] = updateObjects;
    }
    if (insertObjects.length > 0) {
        dataArray["insert"] = insertObjects;
    }

    showLoaderInModal('#manageTestDataLibDataModal');
    var jqxhr = $.post("UpdateTestDataLibData", {data: JSON.stringify(dataArray)}, "json");
    $.when(jqxhr).then(function(data) {
        hideLoaderInModal('#manageTestDataLibDataModal');
        console.log("data" + data);
        if (getAlertType(data.messageType) === 'success') {
            $('#manageTestDataLibDataModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#manageTestDataLibDataModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);

}
function saveTestDataLibClickHandler() {
    var formEdit = $('#editTestDataLibModal').find('form#editTestLibData');
    showLoaderInModal('#editTestDataLibModal');

    var jqxhr = $.post("UpdateTestDataLib", formEdit.serialize(), "json");
    $.when(jqxhr).then(function(data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editTestDataLibModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#listOfTestDataLib").dataTable();
            oTable.fnDraw(true);
            $('#editTestDataLibModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editTestDataLibModal'));
        }
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
    var elementsWithRepeatedSubdata = $("#" + tableBody + " tr td:nth-child(2) input").filter(function() {
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
 * Auxiliary method that handles the group options.
 * @returns {undefined}
 */
function groupChangeHandler() {
    var suffix = "";
    if ($(this).prop("id") === "groupedit") {
        suffix = "edit";
    }
    if ($(this).val() !== '') {
        $(this).removeClass("emptySelectOption");
    } else {
        $(this).addClass("emptySelectOption");
    }
    var option = $(this).find("option:selected").val();
    if (option !== "") {
        $("#groupinput" + suffix).prop("disabled", "disabled");
        $("#groupinput" + suffix).prop("name", "groupdisabled");
        $(this).prop("name", "group");
    } else {
        $("#groupinput" + suffix).removeAttr("disabled");
        $("#groupinput" + suffix).prop("name", "group");
        $(this).prop("name", "groupdisabled");
    }

}


function deleteRowTestDataLibData(element) {
    deleteRow(element);
    updateSubDataTabLabel();
}

function deleteRow(element) {
    $(element).parents("tr").remove();
}

/**
 * Method that updates the tab text with number of entries that are currently defined.
 */
function updateSubDataTabLabel() {
    var doc = new Doc();

    //$("#tab2Text").text(doc.getDocLabel("page_testdatalib_m_createlib", "m_tab2_text"));
    $('#tab2Text').text(doc.getDocLabel("page_testdatalib_m_createlib", "m_tab2_text") + " (" +
            ($('#addSubDataTable tr[class="trData"]').size() + 1) + " " + doc.getDocLabel("page_testdatalib_m_createlib", "m_tab2_text_entries") + ")");
}
function editDeleteRowTestDataLibData(element) {
    //if is a new record then we know that is to remove from the interface
    var doc = new Doc();

    if ($(element).parents("tr").attr("data-operation") === 'insert') {
        deleteRow(element);
    } else if ($(element).parents("tr").attr("data-operation") === 'remove') {
        //the line was loaded from the database, then it should be market to be removed
        $(element).prop("title", doc.getDocLabel("page_global", "tooltip_mark_remove"));
        $(element).parents("tr").attr("data-operation", "update");
        $(element).find("span:first").removeAttr("class").addClass("glyphicon glyphicon-trash")
    } else {
        $(element).prop("title", doc.getDocLabel("page_global", "tooltip_delete_item"));
        $(element).parents("tr").attr("data-operation", "remove");
        $(element).find("span:first").removeAttr("class").addClass("glyphicon glyphicon-remove colorRed");
    }
}






function removeAllEntries(tableID) {
    removeRows(tableID);
}

function removeRows(tableID) {
    $('#' + tableID + ' tr[class="trData"]').remove();
}





/*************************** TestDataLib ******************************/
/**
 * Shows the alert that asks the user if he/she wants to delete the selected testdatalib and its entries.
 * @param {type} testDataLibID - id of the entry that will be deleted
 * @param {type} name - name of the entry used to create custom message
 * @param {type} system - system of the entry used to create custom message
 * @param {type} environment - environment of the entry used to create custom message
 * @param {type} country - country of the entry used to create custom message
 * @param {type} type - type of the entry used to create custom message
 */
function deleteTestDataLib(testDataLibID, name, system, environment, country, type) {
    var doc = new Doc();

    var systemLabel = system === '' ? doc.getDocLabel("page_global", "lbl_all") : system;
    var environmentLabel = environment === '' ? doc.getDocLabel("page_global", "lbl_all") : environment;
    var countryLabel = country === '' ? doc.getDocLabel("page_global", "lbl_all") : country;

    var deleteMessage = " TestDataLib: <ul><li>ID: " +
            testDataLibID +
            "</li><li>" + doc.getDocLabel("testdatalib", "name") + ": " + name +
            "</li><li>" + doc.getDocLabel("testdatalib", "type") + ": " + type +
            " </li><li>" + doc.getDocLabel("testdatalib", "system") + ": " + systemLabel +
            "</li><li> " + doc.getDocLabel("testdatalib", "environment") + ": " + environmentLabel +
            " </li><li>" + doc.getDocLabel("testdatalib", "country") + ":" + countryLabel +
            "</li> </ul>" + doc.getDocLabel("page_testdatalib_delete", "subdata_msg");
    var messageComplete = doc.getDocLabel("page_global", "deleteMessage").replace("%ENTRY%", deleteMessage).replace("%TABLE%", "");
    showModalConfirmation(deleteTestDataLibHandlerClick, doc.getDocLabel("page_testdatalib_delete", "title"), messageComplete, testDataLibID, "", "", "");

}

function editTestDataLib(testDataLibID) {
    clearResponseMessageMainPage();
    //load the data from the row 
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID);

    $.when(jqxhr).then(function(data) {

        var obj = data["testDataLib"];

        $('#editTestDataLibModal #testdatalibid').prop("value", testDataLibID);
        $('#editTestDataLibModal #name').prop("value", obj.name);
        $('#editTestDataLibModal #libdescription').prop("value", obj.description);
        //loads the information for soap entries
        $('#editTestDataLibModal #servicepath').prop("value", obj.servicePath);
        $('#editTestDataLibModal #method').prop("value", obj.method);
        $('#editTestDataLibModal #envelope').prop("value", obj.envelope);


        var jqxhrInvariant = $.getJSON("ReadInvariant", "");

        $.when(jqxhrInvariant).then(function(invariantData) {

            var systemsList = [];
            var environmentList = [];
            var countryList = [];
            var databaseList = [];
            var testDataTypeList = [];


            $.each(invariantData["contentTable"], function(idx, obj) {
                //extract all invariants that are needed for the page
                if (obj.idName === 'SYSTEM') {
                    systemsList.push(obj.value);
                } else if (obj.idName === 'ENVIRONMENT') {
                    environmentList.push(obj.value);
                } else if (obj.idName === 'COUNTRY') {
                    countryList.push(obj.value);
                } else if (obj.idName === 'PROPERTYDATABASE') {
                    databaseList.push(obj.value);
                } else if (obj.idName === 'TESTDATATYPE') {
                    testDataTypeList.push(obj.value);
                }
            });


            //load TYPE
            loadSelectElement(testDataTypeList, $('#editTestDataLibModal #type'), false, '');
            $('#editTestDataLibModal #type option[value="' + obj.type + '"]').attr("selected", "selected");

            if (obj.type === "SQL") {
                $("#panelSQLEdit").collapse("show");
                $("#panelSOAPEdit").collapse("hide");
            } else if (obj.type === "SOAP") {
                $("#panelSOAPEdit").collapse("show");
                $("#panelSQLEdit").collapse("hide");
            } else {
                //hide all if the type is static
                $("#panelSQLEdit").collapse("hide");
                $("#panelSOAPEdit").collapse("hide");
            }

            //SYSTEM
            loadSelectElement(systemsList, $('#editTestDataLibModal #system'), true, '');
            $('#editTestDataLibModal #system').find('option[value="' + obj.system + '"]').prop("selected", true);

            //ENVIRONMENT
            loadSelectElement(environmentList, $('#editTestDataLibModal #environment'), true, '');
            $('#editTestDataLibModal #environment').find('option[value="' + obj.environment + '"]').prop("selected", true);

            //Country
            loadSelectElement(countryList, $('#editTestDataLibModal #country'), true, '');
            $('#editTestDataLibModal #country').find('option[value="' + obj.country + '"]').prop("selected", true);

            //database
            loadSelectElement(databaseList, $('#editTestDataLibModal #database'), true, '');
            $('#editTestDataLibModal #database').find('option[value="' + obj.database + '"]:first').prop("selected", "selected");


            //loads groups from database
            var jqxhrGroups = $.getJSON("ReadTestDataLib", "groups");
            $.when(jqxhrGroups).then(function(groupsData) {
                //load distinct groups
                var doc = new Doc();
                loadSelectElement(groupsData["contentTable"], $('#editTestDataLibModal #groupedit'), true,
                        doc.getDocLabel("page_testdatalib_m_createlib", "lbl_dropdown_help"));
                //selects the group entered by the user

                $('#editTestDataLibModal #groupedit').find('option[value="' + obj.group + '"]:first').prop("selected", "selected");
                $('#editTestDataLibModal #groupedit').find('option:first').addClass("emptySelectOption");
                $('#editTestDataLibModal #groupedit').change();
            });


            //after everything. then shows the modal
            $('#editTestDataLibModal').modal('show');
        });




    }).fail(handleErrorAjaxAfterTimeout);

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

    $.when(jqxhr).then(function(result) {

        $('#testCaseListModal #totalTestCases').text(doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTests") + " " + result["TestCasesList"].length);
        var htmlContent = "";

        $.each(result["TestCasesList"], function(idx, obj) {

            var item = '<b><a class="list-group-item ListItem" data-remote="true" href="#sub_cat' + idx + '" id="cat' + idx + '" data-toggle="collapse" \n\
            data-parent="#sub_cat' + idx + '"><span class="pull-left">' + obj[0] + '</span>\n\
                                        <span style="margin-left: 25px;" class="pull-right">' + doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTestCases") + obj[2] + '</span>\n\
                                        <span class="menu-ico-collapse"><i class="fa fa-chevron-down"></i></span>\n\
                                    </a></b>';
            htmlContent += item;
            htmlContent += '<div class="collapse list-group-submenu" id="sub_cat' + idx + '">';


            $.each(obj[3], function(idx2, obj2) {
                var hrefTest = 'TestCase.jsp?Test=' + obj[0] + '&TestCase=' + obj2.TestCaseNumber;
                htmlContent += '<span class="list-group-item sub-item ListItem" data-parent="#sub_cat' + idx + '" style="padding-left: 78px;">';
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

function appendNewSubDataRow(rowtestDataLibDataId, testDataLibId, subData, value, column, parsingAnswer, description) {
    var doc = new Doc();
    var isReadOnly = '';
    var onClickEvent = 'onclick="editDeleteRowTestDataLibData(this)"';
    var buttonTitle = doc.getDocLabel("page_global", "tooltip_mark_remove");
    var buttonStyle = "trash";
    if (subData === '') { //is the default entry readonly
        buttonTitle = doc.getDocLabel("page_testdatalib_m_managetestdatalibdata", "tooltip_defaultsubdata");
        onClickEvent = 'disabled="disabled"';
        isReadOnly = "readonly='readonly'";
        buttonStyle = "minus";
    }
    //for each subdata entry adds a new row
    $('#editSubDataTableBody').append('<tr id="' + rowtestDataLibDataId + '" testdatalibid="' + testDataLibId + '" data-operation="update"> \n\
        <td><div class="nomarginbottom marginTop5"> \n\
        <button ' + onClickEvent + ' ' + buttonTitle + '\n\
class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-' + buttonStyle + '"></span></button></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
        <input ' + isReadOnly + ' name="subdata" type="text" class="subDataClass form-control input-xs" value="' + subData + '"/><span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="value" type="text" class="dataClass form-control input-xs" value="' + value + '" /></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="column" type="text" class="dataClass form-control input-xs" value="' + column + '" /></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="parsingAnswer" type="text" class="dataClass form-control input-xs" value="' + parsingAnswer + '" /></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="description" type="text" class="descriptionClass form-control input-xs" value="' + description + '" /></div></td></tr>');

}

function viewSubDataEntries(testDataLibID) {
    showLoaderInModal('#viewTestDataLibDataModal');

    var jqxhr = $.getJSON("ReadTestDataLibData", "testdatalibid=" + testDataLibID);

    $.when(jqxhr).then(function(result) {
        var configurations = new TableConfigurationsClientSide("viewTestDataLibDataEntriesTable", result["contentTable"], aoColumnsViewTestDataLibData(), true);
        configurations.tableWidth = "550px";

        if ($('#viewTestDataLibDataEntriesTable').hasClass('dataTable') === false) {
            createDataTable(configurations);
        } else {
            var oTable = $("#viewTestDataLibDataEntriesTable").dataTable();
            oTable.fnClearTable();
            if (result["contentTable"].length > 0) {
                oTable.fnAddData(result["contentTable"]);
            }
        }
        $('#viewTestDataLibDataModal').modal('show');
        hideLoaderInModal('#viewTestDataLibDataModal');

    }).fail(handleErrorAjaxAfterTimeout);

}


/**
 *
 * Method that allows the user to update the subdata entries for a testdatalib entry
 * @param {type} testDataLibID - test data lib id
 * @returns {undefined}
 */
function editSubData(testDataLibID) {
    clearResponseMessageMainPage();

    var jqxhr = $.getJSON("ReadTestDataLibData", "testdatalibid=" + testDataLibID);

    $.when(jqxhr).then(function(result) {
        $.each(result["contentTable"], function(idx, obj) {
            appendNewSubDataRow(obj.testDataLibDataID, obj.testDataLibID, obj.subData, obj.value, obj.column, obj.parsingAnswer, obj.description);
        });
        //show modal
        $('#manageTestDataLibDataModal').modal('show');

    }).fail(handleErrorAjaxAfterTimeout);
}


//https://datatables.net/examples/api/show_hide.html


function loadSelectElement(data, element, includeEmpty, includeEmptyText) {
    $(element).empty();
    if (includeEmpty !== null && includeEmpty) {
        $(element).append("<option value=''>" + includeEmptyText + "</option>");
    }
    data.sort();
    $.each(data, function(idx, obj) {
        $(element).append("<option value='" + obj + "'>" + obj + "</option>");
    });
}

function aoColumnsViewTestDataLibData() {
    var doc = new Doc();
    var aoColumns = [];
    $("#viewTestDataLibDataEntriesTable th").each(function(i) {
        switch (i) {
            case 0 :
                aoColumns.push({className: "width80", "sName": "TestDataLibID", "data": "testDataLibID", "title": doc.getDocOnline("testdatalib", "testdatalibid")});
                break;
            case 1:
                aoColumns.push({className: "width200", "sName": "Subdata", "data": "subData", "title": doc.getDocOnline("testdatalibdata", "subData")});
                break;
            case 2 :
                aoColumns.push({className: "width150", "sName": "value", "data": "value", "title": doc.getDocOnline("testdatalibdata", "value")});
                break;
            case 3:
                aoColumns.push({className: "width150", "sName": "column", "data": "column", "title": doc.getDocOnline("testdatalibdata", "column")});
                break;
            case 4:
                aoColumns.push({className: "width150", "sName": "parsingAnswer", "data": "parsingAnswer", "title": doc.getDocOnline("testdatalibdata", "parsingAnswer")});
                break;
            case 5:
                aoColumns.push({className: "width150", "sName": "Description", "data": "description", "title": doc.getDocOnline("testdatalibdata", "description")});
                break;

        }
    });
    return aoColumns;

}

function aoColumnsFuncTestDataLib(tableId) {
    var doc = new Doc();

    var aoColumns = [];
    $("#listOfTestDataLib th").each(function(i) {
        switch (i) {
            case 0:
                aoColumns.push({
                    className: "width250",
                    "sName": "TestDataLibID",
                    "data": "testDataLibID",
                    "bSortable": false,
                    "title": doc.getDocLabel("testdatalib", "actions"),
                    "mRender": function(data, type, oObj) {
                        var hasPermissions = $("#" + tableId).attr("hasPermissions");
                        var viewTestCase = '<button  class="getTestCasesUsing btn  btn-default btn-xs margin-right5" \n\
                            name="getTestCasesUsing" title="' + doc.getDocLabel("page_testdatalib", "tooltip_gettestcases") + '" type="button" \n\
                            onclick="getTestCasesUsing(' + data + ', \'' + oObj.name + '\', \'' + oObj.country + '\')">\n\
                            TC</button>';

                        if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                            var editElement = '<button id="editTestDataLib' + data + '"  onclick="editTestDataLib(' + data + ');" \n\
                                class="editTestDataLib btn btn-default btn-xs margin-right5" \n\
                            name="editTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-pencil"></span></button>';

                            var deleteElement = '<button onclick="deleteTestDataLib(' + oObj.testDataLibID + ',\'' + oObj.name
                                    + '\', ' + '\'' + oObj.system + '\', ' + '\'' + oObj.environment + '\', ' + '\'' + oObj.country + '\', '
                                    + '\'' + oObj.type + '\');" class="btn btn-default btn-xs margin-right25 " \n\
                            name="deleteTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_delete") + '" type="button">\n\
                            <span class="glyphicon glyphicon-trash"></span></button>';

                            var viewDataElement = '<button  class="editTestDataLib btn  btn-primary btn-xs margin-right5" \n\
                            name="editTestDataLib" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editsubdata") + '" type="button" onclick="editSubData(' + data + ')">\n\
                            <span class="glyphicon glyphicon-list-alt"></span></button>';

                            return '<div class="center btn-group width250">' + editElement + deleteElement + viewDataElement + viewTestCase + '</div>';
                        } else {
                            var viewDataElement = '<button  class="viewSubDataEntries btn  btn-primary btn-xs margin-right5" \n\
                            name="viewSubDataEntries" title="' + doc.getDocLabel("page_testdatalib", "tooltip_viewsubdata") + '" type="button" onclick="viewSubDataEntries(' + data + ', \'' + oObj.type + '\')">\n\
                            <span class="glyphicon glyphicon-list-alt"></span></button>';


                            return '<div class="center btn-group width250">' + viewDataElement + viewTestCase + '</div>';
                        }
                    }
                });
                break;

            case 1 :
                aoColumns.push({className: "width250", "sName": "Name", "data": "name", "title": doc.getDocOnline("testdatalib", "name")});
                break;
            case 2 :
                aoColumns.push({className: "width130", "sName": "System", "data": "system", "title": doc.getDocOnline("testdatalib", "system")});
                break;
            case 3 :
                aoColumns.push({className: "width130", "sName": "Environment", "data": "environment", "title": doc.getDocOnline("testdatalib", "environment")});
                break;
            case 4 :
                aoColumns.push({className: "width130", "sName": "Country", "data": "country", "title": doc.getDocOnline("testdatalib", "country")});
                break;
            case 5 :
                aoColumns.push({className: "width100", "sName": "Group", "data": "group", "title": doc.getDocOnline("testdatalib", "group")});
                break;
            case 6 :
                aoColumns.push({className: "width80", "sName": "Type", "data": "type", "title": doc.getDocOnline("testdatalib", "type")});
                break;
            case 7 :
                aoColumns.push({className: "width100", "sName": "Database", "data": "database", "title": doc.getDocOnline("testdatalib", "database")});
                break;
            case 8 :
                aoColumns.push({className: "width500", "sName": "Script", "data": "script", "title": doc.getDocOnline("testdatalib", "script")});
                break;
            case 9 :
                aoColumns.push({className: "width250", "sName": "ServicePath", "data": "servicePath", "title": doc.getDocOnline("testdatalib", "servicepath"),
                    "mRender": function(data, type, oObj) {
                        return drawURL(data);
                    }});
                break;
            case 10 :
                aoColumns.push({className: "width250", "sName": "Method", "data": "method", "title": doc.getDocOnline("testdatalib", "method")});
                break;
            case 11 :
                aoColumns.push({className: "width500", "sName": "Envelope", "data": "envelope", "title": doc.getDocOnline("testdatalib", "envelope")});
                break;
            case 12:
                aoColumns.push({className: "width150", "sName": "Description", "data": "description", "title": doc.getDocOnline("testdatalib", "description")});
                break;

            default :
                aoColumns.push({"sWidth": "100px"});
                break;
        }
    });
    return aoColumns;

}
