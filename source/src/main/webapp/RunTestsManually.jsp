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
<%@page import="org.cerberus.entity.Robot"%>
<%@page import="org.cerberus.service.IRobotService"%>
<%@page import="java.util.Enumeration"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.impl.ApplicationService"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.util.SqlUtil"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Run Test Case</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <style>
            .manualExecutionIframe {
                width: 100%;
                height: 90%;
                bottom: 0;
                position: absolute;
            }
            
            .action {
                background-color: green;
            }
            
            .control {
                background-color: brown;
            }
            
            .manualExecution {
                padding: 10px;
                color: white;
                font-size: 2em;
                height: 10%;
                width: 100%;
                display: block;
                position: absolute;
                top: 0;
            }
            
            .testCaseValue,
            .testCaseProperty {
                border: 1px dashed;
                color: lightblue;
                font-size: 0.8em;
                font-style: italic;
                margin: 5px;
                padding: 5px;
            }

            
            .executionStatusBlock {
                display: inline-block;
                position: fixed;
                right: 10px;
            }

        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <div id="body" class="manualExecution">
            <span class='testCaseDescription'></span>
            <span class='testCaseProperty'></span>
            <span class='testCaseValue'></span>
            <div class="executionStatusBlock">
                <form>
                    <input type='hidden' id='Test' value='<%=request.getParameter("Test")%>'>
                    <input type='hidden' id='TestCase' value='<%=request.getParameter("TestCase")%>'>
                    <input type='hidden' id='Type' value=''>
                    <input type='hidden' id='Sequence' value=''>
                    <button value="OK" type="button" id="okbutton">OK</button>
                    <span class="separator">/</span>
                    <button value="OK" type="button" id="kobutton">KO</button>
                </form>
            </div>
        </div>
        <iframe id="webSiteExecution" class="manualExecutionIframe" src="http://fr.laredoute.cataloguesolutions.com/"></iframe>
        <script>
            var TestCaseData;
            var steps=0;
            var actions=0;
            var controls=0;
            var currentAction;
            var currentControl = 0;
            $(document).ready(function(){
                $.post("RunTestCaseManually",{Test: $("#Test").val(), TestCase: $("#TestCase").val()},function(data) {
                    TestCaseData = data;

                    steps = 0;
                    currentAction = TestCaseData.Steps[steps].Actions[0][actions];
                    $(".testCaseDescription").text(currentAction.Description);
                    $(".testCaseProperty").text(currentAction.Object);
                    $(".testCaseValue").text(currentAction.Property);
                    
                    $("#body").removeClass("control").addClass("action");
                });
                
                $("#okbutton").on("click",function() {
                   if(currentAction.Controls[0] && currentAction.Controls[0].length > (currentControl)) {
                        $(".testCaseDescription").text(currentAction.Controls[0][currentControl].Description);
                        $(".testCaseProperty").text(currentAction.Controls[0][currentControl].Property);
                        $(".testCaseValue").text(currentAction.Controls[0][currentControl].Value);
                        currentControl++;
                        $("#body").removeClass("action").addClass("control");
                   } else if (TestCaseData.Steps[steps].Actions[0] && TestCaseData.Steps[steps].Actions[0].length > (actions+1)) {
                        actions++;
                        currentControl = 0;
                        currentAction = TestCaseData.Steps[steps].Actions[0][actions];
                        $(".testCaseDescription").text(currentAction.Description);
                        $(".testCaseProperty").text(currentAction.Object);
                        $(".testCaseValue").text(currentAction.Property);
                        $("#body").removeClass("control").addClass("action");
                   } else if (TestCaseData.Steps && TestCaseData.Steps.length > (steps+1)) {
                        steps++;
                        currentControl = 0;
                        actions = 0;
                        currentAction = TestCaseData.Steps[steps].Actions[0][actions];
                        $(".testCaseDescription").text(currentAction.Description);
                        $(".testCaseProperty").text(currentAction.Object);
                        $(".testCaseValue").text(currentAction.Property);
                        $("#body").removeClass("control").addClass("action");
                   }
                });
            });
        </script>
    </body>
</html>
