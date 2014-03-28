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
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
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

    <%@ include file="include/function.jsp"%>

        <%
     //Database Connection

                    Connection conn = db.connect();

    try {
            String Lign;
            Lign = request.getParameter("Lign");
            String propertyType;
            propertyType = "type"+Lign;

        String Type;
            Type = request.getParameter("Type");
            //Enter when type is selected
        if (request.getParameter("Type") != null) {


            //List of SQL script when type is selected
            Statement stmtQueryVal = conn.createStatement();
            String sqVal = "SELECT Name, Script, Description FROM sqllibrary where Type = '"+ Type +"'";
            ResultSet qVal = stmtQueryVal.executeQuery(sqVal);



                    %>

    <h3><a href="#" onclick="loadSqlLibraryPopin('<%=Lign%>');">Root</a> / <%=Type %></h3>
    <table>
            <% 
            if (qVal.first()) 
            {
                do {
                    String name = qVal.getString("Name");
                    String script = qVal.getString("Script").replace("'", "\\'");
                %>
            <tr id="header">
            <td rowspan="2"><%=qVal.getString("Name")%><input type="button" value="Select SQL" onclick="reporter('<%=name%>', '<%=Lign%>', '<%=propertyType%>');"></td>
            <td style="font-style: normal"><%=qVal.getString("Description")%></td></tr>
            <tr><td><textarea rows="2" style="width: 700px"><%=qVal.getString("Script")%></textarea>
                <input type="button" value="Import SQL" onclick="reporterScript('<%=script%>', '<%=Lign%>', '<%=propertyType%>');">
                </td>
            </tr>
            
            <% 
                }
                while (qVal.next());
                               }
            %>
        </table>

        <%
} else {
            Type = new String("empty");
                   //List of SQL Type
            Statement stmtQuery1 = conn.createStatement();
            String sq1 = "SELECT distinct Type FROM sqllibrary ";
            ResultSet q1 = stmtQuery1.executeQuery(sq1);
            
        %>
    <h3> SQL Library !</h3>
            <% 
            if (q1.first()) {
                do {
                    %><a href="#" class="sqlLib" onclick="loadSqlLibraryPopin('<%=Lign%>&Type=<%=q1.getString("Type")%>');"><%=q1.getString("Type")%></a><%
                }
                while (q1.next());
            }

}
                
            } catch (Exception e) {
                out.println("<br> error message : " + e.getMessage() + " "
                        + e.toString() + "<br>");
            } finally{
                try {
                    conn.close();
                } catch (SQLException e) {

                }
            }
        %>

        <br>

