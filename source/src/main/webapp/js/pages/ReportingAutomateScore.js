/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
/* global handleErrorAjaxAfterTimeout */
// ChartJS Config Graphs
var configHistoFreq = {};
var configHistoStab = {};
var configHistoDur = {};
var configHistoMnt = {};

// Counters of different countries, env and robotdecli (used to shorten the labels)
var nbEnv = 0;

const imageA = new Image(20, 20);
imageA.src = "images/AS-A2.png";
const imageB = new Image(20, 20);
imageB.src = "images/AS-B1.png";
const imageC = new Image(20, 20);
imageC.src = "images/AS-C1.png";
const imageD = new Image(20, 20);
imageD.src = "images/AS-D1.png";
const imageE = new Image(20, 20);
imageE.src = "images/AS-E2.png";
const imageNA = new Image(20, 20);
imageNA.src = "images/AS-NA2.png";



$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        initPage();
        bindToggleCollapse();
        //open Run navbar Menu
        openNavbarMenu("navMenuExecutionReporting");
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        moment.locale('en', {
            week: {dow: 1} // Monday is the first day of the week
        });

        $('#topicker').datetimepicker({
            format: 'YYYY-MM-DD [(Week] WW YYYY)',
            keepOpen: false,
            calendarWeeks: true
        });

        var campaigns = GetURLParameters("campaigns");
        var systems = GetURLParameters("systems");
        var to = GetURLParameter("to");

        var nbWeeks = GetURLParameter("nbWeeks");
        if (nbWeeks === null || nbWeeks === undefined) {
            nbWeeks = 15;
        }
        $('#trendWeeks').val(nbWeeks);

        var environments = GetURLParameters("environments");
        //        
        var gp1s = GetURLParameters("group1s");
        var gp2s = GetURLParameters("group2s");
        var gp3s = GetURLParameters("group3s");

        let toD;
        if (to === null) {
            toD = new Date();
            toD.setDate(toD.getDate() - 7);
        } else {
            toD = new Date(to);
        }
        $('#topicker').data("DateTimePicker").date(moment(toD));

        $("#campaignSelect").empty();
        $("#campaignSelect").select2({width: "100%"});
        feedCampaignCombos("#campaignSelect", campaigns, environments, gp1s, gp2s, gp3s);
        $("#systemSelect").empty();
        $("#systemSelect").select2({width: "100%"});
        feedSystemSelectOptions("#systemSelect");

        loadKPIGraphBars(false, environments, gp1s, gp2s, gp3s);

    });
});


function aoColumnsFuncTestCase() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": "testFolder",
            "bSortable": true,
            "sName": "testFolder",
            "title": "Test Folder",
            "sWidth": "200px"
        },
        {
            "data": "testcaseId",
            "bSortable": true,
            "sName": "testcaseId",
            "title": "Test Case ID",
            "sWidth": "70px",
            "mRender": function (data, type, obj) {
                if (type === "display") {
                    console.info(obj);
                    var buttons = "";

                    var editScript = '<a id="testCaseLink" class="btn btn-primary btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_editScript") + '" href="./TestCaseScript.jsp?test=' + encodeURIComponent(obj["testFolder"]) + '&testcase=' + encodeURIComponent(obj["testcaseId"]) + '">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </a>';

                    buttons += editScript;

                    return '<div class="center btn-group width250">' + buttons + obj["testcaseId"] + '</div> ';
                } else {
                    return obj["testFolder"] + data;
                }
            }
        },
        {
            "data": "application",
            "bSortable": true,
            "sName": "application",
            "title": "Application",
            "sWidth": "70px"
        },
        {
            "data": "nb",
            "bSortable": true,
            "sName": "nb",
            "title": "nb of executions",
            "sWidth": "50px"
        },
        {
            "data": "duration",
            "bSortable": true,
            "sName": "duration",
            "title": "Avg Duration",
            "sWidth": "50px"
            ,
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "durationMin",
            "bSortable": true,
            "sName": "durationMin",
            "title": "Min Duration",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "durationMax",
            "bSortable": true,
            "sName": "durationMax",
            "title": "Max Duration",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "nbFN",
            "bSortable": true,
            "sName": "nbFN",
            "title": "nb of False Negative",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                return data;
            }
        },
        {
            "data": "nbFlaky",
            "bSortable": true,
            "sName": "nbFlaky",
            "title": "nb of Flaky",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                return data;
            }
        },
        {
            "data": "nbFlaky",
            "bSortable": true,
            "sName": "nbFlaky",
            "title": "% of Flaky",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                let per = data / oObj.nb * 100;
                if (type === 'display') {
                    return "" + Math.round(per) + " %";
                }
                return per;
            }
        }
    ];

    return aoColumns;
}

