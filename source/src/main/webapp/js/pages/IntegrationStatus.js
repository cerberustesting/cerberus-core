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

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();

        bindToggleCollapse();

        $("#loadLastModifbutton").click(loadHistoTable);

        var select = $('#selectEngGp');
        select.append($('<option></option>').text("-- ALL --").val("ALL"));
        displayInvariantList("selectEngGp", "ENVGP", false);
        displayInvariantList("selectSince", "FILTERNBDAYS", false);

        //Loading interation status table
        loadBuildRevTable();

        //Loading history deploy table
        loadHistoTable();

        //open Run navbar Menu
        openNavbarMenu("navMenuIntegration");

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

/*
 * Loading functions
 */

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);

}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("page_integrationstatus", "title"));
    $("#title").html(doc.getDocOnline("page_integrationstatus", "title"));

    $("#reportChanges").html(doc.getDocOnline("page_integrationstatus", "lastChanges"));
    $("#selectEngGpLabel").html(doc.getDocOnline("invariant", "ENVGP"));
    $("#selectSinceLabel").html(doc.getDocOnline("invariant", "FILTERNBDAYS"));
    $("#loadLastModifbutton").html(doc.getDocLabel("page_global", "buttonLoad"));

    $("#reportStatus").html(doc.getDocOnline("page_integrationstatus", "environmentStatus"));
    $("#buildHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname01"));
    $("#revisionHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname02"));
    $("#devHeader").html(doc.getDocOnline("page_integrationstatus", "DEV"));
    $("#qaHeader").html(doc.getDocOnline("page_integrationstatus", "QA"));
    $("#uatHeader").html(doc.getDocOnline("page_integrationstatus", "UAT"));
    $("#prodHeader").html(doc.getDocOnline("page_integrationstatus", "PROD"));
}

function loadHistoTable() {

    var nbDays = $("#selectSince").val();
    var envGp = $("#selectEngGp").val();

    var urlParam = "q=1" + getUser().defaultSystemsQuery;
    if (nbDays === null) {
        urlParam += "&nbdays=14";
    } else {
        urlParam += "&nbdays=" + nbDays;
    }
    if (envGp !== "ALL")
        urlParam += "&envgp=" + envGp;

    $('#histoTableHead tr').remove();
    $('#histoTableBody tr').remove();
    var jqxhr = $.getJSON("GetEnvironmentsLastChangePerCountry", urlParam);
    $.when(jqxhr).then(function (result) {

        var tableHead = $("#histoTableHead");
        var row = $("<tr></tr>");
        $.each(result["contentTable"], function (idx, obj) {
            var countryCol = $("<th></th>").append(obj.value);
            row.append(countryCol);
        });
        tableHead.append(row);

        var tableBody = $("#histoTableBody");
        var row = $("<tr></tr>");
        $.each(result["contentTable"], function (idx, obj) {
            var countryCol = $("<td></td>").append(obj.contentTable.length).append(" Changes");
            countryCol.append("<br>");
            for (var option in obj.contentTable) {
                dateold = obj.contentTable[option].datecre;
                var date = new Date(dateold);
                var formatted = date.getDate() + "/" + (date.getMonth() + 1);
//                var formatted = $.format.date(new Date(dateold), 'yyyy-MM-dd HH:mm:ss');
                countryCol
                        .append("<div style=\"text-align: right; bold;\">").append(formatted).append("</div><br>")
                        .append("&nbsp;&nbsp;&nbsp;[").append(obj.contentTable[option].system).append(" ").append(obj.contentTable[option].environment).append("]")
                        .append("<br>")
                        .append("&nbsp;&nbsp;&nbsp;").append(obj.contentTable[option].build).append(" ")
                        .append(obj.contentTable[option].revision)
                        .append("<br>");

//            $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value).val(data[option].value));
            }
            row.append(countryCol);
        });
        tableBody.append(row);

    }).fail(handleErrorAjaxAfterTimeout);
}

function loadBuildRevTable() {
    $('#envTableBody tr').remove();
    var jqxhr = $.getJSON("GetEnvironmentsPerBuildRevision", "q=1" + getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendBuildRevRow(obj);
        });
    }).fail(handleErrorAjaxAfterTimeout);
}

function counterFormated(system, nb, build, revision, envGP) {
    if (nb === 0) {
        return "";
    } else {
        return "<a href=\"Environment.jsp?" + "&system=" + system + "&build=" + build + "&revision=" + revision + "&envgp=" + envGP + "&active=Y\">" + nb + "</a>"
    }
}

function appendBuildRevRow(dtb) {
    var doc = new Doc();
    var table = $("#envTableBody");

    var toto = counterFormated(dtb.nbEnvDEV);

    var row = $("<tr></tr>");
    var systemCell = $("<th></th>").append(dtb.system);
    var buildCell = $("<th></th>").append(dtb.build);
    var revCell = $("<th></th>").append(dtb.revision);
    var nbdev = $("<th style=\"text-align: right;\"></th>").append(counterFormated(dtb.system, dtb.nbEnvDEV, dtb.build, dtb.revision, "DEV"));
    var nbqa = $("<th style=\"text-align: right;\"></th>").append(counterFormated(dtb.system, dtb.nbEnvQA, dtb.build, dtb.revision, "QA"));
    var nbuat = $("<th style=\"text-align: right;\"></th>").append(counterFormated(dtb.system, dtb.nbEnvUAT, dtb.build, dtb.revision, "UAT"));
    var nbprod = $("<th style=\"text-align: right;\"></th>").append(counterFormated(dtb.system, dtb.nbEnvPROD, dtb.build, dtb.revision, "PROD"));

    row.append(systemCell);
    row.append(buildCell);
    row.append(revCell);
    row.append(nbdev);
    row.append(nbqa);
    row.append(nbuat);
    row.append(nbprod);
    table.append(row);
}

