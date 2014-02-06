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
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.Statement"%>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.cerberus.database.DatabaseSpring" %>
<%@ page import="java.sql.SQLException" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <%
        String DocTable = ParameterParserUtil.parseStringParam(request.getParameter("DocTable"), "empty");
        String DocField = ParameterParserUtil.parseStringParam(request.getParameter("DocField"), "empty");
        String DocValue = ParameterParserUtil.parseStringParam(request.getParameter("DocValue"), "empty");
        boolean DocValue_isdefined = true;
        if (DocValue.equalsIgnoreCase("empty")) {
            DocValue_isdefined = false;
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring db = appContext.getBean(DatabaseSpring.class);

        String Title = "";
        List<String> TitleList;
        TitleList = new ArrayList<String>();
        String Doc = "";
        List<String> DocList;
        DocList = new ArrayList<String>();

        Connection conn = db.connect();

        try {

            if (DocValue_isdefined == false) {

                Statement stmtQuery = conn.createStatement();
                Statement stmtQuerydet = conn.createStatement();
                String sqDet = "";
                String sq = "";
                if (DocTable.equalsIgnoreCase("all")) { // All the documentation displayed inside a single page.
                    sq = "SELECT DocTable, DocField, DocValue, DocLabel, DocDesc, DocLabel FROM documentation";
                    ResultSet q = stmtQuery.executeQuery(sq);
                    while (q.next()) {
                        Doc = q.getString("DocDesc");
                        if (StringUtil.isNullOrEmpty(q.getString("DocValue"))) {
                            if (StringUtil.isNullOrEmpty(request.getParameter("HideKey"))) {
                                Title = "[" + q.getString("DocTable") + " - " + q.getString("DocField") + "] - " + q.getString("DocLabel");
                            } else {
                                Title = q.getString("DocLabel");
                            }
                            sqDet = "SELECT DocValue, DocDesc, DocLabel FROM documentation where DocTable = '" + q.getString("DocTable") + "' and docfield = '" + q.getString("DocField") + "' and docValue IS NOT NULL and length(docValue) > 1 AND length(docdesc) > 1";
                            ResultSet qDet = stmtQuerydet.executeQuery(sqDet);
                            if (qDet.first()) {
                                Doc = Doc + "<table>";
                                do {
                                    Doc = Doc + "<tr><td><a href=\"?DocTable=" + q.getString("DocTable");
                                    Doc = Doc + "&amp;DocField=" + q.getString("DocField") + "&amp;DocValue=" + qDet.getString("DocValue") + "\">";
                                    Doc = Doc + qDet.getString("DocValue") + "</a></td><td>" + qDet.getString("DocLabel") + "</td></tr>";
                                } while (qDet.next());
                                Doc = Doc + "</table>";
                            }
                        } else {
                            if (StringUtil.isNullOrEmpty(request.getParameter("HideKey"))) {
                                Title = "[" + q.getString("DocTable") + " - " + q.getString("DocField") + " - " + q.getString("DocValue") + "] - " + q.getString("DocLabel");
                            } else {
                                Title = q.getString("DocLabel");
                            }
                        }
                        TitleList.add(Title);
                        DocList.add(Doc);
                    }
                } else { // Documentation of a normal field. The field could potencially have occurences at Value level that will be displayed.
                    sq = "SELECT DocLabel, DocDesc FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and length(docvalue)=0 and length(docdesc) > 1";
                    ResultSet q = stmtQuery.executeQuery(sq);
                    if (q.first()) {
                        Title = q.getString("DocLabel");
                        Doc = q.getString("DocDesc");
                        sqDet = "SELECT DocValue, DocDesc, DocLabel FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and docValue IS NOT NULL and length(docValue) > 1  AND length(docdesc) > 1";
                        ResultSet qDet = stmtQuery.executeQuery(sqDet);
                        if (qDet.first()) {
                            Doc = Doc + "<table>";
                            do {
                                Doc = Doc + "<tr><td><a href=\"?DocTable=" + DocTable;
                                Doc = Doc + "&amp;DocField=" + DocField + "&amp;DocValue=" + qDet.getString("DocValue") + "\">";
                                Doc = Doc + qDet.getString("DocValue") + "</a></td><td>" + qDet.getString("DocLabel") + "</td></tr>";
                            } while (qDet.next());
                            Doc = Doc + "</table>";
                        }
                        TitleList.add(Title);
                        DocList.add(Doc);
                    } else {
                        Title = "No Documentation Found !";
                        Doc = "";
                        TitleList.add(Title);
                        DocList.add(Doc);
                    }
                }

            } else { // Documentation of the detail of a field + value.

                Statement stmtQuery1 = conn.createStatement();
                String sq1 = "SELECT DocLabel FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and length(docvalue)=0 and length(docdesc) > 1";
                ResultSet q1 = stmtQuery1.executeQuery(sq1);
                String nav;
                nav = "";
                if (q1.first()) {
                    nav = "<a href=\"?DocTable=" + DocTable + "&amp;DocField=" + DocField + "\">" + q1.getString("DocLabel") + "</a>";
                }
                Statement stmtQueryVal = conn.createStatement();
                String sqVal = "SELECT DocDesc FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and docvalue = '" + DocValue + "' and length(docdesc) > 1";
                ResultSet qVal = stmtQueryVal.executeQuery(sqVal);
                if (qVal.first()) {
                    Title = nav + " >> " + DocValue;
                    Doc = qVal.getString("DocDesc");
                    Doc = Doc + "<br><br>Back to " + nav;
                    TitleList.add(Title);
                    DocList.add(Doc);
                } else {
                    Title = "No Documentation Found !";
                    Doc = "";
                    TitleList.add(Title);
                    DocList.add(Doc);
                }

            }

            if (TitleList.size() > 1) {
                Title = "Full Documentation";
            }

    %>


    <head>
        <title><%= Title%></title>
        <link rel="stylesheet" href="css/crb_style_doc.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>

    <body>
        <%
            Integer i = 0;
            for (String tcs : TitleList) {
                if (i > 0) {
                    out.print("<br>");
                }
        %>      
        <table class="doctb">
            <tr id="header" class="doctl"><td><%= tcs%></td></tr>
            <tr>
                <td style="width: 100%;">
                    <%= DocList.get(i)%>
                </td>
            </tr>
        </table>
        <%
                    i++;
                }
            } catch (Exception e) {
                out.println("<br> error message : " + e.getMessage() + " "
                        + e.toString() + "<br>");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        //TODO logger
                    }
                }
            }
        %>
        <br>
        <span class="close"><a href="javascript:self.close()">Close</a> the popup.</span>
        <br>
        <span class="footer">DocTable:<%= DocTable%> | DocField:<%= DocField%> | DocValue:<%= DocValue%></span>
    </body>
</html>

