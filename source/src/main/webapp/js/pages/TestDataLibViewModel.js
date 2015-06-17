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
$(function(){
    refreshTestDataLib(); 
    /**
    * Loads the common functions from a global javascript file
    * @param {type} param1 - filename
    */
   $.getScript("js/pages/global.js");
     
    var i = 0;
    var j = 0;
    /*****************************************************************************/
    //adds new rows to the subdata table
    $("#newSubData_addRow").click(function() {
 
        $('#addSubDataTableBody').append('<tr class="trData" id="row' + (i + 1) + '">\n\\n\
            <td ><div class="nomarginbottom marginTop5"> <button onclick="deleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"   /></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs"  /></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs"  /></div></td>\n\
            \n\
        </tr>');
        i++; 
        updateSubDataTabLabel();
    });
    /*****************************************************************************/
    //adds a new run in the edit window
    $("#editSubData_addRow").click(function() { 
        $('#editSubDataTableBody').append('<tr class="trData" id="row' + (j + 1) + '" data-operation="insert">\n\\n\
            <td ><div class="nomarginbottom marginTop5"> <button onclick="editDeleteRowTestDataLibData(this)" class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="subdata" type="text" class="subDataClass form-control input-xs"   /></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs"  /></div></td>\n\
            <td><div class="nomarginbottom form-group form-group-sm"><input name="description" value="" type="text" class="descriptionClass form-control input-xs"  /></div></td>\n\
            \n\
        </tr>');
        j++;
    });
    
    /*****************************************************************************/
    //delete all subdata rows     
    $("#newSubData_deleteAll").click(function() {
        removeAllEntries("addSubDataTable");
        updateSubDataTabLabel();
    });
    
    /*****************************************************************************/
    //TODO:FN comment
    $("#editSubData_deleteAll").click(function() {
        removeAllEntries("editSubDataTable");
    });
    
    /*****************************************************************************/
    /**
     * Handles the click to save the test data lib entry
     */
    $("#saveTestDataLib").on("click", function(){
        
        var formEdit = $('#editTestDataLibModal').find('form#editTestLibData');
        
        $.when($.post("UpdateTestDataLib", formEdit.serialize(), "json")).then(function(data) {
            if(data.messageType === "success"){
                var oTable = $("#listOfTestDataLib").dataTable();
                oTable.fnDraw(true)
                $('#editTestDataLibModal').modal('hide');
                showMessage(data);
                
            }else{
                showMessage(data, $('#editTestDataLibModal'));
            }
        });
        
    });
    /*****************************************************************************/
    /**
    * Disables the group text box when the users selects an existing group
    */
    $("#Group").change(function(){        
        if($(this).val() !== ''){
            $(this).removeClass("emptySelectOption");
        }else{
            $(this).addClass("emptySelectOption");
        }
        var option = $(this).find("option:selected").val();
        if(option !== ""){
            $("#GroupInput").prop("disabled", "disabled");
            $("#GroupInput").prop("name", "GroupDisabled");
            $(this).prop("name", "Group");
        }else{
             $("#GroupInput").removeAttr("disabled");
             $("#GroupInput").prop("name", "Group");
             $(this).prop("name", "GroupDisabled");
        }                
    });
   /*****************************************************************************/
   //TODO:FN refactoring
   /**
    * Disables the group text box when the users selects an existing group
    */
   $("#GroupEdit").change(function(){        
       
        if($(this).val() !== ''){
            $(this).removeClass("emptySelectOption");
        }else{
            $(this).addClass("emptySelectOption");
        }
        
        
        var option = $(this).find("option:selected").val();
        console.log("ONCHANGE WAS LOADED");
        if(option !== ""){
            $("#GroupEditInput").prop("disabled", "disabled");
            $("#GroupEditInput").prop("name", "GroupDisabled");
            $(this).prop("name", "GroupEdit");
       
        }else{
             
             $("#GroupEditInput").removeAttr("disabled");
             $("#GroupEditInput").prop("name", "GroupEdit");
             $(this).prop("name", "GroupDisabled");
        }                
        
    });
    
    /*****************************************************************************/
    //TODO:FN common file?
    $("[data-hide]").on("click", function(){
        $(this).closest("." + $(this).attr("data-hide")).hide();
    });
    
    /*****************************************************************************/    
    /*
     * Handles the change of the type when adding a new test data lib entry
     */    
    $('#addTestDataLibModal').find("#Type").change(function (){
        //console.log("area " + $('#addTestDataLibModal').find("#Type option:selected").val());
        refreshSpecificAreas();
    });
    
    /**
     * Removes all rows when the modal window is hidden and clears the message
     */
    $('#addTestDataLibModal').on('hidden.bs.modal', function () {
        $('#addSubDataTableBody tr').remove();
        clearResponseMessage($('#addTestDataLibModal'));
    });
    
    /**
    * Method that saves new test data lib entry
    */
    $("#saveSubData").click(function() { 

        //shows the modal that allows the creation of test data lib 
       var formAdd = $('#addTestDataLibModal').find("#addTestDataLibModalForm");
       
       var nameElement = formAdd.find("#Name");
       //validates if the property name is not empty
       if(nameElement.prop("value") === ''){
           var localMessage = new Message("danger", "Please specify the name of the entry!");
           nameElement.parents("div.form-group").addClass("has-error");
           showMessage(localMessage, $('#addTestDataLibModal'));                   
           return;
       }
       //validates if there are sub data entries with no name defined
       
       
       $.post("AddTestDataLib",  formAdd.serialize(), function(data){
            if(data.messageType === 'success'){
                var oTable = $("#listOfTestDataLib").dataTable();
                //redraws table and goes to last page
                //oTable.fnDraw(true);
                //It is possible to go directly to the last page because that is order by id
                oTable.fnPageChange( 'last' );
                showMessage(data);
                $('#addTestDataLibModal').modal('hide');    
            }else{
               showMessage(data, $('#addTestDataLibModal'));
            }
        });

    });
    /*****************************************************************************/
     /***
     * Removes all rows when the modal window is hidden and clears the message
     */
    $('#myModal').on('hidden.bs.modal', function () {
        $('#editSubDataTableBody tr').remove();
        clearResponseMessage($('#myModal'));
    });
    /*****************************************************************************/
    /**
    * Removes all rows when the modal window is hidden and clears the message
    */
    $('#editTestDataLibModal').on('hidden.bs.modal', function () {
        $('#editTestDataLibModal #GroupEditInput').prop("value", "");

        clearResponseMessage($('#editTestDataLibModal'));
    });
    /*****************************************************************************/
    /**
     * Save changes performed in the subdata list
     */
    $("#saveChangesSubData").click(function(){
        //clears the current messages before each click on save
        clearResponseMessage($("#myModal"));
        
        var dataArray = {};
        var removeObjects = [];
        var updateObjects = [];
        var insertObjects = [];
        
        //client-side validation -- TODO:FN colocar em ficheiro comum
        var elementsWithoutSubdata = $("#editSubDataTableBody tr td:nth-child(2) input").filter(function(){
            if(this.value === ""){
                $(this).parents("div.form-group").addClass('has-error');
            }else{
                $(this).parents("div.form-group").removeClass('has-error');
            }
            return this.value ===  "";   
        }).size();
        
        
        
        if(elementsWithoutSubdata >0 ){
            console.log("Element s without subdataname" + elementsWithoutSubdata);
            var localMessage = new Message("danger", "You have entries without subdata name. Please check the subdata entries.");
            showMessage(localMessage, $("#myModal"));
            return;
        }
        
        //var totalToRemove = $("#editSubDataTableBody").find("tr[data-remove='yes']").size();
        //selects the elements that were marked as to remove
        $("#editSubDataTableBody tr[data-operation='remove']").each(function(){
            var item = {};
            var subData = $(this).find("td:nth-child(2) input").prop("value");
            item ["Subdata"] =  subData;
            removeObjects.push(item);
        });
        //gets the elements that will be updated
        $("#editSubDataTableBody tr[data-operation='update']").each(function(){
            var item = {};
            item ["Subdata"] = $(this).find("td:nth-child(2) input").prop("value");
            item ["Value"] = $(this).find("td:nth-child(3) input").prop("value");
            item ["Description"] = $(this).find("td:nth-child(4) input").prop("value");
            updateObjects.push(item);             
        });
        
        var resultInsert = true;
        //gets the elements that should be inserted
        $("#editSubDataTableBody tr[data-operation='insert']").each(function(){
            var item = {};
            item ["Subdata"] = $(this).find("td:nth-child(2) input").prop("value");
            item ["Value"] = $(this).find("td:nth-child(3) input").prop("value");
            item ["Description"] = $(this).find("td:nth-child(4) input").prop("value");
            var foundRepeated = false;
            
            //check if is defined in the insert objects
            for(var j in insertObjects){
                if(insertObjects[j]["Subdata"] === item ["Subdata"]){
                    //the user is trying to insert entries with the same name
                    $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                    foundRepeated = true;
                }
            }
            
            //check if is defined in the edit objects
            for(var i in updateObjects){
                if(updateObjects[i]["Subdata"] === item ["Subdata"]){
                    //the user is trying to insert entries with the same name
                    $(this).find("td:nth-child(2) div.form-group").addClass('has-error');
                    foundRepeated = true;
                }
            }
            
            var foundRowToRemove = false;
            var rowToRemove = null;
            for(var i in removeObjects){
                if(removeObjects[i]["Subdata"] === item ["Subdata"]){
                    //the user is trying to insert an entry that already exists and was marked to be removed, 
                    //an update should be performed instead
                    foundRowToRemove = true;
                    rowToRemove = removeObjects[i];                 
                    break;
                }                 
            }
            
            var istoUpdate = false;
            if(foundRowToRemove){
                var index = removeObjects.indexOf(rowToRemove);
                
                if(index > -1){
                    removeObjects.splice(index, 1);
                    istoUpdate = true;                    
                }                
            }
            
            if(foundRepeated){
                return resultInsert = false;
            }else{
                if(istoUpdate){
                    updateObjects.push(item);                 
                }else{
                    insertObjects.push(item);                 
                }

            }
        });

        if(!resultInsert){
            var localMessage = new Message("danger", "You have entries with duplicated names.");
            showMessage(localMessage, $("#myModal"));
            return ;    
        }
        
        if(removeObjects.length > 0){
            dataArray["remove"] = removeObjects;
        }
        if(updateObjects.length > 0){
            dataArray["update"] = updateObjects;
        }
        if(insertObjects.length > 0){
            dataArray["insert"] = insertObjects;
        }
 

        
        //console.log("edit/insert " + editInsertObjects);
        var testDataLibID = $('#myModal').find("#testDataLibID").attr("value");
        var testDataLibType = $('#myModal').find("#testDataLibType").attr("value");
     
        $.post("UpdateTestDataLibData", {id: testDataLibID, type: testDataLibType, data: JSON.stringify(dataArray)},"json").done(function(data) {            
            if(data.messageType === 'success'){
                showMessage(data);
                $('#myModal').modal('hide');
            }else{            
                showMessage(data, $('#myModal'));
            }            
        });

    });
   
     
    
    var system = $('#MySystem').val();
       
    /*****************************************************************************/
    /**
    * Handles the click on the create new lib
    */
     $('#testdatalib #createLibButton').click(function() { 
        //loads the type of data for the window
        //Data that we want to retrieve
        var params = {};
        params[0] = "TESTDATATYPE";
        params[1] = "SYSTEM";
        params[2] = "ENVIRONMENT";
        params[3] = "Country";
        
        getInvariantListN(params, function(data){
            //TESTDATATYPE
            loadTypes(data["TESTDATATYPE"]);             
            //SYSTEM
            loadSystems(data["SYSTEM"]);
            //ENVIRONMENT
            loadEnvironments(data["ENVIRONMENT"]);            
            //Country
            loadCountries(data["Country"]);
                

        });
        //shows the modal that allows the creation of test data lib 
        $('#addTestDataLibModal').modal('show');            
      
     
        
   });   
});




