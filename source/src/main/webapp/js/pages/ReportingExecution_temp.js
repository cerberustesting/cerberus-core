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
    $(document).ready(function () {
        initPage();
    });
});

function initPage(){
    var doc = new Doc();

    displayHeaderLabel(doc);
    /*displayPageLabel(doc);*/
    displayFooter(doc);
    //create handlers for buttons
    $("#searchExecutionsButton").click(searchExecutionsClickHandler);
    $("#resetButton").click(resetClickHandler);
    $("#selectFiltersButton").click(selectFiltersClickHandler);
    $("#setFiltersButton").click(setFiltersClickHandler);
    
    loadFilters();
    //$("#exportList").change(controlExportRadioButtons);
    //loadSummaryTableOptions();
}

function parseReportingFavoriteURLFormat(favorite){
    console.log("Favorite " + favorite);
    var res = favorite.split("&");
    var favoriteObj = {};
    
    var country = [];
    var project = [];
    var tcactive = [];
    var tcstatus = [];
    var group = [];
    var environment = [];
    var priority = [];
    var browser = [];
    
    $.each(res, function(idx, obj){
        if(obj.indexOf("Ip=") > -1){
            favoriteObj["ip"] = obj.replace("Ip=", "");
        }else if(obj.indexOf("Port=") > -1){
            favoriteObj["port"] = obj.replace("Port=", "");
        }else if(obj.indexOf("Tag=") > -1){
            favoriteObj["tag"] = obj.replace("Tag=", "");
        }else if(obj.indexOf("Comment=") > -1){
            favoriteObj["comment"] = obj.replace("Comment=", "");
        }else if(obj.indexOf("BrowserFullVersion=") > -1){
            favoriteObj["browserfullversion"] = obj.replace("BrowserFullVersion=", "");
        }else if(obj.indexOf("Country=") > -1){
            country.push(obj.replace("Country=", ""));
        }else if(obj.indexOf("Project=") > -1){
            project.push(obj.replace("Project=", ""));
        }else if(obj.indexOf("TcActive=") > -1){
            tcactive.push(obj.replace("TcActive=", ""));
        }else if(obj.indexOf("Status=") > -1){
            tcstatus.push(obj.replace("Status=", ""));
        }else if(obj.indexOf("Group=") > -1){
            group.push(obj.replace("Group=", ""));
        }else if(obj.indexOf("Environment=") > -1){
            environment.push(obj.replace("Environment=", ""));
        }else if(obj.indexOf("Priority=") > -1){
            priority.push(obj.replace("Priority=", ""));
        }else if(obj.indexOf("Browser=") > -1){
            browser.push(obj.replace("Browser=", ""));
        }
        
    });
    
    //filters with more than one value
    favoriteObj["country"] = country;
    favoriteObj["project"] = project;
    favoriteObj["tcactive"] = tcactive;    
    favoriteObj["tcstatus"] = tcstatus;
    favoriteObj["group"] = group;
    favoriteObj["environment"] = environment;
    favoriteObj["priority"] = priority;
    favoriteObj["browser"] = browser;

    return favoriteObj;
}

