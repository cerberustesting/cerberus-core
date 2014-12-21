<%--
  ~ Cerberus Copyright (C) 2013 vertigo17
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
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE HTML>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
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
        <script>
            function getSys()
            {
                var x = document.getElementById("systemSelected").value;
                return x;
            }
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>

        <%
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            String MySystem = request.getAttribute("MySystem").toString();
        %>
        <script type="text/javascript">

            $(document).ready(function() {
                var numberOfCol = [];
            <%
                List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
                for (int i = 0; i < myInvariants.size(); i++) {
            %>
                numberOfCol.push("<%=i%>");
            <% }%>
                var mySys = getSys();
                var oTable = $('#testTable').dataTable({
                "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "sAjaxSource": "Homepage?MySystem=<%=MySystem%>",
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 25,
                        "fnFooterCallback": function(nRow, aaData, iStart, iEnd, aiDisplay) {
                            var nCells = nRow.getElementsByTagName('th');
                            for (var j = 0; j < numberOfCol.length+1; j++) {
                                var iTotalDebit = 0;
                                for (var i = 0; i < aaData.length; i++) {
                                    iTotalDebit += parseInt(aaData[i][j + 1] === null ? 0 : aaData[i][j + 1]);
                                }
                                nCells[j + 1].innerHTML = iTotalDebit;
                            }
                            $('#testTable tfoot th').css('text-align', 'center');
                            $('#testTable tfoot th').css('padding', '3px 5px');
                            $('#testTable tfoot th').css('font-weight', 'bold');
                        },
                        "aoColumns": [
                        {"sName": "Application", "sWidth": "40%"},
                        {"sName": "Total", "sWidth": "10%"}
            <%
                //List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
                for (Invariant i : myInvariants) {

            %>
                        , {"sName": "<%=i.getValue()%>"}
            <% } %>

                        ]


            }
            );
            });</script>
            <%

                IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
                if (!(DatabaseVersioningService.isDatabaseUptodate()) && request.isUserInRole("Administrator")) {%>
        <script>
                    var r = confirm("WARNING : Database Not Uptodate >> Redirect to the DatabaseMaintenance page ?");
            if (r == true)
            {
                location.href = './DatabaseMaintenance.jsp';
            }
        </script>

        <% }
        %>
        <input id="systemSelected" value="<%=MySystem%>" style="display:none">
        <p class="dttTitle">TestCase per Application</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="testTable" class="display">
                <thead>
                    <tr>
                        <th>Application</th>
                        <th>Total</th>
                            <%
                                for (Invariant i : myInvariants) {

                            %>
                        <th><%=i.getValue()%></th>
                            <%
                                }
                            %>
                    </tr>
                </thead>
                <tbody>
                </tbody>
                <tfoot>
                    <tr>
                        <th>GRAND TOTAL</th>
                        <th>Total</th>
                            <%
                                for (Invariant i : myInvariants) {

                            %>
                        <th><%=i.getValue()%></th>
                            <%
                                }
                            %>
                    </tr> 
                </tfoot>
            </table>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
