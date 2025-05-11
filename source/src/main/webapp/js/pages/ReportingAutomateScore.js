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
var configHistoRel = {};
var configHistoDur = {};
var configHistoMnt = {};

// Counters of different countries, env and robotdecli (used to shorten the labels)
var nbEnv = 0;

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

        $('#frompicker').datetimepicker({
            format: 'YYYY-MM-DD [(Week] WW YYYY)',
            keepOpen: false,
            calendarWeeks: true
        });

        var campaigns = GetURLParameters("campaigns");
        var from = GetURLParameter("from");

        var environments = GetURLParameters("environments");
        //        
        var gp1s = GetURLParameters("group1s");
        var gp2s = GetURLParameters("group2s");
        var gp3s = GetURLParameters("group3s");

        let fromD;
        if (from === null) {
            fromD = new Date();
            fromD.setMonth(fromD.getMonth() - 1);
        } else {
            fromD = new Date(from);
        }
        $('#frompicker').data("DateTimePicker").date(moment(fromD));

        $("#campaignSelect").empty();
        $("#campaignSelect").select2({width: "100%"});
        feedCampaignCombos("#campaignSelect", campaigns, environments, gp1s, gp2s, gp3s);

    });
});

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
    showLoader($("#otFilterPanel"));

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

        loadKPIGraphBars(false, environments, gp1s, gp2s, gp3s);

        hideLoader($("#otFilterPanel"));

    });
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
    showLoader($("#otFilterPanel"));

    if (environments === null || environments === undefined) {
        environments = [];
    }

    let from = new Date($('#frompicker').data("DateTimePicker").date());

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
            var campaignString = campaignString + "&campaigns=" + encodeURI($("#campaignSelect").val()[i]);
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

    let qS = "from=" + from + campaignString + environmentsQ + gp1sQ + gp2sQ + gp3sQ;

    if (saveURLtoHistory) {
        InsertURLInHistory("./ReportingAutomateScore.jsp?" + qS);
    }

    $.ajax({
        url: "ReadTagStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            if (data.messageType === "OK") {
                updateNbDistinct(data.distinct);
                loadEnvironmentCombo(data);

                renderGlobalAS("E");

                let labelsDatasets = ['W10', 'W11', 'W12', 'W13', 'W14'];

                renderKPIHeader("freqChart", "4", "runs per week", "OK", "ISO", "+20", "OK");
                let tagfreqdatasets = [{
                        label: '# / week',
                        data: [5, 3, 5, 2, 3],
                        backgroundColor: 'rgba(54, 162, 235, 1)',
                        borderColor: chartBarColorLabel('rgba(54, 162, 235, 1)', 'red'),
                        borderWidth: 1
                    }];
                configHistoFreq.data.datasets = tagfreqdatasets;
                configHistoFreq.data.labels = labelsDatasets;
                window.myHistoFreq.update();


                renderKPIHeader("relChart", "2", "flaky tests", "WARNING", "DOWN", "-1", "KO");
                let tagreldatasets = [{
                        label: '% of Flacky',
                        data: [6, 3, 5, 2, 3],
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        borderColor: chartBarColorLabel('rgba(54, 162, 235, 1)', 'green'),
                        borderWidth: 1
                    }];
                configHistoRel.data.datasets = tagreldatasets;
                configHistoRel.data.labels = labelsDatasets;
                window.myHistoRel.update();


                renderKPIHeader("durChart", "20", "min avg", "OK", "UP", "+5", "WARNING");
                let tagdurdatasets = [{
                        label: 'minutes',
                        data: [6, 3, 5, 1, 2],
                        backgroundColor: 'rgba(153, 102, 255, 1)',
                        borderColor: chartBarColorLabel('rgba(54, 162, 235, 1)', 'orange'),
                        borderWidth: 1
                    }];
                configHistoDur.data.datasets = tagdurdatasets;
                configHistoDur.data.labels = labelsDatasets;
                window.myHistoDur.update();


                renderKPIHeader("mntChart", "8", "hours", "KO", "DOWN", "-20", "OK");
                let tagmntdatasets = [{
                        label: 'hours',
                        data: [6, 3, 4, 2, 4],
                        backgroundColor: 'rgba(255, 159, 64, 1)',
                        borderColor: chartBarColorLabel('rgba(54, 162, 235, 1)', 'green'),
                        borderWidth: 1
                    }];
                configHistoMnt.data.datasets = tagmntdatasets;
                configHistoMnt.data.labels = labelsDatasets;
                window.myHistoMnt.update();

            }
            hideLoader($("#otFilterPanel"));
        }
    });
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
    console.info(asValue);
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
            $("#ASC").attr('style', 'font-size: 40px;background-color: yellow');
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

