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
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="include/function.jsp"%>
<%
    String test = request.getParameter("test");
    String testcase = request.getParameter("testcase");
    String property = request.getParameter("property");
    String database = request.getParameter("db");
    String type = request.getParameter("type");

    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    String myLang = ParameterParserUtil.parseStringParam(request.getParameter("MyLang"), "en");

    if(test != null && !"".equals(test.trim()) 
        && testcase != null && !"".equals(testcase.trim())) {
%>
        <form action="">
            <input type="hidden" id="test" name="test" value="<%=test%>">
            <input type="hidden" id="testCase" name="testCase" value="<%=testcase%>">
            <input type="hidden" id="type" name="type" value="<%=type%>">
        <%if(!"executeSoapFromLib".equals(type) && !"getFromTestData".equals(type)) {%>
            <input type="hidden" id="db" name="db" value="<%=database%>">
            <label for="country"><% out.print(docService.findLabelHTML("invariant", "Country", "Country", myLang));%></label>
            <select id="country" name="country" onchange="getEnvironmentSelectBox()"></select>
            &nbsp;&nbsp;
            <label for="environment"><% out.print(docService.findLabelHTML( "invariant", "Environment", "Environment", myLang));%></label>
            <select id="environment" name="environment"></select>
            <br>
            <script>
                $(document).ready(function() {
                    getCountrySelectBox();
                });

            </script>
        <%}%>
            <textarea rows="5" cols="80" id="property" name="property"><%=property%></textarea>
            <br>
            <button class="btn btn-default" type="button" onclick="calculateProperty();" name="calculate" id="calculate" >Calculate Property</button>
            <div id="result"></div>
            <br>
            <div id="propdesc"></div>

<% } %>
</form>