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
$.when($.getScript("js/pages/global/global.js")).then(function(){

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
     * Handles the click on the subdata button for properties getFromDataLib
     */
    $('button[id*="SubDataButton_"]').click(setSubDataButtonClickHandler);
    /*****************************************************************************/
    /***
     * Disables the button that allows the selection of subdata entries, when there is no entry specified.
     */
     $("textarea[name*='properties_value1_'][class*='getFromDataLib']").on("change keyup paste", textArea1ChangeCallback);
     
    /*****************************************************************************/
    /**
     * Clears all rows inserted for subdata
     */
    $('#selectSubDataFromListModal').on('hidden.bs.modal', function () {
       $('#subDataTableBody tr').remove();    
    });
    
    
             

    //temporary fix. TODO:FN refactoring in the next iteration -  should be refactored in the future after the update of the list of nature types
    /**
     * Changes the property nature when we change the property type. If we select the get from datalib, two options are hidden and therefore another should
     * be selected.
     * */
    $("div[id*='propertyRow'] select[id*='properties_nature_']").each(function(){
        var parents = $(this).parents("div[id*='propertyRow']");
        var natureElement = parents.find("select[id*='properties_type_']"); 
        var propertyType = $(natureElement).find("option:selected").prop("value");
        if(propertyType === "getFromDataLib"){
           $(this).find("option:first").prop("selected", true);
           $(this).find("option[value='RANDOMNEW']").addClass("hideElement");
           $(this).find("option[value='NOTINUSE']").addClass("hideElement");
        }else{
           $(this).find("option[value='RANDOMNEW']").removeClass("hideElement");
           $(this).find("option[value='NOTINUSE']").removeClass("hideElement");            
        }
    });
 
    
    //TODO: this needs to be redefined when the refactor of this page is performed
    $("div[id*='propertyRow'] select[id*='properties_type_']").change(function(){
        //check if the autocomplete was defined, //if it is not then define it
        var parents = $(this).parents("[id*='propertyRow']");
        var textArea1 = $(parents).find("textarea[id*='properties_value1']");
        
        //var textArea1 = $(parents).find("textarea[id*='properties_value1']");
        
        if($(this).prop("value") === "getFromDataLib"){
            console.log("is getfromdatalib");
            //gets the second textbox                
           
            //var textArea2 = $(parents).find("textarea[id*='properties_value2']");
            
            if($(textArea1).hasClass("getFromDataLib")){
                if(!$(textArea1).hasClass("ui-autocomplete-input")){
                    addAutoComplete($(textArea1), callbackAutoCompleteTestDataLibName, 1, true);                        
                }
            }
            //gets the first textbox
        }else{
            if($(textArea1).hasClass("ui-autocomplete-input")){
                $(textArea1).autocomplete("disable");  
            }
        }
    });

    //adds auto complete for all text areas with the class "getFromDataLib" that are rendered in the page when the pages loads
    //setPropertyValuesAutoComplete($("textarea[id*='properties_value1'][class*='getFromDataLib']"), callbackAutoCompleteTestDataLibName);
    
    $("textarea[id*='properties_value1'][class*='getFromDataLib']").each(function(){
        setPropertyValuesAutoComplete($(this), callbackAutoCompleteTestDataLibName);
    });
});
});
/**
 * Handles the click on the button that selects a subdata entry
 * @param {type} element - html element that was clicked
*/
function selectSubDataEntry(element){
    //select the subdata entry name in order to put it into the Value2 textarea
    var texto = $(element).parents("td").siblings(":first").text();    
    $('#selectSubDataFromListModal').modal("hide");            
    //updates the data stored in the testcase.jsp page
    var propertyValue = $('#selectSubDataFromListModal').find("#propertyValueSubDataID").prop("value");
    $("textarea#properties_value2_" + propertyValue).prop("value", texto);
}
/*****************************************************************************/
/**
 * Set the entry name selected in the textarea "Value1"
 * @param {type} button -- button selected 
 * @returns {undefined}
 */
function selectEntry(button){
    //select the entry name in order to put it into the Value1 textarea, and uses it to get the subdata list entries
    var texto = $(button).parents("td").siblings(":first").text();
    var propertyValue = $('#selectEntryFromListModal').find("#propertyValueID").prop("value");
    
    //hides de modal dialog
    $('#selectEntryFromListModal').modal("hide");
    
    
    //updates the data stored in the testcase.jsp page
    //triggers the change event if the current value is different from the new value
    
    if($("#properties_value1_" + propertyValue).prop("value") !== texto){
        $("#properties_value1_" + propertyValue).prop("value", texto); 
        //we need to trigger the change event in order to handle the required actions
        $("#properties_value1_" + propertyValue).trigger("change");
        //we need to invoke the blur() method to activate the changes on the autocomplete plugin
        $("#properties_value1_" + propertyValue).blur();        
    }
    
    //disables the subdata button if needed
    $("#SubDataButton_" + propertyValue).removeProp("disabled");
}

