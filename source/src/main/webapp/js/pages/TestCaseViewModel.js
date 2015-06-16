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
 * Document ready methods
 */
$(function() {
    /**
    * Loads the common functions from a global javascript file
    * @param {type} param1 - filename
    */
    $.getScript("js/pages/global.js");
    /**
     * Removes all rows when the modal window is hidden and clears the message
     */
    $('#myModal').on('hidden.bs.modal', function() {
        $('#editSubDataTableBody tr').remove();
        clearResponselMessage($('#myModal'));
    });

    /*****************************************************************************/
    /**
     * Handles the click on the entry button for properties getFromDataLib
     */
    $('button[id*="entryButton_"]').click(function() {
        
        var id = $(this).prop("id");
        var propertyValue =id.replace("entryButton_", ""); 
        createEntriesTable(propertyValue);
        $('#selectEntryFromListModal').modal("show");

    });
    /*****************************************************************************/
     /**
     * Handles the click on the subdata button for properties getFromDataLib
     */
    $('button[id*="SubDataButton_"]').click(function() {
        
        var id = $(this).prop("id");
        var propertyValue = id.replace("SubDataButton_", "");  
        //gets the information for the entry id and the type in order to retrieve the subdatalist
        //TODO:FN retirar isto
        /*var entryID = $(this).siblings("#testDataLibID_" + propertyValue).prop("value");
        var entryType = $(this).siblings("#testDataLibType_" + propertyValue).prop("value");*/
         
         //TODO:FN mudar isto
        //createSubEntriesTable(entryID, entryType, propertyValue);
        
        $('#selectSubDataFromListModal').modal("show");

    });
    /*****************************************************************************/
    /***
     * Disables the button that allows the selection of subdata entries, when there is no entry specified.
     */
     $("textarea[id*='properties_value1_']").on("change keyup paste",function() {
        var propertyValue = $(this).prop("id").replace("properties_value1_", "");
        var subDataButton = $("#SubDataButton_" + propertyValue);
        var text = $(this).prop("value");
        
        if(text === ''){ //if text is empty then disables the button for subdata list
            $(subDataButton).prop("disabled", "disabled");
        }else{
            $(subDataButton).removeProp("disabled");
        }
    });
    /*****************************************************************************/
    /**
     * Clears all rows inserted for subdata
     */
    $('#selectSubDataFromListModal').on('hidden.bs.modal', function () {
       $('#subDataTableBody tr').remove();    
    });
});
 
 
/**
 * Creates the list of sub data entries for the selected entry
 * @param {type} entryId
 * @param {type} type
 * @param {type} propertyValue - index of the property for which we want to apply the entry
 * @returns {undefined}
 */
function createSubEntriesTable(entryId, type, propertyValue){ 
        
    $.when($.getJSON("GetTestDataLib", "action=findAllTestDataLibContent&testDataLib=" + entryId + "&type=" + type)).then(function(result) {
         
        $.each( result["TestDataLibDatas"] , function(idx, obj){ 
            console.log(obj)
            /*var subdata = obj[1];
            var data = obj[2];
            var description = obj[3]; 
            
            $('#subDataTableBody').append('<tr id="'+ idx +'" data-operation="update"> \n\
                <td><div class="nomarginbottom marginTop5"> \n\
                <button title="Select subdata entry" onclick="selectSubDataEntry(this, '+ propertyValue+')" \n\
                class="pull-left btn btn-default btn-xs"><span class="glyphicon glyphicon-hand-up"></span></button></div></td>\n\
                <td>'+ subdata +'/></td>\n\
                <td>'+data+'</td>\n\
                    <td>'+description+'</td>\n\
                </tr>');*/
            
        });
        
        //sets the values
        $('#selectSubDataFromListModal').find("#subDataListEntryDesc").text(getSubDataLabel(type));
        //('#selectSubDataFromListModal').modal('show');
    });
}
/**
 * Handles the click on the button that selects a subdata entry
 * @param {type} element - html element that was clicked
 * @param {type} propertyValue - index of the property 
 */
function selectSubDataEntry(element, propertyValue){
    //select the subdata entry name in order to put it into the Value2 textarea
    var texto = $(element).parents("td").siblings(":first").text();    
    $('#selectSubDataFromListModal').modal("hide");            
    //updates the data stored in the testcase.jsp page
    $("#properties_value2_" + propertyValue).prop("value", texto);
}
/*****************************************************************************/
/**
 * Set the entry name selected in the textarea "Value1"
 * @param {type} button -- button selected 
 * @param {type} propertyValue -- index of the property for which we want to apply the entry
 * @returns {undefined}
 */
function selectEntry(button, propertyValue){
    //select the entry name in order to put it into the Value1 textarea
    var texto = $(button).parents("td").siblings(":first").text();
    //gets the id of the selected row
    var entryID = $(button).parents("tr").prop("id");    
    //gets the type of the row
    var entryType = $("#" + entryID + " td:nth-child(7)").text();
    
    //clears the table //TODO:FN check this
    var table = $('#listOfTestDataLib').DataTable();
    table.destroy();
    $('#selectEntryFromListModal').modal("hide");
    
    //updates the data stored in the testcase.jsp page
    $("#properties_value1_" + propertyValue).prop("value", texto);
    //hidden information: entry id and entry type
    $("#testDataLibID_" + propertyValue).prop("value", entryID);
    $("#testDataLibType_" + propertyValue).prop("value", entryType);
}
/**
 * Creates a table that shows the list of entries
 * @param {type} propertyValue
 * @returns {undefined}
 */
function createEntriesTable(propertyValue) {
    
    var oTable = $('#listOfTestDataLib').dataTable({
        "dom": 'C<"clear">lfrtip',
        "bServerSide": true,
        "sAjaxSource": "GetTestDataLib",
        "sAjaxDataProp": "TestDataLib",
        "bJQueryUI": true,
        "bProcessing": true,
        "bPaginate": true,
        "bLengthChange": false,
        "bAutoWidth": false,
        "sPaginationType": "full_numbers",
        "bSearchable": false,
        "aTargets": [0],
        "iDisplayLength": 10,
        "scrollX": true,
        "aoColumns": [
            {"sName": "TestDataLibID",
                "bSearchable": false,
                "bSortable": false,
                "mRender": function(data, type, oObj) {
                   
                    var selectElement = '<button id="selectEntry' + data + '"  onclick="selectEntry(this, '+ propertyValue  +');" \n\
                                            class="selectEntry btn btn-default btn-xs margin-right5" \n\
                                        name="editTestDataLib" title="Select entry" type="button">\n\
                                        <span class="glyphicon glyphicon-hand-up"></span></button>'; 

                    return '<div class="btn-group center" style="width: 50px">' + selectElement + '</div>';
                },
            },
            {"sName": "Name"},
            {"sName": "System"},
            {"sName": "Environment"},
            {"sName": "Country"},
            {"sName": "Group"},
            {"sName": "Type"},
            {"sName": "Database"},
            {"sName": "Script"},
            {"sName": "ServicePath"},
            {"sName": "Method"},
            {"sName": "Envelope"},
            {"sName": "Description"},
        ],  
    }).makeEditable({
        "aoColumns": [
            null, null, null, null,
            null, null, null, null,
            null, null, null, null]
    }); 
}