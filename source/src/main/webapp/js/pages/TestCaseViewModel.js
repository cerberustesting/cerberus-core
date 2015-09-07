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

/**
 * Loads the common functions from a global javascript file
 * @param {type} param1 - filename
 */
$.when($.getScript("js/pages/global/global.js")).then(function() {
    displayPageLabel();
    /**
     * Document ready methods
     */
    $(function() {

        /**
         * Removes all rows when the modal window is hidden and clears the message
         */
        $('#myModal').on('hidden.bs.modal', function() {
            $('#editSubDataTableBody tr').remove();
            clearResponseMessage($('#myModal'));
        });

        /*****************************************************************************/
        /**
         * Handles the click on the entry button for properties getFromDataLib
         */
        $('button[id*="entryButton_"]').click(setEntrybuttonClickHandler);
        /*****************************************************************************/
        /**
         * Clears all rows inserted for subdata
         */
        $('#selectSubDataFromListModal').on('hidden.bs.modal', function() {
            $('#subDataTableBody tr').remove();
        });




        //temporary fix. TODO:FN refactoring in the next iteration -  should be refactored in the future after the update of the list of nature types
        /**
         * Changes the property nature when we change the property type. If we select the get from datalib, two options are hidden and therefore another should
         * be selected.
         * */
        $("div[id*='propertyRow'] select[id*='properties_nature_']").each(function() {
            var parents = $(this).parents("div[id*='propertyRow']");
            var natureElement = parents.find("select[id*='properties_type_']");
            var propertyType = $(natureElement).find("option:selected").prop("value");
            if (propertyType === "getFromDataLib") {
                $(this).find("option:first").prop("selected", true);
                $(this).find("option[value='RANDOMNEW']").addClass("hideElement");
                $(this).find("option[value='NOTINUSE']").addClass("hideElement");
            } else {
                $(this).find("option[value='RANDOMNEW']").removeClass("hideElement");
                $(this).find("option[value='NOTINUSE']").removeClass("hideElement");
            }
        });


        //TODO: this needs to be redefined when the refactor of this page is performed
        $("div[id*='propertyRow'] select[id*='properties_type_']").change(function() {
            //check if the autocomplete was defined, //if it is not then define it
            var parents = $(this).parents("[id*='propertyRow']");
            var textArea1 = $(parents).find("textarea[id*='properties_value1']");

            //var textArea1 = $(parents).find("textarea[id*='properties_value1']");

            if ($(this).prop("value") === "getFromDataLib") {
                console.log("is getfromdatalib");
                //gets the second textbox                

                //var textArea2 = $(parents).find("textarea[id*='properties_value2']");

                if ($(textArea1).hasClass("getFromDataLib")) {
                    if (!$(textArea1).hasClass("ui-autocomplete-input")) {
                        addAutoComplete($(textArea1), callbackAutoCompleteTestDataLibName, 1, true);
                    }
                }
                //gets the first textbox
            } else {
                if ($(textArea1).hasClass("ui-autocomplete-input")) {
                    $(textArea1).autocomplete("disable");
                }
            }
        });

        //adds auto complete for all text areas with the class "getFromDataLib" that are rendered in the page when the pages loads
        //setPropertyValuesAutoComplete($("textarea[id*='properties_value1'][class*='getFromDataLib']"), callbackAutoCompleteTestDataLibName);

        $("textarea[id*='properties_value1'][class*='getFromDataLib']").each(function() {
            setPropertyValuesAutoComplete($(this), callbackAutoCompleteTestDataLibName);
        });
    });
});
/**
 * Method that translates the content of the pages with base on the user language.
 */
function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    //tooltips of the buttons for the property type getFromDataLib
    $("button[id='entryButton']").prop("title", doc.getDocLabel('page_testcase','tooltip_select_entry'));
    $("button[data-id='entryButton_template']").prop("title", doc.getDocLabel('page_testcase','tooltip_select_entry'));
    $("*[name='labelTest']").html(doc.getDocOnline("test", "Test"));
    $("*[name='labelTestCase']").html(doc.getDocOnline("testcase", "TestCase"));
    $("*[name='labelTestCaseStepActionDescription']").html(doc.getDocOnline("testcasestepaction", "description"));
    $("*[name='labelTestCaseStepActionAction']").html(doc.getDocOnline("testcasestepaction", "Action"));
    $("*[name='labelTestCaseStepActionObject']").html(doc.getDocOnline("testcasestepaction", "Object"));
    $("*[name='labelTestCaseStepActionProperty']").html(doc.getDocOnline("testcasestepaction", "Property2"));
}
/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayListTestDataLib(doc){
    //title
    $("#selectEntryFromListModalLabel").html(doc.page_testcase_m_listtestdatalib.title.docLabel);
    //close button
    $("#closeTCListTestDataLib").text(doc.page_global.buttonClose.docLabel);    
}

/*****************************************************************************/
/**
 * Set the entry name selected in the textarea "Value1"
 * @param {type} button -- button selected 
 * @returns {undefined}
 */
