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
    $(document).ready(function() {
        initPage();
    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    //create handlers for buttons
    $("#searchExecutionsButton").click(searchExecutionsClickHandler);
    $("#resetButton").click(resetClickHandler);
    $("#selectFiltersButton").click(selectFiltersClickHandler);
    $("#setFiltersButton").click(setFiltersClickHandler);
    $("#getURLButton").click(getURLClickHandler);
    
    loadFilters();
    //$("#exportList").change(controlExportRadioButtons);
    //loadSummaryTableOptions();
}

function displayPageLabel(doc) {
    //TODO define translations in database versionining service
}

function parseReportingFavoriteURLFormat(favorite) {

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

    $.each(res, function(idx, obj) {
        if (obj.indexOf("Ip=") > -1) {
            favoriteObj["ip"] = obj.replace("Ip=", "");
        } else if (obj.indexOf("Port=") > -1) {
            favoriteObj["p"] = obj.replace("Port=", "");
        } else if (obj.indexOf("Tag=") > -1) {
            favoriteObj["t"] = obj.replace("Tag=", "");
        } else if (obj.indexOf("Comment=") > -1) {
            favoriteObj["cm"] = obj.replace("Comment=", "");
        } else if (obj.indexOf("BrowserFullVersion=") > -1) {
            favoriteObj["bv"] = obj.replace("BrowserFullVersion=", "");
        } else if (obj.indexOf("Country=") > -1) {
            country.push(obj.replace("Country=", ""));
        } else if (obj.indexOf("Project=") > -1) {
            project.push(obj.replace("Project=", ""));
        } else if (obj.indexOf("TcActive=") > -1) {
            tcactive.push(obj.replace("TcActive=", ""));
        } else if (obj.indexOf("Status=") > -1) {
            tcstatus.push(obj.replace("Status=", ""));
        } else if (obj.indexOf("Group=") > -1) {
            group.push(obj.replace("Group=", ""));
        } else if (obj.indexOf("Environment=") > -1) {
            environment.push(obj.replace("Environment=", ""));
        } else if (obj.indexOf("Priority=") > -1) {
            priority.push(obj.replace("Priority=", ""));
        } else if (obj.indexOf("Browser=") > -1) {
            browser.push(obj.replace("Browser=", ""));
        }

    });

    //filters with more than one value
    favoriteObj["co"] = country;
    favoriteObj["prj"] = project;
    favoriteObj["a"] = tcactive;
    favoriteObj["s"] = tcstatus;
    favoriteObj["g"] = group;
    favoriteObj["e"] = environment;
    favoriteObj["pr"] = priority;
    favoriteObj["b"] = browser;

    return favoriteObj;
}

function loadPreferences() {
    var preferences = {};

    //check if the user is senting parameters through url, if not then the reportingFavorite values should be used

    var urlValues = $.getUrlVars();

    if (urlValues !== null && urlValues.length > 0) {
        preferences = extractPreferencesFromURL(urlValues);
    } else {
        //no URL parameters were defined then we use the information associated with user profile
        var user = JSON.parse(sessionStorage.getItem("user"));
        var favorite = user.reportingFavorite;
        //check if the reporting favorite is json 


        if (favorite !== '') {
            try {
                preferences = JSON.parse(favorite);
            } catch (e) {
                //it is not json, so we need to parse all data retrieved
                preferences = parseReportingFavoriteURLFormat(favorite);
            }
        }
    }
    return preferences;
}

function extractPreferencesFromURL(urlValues) {
    var preferences = {};
 
    //filters that should have one value only - we select the last one
    if(Boolean(urlValues["cm"])){
        var size = urlValues["cm"].length;
        var lastValue = urlValues["cm"][size-1];
        preferences["cm"] = lastValue;
    }
    
    if(Boolean(urlValues["ip"])){
        var size = urlValues["ip"].length;
        var lastValue = urlValues["ip"][size-1];
        preferences["ip"] = lastValue;
    }
    
    if(Boolean(urlValues["p"])){
        var size = urlValues["p"].length;
        var lastValue = urlValues["p"][size-1];
        preferences["p"] = lastValue;
    }
    
    if(Boolean(urlValues["t"])){
        var size = urlValues["t"].length;
        var lastValue = urlValues["t"][size-1];
        preferences["t"] = lastValue;
    }
    
    if(Boolean(urlValues["br"])){
        var size = urlValues["br"].length;
        var lastValue = urlValues["br"][size-1];
        preferences["br"] = lastValue;
    }
    //filters with more than one value
    //country
    if(Boolean(urlValues["co"])){
        preferences["co"] = urlValues["co"];
    }
    //project
    if(Boolean(urlValues["prj"])){
        preferences["prj"] = urlValues["prj"];
    }
    //active
    if(Boolean(urlValues["a"])){
        preferences["a"] = urlValues["a"];    
    }
    //status
    if(Boolean(urlValues["s"])){
        preferences["s"] = urlValues["s"];
    }
    //group
    if(Boolean(urlValues["g"])){
        preferences["g"] = urlValues["g"];
    }
    //environment
    if(Boolean(urlValues["e"])){
        preferences["e"] = urlValues["e"];
    }
    //priority
    if(Boolean(urlValues["pr"])){
        preferences["pr"] = urlValues["pr"];
    }
    //browser
    if(Boolean(urlValues["b"])){
        preferences["b"] = urlValues["b"];
    }
    //execution status
    if(Boolean(urlValues["es"])){
        preferences["es"] = urlValues["es"];
    }
    
    return preferences;
}
function loadFilters() {

    var preferences = loadPreferences();

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
            /*if (obj.idName === 'SYSTEM') {l
             systemsList.push(obj.value);
             } else */
            if (obj.idName === 'ENVIRONMENT') {
                environmentList.push(obj.value);
            } else if (obj.idName === 'COUNTRY') {
                if (preferences.hasOwnProperty("co") && preferences.co.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
                countryList.push(element);
            } else if (obj.idName === 'BROWSER') {
                browserList.push(element);
                if (preferences.hasOwnProperty("b") && preferences.b.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
            } else if (obj.idName === 'PRIORITY') {
                priorityList.push(element);
                if (preferences.hasOwnProperty("pr") && preferences.pr.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
            } else if (obj.idName === 'TCSTATUS') {
                tcStatusList.push(element);
                if (preferences.hasOwnProperty("s") && preferences.s.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
            } else if (obj.idName === 'GROUP') {
                groupList.push(element);
                if (preferences.hasOwnProperty("g") && preferences.g.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
            } else if (obj.idName === 'TCACTIVE') {
                activeList.push(element);
                if (preferences.hasOwnProperty("a") && preferences.a.indexOf(obj.value) > -1) {
                    element["checked"] = "checked";
                }
            } else if (obj.idName === 'TCESTATUS') {
                executionStatusList.push(element);
                if (preferences.hasOwnProperty("es") && preferences.es.indexOf(obj.value) > -1) {
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

        //comment
        loadTextComponent("#comment", preferences.cm);
        //ip
        loadTextComponent("#ip", preferences.ip);
        //port
        loadTextComponent("#port", preferences.p);
        //tag
        loadTextComponent("#tag", preferences.t);
        //browser version
        loadTextComponent("#browserversion", preferences.br);

        //multiselect components
        loadEnvironments(environmentList, preferences.e);
        readAndLoadApplication(system);
        readAndLoadTest(system);
    });
    readAndLoadCreatorImplementer();
    readAndLoadTargetRevisionAndSprint(system);
    readAndLoadProject(preferences.prj);

}

function loadTextComponent(element, text) {
    if (typeof text !== 'undefined') {
        $(element).prop("value", text);
    } else {
        $(element).prop("value", "");
    }
}

function readAndLoadTest(selectedSystem) {
    var jqxhr = $.getJSON("ReadTest", "system=" + selectedSystem);
    var testList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj) {
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
function readAndLoadApplication(selectedSystem) {
    var jqxhr = $.getJSON("ReadApplication", "system=" + selectedSystem);
    var applicationsList = [];
    $.when(jqxhr).then(function(data) {
        $.each(data["contentTable"], function(idx, obj) {
            var element = {application: obj.application, system: obj.system};
            applicationsList.push(element);

        });
        loadApplications(applicationsList);

    });
}

function readAndLoadProject(projectsToSelect) {
    var jqxhr = $.getJSON("ReadProject", "");

    $.when(jqxhr).then(function(data) {
        var projectList = [];

        $.each(data["contentTable"], function(idx, obj) {
            var element = {idProject: obj.idProject, description: obj.description};
            projectList.push(element);
        });
        loadProjects(projectList);
        loadMultiselectOptions("project", projectsToSelect);
    });
}

function readAndLoadCreatorImplementer() {
    //loads the creator and implementer
    var jqxhr = $.getJSON("ReadUserPublic", "");
    $.when(jqxhr).then(function(data) {
        //console.log("data " + data["contentTable"]);
        var loginList = [];
        $.each(data["contentTable"], function(idx, obj) {
            loginList.push(obj.login);
        });
        loadCreators(loginList);
        loadImplementers(loginList);
    });

}
function readAndLoadTargetRevisionAndSprint(system) {

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + system);
    $.when(jqxhr).then(function(data) {

        var buildList = [];
        var revisionList = [];
        ///console.log(data)
        $.each(data["contentTable"], function(idx, obj) {
            if (obj.level === 1) { //build
                buildList.push(obj.versionName);
            } else if (obj.level === 2) { //revision
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
function searchExecutionsClickHandler() {
    alert("search for executions");
    //form to serialize
    //executionReportingForm
}

/**
 * Auxiliary method that removes all selections and text from the form
 * @returns {undefined}
 */
function resetClickHandler() {

    //remove any selection from any checkbox that has the attribute checked
    $("input[name='tcstatus']").removeAttr("checked");
    $("input[name='group']").removeAttr("checked");
    $("input[name='tcactive']").removeAttr("checked");
    $("input[name='priority']").removeAttr("checked");
    $("input[name='country']").removeAttr("checked");
    $("input[name='browser']").removeAttr("checked");
    $("input[name='tcestatus']").removeAttr("checked");

    clearMultiselectSelection("#test");
    clearMultiselectSelection("#application");
    clearMultiselectSelection("#project");
    clearMultiselectSelection("#targetsprint");
    clearMultiselectSelection("#targetrevision");
    clearMultiselectSelection("#environment");
    clearMultiselectSelection("#build");
    clearMultiselectSelection("#revision");
    clearMultiselectSelection("#creator");
    clearMultiselectSelection("#implementer");

    loadTextComponent("#comment", "");
    loadTextComponent("#ip", "");
    loadTextComponent("#port", "");
    loadTextComponent("#tag", "");
    loadTextComponent("#browserversion", "");

}

/**
 * Filters' handlers
 */

function getURLClickHandler() {
    //get list of parameters
    var urlParameters = "";
    //TODO:FN finish these parameters
    window.open("ReportExecution_temp.jsp" + urlParameters, '_blank');
}

function setFiltersClickHandler() {

    var form = $('#executionReportingForm');

    var jqxhr = $.post("UpdateMyUserReporting1", form.serialize(), "json");
    $.when(jqxhr).then(function(data) {
        var dataJson = JSON.parse(data);
        var code = getAlertType(dataJson.messageType);
        if (code === "success") {
            //if the update succeed then sends a message to the user
            if (dataJson.hasOwnProperty("preferences")) {
                var preferences = dataJson.preferences;
                var user = JSON.parse(sessionStorage.getItem("user"));
                user.reportingFavorite = JSON.stringify(preferences);
                sessionStorage.setItem("user", JSON.stringify(user));
            }
        }
        showMessageMainPage(code, dataJson.message);

    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Method that applies the user prefereces in the form.
 * @returns {undefined}
 */
function selectFiltersClickHandler() {
    var user = JSON.parse(sessionStorage.getItem("user"));
    var preferences = JSON.parse(user.reportingFavorite);

    //multiselect
    //project
    loadMultiselectOptions("project", preferences.prj);
    //environment
    loadMultiselectOptions("environment", preferences.e);

    //checkboxes
    //tc status
    loadCheckboxValuesSelection("tcstatus", preferences.s);
    //group
    loadCheckboxValuesSelection("group", preferences.g);
    //active
    loadCheckboxValuesSelection("tcactive", preferences.a);
    //priority
    loadCheckboxValuesSelection("priority", preferences.pr);
    //country
    loadCheckboxValuesSelection("country", preferences.co);
    //browser
    loadCheckboxValuesSelection("browser", preferences.b);
    //execution status
    loadCheckboxValuesSelection("tcestatus", preferences.es);

    //free text 
    //comment
    loadTextComponent("#comment", preferences.cm);
    //ip
    loadTextComponent("#ip", preferences.ip);
    //port
    loadTextComponent("#port", preferences.p);
    //tag
    loadTextComponent("#tag", preferences.t);
    //browser version
    loadTextComponent("#browserversion", preferences.br);

}

function loadMultiselectOptions(elementid, listToSelect) {
    //clears the set of options in the multiselect

    clearMultiselectSelection("#" + elementid);

    if (typeof listToSelect !== 'undefined') {
        for (var i = 0; i < listToSelect.length; i++) {
            $("#" + elementid).multiselect().find(":checkbox[value='" + listToSelect[i] + "']").prop("checked", "checked").attr("checked", "checked");
            $("#" + elementid + " option[value='" + listToSelect[i] + "']").attr("selected", "selected").prop("selected", "selected");
        }
        $("#" + elementid).multiselect("refresh");
    }
}

function loadCheckboxValuesSelection(group, listToSelect) {
    //clears the set of checkboxes
    $("input[name='" + group + "']").removeProp("checked");

    if (typeof listToSelect !== 'undefined') {
        $("input[name='" + group + "']").each(function() {
            var value = $(this).prop("value");
            if (listToSelect.indexOf(value) > -1) {
                $(this).prop("checked", "checked");
            }
        });
    }
}
function clearMultiselectSelection(elementID) {
    $(elementID).multiselect("clearSelection");
    //$(elementID).multiselect( 'refresh' );
}
function loadTests(data) {
    $("#test").empty();
    $.each(data, function(idx, obj) {
        $("#test").append("<option value='" + obj.test + "'>" + obj.test + " - " + obj.description + "</option>");
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

function loadCreators(data) {
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

function loadImplementers(data) {
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

function loadEnvironments(data, environmentsToSelect) {
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
    loadMultiselectOptions("environment", environmentsToSelect);
}

function loadBuilds(data) {
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

function loadRevisions(data) {
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

function loadRevisions(data) {
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

function loadApplications(data) {

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

function loadProjects(data) {

    $("#project").empty();
    $.each(data, function(idx, obj) {
        $("#project").append("<option value='" + obj.idProject + "'>" + obj.idProject + " - " + obj.description + "</option>");
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
function loadTargetSprint(data) {
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
function loadTargetRevision(data) {
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

function loadTestCaseExecutionStatus(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcestatusFilters"), "tcestatus");
}
function loadActiveFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcactiveFilters"), "tcactive");
}

function loadGroupFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#groupFilters"), "group");
}

function loadTCStatusFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#tcStatusFilters"), "tcstatus");
}

function loadPriorityFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#priorityFilters"), "priority");
}
function loadCountryFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#countryFilters"), "country");
}

function loadBrowserFilter(list) {
    //should load filters according to the preferences of the user    
    loadFilter(list, $("#browserFilters"), "browser");

}

function loadFilter(list, element, name) {

    $.each(list, function(idx, obj) {
        var checked = obj.hasOwnProperty("checked") ? ' checked="checked" ' : "";
        var cb = '<label title="' + obj.description + '"  class="checkbox-inline">\n\
                        <input title="' + obj.description + '"  type="checkbox" name="' + name + '" id="' + obj.value
                + '" ' + checked + ' value="' + obj.value + '"/>' + obj.value + '</label>';
        $(element).append(cb);
    });
}

$.extend({
    getUrlVars: function() {
        var vars = [], hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (var i = 0; i < hashes.length; i++){
            
            hash = hashes[i].split('=');
            if(hash.length > 1){
                var values = [];

                if (vars.indexOf(hash[0]) > -1) { //hash already exists
                    values = vars[hash[0]];
                } else {
                    vars.push(hash[0]);
                }

                values.push(hash[1]);
                vars[hash[0]] = values;
            }
        }
           

        return vars;
    },
    getUrlVar: function(name) {
        return $.getUrlVars()[name];
    },
    /*getUrlVarsByname: function(name){
     var vars = $.getUrlVars();
     var varsByName = [];
     $.each(vars, function(idx, obj){
     console.log("idx " + idx + " obj " + obj );
     if(obj === name){
     varsByName.push(vars[obj]);
     }
     });
     
     return varsByName;
     }*/
});