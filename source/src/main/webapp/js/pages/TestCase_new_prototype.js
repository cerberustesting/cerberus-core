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
    $(function () {
       initTestCasePage(); 
    }); 
});


function initTestCasePage(){
        var doc = new Doc(); 

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displaySearchArea(doc);
//
//    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
//    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));
//
//    //set translations for modals
//    displayCreateTestDataLibLabels(doc);   
//    displayUpdateTestDataLibLabels(doc);
//    displayManageTestDataLibDataLabels(doc);
//    displayListTestCasesLabels(doc);    
//    displayListTestDataLibDataLabels(doc);
    
    //load search area translations
    //$("#title")
    
    //get systems for which user has access 
    var user = getUser();
    console.log(user);
    console.log(user.system);
    //get url parameters
    
    var system = GetURLParameter("system");
    
    //by default selects the value that is in my system
    loadSystemsByUser($("#searchArea"), user.system, system);
    
    //TODO:FN  traduzir
    /*
    
   //translations
    //<label id="lbl_system" 
    //<label id="lbl_test" 
    ////<label id="lbl_testcase" 
    //<label id="lbl_details">Details</label>
    //<label id="lbl_steps">Steps</label>
    //<label id="lbl_properties">Properties</label>
    //<button type="button" id="btnResetSearch" class="btn btn-default"> Reset Filters </button>
    //<button type="button" id="btnLoadTC" class="btn btn-primary"> Load </button>
     * 
     */
    
    /*bindToggleCollapse("#tcHeader");
    bindToggleCollapse("#tcSteps");
    bindToggleCollapse("#tcProperties");*/
    
    //if URL contains parameters then load the test, otherwise does not load
    displayFooter(doc);
}



function displaySearchArea(doc){
    
    //define handlers for select options
    
    $("#system").change(systemChangeHandlder);
    $("#test").change(testChangeHandler);
    $("#testcase").change(testCaseChangeHandler);
    
    //TODO:FN check for translations - use doc
    $("#btnLoadTC").click(loatTestCaseClickHandler);
    $("#btnResetSearch").click(resetSearchAreaClickHandler);
    
}

/* handlers for buttons in search area*/
function loatTestCaseClickHandler(){
    //TODO:FN loads test case + selects the test case that matches the 
    
    
    
}

function resetSearchAreaClickHandler(){
    //TODO:FN cleares all inputs in the search areaform
}



function loadSystemsByUser(parent, data, systemURL) {
    loadSelectElement(data, parent.find("#system"), false);
    parent.find("#system").multiselect({
        maxHeight: 150,
        checkboxName: 'system',
        buttonWidth: '100%',
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-system',
    });
    console.log("default system " + $("#MySystem").val());
    
    //if the systemURL is not in the list of systems that the user has access
    //then it will ignored
    var userHasAccess = ($.inArray(systemURL, data)) >= 0;
    
    //selects the value
    if(systemURL !== null && userHasAccess){
        $("#system").find("option[value='" + systemURL + "']").prop("selected", true);
    }else{
        //selects the default mysystem
        $("#system").find("option[value='"+$("#MySystem").val()+"']").prop("selected", true);
    }
    
}

function systemChangeHandlder(){
    //loads the tests avaliable for the system selected
    
}

function testChangeHandler(){
    //loads the test cases avaliable for the system selected    
}

function testCaseChangeHandler(){
    //activates the load button - user can load the test case data
}