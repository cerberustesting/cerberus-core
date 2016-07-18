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
<%@page import="java.util.Date"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Network Traffic Detail</title>
        <%@ include file="include/dependenciesInclusions_old.html" %>
    </head>
    <body>
        <%
            String id = request.getParameter("id");
        %>
        <input style="display:none" id="idvalue" name="idvalue" value="<%=id%>">
        <script type="text/javascript">      
            var id = document.getElementById("idvalue").value;
          
            $(document).ready(function(){
                $('#nonConformityList').dataTable({
                    "bServerSide": true,
                    "sAjaxSource": "TCEwwwDetail?id="+id,
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true, 
                    "aoColumns": [
                        {"bVisible": false, "sName": "ID", "sWidth": "20px"},
                        {"bVisible": false, "sName": "EXECID", "sWidth": "50px"},
                        {"sName": "START", "sWidth": "100px"},
                        {"sName": "URL", "sWidth": "400px"},
                        {"sName": "END", "sWidth": "100px"},
                        {"sName": "EXT", "sWidth": "20px"},
                        {"sName": "StatusCode", "sWidth": "50px"},
                        {"sName": "Method", "sWidth": "30px"},
                        {"sName": "Bytes", "sWidth": "50px"},
                        {"sName": "TimeInMillis", "sWidth": "50px"},
                        {"sName": "ReqHeader_Host", "sWidth": "50px"},
                        {"sName": "ResHeader_ContentType", "sWidth": "50px"}
                    ]
                }
            )
            });
  
        </script>

        <div style="width: 80%; padding: 25px; font: 70% sans-serif">
            <table id="nonConformityList" class="display">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>EXECID</th>
                        <th>START</th>
                        <th>URL</th>
                        <th>END</th>
                        <th>EXT</th>
                        <th>StatusCode</th>
                        <th>Method</th>
                        <th>Bytes</th>
                        <th>TimeInMillis</th>
                        <th>ReqHeader_Host</th>
                        <th>ResHeader_ContentType</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br>          
    </body>
</html>