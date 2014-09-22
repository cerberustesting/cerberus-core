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
<%@ page import="org.cerberus.service.ISqlLibraryService" %>
<%@ page import="org.cerberus.entity.SqlLibrary" %>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<script>
    function reporter(script, valueField, propertyType) {
        document.getElementById(valueField).value = script;
        document.getElementById(propertyType).value = "executeSqlFromLib";

        $("#popin").dialog('close');
    }

    function reporterScript(script, valueField, propertyType) {
        document.getElementById(valueField).value = script;
        document.getElementById(propertyType).value = "executeSql";
        $("#popin").dialog('close');
    }

</script>

<%@ include file="include/function.jsp" %>

<%
    ISqlLibraryService sqlLibraryService = appContext.getBean(ISqlLibraryService.class);
    try {
        String Lign = request.getParameter("Lign");
        String propertyType = "type" + Lign;

        String Type = request.getParameter("Type");
        //Enter when type is selected
        if (request.getParameter("Type") != null) {
%>

<h3><a href="#" onclick="loadSqlLibraryPopin('<%=Lign%>');">Root</a> / <%=Type %>
</h3>
<table>
    <%
        List<SqlLibrary> list = sqlLibraryService.findSqlLibraryListByCriteria(0, 1000, "Type", "ASC", Type, "");
        if (!list.isEmpty()) {
            for (SqlLibrary sqlLibrary : list) {

                String name = sqlLibrary.getName();
                String script = sqlLibrary.getScript().replace("'", "\\'");
    %>
    <tr id="header">
        <td rowspan="2"><%=name%><input type="button" value="Select SQL"
                                        onclick="reporter('<%=name%>', '<%=Lign%>', '<%=propertyType%>');"></td>
        <td style="font-style: normal"><%=sqlLibrary.getDescription()%>
        </td>
    </tr>
    <tr>
        <td><textarea rows="2" style="width: 700px"><%=script%>
        </textarea>
            <input type="button" value="Import SQL"
                   onclick="reporterScript('<%=script%>', '<%=Lign%>', '<%=propertyType%>');">
        </td>
    </tr>

    <%
            }
        }
    %>
</table>

<%
} else {
    //List of SQL Type

%>
<h3> SQL Library !</h3>
<%
    for (String type : sqlLibraryService.findDistinctTypeOfSqlLibrary()) {
%>
<a href="#" class="sqlLib"
   onclick="loadSqlLibraryPopin('<%=Lign%>&Type=<%=type%>');"><%=type%>
</a>
<%
            }

        }

    } catch (Exception e) {
        out.println("<br> error message : " + e.getMessage() + " "
                + e.toString() + "<br>");
    }
%>

<br>