function aoColumnsFuncCampaign() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": "campaign",
            "bSortable": true,
            "sName": "campaign",
            "title": "Campaign",
            "sWidth": "200px"
        },
        {
            "data": "nb",
            "bSortable": true,
            "sName": "nb",
            "title": "nb of campaign exe",
            "sWidth": "50px"
        },
        {
            "data": "nbExe",
            "bSortable": true,
            "sName": "nbExe",
            "title": "Avg nb of executions",
            "sWidth": "50px"
        },
        {
            "data": "nbExeMin",
            "bSortable": true,
            "sName": "nbExeMin",
            "title": "Min nb of executions",
            "sWidth": "50px"
        },
        {
            "data": "nbExeMax",
            "bSortable": true,
            "sName": "nbExeMax",
            "title": "Max nb of executions",
            "sWidth": "50px"
        },
        {
            "data": "duration",
//            "bSortable": true,
            "sName": "duration",
            "title": "Avg Duration",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "durationMin",
//            "bSortable": true,
            "sName": "durationMin",
            "title": "Min Duration",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "durationMax",
//S            "bSortable": true,
            "sName": "durationMax",
            "title": "Max Duration",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "nbFlaky",
            "bSortable": true,
            "sName": "nbFlaky",
            "title": "Avg nb of Flaky",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                return data;
            }
        },
        {
            "data": "nbFlakyMin",
            "bSortable": true,
            "sName": "nbFlakyMin",
            "title": "Min nb of Flaky",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                return data;
            }
        },
        {
            "data": "nbFlakyMax",
            "bSortable": true,
            "sName": "nbFlakyMax",
            "title": "Max nb of Flaky",
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                if ((data) === 0) {
                    return "";
                }
                return data;
            }
        }
    ];

    return aoColumns;
}


function drawTable_TestCases(data, targetTable, targetPanel) {
    if (data && data.length > 0) {
        var configurations = new TableConfigurationsClientSide(targetTable, data, aoColumnsFuncTestCase(), true, [0, 'asc']);
        configurations.lengthMenu = [10, 15, 20, 30, 50, 100, 10000];

        if ($('#' + targetTable).hasClass('dataTable') === false) {

            createDataTableWithPermissions(configurations, undefined, targetPanel);
            showTitleWhenTextOverflow();
        } else {

            var oTableTC = $("#testcasesTable").dataTable();
            oTableTC.fnClearTable();
            oTableTC.fnAddData(data);
        }
    } else {
        if (oTableTC) {
            var oTableTC = $("#campaignsTable").dataTable();
            oTableTC.fnClearTable();
        }
    }
}

function drawTable_Campaigns(data, targetTable, targetPanel) {
    if (data && data.length > 0) {
        var configurations = new TableConfigurationsClientSide(targetTable, data, aoColumnsFuncCampaign(), true, [0, 'asc']);
        configurations.lengthMenu = [10, 15, 20, 30, 50, 100, 10000];

        if ($('#' + targetTable).hasClass('dataTable') === false) {

            createDataTableWithPermissions(configurations, undefined, targetPanel);
            showTitleWhenTextOverflow();
        } else {

            var oTableCmp = $("#campaignsTable").dataTable();
            oTableCmp.fnClearTable();
            oTableCmp.fnAddData(data);
        }
    } else {
        if (oTableCmp) {
            var oTableCmp = $("#campaignsTable").dataTable();
            oTableCmp.fnClearTable();
        }
    }
}



