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
<%
    String campaignName = request.getParameter("CampaignName");
    String tag = request.getParameter("Tag");
    String[] environments = request.getParameterValues("Environment");
    String[] countries = request.getParameterValues("Country");
    String[] browsers = request.getParameterValues("Browser");

    boolean onlyFunction = ("true".equalsIgnoreCase(request.getParameter("OnlyFunction")));

    StringBuffer query = new StringBuffer("CampaignName=").append(campaignName);
    query.append("&Tag=").append(tag);

    if (environments != null && environments.length > 0) {
        for (String environment : environments) {
            query.append("&Environment=").append(environment);
        }
    }

    if (countries != null && countries.length > 0) {
        for (String country : countries) {
            query.append("&Country=").append(country);
        }
    }

    if (browsers != null && browsers.length > 0) {
        for (String browser : browsers) {
            query.append("&Browser=").append(browser);
        }
    }
%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Campaign Reporting</title>
        <link rel="stylesheet" 
              type="text/css" href="css/crb_style.css"
              />
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/sorttable.js"></script>
        <script type="text/javascript" src="js/Chartjs/Chart.js"></script>
        <script type="text/javascript" src="js/Chartjs/extensions/Chart.ColoredBar.js"></script>
        <script type="text/javascript" src="js/Chartjs/extensions/Chart.StackedBar.js"></script>
        <script type="text/javascript" src="js/campaignReport.js"></script>

        <script>
            Chart.defaults.global.responsive = true;
            // Number - Scale label font size in pixels
            Chart.defaults.global.scaleFontSize = '14';

            var executionStatus, pieExecutionStatus;

            $.isParamInURL = function(name, value) {
                var results = new RegExp('[\?&]' + name + '=' + value + '([^&#]*)').exec(window.location.href);
                if (results == null) {
                    return false;
                }
                else {
                    return true;
                }
            };

            $(document).ready(function() {
                jQuery.ajax({url: './GetTagExecutions?withUUID', async: false}).done(function(data) {
                    var index;
                    $('#selectTag').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Tag");
                    for (index = 0; index < data.tags.length; index++) {
                        var option = $('<option></option>').attr("value", data.tags[index]).text(data.tags[index]);

                        if ($.isParamInURL("Tag", data.tags[index])) {
                            option.attr("selected", "selected");
                        }

                        $('#selectTag').append(option);
                    }
                });

                if ($("#selectTag").val() !== null && $("#selectTag").val() !== "") {
                    loadData();

                }
            });

            function loadData() {

                createGraphFromAjaxToElement("./CampaignExecutionReportByFunction?<%=query.toString()%>", "#functionTest", null);
                createGraphFromAjaxToElement("./CampaignExecutionStatusBarGraphByFunction?<%=query.toString()%>", "#functionBar", null);

                jQuery.ajax("./CampaignExecutionGraphByStatus?<%=query.toString()%>").done(function(data) {
                    // function used to generate the Pie graph about status number
                    var pie = createGraphFromDataToElement(data, "#myDonut", null);

                    $("#myDonut").on('click', function(evt) {
                        var activePoints = pie.getSegmentsAtEvent(evt);

                        var anchor = $('a[name="Status' + activePoints[0].label + '"]');
                        $('html').animate({
                            scrollTop: anchor.offset().top
                        }, 'slow');

                        return false;
                    });


                    // code used to create the execution status table.
                    $("div.executionStatus").empty().append("<table  class='arrondTable fullSize'><thead><tr><th>Execution status</th><th>TestCase Number</th></tr></thead><tbody></tbody></table>");
                    var total = 0;
                    // create each line of the table
                    for (var index = 0; index < data.labels.length; index++) {
                        $("div.executionStatus table tbody").append(
                                $("<tr></tr>").append(
                                $("<td></td>").text(data.axis[index].label))
                                .append($("<td></td>").text(data.axis[index].value))
                                );
                        // increase the total execution
                        total = total + data.axis[index].value;
                    }
                    // add a line for the total
                    $("div.executionStatus table tbody").append(
                            $("<tr></tr>").append(
                            $("<th>Total</th>"))
                            .append($("<th></th>").text(total))
                            );
                });

                $.get("./CampaignExecutionReport", "<%=query.toString()%>", function(dataList) {
                    // Get context with jQuery - using jQuery's .get() method.


                    displayFilter2(dataList.Columns);


                    //First load header
                    $("#detailedTableDiv").empty().append(buildDetailedTableHeader(dataList.Columns));
                    $("#searchColumns").attr('onkeyup', 'filterLines(this.value)');
                    loadTableContent(dataList.Lines, dataList.Columns, dataList.Values);
                    });
            }

                    //            var websocket = new WebSocket("ws://localhost:8080/Cerberus-1.0.2-SNAPSHOT/WebsocketTest");