/**
 * Callback function that handles the click on the button that allows the user to select a subdata entry.
 */
function setSubDataButtonClickHandler(){
    var id = $(this).prop("id");
    var propertyValue = id.replace("SubDataButton_", "");  
    //gets the information for the entry id and the type in order to retrieve the subdatalist
    var textValue1 = $(this).parents("div[id*='propertyRow']").find("textarea#properties_value1_" + propertyValue).val(); 
    $('#selectSubDataFromListModal').find("#propertyValueSubDataID").prop("value", propertyValue);
    createSubEntriesTable(textValue1);
    $('#selectSubDataFromListModal').modal("show");
    
}

/**
 * Callback function that handles the click on the button that allows the user to select a entry.
 */
function setEntrybuttonClickHandler(){        
    var id = $(this).prop("id");
    var propertyValue = id.replace("entryButton_", ""); 
    createEntriesTable(propertyValue);
    $('#selectEntryFromListModal').find("#propertyValueID").prop("value", propertyValue);
    $('#selectEntryFromListModal').modal("show");
}

function setPropertyValuesAutoComplete(selector, source) {
    var taValue1Config = {};
    
    taValue1Config["source"] = source;
    
    //does not display the summary text
    taValue1Config["messages"] = {
            noResults: '',
            results: function() {}
    };       
    taValue1Config["delay"] = 500;         
    //on change allows us to refresh the autocomplete of the text area for value 2
    taValue1Config["change"] =  taValue1Config["create"] = function(event,ui){ 
        var taCurrentValue1 = $(this).prop("value");
        var propertyNumber = $(this).prop("id").replace("properties_value1_", "");
        var taValue2 = $("textarea[id*='properties_value2_" + propertyNumber + "']");
        
        if(taCurrentValue1 !== ''){
            //check if it was deactivated and activates the textarea2
            if($(taValue2).hasClass("ui-autocomplete-input")){
                $(taValue2).autocomplete("enable");
            }
            setAutoCompleteServerSide(taValue2, function(request, response){
                    $.ajax({
                    url: "GetTestDataLibData?action=2&subdata=" + request.term + "&limit=10&testDataLib="+ taCurrentValue1,
                    dataType: "json",      
                    success: function(data) {
                      response(data["data"]);
                    }
                });
            });
        }else{
            //deactivates the autocomplete from textarea 2 when textarea 1 has no data.
            if($(this).hasClass("ui-autocomplete-input")){
                if($(taValue2).hasClass("ui-autocomplete-input")){
                    $(taValue2).autocomplete("disable");
                }
            }
            
        }
    };

    //handles the selection of an item from the auto complete
    taValue1Config["select"]  = function(event,ui){ 
        var propertyNumber = $(this).prop("id").replace("properties_value1_", "");
        var taValue2 = $("textarea[id*='properties_value2_" + propertyNumber + "']");
        
        setAutoCompleteServerSide(taValue2, function(request, response){
                $.ajax({
                url: "GetTestDataLibData?action=2&subdata=" + request.term + "&limit=10&testDataLib="+ ui.item.label, //gets the label of the selected element
                dataType: "json",      
                success: function(data) {
                  response(data["data"]);
                }
            });
        });
    };
    
    //for each textarea for value1 that has the property type getFromDataLib we will set the autocomplete
    $(selector).autocomplete(taValue1Config);
    
}

function callbackAutoCompleteTestDataLibName(request, response) {
    $.ajax({
        url: "GetTestDataLib?action=2&testDataLib=" + request.term + "&limit=10",
        dataType: "json",      
        success: function(data) {
          response(data["data"]);
        }
    });
}

/**
 * Method that handles the change on the text area for value 1 when the type of property is getFromDataLib
 */
function textArea1ChangeCallback(){    
    
    var propertyValue = $(this).prop("id").replace("properties_value1_", "");
    var subDataButton = $("#SubDataButton_" + propertyValue);
    var text = $(this).prop("value");
     
    //setPropertyValuesAutoComplete($(this), callbackAutoCompleteTestDataLibName);
    //    $(this).trigger("change");
    //setPropertyValuesAutoComplete($(this), callbackAutoCompleteTestDataLibName);
    if(text === ''){ //if text is empty then disables the button for subdata list
        $(subDataButton).prop("disabled", "disabled");
        //disables the autocomplete
        //$(this).prop("autocomplete", "off"); TODO:FN disable/enable autocomplete
    }else{
        $(subDataButton).removeProp("disabled");                
    }    
}

 
/**
 * Creates the list of sub data entries for the selected entry
 * @param {type} value1Text
 * @returns {undefined}
 */
