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
    /**
     * Document ready methods
     */

    displayPageLabel();

    $(function () {


        var i = 0;
        var j = 0;
        /*****************************************************************************/
        //adds new rows to the subdata table
        $("#newSubData_addRow").click(function () {

            $('#addSubDataTableBody').append('<tr class="trData" id="row' + (i + 1) + '">\n\\n\
                <td ><div class="nomarginbottom marginTop5"> <button onclick="deleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"  maxlength="200"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs" maxlength="1000"  /></div></td>\n\
                \n\
            </tr>');
            i++;

            $("#addTestDataLibModal #addSubDataTableBody tr td:nth-child(2) input:last").change(subdataNameOnChangeHandler);
            updateSubDataTabLabel();
        });
        /*****************************************************************************/
        //adds a new run in the edit window
        $("#editSubData_addRow").click(function () {
            $('#editSubDataTableBody').append('<tr class="trData" id="row' + (j + 1) + '" data-operation="insert">\n\\n\
                <td ><div class="nomarginbottom marginTop5"> <button onclick="editDeleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"   /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs"  /></div></td>\n\
                <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs"  /></div></td>\n\
                \n\
            </tr>');
            $("#manageTestDataLibDataModal #editSubDataTableBody tr td:nth-child(2) input:last").change(subdataNameOnChangeHandler);
            j++;
        });

        /*****************************************************************************/
        //delete all subdata rows     
        $("#newSubData_deleteAll").click(function () {
            removeAllEntries("addSubDataTable");
            updateSubDataTabLabel();
        });


        /*****************************************************************************/
        /**
         * Handles the click to save the test data lib entry
         */
        $("#saveTestDataLib").on("click", function () {

            var formEdit = $('#editTestDataLibModal').find('form#editTestLibData');
            showLoaderInModal('#editTestDataLibModal');

            var jqxhr = $.post("UpdateTestDataLib", formEdit.serialize(), "json");
            $.when(jqxhr).then(function (data) {
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

        });
        /*****************************************************************************/
        /**
         * Disables the group text box when the users selects an existing group
         */
        $("#Group").change(function () {
            if ($(this).val() !== '') {
                $(this).removeClass("emptySelectOption");
            } else {
                $(this).addClass("emptySelectOption");
            }
            var option = $(this).find("option:selected").val();
            if (option !== "") {
                $("#GroupInput").prop("disabled", "disabled");
                $("#GroupInput").prop("name", "GroupDisabled");
                $(this).prop("name", "Group");
            } else {
                $("#GroupInput").removeAttr("disabled");
                $("#GroupInput").prop("name", "Group");
                $(this).prop("name", "GroupDisabled");
            }
        });
        /*****************************************************************************/
        //TODO:FN refactoring in next iteration
        /**
         * Disables the group text box when the users selects an existing group
         */
        $("#GroupEdit").change(function () {

            if ($(this).val() !== '') {
                $(this).removeClass("emptySelectOption");
            } else {
                $(this).addClass("emptySelectOption");
            }


            var option = $(this).find("option:selected").val();
            if (option !== "") {
                $("#GroupEditInput").prop("disabled", "disabled");
                $("#GroupEditInput").prop("name", "GroupDisabled");
                $(this).prop("name", "GroupEdit");

            } else {

                $("#GroupEditInput").removeAttr("disabled");
                $("#GroupEditInput").prop("name", "GroupEdit");
                $(this).prop("name", "GroupDisabled");
            }

        });


        /*****************************************************************************/
        /*
         * Handles the change of the type when adding a new test data lib entry
         */
        $('#addTestDataLibModal').find("#Type").change(function () {
            refreshSpecificAreas();
        });

        /*
         * Handles the change of the type select  when editing a test data lib entry
         */
        $('#editTestDataLibModal #TypeSelect').change(function () {
            refreshSpecificAreasEdit();
        });

        /**
         * Method that saves new test data lib entry
         */
        $("#addTestDataLibButton").click(saveNewTestDataLibHandler);


        /**
         * Save changes performed in the subdata list
         */
        $("#saveChangesSubData").click(function () {
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
            $("#editSubDataTableBody tr[data-operation='remove']").each(function () {
                var item = {};
                var subData = $(this).find("td:nth-child(2) input").prop("value");
                item ["Subdata"] = subData;
                removeObjects.push(item);
            });
            //gets the elements that will be updated
            $("#editSubDataTableBody tr[data-operation='update']").each(function () {
                var item = {};
                item ["Subdata"] = $(this).find("td:nth-child(2) input").prop("value");
                item ["Subdata_original"] = $(this).find("td:nth-child(2) input").attr("data-original-value"); //allows the modification of the key
                item ["Value"] = $(this).find("td:nth-child(3) input").prop("value");
                item ["Description"] = $(this).find("td:nth-child(4) input").prop("value");
                updateObjects.push(item);
            });

            var resultInsert = true;
            //gets the elements that should be inserted
            $("#editSubDataTableBody tr[data-operation='insert']").each(function () {
                var item = {};
                item ["Subdata"] = $(this).find("td:nth-child(2) input").prop("value");
                item ["Value"] = $(this).find("td:nth-child(3) input").prop("value");
                item ["Description"] = $(this).find("td:nth-child(4) input").prop("value");
                var foundRepeated = false;

                //check if is defined in the insert objects
                for (var j in insertObjects) {
                    if (insertObjects[j]["Subdata"] === item ["Subdata"]) {
                        //the user is trying to insert entries with the same name
                        $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                        foundRepeated = true;
                    }
                }

                //check if is defined in the edit objects
                for (var i in updateObjects) {
                    if (updateObjects[i]["Subdata"] === item ["Subdata"]) {
                        //the user is trying to insert entries with the same name
                        $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                        foundRepeated = true;
                    }
                }

                var foundRowToRemove = false;
                var rowToRemove = null;
                for (var i in removeObjects) {
                    if (removeObjects[i]["Subdata"] === item ["Subdata"]) {
                        //the user is trying to insert an entry that already exists and was marked to be removed, 
                        //an update should be performed instead
                        foundRowToRemove = true;
                        rowToRemove = removeObjects[i];
                        break;
                    }
                }

                var istoUpdate = false;
                if (foundRowToRemove) {
                    var index = removeObjects.indexOf(rowToRemove);

                    if (index > -1) {
                        removeObjects.splice(index, 1);
                        istoUpdate = true;
                    }
                }

                if (foundRepeated) {
                    return resultInsert = false;
                } else {
                    if (istoUpdate) {
                        updateObjects.push(item);
                    } else {
                        insertObjects.push(item);
                    }

                }
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

            var testDataLibID = $('#manageTestDataLibDataModal').find("#testDataLibID").attr("value");
            var testDataLibType = $('#manageTestDataLibDataModal').find("#testDataLibType").attr("value");
            showLoaderInModal('#manageTestDataLibDataModal');
            var jqxhr = $.post("UpdateTestDataLibData", {id: testDataLibID, type: testDataLibType, data: JSON.stringify(dataArray)}, "json");
            $.when(jqxhr).then(function (data) {
                hideLoaderInModal('#manageTestDataLibDataModal');
                if (getAlertType(data.messageType) === 'success') {
                    $('#manageTestDataLibDataModal').modal('hide');
                    showMessage(data);
                } else {
                    showMessage(data, $('#manageTestDataLibDataModal'));
                }
            }).fail(handleErrorAjaxAfterTimeout);

        });

        /*
         * Specification of the methods that handle the bs.modal close.
         */
        $('#testCaseListModal').on('hidden.bs.modal', testCaseListModalCloseHandler);
        $('#manageTestDataLibDataModal').on('hidden.bs.modal', editTestDataLibDataModalCloseHandler);
        $('#editTestDataLibModal').on('hidden.bs.modal', editTestDataLibModalCloseHandler);
        $('#addTestDataLibModal').on('hidden.bs.modal', addTestDataLibModalCloseHandler);
        $('#testCaseListModal').on('hidden.bs.modal', testCaseListModalCloseHandler);

        $("#addTestDataLibModal #addSubDataTableBody tr td:nth-child(2) input").change(subdataNameOnChangeHandler);

        var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "TestDataLib", aoColumnsFuncTestDataLib());


        //creates the main table and draws the management buttons if the user has the permissions
        //TODO:FN refactoring in next iteration
        $.when(createDataTableWithPermissions(configurations, renderOptionsForTestDataManager)).then(function () {
            $("#listOfTestDataLib_wrapper div.ColVis .ColVis_MasterButton").addClass("btn btn-default");
        });
    })
});

/**
 * Method that translates the content of the pages with base on the user language.
 */
function displayPageLabel() {
    var doc = new Doc();
    var old_doc = getDoc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);

//    var docPage = doc.page_testdatalib;
    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));