//        websocket.onmessage = function processMessage(message){
//        console.log(message.data);
//            loadData();
                        //        }

        </script>
        <style>

            html, body { 
                height: 100%;
                padding:0; margin:0;
                background: white;
            }


            .ID {
                width: 5%;
                float:left;
                min-height: 1px;
                display : inline;
            }
            .TestPart{
                width: 50%;
                float : left;
                min-height: 1px;
            }
            .StatusPart{
                width: 50%;
                float : left;
                min-height: 1px;
            }
            .Test {
                font-weight: bold;
                float:left;
                min-height: 1px;
                background-color: #EEE;
            }
            .TestCase {
                font-weight: bold;
                float:left;
                min-height: 1px;
                background-color: #EEE;
            }
            .ShortDescription {
                width: 99%;
                clear:both;
                min-height: 1px;
            }
            .Function {
                width: 99%;
                clear:both;
                display:block;
                min-height: 1px;
            }
            .Control {
                width: 99%;
                clear:both;
                min-height: 1px;
            }
            .Status {
                width: 99%;
                clear:both;
                min-height: 1px;
            }
            .Application {
                width: 99%;
                clear:both;
                min-height: 1px;
            }
            .Country {
                width: 100%;
                float:left;
                min-height: 1px;
            }
            .BugID {
                width: 5%;
                float:left;
                min-height: 1px;
            }
            .Comment {
                width: 99%;
                float:left;
                min-height: 1px;
            }
            .ControlMessage {
                width: 99%;
                float:left;
                min-height: 1px;
            }
            .Start {
                width: 99%;
                float:left;
                min-height: 1px;
            }
            .End {
                width: 10%;
                float:left;
                min-height: 1px;
            }

            table.noBorder td {
                border: none;
            }

            table.fullSize {
                width: 100%;
            }

            a.StatusOK {
                color: #00EE00;
            }

            .StatusOK {
                background-color: #00EE00;
            }

            a.StatusCA {
                color: mistyrose;
            }

            a.StatusKO {
                color: #F7464A;
            }

            a.StatusFA {
                color: #FDB45C;
            }

            a.StatusNA {
                color: #EEEE00;
            }

            a.StatusNE {
                color: #000;
            }

            a.StatusPE {
                color: #2222FF;
            }
            .TableLine{
                background-color: white;
                border-width: thin;
                border-color: gray; 
                border-style:solid;
                width : 100%;
                clear:both; 
            }
            .TableLine:hover{
                background-color: #EEEEEE;

            }

            .indFilter{
                float:left;
                width : 100px;
                height : 30px;
                background-color: lightgray;

            }
            .indFilter input{
                margin-left: auto;
                margin-right: auto;

            }

            table.needToBeSort th:not(.sorttable_sorted):not(.sorttable_sorted_reverse):not(.sorttable_nosort):after { 
                content: " \25B4\25BE" 
            }

            table tr td.title {
                margin: 0 auto;
                text-align: center;
                font-weight: bold;
            }

        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <div id="main">
            <div style="margin-left:3%; display:block;height:200px;" class="title addborder">
                <div class="arrondTable fullSize" style="width: 46%; float:left; height:200px; overflow: auto;">
                    <div style="width: 99%;float:left">
                        <h1 style="width: 99%; text-align: center">Filters</h1>
                    </div>
                    <div style="width: 99%;clear:both; display:block">
                        <form method="get">
                            <label style="width:20%;display:block" for="selectTag">Tag: </label><select style="width:79%;display:block" name="Tag" id="selectTag"></select><br>
                            <br>
                            <button style="float:right" type="submit" value="Load">Load</button>
                        </form>
                    </div>
                </div>
                <div class="arrondTable fullSize" style="width: 46%;float:left; display:block;margin-left:2%; height:200px">
                    <div style="width: 99%;float:left">
                        <h1 style="width: 99%; text-align: center">Report by Status</h1>
                    </div>
                    <div style="width: 99%;clear:both; display:block">
                        <div style="width: 48%;float:left;float:bottom; margin-left:1%" class="executionStatus"></div>
                        <div  style="width: 48%;float:left">
                            <canvas id="myDonut"></canvas>
                        </div>
                    </div>

                </div>
            </div>
            <div style="margin-left:3%; margin-top:20px;clear:both;display:block;height:400px;" class="title addborder">
                <div class="arrondTable fullSize" style="width: 94%; float:left;height:400px;">
                    <div style="width: 99%;float:left">
                        <h1 style="width: 99%; text-align: center">Report by Function</h1>
                    </div>
                    <canvas height="280px" width="1200px" id="functionBar"></canvas>
                </div>
            </div>
            <br>
            <div id="tableFilter" style="margin-left:3%;width:92%;display:inline-block;background-color:lightgray">
            </div>
            <div style="clear:both; width:92%; margin-left:3%">
                <div id="detailedTableDiv" class="">
                </div>
                <div id="detailedTableContentDiv" class="">
                </div>
            </div>
            <script type="text/javascript">
                function loadCookieValues() {
                    var defaultReporting = ["ShortDescription", "Start"];
                    $(document).find(".indFilter input").each(function(i, e) {
                        console.log(e);
                        if ($.urlParam(e.value) !== null) {
                            $(e).attr('checked', true);
                        } else if (getCookie("ReportingExecutionByTag_" + e.value) !== "") {
                            var ckd = getCookie("ReportingExecutionByTag_" + e.value);
                            console.log(ckd);
                            $(e).attr('checked', ckd === 'true' ? true : false);
                console.log(ckd);
                    } else if ($.inArray(e.value, defaultReporting) !== -1) {
                    $(e).attr('checked', true);
                        } else {
                        $(e).attr('checked', false);
                        }
                            console.log(e);
                        showOrHideColumns(e, e.value);
                    });
                }
                ;

                            $.urlParam = function(name) {
                            var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
                    if (results === null) {
                            return null;
                    }
                        else {
                        return results[1] || 0;
                            }
                }

            </script>
            <script>
                            function recordColumnSelection(id) {
                        var expiration_date = new Date();
                        expiration_date.setFullYear(expiration_date.getFullYear() + 1);
                var idVal = $("#filterId_" + id).is(":checked");
                    document.cookie = "ReportingExecutionByTag_" + id + "=" + idVal + ";expires=" + expiration_date.toGMTString();
                }
                    </script>
            <script>
                    function setCookie(cookieName, element) {
                        var name = cookieName + "=";
                var ca = document.cookie.split(';');
                    for (var i = 0; i < ca.length; i++) {
                var c = ca[i].trim();
                    var val = c.split('=')[1];
                    if (c.indexOf(name) === 0) {
                    document.getElementById(element).value = val;
                        }
                    }
                }
            </script>
            <script>
                function getCookie(cname) {
                var name = cname + "=";
                var ca = document.cookie.split(';');
                    for (var i = 0; i < ca.length; i++) {
                    var c = ca[i].trim();
                    if (c.indexOf(name) === 0)
                        return c.substring(name.length, c.length);                     }
                    return "";
                        }
            </script>
    </body>
</html>
