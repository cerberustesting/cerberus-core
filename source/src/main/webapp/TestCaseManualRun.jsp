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
<%@page import="org.cerberus.enums.MessageGeneralEnum"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionInQueueService"%>
<%@page import="org.cerberus.crud.service.ICountryEnvironmentApplicationService"%>
<%@page import="org.cerberus.crud.entity.CountryEnvironmentApplication"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionSysVerService"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecutionSysVer"%>
<%@page import="org.cerberus.crud.factory.IFactoryTestCaseExecutionSysVer"%>
<%@page import="org.cerberus.crud.factory.IFactoryTestCaseExecution"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecution"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.crud.service.ICountryEnvParamService"%>
<%@page import="org.cerberus.crud.entity.CountryEnvParam"%>
<%@page import="org.cerberus.enums.MessageGeneralEnum"%>
<%@page import="org.cerberus.crud.entity.MessageGeneral"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepActionControl"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepAction"%>
<%@page import="org.cerberus.crud.entity.TestCaseStep"%>
<%@page import="org.cerberus.crud.entity.Application"%>
<%@page import="org.cerberus.crud.entity.TCase"%>
<%@page import="org.cerberus.crud.entity.Test"%>
<%@page import="org.cerberus.crud.service.IUserSystemService"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionService"%>
<%@page import="org.cerberus.crud.service.ISqlLibraryService"%>
<%@page import="org.cerberus.crud.service.ITestCaseCountryPropertiesService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionControlService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepService"%>
<%@page import="org.cerberus.crud.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.crud.service.ITestCaseService"%>
<%@page import="org.cerberus.crud.service.ITestService"%>
<%@page import="org.cerberus.crud.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	Date DatePageStart = new Date();
%>
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

    .ExecutionHeaderDiv {
        width:100%;
        clear:both;
        display:block;
        border-style: solid;
        border-width:thin;
        border-color:#CCCCCC;
        background-image: -moz-linear-gradient(top, #ebebeb, #CCCCCC); 
        background-image: -webkit-linear-gradient(top, #ebebeb, #CCCCCC); 
        font-weight:bold;
        font-family: Trebuchet MS;
        color:#555555;

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
                ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
                ICountryEnvironmentApplicationService countryEnvironmentApplicationService = appContext.getBean(ICountryEnvironmentApplicationService.class);
                IFactoryTestCaseExecution factoryTestCaseExecution = appContext.getBean(IFactoryTestCaseExecution.class);
                IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer = appContext.getBean(IFactoryTestCaseExecutionSysVer.class);
                ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
                ITestCaseExecutionInQueueService testcaseExecutionQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

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
                String idFromQueue = getRequestParameterWildcardIfEmpty(request, "IdFromQueue");
                String browser = getRequestParameterWildcardIfEmpty(request, "Browser");
                String browserVersion = "";
                String tag = getRequestParameterWildcardIfEmpty(request, "Tag");
                Boolean tinf = getBooleanParameterFalseIfEmpty(request, "Tinf");

                Application myApp = null;
                TCase tCase = testCaseService.findTestCaseByKey(test, testcase);
                if (tCase != null) {
                    myApp = myApplicationService.convert(myApplicationService.readByKey(tCase.getApplication()));
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
                CountryEnvParam countryEnvParam;
                try {
                    countryEnvParam = countryEnvParamService.findCountryEnvParamByKey(myApp.getSystem(), country, environment);
                } catch (CerberusException e) {
                    CerberusException ex = new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    ex.getMessageError().setDescription("Combination Environment: '" + environment + "' and Country: '" + country
                            + "' not defined for System/Application: " + myApp.getSystem() + "/" + myApp.getApplication());
                    throw ex;
                }
                CountryEnvironmentApplication countryEnvironmentParameter;
                try {
                    countryEnvironmentParameter = countryEnvironmentApplicationService.findCountryEnvironmentParameterByKey(myApp.getSystem(), country, environment, myApp.getApplication());
                } catch (CerberusException e) {
                    CerberusException ex = new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    throw ex;
                }
                String build = countryEnvParam.getBuild();
                String revision = countryEnvParam.getRevision();
                long now = new Date().getTime();
                String version = Infos.getInstance().getProjectNameAndVersion();

                String myUser = "";
                if (!(request.getUserPrincipal() == null)) {
                    myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
                }

                if (myUser == null || myUser.length() <= 0) {
                    myUser = "Manual";
                }

                TestCaseExecution execution = factoryTestCaseExecution.create(0, test, testcase, build, revision, environment, country, browser, "", "", browserVersion, now, now,
                        "PE", "Execution Started", myApp, "", "", "", tag, "Y", 0, 0, 0, 0, true, "", "", tCase.getStatus(), version,
                        null, null, null, false, "", "", "", "", "", "", null, null, myUser);

                long executionId = testCaseExecutionService.insertTCExecution(execution);
                execution.setId(executionId);

                if (idFromQueue != null && !"".equals(idFromQueue)) {
                    testcaseExecutionQueueService.remove(Long.valueOf(idFromQueue));
                }

                TestCaseExecutionSysVer testCaseExecutionSysVer = factoryTestCaseExecutionSysVer.create(execution.getId(), myApp.getSystem(), build, revision);
                testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);
    %>
    <%if (!test.equals("") && !testcase.equals("")) {
            TCase tcase = testCaseService.findTestCaseByKey(test, testcase);

            group = tcase.getGroup();
            status = tcase.getStatus();

            Application myApplication = null;
            if (tcase.getApplication() != null) {
                myApplication = myApplicationService.convert(myApplicationService.readByKey(tcase.getApplication()));
                appSystem = myApplication.getSystem();
                SitdmossBugtrackingURL = myApplication.getBugTrackerUrl();
            } else {
                appSystem = "";
                SitdmossBugtrackingURL = "";
            }
            
            String shortDesc = tcase.getShortDescription();


    %>
    <br>
    <form method="post" name="RunManualTest"  id="RunManualTest" action="RunManualTest">
        <div class="ExecutionHeaderDiv">
            <p style="font-size:14px; font-weight:bold; color:red">Execution <%=executionId%> Started</p>
            <input class="wob" name="test" id="test" value="<%=test%>" disabled="true">
            <input class="wob" name="testCase" id="testCase"  value="<%=testcase%>" disabled="true">
            <input class="wob" name="env" id="env" value="<%=environment%>" disabled="true">
            <input class="wob" name="country" id="country" value="<%=country%>" disabled="true">
            <input class="wob" name="tag" id="tag" value="<%=tag%>" disabled="true">
            <input class="wob" name="browser" id="browser" value="<%=browser%>" disabled="true">
            <a href="http://<%=countryEnvironmentParameter.getIp() + countryEnvironmentParameter.getUrl()%>" target="_blank">http://<%=countryEnvironmentParameter.getIp() + countryEnvironmentParameter.getUrl()%></a>
            <input class="wob" name="executionId" id="executionId" value="<%=executionId%>" style="display:none">
            <input class="wob" name="IdFromQueue" id="IdFromQueue" value="<%=idFromQueue%>" style="display:none">
            <br>

            <div id="ExecutionGlobalStatusDiv" style="width:97%;display:inline-block">
                <div  style="display:inline-block; float:left;width:50%">
                    <input style="width:99%;color:#555555;" name="shortDesc" disabled="true" value="<%=shortDesc%>">
                </div>
                <div  style="display:block; float:right;width:19%">
                    <textarea style="width:99%" name="controlMessage" placeholder="Comment execution"></textarea>
                </div>
                <div style="display:inline-block;float:right;">
                    <p style="font-weight:bold;color:yellow;margin-top:5px">NA</p>
                    <input type="radio" name="executionStatus" style="color:red;font-weight: bold; width:20px;"
                           onclick="checkAllAction(this, '', 'NA')" value="NA">
                </div>
                <div style="float:right;">
                    <p style="font-weight:bold;color:red;margin-top:5px">KO</p>
                    <input type="radio" name="executionStatus" style="color:red;font-weight: bold; width:20px"
                           onclick="checkAllAction(this, '', 'KO')" value="KO">
                </div>
                <div style="float:right;">
                    <p style="font-weight:bold;color:green;margin-top:5px">OK</p>
                    <input type="radio" name="executionStatus" style="color:green;font-weight: bold; width:20px"
                           onclick="checkAllAction(this, '', 'OK')" value="OK">
                </div>
            </div>
        </div>
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

                                <div style=" width:3px;height:100%;display:inline-block;float:right">
                                </div>
                                <div style="float:right; display:inline-block;width:15%">
                                    <input style="width:90%; margin-top:10px" name="stepResultMessage_<%=incrementStep%>"
                                           placeholder="Describe if necessary">
                                </div>
                                <div style="float:right; width:5%">
                                    <p style="font-size:9px;margin-top:15px">Screenshot</p>
                                </div> 
                                <div style="float:right;">
                                    <p style="font-weight:bold;color:yellow;margin-top:5px">NA</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" class="step_NA" data-stepId="<%=incrementStep%>" style="color:red;font-weight: bold; width:20px;"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'NA')" value="NA">
                                </div>
                                <div style="float:right;">
                                    <p style="font-weight:bold;color:red;margin-top:5px">KO</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" class="step_KO" data-stepId="<%=incrementStep%>" style="color:red;font-weight: bold; width:20px"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'KO')" value="KO">
                                </div>
                                <div style="float:right;">
                                    <p style="font-weight:bold;color:green;margin-top:5px">OK</p>
                                    <input type="radio" name="stepStatus_<%=incrementStep%>" class="step_OK" data-stepId="<%=incrementStep%>" style="color:green;font-weight: bold; width:20px"
                                           onclick="checkAllAction(this, '<%=incrementStep%>', 'OK')" value="OK">
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
                                                        <%if (!tcsa.getProperty().equals("")) {%>
                                                        <div style="width:10%; float:left">
                                                            <input type="button" value="c" onclick="calcProp('<%=tcsa.getProperty()%>')">
                                                        </div>
                                                        <%}%>
                                                    </div>
                                                </div>
                                                <div style="background-color:blue; width:3px;height:100%;display:inline-block;float:right">
                                                </div>
                                                <div style="float:right;display:inline-block; width:15%">
                                                    <input style="width:90%" name="actionResultMessage_<%=incrementStep%>_<%=incrementAction%>"
                                                           placeholder="Describe if necessary">
                                                </div>
                                                <div style="float:right; width:5%">
                                                    <input type="button" style="margin-left:15px ;width:10px;background-image: url(images/photo.png);background-size: 100%; width: 19px; height: 12px;border: 0 none; bottom: 0px" 
                                                           onclick="showActionScreenshotDiv('<%=incrementStep%>', '<%=incrementAction%>')">
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
                                            <div id="screenshotDiv_<%=incrementStep%>_<%=incrementAction%>" style="display:none">
                                                <span>
                                                    Upload screenshot of the execution
                                                </span>
                                                <div id="formPictureSave_<%=incrementStep%>_<%=incrementAction%>" data-type="picture">
                                                    <input id="fileupload_<%=incrementStep%>_<%=incrementAction%>" type="file" name="files[]" data-url="SaveManualExecutionPicture" multiple/>
                                                    <input name="pictStep" value="<%=incrementStep%>" style="display:none">
                                                    <input name="pictAction" value="<%=incrementAction%>" style="display:none">
                                                    <input id="runId" name="runId" type="hidden" value="<%=executionId%>"/>
                                                    <input id="picTest" name="picTest" type="hidden" value="<%=test%>"/>
                                                    <input id="picTestCase" name="picTestCase" type="hidden" value="<%=testcase%>"/>
                                                </div>
                                                <input id="takeScreenshot_<%=incrementStep%>_<%=incrementAction%>" name="takeScreenshot_<%=incrementStep%>_<%=incrementAction%>" style="display:none" value="N">
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
                                                <div style="float:right; width:15%">
                                                    <input style="width:90%" name="controlResultMessage_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"
                                                           placeholder="Describe if necessary">
                                                </div>
                                                <div style="float:right; width:5%">
                                                    <input type="button" title="Take ScreenShot" style="margin-left:15px ;width:10px;background-image: url(images/photo.png);background-size: 100%; width: 19px; height: 12px;border: 0 none; bottom: 0px" 
                                                           onclick="showControlScreenshotDiv('<%=incrementStep%>', '<%=incrementAction%>', '<%=incrementControl%>')">
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
                                            <div id="screenshotDiv_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="display:none">
                                                <span>
                                                    You can upload screenshots of the execution <b>(only JPG files)</b>
                                                </span>
                                                <div id="formPictureSave_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" data-type="picture">
                                                    <input id="fileupload_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" type="file" name="files[]" data-url="SaveManualExecutionPicture" multiple/>
                                                    <input name="pictStep" value="<%=incrementStep%>" style="display:none">
                                                    <input name="pictAction" value="<%=incrementAction%>" style="display:none">
                                                    <input name="pictControl" value="<%=incrementControl%>" style="display:none">
                                                    <input id="runId" name="runId" type="hidden" value="<%=executionId%>"/>
                                                    <input id="picTest" name="picTest" type="hidden" value="<%=test%>"/>
                                                    <input id="picTestCase" name="picTestCase" type="hidden" value="<%=testcase%>"/>
                                                </div>
                                                <input id="takeScreenshot_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" name="takeScreenshot_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="display:none" value="N">
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
        <input name="isCancelExecution"  id="isCancelExecution" style="display:none">
    </form>
    <form id="formPictureSave" action="SaveManualExecutionPicture" method="post" enctype="multipart/form-data">
        <div id="formPictureSave" action="SaveManualExecutionPicture" method="post" enctype="multipart/form-data">
            
        </div>    
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
        if (stepId !== '') {
            $('#StepsBorderDiv' + stepId).find('input[class="actioncontrol_' + status + '_' + stepId + '"]')
                    .each(function() {
                        $(this).prop('checked', bool);

                    });
        } else {
            $('div[class="StepHeaderDiv"]').each(function() {
                $(this).find('input[class="step_' + status + '"]').each(function() {
                    $(this).prop('checked', bool);
                    checkAllAction(this, $(this).attr('data-stepId'), status);
                });
            });

        }

    }
</script>
<script>
    function showActionScreenshotDiv(step, action) {
        $('#screenshotDiv_' + step + '_' + action).attr('style', 'display:block');
        $('#takeScreenshot_' + step + '_' + action).val('Y');
    }

    function showControlScreenshotDiv(step, action, control) {
        $('#screenshotDiv_' + step + '_' + action + '_' + control).attr('style', 'display:block');
        $('#takeScreenshot_' + step + '_' + action + '_' + control).val('Y');
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

    function submitExecution() {
        $("[data-type='picture']").each(function(index, element) {
            if ($(element).parent().attr('style') !== 'display:none') {
                $("#formPictureSave").empty().append(element);




                var formObj = $("#formPictureSave");
                var formURL = formObj.attr("action");
                var formData = new FormData(formObj[0]);
                $.ajax({
                    url: formURL,
                    type: 'POST',
                    data: formData,
                    mimeType: "multipart/form-data",
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    error: function(jqXHR, textStatus, errorThrown) {
                        alert(errorThrown);
                    }
                });
            }
        });

        $("#RunManualTest").submit();
    }

</script>

<br><% out.print(display_footer(DatePageStart));%>

