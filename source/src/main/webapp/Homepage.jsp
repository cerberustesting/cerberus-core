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
<%@page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE HTML>
<!DOCTYPE html>

<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <!--<link rel="stylesheet" type="text/css" href="css/crb_style.css">-->
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <style media="screen" type="text/css">
            @import "css/demo_page.css";
            @import "css/demo_table.css";
            @import "css/demo_table_jui.css";
            @import "css/themes/base/jquery-ui.css";
            @import "css/themes/smoothness/jquery-ui-1.7.2.custom.css";
        </style>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
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
%>
        <script type="text/javascript">

            $(document).ready(function() {
                var mySys = getSys();
                var oTable = $('#testTable').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": false,
                    "sAjaxSource": "Homepage?MySystem="+mySys,
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Application", "sWidth": "40%"},
                        {"sName": "Total", "sWidth": "10%"}
                        <%
                                    List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
            for (Invariant i : myInvariants) {

                        %>
                        ,{"sName": "<%=i.getValue()%>"}
                                            <%
                                                                                       }
                                            %>

                    ]
                    
                }
            );
            });


        </script>
        <%
            IUserService userService = appContext.getBean(IUserService.class);
            User myUser = userService.findUserByKey(request.getUserPrincipal().getName());
            String MySystem = ParameterParserUtil.parseStringParam(request.getParameter("MySystem"), "");

            if (MySystem.equals("")) {
                MySystem = myUser.getDefaultSystem();
            } else {
                if (!(myUser.getDefaultSystem().equals(MySystem))) {
                    myUser.setDefaultSystem(MySystem);
                    userService.updateUser(myUser);
                }
            }


            IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
                if (!(DatabaseVersioningService.isDatabaseUptodate()) && request.isUserInRole("Administrator")) {%>
        <script>
            var r=confirm("WARNING : Database Not Uptodate   >>   Redirect to the DatabaseMaintenance page ?");
            if (r==true)
            {
                location.href='./DatabaseMaintenance.jsp';
            }
        </script>

        <% }


//               if (myUser.getRequest().equalsIgnoreCase("Y")) {
//                request.getRequestDispatcher("/ChangePassword.jsp").forward(request, response);
//            } else {


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
            </table>
        </div>
        <% // } %>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
