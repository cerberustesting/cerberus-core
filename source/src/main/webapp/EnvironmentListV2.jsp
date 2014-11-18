<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.cerberus.entity.*" %>
<%@ page import="org.cerberus.service.*" %>
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
<%@ include file="include/function.jsp" %>

<%
    final Logger LOG = Logger.getLogger(this.getClass());
    IInvariantService invariantService = appContext.getBean(IInvariantService.class);
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Environment Management</title>

    <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="css/crb_style.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
</head>
<body>
<%@ include file="include/header.jsp" %>

<script type="text/javascript">
    $().ready(function() {
        var data = [
            ["VCCRM","UA","QA","2014S1","R67","","N","COMPARISON","<a href='/Environment.jsp?system=VCCRM&country=UA&env=QA' target='_blank'>select</a>"],
            ["VCCRM","UA","UAT","2014S2","R24","","N","COMPARISON","<a href='/Environment.jsp?system=VCCRM&country=UA&env=UAT' target='_blank'>select</a>"],
            ["VCCRM","UA","PROD","2014S1","R72","","N","COMPARISON","<a href='/Environment.jsp?system=VCCRM&country=UA&env=PROD' target='_blank'>select</a>"],
            ["VCCRM","BE","DEV","2013S2","R27","","Y","STD","<a href='/Environment.jsp?system=VCCRM&country=BE&env=DEV' target='_blank'>select</a>"],
            ["VCCRM","BE","QA","VCCRM14","R09","","Y","STD","<a href='/Environment.jsp?system=VCCRM&country=BE&env=QA' target='_blank'>select</a>"]
        ];
        var half_length = Math.ceil(data.length / 2);
        var data1 = data.splice(0,half_length);

        $("#environment1").dataTable({
            "aaData": data1,
            "bJQueryUI": false,
            "bFilter": false,
            "bInfo": false,
            "bSort": false,
            "bPaginate": false,
            "bDestroy": true,
            "bAutoWidth": false,
            "fnInitComplete": function () {
                var env = $('#environment1');
                env.find('thead th').css('padding', '0px');
                env.find('td').css('padding', '0px');
                env.css({'width': 'auto', 'margin': '0px', 'text-align': 'center'});
            }
        });
        $("#environment2").dataTable({
            "aaData": data,
            "bJQueryUI": false,
            "bFilter": false,
            "bInfo": false,
            "bSort": false,
            "bPaginate": false,
            "bDestroy": true,
            "bAutoWidth": false,
            "fnInitComplete": function () {
                var env = $('#environment2');
                env.find('thead th').css('padding', '0px');
                env.find('td').css('padding', '0px');
                env.css({'width': 'auto', 'margin': '0px', 'text-align': 'center'});
            }
        });
    });
</script>

<%
    try{
%>

<div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("invariant", "Country", "country")%>
        <%=ComboInvariant(appContext, "Countries", "", "Countries", "", "COUNTRY", "", "", "ALL")%>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("invariant", "Environment", "environment")%>
        <select>
            <option value="ALL">-- ALL --</option>
        <%
            List<String> envGroupList = new ArrayList<String>();
            for (Invariant inv : invariantService.findListOfInvariantById("ENVIRONMENT")) {
        %>
            <option value="<%=inv.getValue()%>"><%=inv.getValue()%></option>
        <%
                if (!envGroupList.contains(inv.getGp1())) {
                    envGroupList.add(inv.getGp1());
                }
            }
        %>
        </select>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("invariant", "environmentgp", "envgp")%>
        <select>
            <option value="ALL">-- ALL --</option>
        <%
            for (String envGroup : envGroupList) {
        %>
            <option value="<%=envGroup%>"><%=envGroup%></option>
        <%
            }
        %>
        </select>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("testcaseexecution", "Build", "build")%>
        <select>
            <option value="ALL">-- ALL --</option>
        <%
            for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel("VCCRM", 1)) {
        %>
            <option value="<%=bri.getSeq()%>"><%=bri.getVersionName()%></option>
        <%
            }
        %>
        </select>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("testcaseexecution", "Revision", "revision")%>
        <select>
            <option value="ALL">-- ALL --</option>
        <%
            for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel("VCCRM", 2)) {
        %>
            <option value="<%=bri.getSeq()%>"><%=bri.getVersionName()%></option>
        <%
            }
        %>
        </select>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("countryenvparam", "chain", "chain")%>
        <input type="text" value="ALL" size="10"/>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("countryenvparam", "active", "active")%>
        <%=ComboInvariant(appContext, "EnvActive", "", "EnvActive", "", "ENVACTIVE", "", "", "ALL")%>
    </div>
    <div style="float: left; padding-left: 5px">
        <%=docService.findLabelHTML("countryenvparam", "Type", "type")%>
        <%=ComboInvariant(appContext, "EnvType", "", "EnvType", "", "ENVTYPE", "", "", "ALL")%>
    </div>
    <div style="float: left; padding-left: 5px">
        <input type="button" value="Apply"/>
    </div>
</div>

<div style="clear: both; float: left;padding-top: 25px; padding-left: 50px; padding-right: 100px">
    <table id="environment1">
        <thead>
            <tr>
                <th>System</th>
                <th>Country</th>
                <th>Environment</th>
                <th>Build</th>
                <th>Revision</th>
                <th>Chain</th>
                <th>Active</th>
                <th>Type</th>
                <th></th>
            </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
<div style="float: left; padding-top: 25px; padding-left: 100px; padding-right: 50px">
    <table id="environment2">
        <thead>
        <tr>
            <th>System</th>
            <th>Country</th>
            <th>Environment</th>
            <th>Build</th>
            <th>Revision</th>
            <th>Chain</th>
            <th>Active</th>
            <th>Type</th>
            <th></th>
        </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
<%
    } catch (CerberusException ex){
        LOG.error("Cerberus exception : " + ex.toString());
        out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
        out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
    }
%>
</body>
</html>