/***
 * Feed the TestCase select with all the testcase from test defined.
 * @param {String} selectElement - id of select to refresh.
 * @param {String} defaultCampaigns - value of default campaign.
 * @param {String} environments - list of selected environments.
 * @param {String} gp1s - list of selected gp1s.
 * @param {String} gp2s - list of selected gp2s.
 * @param {String} gp3s - list of selected gp3s.
 * @returns {null}
 */
function feedCampaignCombos(selectElement, defaultCampaigns, environments, gp1s, gp2s, gp3s) {

    var campaignList = $(selectElement);
    campaignList.empty();

    var jqxhr = $.getJSON("ReadCampaign");
    $.when(jqxhr).then(function (data) {
        for (var index = 0; index < data.contentTable.length; index++) {
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign + " - " + data.contentTable[index].description).val(data.contentTable[index].campaign));
        }
        $('#campaignSelect').val(defaultCampaigns);
        $('#campaignSelect').trigger('change');

        feedCampaignGpX("#gp1Select", data.distinct.group1);
        feedCampaignGpX("#gp2Select", data.distinct.group2);
        feedCampaignGpX("#gp3Select", data.distinct.group3);

    });
}

function feedSystemSelectOptions(selectElement) {
    var systemList = $(selectElement);
    systemList.empty();

    let user = JSON.parse(sessionStorage.getItem('user'));
    let systems = user.system;
    let options = $("#systemSelect").html("");
    $.each(systems, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        systemList.append(option);
    });
//    $('#systemSelect').html(options);
//    $("#systemSelect").multiselect('rebuild');
}

function feedCampaignGpX(selectId, data) {
    var select = $(selectId);
    select.multiselect('destroy');
    var array = data;
    $(selectId + " option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i];
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $(selectId).append($('<option></option>').text(n).val(array[i]));
    }
    select.multiselect(new multiSelectConfPerf(selectId));
}

function multiSelectConfPerf(name) {
    this.maxHeight = 450;
    this.checkboxName = name;
    this.buttonWidth = "100%";
    this.enableFiltering = true;
    this.enableCaseInsensitiveFiltering = true;
    this.includeSelectAllOption = true;
    this.includeSelectAllIfMoreThan = 4;
    this.numberDisplayed = 10;
}


function initPage() {
    var doc = new Doc();
    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    initGraph();
}

function displayPageLabel(doc) {
//    $("#pageTitle").html(doc.getDocLabel("page_campaignreportovertime", "title"));
//    $("#title").html(doc.getDocOnline("page_campaignreportovertime", "title"));
//    $("#loadbutton").html(doc.getDocLabel("page_global", "buttonLoad"));
//    $("#filters").html(doc.getDocOnline("page_global", "filters"));
}

/*
 * Loading functions
 */

