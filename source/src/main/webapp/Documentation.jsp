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
        String DocTable;
        if (request.getParameter("DocTable") != null) {
            DocTable = request.getParameter("DocTable");
        } else {
            DocTable = new String("empty");
        }
        String DocField;
        if (request.getParameter("DocField") != null) {
            DocField = request.getParameter("DocField");
        } else {
            DocField = new String("empty");
        }
        String DocValue;
        boolean DocValue_isdefined;
        DocValue_isdefined = true;
        if (request.getParameter("DocValue") != null) {
            DocValue = request.getParameter("DocValue");
        } else {
            DocValue = new String("empty");
            DocValue_isdefined = false;
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring db = appContext.getBean(DatabaseSpring.class);

        Connection conn = db.connect();
        try {

            if (DocValue_isdefined == false) {


                Statement stmtQuery = conn.createStatement();
                String sq = "SELECT DocLabel, DocDesc FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and length(docvalue)=0 and length(docdesc) > 1";
                ResultSet q = stmtQuery.executeQuery(sq);
                if (q.first()) {

    %>

    <head>
        <title><%= q.getString("DocLabel")%></title>
        <link rel="stylesheet" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>

    <body>
<table class="doctb">
    <tr id="header" class="doctl"><td><%= q.getString("DocLabel")%></td></tr>
       
            <tr>
                <td style="width: 100%;">

                    <%
                        out.print(q.getString("DocDesc"));
                    %>

                    <%
                        Statement stmtQuerydet = conn.createStatement();
                        String sqDet = "SELECT DocValue, DocDesc FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and docValue IS NOT NULL AND length (docdesc) > 1";
                        ResultSet qDet = stmtQuery.executeQuery(sqDet);
                        while (qDet.next()) {
                    %>
                    <a href="?DocTable=<%=DocTable%>&DocField=<%=DocField%>&DocValue=<%=qDet.getString("DocValue")%>"><%=qDet.getString("DocValue")%></a><br>                                                         
                    <%
                        }
                    %>
                </td>
            </tr>
        </table>

        <%
        } else {
        %>
    <head>
        <title>No Documentation Found !</title>
        <link rel="stylesheet" href="css/crb_style.css">
    </head>

    <body>
        <a class="doctl">No Documentation Found !</a>
        <%    }
        } else {
                Statement stmtQuery1 = conn.createStatement();
                String sq1 = "SELECT DocLabel FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and length(docvalue)=0 and length(docdesc) > 1";
                ResultSet q1 = stmtQuery1.executeQuery(sq1);
                String nav;
                nav = "";
                               if (q1.first()) {
                                   nav = "<a href=\"?DocTable=" + DocTable + "&DocField=" + DocField + "\">" + q1.getString("DocLabel") + "</a> -- ";
                                   }
                           Statement stmtQueryVal = conn.createStatement();
            String sqVal = "SELECT DocDesc FROM documentation where DocTable = '" + DocTable + "' and docfield = '" + DocField + "' and docvalue = '" + DocValue + "' and length(docdesc) > 1";
            ResultSet qVal = stmtQueryVal.executeQuery(sqVal);
            if (qVal.first()) {
        %>

    <head>
        <title><%= DocValue%></title>
        <link rel="stylesheet" href="css/crb_style.css">
    </head>

    <body>
        <a class="doctl"><%= nav%><%= DocValue%></a>
        <table class="doctb">
            <tr>
                <td style="width: 100%;">
                    <%
                        out.print(qVal.getString("DocDesc"));
                    %>

                </td>
            </tr>
        </table>

        <%

        } else {
        %>
    <head>
        <title>No Documentation Found !</title>
        <link rel="stylesheet" href="css/crb_style.css">
    </head>

    <body>
        <a class="doctl">No Documentation Found !</a>
        <%            }

                }
            } catch (Exception e) {
                out.println("<br> error message : " + e.getMessage() + " "
                        + e.toString() + "<br>");
            } finally{
                if(conn != null){
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        //TODO logger
                    }
                }
            }
        %>

        <br>
    <z class="close"><a href="javascript:self.close()">Close</a> the popup.</z>
    <br>
    <a class="footer">DocTable:<%= DocTable%> | DocField:<%= DocField%> | DocValue:<%= DocValue%></a>
</body>
</html>