function loadFilters(){
   
    var user = JSON.parse(sessionStorage.getItem("user"));
    var favorite = user.reportingFavorite;
    //check if the reporting favorite is json 
    var favoriteJSON = {};
    
    if(favorite !== ''){
        try {
            favoriteJSON = JSON.parse(favorite);
        } catch (e) {
            //it is not json, so we need to parse all data retrieved
            favoriteJSON = parseReportingFavoriteURLFormat(favorite);
        }
    }        
    
   
    var system = $("#MySystem").val();
    
    //loads all invariants 
     var jqxhr = $.getJSON("ReadInvariant", "");
    $.when(jqxhr).then(function(data) {
        //var systemsList = [];
        var environmentList = [];
        var countryList = [];
        var browserList = [];
        var priorityList = [];
        var tcStatusList = [];
        var groupList = [];
        var activeList = [];
        var executionStatusList = [];
        $.each(data["contentTable"], function(idx, obj) {
            //extract all invariants that are needed for the page
            var element = {value: obj.value, description: obj.description};
            /*if (obj.idName === 'SYSTEM') {
                systemsList.push(obj.value);
            } else */
            if (obj.idName === 'ENVIRONMENT') {
                environmentList.push(obj.value);
            } else if (obj.idName === 'COUNTRY') {
                if(favoriteJSON.hasOwnProperty("country") && favoriteJSON.country.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
                countryList.push(element);
            } else if(obj.idName === 'BROWSER'){
                browserList.push(element);
                if(favoriteJSON.hasOwnProperty("browser") && favoriteJSON.browser.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }else if(obj.idName === 'PRIORITY'){                
                priorityList.push(element);
                if(favoriteJSON.hasOwnProperty("priority") && favoriteJSON.priority.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }else if(obj.idName === 'TCSTATUS'){                
                tcStatusList.push(element);
                if(favoriteJSON.hasOwnProperty("tcstatus") && favoriteJSON.tcstatus.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }else if(obj.idName === 'GROUP'){                
                groupList.push(element);
                if(favoriteJSON.hasOwnProperty("group") && favoriteJSON.group.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }else if(obj.idName === 'TCACTIVE'){                
                activeList.push(element);
                if(favoriteJSON.hasOwnProperty("tcactive") && favoriteJSON.tcactive.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }else if(obj.idName === 'TCESTATUS'){
                executionStatusList.push(element);
                if(favoriteJSON.hasOwnProperty("tcestatus") && favoriteJSON.tcestatus.indexOf(obj.value) >-1 ){
                    element["checked"] = "checked";                    
                } 
            }
            
        });
        
        //loadSystems(systemsList);
        //checkboxes
        loadCountryFilter(countryList);
        loadBrowserFilter(browserList);
        loadPriorityFilter(priorityList);
        loadActiveFilter(activeList);
        loadTCStatusFilter(tcStatusList);
        loadGroupFilter(groupList);
        loadTestCaseExecutionStatus(executionStatusList);
        
        //text
        console.log("fav" + favoriteJSON);
        //comment
        loadTextComponent("#comment", favoriteJSON.comment);
        //ip
        loadTextComponent("#ip", favoriteJSON.ip);
        //port
        loadTextComponent("#port", favoriteJSON.port);
        //tag
        loadTextComponent("#tag", favoriteJSON.tag);
        //browser version
        loadTextComponent("#browserfullversion", favoriteJSON.browserfullversion);
        
        //multiselect components
        loadEnvironments(environmentList);
        readAndLoadApplication(system);
        readAndLoadTest(system);
    });
    readAndLoadCreatorImplementer();
    readAndLoadTargetRevisionAndSprint(system);
    readAndLoadProject();

}

function loadTextComponent(element, text){
    console.log(text)
    if(Boolean(text) && text.trim() !== ''){
        $(element).attr("value", text);
    }
}

function readAndLoadTest(selectedSystem){
    var jqxhr = $.getJSON("ReadTest", "system=" + selectedSystem);
    var testList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj){
            //loads all the tests retrieved 
            var element = {test: obj.test, description: obj.description};
            testList.push(element);
        });
        loadTests(testList);        
    });
}
/*function readAndLoadTest(selectedSystems){
    var parameters = "";
    if(selectedSystems.length > 0){
        parameters = "system=" + selectedSystems.join("&system="); 
    }
    
    var jqxhr = $.getJSON("ReadTest", parameters);
    var testList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj){
            //loads all the tests retrieved 
            var element = {test: obj.test, description: obj.description};
            testList.push(element);
        });
        loadTests(testList);        
    });
*/

/*function readAndLoadApplication(selectedSystems){
    var jqxhr = $.getJSON("ReadApplication", "");
    var applicationsList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj){
            //if the application belongs to one of the selected system
            //or if the systems are not filtered then add to the list of applications
            if((selectedSystems.length !== 0 && selectedSystems.indexOf(obj.system) > -1) || selectedSystems.length === 0){ 
                //console.log("obj.application " + obj.application);
                var element = {application: obj.application, system: obj.system};
                applicationsList.push(element);
            }
        });
        loadApplications(applicationsList);
        
    });
}*/
function readAndLoadApplication(selectedSystem){
    var jqxhr = $.getJSON("ReadApplication", "system=" + selectedSystem);
    var applicationsList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj){
            var element = {application: obj.application, system: obj.system};
            applicationsList.push(element);

        });
        loadApplications(applicationsList);
        
    });
}

function readAndLoadProject(){
    var jqxhr = $.getJSON("ReadProject", "");
    
    $.when(jqxhr).then(function(data) {
        var projectList = [];
        
        $.each(data["contentTable"], function(idx, obj){
            var element = {idProject: obj.idProject, description: obj.description};
            projectList.push(element);
        });
        loadProjects(projectList);
    });
}

function readAndLoadCreatorImplementer(){
    //loads the creator and implementer
    var jqxhr = $.getJSON("ReadUser", "");
    $.when(jqxhr).then(function(data) {
        //console.log("data " + data["contentTable"]);
        var loginList = [];
        $.each(data["contentTable"], function(idx, obj){
            loginList.push(obj.login);
        });
        loadCreators(loginList);
        loadImplementers(loginList);
    });
        
}
function readAndLoadTargetRevisionAndSprint(system){
    
    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + system);
    $.when(jqxhr).then(function (data) {
        
        var buildList = [];
        var revisionList = [];
        ///console.log(data)
        $.each(data["contentTable"], function(idx, obj){
            if(obj.level === 1){ //build
                buildList.push(obj.versionName);
            }else if(obj.level === 2){ //revision
                revisionList.push(obj.versionName);
            }
        });
        loadBuilds(buildList);
        loadTargetSprint(buildList);
        loadRevisions(revisionList);     
        loadTargetRevision(revisionList)
    });

    
}