function loadKPIGraphBars(saveURLtoHistory, environments, gp1s, gp2s, gp3s) {
    var user = getUser();

    showLoader($("#otFilterPanel"));
    showLoader($("#automateScoreChart"));
    showLoader($("#kpiScoreChart"));
    showLoader($("#trendChart"));
    showLoader($("#tabTestcases"));
    showLoader($("#tabCampaigns"));

    if (environments === null || environments === undefined) {
        environments = [];
    }

    let to = new Date($('#topicker').data("DateTimePicker").date());

    if ($("#envSelect").val() !== null) {
        environments = $("#envSelect").val();
    }
    len = environments.length;
    var environmentsQ = "";
    for (var i = 0; i < len; i++) {
        environmentsQ += "&environments=" + encodeURI(environments[i]);
    }

    var campaignString = "";
    if ($("#campaignSelect").val() !== null) {
        for (var i = 0; i < $("#campaignSelect").val().length; i++) {
            campaignString = campaignString + "&campaigns=" + encodeURI($("#campaignSelect").val()[i]);
        }
    }

    var systemString = "";
    if ($("#systemSelect").val() !== null) {
        for (var i = 0; i < $("#systemSelect").val().length; i++) {
            systemString = systemString + "&systems=" + encodeURI($("#systemSelect").val()[i]);
        }
    }

    if ($("#gp1Select").val() !== null) {
        gp1s = $("#gp1Select").val();
    }
    var gp1sQ = "";
    if (gp1s !== undefined) {
        len = gp1s.length;
        for (var i = 0; i < len; i++) {
            gp1sQ += "&group1s=" + encodeURI(gp1s[i]);
        }
    }

    if ($("#gp2Select").val() !== null) {
        gp2s = $("#gp2Select").val();
    }
    var gp2sQ = "";
    if (gp2s !== undefined) {
        len = gp2s.length;
        for (var i = 0; i < len; i++) {
            gp2sQ += "&group2s=" + encodeURI(gp2s[i]);
        }
    }

    if ($("#gp3Select").val() !== null) {
        gp3s = $("#gp3Select").val();
    }
    var gp3sQ = "";
    if (gp3s !== undefined) {
        len = gp3s.length;
        for (var i = 0; i < len; i++) {
            gp3sQ += "&group3s=" + encodeURI(gp3s[i]);
        }
    }

    nbWeeks = document.getElementById("trendWeeks").value;

    let qS = "nbWeeks=" + nbWeeks + "&to=" + mimicISOString(to) + campaignString + systemString + environmentsQ + gp1sQ + gp2sQ + gp3sQ;

    if (saveURLtoHistory) {
        InsertURLInHistory("./ReportingAutomateScore.jsp?" + qS);
    }



    $.ajax({
        url: "api/automatescore/statistics?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {

//            console.info(data);

            if (data.weekStats) {


                let labelsDatasets = [];

                let kpiFreqData = [];
                let kpiFreqColor = [];
                let kpiFreqPoint = [];

                let kpiStabData = [];
                let kpiStabColor = [];
                let kpiStabPoint = [];

                let kpiDurData = [];
                let kpiDurColor = [];
                let kpiDurPoint = [];

                let kpiMaintData = [];
                let kpiMaintColor = [];
                let kpiMaintPoint = [];


                drawTable_TestCases(data.testcases, "testcasesTable", "tabTestcases");
                drawTable_Campaigns(data.campaigns, "campaignsTable", "tabCampaigns");

                for (let index = 0; index < data.weeks.length; index++) {
                    labelsDatasets.push(data.weeks[index].label);

                    kpiFreqData.push(data.weekStats[data.weeks[index].val].kpiFrequency.value);
                    kpiFreqColor.push(getColorFromScore(data.weekStats[data.weeks[index].val].kpiFrequency.scoreL));
                    kpiFreqPoint.push(getPointFromScore(data.weekStats[data.weeks[index].val].kpiFrequency.scoreL));

                    kpiStabData.push(data.weekStats[data.weeks[index].val].kpiStability.value / 100);
                    kpiStabColor.push(getColorFromScore(data.weekStats[data.weeks[index].val].kpiStability.scoreL));
                    kpiStabPoint.push(getPointFromScore(data.weekStats[data.weeks[index].val].kpiStability.scoreL));

                    kpiDurData.push(data.weekStats[data.weeks[index].val].kpiDuration.value / 60000);
                    kpiDurColor.push(getColorFromScore(data.weekStats[data.weeks[index].val].kpiDuration.scoreL));
                    kpiDurPoint.push(getPointFromScore(data.weekStats[data.weeks[index].val].kpiDuration.scoreL));

                    kpiMaintData.push(data.weekStats[data.weeks[index].val].kpiMaintenance.value / 60000);
                    kpiMaintColor.push(getColorFromScore(data.weekStats[data.weeks[index].val].kpiMaintenance.scoreL));
                    kpiMaintPoint.push(getPointFromScore(data.weekStats[data.weeks[index].val].kpiMaintenance.scoreL));
                }

                renderScope(data.campaigns, data.testcases, data.applications);

                let lastKPI = data.weekStats[data.weeks[kpiFreqData.length - 1].val];
                renderKPIHeader("freqChart", lastKPI.kpiFrequency.value, "/week", "Execution Frequency", "Campaigns per week", lastKPI.kpiFrequency.scoreL, lastKPI.kpiFrequency.trend, lastKPI.kpiFrequency.varVsAll / 100);

                renderKPIHeader("relChart", lastKPI.kpiStability.value / 100, "%", "Stability", "Ratio of flaky and false negative", lastKPI.kpiStability.scoreL, lastKPI.kpiStability.trend, lastKPI.kpiStability.varVsAll / 100);

                renderKPIHeader("durChart", getHumanReadableDuration(lastKPI.kpiDuration.value / 1000), "", "Duration", "Average campaign duration", lastKPI.kpiDuration.scoreL, lastKPI.kpiDuration.trend, lastKPI.kpiDuration.varVsAll / 100);

                renderKPIHeader("mntChart", getHumanReadableDuration(lastKPI.kpiMaintenance.value / 1000), "", "Maintenance Effort", "Efforts", lastKPI.kpiMaintenance.scoreL, lastKPI.kpiMaintenance.trend, lastKPI.kpiMaintenance.varVsAll / 100);

                renderGlobalAS(lastKPI.kpi.scoreL);

                renderKPITrend("freqChart", "Campaign executions per week - " + nbWeeks + " Weeks Trend");
                let tagfreqdatasets = [{
                        label: 'Executions per Week',
                        data: kpiFreqData,
                        fill: false,
                        backgroundColor: kpiFreqColor,
                        pointStyle: kpiFreqPoint,
                        pointRadius: 10,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 3,
                        tension: 0.1
                    }];
                configHistoFreq.data.datasets = tagfreqdatasets;
                configHistoFreq.data.labels = labelsDatasets;
                window.myHistoFreq.update();


                renderKPITrend("relChart", "Ratio of flaky and false negative - " + nbWeeks + " Weeks Trend");
                let tagstabdatasets = [{
                        label: '% of Stability',
                        data: kpiStabData,
                        fill: false,
                        backgroundColor: kpiStabColor,
                        pointStyle: kpiStabPoint,
                        pointRadius: 10,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 3,
                        tension: 0.1
                    }];
                configHistoStab.data.datasets = tagstabdatasets;
                configHistoStab.data.labels = labelsDatasets;
                window.myHistoStab.update();


                renderKPITrend("durChart", "Test Campaign in minutes - " + nbWeeks + " Weeks Trend");
                let tagdurdatasets = [{
                        label: 'Campaign average duration',
                        data: kpiDurData,
                        fill: false,
                        backgroundColor: kpiDurColor,
                        pointStyle: kpiDurPoint,
                        pointRadius: 10,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 3,
                        tension: 0.1
                    }];
                configHistoDur.data.datasets = tagdurdatasets;
                configHistoDur.data.labels = labelsDatasets;
                window.myHistoDur.update();


                renderKPITrend("mntChart", "Efforts in minutes - " + nbWeeks + " Weeks Trend");
                let tagmntdatasets = [{
                        label: 'Maintenance hours',
                        data: kpiMaintData,
                        fill: false,
                        backgroundColor: kpiMaintColor,
                        pointStyle: kpiMaintPoint,
                        pointRadius: 10,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 3,
                        tension: 0.1
                    }];
                configHistoMnt.data.datasets = tagmntdatasets;
                configHistoMnt.data.labels = labelsDatasets;
                window.myHistoMnt.update();

                hideLoader($("#automateScoreChart"));
                hideLoader($("#kpiScoreChart"));
                hideLoader($("#trendChart"));
                hideLoader($("#tabTestcases"));
                hideLoader($("#tabCampaigns"));
            } else {
                showMessageMainPage("warning", data.message, true);

            }

            hideLoader($("#otFilterPanel"));
        }
    });

}
function renderScope(campaigns, testcases, applications) {
    $("#scopeCampaigns").text(getTextPlurial(campaigns.length, " Campaign", " Campaigns"));
    let titleDes = "";
    for (var i = 0; i < campaigns.length; i++) {
        titleDes += " / " + campaigns[i].campaign;
    }
    titleDes = titleDes.substring(3);
//    $("#scopeCampaigns").attr('data-toggle', 'tooltip').attr('data-original-title', 'toto');
    $("#scopeCampaigns").attr('title', titleDes);


    $("#scopeApplications").text(getTextPlurial(applications.length, " Application", " Applications"));
    titleDes = "";
    for (var i = 0; i < applications.length; i++) {
        titleDes += " / " + applications[i];
    }
    titleDes = titleDes.substring(3);
//    $("#scopeCampaigns").attr('data-toggle', 'tooltip').attr('data-original-title', 'toto');
    $("#scopeApplications").attr('title', titleDes);


    $("#scopeTests").text(getTextPlurial(testcases.length, " Test", " Tests"));
}

