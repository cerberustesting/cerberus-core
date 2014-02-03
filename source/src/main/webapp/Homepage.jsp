<%@page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE HTML>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
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

<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <style>
            .divBorder{
                background-color: #f3f6fa;
                border: solid;
                border-width: 2px;
                border-color: #8999c4;
                border-radius: 5px 5px 5px 5px;
                margin-top: 20px;
                width : 1260px;
            }

            .verticalText{
                width: 40px;
                font-size: x-small;
            }

            td{
                text-align: center;
            }
        </style>
    </head>
    <body id="body">
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <div class="divBorder">
            <%
                IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
                if (!(DatabaseVersioningService.isDatabaseUptodate())) {
                    out.print("<b>WARNING : Database Not Uptodate</b>");
                }
            %>
            <h3 style="color: blue">Tests Status</h3>
            <table valign="top">
                <tr>
                    <td id="wob" valign="top">
                        <table id="tableau">
                            <%
                                Connection conn = db.connect();
                                try {
                                    String test = (dbDocS(conn, "test", "Test", "Test"));
                                    String nbtest = (dbDocS(conn, "homepage", "NbTest", "NbTest")).split(" ")[0];
                                    String standby = (dbDocS(conn, "homepage", "Standby", "Standy")).split(" ")[0];
                                    String tbi = (dbDocS(conn, "homepage", "tbi", "TBI")).split(" ")[0];
                                    String inp = (dbDocS(conn, "homepage", "InProgress", "InProgress")).split(" ")[0];
                                    String tbv = (dbDocS(conn, "homepage", "tbv", "TBV")).split(" ")[0];
                                    String wor = (dbDocS(conn, "homepage", "Working", "Working")).split(" ")[0];
                            %>
                            <tr id="header">
                                <th style="width: 145px; text-align: left"><%=test%>&nbsp;</th>
                                <th class="verticalText"><%=nbtest%>&nbsp;</th>
                                <th class="verticalText"><%=standby%>&nbsp;</th>
                                <th class="verticalText"><%=tbi%>&nbsp;</th>
                                <th class="verticalText"><%=inp%>&nbsp;</th>
                                <th class="verticalText"><%=tbv%>&nbsp;</th>
                                <th class="verticalText"><%=wor%>&nbsp;</th>
                            </tr>
                            <%
                                ArrayList<ArrayList<String>> arrayTest = (ArrayList<ArrayList<String>>) request.getAttribute("arrayTest");
                                for (int i = 0; i < arrayTest.size(); i++) {
                                    ArrayList<String> array = arrayTest.get(i);

                                    if (i == arrayTest.size() / 3 || i == 2 * (arrayTest.size() / 3)) {
                            %>
                        </table> </td> <td id="wob" valign="top"> <table id="tableau">
                            <tr id="header">
                                <th style="width: 145px; text-align: left"><%=test%>&nbsp;</th>
                                <th class="verticalText"><%=nbtest%>&nbsp;</th>
                                <th class="verticalText"><%=standby%>&nbsp;</th>
                                <th class="verticalText"><%=tbi%>&nbsp;</th>
                                <th class="verticalText"><%=inp%>&nbsp;</th>
                                <th class="verticalText"><%=tbv%>&nbsp;</th>
                                <th class="verticalText"><%=wor%>&nbsp;</th>
                            </tr>
                            <% }%>
                            <tr>
                                <td style="font-weight: bold; background-color: white; text-align: left;" name="Test">
                                    <% if (i != (arrayTest.size() - 1)) {%>
                                    <a style="color:black; text-decoration:none" href="Test.jsp?stestbox=<%=array.get(0)%>"><%=array.get(0)%></a>
                                    <% } else {%>
                                    <a style="color:black; text-decoration:none"><%=array.get(0)%></a>
                                    <% }%>
                                </td>
                                <td id="nbtest" name="NbTest" style="background-color: white;"><%=array.get(1)%>&nbsp;</td>
                                <td name="StandBy" style="background-color: white;"><%=array.get(2)%>&nbsp;</td>
                                <td name="TBI" style="background-color: white;"><%=array.get(3)%>&nbsp;</td>
                                <td name="InProgress" style="background-color: white;"><%=array.get(4)%>&nbsp;</td>
                                <td name="TBV" style="background-color: white;"><%=array.get(5)%>&nbsp;</td>
                                <td name="Working" style="background-color: white;"><%=array.get(6)%>&nbsp;</td>
                            </tr>
                            <% }%>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
        <%
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

        <br/>
        <span><%=display_footer((Date) request.getAttribute("startPageGeneration"))%></span>
    </body>
</html>