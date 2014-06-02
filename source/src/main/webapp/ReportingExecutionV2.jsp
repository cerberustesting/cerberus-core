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
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Execution Reporting : Status</title>

    <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/>

    <link rel="stylesheet" type="text/css" href="css/crb_style.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
    <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="js/FixedHeader.js"></script>

    <script type="text/javascript">
        var oTable;

//        var country = ["BE", "CH", "ES", "FR", "IT", "PT", "RU", "UK", "VI"];
        <% int countries = 3; %>
        var country = ["PT", "ES", "BE"];
        var browser = ["firefox"];
        $(document).ready(function () {
            //columns will be added based on the form
            $.each(country, function (index, elem) {
//                $('#Comment').before("<th colspan='"+browser.length+"'>"+elem+"</th>");
                $('#Comment').before("<th colspan='" + (browser.length * 2) + "'>" + elem + "</th>");
                $.each(browser, function (i, e) {
//                    $('#Country').append("<th>"+e+"</th>");
                    $('#Country').append("<th colspan='2'>" + e + "</th>");
                    $('#teste').append("<th></th><th></th>");
                });
            });

            oTable = $('#reporting').dataTable({
                "bServerSide": true,
                "sAjaxSource": "GetReport",
                "bJQueryUI": true,
                "bProcessing": true,
                "bFilter": false,
                "bInfo": false,
                "bSort": false,
                "bPaginate": false,
                "iDisplayLength": -1,
                "aoColumnDefs": [
                    <% for(int i = 0; i < countries; i++){ %>
                    {"aTargets": [<%=6+i*2%>],
                        "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                            if (oData[<%=6+i*2%>] === "") {
                                $(nTd).addClass('NOINF');
                            } else {
                                $(nTd).addClass(oData[<%=6+i*2%>]);
                            }
                        },
                        "mRender": function (data, type, full) {
                            return "<a class='" + data + "F' href='ExecutionDetail.jsp?id_tc='>" + data + "</a>";
                        }
                    },
                    <% } %>
                ],
                "fnServerParams": function (aoData) {
                    aoData.push({ "name": "country", "value": country }, { "name": "browser", "value": browser });
                },
                "fnInitComplete": function () {
                    new FixedHeader(oTable, {
                        left: true,
                        zTop: 98
                    });

                    $('.FixedHeader_Left table tr#Country th').remove();
                    $('.FixedHeader_Left table tr#teste th').remove();
                }
            });
        });
    </script>
    <style>
        div.FixedHeader_Cloned th,
        div.FixedHeader_Cloned td {
            background-color: white !important;
        }
    </style>
</head>
<body>
<%@ include file="include/header.jsp" %>

<div id="body">
    <%--<%!--%>
    <%--String getParam(String param){--%>
    <%--return (param != null && !param.isEmpty()) ? param : "";--%>
    <%--}--%>
    <%--%>--%>
    <%--<%--%>
    <%--String tag = getParam(request.getParameter("Tag"));--%>
    <%--String browserFullVersion = getParam(request.getParameter("BrowserFullVersion"));--%>
    <%--String port = getParam(request.getParameter("Port"));--%>
    <%--String ip = getParam(request.getParameter("Ip"));--%>
    <%--String comment = getParam(request.getParameter("Comment"));--%>

    <%--String systemBR; // Used for filtering Build and Revision.--%>
    <%--if (request.getParameter("SystemExe") != null && request.getParameter("SystemExe").compareTo("All") != 0) {--%>
    <%--systemBR = request.getParameter("SystemExe");--%>
    <%--} else {--%>
    <%--if (request.getParameter("system") != null && !request.getParameter("system").isEmpty()) {--%>
    <%--systemBR = request.getParameter("system");--%>
    <%--} else{--%>
    <%--systemBR = request.getAttribute("MySystem").toString();--%>
    <%--}--%>
    <%--}--%>

    <%--%>--%>
    <table id="reporting" class="display" style="color: #555555;font-family: Trebuchet MS;font-weight: bold;">
        <thead>
        <tr>
            <th rowspan="3">Test</th>
            <th rowspan="3">TestCase</th>
            <th rowspan="3">Application</th>
            <th rowspan="3">Description</th>
            <th rowspan="3">Priority</th>
            <th rowspan="3">Status</th>
            <th rowspan="3" id="Comment">Comment</th>
            <th rowspan="3">Bug ID</th>
            <th rowspan="3">Group</th>
        </tr>
        <tr id="Country"></tr>
        <tr id="teste"></tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
</body>
</html>