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
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test Per Application</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
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

            String appSel = "";
            boolean filterApp = false;
                    if (request.getParameter("Application") != null 
                            && !request.getParameter("Application").equals("All")) {
                        appSel = request.getParameter("Application");
                        filterApp = true;
                    }

            
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);
            List<Application> appList = applicationService.findApplicationBySystem(MySystem);
            List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        %>
         <div class="filters" style="float:left; width:100%; height:30px">
                <div style="float:left; width:100px"><p class="dttTitle">Filters</p></div>
                <div style="float:left; width:100px;font-weight: bold;"><%out.print(docService.findLabelHTML("application", "application", "Application"));%></div>
                <div id="selectboxtestpage" style="float:left">
                    <form action="TestPerApplication.jsp" method="post" name="selectApplication">
                        <select id="Application" name="Application" style="width: 500px">
                            <option style="width: 500px" value="All">-- ALL --</option>
                            <%
                                String optstyle = "";
                                for (Application appL : appList) {
                                    
                            %><option style="width: 500px;<%=optstyle%>" <%
                                if (appSel.equalsIgnoreCase(appL.getApplication())) {
                                    out.print("selected=\"selected\"");
                                }
                                    %> value="<%=appL.getApplication()%>"> <%=appL.getApplication()%> </option>
                            <%
                                }
                            %></select>
                        <input id="loadTestbutton" style="height:23px" class="button" type="submit" value="Load">
                    </form>
                </div>
         </div>
                        <div style="clear:both"><br></div>
 <p class="dttTitle">Test Coverage Per Application</p> 
 <div id="tableList" style="display:none">
     <input id="systemSelected" value="<%=MySystem%>" style="display:none">
        <%    
            if (filterApp){
                appList=new ArrayList();
                appList.add(applicationService.findApplicationByKey(appSel));
            }                    
                                
            for (Application applicationL : appList){
        %>
        <br>
        <p class="dttTitle" style="font-size:12px"><%=applicationL.getApplication()%></p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="testTable<%=applicationL.getApplication()%>" class="display">
                <thead>
                    <tr>
                        <th>Test</th>
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
                    <script type="text/javascript">

            $(document).ready(function() {
                var mySys = getSys();
                var myApp = '<%=applicationL.getApplication()%>';
                var tableName = '#testTable'+myApp.replace('.','\\.');
                var oTable = $(tableName).dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": false,
                    "sAjaxSource": "FindTestImplementationStatusPerApplication?MySystem="+mySys+"&Application="+myApp,
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Test", "sWidth": "40%"},
                        {"sName": "Total", "sWidth": "10%"}
                        <%
                                    
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
        <%  } %>
 </div>
 <script>
     $("#tableList").show();
 </script>
        <br><% out.print(display_footer(DatePageStart));%>
    <script type="text/javascript">
            (document).ready($("#Application").multiselect({
                multiple: false,
                header: "Select an option",
                noneSelectedText: "Select an Option",
                selectedList: 1
            }));
        </script>   
    </body>
</html>
