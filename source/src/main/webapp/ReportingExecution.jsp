<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Map"%>
<%@page import="org.cerberus.entity.TestCaseCountry"%>
<%@page import="org.cerberus.entity.Project"%>
<%@page import="org.cerberus.service.IProjectService"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
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
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.impl.InvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<% Date DatePageStart = new Date();%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Execution Reporting : Status</title>
        
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.dataTables.css">
        <link type="text/css" rel="stylesheet" href="css/jquery-ui.css">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/jquery.form.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
            
                TreeMap<String, String> options = new TreeMap<String, String>();

                IInvariantService invariantService = appContext.getBean(InvariantService.class);
                IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
                ITestService testService = appContext.getBean(ITestService.class);
                List<Test> testList = testService.getListOfTest();
                
                IProjectService projectService = appContext.getBean(IProjectService.class);
                List<Project> projectList = projectService.findAllProject();
                

                String tag;
                if (request.getParameter("Tag") != null && request.getParameter("Tag").compareTo("") != 0) {
                    tag = request.getParameter("Tag");
                } else {
                    tag = new String("");
                }

                String browserFullVersion;
                if (request.getParameter("BrowserFullVersion") != null && request.getParameter("BrowserFullVersion").compareTo("") != 0) {
                    browserFullVersion = request.getParameter("BrowserFullVersion");
                } else {
                    browserFullVersion = new String("");
                }

                
                String systemBR; // Used for filtering Build and Revision.
                if (request.getParameter("SystemExe") != null && request.getParameter("SystemExe").compareTo("All") != 0) {
                    systemBR =  request.getParameter("SystemExe");
                } else {
                    systemBR = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        systemBR = request.getParameter("system");
                    }
                }

                String port;
                if (request.getParameter("Port") != null && request.getParameter("Port").compareTo("") != 0) {
                    port = request.getParameter("Port");
                } else {
                    port = new String("");
                }
                String ip;
                if (request.getParameter("Ip") != null && request.getParameter("Ip").compareTo("") != 0) {
                    ip = request.getParameter("Ip");
                } else {
                    ip = new String("");
                }

                String tcActive;
                if (request.getParameter("TcActive") != null) {
                    if (request.getParameter("TcActive").compareTo("A") == 0) {
                        tcActive = "%%";
                    } else {
                        tcActive = request.getParameter("TcActive");
                    }
                } else {
                    tcActive = new String("Y");
                }

                
                String targetBuild = "";
                if (request.getParameter("TargetBuild") != null) {
                    if (request.getParameter("TargetBuild").compareTo("All") == 0) {
                        targetBuild = "All";
                    } else {
                        if (request.getParameter("TargetBuild").equals("NTB")) {
                            targetBuild = "";
                        } else {
                            targetBuild = request.getParameter("TargetBuild");
                        }
                    }
                } else {
                    targetBuild = "All";
                }

                String targetRev = "";
                if (request.getParameter("TargetRev") != null) {
                    if (request.getParameter("TargetRev").compareTo("All") == 0) {
                        targetRev = "All";
                    } else {
                        if (request.getParameter("TargetRev").equals("NTR")) {
                            targetRev = "";
                        } else {
                            targetRev = request.getParameter("TargetRev");
                        }
                    }
                } else {
                    targetRev = "All";
                }

                Boolean apply;
                if (request.getParameter("Apply") != null
                        && request.getParameter("Apply").compareTo("Apply") == 0) {
                    apply = true;
                } else {
                    apply = false;
                }

                String reportingFavorite = "ReportingExecution.jsp?";

                Connection conn = db.connect();
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                try {

                    Statement stmt = conn.createStatement();
                    List<Invariant> invariantCountry = invariantService.findListOfInvariantById("COUNTRY");
                    List<Invariant> invariantTCStatus = invariantService.findListOfInvariantById("TCSTATUS");
                    
            %>
            <form method="GET" name="Apply" id="Apply" action="ReportingExecutionResult.jsp">
                <div class="filters" style="float:left; width:100%;">
                <p class="dttTitle">Filters</p>
                <div id="dropDownDownArrow" style="float:left"><a 
                        onclick="javascript:switchDivVisibleInvisible('dropDownDownArrow', 'filtersList')"><img src="images/dropdown.gif"/></a>
                </div>
                <div id="filtersList" style="float:left; width:100px">
                
                    <div style="clear:both"><p class="dttTitle">Line Filters</p>
                            <table border="0px">
                                <tr>
                                    <td id="wob" style="width: 110px"><%out.print(docService.findLabelHTML("test", "Test", "Test"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(docService.findLabelHTML("project", "idproject", "Project"));%></td>
                                    <td id="wob" style="width: 60px"><%out.print(docService.findLabelHTML("application", "System", "System"));%></td>
                                    <td id="wob" style="width: 100px"><%out.print(docService.findLabelHTML("application", "Application", "Application"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(docService.findLabelHTML("testcase", "tcactive", "TestCase Active"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(docService.findLabelHTML("invariant", "PRIORITY", "Priority"));%></td>
                                </tr>

                                <tr>                        
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            for(Test testL : testList){
                                                    options.put(testL.getTest(), testL.getTest());
                                            }
                                        %>
                                            <%=generateMultiSelect("Test", request.getParameterValues("Test"), options, 
                                                "Select a test", "Select Test", "# of # Test selected", 1) %>
                                    </td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            for (Project project: projectList) {
                                                if(project.getIdProject() != null && !"".equals(project.getIdProject().trim())) {
                                                    options.put(project.getIdProject(), project.getIdProject()+" - "+project.getDescription());
                                                }
                                            }
                                            
                                            
                                        
                                        %>
                                            <%=generateMultiSelect("Project", request.getParameterValues("Project"), options, 
                                                "Select a project", "Select Project", "# of # Project selected", 1) %>
                                    </td>
                                    <td id="wob">
                                        <%
                                                ResultSet rsSys = stmt.executeQuery("SELECT DISTINCT System FROM application Order by System asc");
                                                options.clear();
                                                while (rsSys.next()) {
                                                        options.put(rsSys.getString("System"), rsSys.getString("System"));
                                                }%>
                                                <%=generateMultiSelect("System", request.getParameterValues("System"), options, 
                                                    "Select a sytem", "Select System", "# of # System selected", 1) %>
                                    </td>
                                    <td id="wob">
                                        <%
                                            ResultSet rsApp = stmt.executeQuery("SELECT Application , System FROM application Order by Sort asc");
                                            options.clear();
                                            while (rsApp.next()) {
                                                    options.put(rsApp.getString("Application"), rsApp.getString("Application")+" ["+rsApp.getString("System")+"]");
                                            }
                                        %>
                                            <%=generateMultiSelect("Application", request.getParameterValues("Application"), options, 
                                                "Select an application", "Select Application", "# of # Application selected", 1) %>
                                    </td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            options.put("Y", "Yes");
                                            options.put("N", "No");
                                        %>
                                        <%=generateMultiSelect("TcActive", request.getParameterValues("TcActive"), options, 
                                                "Select Activation state", "Select Activation", "# of # Activation state selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%
                                            ResultSet rsPri = stmt.executeQuery("SELECT DISTINCT value FROM invariant WHERE idname='PRIORITY' Order by sort asc");
                                            options.clear();
                                            while (rsPri.next()) {
                                                    options.put(rsPri.getString(1), rsPri.getString(1));
                                            }
                                        %>
                                            <%=generateMultiSelect("Priority", request.getParameterValues("Priority"), options, 
                                                "Select a Priority", "Select Priority", "# of # Priority selected", 1) %>
                                    </td>
                                </tr><tr>
                                    <td id="wob" style="width: 110px"><%out.print(docService.findLabelHTML("testcase", "Status", "Status"));%></td>
                                    <td id="wob" style="width: 110px"><%out.print(docService.findLabelHTML("invariant", "GROUP", "Group"));%></td>
                                    <td id="wob" style="width: 110px"><%out.print(docService.findLabelHTML("testcase", "targetBuild", "targetBuild"));%></td>                        
                                    <td id="wob" style="width: 110px"><%out.print(docService.findLabelHTML("testcase", "targetRev", "targetRev"));%></td>                        
                                    <td id="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "creator", "Creator"));%></td>
                                    <td id="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "implementer", "implementer"));%></td>
                                </tr><tr>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            for(Invariant statusInv : invariantTCStatus) {
                                                options.put(statusInv.getValue(), statusInv.getValue());
                                            }
                                        %>
                                        <%=generateMultiSelect("Status", request.getParameterValues("Status"), options, 
                                                "Select an option", "Select Status", "# of # Status selected", 1) %>
                                    </td>

                                    <td id="wob">
                                        <%
                                            options.clear();
                                            ResultSet rsGroup = stmt.executeQuery("SELECT value from invariant where idname = 'GROUP' order by sort");
                                                while (rsGroup.next()) {
                                                    if(rsGroup.getString(1) != null && !"".equals(rsGroup.getString(1).trim())) {
                                                        options.put(rsGroup.getString(1), rsGroup.getString(1));
                                                    }
                                            }
                                        %>
                                        <%=generateMultiSelect("Group", request.getParameterValues("Group"), options, 
                                                "Select a Group", "Select Group", "# of # Group selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            options.put("NTB", "-- No Target Build --");
                                            ResultSet rsTargetBuild = stmt.executeQuery("SELECT value from invariant where idname = 'BUILD' order by sort");
                                                while (rsTargetBuild.next()) {
                                                    if(rsTargetBuild.getString(1) != null && !"".equals(rsTargetBuild.getString(1).trim())) {
                                                        options.put(rsTargetBuild.getString(1), rsTargetBuild.getString(1));
                                                    }
                                            }
                                        %>
                                        <%=generateMultiSelect("TargetBuild", request.getParameterValues("TargetBuild"), options, 
                                                "Select a Target Build", "Select Target Build", "# of # Target Build selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            options.put("NTR", "-- No Target Rev --");
                                            ResultSet rsTargetRev = stmt.executeQuery("SELECT value from invariant where idname = 'REVISION' order by sort");
                                            while (rsTargetRev.next()) {
                                                if(rsTargetRev.getString(1) != null && !"".equals(rsTargetRev.getString(1).trim())) {
                                                    options.put(rsTargetRev.getString(1), rsTargetRev.getString(1));
                                                }
                                            }
                                        %>
                                        <%=generateMultiSelect("TargetRev", request.getParameterValues("TargetRev"), options, 
                                                "Select a Target Rev", "Select Target Rev", "# of # Target Rev selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            ResultSet rsCreator = stmt.executeQuery("SELECT login from user");
                                            while (rsCreator.next()) {
                                                if(rsCreator.getString(1) != null && !"".equals(rsCreator.getString(1).trim())) {
                                                    options.put(rsCreator.getString(1), rsCreator.getString(1));
                                                }
                                            }
                                        %>
                                        <%=generateMultiSelect("Creator", request.getParameterValues("Creator"), options, 
                                                "Select a Creator", "Select Creator", "# of # Creator selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%=generateMultiSelect("Implementer", request.getParameterValues("Implementer"), options, 
                                                "Select an Implementer", "Select Implementer", "# of # Implementer selected", 1) %> 
                                    </td>
                                </tr>
                            </table>
                    </div>       <div style="clear:both"><p class="dttTitle">Content Filters</p>
                            <table border="0px">
                                <tr><td colspan="7" class="wob"></td></tr>
                                <tr>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("invariant", "Environment", "Environment"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(docService.findLabelHTML("application", "system", "System"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("buildrevisioninvariant", "versionname01", "Build"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("buildrevisioninvariant", "versionname02", "Revision"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("testcaseexecution", "IP", "Ip"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("testcaseexecution", "Port", "Port"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("testcaseexecution", "tag", "Tag"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("testcaseexecution", "browserfullversion", ""));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(docService.findLabelHTML("testcaseexecution", "status", ""));%></td>
                                </tr>

                                <tr>
                                    <td id="wob">
                                                                                <%
                                            options.clear();
                                            ResultSet rsEnv = stmt.executeQuery("SELECT value from invariant where idname = 'ENVIRONMENT' order by sort");
                                            while (rsEnv.next()) {
                                                if(rsEnv.getString(1) != null && !"".equals(rsEnv.getString(1).trim())) {
                                                    options.put(rsEnv.getString(1), rsEnv.getString(1));
                                                }
                                            }
                                        %>
                                        <%=generateMultiSelect("Environment", request.getParameterValues("Environment"), options, 
                                                "Select an Environment", "Select Environment", "# of # Environment selected", 1) %> 
                                    </td>
                                    <td id="wob">
                                        <%
                                            List<Invariant> systemList = invariantService.findListOfInvariantById("SYSTEM");
                                            options.clear();
                                            for(Invariant systemInv : systemList) {
                                                options.put(systemInv.getValue(), systemInv.getValue());
                                            }
                                        %>
                                        <%=generateMultiSelect("SystemExe", request.getParameterValues("SystemExe"), options, 
                                                "Select a System", "Select System", "# of # System selected", 1) %>
                                                
                                    </td>
                                    <td id="wob">
                                        <%
                                            List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 1);
                                            options.clear();
                                            for(BuildRevisionInvariant myBR : listBuildRev) {
                                                options.put(myBR.getVersionName(), myBR.getVersionName());
                                            }
                                        %>
                                        <%=generateMultiSelect("Build", request.getParameterValues("Build"), options, 
                                                "Select a Build", "Select Build", "# of # Build selected", 1) %>
                                    </td>
                                    <td id="wob">
                                        <%
                                            listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 2);
                                            options.clear();
                                            for(BuildRevisionInvariant myBR : listBuildRev) {
                                                options.put(myBR.getVersionName(), myBR.getVersionName());
                                            }
                                        %>
                                        <%=generateMultiSelect("Revision", request.getParameterValues("Revision"), options, 
                                                "Select a Revision", "Select Revision", "# of # Revision selected", 1) %>
                                    </td>
                                    <td id="wob"><input style="font-weight: bold; width: 130px" name="Ip" id="Ip" value="<%=ip%>"></td>
                                    <td id="wob"><input style="font-weight: bold; width: 60px" name="Port" id="Port" value="<%=port%>"></td>
                                    <td id="wob"><input style="font-weight: bold; width: 130px" name="Tag" id="Tag" value="<%=tag%>"></td>
                                    <td id="wob"><input style="font-weight: bold; width: 130px" name="BrowserFullVersion" id="Tag" value="<%=browserFullVersion%>"></td>
                                    <td id="wob">
                                        <%
                                            options.clear();
                                            for(Invariant statusInv : invariantTCStatus) {
                                                options.put(statusInv.getValue(), statusInv.getValue());
                                            }
                                        %>
                                        <%=generateMultiSelect("ExeStatus", request.getParameterValues("exeStatus"), options, 
                                                "Select a TC Status", "Select TC Status", "# of # TC Status selected", 1) %>
                                    </td>
                                </tr>
                            </table>
                    </div>
                            <%
                            %>
                            <table border="0px">
                                <tr><td></td></tr>
                                <tr>
                                    <td id ="arrond">
                                        <table>
                                            <tr><td class="wob"><h4 style="color:black">Country</h4></td></tr>
                                            <tr><td class="wob"><%
                                                options.clear();
                                                for (Invariant countryInv : invariantCountry){
                                                    options.put(countryInv.getValue(), countryInv.getValue()+" - "+countryInv.getDescription());
                                                }
                                                
                                                
                                            %><%=generateMultiSelect("Country", request.getParameterValues("Country"), options, 
                                                "Select a country", "Select Country", "# of # Country selected", 1) %></td>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td id="wob">
                                        <table>
                                            <tr>
                                                <td class="wob">
                                                    <input id="button" type="submit" name="Apply" value="Apply">
                                                </td>
                                                <%if (!apply) {
                                                %>
                                                <td class="wob">
                                                    <input id="button" type="button" name="defaultFilter" value="Select My Default Filters" onclick="loadReporting('<%=reportingFavorite%>')">           
                                                </td><% }

                                                %>
                                                <td class="wob">
                                                    <input id="button" type="button" value="Set As My Default Filter" onclick="saveFilters()">
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </div> 
                </div>

                <br><br>
                <div id="displayResult">
                    <br>
                    <br>
                    <br>
                    <br>
                    <br>
                    <br>
                </div>
                <%
                    } catch (Exception e) {
                        out.println(e);
                    } finally {
                        try {
                            conn.close();
                        } catch (Exception ex) {
                        }
                    }

                %>

            </form>
        </div>

        <script type="text/javascript">
                    $(document).ready(function() {
                        $(".multiSelectOptions").each(function(){
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
                    });
        </script>
        
        <script type="text/javascript">
            $(document).ready(function() {

                // prepare all forms for ajax submission
                $('#Apply').on('submit', function(e) {
                    $('#displayResult').html('<img src="./images/loading.gif"> loading...');
                    e.preventDefault(); // <-- important
                    $(this).ajaxSubmit({
                        target: '#displayResult'
                    });
                });

                <%
                    if("Apply".equals(request.getParameter("Apply"))) {
                        %>
                                $('#Apply').submit();
                        <%
                    }
                %>

                function saveFilters() {
                    $('#Apply').action = $('#Apply').action+"?Apply=Apply&RecordPref=Y";
                    $('#Apply').submit();
                }

            });
        </script>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
