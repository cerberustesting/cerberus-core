<%--

    Cerberus Copyright (C) 2013 - 2017 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.cerberus.crud.service.ISqlLibraryService" %>
<%@ page import="org.cerberus.crud.entity.SqlLibrary" %>

<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ include file="include/function.jsp" %>

<%
    ISqlLibraryService sqlLibraryService = appContext.getBean(ISqlLibraryService.class);
    try {
        String valueid = request.getParameter("valueid");
        String typeid = request.getParameter("typeid");

        String Type = request.getParameter("Type");
        //Enter when type is selected
        if (request.getParameter("Type") != null) {
%>

<h3><a href="#" onclick="loadSqlLibraryPopin('<%=valueid%>', '<%=typeid%>');">Root</a> / <%=Type %>
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
                                        onclick="setSQLValue('<%=name%>','executeSqlFromLib', '<%=valueid%>', '<%=typeid%>' );"></td>
        <td style="font-style: normal"><%=sqlLibrary.getDescription()%>
        </td>
    </tr>
    <tr>
        <td><textarea rows="2" class="width600" style="max-width: 600px"><%=script%>
        </textarea>
            <input type="button" value="Import SQL"
                   onclick="setSQLValue('<%=script%>','executeSql', '<%=valueid%>', '<%=typeid%>' );">
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
    List<String> list = sqlLibraryService.findDistinctTypeOfSqlLibrary();
        for (int i = 0 ; i< list.size(); i++) {
            String type = list.get(i);
            //gets de first
%>
            <div class="center"> 
                <a href="#" class="sqlLib width200"
                   onclick="loadSqlLibraryPopin('<%=valueid%>', '<%=typeid%>&Type=<%=type%>');"><%=type%>
                </a>
                <% 
                    //gets the second
                    if( ++i < list.size()){
                        type = list.get(i);%>
                        <a href="#" class="sqlLib width200"
                           onclick="loadSqlLibraryPopin('<%=valueid%>', '<%=typeid%>&Type=<%=type%>');"><%=type%>
                        </a>
                   <% } %>
               <% 
                    //gets the third
                    if( ++i < list.size()){
                        type = list.get(i);
                    %>
                    
                    <a href="#" class="sqlLib width200"
                       onclick="loadSqlLibraryPopin('<%=valueid%>', '<%=typeid%>&Type=<%=type%>');"><%=type%>
                    </a>
               <% } %>
            </div>
<%
            }

        }

    } catch (Exception e) {
        out.println("<br> error message : " + e.getMessage() + " "
                + e.toString() + "<br>");
    }
%>

<br>

