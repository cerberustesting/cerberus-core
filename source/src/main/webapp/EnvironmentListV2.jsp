<%@page import="org.cerberus.crud.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.crud.entity.BuildRevisionInvariant"%>
<%@ page import="org.apache.log4j.Logger" %>
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

<%    final Logger LOG = Logger.getLogger(this.getClass());
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

        <%
            String MySystem = request.getAttribute("MySystem").toString();
            String myLang = request.getAttribute("MyLang").toString();
        %>
        <script type="text/javascript">
            var data;

            $().ready(function () {

                $('#formEnvironment').submit(function (e) {
                    e.preventDefault();

                    $("#system").val($("#MySystem").val());

                    var postData = $(this).serialize();

                    $.post("FindEnvironments?" + postData, function (resp) {
                        data = resp.aaData;

                        var half_length = Math.ceil(data.length / 2);
                        var data1 = data.splice(0, half_length);

                        $("#environment1").dataTable({
                            "aaData": data1,
                            "bJQueryUI": false,
                            "bFilter": false,
                            "bInfo": false,
                            "bSort": false,
                            "bPaginate": false,
                            "bDestroy": true,
                            "bAutoWidth": false,
                            "sAjaxSource": "",
                            "fnInitComplete": function () {
                                var env = $('#environment1');
                                env.find('thead th').css('padding', '0px');
                                env.find('td').css('padding', '0px');
                                env.css({'width': 'auto', 'margin': '0px', 'text-align': 'center'});
                            },
                            "fnCreatedRow": function (nRow, aData) {
                                if (aData[6] === "Y") {
                                    $(nRow).css("background-color", "#f3f6fa");
                                } else {
                                    $(nRow).css("background-color", "white");
                                }
                                return nRow;
                            }, "aoColumnDefs": [
                                {
                                    "aTargets": [0],
                                    "bSearchable": false,
                                    "bVisible": false
                                },
                                {
                                    "aTargets": [8],
                                    "mRender": function (data) {
                                        return "<a href='" + data + "' target='_blank'>select</a>";
                                    }
                                }
                            ]
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
                            },
                            "fnCreatedRow": function (nRow, aData) {
                                if (aData[6] === "Y") {
                                    $(nRow).css("background-color", "#f3f6fa");
                                } else {
                                    $(nRow).css("background-color", "white");
                                }
                                return nRow;
                            }, "aoColumnDefs": [
                                {
                                    "aTargets": [0],
                                    "bSearchable": false,
                                    "bVisible": false
                                },
                                {
                                    "aTargets": [8],
                                    "mRender": function (data) {
                                        return "<a href='" + data + "' target='_blank'>select</a>";
                                    }
                                }
                            ]
                        });
                    });
                });

                $('#submit').click();
            });
        </script>

        <%
            try {
        %>

        <div>
            <form id="formEnvironment">
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("invariant", "Country", "country", myLang)%>
                    <%=ComboInvariant(appContext, "Country", "", "Country", "", "COUNTRY", "", "", "ALL")%>
                </div>
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("invariant", "Environment", "environment", myLang)%>
                    <select id="environment" name="environment">
                        <option value="ALL">-- ALL --</option>
                        <%
                            List<String> envGroupList = new ArrayList<String>();
                            
                            AnswerList answer = invariantService.readByIdname("ENVIRONMENT");
                            List<Invariant> envList = (List<Invariant>)answer.getDataList(); 
                            
                            for (Invariant inv : envList) {
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
                    <%=docService.findLabelHTML("invariant", "environmentgp", "envgp", myLang)%>
                    <select id="envGroup" name="envGroup">
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
                    <%=docService.findLabelHTML("testcaseexecution", "Build", "build", myLang)%>
                    <select id="build" name="build">
                        <option value="ALL">-- ALL --</option>
                        <%
                            for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel("VCCRM", 1)) {
                        %>
                        <option value="<%=bri.getVersionName()%>"><%=bri.getVersionName()%></option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("testcaseexecution", "Revision", "revision", myLang)%>
                    <select id="revision" name="revision">
                        <option value="ALL">-- ALL --</option>
                        <%
                            for (BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel("VCCRM", 2)) {
                        %>
                        <option value="<%=bri.getVersionName()%>"><%=bri.getVersionName()%></option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("countryenvparam", "chain", "chain", myLang)%>
                    <input id="chain" name="chain" type="text" value="ALL" size="10"/>
                </div>
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("countryenvparam", "active", "active", myLang)%>
                    <%=ComboInvariant(appContext, "EnvActive", "", "EnvActive", "", "ENVACTIVE", "", "", "ALL")%>
                </div>
                <div style="float: left; padding-left: 5px">
                    <%=docService.findLabelHTML("countryenvparam", "Type", "type", myLang)%>
                    <%=ComboInvariant(appContext, "EnvType", "", "EnvType", "", "ENVTYPE", "", "", "ALL")%>
                </div>
                <div style="float: left; padding-left: 5px">
                    <input id="system" name="system" type="hidden" value="" />
                    <input id="submit" type="submit" value="Apply"/>
                </div>
            </form>
        </div>

        <div style="clear: both; float: left;padding-top: 25px; padding-left: 50px; padding-right: 100px">
            <table id="environment1">
                <thead>
                    <tr id="header">
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
            } catch (CerberusException ex) {
                LOG.error("Cerberus exception : " + ex.toString());
                out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
                out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
            }
        %>
    </body>
</html>