//    
//    //set translations for create testdatalib modal
    displayCreateTestDataLibLabels(old_doc);   
    displayUpdateTestDataLibLabels(old_doc);
    displayManageTestDataLibDataLabels(old_doc);
    displayListTestCasesLabels(old_doc);    
    displayListTestDataLibDataLabels(old_doc);
    displayFooter(doc);
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayUpdateTestDataLibLabels(doc) {
    var docTestDataLib = doc.testdatalib;
    //title 
    $("#editTestDataLibTitle").text(doc.page_testdatalib_m_updatelib.title.docLabel);
    //content

    $("#lbl_type_edit").html(displayDocLink(docTestDataLib.type));
    $("#lbl_system_edit").html(displayDocLink(docTestDataLib.system));
    $("#lbl_environment_edit").html(displayDocLink(docTestDataLib.environment));
    $("#lbl_country_edit").html(displayDocLink(docTestDataLib.country));

    $("#lbl_description_edit").html(displayDocLink(docTestDataLib.description));
    $("#lbl_database_edit").html(displayDocLink(docTestDataLib.database));
    $("#lbl_script_edit").html(displayDocLink(docTestDataLib.script));
    $("#lbl_service_path_edit").html(displayDocLink(docTestDataLib.servicepath));
    $("#lbl_method_edit").html(displayDocLink(docTestDataLib.method));
    $("#lbl_envelope_edit").html(displayDocLink(docTestDataLib.envelope));
    //buttons    
    $("#cancelTestDataLib").text(doc.page_global.btn_cancel.docLabel);
    $("#saveTestDataLib").text(doc.page_global.buttonAdd.docLabel);

    var groupDoc = doc.page_testdatalib_m_createupdatelib;
    //auxiliar for group edition
    $("#lbl_choose_group_edit").html(displayDocLink(groupDoc.lbl_choose_group));
    $("#lbl_enter_group_edit").html(displayDocLink(groupDoc.lbl_enter_group));
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayManageTestDataLibDataLabels(doc) {
    var docManageSubData = doc.page_testdatalib_m_managetestdatalibdata;
    $("#manageTestDataLibDataModalLabel").html(docManageSubData.title.docLabel);
    $("#subdataActionsHeader").html(displayDocLink(docManageSubData.actions));
    $("#subdataHeaderManage").html(displayDocLink(doc.testdatalibdata.subData));
    $("#subdataDescriptionManage").html(displayDocLink(doc.testdatalibdata.description));
    //subdataLabelManage will be filled depending on the type of the library entry
    $("#editSubData_addRow").text(docManageSubData.link_add_new.docLabel);
    $("#editSubData_addRow").prop("title", docManageSubData.link_add_new_title.docLabel);
    //buttons    
    $("#cancelSubDataManage").text(doc.page_global.btn_cancel.docLabel);
    $("#saveChangesSubData").text(doc.page_global.buttonAdd.docLabel);
}

/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayListTestDataLibDataLabels(doc) {
    //title
    $("#viewTestDataLibDataModalLabel").html(doc.page_testdatalib_m_listtestdatalibdata.title.docLabel);
    //table headers
    $("#viewTestDataLibDataID").html(displayDocLink(doc.testdatalib.testdatalibid));
    $("#viewTestDataLibDataSubData").html(displayDocLink(doc.testdatalibdata.subData));
    $("#viewTestDataLibDataDescription").html(displayDocLink(doc.testdatalibdata.description));
    //the #viewTestDataLibDataLabel" is filled when the modal is opened because its value depends on the type of entry selected
    //button 
    $("#closeSubDataManage").text(doc.page_global.buttonClose.docLabel);
}
/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 * 
 */
function displayListTestCasesLabels(doc) {
    var docGetListTC = doc.page_testdatalib_m_gettestcases;
    var docGlobal = doc.page_global;
    //title
    $("#testCaseListModalLabel").text(docGetListTC.title.docLabel);
    //button
    $("#closeButton").text(docGlobal.buttonClose.docLabel);
}

/**
 * Applies the translations for the create new library modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayCreateTestDataLibLabels(doc) {
    var docCreate = doc.page_testdatalib_m_createlib;

    var docTestDataLib = doc.testdatalib;
    var docTestDataLibData = doc.testdatalibdata;
    //title
    $("#addTestDataLibModalLabel").text(docCreate.title.docLabel);
    //cancel + add buttons
    $("#addTestDataLibButton").text(doc.page_global.btn_add.docLabel);
    $("#cancelTestDataLibButton").text(doc.page_global.btn_cancel.docLabel);
    //tabs, tab2 is updated when the entries are managed
    $("#tab1Text").text(docCreate.m_tab1_text.docLabel);


    //group information
    var groupDoc = doc.page_testdatalib_m_createupdatelib;
    $("#lbl_choose_group").html(displayDocLink(groupDoc.lbl_choose_group));
    $("#lbl_enter_group").html(displayDocLink(groupDoc.lbl_enter_group));

    //common information
    $("#lbl_name").html(displayDocLink(docTestDataLib.name));
    $("#lbl_type").html(displayDocLink(docTestDataLib.type));
    $("#lbl_system").html(displayDocLink(docTestDataLib.system));
    $("#lbl_environment").html(displayDocLink(docTestDataLib.environment));
    $("#lbl_country").html(displayDocLink(docTestDataLib.country));
    $("#lbl_description").html(displayDocLink(docTestDataLib.description));
    $("#lbl_database").html(displayDocLink(docTestDataLib.database));
    $("#lbl_script").html(displayDocLink(docTestDataLib.script));
    $("#lbl_service_path").html(displayDocLink(docTestDataLib.servicepath));
    $("#lbl_method").html(displayDocLink(docTestDataLib.method));
    $("#lbl_envelope").html(displayDocLink(docTestDataLib.envelope));

    //documentation for sub-data entries
    $("#subdataHeader").html(displayDocLink(docTestDataLibData.subData));
    $("#dataHeader").html(displayDocLink(docTestDataLibData.value));
    $("#descriptionHeader").html(displayDocLink(docTestDataLibData.description));


    //links for managing the subdata information
    $("#link_add_new").html(displayDocLink(docCreate.link_add_new));
    $("#link_delete_all").html(displayDocLink(docCreate.link_delete_all));
    $("#link_add_new_title").html(displayDocLink(docCreate.link_add_new_title));
    $("#link_delete_all_title").html(displayDocLink(docCreate.link_delete_all_title));


    //tab2 - links to edit table
    $("#newSubData_addRow").text(docCreate.link_add_new.docLabel);
    $("#newSubData_addRow").prop("title", docCreate.link_add_new_title.docLabel);
    $("#newSubData_deleteAll").text(docCreate.link_delete_all.docLabel);
    $("#newSubData_deleteAll").prop("title", docCreate.link_delete_all_title.docLabel);

}

/**
 * Auxiliary method that adds options to the testdatalib table, when the user has permissions for management operation
 * @param {type} data  sent from user 
 */
function renderOptionsForTestDataManager(data) {
    //check if user has permissions to perform the add and import operations
    var doc = getDoc();

    if (data["hasPermissions"]) {
        if ($("#createLibButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createLibButton' type='bytton' class='btn btn-default'>";
            contentToAdd += doc.page_testdatalib.btn_create.docLabel; //translation for the create button;
            contentToAdd += "</button><button id='importDataButton' type='bytton' class='btn btn-default marginLeft5'>";
            contentToAdd += doc.page_testdatalib.btn_import.docLabel; //translation for the create button;
            contentToAdd += "</button></div>";

            $("#listOfTestDataLib_wrapper div.ColVis").before(contentToAdd);
            $('#testdatalib #createLibButton').click(createLibButtonClick);

            $('#importDataButton').click(function () {
                var translations = {};
                //I defined specific translations for this upload modal
                var docImportTestDataLib = doc.page_testdatalib_m_upload;
                translations["modalUploadLabel"] = docImportTestDataLib.title.docLabel;
                translations["choseFileLabel"] = docImportTestDataLib.btn_choose.docLabel;
                translations["cancelButton"] = docImportTestDataLib.btn_cancel.docLabel;
                translations["uploadOk"] = docImportTestDataLib.btn_upload.docLabel;

                showModalUpload(uploadTestDataLibFromXMLFile, "XML", translations);
            });
        }
    } else {

        var actionsNoPermissions = doc.testdatalib.actions_nopermissions;
        $("#testdatalibFirstColumnHeader").html(displayDocLink(actionsNoPermissions));
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

    var nameElement = formAdd.find("#Name");
    //validates if the property name is not empty
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var doc = getDoc();
        var docPageTestDataLib = doc.page_testdatalib;
        var localMessage = new Message("danger", docPageTestDataLib.empty_name_message.docLabel);
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


    var isSystemAll = $("#addTestDataLibModal input[type='checkbox'][value='multiselect-all-system']").is(":checked");
    var isEnvironmentAll = $("#addTestDataLibModal input[type='checkbox'][value='multiselect-all-environment']").is(":checked");
    var isCountryAll = $("#addTestDataLibModal input[type='checkbox'][value='multiselect-all-country']").is(":checked");

    $('#addTestDataLibModal #systemAll').prop("value", isSystemAll);
    $('#addTestDataLibModal #environmentAll').prop("value", isEnvironmentAll);
    $('#addTestDataLibModal #countryAll').prop("value", isCountryAll);

    showLoaderInModal('#addTestDataLibModal');
    var jqxhr = $.post("CreateTestDataLib", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
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
    }).fail(handleErrorAjaxAfterTimeout);

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
    $('#editTestDataLibModal').find("#DatabaseEditHidden").prop("value", "");
    clearResponseMessage($('#editTestDataLibModal'));
}

/**
 * Handler method that deletes a test data lib
 */
function deleteTestDataLibHandlerClick() {
    var testDataLibID = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteTestDataLib", {action: "delete", id: testDataLibID}, "json");
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

    $.when(jqxhr).then(function (data) {
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
function createLibButtonClick() {
    clearResponseMessageMainPage();
    showLoaderInModal("#addTestDataLibModal");

    //action = 3 retrieves the groups and the if is sql type it also returns the list of databases
    var jqxhr = $.getJSON("ReadTestDataLib", "action=0&Type=STATIC");
    $.when(jqxhr).then(function (data) {
        var doc = getDoc();
        $('#addTestDataLibModal').modal('show');
        showLoaderInModal("#addTestDataLibModal");
        loadSelectElement(data["GROUPS"], $('#addTestDataLibModal #Group'), true, doc.page_testdatalib_m_createlib.lbl_dropdown_help.docLabel);
        $('#addTestDataLibModal #Group option:first-child').attr("selected", "selected");
        $('#addTestDataLibModal #Group option:first').addClass("emptySelectOption");
        $('#addTestDataLibModal #Group').change();

        loadTypes(data["TESTDATATYPE"]);
        loadSystems($('#addTestDataLibModal'), data["SYSTEM"]);
        //ENVIRONMENT
        loadEnvironments($('#addTestDataLibModal'), data["ENVIRONMENT"]);
        //Country
        loadCountries($('#addTestDataLibModal'), data["Country"]);
        hideLoaderInModal("#addTestDataLibModal");

    }).fail(handleErrorAjaxAfterTimeout);


}
/**
 * Handler that cleans the modal for adding a testdatalib entry when it is closed
 */
function addTestDataLibModalCloseHandler() {
    var doc = getDoc();
    var docMultiSelect = doc.multiselect;

    $('#addTestDataLibModal #addTestDataLibModalForm')[0].reset();
    $('#addSubDataTableBody tr').remove();
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
    $(this).find('#Group option:first').prop("selected", "selected");
    //selects the first option for type
    $(this).find('#Type option:first').prop("selected", "selected");
    $(this).find('div.has-error').removeClass("has-error");
    //clears the response messages
    clearResponseMessage($('#addTestDataLibModal'));


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
        var doc = getDoc();
        var docPageTestDataLib = doc.page_testdatalib;
        var localMessage = new Message("danger", docPageTestDataLib.duplicated_message.docLabel + elementsWithRepeatedSubdata);
        showMessage(localMessage, dialog);
        
        return false;
    }
    return true;

}
/**
 * Auxiliary method that refreshes the panels in the add form
 */
function refreshPanelsByType() {
    var selectedType = $('#addTestDataLibModal #Type option:selected').val();

    $('#addTestDataLibModal').find('[id = "panel' + selectedType + '"][name="panelData"]').css("display", "block");
    var panelsToHide = $('#addTestDataLibModal').find('[id != "panel' + selectedType + '"][name="panelData"]');
    //while inserting we will clean the values
    panelsToHide.find("#Database option:first").prop("selected", "selected");
    panelsToHide.find("input[type='text']").prop("value", "");
    panelsToHide.find("textarea").prop("value", "");
    panelsToHide.css("display", "none");

    updateSubDataTabLabel();
}

/**
 * Auxiliary method that refreshes the panels in the edit form
 */
function refreshPanelsByTypeEdit() {

    var selectedType = $('#editTestDataLibModal #TypeSelect option:selected').val();

    $('#editTestDataLibModal').find('[id = "panel' + selectedType + 'Edit"][class*="panelData"]').css("display", "block");

    var panelsToHide = $('#editTestDataLibModal').find('[id != "panel' + selectedType + 'Edit"][class*="panelData"]');
    panelsToHide.css("display", "none");
}

/**
 * Auxiliary method that refreshes the html elements according with the selected type - in the Edit test data lib modal
 */
function refreshSpecificAreasEdit() {
    //shows / hides the test data     
    var selectedType = $('#editTestDataLibModal #TypeSelect option:selected').val();
    refreshListsByType(selectedType, "editTestDataLibModal", "GroupEdit", "DatabaseEdit", false);
    refreshPanelsByTypeEdit();
}

/**
 * Auxiliary method that refreshes the html elements according with the selected type
 */
function refreshSpecificAreas() {
    //shows / hides the test data lib

    var selectedType = $('#addTestDataLibModal #Type option:selected').val();
    refreshListsByType(selectedType, "addTestDataLibModal", "Group", "Database", true);
    refreshPanelsByType();

}
/**
 * Auxilary metho that refreshs the group list with basis on the selected type 
 * 
 * @param {type} selectedType -  type that is going to be changed 
 * @param {type} formID - form where the elements will be refreshed
 * @param {type} groupElementID - ID of the select that lists the groups per type
 * @param {type} databaseElementID - ID of the select that lists the databases
 * @param {type} isToAdd - indicates if the function is being used in the form to add a new record or in the form to edit
 * @returns {undefined}
 */
function refreshListsByType(selectedType, formID, groupElementID, databaseElementID, isToAdd) {
    //action = 3 retrieves the groups and the if is sql type it also returns the list of databases
    var jqxhr = $.getJSON("ReadTestDataLib", "action=3&Type=" + selectedType)
    $.when(jqxhr).then(function (data) {
        var groupSelector = "#" + formID + " #" + groupElementID;
        var groupCurrentSelected = $(groupSelector + ' option:selected').prop("value");
        var doc = getDoc();
        loadSelectElement(data["GROUPS"], $(groupSelector), true, doc.page_testdatalib_m_createlib.lbl_dropdown_help.docLabel);

        if (isToAdd) {
            //selects the first item and changes its style
            $(groupSelector + ' option:first-child').attr("selected", "selected");
            $(groupSelector + ' option:first').addClass("emptySelectOption");
            $(groupSelector).change();
        } else {
            //selects the current 

            var elementToSelect = $(groupSelector + ' option[value="' + groupCurrentSelected + '"]');
            if (elementToSelect.size() === 0) {
                //adds a temporary group in the combobox
                $(groupSelector).append("<option value='" + groupCurrentSelected + "'>" + groupCurrentSelected + "</option>");
            }

            $(groupSelector + ' option[value="' + groupCurrentSelected + '"]').attr("selected", "selected");

        }
        if (selectedType === "SQL") {
            var currentSelected = $(databaseSelector + " option:selected").prop("value");

            if ($("#" + formID).find("#DatabaseEditHidden").prop("value") !== "") {
                currentSelected = $("#" + formID).find("#DatabaseEditHidden").prop("value");
                $("#" + formID).find("#DatabaseEditHidden").prop("value", "");
            }
            var databaseSelector = "#" + formID + " #" + databaseElementID;

            loadSelectElement(data["PROPERTYDATABASE"], $(databaseSelector), true, '');
            if (isToAdd === true) {
                $(databaseSelector + ' option:first-child').attr("selected", "selected");
            } else {
                $(databaseSelector + ' option[value="' + currentSelected + '"]').attr("selected", "selected");
            }
        }
        //updates the label, if exists
        if (isToAdd) {
            var labelEntry = getSubDataLabel(selectedType);
            $('#' + formID).find("#dataHeader").html(labelEntry);
        }

    }).fail(handleErrorAjaxAfterTimeout);
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
    var doc = getDoc();
    var docCreate = doc.page_testdatalib_m_createlib;
    $("#tab2Text").text(docCreate.m_tab2_text.docLabel);
    $('#tab2Text').text(docCreate.m_tab2_text.docLabel + " (" + $('#addSubDataTable tr[class="trData"]').size() + " " + docCreate.m_tab2_text_entries.docLabel + ")");
}
function editDeleteRowTestDataLibData(element) {
    //if is a new record then we know that is to remove from the interface
    var doc = getDoc();
    var docPageGlobal = doc.page_global;
    if ($(element).parents("tr").attr("data-operation") === 'insert') {
        deleteRow(element);
    } else if ($(element).parents("tr").attr("data-operation") === 'remove') {
        //the line was loaded from the database, then it should be market to be removed
        $(element).prop("title", docPageGlobal.tooltip_mark_remove.docLabel);
        $(element).parents("tr").attr("data-operation", "update");
        $(element).find("span:first").removeAttr("class").addClass("glyphicon glyphicon-trash")
    } else {
        $(element).prop("title", docPageGlobal.tooltip_delete_item.docLabel);
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
    var doc = getDoc();
    var docTestDataLib = doc.testdatalib;
    var docTestDataLibDelete = doc.page_testdatalib_delete;
    var docPageGlobal = doc.page_global;
    var baseMessage = docPageGlobal.deleteMessage.docLabel;
    var systemLabel = system === '' ? docPageGlobal.lbl_all.docLabel : system;
    var environmentLabel = environment === '' ? docPageGlobal.lbl_all.docLabel : environment;
    var countryLabel = country === '' ? docPageGlobal.lbl_all.docLabel : country;

    var deleteMessage = " TestDataLib: <ul><li>ID: " +
            testDataLibID +
            "</li><li>" + docTestDataLib.type.docLabel + ": " + name +
            "</li><li>" + docTestDataLib.type.docLabel + ": " + type +
            " </li><li>" + docTestDataLib.system.docLabel + ": " + systemLabel +
            "</li><li> " + docTestDataLib.environment.docLabel + ": " + environmentLabel +
            " </li><li>" + docTestDataLib.country.docLabel + ":" + countryLabel +
            "</li> </ul>" + docTestDataLibDelete.subdata_msg.docLabel;
    var messageComplete = baseMessage.replace("%ENTRY%", deleteMessage).replace("%TABLE%", "");
    showModalConfirmation(deleteTestDataLibHandlerClick, docTestDataLibDelete.title.docLabel, messageComplete, testDataLibID);

}

function editTestDataLib(testDataLibID, type) {
    clearResponseMessageMainPage();
    //load the data from the row 
    //action=1 -> findTestDataLibByID
    var jqxhr = $.getJSON("ReadTestDataLib", "action=1&testDataLib=" + testDataLibID + "&Type=" + type);
    $.when(jqxhr).then(function (data) {
        var obj = data["testDataLib"];

        var formEdit = $('#editTestDataLibModal');

        formEdit.find('#testDataLibIDEdit').prop("value", testDataLibID);

        formEdit.find('input[name="NameEdit"]').prop("value", obj[1]);
        formEdit.find('#NameEdit').text(obj[1]);

        //specify the system+environment+country information
        var systemText = obj[2];
        var environmentText = obj[3];
        var countryText = obj[4];

        //load TYPE
        loadSelectElement(data["TESTDATATYPE"], $('#editTestDataLibModal').find("#TypeSelect"), false, "");
        //selects the type correspondent to the current library
        $('#editTestDataLibModal #TypeSelect option[value="' + obj[6] + '"]').attr("selected", "selected");

        //SYSTEM
        //loadSystems($('#editTestDataLibModal'), data["SYSTEM"]);
        loadSelectElement(data["SYSTEM"], $('#editTestDataLibModal').find("#System"), true, "");
        //ENVIRONMENT
        //loadEnvironments($('#editTestDataLibModal'), data["ENVIRONMENT"]);            
        loadSelectElement(data["ENVIRONMENT"], $('#editTestDataLibModal').find("#Environment"), true, "");
        //Country
        //loadCountries($('#editTestDataLibModal'), data["Country"]);
        loadSelectElement(data["Country"], $('#editTestDataLibModal').find("#Country"), true, "");

        formEdit.find('#System option[value="' + systemText + '"]').prop("selected", true);
        formEdit.find('#Environment option[value="' + environmentText + '"]').prop("selected", true);
        formEdit.find('#Country option[value="' + countryText + '"]').prop("selected", true);


        //hide the areas that are not relevant
        formEdit.find('[id = "panel' + obj[6] + 'Edit"][class="panelData"]').css("display", "block");
        formEdit.find('[id != "panel' + obj[6] + 'Edit"][class="panelData"]').css("display", "none");



        //loads the information for soap entries
        formEdit.find('#ServicePathEdit').prop("value", obj[9]);
        formEdit.find('#MethodEdit').prop("value", obj[10]);
        formEdit.find('#EnvelopeEdit').prop("value", obj[11]);

        if (obj[6] === "SQL") {
            //loads the information for sql entriesfica aquii
            //getInvariantList("PROPERTYDATABASE", function(invariantData){
            loadSelectElement(data["PROPERTYDATABASE"], formEdit.find("#DatabaseEdit"), true, '');
            formEdit.find('#DatabaseEdit option[value="' + obj[7] + '"]:first').prop("selected", "selected");
        } else {
            //append the value if it does not the SQL type
            formEdit.find("#DatabaseEditHidden").prop("value", obj[7]);
        }
        formEdit.find('#ScriptEdit').prop("value", obj[8]);
        //load groups per type
        var doc = getDoc();
        loadSelectElement(data["GROUPS"]["GROUPS"], $('#editTestDataLibModal').find("#GroupEdit"), true, doc.page_testdatalib_m_createlib.lbl_dropdown_help.docLabel);
        //selects the group entered by the user

        formEdit.find('#GroupEdit option[value="' + obj[5] + '"]:first').prop("selected", "selected");
        formEdit.find('#GroupEdit option:first').addClass("emptySelectOption");
        formEdit.find('#GroupEdit').change();

        //end load groups per type

        formEdit.find('#EntryDescriptionEdit').prop("value", obj[12]);

        formEdit.modal('show');



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
    var jqxhr = $.getJSON("ReadTestDataLib", "action=4&testDataLib=" + testDataLibID + "&name=" + name + "&country=" + country);

    var doc = getDoc();
    var docGetTestCases = doc.page_testdatalib_m_gettestcases;

    $.when(jqxhr).then(function (result) {

        $('#testCaseListModal #totalTestCases').text(docGetTestCases.nrTests.docLabel + " " + result["TestCasesList"].length);
        var htmlContent = "";
        var docTestCases = doc.testcase;
        $.each(result["TestCasesList"], function (idx, obj) {

            var item = '<b><a class="list-group-item ListItem" data-remote="true" href="#sub_cat' + idx + '" id="cat' + idx + '" data-toggle="collapse" \n\
            data-parent="#sub_cat' + idx + '"><span class="pull-left">' + obj[0] + '</span>\n\
                                        <span style="margin-left: 25px;" class="pull-right">' + docGetTestCases.nrTestCases.docLabel + obj[2] + '</span>\n\
                                        <span class="menu-ico-collapse"><i class="fa fa-chevron-down"></i></span>\n\
                                    </a></b>';
            htmlContent += item;
            htmlContent += '<div class="collapse list-group-submenu" id="sub_cat' + idx + '">';


            $.each(obj[3], function (idx2, obj2) {
                var hrefTest = 'TestCase.jsp?Test=' + obj[0] + '&TestCase=' + obj2.TestCaseNumber;
                htmlContent += '<span class="list-group-item sub-item ListItem" data-parent="#sub_cat' + idx + '" style="padding-left: 78px;">';
                htmlContent += '<span class="pull-left"><a href="' + hrefTest + '">' + obj2.TestCaseNumber + '- ' + obj2.TestCaseDescription + '</a></span>';
                htmlContent += '<span class="pull-right">' + docGetTestCases.nrTests.docLabel + " " + obj2.NrProperties + '</span><br/>';
                htmlContent += '<span class="pull-left"> ' + docTestCases.Creator.docLabel + ": " + obj2.Creator + ' | '
                        + docTestCases.TcActive.docLabel + ": " + obj2.Active + ' | ' + docTestCases.Status.docLabel + ": " + obj2.Status + ' | ' +
                        doc.invariant.GROUP.docLabel + ": " + obj2.Group + ' | ' + doc.application.Application.docLabel + ": " + obj2.Application + '</span>';
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

function appendNewSubDataRow(rowId, subdata, data, description) {
    var doc = getDoc();

    //for each subdata entry adds a new row
    $('#editSubDataTableBody').append('<tr id="' + rowId + '" data-operation="update"> \n\
        <td><div class="nomarginbottom marginTop5"> \n\
        <button title="' + doc.page_global.tooltip_mark_remove.docLabel + '" onclick="editDeleteRowTestDataLibData(this)" \n\
class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
        <input name="subdata" type="text" class="subDataClass form-control input-xs" data-original-value="' + subdata + '" value="' + subdata + '"/><span></span></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs" value="' + data + '" /></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="description" type="text" class="descriptionClass form-control input-xs" value="' + description + '" /></div></td></tr>');

}

function viewSubDataEntries(testDataLibID, type) {
    showLoaderInModal('#viewTestDataLibDataModal');

    var jqxhr = $.getJSON("ReadTestDataLibData", "action=0&testDataLib=" + testDataLibID + "&type=" + type);

    $.when(jqxhr).then(function (result) {
        var configurations = new TableConfigurationsClientSide("viewTestDataLibDataEntriesTable", result["TestDataLibDatas"], aoColumnsViewTestDataLibData());
        configurations.tableWidth = "550px";

        if ($('#viewTestDataLibDataEntriesTable').hasClass('dataTable') === false) {
            createDataTable(configurations);
        } else {
            var oTable = $("#viewTestDataLibDataEntriesTable").dataTable();
            oTable.fnClearTable();
            if (result["TestDataLibDatas"].length > 0) {
                oTable.fnAddData(result["TestDataLibDatas"]);
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
 * @param {type} type - type of the entry
 * @returns {undefined}
 */
function editSubData(testDataLibID, type) {
    clearResponseMessageMainPage();
    //var type = $("#" + testDataLibID + " td:nth-child(7)").text();
    //action = 0 ACTION_GETALL_BYID
    var jqxhr = $.getJSON("ReadTestDataLibData", "action=0&testDataLib=" + testDataLibID + "&type=" + type);

    $.when(jqxhr).then(function (result) {
        $.each(result["TestDataLibDatas"], function (idx, obj) {
            var testDataLibID = obj[0];
            var subdata = obj[1];
            var data = obj[2];
            var description = obj[3];

            appendNewSubDataRow((testDataLibID + subdata), subdata, data, description);
        });

        //sets the values
        $('#manageTestDataLibDataModal').find("#subdataLabelManage").html(getSubDataLabel(type));
        $('#manageTestDataLibDataModal').find("#testDataLibID").attr("value", testDataLibID);
        $('#manageTestDataLibDataModal').find("#testDataLibType").attr("value", type);
        $('#manageTestDataLibDataModal').modal('show');

    }).fail(handleErrorAjaxAfterTimeout);


}





//https://datatables.net/examples/api/show_hide.html

function loadSystems(parent, data) {
    loadSelectElement(data, parent.find("#System"), false);
    parent.find("#System").multiselect({
        maxHeight: 150,
        checkboxName: 'System',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-system',
    });
}
function loadEnvironments(parent, data) {
    loadSelectElement(data, parent.find("#Environment"), false);
    parent.find("#Environment").multiselect({
        maxHeight: 150,
        checkboxName: 'Environment',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-environment',
    });
}
function loadCountries(parent, data) {
    loadSelectElement(data, parent.find("#Country"), false);
    parent.find("#Country").multiselect({
        maxHeight: 150,
        checkboxName: 'Country',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-country',
    });
}



function loadTypes(data) {
    loadSelectElement(data, $('#addTestDataLibModal #Type'));
    $('#addTestDataLibModal #Type option[value="STATIC"]').attr("selected", "selected");
    var labelEntry = getSubDataLabel("STATIC");
    $('#addTestDataLibModal').find("#labelSubdataEntry").html(labelEntry);
    refreshPanelsByType();
}

function loadSelectElement(data, element, includeEmpty, includeEmptyText) {
    $(element).empty();
    if (includeEmpty !== null && includeEmpty) {
        $(element).append("<option value=''>" + includeEmptyText + "</option>");
    }
    $.each(data, function (idx, obj) {
        $(element).append("<option value='" + obj + "'>" + obj + "</option>");
    });

}

function aoColumnsViewTestDataLibData() {
    var doc = getDoc();
    var docTestdatalib = doc.testdatalib;
    var docTestdatalibdata = doc.testdatalibdata;
    var aoColumns = [];
    $("#viewTestDataLibDataEntriesTable th").each(function (i) {
        switch (i) {
            case 0 :
                aoColumns.push({className: "width80", "sName": "TestDataLibID", "title": displayDocLink(docTestdatalib.testdatalibid)});
                break;
            case 1:
                aoColumns.push({className: "width150", "sName": "Subdata", "title": displayDocLink(docTestdatalibdata.subData)});
                break;
            case 2 :
                aoColumns.push({className: "width350", "sName": "Data", "title": displayDocLink(docTestdatalibdata.value)});
                break;
            case 3 :
                aoColumns.push({className: "width150", "sName": "Description", "title": displayDocLink(docTestdatalibdata.description)});
                break;

        }
    });
    return aoColumns;

}
function aoColumnsFuncTestDataLib() {
    var doc = getDoc();
    var docPage = doc.page_testdatalib;
    var docObj = doc.testdatalib;

    var aoColumns = [];
    $("#listOfTestDataLib th").each(function (i) {
        switch (i) {
            case 0:
                aoColumns.push({
                    className: "width250",
                    "sName": "TestDataLibID",
                    "bSortable": false,
                    "title": displayDocLink(docObj.actions),
                    "mRender": function (data, type, oObj) {
                        var viewTestCase = '<button  class="getTestCasesUsing btn  btn-default btn-xs margin-right5" \n\
                            name="getTestCasesUsing" title="' + docPage.tooltip_gettestcases.docLabel + '" type="button" \n\
                            onclick="getTestCasesUsing(' + data + ', \'' + oObj[1] + '\', \'' + oObj[4] + '\')">\n\
                            TC</button>';
                        //if the user have permissions to edit, then the following button will be displayed
                        if (oObj[13] !== false) {
                            var editElement = '<button id="editTestDataLib' + data + '"  onclick="editTestDataLib(' + data + ', ' + '\'' + oObj[6] + '\');" \n\
                                class="editTestDataLib btn btn-default btn-xs margin-right5" \n\
                            name="editTestDataLib" title="' + docPage.tooltip_editentry.docLabel + '" type="button">\n\
                            <span class="glyphicon glyphicon-pencil"></span></button>';

                            var deleteElement = '<button onclick="deleteTestDataLib(' + data + ',\'' + oObj[1]
                                    + '\', ' + '\'' + oObj[2] + '\', ' + '\'' + oObj[3] + '\', ' + '\'' + oObj[4] + '\', '
                                    + '\'' + oObj[5] + '\', ' + '\'' + oObj[7] + '\');" class="btn btn-default btn-xs margin-right25 " \n\
                            name="deleteTestDataLib" title="' + docPage.tooltip_delete.docLabel + '" type="button">\n\
                            <span class="glyphicon glyphicon-trash"></span></button>';

                            var viewDataElement = '<button  class="editTestDataLib btn  btn-primary btn-xs margin-right5" \n\
                            name="editTestDataLib" title="' + docPage.tooltip_editsubdata.docLabel + '" type="button" onclick="editSubData(' + data + ', \'' + oObj[6] + '\')">\n\
                            <span class="glyphicon glyphicon-list-alt"></span></button>';

                            return '<div class="center btn-group width250">' + editElement + deleteElement + viewDataElement + viewTestCase + '</div>';
                        } else {
                            var viewDataElement = '<button  class="viewSubDataEntries btn  btn-primary btn-xs margin-right5" \n\
                            name="viewSubDataEntries" title="' + docPage.tooltip_viewsubdata.docLabel + '" type="button" onclick="viewSubDataEntries(' + data + ', \'' + oObj[6] + '\')">\n\
                            <span class="glyphicon glyphicon-list-alt"></span></button>';


                            return '<div class="center btn-group width250">' + viewDataElement + viewTestCase + '</div>';
                        }
                    }

                });
                break;

            case 1 :
                aoColumns.push({className: "width250", "sName": "Name", "title": displayDocLink(docObj.name)});
                break;
            case 2 :
                aoColumns.push({className: "width130", "sName": "System", "title": displayDocLink(docObj.system)});
                break;
            case 3 :
                aoColumns.push({className: "width130", "sName": "Environment", "title": displayDocLink(docObj.environment)});
                break;
            case 4 :
                aoColumns.push({className: "width130", "sName": "Country", "title": displayDocLink(docObj.country)});
                break;
            case 5 :
                aoColumns.push({className: "width100", "sName": "Group", "title": displayDocLink(docObj.group)});
                break;
            case 6 :
                aoColumns.push({className: "width80", "sName": "Type", "title": displayDocLink(docObj.type)});
                break;
            case 7 :
                aoColumns.push({className: "width100", "sName": "Database", "title": displayDocLink(docObj.database)});
                break;
            case 8 :
                aoColumns.push({className: "width500", "sName": "Script", "title": displayDocLink(docObj.script)});
                break;
            case 9 :
                aoColumns.push({className: "width250", "sName": "ServicePath", "title": displayDocLink(docObj.servicepath),
                    "mRender": function (data, type, oObj) {
                        if (data !== '') {
                            return "<a target = '_blank' href='" + data + "'>" + data + "</a>";//TODO:FN ver se tem caracters que precisam de ser encapsulados
                        }
                        return '';
                    }});
                break;
            case 10 :
                aoColumns.push({className: "width250", "sName": "Method", "title": displayDocLink(docObj.method)});
                break;
            case 11 :
                aoColumns.push({className: "width500", "sName": "Envelope", "title": displayDocLink(docObj.envelope)});
                break;
            case 12:
                aoColumns.push({className: "width150", "sName": "Description", "title": displayDocLink(docObj.description)});
                break;

            default :
                aoColumns.push({"sWidth": "100px"});
                break;
        }
    });
    return aoColumns;

}
