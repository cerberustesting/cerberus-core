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

            function appendValueInInvariantSelect(selectId, invariantId) {

                $.get("ListCampaignParameter", "invariant=" + invariantId, function(data) {
                    var index, parameter;
                    var select = $(selectId);
                    select.empty();
                    for (index = 0; index < data.ParameterValues.length; index++) {
                        parameter = data.ParameterValues[index];
                        //console.log(parameter);
                        var option = $("<option></option>").attr('value', parameter[1])
                                .text(parameter[3] + " - " + parameter[1])
                                .attr("title", parameter[3] + " - " + parameter[1]);

                        if ($.isParamInURL(select.attr("name"), parameter[1])) {
                            option.attr("selected", "selected");
                        }

                        select.append(option);
                    }
                });
            }

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

                appendValueInInvariantSelect("#country", "COUNTRY");
                appendValueInInvariantSelect("#environment", "ENVIRONMENT");
                appendValueInInvariantSelect("#browser", "BROWSER");

                jQuery.ajax('./GetTagExecutions?withUUID').done(function(data) {
                    var index;
                    for (index = 0; index < data.tags.length; index++) {
                        var option = $('<option></option>').attr("value", data.tags[index]).text(data.tags[index]);

                        if ($.isParamInURL("Tag", data.tags[index])) {
                            option.attr("selected", "selected");
                        }

                        $('#selectTag').append(option);
                    }
                });

                jQuery.ajax('./GetCampaign?action=findAllCampaign&withoutLink=true').done(function(data) {
                    var index;
                    for (index = 0; index < data.Campaigns.length; index++) {
                        var option = $('<option></option>').attr("value", data.Campaigns[index][1])
                                .text(data.Campaigns[index][0] + " - " + data.Campaigns[index][1] + " - " + data.Campaigns[index][2]);

                        if ($.isParamInURL("CampaignName", data.Campaigns[index][1])) {
                            option.attr("selected", "selected");
                        }

                        $('#selectCampaign').append(option);
                    }
                });



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

                $.get("./CampaignExecutionReport", "<%=query.toString()%>", function(report) {
                    // Get context with jQuery - using jQuery's .get() method.

                    for (var index = 0; index < report.length; index++) {
            <%
                if (!onlyFunction) {
            %>
                        report[index].Function = (report[index].Function ? report[index].Function : report[index].Test);
            <%
                }
            %>
                        addTestCaseToStatusTabs(report[index]);
                    }

                    $("table.needToBeSort").each(function() {
                        sorttable.makeSortable(this);
                    });

                });
            });
        </script>
        <style>

            html, body { 
                height: 100%;
                padding:0; margin:0;
                background: white;
            }


            .ID {
                width: 5%;
            }
            .Test {
                width: 10%;
            }
            .TestCase {
                width: 5%;
            }
            .ShortDescription {
                width: 32%;
            }
            .Function {
                width: 10%;
            }
            .Control {
                width: 3%;
            }
            .Status {
                width: 5%;
            }
            .Application {
                width: 5%;
            }
            .BugID {
                width: 5%;
            }
            .Comment {
                width: 10%;
            }
            .Start {
                width: 10%;
            }
            .End {
                width: 10%;
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
                            <label style="width:20%;display:block" for="selectCampaign">Campaign: </label><select style="float:left;width:79%;display:block" name="CampaignName" id="selectCampaign"></select><br>
                            <label style="width:20%;display:block" for="selectTag">Tag: </label><select style="width:79%;display:block" name="Tag" id="selectTag"></select><br>
                            <label style="width:20%;display:block" for="environment">Environment: </label><select style="width:79%;display:block" multiple="true" name="Environment" id="environment"></select><br>
                            <label style="width:20%;display:block" for="country">Country: </label><select style="width:79%;display:block" multiple="true" name="Country" id="country"></select><br>
                            <label style="width:20%;display:block;" for="browser">Browser: </label><select style="width:79%;display:block" multiple="true" name="Browser" id="browser"></select><br>
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
                <div class="arrondTable fullSize" style="width: 46%; float:left;height:400px;">
                    <div style="width: 99%;float:left">
                        <h1 style="width: 99%; text-align: center">Report by Function</h1>
                    </div>
                    <canvas style="height:380px;" id="functionBar"></canvas>
                </div>
                <div class="arrondTable fullSize" style="width: 46%; float:left;height:400px;margin-left:2%">
                    <div style="width: 99%;float:left">
                        <h1 style="width: 99%; text-align: center">Radar by function</h1>
                    </div>
                    <canvas style="height:400px;" id="functionTest"></canvas>
                </div>
            </div>
            <br>
            <div style="clear:both; width:92%; margin-left:3%">
                <h1><a name="StatusNE" class="StatusNE">Not Executed</a></h1>
                <table id="StatusNE" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>            
                <h1><a name="StatusKO" class="StatusKO">Status KO</a></h1>
                <table id="StatusKO" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <h1><a name="StatusFA" class="StatusFA">Status FA</a></h1>
                <table id="StatusFA" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <h1><a name="StatusNA" class="StatusNA">Status NA</a></h1>
                <table id="StatusNA" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <h1><a name="StatusPE" class="StatusPE">Status PE</a></h1>
                <table id="StatusPE" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <h1><a name="StatusOK" class="StatusOK">Status OK</a></h1>
                <table id="StatusOK" class="arrondTable fullSize needToBeSort">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Function</th>
                            <th>Test</th>
                            <th>TestCase</th>
                            <th>Description</th>
                            <th>Control</th>
                            <th>Status</th>
                            <th>Application</th>
                            <th>BugID</th>
                            <th class="wrapAll">Comment</th>
                            <th>Start</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>