function refreshSpecificAreas() {
    //shows / hides the test data çob
    //var selectedType = $('#Type option:selected').val();
    var selectedType = $('#addTestDataLibModal').find('#Type option:selected').val();
    
    
    $('#addTestDataLibModal').find('[id = "panel' + selectedType + '"][name="panelData"]').css("display", "block");
    $('#addTestDataLibModal').find('[id != "panel' + selectedType + '"][name="panelData"]').css("display", "none");

    //shows hide the comboboxes
     $('#addTestDataLibModal').find('[id = "area' + selectedType + '"][name="groupArea"]').css("display", "block");
     $('#addTestDataLibModal').find('[id != "area' + selectedType + '"][name="groupArea"]').css("display", "none");

    var labelEntry = 

    
    getGroupData(selectedType, function(data){
        loadSelectElement(data["groupList"], $('#addTestDataLibModal').find("#Group"), true, '-- select to enter mannually new group --');
        $('#addTestDataLibModal').find('#Group option:first').addClass("emptySelectOption");
        $('#addTestDataLibModal').find('#Group').change();  
    });
    
    if(selectedType === "SQL"){
        //loads the information for the databases
        getInvariantList("PROPERTYDATABASE", function(invariantData){
            loadSelectElement(invariantData, $('#addTestDataLibModal').find("#Database"), true, ''); 
        });        
    }
    
    $('#addTestDataLibModal').find("#labelSubdataEntry").html(labelEntry);
}

