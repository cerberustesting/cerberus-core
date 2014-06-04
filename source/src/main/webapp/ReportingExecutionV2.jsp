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
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.cerberus.service.*" %>
<%@ page import="org.cerberus.service.impl.InvariantService" %>
<%@ page import="org.cerberus.entity.*" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Execution Reporting : Status</title>

    <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/>

    <link rel="stylesheet" type="text/css" href="css/crb_style.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
    <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
    <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="js/FixedHeader.js"></script>
    <script type="text/javascript" src="js/jquery.multiselect.js"></script>

    <script type="text/javascript">
        var oTable;

        //        var country = ["BE", "CH", "ES", "FR", "IT", "PT", "RU", "UK", "VI"];
        <% int countries = 3; %>
        var country = ["PT", "ES", "BE"];
        var browser = ["firefox"];
        $(document).ready(function () {
            $(".multiSelectOptions").each(function () {
                var currentElement = $(this);
                currentElement.multiselect({
                    multiple: true,
                    minWidth: 150,
                    header: currentElement.data('header'),
                    noneSelectedText: currentElement.data('none-selected-text'),
                    selectedText: currentElement.data('selected-text'),
                    selectedList: currentElement.data('selected-list')
                });
            });

            $.each(country, function (index, elem) {
                $('#TCComment').before("<th colspan='" + (browser.length * 2) + "'>" + elem + "</th>");
                $.each(browser, function (i, e) {
                    $('#tableCountry').append("<th colspan='2'>" + e + "</th>");
                    $('#TCResult').append("<th></th><th></th>");
                });
            });

            oTable = $('#reporting').dataTable({
                "bServerSide": true,
                "sAjaxSource": "GetReport",
                "bJQueryUI": true,
                "bProcessing": true,
                "bFilter": false,
                "bInfo": false,
                "bSort": false,
                "bPaginate": false,
                "iDisplayLength": -1,
                "aoColumnDefs": [
                    <% for(int i = 0; i < countries; i++){ %>
                    {"aTargets": [<%=6+i*2%>],
                        "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                            if (oData[<%=6+i*2%>] === "") {
                                $(nTd).addClass('NOINF');
                            } else {
                                $(nTd).addClass(oData[<%=6+i*2%>]);
                            }
                        },
                        "mRender": function (data, type, full) {
                            return "<a class='" + data + "F' href='ExecutionDetail.jsp?id_tc='>" + data + "</a>";
                        }
                    },
                    <% } %>
                ],
                "fnServerParams": function (aoData) {
                    aoData.push({ "name": "country", "value": country }, { "name": "browser", "value": browser });
                },
                "fnInitComplete": function () {
                    new FixedHeader(oTable, {
                        left: true,
                        leftColumns: 2,
                        zTop: 98
                    });

                    $('.FixedHeader_Left table tr#tableCountry th').remove();
                    $('.FixedHeader_Left table tr#TCResult th').remove();
                }
            });
        });
    </script>
    <style>
        div.FixedHeader_Cloned th,
        div.FixedHeader_Cloned td {
            background-color: white !important;
        }
    </style>
</head>
<body>
<%@ include file="include/header.jsp" %>

<%!
    String getParam(String param) {
        return (param != null && !param.isEmpty()) ? param : "";
    }

    String generateMultiSelect(String parameterName, String[] parameters, TreeMap<String, String> options, String headerText,
                               String noneSeletedText, String selectedText, int selectedList, boolean firstValueAll) {
        String parameter = "";
        if (parameters != null && parameters.length > 0 && (parameters[0]).compareTo("All") != 0) {
            parameter = StringUtils.join(parameters, ",");
        }
        parameter += ",";

        String select = "<select class=\"multiSelectOptions\" multiple  "
                + "data-header=\"" + headerText + "\" "
                + "data-none-selected-text=\"" + noneSeletedText + "\" "
                + "data-selected-text=\"" + selectedText + "\" "
                + "data-selected-list=\"" + selectedList + "\" "
                + "size=\"3\" id=\"" + parameterName + "\" name=\"" + parameterName + "\">\n";
        if (firstValueAll) {
            select += "<option value=\"All\">-- ALL --</option>\n";
        }
        for (String key : options.keySet()) {
            select += " <option value=\"" + key + "\"";

            if ((parameter != null) && (parameter.indexOf(key + ",") >= 0)) {
                select += " SELECTED ";
            }
            select += ">" + options.get(key) + "</option>\n";
        }
        select += "</select>\n";
        select += "<!-- " + parameter + " -->\n";
        return select;
    }
%>

<%
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
//            systemBR = request.getAttribute("MySystem").toString();
            systemBR = "VC";
        }
    }
%>

<%
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    ITestService testService = appContext.getBean(ITestService.class);
    IProjectService projectService = appContext.getBean(IProjectService.class);
    IInvariantService invariantService = appContext.getBean(InvariantService.class);
    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);

    TreeMap<String, String> options = new TreeMap<String, String>();
%>
<div class="filters" style="text-align: left; width:100%;">
<div style="display: block; width: 100%">
    <p class="dttTitle" style="float:left">Filters</p>

    <div id="dropDownUpArrow" style="display:none;">
        <a onclick="javascript:switchDivVisibleInvisible('filtersList', 'dropDownUpArrow');switchDivVisibleInvisible('dropDownDownArrow', 'dropDownUpArrow')">
            <img src="images/dropdown.gif"/>
        </a>
    </div>
    <div id="dropDownDownArrow" style="display: inline-block">
        <a onclick="javascript:switchDivVisibleInvisible('dropDownUpArrow', 'filtersList'); switchDivVisibleInvisible('dropDownUpArrow', 'dropDownDownArrow')">
            <img src="images/dropdown.gif"/>
        </a>
    </div>
