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

var globalGroupList;
var globalTCStatusList;
var globalControlStatusList;
$.when($.getScript("js/pages/global/global.js")).then(function() {
    $(document).ready(function() {
        initPage();
        $('a[id="tab4Text"]').on('shown.bs.tab', function (e) {
            //need to redraw the table to avoid problems with misaliged columns
            if ($.fn.DataTable.isDataTable('#statisticsPerTCGroup')) {
                $('#statisticsPerTCGroup').dataTable().fnDraw(true);
            }
        });
        $('a[id="tab3Text"]').on('shown.bs.tab', function (e) {
            //need to redraw the table to avoid problems with misaliged columns
            if ($.fn.DataTable.isDataTable('#statisticsPerTCStatus')) {
                $('#statisticsPerTCStatus').dataTable().fnDraw(true);
            }
        });
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
    //TODO:FN define translations in database versionining service
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
    if (Boolean(urlValues["cm"])) {
        var size = urlValues["cm"].length;
        var lastValue = urlValues["cm"][size - 1];
        preferences["cm"] = lastValue;
    }

    if (Boolean(urlValues["ip"])) {
        var size = urlValues["ip"].length;
        var lastValue = urlValues["ip"][size - 1];
        preferences["ip"] = lastValue;
    }

    if (Boolean(urlValues["p"])) {
        var size = urlValues["p"].length;
        var lastValue = urlValues["p"][size - 1];
        preferences["p"] = lastValue;
    }

    if (Boolean(urlValues["t"])) {
        var size = urlValues["t"].length;
        var lastValue = urlValues["t"][size - 1];
        preferences["t"] = lastValue;
    }

    if (Boolean(urlValues["br"])) {
        var size = urlValues["br"].length;
        var lastValue = urlValues["br"][size - 1];
        preferences["br"] = lastValue;
    }
    //filters with more than one value
    //country
    if (Boolean(urlValues["co"])) {
        preferences["co"] = urlValues["co"];
    }
    //project
    if (Boolean(urlValues["prj"])) {
        preferences["prj"] = urlValues["prj"];
    }
    //active
    if (Boolean(urlValues["a"])) {
        preferences["a"] = urlValues["a"];
    }
    //status
    if (Boolean(urlValues["s"])) {
        preferences["s"] = urlValues["s"];
    }
    //group
    if (Boolean(urlValues["g"])) {
        preferences["g"] = urlValues["g"];
    }
    //environment
    if (Boolean(urlValues["e"])) {
        preferences["e"] = urlValues["e"];
    }
    //priority
    if (Boolean(urlValues["pr"])) {
        preferences["pr"] = urlValues["pr"];
    }
    //browser
    if (Boolean(urlValues["b"])) {
        preferences["b"] = urlValues["b"];
    }
    //execution status
    if (Boolean(urlValues["es"])) {
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
        globalTCStatusList = tcStatusList;
        loadGroupFilter(groupList);
        globalGroupList = groupList;
        loadTestCaseExecutionStatus(executionStatusList);
        //TODO: load this list globalControlStatusList to create the statistics
        
        //
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
    //form to serialize
    var form = $('#executionReportingForm');

    var countriesSelected = [];
    $("input[type='checkbox'][name='country']").each(function(idx, obj) {
        if ($(this).prop("checked")) {
            countriesSelected.push($(this).val());
        }
    });
    countriesSelected.sort();

    var browsersSelected = [];
    $("input[type='checkbox'][name='browser']").each(function(idx, obj) {
        if ($(this).prop("checked")) {
            browsersSelected.push($(this).val());
        }
    });
    browsersSelected.sort();

    if (browsersSelected.length === 0 || countriesSelected.lenght === 0) {
        showMessageMainPage("danger", "Browser and Country are mandatory filters!"); //TODO add translations
        return;
    }
    var system = $("#MySystem").val();
    //sends the system and all information available in the form
    var jqxhr = $.post("ReadTestCaseExecution", "system=" + system + "&" + form.serialize(), "json");//TODO:FN add getreport_1 to the servlet

    $.when(jqxhr).then(function(result) {
        //check how many countries should be displayed
        $('#executionTable').removeClass("invisible");


        if ($.fn.DataTable.isDataTable('#executionTable')) {
            //    $('#executionTable').dataTable().fnClearTable();
            //$("#executionTable th[class='dynamicHeader']").remove();
            $('#executionTable').dataTable().fnDestroy(true);
            reconstructTable();
        }

        //adds the header columns based on the countries and browser selected

        addCountryandBrowserColumnHeaders(countriesSelected, browsersSelected);
        var configurations = new TableConfigurationsClientSide("executionTable", result["contentTable"],
                aoColumnsFuncReportExecution("executionTable", countriesSelected, browsersSelected));


        configurations.scrollX = true;
        createDataTable(configurations, undefined, undefined, undefined);

        createSummaryTables(result["contentTable"], countriesSelected, browsersSelected);

        //enables the get url button
        $("#getURLButton").removeProp("disabled");
    });
}

function createSummaryTables(data, countriesSelected, browsersSelected) {
    var recordsPerGroup = [];
    var recordsPerTCStatus = [];

    $.each(data, function(idx, row) {
        var testName = row.test;
        var groupName = row.group;
        var tcStatusName = row.status;

        var elementGroup = null, elementTCstatus = null;

        $.each(recordsPerGroup, function(idxTest, testObject) {

            if (testObject.test === testName) {
                elementGroup = testObject;
                $.each(testObject.data, function(idxGroup, groupObject) {
                    groupObject.total++;
                    testObject.data[idxGroup] = groupObject;
                    recordsPerGroup[idxTest] = testObject;
                    return;
                });

            }
        });

        $.each(recordsPerTCStatus, function(idxTest, testObject) {
            if (testObject.test === testName) {
                elementTCstatus = testObject;
                $.each(testObject.data, function(idxStatus, tcStatusObj) {
                    tcStatusObj.total++;
                    testObject.data[idxStatus] = tcStatusObj;
                    recordsPerTCStatus[idxTest] = testObject;
                    return;
                });

            }
        });



        if (elementGroup === null) {
            var groupSet = [];
            var eGroup = {name: groupName, total: 1};
            groupSet.push(eGroup);
            elementGroup = {test: testName, data: groupSet};
            recordsPerGroup.push(elementGroup);
        }

        if (elementTCstatus === null) {
            var tcStatusSet = [];
            var eTcStatus = {name: tcStatusName, total: 1};
            tcStatusSet.push(eTcStatus);
            elementTCstatus = {test: testName, data: tcStatusSet};
            recordsPerTCStatus.push(elementTCstatus);

        }

        //TODO implement statistics per control status
    });

    /*drawTotalsPerExecutionStatus(recordsPerGroup);*/
    //TODO: check if there are problems when we click the search
    drawTotalsPerTCStatus(recordsPerTCStatus);
    drawTotalsPerGroup(recordsPerGroup);
}
function drawTotalsPerGroup(data) {
    var dataSet = getStatisticsDataSet(data, globalGroupList);
    var dataSetTable = dataSet["dataSetTable"];

    if ($.fn.DataTable.isDataTable('#statisticsPerTCGroup')) {
        $('#statisticsPerTCGroup').dataTable().fnDestroy(true);
        recreateGroupTable();
    }

    
    var configurations = new TableConfigurationsClientSide("statisticsPerTCGroup", dataSetTable, aoColumnsFuncAux(globalGroupList));

    configurations.scrollX = true;
    createDataTable(configurations);
    //addColumnHeaders(globalGroupList, "statisticsPerTCGroupHeader");

    var totalTests = [];
    totalTests = dataSet["totalTestsData"];
    //add total tests to foot
    var $tr = $('<tr>');
    $tr.addClass("summaryTotal");
    $.each(totalTests, function(idx, totalValue) {
        var $td = $('<td>');
        $td.text(totalValue);
        $td.addClass("width150");
        $tr.append($td);
    });

    $('#statisticsPerTCGroupFoot').append($tr);
    $('#statisticsPerTCGroupFoot td:first').addClass("width200");
    $('#statisticsPerTCGroup').removeAttr('style');
}



function recreateGroupTable() {
    var htmlToAppend = '<table id="statisticsPerTCGroup" class="table table-hover display">';
    htmlToAppend += '<thead id="statisticsPerTCGroupHeader"></thead>';
    htmlToAppend += '<tbody></tbody><tfoot id="statisticsPerTCGroupFoot"></tfoot></table>';
    $("#statisticsPerTCGroupArea").append(htmlToAppend);
}
function recreateTCStatusTable() {
    var htmlToAppend = '<table id="statisticsPerTCStatus" class="table table-hover display">';
    htmlToAppend += '<thead id="statisticsPerTCStatusHeader"></thead>';
    htmlToAppend += '<tbody></tbody><tfoot id="statisticsPerTCStatusFoot"></tfoot></table>';
    $("#statisticsPerTCStatusArea").append(htmlToAppend);
}
function drawTotalsPerTCStatus(data) {
    var dataSet = getStatisticsDataSet(data, globalTCStatusList);
    var dataSetTable = dataSet["dataSetTable"];

    if ($.fn.DataTable.isDataTable('#statisticsPerTCStatus')) {
        $('#statisticsPerTCStatus').dataTable().fnDestroy(true);
        recreateTCStatusTable();
    }

    
    var configurations = new TableConfigurationsClientSide("statisticsPerTCStatus", dataSetTable, aoColumnsFuncAux(globalTCStatusList));

    configurations.scrollX = true;
    createDataTable(configurations);
    //addColumnHeaders(globalGroupList, "statisticsPerTCGroupHeader");

    var totalTests = [];
    totalTests = dataSet["totalTestsData"];
    //add total tests to foot
    var $tr = $('<tr>');
    $tr.addClass("summaryTotal");
    $.each(totalTests, function(idx, totalValue) {
        var $td = $('<td>');
        $td.text(totalValue);
        $td.addClass("width150");
        $tr.append($td);
    });

    $('#statisticsPerTCStatusFoot').append($tr);
    $('#statisticsPerTCStatusFoot td:first').addClass("width200");
    $('#statisticsPerTCStatus').removeAttr('style');

}
function drawTotalsPerExecutionStatus(data) {
//TODO: implement this
}

function getStatisticsDataSet(dataSet, globalList) {
    var dataSetTable = [];
    var row = 0;
    var totalTests = {};
    totalTests["total"]  = "Total"; //add translations
    //Add header
    $.each(dataSet, function(idx, testObj) {
        var rowData = {};

        var totalRow = 0;
        rowData["test"] = testObj.test;
        $.each(globalList, function(idx, item) {
            if (!totalTests.hasOwnProperty(item.value)) {
                totalTests[item.value] = 0;
            }
            var found = false;
            var total = -1;
            $.each(testObj.data, function(idxGroup, statistic) {
                if (statistic.name === item.value) {
                    totalRow += statistic.total;
                    total = statistic.total;
                    found = true;


                }
            });
            if (!found) {
                total = 0;
            }
            rowData[item.value] = total;
            totalTests[item.value] += total;
            //totalTests["globalTotal"] += total;
        });
        rowData["totalRow"] = totalRow;
        dataSetTable.push(rowData);
        row++;
    });

    var data = {dataSetTable: dataSetTable, totalTestsData: totalTests};
    return data;
}

function aoColumnsFuncAux(globalList) {
    var aoColumns = [];
    aoColumns.push({className: "width200", "sName": "test", "data": "test", "title": "Test"});
    $.each(globalList, function(idx, obj) {
        aoColumns.push({className: "width150", "sName": obj.value, "data": obj.value, "title": obj.value});
    });
    aoColumns.push({className: "width150", "sName": "totalRow", "data": "totalRow", "title": "Total"});
    return aoColumns;
}
//TODO:DELETE
function drawStatisticsTable2(dataSet, globalList, tableID) {
    $("#" + tableID).empty();
    //draw header
    globalList.sort();
    var $thead = $('<thead>');
    var $tr = $('<tr>');
    var $th = $('<th>');
    $th.text("Test"); //TODO: add translation
    $tr.append($th);
    var totalTests = [];

    $.each(globalList, function(idx, item) {
        $th = $('<th>');
        $th.addClass("width150");
        $th.text(item.value);
        $tr.append($th);
        totalTests[item.value] = 0;
    });
    totalTests["globalTotal"] = 0;


    $th = $('<th>');
    $th.text("Total"); //TODO: add translation
    $tr.append($th);


    $thead.append($tr);
    $("#" + tableID).append($thead);


    $.each(dataSet, function(idx, testObj) {
        var $tr = $('<tr>');
        var $td = $('<td>');

        $td.text(testObj.test);

        $tr.append($td);
        var totalRow = 0;

        $.each(globalList, function(idx, item) {
            $td = $('<td>');
            var found = false;
            $.each(testObj.data, function(idxGroup, statistic) {
                if (statistic.name === item.value) {
                    totalRow += statistic.total;
                    $td.text(statistic.total);
                    found = true;
                    totalTests[statistic.name] += statistic.total;
                    totalTests["globalTotal"] += statistic.total;
                }
            });
            if (!found) {
                $td.text("0");
            }
            $tr.append($td);

        });

        $td = $('<td>');
        $td.text(totalRow);

        $tr.append($td);
        $("#" + tableID).append($tr);
    });

    $tr = $('<tr>');
    var $td = $('<td>');
    $td.text("Total"); //translations
    $tr.append($td);
    $.each(globalList, function(idx, tcStatusObj) {
        $td = $('<td>');
        $td.text(totalTests[tcStatusObj.value]);
        $tr.append($td);
    });
    $td = $('<td>');
    $td.text(totalTests["globalTotal"]); //TODO: add translations
    $tr.append($td);

    $("#" + tableID).append($tr);
}
function reconstructTable() {
    /*var tableHTML = $('<table id="executionTable" class="table table-hover display invisible" name="executionTable">');
     tableHTML += $('<table id="executionTable" class="table table-hover display invisible" name="executionTable">');*/
    var $tableHTML = $("<table>");
    var $rowHTML = $("<tr>");
    $tableHTML.prop("id", "executionTable");
    $tableHTML.attr("id", "executionTable");
    $tableHTML.addClass("table");
    $tableHTML.addClass("table-hover");
    $tableHTML.addClass("display");

    var $theadHTML = $('<thead>');
    $theadHTML.prop("id", "executionTableHeader");
    $theadHTML.attr("id", "executionTableHeader");
    $theadHTML.prop("name", "executionTableHeader");
    $theadHTML.attr("name", "executionTableHeader");
    $theadHTML.append($('<th>').text("Test"));
    $theadHTML.append($('<th>').text("TestCase"));
    $theadHTML.append($('<th>').text("Application"));
    $theadHTML.append($('<th>').text("Ticket"));
    $theadHTML.append($('<th>').text("Bug ID"));
    $theadHTML.append($('<th>').text("Ticket"));
    $theadHTML.append($('<th>').text("Group"));
    $theadHTML.append($('<th>').text("Priority"));
    $theadHTML.append($('<th>').attr("id", "headerStatus").prop("id", "headerStatus").text("Status"));
    $rowHTML.append($theadHTML);
    $tableHTML.append($rowHTML);
    $("#tableArea div[id = 'afterTableDiv']").before($tableHTML);
}

function addCountryandBrowserColumnHeaders(countriesSelected, browsersSelected) {
    var currentTD = $('table[name="executionTable"] #headerStatus');

    $.each(countriesSelected, function(idx, obj) {
        $.each(browsersSelected, function(idx2, obj2) {
            var $newTd = $('<th class="dynamicHeader">');
            var $divNewTD = $('<div>').text(obj + "-" + obj2);
            $divNewTD.css("width", "70px");
            $divNewTD.css("max-width", "70px");

            $newTd.append($divNewTD);

            currentTD.after($newTd);
            currentTD = $newTd;

        });
    });
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
    loadTextComponent("#bugid", "");
    loadTextComponent("#ticked", "");

}

/**
 * Filters' handlers
 */

function getURLClickHandler() {
    //get list of parameters
    var urlParameters = "";
    //TODO finish these parameters, include application, test, and others?
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



/*Functions for table*/
function aoColumnsFuncReportExecution(tableId, countryList, browserList) {
    var doc = new Doc();
    //doc.getDocOnline("testdatalib", "name")
    var aoColumns = [];
    $("#" + tableId + " th ").each(function(i) {
        switch (i) {
            case 0:
                aoColumns.push({className: "width350", "sName": "test", "data": "test", "title": "Test",
                    "mRender": function(data, type, oObj) {
                        return "<div>" + oObj.test + " " + drawHyperlinkExternal("TestCaseList.jsp?test=" + encodeURIComponent(oObj.test),
                                '<span title="Open list of test cases for this test" class="glyphicon glyphicon-new-window"></span>') + "</div>";
                    }});
                break;
            case 1 :
                aoColumns.push({className: "width100", "sName": "testCase", "data": "testCase", "title": "Test Case",
                    "mRender": function(data, type, oObj) {
                        return "<div>" + oObj.testCase + " " + drawHyperlinkExternal("TestCase.jsp?Test=" + encodeURIComponent(oObj.test) + "&TestCase=" +
                                encodeURIComponent(oObj.testCase) + "&Load=Load", '<span title="Open Test Case" class="glyphicon glyphicon-new-window"></span>') + "</div>";
                    }});
                break;
            case 2 :
                aoColumns.push({className: "width130", "sName": "application", "data": "application", "title": "Application",
                    "mRender": function(data, type, oObj) {
                        if (!jQuery.isEmptyObject(oObj.application)) {
                            return "<div> " + oObj.application + " " + drawHyperlinkExternal("TestPerApplication.jsp?Application=" + encodeURIComponent(oObj.application),
                                    '<span title="Open list of test cases for this application." class="glyphicon glyphicon-new-window"></span>'.trim()) + "</div>";
                        }
                        return "";
                    }});
                break;
            case 3 :
                aoColumns.push({className: "width80", "sName": "bugId", "data": "bugId", "title": "Bug ID"});
                break;
            case 4 :
                aoColumns.push({className: "width80", "sName": "ticket", "data": "ticket", "title": "Ticket"});
                break;
            case 5 :
                aoColumns.push({className: "width80", "sName": "group", "data": "group", "title": "Group"});
                break;
            case 6 :
                aoColumns.push({className: "width80", "sName": "priority", "data": "priority", "title": "Priority"});
                break;
            case 7 :
                aoColumns.push({className: "width80", "sName": "status", "data": "status", "title": "Status"});
                break;

        }
    });
    //adds each column for each pair country-browser
    $.each(countryList, function(idx, country) {
        $.each(browserList, function(idx2, browser) {
            var key = country + " " + browser;
            aoColumns.push({className: "dynamicHeader width100", "sName": key, "data": key, "title": key,
                "mRender": function(data, type, oObj) {

                    if (!jQuery.isEmptyObject(oObj.execTab[key])) {
                        var rowDetails = oObj.execTab[key];
                        if (rowDetails.Country === country && rowDetails.Browser) {
                            var executionLink = generateExecutionLink(rowDetails.ControlStatus, rowDetails.ID);
                            var glyphClass = getRowClass(rowDetails.ControlStatus);
                            var tooltip = generateTooltip(rowDetails);
                            var cell = '<div class="progress-bar status' + rowDetails.ControlStatus + '" \n\
                                    role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;cursor: pointer; height: 40px;" \n\
                                    data-toggle="tooltip" data-html="true" title="' + tooltip + '"\n\
                                    onclick="window.open(\'' + executionLink + '\')">\n\
                                    <span class="' + glyphClass.glyph + ' marginRight5"></span>\n\
                                     <span>' + rowDetails.ControlStatus + '<span></div>';
                            return cell;
                        } else {
                            return "";
                        }

                    }
                    return "";
                }});
        });
    });

    return aoColumns;

}



function generateTooltip(data) {
    var htmlRes;

    htmlRes = '<div><span class=\'bold\'>Execution ID :</span> ' + data.ID + '</div>' +
            '<div><span class=\'bold\'>Country : </span>' + data.Country + '</div>' +
            '<div><span class=\'bold\'>Environment : </span>' + data.Environment + '</div>' +
            '<div><span class=\'bold\'>Browser : </span>' + data.Browser + '</div>' +
            '<div><span class=\'bold\'>Start : </span>' + data.Start + '</div>' +
            '<div><span class=\'bold\'>End : </span>' + data.End + '</div>' +
            '<div>' + data.ControlMessage + '</div>';

    return htmlRes;
}