function getGroupData(selectedType, handleData){
    $.when($.getJSON("GetTestDataLib", "action=getListOfGroupsPerType&Type="+selectedType)).then(function(data) {
        handleData(data);
    });
}

function deleteRowTestDataLibData(element) {
    deleteRow(element);
    updateSubDataTabLabel();
}

function deleteRow(element){
    $(element).parents("tr").remove();
}

function updateSubDataTabLabel(){
    $('#tab2Text').text("Sub data (" + $('#addSubDataTable tr[class="trData"]').size()+ " entries)");
}
function editDeleteRowTestDataLibData(element){  
    //if is a new record then we know that is to remove from the interface
    if($(element).parents("tr").attr("data-operation") === 'insert'){
        deleteRow(element);
    }else if($(element).parents("tr").attr("data-operation") === 'remove'){
        //the line was loaded from the database, then it should be market to be removed
        $(element).prop("title", "Mark item to be removed from the database");
        $(element).parents("tr").attr("data-operation", "update"); 
        $(element).find("span:first").removeAttr("class").addClass("glyphicon glyphicon-trash")            
    }else{
        $(element).prop("title", "This element will be removed from the database");
        $(element).parents("tr").attr("data-operation", "remove");
        $(element).find("span:first").removeAttr("class").addClass("glyphicon glyphicon-remove colorRed");  
    }          
}
 

 