function selectEntry(button) {
    //select the entry name in order to put it into the Value1 textarea, and uses it to get the subdata list entries
    var text = $(button).parents("td").siblings(":first").text();
    var propertyValue = $('#selectEntryFromListModal').find("#propertyValueID").prop("value");

    //hides de modal dialog
    $('#selectEntryFromListModal').modal("hide");


    //updates the data stored in the testcase.jsp page
    //triggers the change event if the current value is different from the new value

    if ($("#properties_value1_" + propertyValue).prop("value") !== text) {
        $("#properties_value1_" + propertyValue).prop("value", text);
        //we need to trigger the change event in order to handle the required actions
        $("#properties_value1_" + propertyValue).trigger("change");
        //we need to invoke the blur() method to activate the changes on the autocomplete plugin
        $("#properties_value1_" + propertyValue).blur();
    }


}


/**
 * Callback function that handles the click on the button that allows the user to select a entry.
 */
function setEntrybuttonClickHandler() {
    var id = $(this).prop("id");
    var propertyValue = id.replace("entryButton_", "");
    createEntriesTable(propertyValue);
    $('#selectEntryFromListModal').find("#propertyValueID").prop("value", propertyValue);
    $('#selectEntryFromListModal').modal("show");
}
/**
 * Auxiliary function that configures the autocomplete component
 * @param {type} selector
 * @param {type} source
 * @returns {undefined}
 */
function setPropertyValuesAutoComplete(selector, source) {
    var taValue1Config = {};

    taValue1Config["source"] = source;

    //does not display the summary text
    taValue1Config["messages"] = {
        noResults: '',
        results: function() {
        }
    };
    taValue1Config["delay"] = 500;
    //for each textarea for value1 that has the property type getFromDataLib we will set the autocomplete
    $(selector).autocomplete(taValue1Config);

}
/**
 * Callback function used to perform autocomplete while user is typing
 * @param {type} request 
 * @param {type} response
 * @returns {undefined}
 */
function callbackAutoCompleteTestDataLibName(request, response) {
    $.ajax({
        url: "ReadTestDataLib?action=2&testDataLib=" + request.term + "&limit=10",
        dataType: "json",
        success: function(data) {
            response(data["data"]);
        }
    });
}

/**
 * Creates a table that shows the list of entries
 * @returns {undefined}
 */
function createEntriesTable() {

    var oTable = $('#listOfTestDataLib').hasClass('dataTable');

    if (oTable === false) {

        //var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "GetTestDataLib", "TestDataLib", aoColumnsFunc(propertyValue));
        var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "TestDataLib", aoColumnsFunc());
        configurations.scrollY = "460px";
        showLoaderInModal('#selectEntryFromListModal');
        $.when(createDataTable(configurations)).then(function() {
            hideLoaderInModal('#selectEntryFromListModal');
        });

    }
}
/***
 * Auxiliary function used to customise the datatable aesthetics and translations
 * @returns {aoColumnsFunc.aoColumns|Array}
 */
function aoColumnsFunc() {
    var doc = getDoc();
    var docTestDataLib = doc.testdatalib;
    var docModal = doc.page_testcase_m_listtestdatalib;
    var aoColumns = [];
    $("#listOfTestDataLib th").each(function(i) {
        switch (i) {
            case 0:
                aoColumns.push({
                    className: "width150  center",
                    "sName": "TestDataLibID",
                    "bSortable": false,
                    "title":displayDocLink(docModal.actions),
                    "mRender": function(data, type, oObj) {
                        var selectElement = '<button id="selectEntry' + data + '"  onclick="selectEntry(this);" \n\
                                                class="selectEntry btn btn-default btn-xs margin-right5" \n\
                                            name="editTestDataLib" title="' + docModal.tooltip_choose_entry.docLabel + '" type="button">\n\
                                            <span class="glyphicon glyphicon-hand-up"></span></button>';
                        return '<div class="btn-group center">' + selectElement + '</div>';
                    }});
                break;

            case 1 :
                aoColumns.push({className: "width250", "sName": "Name", "title": displayDocLink(docTestDataLib.name)});
                break;
            case 2 :
                aoColumns.push({className: "width80", "sName": "System", "title": displayDocLink(docTestDataLib.system)});
                break;
            case 3 :
                aoColumns.push({className: "width100", "sName": "Environment", "title": displayDocLink(docTestDataLib.environment)});
                break;
            case 4 :
                aoColumns.push({className: "width80", "sName": "Country", "title": displayDocLink(docTestDataLib.country)});
                break;
            case 5 :
                aoColumns.push({className: "width100", "sName": "Group", "title": displayDocLink(docTestDataLib.group)});
                break;
            case 6 :
                aoColumns.push({className: "width80", "sName": "Type", "title": displayDocLink(docTestDataLib.type)});
                break;
            case 7 :
                aoColumns.push({className: "width100", "sName": "Database", "title": displayDocLink(docTestDataLib.database)});
                break;
            case 8 :
                aoColumns.push({className: "width500", "sName": "Script", "title": displayDocLink(docTestDataLib.script)});
                break;
            case 9 :
                aoColumns.push({className: "width250", "sName": "ServicePath", "title": displayDocLink(docTestDataLib.servicepath)});
                break;
            case 10 :
                aoColumns.push({className: "width250", "sName": "Method", "title": displayDocLink(docTestDataLib.method)});
                break;
            case 11 :
                aoColumns.push({className: "width500", "sName": "Envelope", "title": displayDocLink(docTestDataLib.envelope)});
                break;
            case 12:
                aoColumns.push({className: "width150", "sName": "Description", "title": displayDocLink(docTestDataLib.description)});
                break;

            default :
                aoColumns.push({"sWidth": "100px"});
                break;
        }
    });
    return aoColumns;


}