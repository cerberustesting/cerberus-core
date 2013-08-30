<%-- 
    Document   : DatabaseMaintenance
    Created on : Jun 9, 2013, 1:31:44 PM
    Author     : vertigo
--%>

<%@page import="com.redcats.tst.factory.IFactoryMyversion"%>
<%@page import="com.redcats.tst.factory.impl.FactoryMyversion"%>
<%@page import="com.redcats.tst.service.IDatabaseVersioningService"%>
<%@page import="com.redcats.tst.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="com.redcats.tst.database.DatabaseSpring"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="com.redcats.tst.entity.MyVersion"%>
<%@page import="com.redcats.tst.service.IMyVersionService"%>
<%@page import="org.springframework.context.ApplicationContext" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% Date DatePageStart = new Date();%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cerberus Database Maintenance</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
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
                Integer SQLLimit = 60;
                IFactoryMyversion factoryMyversion;

                try {
                    // I get here the current version of the database. (null if no database found)
                    MyVersion DtbVersion = null;
                    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                    IMyVersionService myVersionService = appContext.getBean(IMyVersionService.class);
                    if (myVersionService.findMyVersionByKey("database") != null) {
                        DtbVersion = myVersionService.findMyVersionByKey("database");
                    } else {
                        out.print("<h1><b>Database is empty and a first version has been initialised. Anytime you will deploy a new version of Cerberus, you will have to come back to this page (Admin / Database Maintenance) and execute the missing SQL in order to upgrade the database.</b></h1><br><br>");
                        factoryMyversion = new FactoryMyversion();
                        DtbVersion = factoryMyversion.create("database", 0);
                    }

                    // Displaying the current version of the database.
                    out.print("<br><table><tr><td><b>Current<br>Database Version : ");
                    out.print("</b></td>");
                    out.print("<td><b>Target<br>Database Version : </td>");
                    out.print("</b></td></tr><tr><td><b>");
                    out.print(DtbVersion.getValue());
                    out.print("</b></td>");

                    // Start to build the SQL Script here.
                    SQLInstruction = new ArrayList<String>();
                    appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                    IDatabaseVersioningService DatabaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
                    SQLInstruction = DatabaseVersionService.getSQLScript();

                    // Initialize the array that will receive the RC of every execution.
                    SQLRC = new ArrayList<String>();

                    // Calculate the version that will be updated. Version correspond directly to the size of the arry (ie the number of SQL to execute)
                    NewVersion = SQLInstruction.size();
                    out.print("<td><i><b>");
                    out.print(NewVersion);
                    out.print("</b></i></td></tr></table>");
                    out.print("<br>");

                    if (DtbVersion.getValue() < NewVersion) {
                        out.print("<h1>SQL performed in that Batch :</h1><br>");
                        out.print("<table>");
                        out.print("<tr><td>version</td><td>SQL</td></tr>");
                        Integer i = 0;
                        for (String MySQL : SQLInstruction) {
                            i = i + 1;
                            if ((i > DtbVersion.getValue()) && ((request.getParameter("GO") != null))) { // Only if the SQL has not been executed already. and button pressed.
                                if ((i < SQLLimit) || (SQLExecuted == false)) { // After version SQLLimit, only 1 execution at a time.
                                    out.print("<tr><td>");
                                    out.print(i);
                                    out.print("</td><td>");
                                    out.print(MySQL);
                                    out.print("</td>");
                                    // Execute the SQL Here
                                    MySQLRC = DatabaseVersionService.exeSQL(MySQL);
                                    SQLExecuted = true;
                                    SQLRC.add(MySQLRC);
                                    if ((i >= 3) && (MySQLRC.equalsIgnoreCase("OK"))) { // The myversion table is only available after the Version 3
                                        // Update the myversion table to comit the execution of the SQL Instruction.
                                        DtbVersion.setValue(i);
                                        myVersionService.UpdateMyVersionTable(DtbVersion);
                                    }
                                    if (i >= 4) { // The log table is only available after the Version 4
                                        // Log the SQL execution here
                                    }
                                    if (MySQLRC.equalsIgnoreCase("OK")) {
                                        out.print("<td class=\"OK\">");
                                        out.print(MySQLRC);
                                        out.print("</td>");
                                    } else {
                                        out.print("<td class=\"KO\">");
                                        out.print(MySQLRC);
                                        out.print("</td>");
                                    }
                                    out.println("</tr>");
                                }
                            }
                        }
                        out.print("</table>");
                        DtbVersion = myVersionService.findMyVersionByKey("database");
                        out.print("<b>Database Moved to Version : ");
                        out.print(DtbVersion.getValue());
                        out.print("</b><br><br>");

                        if (DtbVersion.getValue() < NewVersion) {
                            out.print("<h1>Pending SQL To be performed : </h1><br>");
            %><form action="DatabaseMaintenance.jsp?GO=Y" method="post" name="ExecApply" id="ExecApply">
                <input style="font-size: large" type="submit" value="Apply Next SQL" onClick="this.form.submit(); this.disabled=true; this.value='Processing...'; "></form>
                <%
                            i = 0;
                            out.print("<table>");
                            out.print("<tr><td>version</td><td>SQL</td></tr>");
                            for (String MySQL : SQLInstruction) {
                                i = i + 1;
                                if (i > DtbVersion.getValue()) {
                                    out.print("<tr><td>");
                                    out.print(i);
                                    out.print("</td><td>");
                                    out.print(MySQL);
                                    out.print("</td>");
                                    out.println("</tr>");
                                }
                            }
                            out.print("</table>");
                            }
                        }

                        if (DtbVersion.getValue() == (NewVersion)) { // Database is already (or just have been) updated

                            out.print("Database is now uptodate. Enjoy the tool.<br>");

                        }

                        if (DtbVersion.getValue() > NewVersion) { // Database is earlier than what it is supposed to do. In theory, that should never happen.

                            out.print("Database version is earlier than application. Please update the version of Cerberus quickly as retro compatibility is not supported.<br>");

                        }


                    } catch (Exception exception1) {
                        MyLogger.log("DatabaseMaintenance.jsp", Level.ERROR, exception1.toString());
                        out.print(exception1.toString());
                    } finally {
                    }
                %>         
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>
