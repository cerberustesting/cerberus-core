<%@ page import="org.cerberus.entity.*" %>
<%@ page import="org.cerberus.service.*" %>
<%@ page import="org.apache.log4j.Logger" %>
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="include/function.jsp" %>

<%
    final Logger LOG = Logger.getLogger(this.getClass());
    IUserService userService = appContext.getBean(IUserService.class);
    IProjectService projectService = appContext.getBean(IProjectService.class);
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    IApplicationService applicationService = appContext.getBean(IApplicationService.class);
    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
    IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(IBuildRevisionParametersService.class);
    Date DatePageStart = new Date();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Build Content</title>

    <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="css/crb_style.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
    <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
    <script type="text/javascript" src="js/jquery.validate.min.js"></script>

    <style>
        form label {
            display: inline-block;
            font-weight: 700;
            padding-bottom: 7px;
            width: 100px;
        }


    </style>
</head>
<body>
<%@ include file="include/header.jsp" %>

<%
    try {
        String MySystem = request.getAttribute("MySystem").toString();
        String myLang = request.getAttribute("MyLang").toString();
        String maxBuild = buildRevisionParametersService.getMaxBuildBySystem(MySystem);
        String maxRevision = buildRevisionParametersService.getMaxRevisionBySystemAndBuild(MySystem, maxBuild);

        StringBuilder users = new StringBuilder("{");
        StringBuilder userOptions = new StringBuilder();
        users.append("'':'',");
        userOptions.append("<option value=''></option>");
        for (User user : userService.findAllUserBySystem(MySystem)) {
            users.append("'");
            users.append(user.getLogin());
            users.append("':'");
            users.append(user.getName());
            users.append("',");

            userOptions.append("<option value='");
            userOptions.append(user.getLogin());
            if (user.getLogin().equalsIgnoreCase(request.getUserPrincipal().getName())) {
                userOptions.append("' selected='selected'>");
            } else {
                userOptions.append("'>");
            }
            userOptions.append(user.getName());
            userOptions.append("</option>\n");
        }
        users.append("}");

        StringBuilder projects = new StringBuilder("{");
        StringBuilder projectOptions = new StringBuilder();
        for (Project project : projectService.readAll_Deprecated()) {
            projects.append("'");
            projects.append(project.getIdProject());
            projects.append("':'");
            projects.append(project.getIdProject());
            projects.append(" [");
            projects.append(project.getCode());
            projects.append("] ");
            projects.append(project.getDescription());
            projects.append("',");

            projectOptions.append("<option value='");
            projectOptions.append(project.getIdProject());
            projectOptions.append("'>");
            projectOptions.append(project.getIdProject());
            projectOptions.append(" [");
            projectOptions.append(project.getCode());
            projectOptions.append("] ");
            projectOptions.append(project.getDescription());
            projectOptions.append("</option>\n");
        }
        projects.append("}");

        StringBuilder applications = new StringBuilder("{");
        StringBuilder applicationOptions = new StringBuilder();
        for (Application app : applicationService.readBySystem_Deprecated(MySystem)) {
            applications.append("'");
            applications.append(app.getApplication());
            applications.append("':'");
            applications.append(app.getApplication());
            applications.append("',");

            applicationOptions.append("<option value='");
            applicationOptions.append(app.getApplication());
            applicationOptions.append("'>");
            applicationOptions.append(app.getApplication());
            applicationOptions.append("</option>\n");
        }
        applications.append("}");

        StringBuilder revisions = new StringBuilder("{");
        StringBuilder revisionOptions = new StringBuilder();
        revisions.append("'NONE':'-- NONE --',");
        revisionOptions.append("<option value=''>-- ALL --</option>");
        revisionOptions.append("<option value='NONE'>-- NONE --</option>");
        for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2)) {
            revisions.append("'");
            revisions.append(bri.getVersionName());
            revisions.append("':'");
            revisions.append(bri.getVersionName());
            revisions.append("',");

            revisionOptions.append("<option value='");
            revisionOptions.append(bri.getVersionName());
            revisionOptions.append("'>");
            revisionOptions.append(bri.getVersionName());
            revisionOptions.append("</option>\n");
        }
        revisions.append("}");

        StringBuilder builds = new StringBuilder("{");
        StringBuilder buildOptions = new StringBuilder();
        builds.append("'NONE':'-- NONE --',");
        buildOptions.append("<option value='NONE'>-- NONE --</option>");
        for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1)) {
            builds.append("'");
            builds.append(bri.getVersionName());
            builds.append("':'");
            builds.append(bri.getVersionName());
            builds.append("',");

            buildOptions.append("<option value='");
            buildOptions.append(bri.getVersionName());
            buildOptions.append("'>");
            buildOptions.append(bri.getVersionName());
            buildOptions.append("</option>\n");
        }
        builds.append("}");
%>

