<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@ page import="java.util.TreeMap" %>
<%@ page import="org.cerberus.crud.service.IDocumentationService" %>
<%@ page import="org.cerberus.crud.service.ITestService" %>
<%@ page import="org.cerberus.crud.service.IProjectService" %>
<%@ page import="org.cerberus.crud.service.IInvariantService" %>
<%@ page import="org.cerberus.crud.service.IBuildRevisionInvariantService" %>
<%@ page import="org.cerberus.crud.service.IApplicationService" %>
<%@ page import="org.cerberus.crud.service.IUserService" %>
<%@ page import="org.cerberus.crud.entity.Test" %>
<%@ page import="org.cerberus.crud.entity.Project" %>
<%@ page import="org.cerberus.crud.entity.Application" %>
<%@ page import="org.cerberus.crud.entity.BuildRevisionInvariant" %>
<%@ page import="org.cerberus.exception.CerberusException" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="include/function.jsp" %>
<%
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    ITestService testService = appContext.getBean(ITestService.class);
    IProjectService projectService = appContext.getBean(IProjectService.class);
    IInvariantService invariantService = appContext.getBean(IInvariantService.class);
    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
    IApplicationService applicationService = appContext.getBean(IApplicationService.class);
    IUserService userService = appContext.getBean(IUserService.class);
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Execution Reporting : Status</title>
    <%@ include file="include/dependenciesInclusions_old.html" %>
    <script type="text/javascript" src="dependencies/zz_OldDependencies/js/FixedHeader.js"></script>

