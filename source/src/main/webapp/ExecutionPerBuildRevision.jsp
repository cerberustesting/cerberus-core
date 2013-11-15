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
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Execution Per Build and Revision</title>
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
        <%
            Connection conn = db.connect();
            try {
        %>

        <%
            String title1 = dbDocS(conn, "homepage", "RegressionExecutionStatus", "");
            String build = dbDocS(conn, "testcaseexecution", "Build", "Build");
            String revision = dbDocS(conn, "testcaseexecution", "Revision", "Revision");
            String nbExecution = dbDocS(conn, "homepage", "NbExecution", "NbExecution");
            String nbOK = dbDocS(conn, "homepage", "NbOK", "NbOK");
            String okPercentage = dbDocS(conn, "homepage", "OK_percentage", "%OK");
            String nbTC = dbDocS(conn, "homepage", "NbTC", "NbTC");
            String nbExePerTc = dbDocS(conn, "homepage", "nb_exe_per_tc", "nb_exe_per_tc");
            String days = dbDocS(conn, "homepage", "Days", "Days");
            String nbTcPerDay = dbDocS(conn, "homepage", "nb_tc_per_day", "nb_tc_per_day");
            String nbApp = dbDocS(conn, "homepage", "NbAPP", "NbAPP");
        %>

        <div class="divBorder">
            <h3 style="color: blue"><%=title1%></h3>
            <table id="tableau">
                <thead>
                    <tr id="header">
                        <th style="width: 80px"><%=build%></th>
                        <th style="width: 60px"><%=revision%></th>
                        <th style="width: 60px"><%=nbExecution%></th>
                        <th style="width: 60px"><%=nbOK%></th>
                        <th style="width: 60px"><%=okPercentage%></th>
                        <th style="width: 60px"><%=nbTC%></th>
                        <th style="width: 60px"><%=nbExePerTc%></th>
                        <th style="width: 60px"><%=days%></th>
                        <th style="width: 80px"><%=nbTcPerDay%></th>
                        <th style="width: 80px"><%=nbApp%></th>
                        <th style="width: 80px">Env Detail</th>
                        <th style="width: 80px">Build Content</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        ArrayList<ArrayList<String>> arrayExecution = (ArrayList<ArrayList<String>>) request.getAttribute("arrayExecution");
                        ArrayList<ArrayList<ArrayList<String>>> arrayContent = (ArrayList<ArrayList<ArrayList<String>>>) request.getAttribute("arrayContent");
                        ArrayList<ArrayList<ArrayList<String>>> arrayExecutionEnv = (ArrayList<ArrayList<ArrayList<String>>>) request.getAttribute("arrayExecutionEnv");
                        for (ArrayList<String> arrayE : arrayExecution) {
                    %>
                    <tr>
                        <td style="font-weight: bold; background-color: white" name="Build"><%=arrayE.get(0)%></td>
                        <td style="font-weight: bold; background-color: white" name="Revision"><%=arrayE.get(1)%></td>
                        <td name="NbExecution" style="background-color: white"><%=arrayE.get(2)%></td>
                        <td name="NbOK" style="background-color: white"><%=arrayE.get(3)%></td>
                        <td name="PerOK" style="background-color: white"><%=arrayE.get(4)%></td>
                        <td name="NbTc" style="background-color: white"><%=arrayE.get(5)%></td>
                        <td name="NbExecPerTc" style="background-color: white"><%=arrayE.get(6)%></td>
                        <td name="revDuration" style="background-color: white"><%=arrayE.get(7)%></td>
                        <td name="NbExecPerTcPerDay" style="background-color: white"><%=arrayE.get(8)%></td>
                        <td name="NbApp" style="background-color: white"><%=arrayE.get(9)%></td>
                        <td>
                            <input id="button<%=arrayE.get(0)%><%=arrayE.get(1)%>EnvMore" style="display:inline" type="button"
                                   value="+" onclick="setVisibleEnv('<%=arrayE.get(0)%>', '<%=arrayE.get(1)%>');">
                            <input id="button<%=arrayE.get(0)%><%=arrayE.get(1)%>EnvLess" style="display:none" type="button" value="-"
                                   onclick="setInvisibleEnv('<%=arrayE.get(0)%>', '<%=arrayE.get(1)%>');">
                        </td>
                        <td style="background-color: white; text-align: center">
                            <%
                                ArrayList<ArrayList<String>> content = arrayContent.get(arrayExecution.indexOf(arrayE));
                                ArrayList<ArrayList<String>> environment = arrayExecutionEnv.get(arrayExecution.indexOf(arrayE));
                                if (!content.isEmpty()) {
                            %>
                            <input id="button<%=arrayE.get(0)%><%=arrayE.get(1)%>More" style="display:inline" type="button" value="+" onclick="setVisibleContent('<%=arrayE.get(0)%>', '<%=arrayE.get(1)%>');">
                            <input id="button<%=arrayE.get(0)%><%=arrayE.get(1)%>Less" style="display:none" type="button" value="-" onclick="setInvisibleContent('<%=arrayE.get(0)%>', '<%=arrayE.get(1)%>');">
                            <%
                                }
                            %>
                        </td>
                    </tr>
                    <%
                        if (!content.isEmpty()) {
                    %>
                    <tr>
                        <td id="wob" colspan="10">
                            <table id="<%=arrayE.get(0)%><%=arrayE.get(1)%>" style="display: none">
                                <%
                                    for (ArrayList<String> arrayC : content) {
                                %>
                                <tr>
                                    <td style="font-weight: bold; background-color: white; width:80px" name="Build"><%= arrayC.get(0)%></td>
                                    <td style="font-weight: bold; background-color: white; width:60px" name="Revision"><%= arrayC.get(1)%></td>
                                    <td name="Application" style="background-color: white" colspan="3"><%= arrayC.get(2)%></td>
                                    <td name="Release" style="background-color: white" colspan="3">Rls: <%= arrayC.get(3)%></td>
                                    <td><a style="text-align:right; color:black;" href="<%= arrayC.get(4)%>">Details</a></td>
                                </tr>
                                <%  }%>

                            </table>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <td id="wob" colspan="10">
                            <table id="<%=arrayE.get(0)%><%=arrayE.get(1)%>Env" style="display: none">
                                <thead>
                                    <tr>
                                        <th style="width: 60px; border-style: none;"></th>
                                        <th>Environment</th>
                                        <th style="width: 60px"><%=nbExecution%></th>
                                        <th style="width: 60px"><%=nbOK%></th>
                                        <th style="width: 60px"><%=okPercentage%></th>
                                        <th style="width: 60px"><%=nbTC%></th>
                                        <th style="width: 60px"><%=nbExePerTc%></th>
                                        <th style="width: 60px"><%=days%></th>
                                        <th style="width: 80px"><%=nbTcPerDay%></th>
                                        <th style="width: 80px"><%=nbApp%></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        for (ArrayList<String> arrayEnv : environment) {
                                    %>
                                    <tr>
                                        <td style="border-style: none;"></td>
                                        <td><%=arrayEnv.get(0)%></td>
                                        <td name="NbExecution" style="background-color: white"><%=arrayEnv.get(1)%></td>
                                        <td name="NbOK" style="background-color: white"><%=arrayEnv.get(2)%></td>
                                        <td name="PerOK" style="background-color: white"><%=arrayEnv.get(3)%></td>
                                        <td name="NbTc" style="background-color: white"><%=arrayEnv.get(4)%></td>
                                        <td name="NbExecPerTc" style="background-color: white"><%=arrayEnv.get(5)%></td>
                                        <td name="revDuration" style="background-color: white"><%=arrayEnv.get(6)%></td>
                                        <td name="NbExecPerTcPerDay" style="background-color: white"><%=arrayEnv.get(7)%></td>
                                        <td name="NbApp" style="background-color: white"><%=arrayEnv.get(8)%></td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <% }
                    %>
                </tbody>
            </table>
        </div>

        <%
            title1 = dbDocS(conn, "homepage", "RegressionExecutionStatus1", "");

            arrayExecution = (ArrayList<ArrayList<String>>) request.getAttribute("arrayExecutionExternal");
            arrayExecutionEnv = (ArrayList<ArrayList<ArrayList<String>>>) request.getAttribute("arrayExecutionEnvExternal");
        %>
        <div class="divBorder">
            <h3 style="color: blue"><%=title1%></h3>
            <table id="tableau">
                <thead>
                    <tr id="header">
                        <th style="width: 80px"><%=build%></th>
                        <th style="width: 60px"><%=revision%></th>
                        <th style="width: 60px"><%=nbExecution%></th>
                        <th style="width: 60px"><%=nbOK%></th>
                        <th style="width: 60px"><%=okPercentage%></th>
                        <th style="width: 60px"><%=nbTC%></th>
                        <th style="width: 60px"><%=nbExePerTc%></th>
                        <th style="width: 60px"><%=days%></th>
                        <th style="width: 80px"><%=nbTcPerDay%></th>
                        <th style="width: 80px"><%=nbApp%></th>
                        <th style="width: 80px">Env Detail</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (!(arrayExecution == null)) {
                            for (ArrayList<String> arrayE1 : arrayExecution) {
                    %>
                    <tr>
                        <td style="font-weight: bold; background-color: white" name="Build"><%=arrayE1.get(0)%></td>
                        <td style="font-weight: bold; background-color: white" name="Revision"><%=arrayE1.get(1)%></td>
                        <td name="NbExecution" style="background-color: white"><%=arrayE1.get(2)%></td>
                        <td name="NbOK" style="background-color: white"><%=arrayE1.get(3)%></td>
                        <td name="PerOK" style="background-color: white"><%=arrayE1.get(4)%></td>
                        <td name="NbTc" style="background-color: white"><%=arrayE1.get(5)%></td>
                        <td name="NbExecPerTc" style="background-color: white"><%=arrayE1.get(6)%></td>
                        <td name="revDuration" style="background-color: white"><%=arrayE1.get(7)%></td>
                        <td name="NbExecPerTcPerDay" style="background-color: white"><%=arrayE1.get(8)%></td>
                        <td name="NbApp" style="background-color: white"><%=arrayE1.get(9)%></td>
                        <td>
                            <input id="button<%=arrayE1.get(0)%><%=arrayE1.get(1)%>EnvExtMore" style="display:inline" type="button"
                                   value="+" onclick="setVisibleEnvExternal('<%=arrayE1.get(0)%>', '<%=arrayE1.get(1)%>');">
                            <input id="button<%=arrayE1.get(0)%><%=arrayE1.get(1)%>EnvExtLess" style="display:none" type="button" value="-"
                                   onclick="setInvisibleEnvExternal('<%=arrayE1.get(0)%>', '<%=arrayE1.get(1)%>');">
                        </td>
                    </tr>
                    <tr>
                        <td id="wob" colspan="10">
                            <table id="<%=arrayE1.get(0)%><%=arrayE1.get(1)%>EnvExt" style="display: none">
                                <thead>
                                    <tr>
                                        <th style="width: 60px; border-style: none;"></th>
                                        <th>Environment</th>
                                        <th style="width: 60px"><%=nbExecution%></th>
                                        <th style="width: 60px"><%=nbOK%></th>
                                        <th style="width: 60px"><%=okPercentage%></th>
                                        <th style="width: 60px"><%=nbTC%></th>
                                        <th style="width: 60px"><%=nbExePerTc%></th>
                                        <th style="width: 60px"><%=days%></th>
                                        <th style="width: 80px"><%=nbTcPerDay%></th>
                                        <th style="width: 80px"><%=nbApp%></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        ArrayList<ArrayList<String>> environment = arrayExecutionEnv.get(arrayExecution.indexOf(arrayE1));
                                        for (ArrayList<String> arrayEnv : environment) {
                                    %>
                                    <tr>
                                        <td style="border-style: none;"></td>
                                        <td><%=arrayEnv.get(0)%></td>
                                        <td name="NbExecution" style="background-color: white"><%=arrayEnv.get(1)%></td>
                                        <td name="NbOK" style="background-color: white"><%=arrayEnv.get(2)%></td>
                                        <td name="PerOK" style="background-color: white"><%=arrayEnv.get(3)%></td>
                                        <td name="NbTc" style="background-color: white"><%=arrayEnv.get(4)%></td>
                                        <td name="NbExecPerTc" style="background-color: white"><%=arrayEnv.get(5)%></td>
                                        <td name="revDuration" style="background-color: white"><%=arrayEnv.get(6)%></td>
                                        <td name="NbExecPerTcPerDay" style="background-color: white"><%=arrayEnv.get(7)%></td>
                                        <td name="NbApp" style="background-color: white"><%=arrayEnv.get(8)%></td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
        <%
        } else {
        %>
        <div class="divBorder">
            <h3 style="color: blue"><%=title1%></h3>
        </div>
        <%
                }
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
        <script type="text/javascript">
            $.each( $(".verticalText"), function () {
                $(this).html($(this).text().replace(/(.)/g, "$1<br />"))
            }
        );

            function setVisibleContent(build, revision){
                setInvisibleEnv(build, revision);
                document.getElementById(build+revision).style.display = "table";
                document.getElementById('button'+build+revision+'Less').style.display = "inline";
                document.getElementById('button'+build+revision+'More').style.display = "none";
            }

            function setInvisibleContent(build, revision){
                if($("#"+build+revision).length > 0 ){
                    document.getElementById(build+revision).style.display = "none";
                    document.getElementById('button'+build+revision+'Less').style.display = "none";
                    document.getElementById('button'+build+revision+'More').style.display = "inline";
                }
            }

            function setVisibleEnv(build, revision) {
                setInvisibleContent(build, revision);
                document.getElementById(build + revision + 'Env').style.display = "table";
                document.getElementById('button' + build + revision + 'EnvLess').style.display = "inline";
                document.getElementById('button' + build + revision + 'EnvMore').style.display = "none";
            }

            function setInvisibleEnv(build, revision) {
                document.getElementById(build + revision + 'Env').style.display = "none";
                document.getElementById('button' + build + revision + 'EnvLess').style.display = "none";
                document.getElementById('button' + build + revision + 'EnvMore').style.display = "inline";
            }
            
            function setVisibleEnvExternal(build, revision) {
                document.getElementById(build + revision + 'EnvExt').style.display = "table";
                document.getElementById('button' + build + revision + 'EnvExtLess').style.display = "inline";
                document.getElementById('button' + build + revision + 'EnvExtMore').style.display = "none";
            }

            function setInvisibleEnvExternal(build, revision) {
                document.getElementById(build + revision + 'EnvExt').style.display = "none";
                document.getElementById('button' + build + revision + 'EnvExtLess').style.display = "none";
                document.getElementById('button' + build + revision + 'EnvExtMore').style.display = "inline";
            }
            
        </script>
        <br/>
        <span><%=display_footer((Date) request.getAttribute("startPageGeneration"))%></span>
    </body>
</html>