function removeAllEntries(tableID){
    removeRows(tableID);
}
 
 function removeRows(tableID){
     $('#'+tableID+' tr[class="trData"]').remove();        
 }
 
 
 
 
/*************************** TestDataLib ******************************/
function deleteTestDataLib(testDataLibID){
    console.log("id para eliminar " + testDataLibID);
    //configuration for delete modal
    $('#confirmationModalLabel').html("Confirmation - Delete");
    $('#modalBody').html("Do you want to delete this record?");
    
    
    $("#confirmOk").on("click", function(){
        $.post("DeleteTestDataLib", {action: "delete", id: testDataLibID}, "json").done(function(data) {
            if (data.messageType === "success") {
                //redraw the datatable
                var oTable = $("#listOfTestDataLib").dataTable();
                oTable.fnDraw(true);
            }
            //show message 
            showMessage(data, null);
            //close confirmation window
            $('#confirmationModal').modal('hide');
        });
    });
    
    $('#confirmationModal').modal('show');
       
}

function editTestDataLib(testDataLibID){
   //load the data from the row 
   
    $.when($.getJSON("GetTestDataLib", "action=findTestDataLibByID&testDataLib=" + testDataLibID)).then(function(data) {
       
//        if(data.messageType === 'success'){
            var obj = data["testDataLib"];
            console.log(obj);             
            var formEdit = $('#editTestDataLibModal');
             formEdit.find('#testDataLibIDEdit').prop("value", testDataLibID) ; 
             
            formEdit.find('input[name="NameEdit"]').prop("value", obj[1]) ;
            formEdit.find('input[name="TypeEdit"]').prop("value", obj[6]) ;
            
            formEdit.find('#NameEdit').text(obj[1]) ;
            formEdit.find('#TypeEdit').text(obj[6]) ;
            //specify the system+environment+country information
            var systemText = obj[2] === "" ? "All" : obj[2];
            var environmentText = obj[3] === "" ? "All" : obj[3];
            var countryText = obj[4] === ""  ? "All" : obj[4];
            formEdit.find('#AvailableTextSystem').text(systemText) ;
            formEdit.find('#AvailableTextEnvironment').text(environmentText) ;
            formEdit.find('#AvailableTextCountry').text(countryText) ;
             
            
            //hide the areas that are not relevant
            formEdit.find('[id = "panel' + obj[6]  + 'Edit"][class="panelData"]').css("display", "block");
            formEdit.find('[id != "panel' + obj[6]  + 'Edit"][class="panelData"]').css("display", "none");
             
            if(obj[6] === "SOAP"){
                //loads the information for soap entries
                formEdit.find('#ServicePathEdit').prop("value", obj[9]) ;
                formEdit.find('#MethodEdit').prop("value", obj[10]) ;
                formEdit.find('#EnvelopeEdit').prop("value", obj[11]) ;
            }if(obj[6] === "SQL"){
                //loads the information for sql entries
                getInvariantList("PROPERTYDATABASE", function(invariantData){
                    loadSelectElement(invariantData, formEdit.find("#DatabaseEdit"), true, '');
                    formEdit.find('#DatabaseEdit option[value="'+ obj[7] + '"]:first').prop("selected", "selected") ;
                });
                formEdit.find('#ScriptEdit').prop("value", obj[8]) ;
            }
            
            //loads the group data and selects the one that was inserted by the user
            getGroupData(obj[6], function(data){
                loadSelectElement(data["groupList"], $('#editTestDataLibModal').find("#GroupEdit"), true, '-- select to enter mannually new group --');
                //selects the group entered by the user
                
                formEdit.find('#GroupEdit option[value="'+ obj[5] + '"]:first').prop("selected", "selected") ;
                formEdit.find('#GroupEdit option:first').addClass("emptySelectOption");
                formEdit.find('#GroupEdit').change();                
                
            });
            
            formEdit.find('#EntryDescriptionEdit').prop("value", obj[12]) ;
           
            formEdit.modal('show');    
//        }
    });
    
}
 
 
 