function getTextPlurial(nb, textSingle, textPlusial) {
    if (nb > 1) {
        return "" + nb + textPlusial;
    } else if (nb === 1) {
        return "" + nb + textSingle;
    } else {
        return "";
    }
}
function getColorFromScore(score) {
    switch (score) {
        case "A":
            return 'green';
        case "B":
            return 'lightgreen';
        case "C":
            return 'yellow';
        case "D":
            return 'orange';
        case "E":
            return 'red';
        default:
            return 'grey';
    }

}
function getPointFromScore(score) {
    switch (score) {
        case "A":
            return imageA;
        case "B":
            return imageB;
        case "C":
            return imageC;
        case "D":
            return imageD;
        case "E":
            return imageE;
        case "NA":
            return imageNA;
        default:
            return imageNA;
    }

}

function updateNbDistinct(data) {

    nbEnv = 0;
    for (var i = 0; i < data.environments.length; i++) {
        if (data.environments[i].isRequested) {
            nbEnv++;
        }
    }
}

function loadEnvironmentCombo(data) {

    var select = $("#envSelect");
    select.multiselect('destroy');
    var array = data.distinct.environments;
    $("#envSelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#envSelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#envSelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("envSelect"));
}

/**
 * 
 * Bar et UI rendering functions
 * 
 */


function getOptionsBar(title, unit) {
    let option = {
        legend: {
            display: false
        },
        responsive: true,
        maintainAspectRatio: false,
        tooltips: {
            callbacks: {
                label: function (t, d) {
                    newlabel = [];
                    var xLabel = d.datasets[t.datasetIndex].label;
                    if (unit === "size") {
                        newlabel.push(formatNumber(Math.round(t.yLabel / 1024)) + " kb");
                    } else if (unit === "time") {
                        newlabel.push(getHumanReadableDuration(t.yLabel * 60));
                    } else if (unit === "percentage") {
                        newlabel.push(t.yLabel + ' %');
                    } else if (unit === "nb") {
                        newlabel.push(t.yLabel);
                    } else {
                        newlabel.push(xLabel + ': ' + t.yLabel);
                    }
                    return newlabel;
                }
            }
        },
        title: {
            text: title
        },
        scales: {
            yAxes: [{
                    stacked: true,
                    ticks: {
                        beginAtZero: true
                    }
                }]
        }
    };
    return option;
}

function chartBarColorLabel(current, final) {
    return [current, current, current, current, final];
}

function renderGlobalAS(asValue) {

    $("#ASA").removeClass('btn-default btn-info active');
    $("#ASB").removeClass('btn-default btn-info active');
    $("#ASC").removeClass('btn-default btn-info active');
    $("#ASD").removeClass('btn-default btn-info active');
    $("#ASE").removeClass('btn-default btn-info active');

    $("#ASA").removeAttr('style');
    $("#ASB").removeAttr('style');
    $("#ASC").removeAttr('style');
    $("#ASD").removeAttr('style');
    $("#ASE").removeAttr('style');

    switch (asValue) {
        case "A":
            $("#ASA").addClass('btn-info active');
            $("#ASB").addClass('btn-default');
            $("#ASC").addClass('btn-default');
            $("#ASD").addClass('btn-default');
            $("#ASE").addClass('btn-default');
            $("#ASA").attr('style', 'font-size: 40px;background-color: green');
            $("#ASB").attr('style', 'font-size: 30px');
            $("#ASC").attr('style', 'font-size: 30px');
            $("#ASD").attr('style', 'font-size: 30px');
            $("#ASE").attr('style', 'font-size: 30px');
            break;
        case "B":
            $("#ASB").addClass('btn-info active');
            $("#ASA").addClass('btn-default');
            $("#ASC").addClass('btn-default');
            $("#ASD").addClass('btn-default');
            $("#ASE").addClass('btn-default');
            $("#ASB").attr('style', 'font-size: 40px;background-color: lightgreen');
            $("#ASA").attr('style', 'font-size: 30px');
            $("#ASC").attr('style', 'font-size: 30px');
            $("#ASD").attr('style', 'font-size: 30px');
            $("#ASE").attr('style', 'font-size: 30px');
            break;
        case "C":
            $("#ASC").addClass('btn-info active');
            $("#ASB").addClass('btn-default');
            $("#ASA").addClass('btn-default');
            $("#ASD").addClass('btn-default');
            $("#ASE").addClass('btn-default');
            $("#ASC").attr('style', 'font-size: 40px;background-color: yellow;color: black');
            $("#ASB").attr('style', 'font-size: 30px');
            $("#ASA").attr('style', 'font-size: 30px');
            $("#ASD").attr('style', 'font-size: 30px');
            $("#ASE").attr('style', 'font-size: 30px');
            break;
        case "D":
            $("#ASD").addClass('btn-info active');
            $("#ASB").addClass('btn-default');
            $("#ASC").addClass('btn-default');
            $("#ASA").addClass('btn-default');
            $("#ASE").addClass('btn-default');
            $("#ASD").attr('style', 'font-size: 40px;background-color: orange');
            $("#ASB").attr('style', 'font-size: 30px');
            $("#ASC").attr('style', 'font-size: 30px');
            $("#ASA").attr('style', 'font-size: 30px');
            $("#ASE").attr('style', 'font-size: 30px');
            break;
        case "E":
            $("#ASE").addClass('btn-info active');
            $("#ASB").addClass('btn-default');
            $("#ASC").addClass('btn-default');
            $("#ASD").addClass('btn-default');
            $("#ASA").addClass('btn-default');
            $("#ASE").attr('style', 'font-size: 40px;background-color: red');
            $("#ASB").attr('style', 'font-size: 30px');
            $("#ASC").attr('style', 'font-size: 30px');
            $("#ASD").attr('style', 'font-size: 30px');
            $("#ASA").attr('style', 'font-size: 30px');
            break;
        default:
            break;
    }
}

function renderKPITrend(idBlock, kpiDesc) {

    $("#" + idBlock + " [name='L1']").text(kpiDesc);
}

function renderKPIHeader(idBlock, kpiValue, kpiUnit, kpiDesc, kpiSubDesc, kpiScore, kpiVariation, kpiVariationValue) {

//    console.info("'''''''''''''''");
//    console.info("idBlock " + idBlock);
//    console.info("kpiValue " + kpiValue);
//    console.info("kpiUnit " + kpiUnit);
//    console.info("kpiDesc " + kpiDesc);
//    console.info("kpiSubDesc " + kpiSubDesc);
//    console.info("kpiStatus " + kpiScore);
//    console.info("kpiVariation " + kpiVariation);
//    console.info("kpiVariationValue " + kpiVariationValue);

    $("#" + idBlock + "Title [name='kpi']").text(kpiValue + " " + kpiUnit);

    $("#" + idBlock + "Title [name='title']").text(kpiDesc);

    $("#" + idBlock + "Title [name='subtitle']").text(kpiSubDesc);

    if (kpiScore !== "NA") {

        switch (kpiVariation) {
            case "OKUP":
                $("#" + idBlock + "Var [name='var']").html("<img width='20px' style='border-right: 20px;' src='images/AS-OKUP.png'> + " + kpiVariationValue + " %");
                break;
            case "KOUP":
                $("#" + idBlock + "Var [name='var']").html("<img width='20px' style='border-right: 20px;' src='images/AS-KOUP.png'> + " + kpiVariationValue + " %");
                break;
            case "OKDOWN":
                $("#" + idBlock + "Var [name='var']").html("<img width='20px' style='border-right: 20px;' src='images/AS-OKDOWN.png'> - " + kpiVariationValue + " %");
                break;
            case "KODOWN":
                $("#" + idBlock + "Var [name='var']").html("<img width='20px' style='border-right: 20px;' src='images/AS-KODOWN.png'> - " + kpiVariationValue + " %");
                break;
            case "ISO":
            case "NA":
                $("#" + idBlock + "Var [name='var']").html();
                break;
            default:
                break;
        }
    } else {
        $("#" + idBlock + "Var [name='var']").html();

    }

    $("#" + idBlock + "Score [name='ASA']").removeClass('active');
    $("#" + idBlock + "Score [name='ASB']").removeClass('active');
    $("#" + idBlock + "Score [name='ASC']").removeClass('active');
    $("#" + idBlock + "Score [name='ASD']").removeClass('active');
    $("#" + idBlock + "Score [name='ASE']").removeClass('active');

    $("#" + idBlock + "Score [name='AS" + kpiScore + "']").addClass('active');
}



function initGraph() {

    var tagfreqoption = getOptionsBar("Frequency", "nb", "linear");
    var tagstaboption = getOptionsBar("Stability", "percentage", "logarithmic");
    var tagduroption = getOptionsBar("Duration", "time", "linear");
    var tagmntoption = getOptionsBar("Maintenance", "time");

    let labelsDatasets = [];
    let listBackgroundColor = chartBarColorLabel('rgba(54, 162, 235, 1)', 'rgba(54, 162, 235, 1)');

    let tagfreqdatasets = [];
    let tagstabdatasets = [];
    let tagdurdatasets = [];
    let tagmntdatasets = [];

    let asdatasets = [];

    configHistoFreq = {
        type: 'line',
        data: {
            labels: labelsDatasets,
            datasets: tagfreqdatasets
        },
        options: tagfreqoption
    };
    configHistoStab = {
        type: 'line',
        data: {
            labels: labelsDatasets,
            datasets: tagstabdatasets
        },
        options: tagstaboption
    };
    configHistoDur = {
        type: 'line',
        data: {
            labels: labelsDatasets,
            datasets: tagdurdatasets
        },
        options: tagduroption
    };
    configHistoMnt = {
        type: 'line',
        data: {
            labels: labelsDatasets,
            datasets: tagmntdatasets
        },
        options: tagmntoption
    };


    // Execution Frequency
    var ctx = document.getElementById('canvasFreqStat').getContext('2d');
    window.myHistoFreq = new Chart(ctx, configHistoFreq);

    // Stability
    var ctx = document.getElementById('canvasRelStat').getContext('2d');
    window.myHistoStab = new Chart(ctx, configHistoStab);

    // Duration
    var ctx = document.getElementById('canvasDurStat').getContext('2d');
    window.myHistoDur = new Chart(ctx, configHistoDur);

    // Maintenance
    var ctx = document.getElementById('canvasMntStat').getContext('2d');
    window.myHistoMnt = new Chart(ctx, configHistoMnt);

}
