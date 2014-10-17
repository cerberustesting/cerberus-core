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
<%@page import="org.cerberus.entity.TestCaseStepActionControl"%>
<%@page import="org.cerberus.entity.TestCaseStepAction"%>
<%@page import="org.cerberus.entity.TestCaseStep"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.entity.TCase"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.service.IUserSystemService"%>
<%@page import="org.cerberus.service.ITestCaseExecutionService"%>
<%@page import="org.cerberus.service.ISqlLibraryService"%>
<%@page import="org.cerberus.service.ITestCaseCountryPropertiesService"%>
<%@page import="org.cerberus.service.ITestCaseStepActionControlService"%>
<%@page import="org.cerberus.service.ITestCaseStepActionService"%>
<%@page import="org.cerberus.service.ITestCaseStepService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.service.IApplicationService"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% Date DatePageStart = new Date();%>
<%@ include file="include/function.jsp" %>
<style>
    .RowActionDiv{
        display:inline-block;
        background-color: white;
    }
    .RowActionDiv:hover{
        background-color: #CCCCCC;
    }
    .RowActionDiv:focus{
        background-color: #CCCCCC;
    }
    .generalPropertyDiv{
        display:inline-block;
        background-color: white;
    }
    .generalPropertyDiv:hover{
        background-color: #CCCCCC;
    }
    .generalPropertyDiv:focus{
        background-color: #CCCCCC;
    }
    .StepHeaderDiv {
        width:100%;
        height:40px;
        clear:both;
        /*display:block;*/
        border-style: solid;
        border-width:thin;
        border-color:#CCCCCC;
        background-image: -moz-linear-gradient(top, #ebebeb, #CCCCCC); 
        background-image: -webkit-linear-gradient(top, #ebebeb, #CCCCCC); 
        font-weight:bold;
        font-family: Trebuchet MS;
        color:#555555;
        text-align: center;

    }

    .StepHeaderContent {
        margin-top:15px; 
    }

    a.docOnline{
        color:white;
    }



</style>
<div id="ManualRun">
    <%
        boolean booleanFunction = false;
        try {
            /*
             * Services
             */
            IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
            IParameterService parameterService = appContext.getBean(IParameterService.class);
            IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
            ITestService testService = appContext.getBean(ITestService.class);
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
            ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
            ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
            ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);
            ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            ISqlLibraryService libService = appContext.getBean(ISqlLibraryService.class);
            ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);

            /**
             * String init
             */
            String SitdmossBugtrackingURL;
            SitdmossBugtrackingURL = "";
            String appSystem = "";
            String proplist = "";

            /*
             * Get Parameters
             */
            String MySystem = "";//request.getAttribute("MySystem").toString();
            if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                MySystem = request.getParameter("system");
            }
            List<String> systems = new ArrayList();
            systems.add(MySystem);

            List<Test> tests = new ArrayList();

            String group = getRequestParameterWildcardIfEmpty(request, "group");
            String status = getRequestParameterWildcardIfEmpty(request, "status");
            String test = getRequestParameterWildcardIfEmpty(request, "Test");
            String testcase = getRequestParameterWildcardIfEmpty(request, "TestCase");
            String country = getRequestParameterWildcardIfEmpty(request, "Country");
            String environment = getRequestParameterWildcardIfEmpty(request, "Environment");
            Boolean tinf = getBooleanParameterFalseIfEmpty(request, "Tinf");
    %>
    <%if (!test.equals("") && !testcase.equals("")) {
            TCase tcase = testCaseService.findTestCaseByKey(test, testcase);

            group = tcase.getGroup();
            status = tcase.getStatus();

            Application myApplication = null;
            if (tcase.getApplication() != null) {
                myApplication = myApplicationService.findApplicationByKey(tcase.getApplication());
                appSystem = myApplication.getSystem();
                SitdmossBugtrackingURL = myApplication.getBugTrackerUrl();
            } else {
                appSystem = "";
                SitdmossBugtrackingURL = "";
            }

    %>
    <br>
    <form method="post" name="RunManualTest"  id="RunManualTest" action="RunManualTest">
        <input name="test" id="test" value="<%=test%>">
        <input name="testCase" id="testCase"  value="<%=testcase%>">
        <input name="env" id="env" value="<%=environment%>">
        <input name="country" id="country" value="<%=country%>">
        <input name="controlMessage">
        <div id="AutomationScriptDiv" style="display : block">
            <div id="StepsMainDiv" style="width:100%;clear:both">
                <div id="StepsDivUnderTitle" style="width:100%;clear:both">
                    <div id="StepsRightDiv" style="width:97%;float:right; margin:2%;">
                        <div id="StepNumberDiv0" style="float:left;">
                        </div>
                        <input style="display:none" value="<%=MySystem%>">
                        <%
                            int incrementStep = 0;
                            List<TestCaseStep> tcsList = tcsService.getListOfSteps(test, testcase);
                            for (TestCaseStep tcs : tcsList) {
                                incrementStep++;
                                String testForQuery = "";
                                String testcaseForQuery = "";
                                int stepForQuery = 0;
                                String isReadonly = "";
                                boolean useStep = false;
                                boolean stepusedByAnotherTest = false;
                                String complementName = "";

                                List<TestCaseStep> tcsUsingThisStep = tcsService.getTestCaseStepUsingStepInParamter(test, testcase, tcs.getStep());
                                if (!tcsUsingThisStep.isEmpty()) {
                                    stepusedByAnotherTest = true;
                                }

                                if (!tcs.getUseStep().equals("Y")) {
                                    testForQuery = tcs.getTest();
                                    testcaseForQuery = tcs.getTestCase();
                                    stepForQuery = tcs.getStep();
                                } else {
                                    testForQuery = tcs.getUseStepTest();
                                    testcaseForQuery = tcs.getUseStepTestCase();
                                    stepForQuery = tcs.getUseStepStep();
                                    isReadonly = "readonly";
                                    useStep = true;
                                    complementName = "Block";
                                }
                                List<TestCaseStepAction> tcsaList = tcsaService.getListOfAction(testForQuery, testcaseForQuery, stepForQuery);

                        %>
                        <div id="listOfTestCaseUsingStep" style="display:none;">
                            <%                                    if (!tcsUsingThisStep.isEmpty()) {
                                    for (TestCaseStep TcUsed : tcsUsingThisStep) {
                            %>    
                            <ul><%=TcUsed.getTest()%> - <%=TcUsed.getTestCase()%></ul><%    }

                                }%>
                        </div>
                        <div style="display:block; clear:both; margin-top:5px">
                            <div id="StepFirstLineDiv<%=incrementStep%>" class="StepHeaderDiv">
                                <div id="StepComboDeleteDiv" style="float:left; width: 30px; text-align: center; height:100%">
                                    <a name="stepAnchor_<%=incrementStep%>"></a>
                                    <a name="stepAnchor_step<%=tcs.getStep()%>"></a>
                                    <input style="display:none" name="step_InUseInOtherTestCase_<%=incrementStep%>" value="<%=stepusedByAnotherTest ? "Y" : "N"%>">
                                    <input style="display:none" name="step_increment" value="<%=incrementStep%>">
                                    <input id="incrementStepNumber" value="<%=incrementStep%>" style="display:none">

                                </div>
                                <div id="StepNumberDiv" style="float:left; width:80px">
                                    &nbsp;&nbsp;Step&nbsp;&nbsp;
                                    <input value="<%=incrementStep%>" name="step_number_<%=incrementStep%>" data-fieldtype="stepNumber" style="margin-top:15px;font-weight: bold; width:20px;background-color:transparent; border-width:0px">
                                    <input type="hidden" name="initial_step_number_<%=incrementStep%>" id="initial_step_number_<%=incrementStep%>" value="<%=tcs.getStep()%>">
                                </div>
                                <div id="StepDescDiv" style="width:550px;float:left">
                                    <div><div><input style="float:right;margin-top:10px;font-weight: bold; width: 500px;background-color:transparent; font-weight:bold;font-size:14px ;font-family: Trebuchet MS; color:#333333; border-color:#EEEEEE;border-style:solid; border-width:thin"
                                                     data-fieldtype="Description" name="step_description_<%=incrementStep%>" value="<%=tcs.getDescription()%>">
                                        </div></div></div>

                                <% if (tcs.getUseStep().equals("Y")) {%>
                                <div id="StepUseStepDiv" style="clear:both">UseStep
                                    <input type="checkbox" name="step_useStep_<%=incrementStep%>" style="margin-top:15px;font-weight: bold; width:20px" onclick="confirmDeletingAction(this, '<%=incrementStep%>')"
                                           <% if (tcs.getUseStep().equals("Y")) {%>
                                           CHECKED
                                           <%}%>
                                           value="Y">
                                </div>
                                <div id="StepCopiedFromDiv" style="float:left">
                                    <p style="margin-top:15px;"> Copied from : </p>
                                </div>
                                <div id="StepUseStepTestDiv" style="float:left">
                                    <select id="step_useStepTest_<%=incrementStep%>" name="step_useStepTest_<%=incrementStep%>" style="width: 100px;margin-top:15px;font-weight: bold;" 
                                            OnChange="findTestcaseByTest(this.value, '<%=MySystem%>', 'step_useStepTestCase_<%=incrementStep%>')">
                                        <%  if (tcs.getUseStepTest().equals("")) { %>
                                        <option style="width: 200px" value="All">-- Choose Test --
                                        </option>
                                        <%  }
                                            List<Test> tList = testService.findTestBySystems(systems);
                                            for (Test tst : tList) {%>
                                        <option style="width: 200px;" class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" <%=tcs.getUseStepTest().compareTo(tst.getTest()) == 0 ? " SELECTED " : ""%>><%=tst.getTest()%>
                                        </option>
                                        <% }
                                        %>
                                    </select>
                                </div>

                                <div id="StepUseStepTestCaseDiv" style="float:left;">
                                    <select name="step_useStepTestCase_<%=incrementStep%>" style="width: 50px;margin-top:15px;font-weight: bold;" 
                                            OnChange="findStepByTestCase($('#step_useStepTest_<%=incrementStep%>').val(), this.value, 'step_useStepStep_<%=incrementStep%>')"
                                            id="step_useStepTestCase_<%=incrementStep%>">
                                        <%  if (tcs.getUseStepTestCase().equals("")) { %>
                                        <option style="width: 200px" value="All">---</option>
                                        <%  } else {
                                            List<TCase> tcList = testCaseService.findTestCaseByTest(test);
                                            for (TCase tc : tcList) {%>
                                        <option style="width: 200px;" class="font_weight_bold_<%=tc.getActive()%>" value="<%=tc.getTestCase()%>" <%=tcs.getUseStepTestCase().compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%>
                                        </option>
                                        <% }
                                                    }%>
                                    </select>
                                </div>
                                <div id="StepUseStepStepDiv" style="float:left">
                                    <select name="step_useStepStep_<%=incrementStep%>" style="width: 50px;margin-top:15px;font-weight: bold;" 
                                            id="step_useStepStep_<%=incrementStep%>" onchange="javascript:$('#UpdateTestCase').submit();">
                                        <%  if (tcs.getUseStepTest().equals("") || tcs.getUseStepTestCase().equals("")) { %>
                                        <option style="width: 200px" value="All">---</option>
                                        <%  } else {
                                            List<TestCaseStep> tcstepList = tcsService.getListOfSteps(tcs.getUseStepTest(), tcs.getUseStepTestCase());
                                            for (TestCaseStep tcstep : tcstepList) {%>
                                        <option style="width: 200px;" value="<%=tcstep.getStep()%>" <%=tcs.getUseStepStep().compareTo(tcstep.getStep()) == 0 ? " SELECTED " : ""%>><%=tcstep.getStep()%>
                                        </option>
                                        <% }
                                                    }%>
                                    </select>
                                </div>
                                <div id="StepUseStepLinkDiv" style="float:left;margin-top:15px">
                                    <a href="TestCase.jsp?Test=<%=tcs.getUseStepTest()%>&TestCase=<%=tcs.getUseStepTestCase()%>#stepAnchor_step<%=tcs.getUseStepStep()%>">Edit Used Step</a>
                                </div>
                                <%}%>
                                <div style="float:right; width:20%">
                                    <input name="stepResultMessage_<%=incrementStep%>"
                                           placeholder="Describe if necessary">
                                </div>

                                <div style="float:right;">
                                    <p>NA</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" style="color:red;font-weight: bold; width:20px"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'NA')" value="NA">
                                </div>
                                <div style="float:right;">
                                    <p>KO</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" style="color:red;font-weight: bold; width:20px"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'KO')" value="KO">
                                </div>
                                <div style="float:right;">
                                    <p>OK</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" style="color:green;font-weight: bold; width:20px"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'OK')" value="KO">
                                </div>



                            </div>
                            <div id="StepsBorderDiv<%=incrementStep%>" style="display:block;margin-top:0px;border-style: solid; border-width:thin ; border-color:#EEEEEE; clear:both;">
                                <div id="StepDetailsDiv" style="clear:both">
                                    <div id="ActionControlDivUnderTitle" style="height:100%;width:100%;clear:both">
                                        <div id="Action<%=tcs.getStep()%>" class="collapseOrExpandStep"  style="height:100%; width:100%;text-align: left; clear:both" >
                                            <div id="BeforeFirstAction<%=tcs.getStep()%>"></div>
                                            <%
                                                String actionColor = "";
                                                String actionFontColor = "#333333";
                                                int incrementAction = 0;
                                                for (TestCaseStepAction tcsa : tcsaList) {

                                                    incrementAction = tcsa.getSequence();
                                                    int b;
                                                    b = incrementAction % 2;
                                                    if (b != 1) {
                                                        //actionColor = "#f3f6fa";
                                                        actionColor = "White";
                                                    } else {
                                                        actionColor = "White";
                                                    }
                                                    if (useStep) {
                                                        actionColor = "#DCDCDC";
                                                        actionFontColor = "grey";
                                                    }
                                            %>
                                            <div id="StepListOfActionDiv<%=incrementStep%><%=incrementAction%>" class="RowActionDiv" style="clear:both;margin-top:0px;display:block;height:20px;width:100%;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                                                <div name="actionRow_color_<%=incrementStep%>" style="background-color:blue; width:8px;height:100%;display:block;float:left">
                                                </div>
                                                <div style="display:inline-block;float:left;width:2%;height:100%;">
                                                    <input type="hidden" name="action_increment_<%=incrementStep%>" value="<%=incrementAction%>" >
                                                    <input type="hidden" name="action_step_<%=incrementStep%>_<%=incrementAction%>" data-fieldtype="stepNumber" value="<%=incrementStep%>" >
                                                </div>
                                                <div style="height:100%;width:4%;display:inline-block;float:left">
                                                    <input class="wob" style="width: 20px; font-weight: bold; background-color: transparent; height:100%; color:<%=actionFontColor%>"
                                                           value="<%=incrementAction%>" data-fieldtype="action_<%=incrementStep%>" data-field="sequence"
                                                           name="action_sequence_<%=incrementStep%>_<%=incrementAction%>" id="action_sequence_<%=incrementStep%>_<%=incrementAction%>">
                                                </div>
                                                <div style="height:20px;width:50%;float:left; display:inline-block">
                                                    <div class="functional_description" style="display:inline-block;clear:both;width:100%; background-color: transparent">

                                                        <div style="float:left; width:59%">
                                                            <input class="wob" class="functional_description" data-fieldtype="Description" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#333333; width: 80%; background-color: transparent;font-size:10px ;font-family: Trebuchet MS; "
                                                                   value="<%=tcsa.getDescription()%>" placeholder="Description"
                                                                   name="action_description_<%=incrementStep%>_<%=incrementAction%>"
                                                                   id="action_description_<%=incrementStep%>_<%=incrementAction%>"<%=isReadonly%>
                                                                   onchange="showChangedRow(this.parentNode.parentNode.parentNode.parentNode)"
                                                                   >

                                                        </div>
                                                        <div class="technical_part" style="width: 30%; float:left; background-color:transparent">
                                                            <input  class="wob property_value" style="width:80%;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; background-color: transparent; color:<%=actionFontColor%>"
                                                                    value="<%=tcsa.getProperty()%>"
                                                                    <%if (useStep) {%>
                                                                    data-usestep-test="<%=testForQuery%>"
                                                                    data-usestep-testcase="<%=testcaseForQuery%>"
                                                                    data-usestep-step="<%=stepForQuery%>"
                                                                    <%}%>
                                                                    onchange="showChangedRow(this.parentNode.parentNode.parentNode.parentNode)" name="action_property_<%=incrementStep%>_<%=incrementAction%>" <%=isReadonly%>>
                                                        </div>
                                                        <%if(!tcsa.getProperty().equals("")){%>
                                                        <div style="width:10%; float:left">
                                                            <input type="button" value="c" onclick="calcProp('<%=tcsa.getProperty()%>')">
                                                        </div>
                                                        <%}%>
                                                    </div>
                                                </div>
                                                <div style="background-color:blue; width:3px;height:100%;display:inline-block;float:right">
                                                </div>
                                                <div style="float:right; width:20%">
                                                    <input name="actionResultMessage_<%=incrementStep%>_<%=incrementAction%>"
                                                           placeholder="Describe if necessary">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="actionStatus_<%=incrementStep%>_<%=incrementAction%>" style="color:red;font-weight: bold; width:20px"
                                                           class="actioncontrol_NA_<%=incrementStep%>" value="NA">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="actionStatus_<%=incrementStep%>_<%=incrementAction%>" style="color:red;font-weight: bold; width:20px"
                                                           class="actioncontrol_KO_<%=incrementStep%>" value="KO">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="actionStatus_<%=incrementStep%>_<%=incrementAction%>"
                                                           class="actioncontrol_OK_<%=incrementStep%>" style="color:green;font-weight: bold; width:20px"
                                                           value="OK">
                                                </div>

                                            </div>

                                            <%
                                                List<TestCaseStepActionControl> tcsacList = tcsacService.findControlByTestTestCaseStepSequence(testForQuery, testcaseForQuery, stepForQuery, tcsa.getSequence());

                                                int incrementControl = 0;
                                                String controlColor = "white";
                                                for (TestCaseStepActionControl tcsac : tcsacList) {
                                                    incrementControl = tcsac.getControl();
                                                    int e;
                                                    e = incrementControl % 2;
                                                    if (e != 1) {
                                                        controlColor = "White";
                                                    } else {
                                                        controlColor = "White";
                                                        //controlColor = "#f3f6fa";
                                                    }

                                                    if (useStep) {
                                                        controlColor = "#DCDCDC";
                                                    }
                                            %>
                                            <div id="StepListOfControlDiv<%=incrementStep%><%=incrementAction%><%=incrementControl%>" class="RowActionDiv" style="width:100%;height:20px;clear:both;display:block;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                                                <div style="background-color:#33CC33; width:8px;height:100%;display:inline-block;float:left">
                                                </div>
                                                <div style="height:100%;width: 2%;float:left; text-align: center;">
                                                    <input type="hidden" value="<%=incrementControl%>" name="control_increment_<%=incrementStep%>_<%=incrementAction%>">
                                                    <input type="hidden" value="<%=incrementStep%>" name="control_step_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" data-fieldtype="stepNumber">
                                                </div>
                                                <div style="width:2%;float:left;height:100%;display:inline-block">
                                                    <input data-fieldtype="ctrlseq_<%=incrementStep%>" data-field="sequence" class="wob" style="width: 20px; font-weight: bold;color:<%=actionFontColor%>"
                                                           value="<%=incrementAction%>" name="control_sequence_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>">
                                                </div>
                                                <div style="width:2%;float:left;height:100%;display:inline-block">
                                                    <input class="wob" style="width: 20px; font-weight: bold; color:<%=actionFontColor%>"
                                                           data-fieldtype="control_<%=incrementStep%>_<%=incrementAction%>" value="<%=incrementControl%>" name="control_control_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>">
                                                </div>
                                                <div style="height:100%;width:50%;float:left;display:inline-block">
                                                    <div class="functional_description_control" style="clear:both;width:100%;height:20px">
                                                        <div style="float:left; width:80%">
                                                            <input class="wob" placeholder="Description" class="functional_description_control" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#333333; width: 80%; background-color: transparent;font-size:10px ;font-family: Trebuchet MS; "
                                                                   data-fieldtype="Description" value="<%=tcsac.getDescription()%>" name="control_description_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" maxlength="1000">
                                                        </div>
                                                    </div>
                                                </div>
                                                <div style="background-color:#33CC33; width:3px;height:100%;display:inline-block;float:right">
                                                </div>
                                                <div style="float:right; width:20%">
                                                    <input name="controlResultMessage_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"
                                                           placeholder="Describe if necessary">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="controlStatus_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="background-color:red;font-weight: bold; width:20px"
                                                           class="actioncontrol_NA_<%=incrementStep%>" value="NA">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="controlStatus_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="background-color:red;font-weight: bold; width:20px"
                                                           class="actioncontrol_KO_<%=incrementStep%>" value="KO">
                                                </div>
                                                <div style="float:right">
                                                    <input type="radio" name="controlStatus_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="color:green;font-weight: bold; width:20px"
                                                           class="actioncontrol_OK_<%=incrementStep%>" value="OK">
                                                </div>



                                            </div>    
                                            <%   }%>
                                            <div id="DivActionEndOfAction<%=incrementStep%><%=incrementAction%>" class="endOfAction"></div>
                                            <%
                                                } /*
                                                 * End actions loop
                                                 */%>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div id="StepsEndDiv<%=incrementStep%>" style="display:none; width:100%;"></div>
                        </div>
                        <% }%>
                    </div>
                </div>
            </div>
        </div>
        <input type="submit" value="submit">
    </form>
    <%
            }
        } catch (Exception e) {
            out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
        }
    %>
</div>
<script>
    function checkAllAction(obj, stepId, status) {
        var bool;
        if (obj.checked === true) {
            bool = true;
        } else {
            bool = false;
        }
        $('#StepsBorderDiv' + stepId).find('input[class="actioncontrol_' + status + '_' + stepId + '"]')
                .each(function() {
                    $(this).prop('checked', bool);

                });

    }
</script>
<script>
    function calcProp(property) {
    var query = {test: $("#test").val(),
        testCase: $("#testCase").val(),
        property: property,
        type: "getFromTestData"//$("#type").val()
    };

    if (query.type !== "executeSoapFromLib" && query.type !== "getFromTestData") {
        query.country = $("#country").val();
        query.environment = $("#env").val();
        query.database = $("#db").val();
    }

    $.get('CalculatePropertyForTestCase', query, function(data) {

        if (data !== null && data.resultList !== null) {
            $("#result").empty().text("Value: '" + data.resultList + "'");
            $("#propdesc").empty().text("Description: '" + data.description + "'");
        } else {
            $("#result").empty().append("<b>Unable to retrieve property in database !</b>");
        }
    });
}
;
</script>

<br><% out.print(display_footer(DatePageStart));%>