</div>
<div id="filtersList" style="display:block">
    <div>
        <div class="underlinedDiv"></div>
        <p style="text-align:left" class="dttTitle">Testcase Filters (Displayed Rows)</p>

        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("test", "Test", "Test")%>
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
                    <%=docService.findLabelHTML("project", "idproject", "Project")%>
                </div>
                <div>
                    <%
                        options.clear();
                        for (Project project : projectService.findAllProject()) {
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
                    <%=docService.findLabelHTML("application", "System", "System")%>
                </div>
                <div>
                    <%
                        options.clear();
                        for (Invariant systemInv : invariantService.findListOfInvariantById("SYSTEM")) {
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
                    <%=docService.findLabelHTML("application", "Application", "Application")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("Application", request.getParameterValues("Application"), options,
                            "Select an application", "Select Application", "# of # Application selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "tcactive", "TestCase Active")%>
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
                    <%=docService.findLabelHTML("invariant", "PRIORITY", "Priority")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("Priority", request.getParameterValues("Priority"), options, "Select a Priority",
                            "Select Priority", "# of # Priority selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "Status", "Status")%>
                </div>
                <div>
                    <%
                        options.clear();
                        for (Invariant statusInv : invariantService.findListOfInvariantById("TCSTATUS")) {
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
                    <%=docService.findLabelHTML("invariant", "GROUP", "Group")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("Group", request.getParameterValues("Group"), options, "Select a Group",
                            "Select Group", "# of # Group selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "targetBuild", "targetBuild")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("TargetBuild", request.getParameterValues("TargetBuild"), options,
                            "Select a Target Build", "Select Target Build", "# of # Target Build selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "targetRev", "targetRev")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("TargetRev", request.getParameterValues("TargetRev"), options,
                            "Select a Target Rev", "Select Target Rev", "# of # Target Rev selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "creator", "Creator")%>
                </div>
                <div>
                    <%
                        options.clear();
                    %>
                    <%=generateMultiSelect("Creator", request.getParameterValues("Creator"), options, "Select a Creator",
                            "Select Creator", "# of # Creator selected", 1, true)%>
                </div>
            </div>
        </div>
        <div style="float: left">
            <div>
                <div>
                    <%=docService.findLabelHTML("testcase", "implementer", "implementer")%>
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
                    <%=docService.findLabelHTML("testcase", "comment", "comment")%>
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
                <%=docService.findLabelHTML("invariant", "Environment", "Environment")%>
            </div>
            <div>
                <%
                    options.clear();
                %>
                <%=generateMultiSelect("Environment", request.getParameterValues("Environment"), options,
                        "Select an Environment", "Select Environment", "# of # Environment selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "Build")%>
            </div>
            <div>
                <%
                    options.clear();
                    for (BuildRevisionInvariant myBR : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 1)) {
                        options.put(myBR.getVersionName(), myBR.getVersionName());
                    }
                %>
                <%=generateMultiSelect("Build", request.getParameterValues("Build"), options, "Select a Build",
                        "Select Build", "# of # Build selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "Revision")%>
            </div>
            <div>
                <%
                    options.clear();
                    for (BuildRevisionInvariant myBR : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 2)) {
                        options.put(myBR.getVersionName(), myBR.getVersionName());
                    }
                %>
                <%=generateMultiSelect("Revision", request.getParameterValues("Revision"), options, "Select a Revision",
                        "Select Revision", "# of # Revision selected", 1, true)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "IP", "Ip")%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Ip" id="Ip" value="<%=ip%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "Port", "Port")%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Port" id="Port" value="<%=port%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "tag", "Tag")%>
            </div>
            <div>
                <input style="font-weight: bold; width: 130px; height:16px" name="Tag" id="Tag" value="<%=tag%>"/>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "browserfullversion", "")%>
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
                    for (Invariant countryInv : invariantService.findListOfInvariantById("COUNTRY")) {
                        options.put(countryInv.getValue(), countryInv.getValue() + " - " + countryInv.getDescription());
                    }
                %>
                <%=generateMultiSelect("Country", request.getParameterValues("Country"), options, "Select a country",
                        "Select Country", "# of # Country selected", 1, false)%>
            </div>
        </div>
        <div style="float: left">
            <div>
                <%=docService.findLabelHTML("testcaseexecution", "Browser", "browser")%> <span
                    class="error-message required">*</span>
            </div>
            <div>
                <%
                    options.clear();
                    for (Invariant browserInv : invariantService.findListOfInvariantById("BROWSER")) {
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
</div>
</div>
<div>
    <table id="reporting" class="display" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
        <tr>
            <th rowspan="3">Test</th>
            <th rowspan="3">TestCase</th>
            <th rowspan="3">Application</th>
            <th rowspan="3">Description</th>
            <th rowspan="3">Priority</th>
            <th rowspan="3">Status</th>
            <th rowspan="3" id="TCComment">Comment</th>
            <th rowspan="3">Bug ID</th>
            <th rowspan="3">Group</th>
        </tr>
        <tr id="tableCountry"></tr>
        <tr id="TCResult"></tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
</body>
</html>