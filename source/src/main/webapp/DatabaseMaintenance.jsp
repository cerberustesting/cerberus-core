<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.cerberus.core.crud.entity.LogEvent"%>
<%@page import="org.cerberus.core.database.DocumentationDatabaseService"%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@page import="org.apache.logging.log4j.Logger"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="org.cerberus.core.crud.factory.IFactoryMyversion"%>
<%@page import="org.cerberus.core.crud.factory.impl.FactoryMyversion"%>
<%@page import="org.cerberus.core.database.IDatabaseVersioningService"%>
<%@page import="org.cerberus.core.crud.entity.MyVersion"%>
<%@page import="org.cerberus.core.crud.service.IMyVersionService"%>
<%@page import="org.cerberus.core.crud.service.ILogEventService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Cerberus Database Maintenance</title>
        <script type="text/javascript" src="js/pages/DatabaseMaintenance.js"></script>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <h1 class="page-title-line" id="title">Database Maintenance</h1>
            <%
                Logger LOG = LogManager.getLogger("DatabaseMaintenance.jsp");

                Integer NewVersion;
                // Full script that create the cerberus database.
                ArrayList<String> SQLInstruction;
                // Store the Return code of the specific SQL.
                ArrayList<String> SQLRC;
                // Store the Return code of the specific SQL.
                // Temporary string to receive the Return code of the SQL Execution.
                String MySQLRC = "";
                // This boolean is used in order to detec is an SQL has been performed. After version SQLLimit, 
                // this is used in order to execute only 1 instruction at a time as it make take a lot of time to process.
                boolean SQLExecuted = false;
                // SQL that has version equal to SQLLimit will not be executed automatically.
                Integer SQLLimit = 1882; // 4.19 Version LEVEL.
                IFactoryMyversion factoryMyversion;

                try {
                    // I get here the current version of the database. (null if no database found)
                    MyVersion DtbVersion = null;
                    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                    IMyVersionService myVersionService = appContext.getBean(IMyVersionService.class);
                    ILogEventService logEventService = appContext.getBean(ILogEventService.class);

                    if (myVersionService.findMyVersionByKey("database") != null) {
                        DtbVersion = myVersionService.findMyVersionByKey("database");
                    } else {
                        out.print("<div class=\"alert alert-warning\"><strong>Database is empty. A first version needs to be initialised. Please click on <i>Initialize Database</i> button bellow in order to initialize it. If already done, please click on <i>Apply Next SQL</i> button until all SQLs has been executed.<br>Anytime you will deploy a new version of Cerberus, you will have to come back to this page (Menu : Admin / Database Maintenance) and execute the missing SQL in order to upgrade the database.</strong></div>");
                        factoryMyversion = new FactoryMyversion();
                        DtbVersion = factoryMyversion.create("database", 0, "");
                    }

                    // Displaying the current version of the database.
                    // Start to build the SQL Script here.
                    SQLInstruction = new ArrayList<String>();
                    appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                    IDatabaseVersioningService databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
                    SQLInstruction = databaseVersionService.getSQLScriptFromFile();

                    // Initialize the array that will receive the RC of every execution.
                    SQLRC = new ArrayList<String>();

                    // Calculate the version that will be updated. Version correspond directly to the size of the arry (ie the number of SQL to execute)
                    NewVersion = SQLInstruction.size();

                    if (DtbVersion.getValue() < NewVersion) {
                        String ButtonText = "Apply Next SQL";
                        if (DtbVersion.getValue() == 0) {
                            ButtonText = "Initialize Database";
                        }

                        out.print("<div class=\"row\"><div class=\"form-group col-xs-6\">");
                        out.print("<label for=\"databaseVersion\" name=\"databaseVersion\">Database Version</label>");
                        out.print("<input type=\"text\" class=\"form-control\" name=\"databaseVersion\" aria-describedby=\"basic-addon1\" value=\"" + DtbVersion.getValue() + "\" readonly></div>");
                        out.print("<div class=\"form-group col-xs-6\">");
                        out.print("<label for=\"targetVersion\" name=\"targetVersion\">Target Database Version</label>");
                        out.print("<input type=\"text\" class=\"form-control\" name=\"targetDatabaseVersion\" aria-describedby=\"basic-addon1\" value=\"" + SQLInstruction.size() + "\" readonly></div></div>");
                        out.print("<input id=\"buttonApplyDatabase\" class=\"btn btn-warning btn-lg\" type=\"submit\" disabled=\"disabled\" value=\"" + ButtonText + "\" onClick=\"ExecApply.submit(); this.disabled=true; this.value='Processing...'; \">");

                        if (DtbVersion.getValue() < NewVersion) {
                            out.print("<div class=\"panel panel-default marginTop20\"><div class=\"panel-heading\"><span class=\"glyphicon glyphicon-list\"></span><label>  SQL performed in that Batch :</label></div>");
                            out.print("<div class=\"panel-body\"><table class=\"table table-hover\">");
                            out.print("<tr><th>version</th><th>SQL</th><th>Status</th></tr>");
                            Integer i = 0;
                            for (String MySQL : SQLInstruction) {
                                i = i + 1;
                                if ((i > DtbVersion.getValue()) && ((request.getParameter("GO") != null))) { // Only if the SQL has not been executed already. and button pressed.
                                    if ((i < SQLLimit) || (SQLExecuted == false)) { // After version SQLLimit, only 1 execution at a time.
                                        // Execute the SQL Here
                                        LOG.info("Execute SQL to version : " + i + " / " + SQLInstruction.size());
                                        MySQLRC = databaseVersionService.exeSQL(MySQL);
                                        SQLExecuted = true;
                                        SQLRC.add(MySQLRC);
                                        String colorClass = "";
                                        String rowLine = "";
                                        if (MySQLRC.equalsIgnoreCase("OK")) {
                                            colorClass = "success";
                                            rowLine = "1";
                                        } else {
                                            colorClass = "danger";
                                            rowLine = "5";
                                        }

                                        out.print("<tr class=\"" + colorClass + "\"><td>");
                                        out.print(i);
                                        out.print("</td><td class=\"wob\"><textarea class=\"form-control\" name=\"SQL\" rows=\"" + rowLine + "\" style=\"background-color:transparent;border:0px;font-size:x-small;width: 100%\" readonly>");
                                        out.print(MySQL.replace("</textarea>", "</text4rea>"));
                                        out.print("</textarea></td>");

                                        if ((i >= 3) && (MySQLRC.equalsIgnoreCase("OK"))) { // The myversion table is only available after the Version 3
                                            // Update the myversion table to comit the execution of the SQL Instruction.
                                            DtbVersion.setValue(i);
                                            myVersionService.update(DtbVersion);
                                        }
                                        if (i >= 4) { // The log table is only available after the Version 4
                                            // Log the SQL execution here
                                            logEventService.createForPrivateCalls("/DatabaseMaintenance.jsp", "SQL", LogEvent.STATUS_INFO, "SQL " + MySQLRC + ": ['" + MySQL + "']", request);
                                        }
                                        out.print("<td><textarea class=\"form-control\" name=\"SQL\" rows=\"" + rowLine + "\" style=\"background-color:transparent;border:0px;font-size:x-small;width: 100%\" readonly>");
                                        out.print(MySQLRC);
                                        out.print("</textarea></td>");
                                        out.println("</tr>");
                                    }
                                }
                            }
                            out.print("</table>");

                            DtbVersion = myVersionService.findMyVersionByKey("database");
                            if (DtbVersion == null) {
                                factoryMyversion = new FactoryMyversion();
                                DtbVersion = factoryMyversion.create("database", 0, "");
                            }
                            out.print("<b>Database Moved to Version : ");
                            out.print(DtbVersion.getValue());
                            out.print("</b><br><br></div></div>");

                            if (DtbVersion.getValue() < NewVersion) {
                                out.print("<div class=\"panel panel-default marginTop20\"><div class=\"panel-heading\"><span class=\"glyphicon glyphicon-list\"></span><label>  Pending SQL To be performed :</label></div>");
                            }
            %>
            <form action="DatabaseMaintenance.jsp?GO=Y" method="post" name="ExecApply" id="ExecApply">
            </form>
            <%
                        i = 0;
                        out.print("<div class=\"panel-body\"><table class=\"table table-hover\">");
                        out.print("<tr><th class=\"col-md-1\">version</th><th class=\"col-md-11\">SQL</th></tr>");
                        for (String MySQL : SQLInstruction) {
                            i = i + 1;
                            if (i > DtbVersion.getValue()) {
                                out.print("<tr><td>");
                                out.print(i);
                                out.print("</td><td class=\"wob\" style=\"padding:0\"><textarea class=\"form-control\" name=\"SQL\" rows=\"3\" style=\"background-color:transparent;border:0px;font-size:x-small;width: 100%\" readonly>");
                                out.print(MySQL.replace("</textarea>", "</text4rea>"));
                                out.print("</textarea></td>");
                                out.println("</tr>");
                            }
                        }
                        out.print("</table></div>");

                        if (DtbVersion.getValue() < NewVersion) {
                            out.print("</div>");
                        }

                    }
                }

                if (DtbVersion.getValue() == (NewVersion)) { // Database is already (or just have been) updated

                    out.print("<div class=\"panel-body\">");
                    out.print("<h3>Database is now uptodate. Enjoy the tool.</h3><br>");
                    out.print("<h3>Get to the <a href='.'>homepage.</a></h3><br>");
                    out.print("<h4>Show all SQL <a href=\"DatabaseMaintenance.jsp?ShowAll\">here</a>.</h4>");
                    out.print("</div>");
                    // We force the reload of the Documentation Database.
                    if ((NewVersion > 100) && (request.getParameter("GO") != null)) {
                        DocumentationDatabaseService documentationService = appContext.getBean(DocumentationDatabaseService.class);
                        documentationService.init();
                    }
            %>
            <script>
                // Database Script is now finished. 
                // We can purge the sessionstorage (to force reload of fresh data.)
                function clearSessionStorage() {
                    sessionStorage.clear();
                    console.info("sessionStorage cleared");
                }
                clearSessionStorage();
            </script>
            <%
                        if (request.getParameter("ShowAll") != null) {
                            Integer i = 0;
                            out.print("<div class=\"panel panel-default marginTop20\"><div class=\"panel-heading\"><span class=\"glyphicon glyphicon-list\"></span><label>  All SQL Scripts :</label></div>");
                            out.print("<div class=\"panel-body\"><table class=\"table table-hover\">");
                            out.print("<tr><th class=\"col-md-1\">version</th><th class=\"col-md-11\">SQL</th></tr>");
                            for (String MySQL : SQLInstruction) {
                                i = i + 1;
                                out.print("<tr><td>");
                                out.print(i);
                                out.print("</td><td class=\"wob\" style=\"padding:0\"><textarea class=\"form-control\" name=\"SQL\" rows=\"3\" style=\"background-color:transparent;border:0px;font-size:x-small;width: 100%\" readonly>");
                                out.print(MySQL.replace("</textarea>", "</text4rea>"));
                                out.print("</textarea></td>");
                                out.println("</tr>");
                            }
                            out.print("</table></div></div>");
                        }

                    }

                    if (DtbVersion.getValue() > NewVersion) { // Database is earlier than what it is supposed to do. In theory, that should never happen.

                        out.print("<div class=\"alert alert-danger\"><strong>Database version is earlier than application. Please update the version of Cerberus quickly as retro compatibility is not supported.</strong></div><br>");

                    }

                } catch (Exception exception1) {
                    LOG.error("Database Maintenance Page exception : " + exception1, exception1);
                    out.print("<div class=\"alert alert-danger\"><strong><h3>Exception raised when connecting to the database...<br><br>There is probably a database connectivity issue.<br>Please check that the database server is correctly started and application server correctly configured to access it.<br>Check the server logs for more details.</h3></strong></div><br>");
                    out.print(exception1);
                } finally {
                }
            %>         
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
        <br>
    </body>
</html>