/**
 * Search and reset handlers
 */
function searchExecutionsClickHandler(){
    alert("search for executions");
    //form to serialize
    //executionReportingForm
}

function resetClickHandler(){
    alert("search for executions");
    
}

/**
 * Filters' handlers
 */



function setFiltersClickHandler(){
    // var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + system);
    var user = JSON.parse(sessionStorage.getItem("user"));
    alert("clicou");
    var jqxhr = $.ajax({
        type: "POST",
        url: "UpdateMyUserReporting1",
        //dataType: 'json',
        //data: {reporting: data, login: user.login}
        data: {login: user.login}
    });
    
    $.when(jqxhr).then(function(data) {
        console.log("data" + data);
    });
}

function selectFiltersClickHandler(){
    
}

function loadTests(data){
    $("#test").empty();    
    $.each(data, function(idx, obj) {
        $("#test").append("<option value='" + obj.test + "'>" + obj.test + " - " +obj.description+ "</option>");
    });
    
    $("#test").multiselect({
        maxHeight: 150,
        checkboxName: 'test',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-test',
    });
}
function loadSystems(data) {
    loadSelectElement(data, $("#system"), false);
     $("#system").multiselect({
        maxHeight: 150,
        checkboxName: 'system',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-system',
    });
}

function loadCreators(data){
    loadSelectElement(data, $("#creator"), false);
     $("#creator").multiselect({
        maxHeight: 150,
        checkboxName: 'creator',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-creator',
    });
}

function loadImplementers(data){
    loadSelectElement(data, $("#implementer"), false);
     $("#implementer").multiselect({
        maxHeight: 150,
        checkboxName: 'implementer',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-implementer',
    });
}

function loadEnvironments(data){
    loadSelectElement(data, $("#environment"), false);
     $("#environment").multiselect({
        maxHeight: 150,
        checkboxName: 'environment',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-environment',
    });
}

function loadBuilds(data){
    loadSelectElement(data, $("#build"), false);
     $("#build").multiselect({
        maxHeight: 150,
        checkboxName: 'build',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-build',
    });
}

function loadRevisions(data){
    loadSelectElement(data, $("#revision"), false);
     $("#revision").multiselect({
        maxHeight: 150,
        checkboxName: 'revision',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-revision',
    });
}

function loadRevisions(data){
    loadSelectElement(data, $("#revision"), false);
     $("#revision").multiselect({
        maxHeight: 150,
        checkboxName: 'revision',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-revision',
    });
}

function loadApplications(data){
    
    $("#application").empty();    
    $.each(data, function(idx, obj) {
        $("#application").append("<option value='" + obj.application + "'>" + obj.application + "</option>");
    });
    
    $("#application").multiselect({
        maxHeight: 150,
        checkboxName: 'application',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-application',
    });
}

function loadProjects(data){
    
    $("#project").empty();    
    $.each(data, function(idx, obj) {
        $("#project").append("<option value='" + obj.idProject + "'>" + obj.idProject + " - " +obj.description + "</option>");
    });
    
    $("#project").multiselect({
        maxHeight: 150,
        checkboxName: 'project',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-project',
    });
}
function loadTargetSprint(data){
    loadSelectElement(data, $("#targetsprint"), false);
     $("#targetsprint").multiselect({
        maxHeight: 150,
        checkboxName: 'targetsprint',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-targetsprint',
    });
}
function loadTargetRevision(data){
    loadSelectElement(data, $("#targetrevision"), false);
     $("#targetrevision").multiselect({
        maxHeight: 150,
        checkboxName: 'targetrevision',
        buttonWidth: '100%',
        includeSelectAllOption: true,
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        selectAllValue: 'multiselect-all-targetrevision',       
    });
}

function loadTestCaseExecutionStatus(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcestatusFilters"), "tcestatus");    
}
function loadActiveFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcactiveFilters"), "tcactive");    
}
 
function loadGroupFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#groupFilters"), "group");    
}

function loadTCStatusFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcStatusFilters"), "tcstatus");    
}

function loadPriorityFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#priorityFilters"), "priority");    
}
function loadCountryFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#countryFilters"), "country");
} 

function loadBrowserFilter(list){
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#browserFilters"), "browser");
    
} 

function loadFilter(list, element, name){
    
    $.each(list, function(idx, obj){
        var checked = obj.hasOwnProperty("checked") ? ' checked="checked" ': "";
        var cb =  '<label title="' + obj.description + '"  class="checkbox-inline">\n\
                        <input title="' + obj.description + '"  type="checkbox" name="' + name + '" id="' + obj.value 
                +  '" ' + checked + ' value="' + obj.value +'"/>' + obj.value + '</label>';
        $(element).append(cb);
    });
}