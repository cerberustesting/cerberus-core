<%-- 
    Document   : TestCaseSearchNew
    Created on : 12 nov. 2014, 15:51:17
    Author     : bcivel
--%>

<%@page import="org.cerberus.crud.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.crud.entity.Application"%>
<%@page import="org.cerberus.crud.entity.Test"%>
<%@page import="org.cerberus.crud.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.crud.service.ITestService"%>
<%@page import="org.cerberus.crud.service.impl.InvariantService"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TestCase Search</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.filter.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.filter.js"></script>

        <script>
            $(document).ready(function() {
                $.get('http://localhost:8080/solr/cerberus/select?q=%2A:%2A&wt=json&indent=true&group=true&group.field=ttc&group.ngroups=true', function(data) {
                    var jsondata = $.parseJSON(data);
                    var indexColor = 0;
                    for (var i = 0; i < jsondata.grouped.ttc.groups.length; i++) {
                        indexColor++;
                        $("#impl").append($("<tr></tr>").attr('style', 'height:20px').attr('class', indexColor % 2 === 1 ? 'even' : 'odd').append($("<td></td>")
                                .text(jsondata.grouped.ttc.groups[i].doclist.docs[0].test)).append($("<td></td>")
                                .text(jsondata.grouped.ttc.groups[i].doclist.docs[0].testcase)).append($("<td></td>").text(jsondata.grouped.ttc.groups[i].doclist.docs[0].description)))
                    }
                    
                    $("#count").append($("<p></p>")
                                .text(jsondata.grouped.ttc.ngroups));
                });
            });
        </script>
        <script>
            function searchData() {
                var freeText = $("[name='freeText']").val();
                var test = $("[name='Test']").val();
                var proj = $("[name='Project']").val();
                var table = [];
                if (test !== null) {
                    table.push(["test", test]);
                }
                if (proj !== null) {
                    table.push(["project", proj]);
                }

var url = "";
if (freeText !== null) {
    var textTable = freeText.split(" ");
    for (var t = 0; t<textTable.length; t++){
        if (t !== 0) {
                        url = url + " AND ";
                    }
                url = url + "collector%3A(%2A"+textTable[t]+"%2A)";
            }
        }
                
                for (var v = 0; v < table.length; v++) {
                    if (v !== 0) {
                        url = url + " AND ";
                    }
                    for (var i = 0; i < table[v][1].length; i++) {
                        if (i !== 0) {
                            url = url + " AND ";
                        }
                        url = url + table[v][0];
                        url = url + "%3A(";
                        url = url + table[v][1][i];
                        url = url + ")";
                    }

                }
                console.log(url);

                $("#impl").empty();
                $("#count").empty();

                $.get('http://localhost:8080/solr/cerberus/select?q=' + url + '&wt=json&indent=true&group=true&group.field=ttc&group.ngroups=true', function(data) {
                    var jsondata = $.parseJSON(data);
                    var indexColor = 0;
                    for (var i = 0; i < jsondata.grouped.ttc.groups.length; i++) {
                        indexColor++;
                        $("#impl").append($("<tr></tr>").attr('style', 'height:20px').attr('class', indexColor % 2 === 1 ? 'even' : 'odd').append($("<td></td>")
                                .text(jsondata.grouped.ttc.groups[i].doclist.docs[0].test)).append($("<td></td>")
                                .text(jsondata.grouped.ttc.groups[i].doclist.docs[0].testcase)).append($("<td></td>").text(jsondata.grouped.ttc.groups[i].doclist.docs[0].description)))
                    }
                    $("#count").append($("<p></p>")
                                .text(jsondata.grouped.ttc.ngroups));
                });
            }
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            final Logger LOG = Logger.getLogger(this.getClass());
            TreeMap<String, String> options = new TreeMap<String, String>();

            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            ITestService testService = appContext.getBean(ITestService.class);
            IProjectService projectService = appContext.getBean(IProjectService.class);
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);
            IUserService userService = appContext.getBean(IUserService.class);
            IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
            
            String myLang = ParameterParserUtil.parseStringParam(request.getAttribute("MyLang").toString(), "en");

            try {
        %>

        <form id="formAddNewContentSearch" action="TestBatteryTestCaseResult.jsp" method="post">
            <div style="float:left; width:100%">
                    <div style="float:left;text-align: left;" class="dttTitle">Search : </div>
                    <div>
                        <input style="float:left;height:20px; width:89%; text-align: left" name="freeText" onkeyup="searchData()" onblur="searchData()"> 
                    </div>
            </div>
            
            <br><br><div style="height:30px" class="underlinedDiv"></div>
            
            <p style="text-align:left" class="dttTitle">Additional Filters</p>
            <div style="float:left; display:block">
                <div style="float:left">
                    <div style="width:150px; text-align: left"><%=docService.findLabelHTML("test", "Test", "Test", myLang)%></div>
                    <div>
                        <%
                            options.clear();
                            for (Test testL : testService.getListOfTest()) {
                                options.put(testL.getTest(), testL.getTest());
                            }
                        %>
                        <%=generateMultiSelect("Test", request.getParameterValues("Test"), options,
                                "Select a test", "Select Test", "# of # Test selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="width:150px; text-align: left"><%=docService.findLabelHTML("project", "idproject", "Project", myLang)%></div>
                    <div>
                        <%
                            options.clear();
                            for (Project project : projectService.convert(projectService.readAll())) {
                                if (project.getIdProject() != null && !"".equals(project.getIdProject().trim())) {
                                    options.put(project.getIdProject(), project.getIdProject() + " - " + project.getDescription());
                                }
                            }
                        %>
                        <%=generateMultiSelect("Project", request.getParameterValues("Project"), options,
                                "Select a project", "Select Project", "# of # Project selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("application", "System", "System", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (String system : applicationService.readDistinctSystem()) {
                                options.put(system, system);
                            }
                        %>
                        <%=generateMultiSelect("System", request.getParameterValues("System"), options,
                                "Select a sytem", "Select System", "# of # System selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("application", "Application", "Application", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (Application app : applicationService.convert(applicationService.readAll())) {
                                options.put(app.getApplication(), app.getApplication() + " [" + app.getSystem() + "]");
                            }
                        %>
                        <%=generateMultiSelect("Application", request.getParameterValues("Application"), options,
                                "Select an application", "Select Application", "# of # Application selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "tcactive", "TestCase Active", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            options.put("Y", "Yes");
                            options.put("N", "No");
                        %>
                        <%=generateMultiSelect("TcActive", request.getParameterValues("TcActive"), options,
                                "Select Activation state", "Select Activation", "# of # Activation state selected", 1, true)%> 
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("invariant", "PRIORITY", "Priority", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (Invariant invPriority : invariantService.findListOfInvariantById("PRIORITY")) {
                                options.put(invPriority.getValue(), invPriority.getValue());
                            }
                        %>
                        <%=generateMultiSelect("Priority", request.getParameterValues("Priority"), options,
                                "Select a Priority", "Select Priority", "# of # Priority selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "Status", "Status", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (Invariant statusInv : invariantService.findListOfInvariantById("TCSTATUS")) {
                                options.put(statusInv.getValue(), statusInv.getValue());
                            }
                        %>
                        <%=generateMultiSelect("Status", request.getParameterValues("Status"), options,
                                "Select an option", "Select Status", "# of # Status selected", 1, true)%>
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("invariant", "GROUP", "Group", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (Invariant invGroup : invariantService.findListOfInvariantById("GROUP")) {
                                if (invGroup.getValue() != null && !"".equals(invGroup.getValue().trim())) {
                                    options.put(invGroup.getValue(), invGroup.getValue());
                                }
                            }
                        %>
                        <%=generateMultiSelect("Group", request.getParameterValues("Group"), options,
                                "Select a Group", "Select Group", "# of # Group selected", 1, true)%> 
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "targetBuild", "targetBuild", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            options.put("NTB", "-- No Target Build --");
                            for (BuildRevisionInvariant invBuild : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(request.getParameter("system"), 1)) {
                                if (invBuild.getVersionName() != null && !"".equals(invBuild.getVersionName().trim())) {
                                    options.put(invBuild.getVersionName(), invBuild.getVersionName());
                                }
                            }
                        %>
                        <%=generateMultiSelect("TargetBuild", request.getParameterValues("TargetBuild"), options,
                                "Select a Target Build", "Select Target Build", "# of # Target Build selected", 1, true)%> 
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "targetRev", "targetRev", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            options.put("NTR", "-- No Target Rev --");
                            for (BuildRevisionInvariant invRevision : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(request.getParameter("system"), 2)) {
                                if (invRevision.getVersionName() != null && !"".equals(invRevision.getVersionName().trim())) {
                                    options.put(invRevision.getVersionName(), invRevision.getVersionName());
                                }
                            }
                        %>
                        <%=generateMultiSelect("TargetRev", request.getParameterValues("TargetRev"), options,
                                "Select a Target Rev", "Select Target Rev", "# of # Target Rev selected", 1, true)%> 
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "creator", "Creator", myLang)%></div>
                    <div style="clear:both">
                        <%
                            options.clear();
                            for (User user : userService.findallUser()) {
                                if (user.getLogin() != null && !"".equals(user.getLogin().trim())) {
                                    options.put(user.getLogin(), user.getLogin());
                                }
                            }
                        %>
                        <%=generateMultiSelect("Creator", request.getParameterValues("Creator"), options,
                                "Select a Creator", "Select Creator", "# of # Creator selected", 1, true)%> 
                    </div>
                </div>
                <div style="float:left">
                    <div style="clear:both; width:150px; text-align: left"><%=docService.findLabelHTML("testcase", "implementer", "implementer", myLang)%></div>
                    <div style="clear:both">
                        <%=generateMultiSelect("Implementer", request.getParameterValues("Implementer"), options,
                                "Select an Implementer", "Select Implementer", "# of # Implementer selected", 1, true)%> 
                    </div>
                </div>
            </div>
            <br><br>
            <button id="bitton" name="button" type="button" onclick="searchData()">Search</button>
        </form>
        <div id="displayResult">
            <br>
            <br>
        </div>
        <%
            } catch (CerberusException ex) {
                LOG.error("Unable to find Invariant, Application or User : " + ex.toString());
                out.println("</script>");
                out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
                out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
            }

        %>
<div class="underlinedDiv"></div>
<div><br>
            <br></div>
    <div style="float:left"  class="dttTitle">Total : </div><div style="float:left" id="count"  class="dttTitle"></div><div style="float:left"  class="dttTitle"> result(s)</div>
    <div style="clear:both" id="impl"></div>


    <script type="text/javascript">
        $(document).ready(function() {
            $(".multiSelectOptions").each(function() {
                var currentElement = $(this);
                currentElement.multiselect({
                    multiple: true,
                    minWidth: 150,
//                        header: currentElement.data('header'),
                    noneSelectedText: currentElement.data('none-selected-text'),
                    selectedText: currentElement.data('selected-text'),
                    selectedList: currentElement.data('selected-list')
                }).multiselectfilter();
            });

            // prepare all forms for ajax submission
            $('#formAddNewContentSearch').on('submit', function(e) {
                $('#displayResult').html('<img src="./images/loading.gif"> loading...');
                e.preventDefault(); // <-- important
                $(this).ajaxSubmit({
                    target: '#displayResult'
                });
            });
        });
    </script>
</body>
</html>
