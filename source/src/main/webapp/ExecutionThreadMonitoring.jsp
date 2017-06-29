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
<%-- 
    Document   : ExecutionThreadMonitoring
    Created on : 3 mars 2015, 12:42:00
    Author     : bcivel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <title>ExecutionThreadMonitoring</title>
        <script>
            $(document).ready(function () {
                $.get('ReadCerberusDetailInformation', function (data) {
                    $("#sizeOfQueue").html(data.size_queue);
                    $("#QueueInExecution").html(data.queue_in_execution);
                    $("#NumberOfThread").html(data.number_of_thread);
                    $("#SimultaneousExecution").html(data.simultaneous_execution);
                    $("#SimultaneousSession").html(data.simultaneous_session);
                    $.each(data.active_users, function (a, v) {
                        $("#ActiveUsers").append("<li>" + v + "</li>");
                    });
                    $.each(data.simultaneous_execution_list, function (a, v) {
                        function getParameter(param, sys, forceReload) {
                            var result;
                            var cacheEntryName = "PARAMETER_" + param;
                            if (forceReload) {
                                sessionStorage.removeItem(cacheEntryName);
                            }
                            var system = sys != undefined ? "&system=" + sys : "";
                            var parameter = JSON.parse(sessionStorage.getItem(cacheEntryName));
                            if (parameter === null) {
                                $.ajax({
                                    url: "ReadParameter?param=" + param + system,
                                    data: {},
                                    async: false,
                                    success: function (data) {
                                        sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable))
                                        result = data.contentTable;
                                    }
                                });
                            } else {
                                result = parameter;
                            }
                            return result;
                        }

                        $("#ExecutionList").append("<li>[<a href='./ExecutionDetail2.jsp?executionId=" + v.id + "'>" + v.id + "</a>] : " + v.test + " " + v.testcase + "</li>");
                    });

                });

            });
        </script>
        <script>
            function resetThreadPool() {
                $.get('ExecutionThreadReset', function (data) {
                    alert('Thread Pool Cleaned');
                });
            }
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <h3>Execution Monitoring</h3>
        <h4>Thread Execution</h4>
        <p>Size Of Pending Execution In Queue : </p><p id="sizeOfQueue"></p>
        <br>
        <p>Number of Workers In Execution : </p><div style="float:left" id="QueueInExecution"></div><div style="float:left">/</div><div style="float:left" id="NumberOfThread"></div>
        <br>
        <input type="button" value="Reset Queue" onclick="resetThreadPool()">
        <br>
        <br>

        <h4>Execution</h4>
        <p>Number of Actual Simultaneous Execution : </p><p id="SimultaneousExecution"></p>
        <br>
        <p>Execution List : </p>
        <br>
        <ul id="ExecutionList"></ul>
        <br><br>
        <h3>Session Monitoring</h3>
        <p>Number of HTTP Session opened : </p><p id="SimultaneousSession"></p>
        <br>
        <p>List of Active Users : </p>
        <br>
        <ul id="ActiveUsers"></ul>
        <br>
    </body>
</html>