function createSubEntriesTable(value1Text){ 
    
    showLoaderInModal('#selectSubDataFromListModal');    
    
    //action=1 -> ACTION_GETALL_BYNAME
    var url = "action=1&testDataLibName=" + value1Text;
    var jqxhr = $.getJSON("GetTestDataLibData", url); 
    $.when(jqxhr).then(function(data) {
        var configurations = new TableConfigurationsClientSide("subDataTable", data["TestDataLibDatas"], aoColumnsFuncSubData());
        configurations.scrollY = "270px";            
    
        if($('#subDataTable').hasClass('dataTable')  === false){    
            createDataTable(configurations);
        }else{            
            var oTable = $("#subDataTable").dataTable();
            oTable.fnClearTable();
            if(data["TestDataLibDatas"].length > 0){
                oTable.fnAddData(data["TestDataLibDatas"]);                
            }
            
        } 
        hideLoaderInModal('#selectSubDataFromListModal');   
    }).fail(handleErrorAjaxAfterTimeout);;
        
    
}

/**
 * Creates a table that shows the list of entries
 * @returns {undefined}
 */
function createEntriesTable() {

    var oTable = $('#listOfTestDataLib').hasClass('dataTable') ; 
    
    if(oTable === false){
        
        //var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "GetTestDataLib", "TestDataLib", aoColumnsFunc(propertyValue));
        var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "GetTestDataLib", "TestDataLib", aoColumnsFunc());
        configurations.scrollY = "460px"; 
        showLoaderInModal('#selectEntryFromListModal');
        $.when(createDataTable(configurations)).then(function(){
            hideLoaderInModal('#selectEntryFromListModal');        
        });
        
    } 
}

//function aoColumnsFunc(propertyValue){
function aoColumnsFunc(){
    var aoColumns = [];
    $("#listOfTestDataLib th").each(function(i) {
        switch (i) {
            case 0: 
                aoColumns.push({
                    className:"width150  center", 
                    "sName": "TestDataLibID", 
                    "bSortable": false, 
                    "mRender": function(data, type, oObj) { 
                    var selectElement = '<button id="selectEntry' + data + '"  onclick="selectEntry(this);" \n\
                                                class="selectEntry btn btn-default btn-xs margin-right5" \n\
                                            name="editTestDataLib" title="Select entry" type="button">\n\
                                            <span class="glyphicon glyphicon-hand-up"></span></button>'; 
                    return '<div class="btn-group center">' + selectElement + '</div>';
                }});
            break;

            case 1 : 
                aoColumns.push({className:"width250", "sName": "Name"});
            break;
            case 2 : 
                aoColumns.push({className:"width80", "sName": "System"});
            break;
            case 3 : 
                aoColumns.push({className:"width100", "sName": "Environment"});
            break;
            case 4 : 
                aoColumns.push({className:"width80", "sName": "Country"});
            break;
            case 5 : 
                aoColumns.push({className: "width100", "sName": "Group"});
            break;
            case 6 : 
                aoColumns.push({className:"width80", "sName": "Type"});
            break;
            case 7 : 
                aoColumns.push({className:"width100", "sName": "Database"});
            break;
            case 8 : 
                aoColumns.push({className: "width500", "sName": "Script"});
            break;
            case 9 : 
                aoColumns.push({className:"width250", "sName": "ServicePath"});
            break;
            case 10 : 
                aoColumns.push({className:"width250", "sName": "Method"});
            break;
            case 11 : 
                aoColumns.push({className:"width500", "sName": "Envelope"});
            break;
            case 12: 
                aoColumns.push({className:"width150", "sName": "Description"});
            break;
            
            default :
                aoColumns.push({"sWidth": "100px"});
            break;
        }
    });
    return aoColumns;
    
    
}

function aoColumnsFuncSubData(){
    var aoColumns = [];
    $("#subDataTable th").each(function(i) {
        switch (i) {
            case 0: 
                aoColumns.push({
                    className:"width50 center", 
                    "sName": "TestDataLibID", 
                    "bSortable": false,                 
                    "mRender": function(data, type, oObj) {
                    var selectElement = '<button id="selectEntry' + data + '"  onclick="selectSubDataEntry(this);" \n\
                                                class="selectEntry btn btn-default btn-xs margin-right5" \n\
                                            name="editTestDataLib" title="Select entry" type="button">\n\
                                            <span class="glyphicon glyphicon-hand-up"></span></button>'; 

                        return '<div class="btn-group center">' + selectElement + '</div>';
                }});
            break;

            case 1 : 
                aoColumns.push({className:"width80", "sName": "Subdata"});
            break;
            case 2 : 
                aoColumns.push({className:"width80", "sName": "Data"});
            break;
            case 3 : 
                aoColumns.push({className:"width80", "sName": "Description"});
            break;
            case 4 : 
                aoColumns.push({className:"width50", "sName": "Type"});
            break;
            case 5 : 
                aoColumns.push({className:"width50", "sName": "System"});
            break;
            case 6 : 
                aoColumns.push({className:"width50", "sName": "Environment"});
            break;
            case 7 : 
                aoColumns.push({className:"width50", "sName": "Country"});
            break;
            

            
            default :
                aoColumns.push({"sWidth": "80px"});
            break;
        }
    });
    return aoColumns;
}