<%
    try{
%>

    <script type="text/javascript">
        var oTable;
        var oTableStatistic;
        var oTableStatus;
        var oTableGroup;
        var postData;

        var country = [];
        var browser = [];
        var oldSystem = null;
        $(document).ready(function () {
            $(".multiSelectOptions").each(function () {
                var currentElement = $(this);

                if (currentElement.attr("id") === "System") {
                    currentElement.multiselect({
                        multiple: true,
                        minWidth: 150,
//                        header: currentElement.data('header'),
                        noneSelectedText: currentElement.data('none-selected-text'),
                        selectedText: currentElement.data('selected-text'),
                        selectedList: currentElement.data('selected-list'),
                        beforeclose: function() {
                            var system = $("#System").val();
                            var appSelect = $("#Application");


                            if (system === null) {
                                appSelect.find("option").removeAttr('disabled');
                                appSelect.find("option").removeAttr('selected');
                            } else {
                                if (oldSystem != null) {
                                    $.each(oldSystem, function(i, v){
                                        if ($.inArray(v, system) === -1) {
                                            appSelect.find("option:contains('["+v+"]')").removeAttr('selected');
                                        }
                                    });
                                }

                                if ($.inArray("All", system) >= 0){
                                    appSelect.find("option").removeAttr('disabled');
                                } else {
                                    appSelect.find("option").attr('disabled','disabled');
                                    $.each(system, function(i, v){
                                        appSelect.find("option:contains('["+v+"]')").removeAttr('disabled');
                                    });
                                }
                            }
                            appSelect.multiselect("refresh");

                            oldSystem = system;
                        }
                    }).multiselectfilter();
                } else {
                    currentElement.multiselect({
                        multiple: true,
                        minWidth: 150,
//                        header: currentElement.data('header'),
                        noneSelectedText: currentElement.data('none-selected-text'),
                        selectedText: currentElement.data('selected-text'),
                        selectedList: currentElement.data('selected-list')
                    }).multiselectfilter();
                }
            });

        $('#formReporting').submit(function(e){
            e.preventDefault();

            postData = $(this).serialize();
            country = $('#Country').val();
            browser= $('#Browser').val();
            var status = [
            <%
                AnswerList answerStatus = invariantService.readByIdname("TCESTATUS");
                for (Invariant status : (List<Invariant>)answerStatus.getDataList()){
                    out.print("'" + status.getValue() + "',");
                }
            %>
                    ];
            if (oTable != null) {
                oTable.fnClearTable();
                oTableStatistic.fnClearTable();
                oTableStatus.fnClearTable();
                oTableGroup.fnClearTable();
            }
            $('.fixedHeader').remove();
            $('.jsAdded').remove();

            $.each(country, function (index, elem) {
                $('.TCComment').before("<th class='jsAdded' colspan='" + (browser.length * 2) + "'>" + elem + "</th>");
                $.each(browser, function (i, e) {
                    $('#tableCountry').append("<th class='jsAdded' colspan='2'>" + e + "</th>");
                    $('#TCResult').append("<th class='TCResult jsAdded'></th><th class='TCTime jsAdded'></th>");
                });

                $('#statisticCountry').append("<th class='jsAdded' colspan='" + (status.length + 1) +"'>" + elem + "</th>");
                $.each(status, function(i, e){
                    $('#statisticStatus').append("<th class='"+e+" "+e+"F jsAdded'>"+ e +"</th>");
                });
                $('#statisticStatus').append("<th class='jsAdded' style='color:#000000'>TOTAL</th>");
            });

            $('#divReporting').show();

            oTable = $('#reporting').dataTable({
                "bServerSide": true,
                "sAjaxSource": "GetReport?"+postData,
                "bJQueryUI": true,
                "bProcessing": true,
                "bFilter": false,
                "bInfo": false,
                "bSort": false,
                "bPaginate": false,
                "bDestroy": true,
                "iDisplayLength": -1,
                "aoColumnDefs": [
                    {"aTargets": ['TCResult'],
                        "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                            if (oData[iCol] === "") {
                                $(nTd).addClass('NOINF');
                            } else {
                                $(nTd).addClass(oData[iCol].result);
                            }
                        },
                        "mRender": function (data, type, full) {
                            if (data != ""){
                                if (data.result === "NotExecuted"){
                                    return "<a target='_blank' class='" + data.result + "F' href='RunTests.jsp?Test="+full[0]+"&TestCase="+full[1]+"&Country="+data.country+"'>"+data.country+"</a>";
                                } else {
                                    getParameter("cerberus_executiondetail_use").then(function(data){
                                        if(data.value == "N"){
                                            return "<a target='_blank' class='" + data.result + "F' href='ExecutionDetail.jsp?id_tc=" + data.execID + "'>" + data.result + "</a>";
                                        }else{
                                            return "<a target='_blank' class='" + data.result + "F' href='ExecutionDetail2.jsp?executionId=" + data.execID + "'>" + data.result + "</a>";
                                        }
                                    });
                                }
                            } else{
                                return "";
                            }
                        }
                    },
                    {"aTargets": ['TCTime'],
                        "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                            if (oData[iCol-1] === "") {
                                $(nTd).addClass('NOINF');
                            }
                        }
                    },
                    {"aTargets": ['bugIDColumn'],
                        "mRender": function (data) {
                            var text = "";
                            if (data.bugID != ""){
                                text += "<a target='_blank' href='"+data.bugURL+"'>"+data.bugID+"</a> ";
                            }
                            if (data.targetSprint != ""){
                                text += "for "+data.targetSprint+"/"+data.targetRevision;
                            }
                            return text;
                        }
                    },
                    {"aTargets": ['testCaseColumn'],
                        "mRender": function (data, type, full) {
                            return "<a href='TestCase.jsp?Load=Load&Test="+full[0]+"&TestCase="+data+"' target='_blank'>"+data+"</a>";
                        }
                    },
                    {"aTargets": ['TCComment'],
                        "fnCreatedCell": function (nTd, sData, oData) {
                            $(nTd).editable("UpdateTestCaseField", {
                                type: "textarea",
                                onblur: "submit",
                                submitdata: {test: oData[0],testcase: oData[1], columnName: "comment"},
                                tooltip: "Doubleclick to edit...",
                                event : "dblclick",
                                placeholder: ""
                            });
                        }
                    }
                ],
                "fnInitComplete": function (oSettings, json) {
                    new FixedHeader(oTable, {
                        zTop: 98
                    });

                    $('.ui-corner-tl').append("<div style='font-weight: bold;font-family: Trebuchet MS; clear: both'>")
                            .append("<div style='float: left'><input id='ShowS' type='button' onclick='showStatistic();' value='Show Summary'></div>")
                            .append("<div style='float: left'>Legend : </div>")
                            .append("<div style='float: left;margin-left: 3px;margin-right: 3px;' title='FILTER : Use this checkbox to filter status.'><input type='checkbox' name='FILTER' class='filterDisplay' value='FILTER' onchange='filterDisplay($(this).is(\":checked\"))'><label title='FILTER'>FILTER</label></div>")
                            .append("<div class='OK' style='float: left;margin-left: 3px;margin-right: 3px;' title='OK : Test was fully executed and no bug are to be reported.'><input type='checkbox' id='FOK' name='OK' value='OK' class='filterCheckbox' disabled='disabled' onchange='toogleDisplay(this)'><label class='OKF' title='OK'>OK</label></div>")
                            .append("<div class='KO' style='float: left;margin-left: 3px;margin-right: 3px;' title='KO : Test was executed and bug have been detected.'><input type='checkbox' name='KO' id='FKO' value='KO' class='filterCheckbox' disabled='disabled' onchange='toogleDisplay(this)'><label  class='KOF' title='KO'>KO</label></div>")
                            .append("<div class='NA' style='float: left;margin-left: 3px;margin-right: 3px;' title='NA : Test could not be executed because some test data are not available.'><input type='checkbox' id='FNA' class='filterCheckbox' disabled='disabled' name='NA' value='NA' onchange='toogleDisplay(this)'><label  title='NA' class='NAF'>NA</label></div>")
                            .append("<div class='FA' style='float: left;margin-left: 3px;margin-right: 3px;' title='FA : Test could not be executed because there is a bug on the test.'><input type='checkbox' name='FA'  id='FFA' class='filterCheckbox' disabled='disabled' value='FA' onchange='toogleDisplay(this)'><label  class='FAF'>FA</label></div>")
                            .append("<div class='PE' style='float: left;margin-left: 3px;margin-right: 3px;' title='PE : Test execution is still running...'><input type='checkbox' name='PE' value='PE' class='filterCheckbox' id='FPE' disabled='disabled' onchange='toogleDisplay(this)'><label class='PEF'>PE</label></div>")
                            .append("<div class='NotExecuted' style='float: left;margin-left: 3px;margin-right: 3px;' title='Test Case has not been executed for that country.'><span class='NotExecutedF'>XX</span></div>")
                            .append("<div class='NOINF' style='float: left;margin-left: 3px;margin-right: 3px;' title='Test Case not available for the country XX.'><span class='NOINFF'>XX</span></div>")
                            .append("<divstyle='float: left;margin-left: 3px;margin-right: 3px;' title='URL for quick access'><a href='./ReportingExecution.jsp?"+json.requestUrl+"'><b>URL for quick access</b></a></div>")
                            .append("</div>");

                    $('#reporting').find('tbody tr').on('click',function() {
                        $('#reporting').find('tbody tr').removeClass('row_selected');
                        $(this).addClass('row_selected');
                    });

                    $('#divStatistic').show();
                    oTableStatistic = $('#statistic').dataTable({
                        "aaData": json.statistic.aaData,
                        "bJQueryUI": false,
                        "bFilter": false,
                        "bInfo": false,
                        "bSort": false,
                        "bPaginate": false,
                        "bDestroy": true,
                        "bAutoWidth": false,
                        "iDisplayLength": -1,
                        "fnInitComplete": function () {
                            var s = $('#statistic');
                            s.find('thead th').css('padding', '0px');
                            s.css({'width': 'auto', 'margin': '0px'});
                            $('#divStatistic').hide();
                        }
                    });

                    $('#divStatus').show();
                    oTableStatus = $('#tableStatus').dataTable({
                        "aaData": json.status.aaData,
                        "bJQueryUI": false,
                        "bFilter": false,
                        "bInfo": false,
                        "bSort": false,
                        "bPaginate": false,
                        "bDestroy": true,
                        "bAutoWidth": false,
                        "iDisplayLength": -1,
                        "fnInitComplete": function () {
                            var ts = $('#tableStatus');
                            ts.find('thead th').css('padding', '0px');
                            ts.find('td').css('padding', '0px');
                            ts.css({'width': 'auto', 'margin': '0px', 'text-align': 'center'});
                            $('#divStatus').hide();
                        }
                    });

                    $('#divGroup').show();
                    oTableGroup = $('#tableGroup').dataTable({
                        "aaData": json.groups.aaData,
                        "bJQueryUI": false,
                        "bFilter": false,
                        "bInfo": false,
                        "bSort": false,
                        "bPaginate": false,
                        "bDestroy": true,
                        "bAutoWidth": false,
                        "iDisplayLength": -1,
                        "fnInitComplete": function () {
                            var tg = $('#tableGroup');
                            tg.find('thead th').css('padding', '0px');
                            tg.find('td').css('padding', '0px');
                            tg.css({'width': 'auto', 'margin': '0px', 'text-align': 'center'});
                            $('#divGroup').hide();
                        }
                    });

                    $('html, body').animate({scrollTop : 425},800);
                }
            });
        });
    });

    function filterDisplay(checked) {
        if(checked) {
            $('#reporting').find('tbody tr').hide();

            $('input.filterCheckbox').removeAttr('disabled');
            $('input.filterDisplay').attr('checked','checked');
        } else {
            $('#reporting').find('tbody tr').show();

            $('input.filterCheckbox').attr('disabled','disabled').removeAttr('checked');
            $('input.filterDisplay').removeAttr('checked');
        }
    }

    function toogleDisplay(input) {
        input = $(input);
        var value = input.val();
        if(input.is(':checked')) {
            $('td.'+value).parent().show();
        } else {
            $('td.'+value).parent().hide();
        }
    }

    function saveFilters(){
        var data = $('#formReporting').serialize();

        $.ajax({
            type: "POST",
            url: "UpdateMyUserReporting",
            data: {reporting: data, login: "<%=request.getUserPrincipal().getName()%>"}
        });
    }

    function showStatistic(){
        $('.fixedHeader').remove();

        $('#divReporting').hide();
        $('#divStatistic').show();
        $('#divStatus').show();
        $('#divGroup').show();

        new FixedHeader(oTableStatistic, {
            zTop: 98
        });
    }

    function hideStatistic(){
        $('.fixedHeader').remove();

        $('#divStatistic').hide();
        $('#divStatus').hide();
        $('#divGroup').hide();
        $('#divReporting').show();

        new FixedHeader(oTable, {
            zTop: 98
        });
    }

    function getParameter(param,sys,forceReload){
        var cacheEntryName = "PARAMETER_"+param;
        if (forceReload) {
            sessionStorage.removeItem(cacheEntryName);
        }
        var system = sys!=undefined?"&system="+sys:"";
        return new Promise(function(resolve, reject){
            var parameter = JSON.parse(sessionStorage.getItem(cacheEntryName));
            if(parameter === null){
                $.get("ReadParameter?param="+param+system, function(data){
                    sessionStorage.setItem(cacheEntryName,JSON.stringify(data.contentTable))
                    resolve(data.contentTable);
                });
            }else{
                resolve(parameter);
            }
        });
    }

    function compareArrays(arr1, arr2) {
        return $(arr1).not(arr2).length == 0 && $(arr2).not(arr1).length == 0
    }
    </script>
    <style>
        .underlinedDiv{
            padding-top: 15px;
        }
        div.FixedHeader_Cloned th,
        div.FixedHeader_Cloned td {
            background-color: white !important;
        }

        tr.row_selected {
            background-color: rgba(248, 255, 33, 0.45);
        }
    </style>