function renderKPIHeader(idBlock, kpiValue, kpiDesc, kpiStatus, kpiVariation, kpiVariationValue, kpiVariationStatus) {

    $("#" + idBlock + " [name='L1']").text(kpiValue + " " + kpiDesc);
    $("#" + idBlock + " [name='L1']").removeClass("ascommentOK ascommentKO ascommentWARNING");
    switch (kpiStatus) {
        case "OK":
            $("#" + idBlock + " [name='L1']").addClass("ascommentOK");
            break;
        case "KO":
            $("#" + idBlock + " [name='L1']").addClass("ascommentKO");
            break;
        case "WARNING":
            $("#" + idBlock + " [name='L1']").addClass("ascommentWARNING");
            break;
        default:
            break;
    }
    $("#" + idBlock + " [name='L2']").empty();
    switch (kpiVariation) {
        case "UP":
            $("#" + idBlock + " [name='L2']").html("<span class='glyphicon glyphicon-arrow-up' aria-hidden='true'></span> " + kpiVariationValue);
            break;
        case "DOWN":
            $("#" + idBlock + " [name='L2']").html("<span class='glyphicon glyphicon-arrow-down' aria-hidden='true'></span> " + kpiVariationValue);
            break;
        case "ISO":
            $("#" + idBlock + " [name='L2']").html("= " + kpiVariationValue);
            break;
        default:
            break;
    }
    $("#" + idBlock + " [name='L2']").removeClass("ascommentL2OK ascommentL2KO ascommentL2WARNING");
    switch (kpiVariationStatus) {
        case "OK":
            $("#" + idBlock + " [name='L2']").addClass("ascommentL2OK");
            break;
        case "KO":
            $("#" + idBlock + " [name='L2']").addClass("ascommentL2KO");
            break;
        case "WARNING":
            $("#" + idBlock + " [name='L2']").addClass("ascommentL2WARNING");
            break;
        default:
            break;
    }




}

function initGraph() {

    var tagfreqoption = getOptionsBar("Frequency", "time", "linear");
    var tagreloption = getOptionsBar("Reliability", "score", "logarithmic");
    var tagduroption = getOptionsBar("Duration", "nb", "linear");
    var tagmntoption = getOptionsBar("Maintenance", "nb");

    let labelsDatasets = [];
    let listBackgroundColor = chartBarColorLabel('rgba(54, 162, 235, 1)', 'rgba(54, 162, 235, 1)');

    let tagfreqdatasets = [];
    let tagreldatasets = [];
    let tagdurdatasets = [];
    let tagmntdatasets = [];

    let asdatasets = [];

    configHistoFreq = {
        type: 'bar',
        data: {
            labels: labelsDatasets,
            datasets: tagfreqdatasets
        },
        options: tagfreqoption
    };
    configHistoRel = {
        type: 'bar',
        data: {
            labels: labelsDatasets,
            datasets: tagreldatasets
        },
        options: tagreloption
    };
    configHistoDur = {
        type: 'bar',
        data: {
            labels: labelsDatasets,
            datasets: tagdurdatasets
        },
        options: tagduroption
    };
    configHistoMnt = {
        type: 'bar',
        data: {
            labels: labelsDatasets,
            datasets: tagmntdatasets
        },
        options: tagmntoption
    };


    // Execution Frequency
    var ctx = document.getElementById('canvasFreqStat').getContext('2d');
    window.myHistoFreq = new Chart(ctx, configHistoFreq);

    // Reliability
    var ctx = document.getElementById('canvasRelStat').getContext('2d');
    window.myHistoRel = new Chart(ctx, configHistoRel);

    // Duration
    var ctx = document.getElementById('canvasDurStat').getContext('2d');
    window.myHistoDur = new Chart(ctx, configHistoDur);

    // Maintenance
    var ctx = document.getElementById('canvasMntStat').getContext('2d');
    window.myHistoMnt = new Chart(ctx, configHistoMnt);

}
