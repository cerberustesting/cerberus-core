<%@page import="com.redcats.tst.service.IDatabaseVersioningService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<% Date DatePageStart = new Date();%>

<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Homepage</title>
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
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
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
                                String test = (dbDocS(conn, "test", "Test", "Test"));
                                String nbtest = (dbDocS(conn, "homepage", "NbTest", "NbTest")).split(" ")[0];
                                String standby = (dbDocS(conn, "homepage", "Standby", "Standy")).split(" ")[0];
                                String tbi = (dbDocS(conn, "homepage", "tbi", "TBI")).split(" ")[0];
                                String inp = (dbDocS(conn, "homepage", "InProgress", "InProgress")).split(" ")[0];
                                String tbv = (dbDocS(conn, "homepage", "tbv", "TBV")).split(" ")[0];
                                String wor = (dbDocS(conn, "homepage", "Working", "Working")).split(" ")[0];
                            %>
                            <tr id="header">
                                <th style="width: 145px; text-align: left"><%=test%></th>
                                <th class="verticalText"><%=nbtest%></th>
                                <th class="verticalText"><%=standby%></th>
                                <th class="verticalText"><%=tbi%></th>
                                <th class="verticalText"><%=inp%></th>
                                <th class="verticalText"><%=tbv%></th>
                                <th class="verticalText"><%=wor%></th>
                            </tr>
                            <%
                                ArrayList<ArrayList<String>> arrayTest = (ArrayList<ArrayList<String>>) request.getAttribute("arrayTest");
                                for (int i = 0; i < arrayTest.size(); i++) {
                                    ArrayList<String> array = arrayTest.get(i);

                                    if (i == arrayTest.size() / 3 || i == 2 * (arrayTest.size() / 3)) {
                            %>
                        </table> </td> <td id="wob" valign="top"> <table id="tableau">
                            <tr id="header">
                                <th style="width: 145px; text-align: left"><%=test%></th>
                                <th class="verticalText"><%=nbtest%></th>
                                <th class="verticalText"><%=standby%></th>
                                <th class="verticalText"><%=tbi%></th>
                                <th class="verticalText"><%=inp%></th>
                                <th class="verticalText"><%=tbv%></th>
                                <th class="verticalText"><%=wor%></th>
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
                                <td id="nbtest" name="NbTest" style="background-color: white;"><%=array.get(1)%></td>
                                <td name="StandBy" style="background-color: white;"><%=array.get(2)%></td>
                                <td name="TBI" style="background-color: white;"><%=array.get(3)%></td>
                                <td name="InProgress" style="background-color: white;"><%=array.get(4)%></td>
                                <td name="TBV" style="background-color: white;"><%=array.get(5)%></td>
                                <td name="Working" style="background-color: white;"><%=array.get(6)%></td>
                            </tr>
                            <% }%>
                        </table>
                    </td>
                </tr>
            </table>
        </div>

        <%
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
            <h3 style="color: blue">Execution Status</h3>
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
                                    for(ArrayList<String> arrayEnv : environment){
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
                        db.disconnect();
                    %>
                </tbody>
            </table>
        </div>
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
        </script>
        <br/>
        <span><%=display_footer(DatePageStart)%></span>
    </body>
</html>