</head>
<body>
<%@ include file="include/header.jsp" %>

<%!
    String getParam(String param) {
        return (param != null && !param.isEmpty()) ? param : "";
    }
%>

<%
    String myLang = request.getAttribute("MyLang").toString();
    String ip = getParam(request.getParameter("Ip"));
    String port = getParam(request.getParameter("Port"));
    String tag = getParam(request.getParameter("Tag"));
    String browserFullVersion = getParam(request.getParameter("BrowserFullVersion"));
    String comment = getParam(request.getParameter("Comment"));

    String systemBR; // Used for filtering Build and Revision.
    if (request.getParameter("SystemExe") != null && request.getParameter("SystemExe").compareTo("All") != 0) {
        systemBR = request.getParameter("SystemExe");
    } else {
        if (request.getParameter("system") != null && !request.getParameter("system").isEmpty()) {
            systemBR = request.getParameter("system");
        } else {
            systemBR = request.getAttribute("MySystem").toString();
        }
    }
%>

<%
    TreeMap<String, String> options = new TreeMap<String, String>();

    User usr = userService.findUserByKey(request.getUserPrincipal().getName());
    String reportingFavorite = "ReportingExecution.jsp?"+usr.getReportingFavorite();
%>
<div class="filters" style="text-align: left; width:100%;">
<div style="display: block; width: 100%">
    <p class="dttTitle" style="float:left">Filters</p>

    <div id="dropDownUpArrow" style="display:none;">
        <a onclick="switchDivVisibleInvisible('filtersList', 'dropDownUpArrow');switchDivVisibleInvisible('dropDownDownArrow', 'dropDownUpArrow')">
            <img src="images/dropdown.gif"/>
        </a>
    </div>
    <div id="dropDownDownArrow" style="display: inline-block">
        <a onclick="switchDivVisibleInvisible('dropDownUpArrow', 'filtersList'); switchDivVisibleInvisible('dropDownUpArrow', 'dropDownDownArrow')">
            <img src="images/dropdown.gif"/>
        </a>
    </div>