function appendNewSubDataRow(rowId, subdata, data, description){
    //for each subdata entry adds a new row
        $('#editSubDataTableBody').append('<tr id="'+ rowId +'" data-operation="update"> \n\
        <td><div class="nomarginbottom marginTop5"> \n\
        <button title="Mark item to be removed from the database" onclick="editDeleteRowTestDataLibData(this)" \n\
class="delete_row pull-left btn btn-default btn-xs manageRowsFont"><span class="glyphicon glyphicon-trash"></span></button></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
        <input readonly="readonly" name="subdata" type="text" class="subDataClass form-control input-xs" value="'+ subdata +'"/><span></span></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="data" type="text" class="dataClass form-control input-xs" value="'+data+'" /></div></td>\n\
        <td><div class="nomarginbottom form-group form-group-sm"><input name="description" type="text" class="descriptionClass form-control input-xs" value="'+description+'" /></div></td></tr>');
    
}
 
function editSubData(testDataLibID){
    var type = $("#" + testDataLibID + " td:nth-child(7)").text();
    
    $.when($.getJSON("GetTestDataLib", "action=findAllTestDataLibContent&testDataLib=" + testDataLibID + "&type=" + type)).then(function(result) {
         
        $.each( result["TestDataLibDatas"] , function(idx, obj){ 
            var testDataLibID = obj[0];
            var subdata = obj[1];
            var data = obj[2];
            var description = obj[3]; 
            
            appendNewSubDataRow((testDataLibID + subdata), subdata, data, description);
            
        });
        
        //sets the values
        $('#myModal').find("#labelSubdataEntryDesc").text(getSubDataLabel(type));
        $('#myModal').find("#testDataLibID").attr("value", testDataLibID);
        $('#myModal').find("#testDataLibType").attr("value", type);
        $('#myModal').modal('show');
    });
    
    
    
    

} 
 

 


//https://datatables.net/examples/api/show_hide.html

 

function loadSystems(data){
    loadSelectElement(data, $('#addTestDataLibModal').find("#System"), true, "All");
} 
function loadEnvironments(data){
    loadSelectElement(data, $('#addTestDataLibModal').find("#Environment"), true, "All");
} 
function loadCountries(data){
    loadSelectElement(data, $('#addTestDataLibModal').find("#Country"), true, "All");
} 



function loadTypes(data) {
    loadSelectElement(data, $('#addTestDataLibModal').find("#Type"));
    refreshSpecificAreas();
}

function loadSelectElement(data, element, includeEmpty, includeEmptyText){
    $(element).empty();
    if(includeEmpty !== null && includeEmpty){
       $(element).append("<option value=''>" + includeEmptyText + "</option>");                
    }
    $.each(data, function(idx, obj){ 
       $(element).append("<option value='" + obj + "'>"+obj+"</option>");               
    });
    
    $(element).find('option:first-child').attr("selected", "selected");
}

 
 
 
 
//TODO:FN passar isto para ficheiro comum
function getInvariantList(idName, handleData){
    $.when($.getJSON("GetInvariantList", "idName="+idName)).then(function(data) {
        handleData(data);
    });
}
//TODO:FN mudar o nome do metodo
function getInvariantListN(list, handleData){
    $.when($.post("GetInvariantList", {action: "getNInvariant", idName: JSON.stringify(list)}, "json")).then(function(data) {
        handleData(data);
    });
}
 
function refreshTestDataLib() { //TODO:FN check the search option.....
    var oTable = $('#listOfTestDataLib').dataTable({
        "dom": 'C<"clear">lfrtip',
        //"aaSorting": [[1, "asc"]],
        "bServerSide": true,
        "sAjaxSource": "GetTestDataLib",
        "sAjaxDataProp": "TestDataLib",
        "bJQueryUI": true,
        "bProcessing": true,
        "bPaginate": true,
        "bAutoWidth": false,
        "sPaginationType": "full_numbers",
        "bSearchable": false,
        "aTargets": [0],
        "iDisplayLength": 25,
        "scrollX": true,
         bStateSave: true, 
        "aoColumns": [
            {"sName": "TestDataLibID",
                //"bSearchable": false,
                "bSortable": false,
                "mRender": function(data, type, oObj) {

                    var editElement = '<button id="editTestDataLib' + data + '"  onclick="editTestDataLib(\'' + data + '\');" \n\
                            class="editTestDataLib btn btn-default btn-xs margin-right5" \n\
                        name="editTestDataLib" title="Edit entry" type="button">\n\
                        <span class="glyphicon glyphicon-pencil"></span></button>';

                    var deleteElement = '<button onclick="deleteTestDataLib(' + data + ');" class="btn btn-default btn-xs margin-right5 " \n\
                        name="deleteTestDataLib" title="Delete entry" type="button">\n\
                        <span class="glyphicon glyphicon-trash"></span></button>';

                    var viewDataElement = '<button  class="editTestDataLib btn  btn-primary btn-xs margin-right25" \n\
                        name="editTestDataLib" title="Edit subdata entries" type="button" onclick="editSubData(' + data + ')">\n\
                        <span class="glyphicon glyphicon-list-alt"></span></button>';


                        return '<div class="btn-group center" style="width: 125px">' + viewDataElement + editElement + deleteElement + '</div>';
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
    $("#listOfTestDataLib_wrapper div.ColVis").
            before("<button id='createLibButton' type='bytton' class='btn btn-default'>Create new lib</button>").
            after("<button id='importDataButton' type='bytton' class='btn btn-default'>Import from file</button>");
    
    
    //TODO:FN delete
    /*$('#listOfTestDataLib').DataTable().search(function(){
        console.log("pesquisa");
    });*/
    /*
     // http://stackoverflow.com/questions/5548893/jquery-datatables-delay-search-until-3-characters-been-typed-or-a-button-clicke
    $('.dataTables_filter input')
        .unbind('keypress keyup')
        .bind('keypress keyup', function(e){
          if ($(this).val().length < 3 && e.keyCode != 13) return;
      oTable.fnFilter($(this).val());
    });*/
}
                