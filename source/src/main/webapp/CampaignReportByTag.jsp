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
        <script type="text/javascript" src="js/Chartjs/Chart.js"></script>
        <script type="text/javascript" src="js/Chartjs/extensions/Chart.ColoredBar.js"></script>
        <script type="text/javascript" src="js/Chartjs/extensions/Chart.HorizontalBar.js"></script>
        <script type="text/javascript" src="js/campaignReport.js"></script>

        <script>
            Chart.defaults.global.responsive = true;
            //Chart.defaults.global.showTooltips = false;
                
            <%
                String campaignName = request.getParameter("campaignName");
                String tag = request.getParameter("tag");
                String environment = request.getParameter("Environment");
            %>
            var executionStatus,pieExecutionStatus;

            $(document).ready(function(){
                
                $.get("./CampaignExecutionReport","campaignName=<%=campaignName%>&Environment=<%=environment%>&tag=<%=tag%>", function(report) {
                    // Get context with jQuery - using jQuery's .get() method.
                    var ctx = [];
                    ctx[0] = $("canvas.executionStatus").get(0).getContext("2d");
                    ctx[1] = $("#myDonut").get(0).getContext("2d");
                    ctx[2] = $("#functionBar").get(0).getContext("2d");

                   //console.log(data.labels);

                    var controlStatus,testCaseFunction;
                    var testCaseTotal = 0;
                    var functionFound = false;
                    for(var index=0; index<report.length; index++) {
                        testCaseTotal++;
                        controlStatus = report[index].ControlStatus;
                        addTestCaseToStatusTabs(report[index]);
                        testCaseFunction = report[index].Function || report[index].Test;
                        for(var label=0; label<data.labels.length; label++) {
                            if(controlStatus == data.labels[label]) {
                               data.datasets[0].data[label] = parseInt(data.datasets[0].data[label])+1;
                            }
                        }
                        for(var donut=0; donut<dataDonut.length; donut++) {
                            if(controlStatus == dataDonut[donut].label) {
                               dataDonut[donut].value = parseInt(dataDonut[donut].value)+1;
                            }
                        }

                        functionFound = false;
                        for(var label=0; label<dataFunction.labels.length; label++) {
                            if(testCaseFunction == dataFunction.labels[label]) {
                               dataFunction.datasets[0].data[label] = parseInt(dataFunction.datasets[0].data[label])+1;
                               //console.log(dataFunction.labels[label]+"="+dataFunction.datasets[0].data[label]);
                               functionFound = true;
                            }
                        }
                        if(!functionFound) {
                            var lengthLabel = dataFunction.labels.length;
                            dataFunction.labels[lengthLabel] = testCaseFunction;
                            dataFunction.datasets[0].data[lengthLabel] = 1;
                        }

                    }

                    $("div.executionStatus").empty().append("<table width='100%'><thead><tr><th colspan=2>Execution status</th></tr></thead><tbody><tr></tr></tbody></table>");
                    for(var index=0; index<data.labels.length; index++) {
                        $("div.executionStatus table tbody").append(
                                $("<tr></tr>").append(
                                    $("<th></th>").text(data.labels[index]))
                                    .append($("<td></td>").text(data.datasets[0].data[index]))
                                );
                        //$("div.executionStatus table tbody tr").append($("<td></td>").text(data.datasets[0].data[index]));
                    }
                    $("div.executionStatus table tbody").append(
                        $("<tr></tr>").append(
                            $("<th>Total</th>"))
                            .append($("<td></td>").text(testCaseTotal))
                    );

                    executionStatus = new Chart(ctx[0]).BarColors(data,config);
                    pieExecutionStatus = new Chart(ctx[1]).Pie(dataDonut);

                    $("#myDonut").on('click',function(evt){
                        var activePoints = pieExecutionStatus.getSegmentsAtEvent(evt);

                        var anchor = $('a[name="Status'+activePoints[0].label+'"]');
                        $('html').animate({  
                            scrollTop:anchor.offset().top  
                        }, 'slow');  

                        return false;  

                        // => activePoints is an array of segments on the canvas that are at the same position as the click event.
                    });

                    $("canvas.executionStatus").on('click',function(evt){
                        var activePoints = executionStatus.getBarsAtEvent(evt);

                        var anchor = $('a[name="Status'+activePoints[0].label+'"]');
                        $('html').animate({  
                            scrollTop:anchor.offset().top  
                        }, 'slow');  

                        return false;  

                        // => activePoints is an array of segments on the canvas that are at the same position as the click event.
                    });

                    // And for a doughnut chart
                    $("div.executionStatusLegend").html(pieExecutionStatus.generateLegend());

                    //new Chart(ctx[2]).HorizontalBar(dataFunction);
                    new Chart(ctx[2]).Radar(dataFunction);

                });
            });
        </script>
        <style>
            
            html, body { 
                    height: 100%;
                    padding:0; margin:0;
                    background: white;
            }

            table {
                margin-bottom: 15px;
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

            a.StatusPE {
                color: #555555;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>


        <div id="main">
            <table>
                <tr>
                    <td>
                        <div class="executionStatus"></div>
                    </td>
                    <td><div class="executionStatusLegend"></div></td>
                </tr>
                <tr>
                    <td>
                        <canvas class="executionStatus" width="400%" height="400%"></canvas>
                    </td>
                    <td>
                        <canvas id="myDonut" width="400%" height="400%"></canvas>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <canvas id="functionBar" width="800%" height="800%"></canvas>
                    </td>
                </tr>
            
            </table>
            <h1><a name="StatusKO" class="StatusKO">Status KO</a></h1>
            <table id="StatusKO" class="arrondTable fullSize">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Control</th>
                        <th>Status</th>
                        <th>TestBattery</th>
                        <th>BugID</th>
                        <th>Comment</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <h1><a name="StatusFA" class="StatusFA">Status FA</a></h1>
            <table id="StatusFA" class="arrondTable fullSize">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Control</th>
                        <th>Status</th>
                        <th>TestBattery</th>
                        <th>BugID</th>
                        <th>Comment</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <h1><a name="StatusNA" class="StatusNA">Status NA</a></h1>
            <table id="StatusNA" class="arrondTable fullSize">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Control</th>
                        <th>Status</th>
                        <th>TestBattery</th>
                        <th>BugID</th>
                        <th>Comment</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <h1><a name="StatusPE" class="StatusPE">Status PE</a></h1>
            <table id="StatusPE" class="arrondTable fullSize">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Control</th>
                        <th>Status</th>
                        <th>TestBattery</th>
                        <th>BugID</th>
                        <th>Comment</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <h1><a name="StatusOK" class="StatusOK">Status OK</a></h1>
            <table id="StatusOK" class="arrondTable fullSize">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Control</th>
                        <th>Status</th>
                        <th>TestBattery</th>
                        <th>BugID</th>
                        <th>Comment</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </body>
</html>