</div>
<div id="filtersList" style="display:block">
<form id="formReporting" onsubmit="return hideStatistic()">
    <div>
        <div class="underlinedDiv"></div>
        <p style="text-align:left" class="dttTitle">Testcase Filters (Displayed Rows)</p>

        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("test", "Test", "Test", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        for (Test testL : testService.getListOfTest()) {
                            options.put(testL.getTest(), testL.getTest());
                        }
                    %>
                    <%=generateMultiSelect("Test", request.getParameterValues("Test"), options, "Select a test",
                            "Select Test", "# of # Test selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("project", "idproject", "Project", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        for (Project project : projectService.convert(projectService.readAll())) {
                            if (project.getIdProject() != null && !"".equals(project.getIdProject().trim())) {
                                options.put(project.getIdProject(), project.getIdProject() + " - " + project.getDescription());
                            }
                        }
                    %>
                    <%=generateMultiSelect("Project", request.getParameterValues("Project"), options, "Select a project",
                            "Select Project", "# of # Project selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("application", "System", "System", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        AnswerList answerSystem = invariantService.readByIdname("SYSTEM");
                        for (Invariant systemInv : (List<Invariant>)answerSystem.getDataList()) {
                            options.put(systemInv.getValue(), systemInv.getValue());
                        }
                    %>
                    <%=generateMultiSelect("System", request.getParameterValues("System"), options, "Select a sytem",
                            "Select System", "# of # System selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("application", "Application", "Application", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        for(Application app : applicationService.convert(applicationService.readAll())){
                            options.put(app.getApplication(), app.getApplication()+" ["+app.getSystem()+"]");
                        }
                    %>
                    <%=generateMultiSelect("Application", request.getParameterValues("Application"), options,
                            "Select an application", "Select Application", "# of # Application selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "tcactive", "TestCase Active", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        options.put("Y", "Yes");
                        options.put("N", "No");
                    %>
                    <%=generateMultiSelect("TcActive", request.getParameterValues("TcActive"), options,
                            "Select Activation state", "Select Activation", "# of # Activation state selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("invariant", "PRIORITY", "Priority", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        AnswerList ansPriority = invariantService.readByIdname("PRIORITY");
                        for (Invariant statusInv : (List<Invariant>) ansPriority.getDataList()) {
                            options.put(statusInv.getValue(), statusInv.getValue());
                        }
                    %>
                    <%=generateMultiSelect("Priority", request.getParameterValues("Priority"), options, "Select a Priority",
                            "Select Priority", "# of # Priority selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "Status", "Status", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        AnswerList ansStatus = invariantService.readByIdname("TCSTATUS");
                        List<Invariant> statusList = (List<Invariant>) ansStatus.getDataList();
                        for (Invariant statusInv : statusList) {
                            options.put(statusInv.getValue(), statusInv.getValue());
                        }
                    %>
                    <%=generateMultiSelect("Status", request.getParameterValues("Status"), options, "Select an option",
                            "Select Status", "# of # Status selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("invariant", "GROUP", "Group", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        AnswerList ansGroup = invariantService.readByIdname("GROUP");
                        List<Invariant> groupList = (List<Invariant>)ansGroup.getDataList();
                        for (Invariant statusInv : groupList) {
                            if(!statusInv.getValue().isEmpty()){
                                options.put(statusInv.getValue(), statusInv.getValue());
                            }
                        }
                    %>
                    <%=generateMultiSelect("Group", request.getParameterValues("Group"), options, "Select a Group",
                            "Select Group", "# of # Group selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "targetBuild", "targetBuild", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        List<BuildRevisionInvariant> buildList = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(systemBR, 1));
                        for (BuildRevisionInvariant myBR : buildList) {
                            options.put(myBR.getVersionName(), myBR.getVersionName());
                        }
                    %>
                    <%=generateMultiSelect("TargetBuild", request.getParameterValues("TargetBuild"), options,
                            "Select a Target Build", "Select Target Build", "# of # Target Build selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "targetRev", "targetRev", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        List<BuildRevisionInvariant> revisionList = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(systemBR, 2));
                        for (BuildRevisionInvariant myBR : revisionList) {
                            options.put(myBR.getVersionName(), myBR.getVersionName());
                        }
                    %>
                    <%=generateMultiSelect("TargetRev", request.getParameterValues("TargetRev"), options,
                            "Select a Target Rev", "Select Target Rev", "# of # Target Rev selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "creator", "Creator", myLang)%>
                </div>
                <div>
                    <%
                        options.clear();
                        for(User user : userService.findallUser()){
                            options.put(user.getLogin(), user.getLogin());
                        }
                    %>
                    <%=generateMultiSelect("Creator", request.getParameterValues("Creator"), options, "Select a Creator",
                            "Select Creator", "# of # Creator selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "implementer", "implementer", myLang)%>
                </div>
                <div>
                    <%=generateMultiSelect("Implementer", request.getParameterValues("Implementer"), options,
                            "Select an Implementer", "Select Implementer", "# of # Implementer selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "comment", "comment", myLang)%>
                </div>
                <div>
                    <input style="font-weight: bold; width: 130px; height:16px" id="Comment" name="Comment" value="<%=comment%>"/>
                </div>
            </div>
        </div>
    </div>
    <div style="clear:both">
        <div class="underlinedDiv"></div>
        <p class="dttTitle">Testcase Execution Filters (Displayed Content)</p>

        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("invariant", "Environment", "Environment", myLang)%>
            </div>
            <div>
                <%
                    options.clear();
                    AnswerList ansEnv = invariantService.readByIdname("ENVIRONMENT");
                    for (Invariant statusInv : (List<Invariant>)ansEnv.getDataList()) {
                        options.put(statusInv.getValue(), statusInv.getValue());
                    }
                %>
                <%=generateMultiSelect("Environment", request.getParameterValues("Environment"), options,
                        "Select an Environment", "Select Environment", "# of # Environment selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "Build", myLang)%>
            </div>
            <div>
                <%
                    options.clear();
                    for (BuildRevisionInvariant myBR : buildList) {
                        options.put(myBR.getVersionName(), myBR.getVersionName());
                    }
                %>
                <%=generateMultiSelect("Build", request.getParameterValues("Build"), options, "Select a Build",
                        "Select Build", "# of # Build selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "Revision", myLang)%>
            </div>
            <div>
                <%
                    options.clear();
                    for (BuildRevisionInvariant myBR : revisionList) {
                        options.put(myBR.getVersionName(), myBR.getVersionName());
                    }
                %>
                <%=generateMultiSelect("Revision", request.getParameterValues("Revision"), options, "Select a Revision",
                        "Select Revision", "# of # Revision selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "IP", "Ip", myLang)%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Ip" id="Ip" value="<%=ip%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "Port", "Port", myLang)%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Port" id="Port" value="<%=port%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "tag", "Tag", myLang)%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Tag" id="Tag" value="<%=StringEscapeUtils.escapeHtml4(tag)%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "browserfullversion", "", myLang)%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="BrowserFullVersion"
                       id="BrowserFullVersion" value="<%=browserFullVersion%>"/>
            </div>
        </div>
    </div>
    <div style="clear:both">
        <div class="underlinedDiv"></div>
        <p class="dttTitle">Execution Context Filters (Displayed Columns)</p>

        <div style="float: left">
            <div>
                Country <span class="error-message required">*</span>
            </div>
            <div>
                <%
                    options.clear();
                    AnswerList ansCountry = invariantService.readByIdname("COUNTRY");
                    for (Invariant countryInv :  (List<Invariant>)ansCountry.getDataList()) {
                        options.put(countryInv.getValue(), countryInv.getValue() + " - " + countryInv.getDescription());
                    }
                %>
                <%=generateMultiSelect("Country", request.getParameterValues("Country"), options, "Select a country",
                        "Select Country", "# of # Country selected", 1, false)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "Browser", "browser", myLang)%> <span
                    class="error-message required">*</span>
            </div>
            <div>
                <%
                    options.clear();
                    AnswerList ansBrowser = invariantService.readByIdname("BROWSER");
                    for (Invariant browserInv : (List<Invariant>)ansBrowser.getDataList()) {
                        options.put(browserInv.getValue(), browserInv.getValue());
                    }
                %>
                <%=generateMultiSelect("Browser", request.getParameterValues("Browser"), options, "Select a Browser",
                        "Select Browser", "# of # Browser selected", 1, false)%>
            </div>
        </div>
        <div style="clear:both; text-align: left">
            <br><span class="error-message required">* Required Fields</span>
        </div>
    </div>
    <div style="clear:both">
        <div class="underlinedDiv"></div>
    </div>
    <div>
        <input id="apply" type="submit" name="Apply" value="Apply">
        <input id="loadFilters" type="button" name="defaultFilter" value="Select My Default Filters" onclick="loadReporting('<%=reportingFavorite%>')">
        <input id="button" type="button" value="Set As My Default Filter" onclick="saveFilters()">
    </div>
</form>
</div>
</div>
<div id="divReporting" style="display: none; margin-top: 25px">
    <table id="reporting" class="display" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
        <tr>
            <th rowspan="3">Test</th>
            <th rowspan="3" class="testCaseColumn">TestCase</th>
            <th rowspan="3">Application</th>
            <th rowspan="3">Description</th>
            <th rowspan="3">Priority</th>
            <th rowspan="3">Status</th>
            <th rowspan="3" class="TCComment">Comment</th>
            <th rowspan="3" class="bugIDColumn">Bug ID</th>
            <th rowspan="3">Group</th>
        </tr>
        <tr id="tableCountry"></tr>
        <tr id="TCResult"></tr>
        </thead>
        <tbody></tbody>
    </table>
</div>

<div id="divStatistic" style="margin-top: 25px; display: none">
    <div style='float: left'>
        <input id='ShowD' type='button' onclick='hideStatistic();' value='Show Details'>
    </div>
    <table id="statistic" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
            <tr id="statisticCountry">
                <th rowspan="2">Tests</th>
            </tr>
            <tr id="statisticStatus">
            </tr>
        </thead>
    </table>
</div>

<div id="divGroup" style="margin-top: 25px; display: none">
    <table id="tableGroup" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
            <tr id="groupHeader">
                <th>Tests</th>
                <%
                    for(Invariant inv : groupList){
                        out.println("<th>"+inv.getValue()+"</th>");
                    }
                %>
                <th>TOTAL</th>
            </tr>
        </thead>
    </table>
</div>

<div id="divStatus" style="margin-top: 25px; display: none;">
    <table id="tableStatus" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
            <tr id="statusHeader">
                <th>Tests</th>
                <%
                    for(Invariant inv : statusList){
                        out.println("<th>"+inv.getValue()+"</th>");
                    }
                %>
                <th>TOTAL</th>
            </tr>
        </thead>
    </table>
</div>

</body>
<%
    } catch (CerberusException ex){
        MyLogger.log("ReportingExecution.jsp", Level.ERROR, "Cerberus exception : " + ex.toString());
        out.println("</script>");
        out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
        out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
    }
%>
</html>