<script type="text/javascript">
    var oTable;
    var build = '<%=request.getParameter("build")%>';
    var revision = '<%=request.getParameter("revision")%>';

    $(document).ready(function(){
        $("#pending").on("click", function(event){
            event.preventDefault();
            $("#selectedBuild").val("NONE");
            $("#selectedRevision").val("NONE");
            $("#search").click();
        });

        $("#last").on("click", function(event){
            event.preventDefault();
            $("#selectedBuild").val("<%=maxBuild%>");
            $("#selectedRevision").val("<%=maxRevision%>");
            $("#search").click();
        });

        if (build === 'null') {
            $("#selectedBuild").val("<%=maxBuild%>");
        } else {
            $("#selectedBuild").val(build);
        }
        if (revision === 'null') {
            $("#selectedRevision").val("<%=maxRevision%>");
        } else {
            $("#selectedRevision").val(revision);
        }

        $("#formAddNewRow").dialog({width: 'auto'}).dialog( "close" );

        $("#revision").find('option')[0].remove();

        var params = "System=<%=MySystem%>&Build=";
        params += $("#selectedBuild").val();
        params += "&Revision=";
        params += $("#selectedRevision").val();

        $("#contentTable").dataTable({
            "aaSorting": [[0, "asc"],[1, "asc"],[2, "asc"],[3, "asc"]],
            "bInfo": false,
            "bJQueryUI": true,
            "bSort": false,
            "bPaginate": false,
            "bServerSide": false,
            "bProcessing": false,
            "sAjaxSource": "FindBuildContent?"+params,
            "aoColumnDefs": [
                {
                    "aTargets": [ 0 ],
                    "bSearchable": false,
                    "bVisible": false
                }
            ],
            fnInitComplete: function() {
                $("#sprint").val($("#selectedBuild").val());
                $("#revision").val($("#selectedRevision").val());
            }
        }).makeEditable({
            sAddDeleteToolbarSelector: ".ui-corner-tl",
            sAddURL: "AddBuildContent",
            sAddHttpMethod: "POST",
            oAddNewRowButtonOptions: {
                label: "Add...",
                icons: {primary: 'ui-icon-plus'}
            },
            sDeleteURL: "DeleteBuildContent",
            sDeleteHttpMethod: "POST",
            oDeleteRowButtonOptions: {
                label: "Remove",
                icons: {primary: 'ui-icon-trash'}
            },
            sUpdateURL: "UpdateBuildContent",
            "aoColumns": [
                {
                    type: "select",
                    data: <%=builds%>,
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    type: "select",
                    data: <%=revisions%>,
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    type: "select",
                    data: <%=applications%>,
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    type: "select",
                    data: <%=projects%>,
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    type: "select",
                    data: <%=users%>,
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                },
                {
                    placeholder: "",
                    onblur: 'cancel',
                    submit: 'Save'
                }
            ]
        });
//    }
    });
</script>

<div style="font-family: sans-serif">
    <form action="BuildContent.jsp" method="get">
        <div style="float: left; padding-left: 10px">
            <a id="pending" href="#">Pending Release</a>
            <a id="last" href="#">Latest Release</a>
        </div>
        <div style="float: left; padding-left: 10px">
            <%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%>
            <select id="selectedBuild" name="build">
                <%=buildOptions%>
            </select>
        </div>
        <div style="float: left; padding-left: 10px">
            <%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%>
            <select id="selectedRevision" name="revision">
                <%=revisionOptions%>
            </select>
        </div>
        <div style="float: left; padding-left: 10px">
            <input id="search" type="submit" value="Apply"/>
        </div>
    </form>
</div>

<div style="font: 90% sans-serif">
    <table id="contentTable" class="display">
        <thead>
        <tr>
            <th></th>
            <th>Sprint</th>
            <th>Revision</th>
            <th>Application</th>
            <th>Release</th>
            <th>Project</th>
            <th>Ticket</th>
            <th>Bug</th>
            <th>Subject</th>
            <th>Owner</th>
            <th>Link</th>
        </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
<div>
    <form id="formAddNewRow" action="#" title="Add new Sprint content" method="post">
        <input type="hidden" rel="0"/>
        <div>
            <label for="sprint">Sprint</label>
            <select id="sprint" name="addSprint" rel="1">
                <%=buildOptions%>
            </select>
        </div>
        <div>
            <label for="revision">Revision</label>
            <select id="revision" name="addRevision" rel="2">
                <%=revisionOptions%>
            </select>
        </div>
        <div>
            <label for="application">Application</label>
            <select id="application" name="application" rel="3">
                <%=applicationOptions%>
            </select>
        </div>
        <div>
            <label for="release">Release</label>
            <input type="text" id="release" name="release" value="" class="required" rel="4"/>
        </div>
        <div>
            <label for="project">Project</label>
            <select id="project" name="project" rel="5">
                <%=projectOptions%>
            </select>
        </div>
        <div>
            <label for="ticket">Ticket</label>
            <input type="text" id="ticket" name="ticket" value="" rel="6"/>
        </div>
        <div>
            <label for="bug">Bug</label>
            <input type="text" id="bug" name="bug" value="" rel="7"/>
        </div>
        <div>
            <label for="subject">Subject</label>
            <input type="text" id="subject" name="subject" value="" rel="8"/>
        </div>
        <div>
            <label for="owner">Owner</label>
            <select id="owner" name="owner" rel="9">
                <%=userOptions%>
            </select>
        </div>
        <div>
            <label for="link">Link</label>
            <input type="text" id="link" name="link" value="" rel="10"/>
        </div>
        <div>
            <button id="btnAddNewRowOk">Add</button>
            <button id="btnAddNewRowCancel">Cancel</button>
        </div>
    </form>
</div>
         <br><%
            out.print(display_footer(DatePageStart));
        %>
<%
    } catch (CerberusException ex){
        LOG.error("Unable to find Invariant, Application or User : " + ex.toString());
        out.println("</script>");
        out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
        out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
    }
%>
</body>
</html>