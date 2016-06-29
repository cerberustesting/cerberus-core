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
<%@page import="java.util.Set"%>
<%@page import="org.cerberus.crud.entity.UserSystem"%>
<%@page import="org.cerberus.crud.entity.User"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.crud.service.IUserService"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashSet"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.cerberus.crud.service.impl.TestCaseStepService"%>
<%@page import="org.cerberus.crud.service.IUserSystemService"%>
<%@page import="org.cerberus.crud.entity.SqlLibrary"%>
<%@page import="org.cerberus.crud.service.ISqlLibraryService"%>
<%@page import="org.cerberus.crud.entity.TestCaseCountryProperties"%>
<%@page import="org.cerberus.crud.service.ITestCaseCountryPropertiesService"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepActionControl"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepAction"%>
<%@page import="org.cerberus.crud.entity.TestCaseStep"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionControlService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepService"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionService"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecution"%>
<%@page import="org.cerberus.crud.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.crud.entity.TCase"%>
<%@page import="org.cerberus.crud.service.ITestCaseService"%>
<%@page import="org.cerberus.crud.entity.Test"%>
<%@page import="org.cerberus.crud.service.ITestService"%>
<%@page import="java.util.Enumeration"%>
<%@page import="org.cerberus.crud.entity.Parameter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.cerberus.crud.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.crud.entity.Application"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.crud.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.crud.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.cerberus.util.answer.AnswerList"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>TestCase</title>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <!--<script type='text/javascript' src='js/Form_1.js'></script>-->
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/elrte.min.js"></script>
        <script type="text/javascript" src="js/i18n/elrte.en.js"></script>
        <script type="text/javascript" src="js/elfinder.min.js"></script>
        <script type="text/javascript" src="js/elFinderSupportVer1.js"></script>
        <!--<script type="text/javascript" src="js/jquery.dataTables.js"></script>-->
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/pages/TestCase.js"></script>
        <link rel="stylesheet" type="text/css" href="css/elrte.min.css">
        <link rel="stylesheet" type="text/css" href="css/elfinder.min.css">
        <link rel="stylesheet" type="text/css" href="css/theme.css">
        

        <script type="text/javascript">
            var displayOnlyFunctional = false;
            function showOnlyFunctional() {
                displayOnlyFunctional = !displayOnlyFunctional;
                $('.functional_description').toggleClass('only_functional_description_size');
                $('.technical_part').toggleClass('only_functional');
                SetCookie("displayOnlyFunctional", (displayOnlyFunctional ? "TRUE" : "FALSE"));
            }

            var collapseOrExpandStep = false;
            collapseOrExpandStep = !collapseOrExpandStep;
            function collapseOrExpandAllStep() {
                $('.collapseOrExpandStep').toggleClass('collapseOrExpandAllStep');
                SetCookie("collapseOrExpandStep", (collapseOrExpandStep ? "TRUE" : "FALSE"));
            }

            $().ready(function() {
                if ("TRUE" == GetCookie("displayOnlyFunctional")) {
                    showOnlyFunctional();
                }

                elRTE.prototype.options.toolbars.cerberus = ['style', 'alignment', 'colors', 'images', 'format', 'indent', 'lists', 'links'];
                var opts = {
                    lang: 'en',
                    styleWithCSS: false,
                    width: 615,
                    height: 200,
                    toolbar: 'cerberus',
                    allowSource: false,
                    cssfiles: ['css/crb_style.css'],
                    fmOpen: function(callback) {
                        $('<div />').dialogelfinder({
                            url: 'PictureConnector',
                            transport: new elFinderSupportVer1(),
                            commandsOptions: {
                                getfile: {
                                    oncomplete: 'destroy' // destroy elFinder after file selection
                                }
                            },
                            getFileCallback: callback // pass callback to file manager
                        });
                    }
                };
                var bool = $('#generalparameter').is(':visible');
                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if (!bool) {
                    $('#generalparameter').show();
                }
                $('#howto').elrte(opts);
                $('#value').elrte(opts);
                $('.el-rte').css('z-index', 0);
                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if (!bool) {
                    $('#generalparameter').hide();
                }
                $("#UpdateTestCase").submit(function() {                    
                    $('#howtoDetail').val($('#howto').elrte('val'));
                    $('#valueDetail').val($('#value').elrte('val'));
                     /*temporary workaround for issue #611*/
                    var stepNumbersValid = validateStepNumbers();

                    if(!stepNumbersValid){
                        //TODO: add translation in future refactoring
                        alert("You have steps with the same number. Please check the step numbers before proceed!!");
                        return false;
                    }else{
                        
                    }
                });
            });</script>
        <script>
            function customizeView(value) {
                if (value === 'onlyStep') {
                    var elem = document.getElementsByClassName('RowActionDiv');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'none';
                    }
                }

                if (value === 'full') {
                    var elem = document.getElementsByClassName('RowActionDiv');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                    var elem = document.getElementsByClassName('functional_description');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                    var elem = document.getElementsByClassName('technical_part');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                }

                if (value === 'hideUseStep') {
                    var elem = document.getElementsByClassName('ActionOfUseStep');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'none';
                    }
                    var elem = document.getElementsByClassName('ActionOfNormalStep');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                }

                if (value === 'technicalView') {
                    var elem = document.getElementsByClassName('functional_description');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'none';
                    }
                    var elem = document.getElementsByClassName('technical_part');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                }

                if (value === 'fonctionalView') {
                    var elem = document.getElementsByClassName('functional_description');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'block';
                    }
                    var elem = document.getElementsByClassName('technical_part');
                    for (var a = 0; a < elem.length; a++) {
                        elem[a].style.display = 'none';
                    }
                }




            }
        </script>
        <script type="text/javascript">
            var track = 0;
            function trackChanges(originalValue, newValue, element) {
                if (originalValue != newValue) {
                    window.track = window.track + 1;
                } else {
                    window.track = window.track - 1;
                }
            }
        </script>
        <script>
            function alertMessage(message) {
                alert(message);
            }
        </script>
        <style>
            .only_functional {
                font-weight:bold;
                font-size:20px ;
                font-family: Trebuchet MS;
            }

            .only_functional_description_size {
                font-weight:bold;
                font-size:20px ;
                font-family: Trebuchet MS;
            }

            .collapseOrExpandAllStep {
                display:none;
            }


        </style>
        <style>
            .RowActionDiv{
                display:inline-block;
                background-color: white;
            }
            .RowActionDiv:hover {
                background-color: #EEEEEE;
                color:black;
            }
            .RowActionDiv:hover .AttachPictureClass{
                display: inline-block;
            }
            .AttachPictureClass{
                display: none;
            }
            .RowActionDiv:hover p{
                color:black;
            }
            .RowActionDiv p{
                color:white;
            }
            .RowActionDiv:hover input{
                background-color:#EEEEEE;
            }
            .RowActionDiv input{
                background-color:white;
            }
            .RowActionDiv:hover select{
                background-color:#EEEEEE;
            }
            .RowActionDiv select{
                background-color:white;
            }
            .RowActionDiv span{
                color:white;
            }
            .RowActionDiv:hover span{
                color : #008cda;
            }
            .RowActionDiv:focus{
                background-color: #EEEEEE;
                color:black;
            }
            .generalPropertyDiv{
                display:inline-block;
                background-color: white;
            }
            .generalPropertyDiv div{
                background-color: white;
            }
            .generalPropertyDiv:hover div{
                background-color: #EEEEEE;
            }
            .generalPropertyDiv:hover{
                background-color: #EEEEEE;
            }
            .generalPropertyDiv:focus{
                background-color: #EEEEEE;
            }
            .StepHeaderDiv {
                width:100%;
                height:45px;
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
                /*color:white;*/
            }

            .RowToDelete{
                display:inline-block;
                background-color: #DD7777;
            }
            .RowToDelete input{
                background-color:#DD7777;
            }
            .RowToDelete select{
                background-color:#DD7777;
            }

            .libLineDiv{
                display:block;
            }
            .libHideLineDiv{
                display:none;
            }

        </style>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.html" %>
        <%@ include file="include/testcase/listTestDataLib.html"%> 
        <%@ include file="include/testcase/addPicture.html"%> 
        <%@ include file="include/testcase/showPicture.html"%> 
        <%@ include file="include/testcase/showTestCases.html"%> 
        <%@ include file="include/testcase/showProperty.html"%> 
        <%@ include file="include/testcase/showSQLLibrary.html"%> 
        <div id="body">
            <%
                boolean booleanFunction = false;
                try {
                    /*
                     * Services
                     */
                    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    IParameterService parameterService = appContext.getBean(IParameterService.class);
                    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
                    ITestService testService = appContext.getBean(ITestService.class);
                    ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
                    ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
                    TestCaseStepService tcsService = appContext.getBean(TestCaseStepService.class);
                    ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
                    ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);
                    ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
                    ISqlLibraryService libService = appContext.getBean(ISqlLibraryService.class);
                    ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
                    IInvariantService invariantService = appContext.getBean(IInvariantService.class);
                    IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
                    IUserService userService = appContext.getBean(IUserService.class);

                    /**
                     * Function
                     */
                    booleanFunction = StringUtil.parseBoolean(parameterService.findParameterByKey("cerberus_testcase_function_booleanListOfFunction", "").getValue());
                    String listOfFunction = "";
                    if (booleanFunction) {
                        Parameter functions = parameterService.findParameterByKey("cerberus_testcase_function_urlForListOfFunction", "");
                        listOfFunction = functions.getValue();
                    }

                    /**
                     * String init
                     */
                    String SitdmossBugtrackingURL;
                    SitdmossBugtrackingURL = "";
                    String appSystem = "";
                    String proplist = "";

                    /*
                     * Get default system and language
                     */
                    String user = request.getUserPrincipal().getName();
                    User myUser = userService.findUserByKey(user);
                    List<String> systems = new ArrayList();
                    //TODO:FN temporary fix
                    String MySystem = myUser.getDefaultSystem();
                    systems.add(MySystem);
                    String myLang = myUser.getLanguage();
                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("MySystem", myUser.getDefaultSystem());
                    httpSession.setAttribute("MyLang", myUser.getLanguage());

                    List<Test> tests = new ArrayList();

                    String group = getRequestParameterWildcardIfEmpty(request, "group");
                    String status = getRequestParameterWildcardIfEmpty(request, "status");
                    String test = getRequestParameterWildcardIfEmpty(request, "Test");
                    String testcase = getRequestParameterWildcardIfEmpty(request, "TestCase");
                    Boolean tinf = getBooleanParameterFalseIfEmpty(request, "Tinf");
            %>
            <input id="urlForListOffunction" value="<%=listOfFunction%>" style="display:none">
            <form action="TestCase.jsp" method="post" name="selectTestCase" id="selectTestCase">
                <div class="filters" style="float:left; width:100%; height:30px">
                    <div style="float:left; width:60px">
                        <p class="dttTitle">Filters
                        </p>
                    </div>
                    <div name="labelTest" style="float:left; width:100px;font-weight: bold;">Test
                    </div>
                    <div style="float:left">
                        <select id="filtertest" name="Test" style="width: 200px" OnChange="document.selectTestCase.submit()">
                            <%  if (test != null && test.compareTo("") == 0) { %>
                            <option style="width: 200px" value="All">-- Choose Test --
                            </option>
                            <%  }

                                //            for (UserSystem us : userSystemService.findUserSystemByUser(request.getUserPrincipal().getName())){
                                //systems.add(us.getSystem());
                                //}
                                tests = testService.findTestBySystems(systems);
                                for (Test tst : tests) {
                            %>
                            <option style="width: 200px;" class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" <%=test.compareTo(tst.getTest()) == 0 ? " SELECTED " : ""%>><%=tst.getTest()%>
                            </option>
                            <% }
                            %>
                        </select>
                    </div>
                    <div name="labelTestCase" style="float:left">TestCase
                    </div>
                    <div style="float:left">
                        <select id="filtertestcase" name="TestCase" style="width: 750px" OnChange="document.selectTestCase.submit()">
                            <% if (test.compareTo("") == 0) { %>
                            <option style="width: 750px" value="All">-- Choose Test First --
                            </option>
                            <%  } else {
                                List<TCase> tcList = testCaseService.findTestCaseByTest(test);
                                for (TCase tc : tcList) {%>
                            <option style="width: 750px;" class="font_weight_bold_<%=tc.getActive()%>" value="<%=tc.getTestCase()%>" <%=testcase.compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%>  [<%=tc.getApplication()%>]  : <%=tc.getShortDescription()%>
                            </option>
                            <%  }
                                } %>
                        </select>
                    </div>
                    <div style="float:left">
                        <input id="loadbutton" class="button" type="submit" name="Load" value="Load">
                    </div>
                </div>
            </form>
            <br>
            <br>
            <%if (!test.equals("") && !testcase.equals("")) {
                    TCase tcase = null;
                    boolean isTestCaseExist = false;
                    tcase = testCaseService.findTestCaseByKey(test, testcase);
                    isTestCaseExist = (tcase == null) ? false : true;

                    if (isTestCaseExist) {

                        //First Check if testcase can be edited (good system selected)
                        User MyUserobj = userService.findUserByKeyWithDependencies(request.getUserPrincipal().getName());

                        // Change system if requested inURL
                        String setSystem = getRequestParameterWildcardIfEmpty(request, "SetSystem");
                        if (!setSystem.equals("")) {
                            MyUserobj.setDefaultSystem(setSystem);
                            userService.updateUser(MyUserobj);
                            MySystem = setSystem;
            %>
            <script>
                $(document).ready(function() {
                    $("#MySystem option:selected").attr('selected', false);
                    $("#MySystem option[value='<%=setSystem%>']").attr("selected", true);
                });
            </script>
            <%
                }

                List<UserSystem> systemList = userSystemService.findUserSystemByUser(request.getUserPrincipal().getName());
                List<String> usList = new ArrayList();
                for (UserSystem us : systemList) {
                    usList.add(us.getSystem());
                }
                String applicationSystem = myApplicationService.convert(myApplicationService.readByKey(tcase.getApplication())).getSystem();
                if (!MySystem.equals(applicationSystem)) {%>
            <script>
                <%
                    //if system selected is not the one of the application but is one of the authorized system, propose to switch
                    if (usList.contains(applicationSystem)) {
                %>
                var sys = '<%=applicationSystem%>';
                if (confirm('This Testcase is only accessible with another system selection\nSwitch to system ' + sys + '?')) {
                    window.location = "./TestCase.jsp?Test=<%=test%>&TestCase=<%=testcase%>&SetSystem=<%=applicationSystem%>";
                } else {
                    window.location = "./Homepage.jsp";
                }
                <%
                } else {%>
                alert("You are not allowed to access to this system\nPlease contact your Cerberus Administrator to modify your account");
                window.location = "./Homepage.jsp";
                <%}%>
            </script>
            <%
                }

                Test testObject = testService.findTestByKey(test);
                AnswerList answerCountry = invariantService.readByIdname("COUNTRY");
                List<Invariant> countryListInvariant = (List<Invariant>)answerCountry.getDataList();
                List<String> countryListTestcase = testCaseCountryService.findListOfCountryByTestTestCase(test, testcase);
                TestCaseExecution tce = testCaseExecutionService.findLastTestCaseExecutionNotPE(test, testcase);
                List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 1));
                List<TestCaseCountryProperties> tccpList = tccpService.findDistinctPropertiesOfTestCase(test, testcase);

                group = tcase.getGroup();
                status = tcase.getStatus();
                String dateCrea = tcase.getTcDateCrea() != null ? tcase.getTcDateCrea() : "-- unknown --";
                // Define the list of country available for this test
                String countries = "";
                for (String c : countryListTestcase) {
                    countries += c + "-";
                }

                Application myApplication = null;
                if (tcase.getApplication() != null) {
                    myApplication = myApplicationService.convert(myApplicationService.readByKey(tcase.getApplication()));
                    appSystem = myApplication.getSystem();
                    SitdmossBugtrackingURL = myApplication.getBugTrackerUrl();
                } else {
                    appSystem = "";
                    SitdmossBugtrackingURL = "";
                }

                /**
                 * We can edit the testcase only if User role is TestAdmin or if
                 * role is Test and testcase is not WORKING
                 */
                boolean canEdit = false;
                if (request.getUserPrincipal() != null
                        && (request.isUserInRole("TestAdmin")) || ((request.isUserInRole("Test")) && !(status.equalsIgnoreCase("WORKING")))) {
                    canEdit = true;
                }

                boolean canDelete = false;
                if (request.getUserPrincipal() != null && request.isUserInRole("TestAdmin")) {
                    canDelete = true;
                }

            %>
            <br>
            <div id="editableContent">   
                <script type="text/javascript">                    
                    //add a loader to the editabl area - prevents save actions done by mistake
                    //TODO:FN this need to be refactored while the page conversion to the new standards
                    showLoader($("#editableContent"));
                </script>    
                <form method="post" name="UpdateTestCase"  id="UpdateTestCase"  action="UpdateTestCaseWithDependencies">
                    <table id="generalparameter" class="arrond"
                           <%if (tinf == false) {%> style="display : none" <%} else {%>style="display : table"<%}%> >
                        <tr>
                            <td class="separation">
                                <table  class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                    <tr>
                                        <td colspan="2" class="wob">
                                            <h4 style="color : blue">Test Information
                                            </h4>
                                        </td>
                                        <td id="wob">
                                        </td>
                                        <td id="wob">
                                        </td>
                                        <td id="wob" align="right">
                                            <input id="button2" style="height:18px; width:10px" type="button" value="-" onclick="javascript:setInvisible();">
                                        </td>
                                    </tr>    
                                    <tr id="header"> 
                                        <td name="labelTest"  class="wob" style="width: 100px">Test
                                        </td>
                                        <td name="labelTestCase" class="wob" style="width: 100px">TestCase
                                        </td>
                                        <td class="wob" style="width: 500px"><%out.print(docService.findLabelHTML("test", "description", "Description", myLang));%>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="wob">
                                            <fieldset class="wob">
                                                <select id="informationTest" name="informationTest" style="width: 250px;background-color: #DCDCDC" OnChange="findTestcaseByTest(this.value, '', 'informationTestCase')"
                                                        disabled="disabled">
                                                    <%  if (test.compareTo("") == 0) { %>
                                                    <option style="width: 250px" value="All">-- Choose Test --
                                                    </option>
                                                    <%  }

                                                        tests = testService.getListOfTest();
                                                        for (Test tst : tests) {%>
                                                    <option style="width: 250px;" class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" <%=test.compareTo(tst.getTest()) == 0 ? " SELECTED " : ""%>><%=tst.getTest()%>
                                                    </option>
                                                    <% }
                                                    %>
                                                </select>
                                                <input id ="hiddenInformationTest" name="informationTest" style="display:none" value="<%=test%>">
                                                <input type="text" style="width:250px; display:none" placeholder="Select test above or define new one" id="inputAddTestInSelectTest" onchange="addOptionInSelect('inputAddTestInSelectTest', 'informationTest')"/>
                                            </fieldset>
                                            <input id="informationInitialTest" type="hidden" name="informationInitialTest" value="<%=tcase.getTest()%>">
                                        </td>
                                        <td class="wob">
                                            <fieldset class="wob">
                                                <select id="informationTestCase" name="informationTestCase" style="width: 200px; background-color: #DCDCDC" disabled="disabled">
                                                    <% if (test.compareTo("") == 0) { %>
                                                    <option style="width: 250px" value="All">-- Choose Test First --
                                                    </option>
                                                    <%  } else {
                                                        List<TCase> tcList = testCaseService.findTestCaseByTest(test);
                                                        for (TCase tc : tcList) {%>
                                                    <option style="width: 250px;" class="font_weight_bold_<%=tc.getActive()%>" value="<%=tc.getTestCase()%>" <%=testcase.compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%>
                                                    </option>
                                                    <%  }
                                                        }%>
                                                </select>
                                                <input id ="hiddenInformationTestCase" style="display:none" name="informationTestCase" value="<%=testcase%>">
                                                <input id="inputAddTestCaseInSelectTestCase" type="text" style="width: 250px;display:none" 
                                                       placeholder="Define TestCase Identifiant different than these value" onblur="addOptionInSelect('inputAddTestCaseInSelectTestCase', 'informationTestCase')"/>
                                            </fieldset>
                                            <input id="informationInitialTestCase" type="hidden" name="informationInitialTestCase" value="<%=tcase.getTestCase()%>">
                                        </td>
                                        <td class="wob">
                                            <input id="informationTestDescription" style="width: 400px; background-color: #DCDCDC" name="informationTestDescription" readonly="readonly"
                                                   value="<%=testObject.getDescription()%>">
                                        </td>                                </tr>
                                </table>
                                <br>
                                <%  if (canDelete) {%>
                                <input type="button" id="deleteTC" name="deleteTC" value="delete" onclick="javascript:deleteTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                                <% }%>
                                <input type="button" id="exportTC" name="exportTC" value="exportTestCase" onclick="javascript:exportTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                                <input type="button" id="saveAs" name="saveAs" value="Save As" onclick="javascript:enableDuplicateField()">
                                <input type="button" style="display:none" id="FirstSaveChanges" name="SaveChanges" value="Save Changes" onclick="return saveAsClickHandler();">
                                <div id="deleteTCDiv">
                                </div>

                            </td>
                        </tr>
                        <tr>
                            <td class="separation">
                                <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                    <tr>
                                        <td class="wob">
                                            <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                                <tr>
                                                    <td class="wob" colspan="2"><h4 style="color : blue">TestCase Information</h4></td>
                                                </tr>
                                                <tr id="header">  
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "origine", "Origin", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "RefOrigine", "RefOrigine", myLang));%></td>
                                                    <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("testcase", "TCDateCrea", "Creation date", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "Creator", "creator", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "Implementer", "implementer", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "LastModifier", "lastModifier", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("project", "idproject", "Project", myLang));%></td>
                                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "ticket", "Ticket", myLang));%></td>
                                                    <td class="wob" style="width: 400px"><%out.print(docService.findLabelHTML("testcase", "Function", "Function", myLang));%></td>
                                                </tr>
                                                <tr>
                                                    <td class="wob"><input id="editOrigin" style="width: 90px;" name="editOrigin" value="<%=tcase.getOrigin()%>"></td>
                                                    <td class="wob"><input id="editRefOrigin" style="width: 90px;  background-color: #DCDCDC" name="editRefOrigin" value="<%=tcase.getRefOrigin()%>"></td>
                                                    <td class="wob"><%=dateCrea%></td>
                                                    <td class="wob"><input readonly="readonly" id="editCreator" style="width: 90px; background-color: #DCDCDC" name="editCreator" value="<%=tcase.getCreator()%>"></td>
                                                    <td class="wob"><input id="editImplementer" style="width: 90px;" name="editImplementer" value="<%=tcase.getImplementer() == null ? "" : tcase.getImplementer()%>"></td>
                                                    <td class="wob"><input readonly="readonly" id="editLastModifier" style="width: 90px; background-color: #DCDCDC" name="editLastModifier" value="<%=tcase.getLastModifier()%>"></td>
                                                    <td class="wob">
                                                        <% out.print(ComboProject(appContext, "editProject", "width: 90px", "editProject", "", tcase.getProject(), "", true, "", "No Project Defined."));%>
                                                    </td>
                                                    <td class="wob"><input id="editTicket" style="width: 90px;" name="editTicket" value="<%=tcase.getTicket() == null ? "" : tcase.getTicket()%>"></td>
                                                    <td class="wob"><input id="editFunction" style="width: 390px;" list="functions" name="editFunction" value="<%=tcase.getFunction() == null ? "" : tcase.getFunction()%>"></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                                <br>
                            </td>
                        </tr>
                        <tr>
                            <td class="separation">
                                <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                    <tr>
                                        <td class="wob">
                                            <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                                <tr><td class="wob"><h4 style="color : blue">TestCase Parameters</h4></td></tr>
                                                <tr id="header">
                                                    <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("application", "application", "Application", myLang));%></td>
                                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActiveQA", "RunQA", myLang));%></td>
                                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActiveUAT", "RunUAT", myLang));%></td>
                                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActivePROD", "RunPROD", myLang));%></td>
                                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("invariant", "PRIORITY", "Priority", myLang));%></td>
                                                    <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("invariant", "GROUP", "Group", myLang));%></td>
                                                    <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("testcase", "status", "Status", myLang));%></td>
                                                    <% for (Invariant country : countryListInvariant) {%>
                                                    <td class="wob" style="font-size : x-small ; width: 260px;"><%=country.getValue()%> <input type="hidden" name="testcase_country_all" value="<%=country.getValue()%>"></td>
                                                        <% } %>
                                                </tr>
                                                <tr>
                                                    <td class="wob"><select id="editApplication" name="editApplication" style="width: 140px"><%
                                                        for (Application app : myApplicationService.convert(myApplicationService.readBySystem(MySystem))) {
                                                            %><option value="<%=app.getApplication()%>"<%=tcase.getApplication().compareTo(app.getApplication()) == 0 ? " SELECTED " : ""%>><%=app.getApplication()%></option>
                                                            <% }%>
                                                        </select></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editRunQA", "width: 75px", "editRunQA", "runqa", "RUNQA", tcase.getRunQA(), "", null)%></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editRunUAT", "width: 75px", "editRunUAT", "runuat", "RUNUAT", tcase.getRunUAT(), "", null)%></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editRunPROD", "width: 75px", "editRunPROD", "runprod", "RUNPROD", tcase.getRunPROD(), "", null)%></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editPriority", "width: 75px", "editPriority", "priority", "PRIORITY", String.valueOf(tcase.getPriority()), "", null)%></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editGroup", "width: 140px", "editGroup", "editgroup", "GROUP", group, "", null)%></td>
                                                    <td class="wob"><%=ComboInvariant(appContext, "editStatus", "width: 140px", "editStatus", "editStatus", "TCSTATUS", status, "", null)%></td>
                                                    <%  for (Invariant countryL : countryListInvariant) {%>
                                                    <td class="wob" style="width:1px"><input value="<%=countryL.getValue()%>" type="checkbox" <% if (countryListTestcase.contains(countryL.getValue())) {%>  CHECKED  <% }%>
                                                                                             name="editTestCaseCountry" onclick="javascript:checkDeletePropertiesUncheckingCountry(this.value)"></td> 
                                                        <%} %>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                                <%
                                    String howTo = tcase.getHowTo();
                                    if (howTo == null || howTo.compareTo("null") == 0) {
                                        howTo = new String(" ");
                                    } else {
                                        howTo = howTo.replace(">", "&gt;");
                                    }
                                    String behavior = tcase.getDescription();
                                    if (behavior == null || behavior.compareTo("null") == 0) {
                                        behavior = new String(" ");
                                    } else {
                                        behavior = behavior.replace(">", "&gt;");
                                    }
                                %> 
                                <table>
                                    <tr>
                                        <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                            <table class="wob"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                                <tr id="header">
                                                    <td class="wob" style="width: 1200px"><%out.print(docService.findLabelHTML("testcase", "description", "Description", myLang));%></td>
                                                </tr><tr>
                                                    <td class="wob"><input id="editDescription" style="width: 1200px;" name="editDescription"
                                                                           value="<%=tcase.getShortDescription()%>"></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr><tr>
                                        <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                            <table>   
                                                <tr id="header">
                                                    <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "BehaviorOrValueExpected", "Value Expected", myLang));%></td>
                                                    <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "HowTo", "HowTo", myLang));%></td>
                                                </tr>
                                                <tr>
                                                    <td class="wob" style="text-align: left; border-collapse: collapse">

                                                        <textarea id="value" rows="9" style="width: 600px;" name="BehaviorOrValueExpected"
                                                                  onchange="trackChanges(this.value, '<%=URLEncoder.encode(behavior, "UTF-8")%>', 'submitButtonChanges')" ><%=behavior%></textarea>
                                                        <input type="hidden" id="valueDetail" name="valueDetail" value="">
                                                    </td>
                                                    <td class="wob">
                                                        <textarea id="howto" rows="9" style="width: 600px;" name="HowTo"
                                                                  onchange="trackChanges(this.value, '<%=URLEncoder.encode(howTo, "UTF-8")%>', 'submitButtonChanges')" ><%=howTo%></textarea>
                                                        <input id="howtoDetail" name="howtoDetail" type="hidden" value="" />
                                                    </td>
                                                </tr>
                                            </table><br>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <%  // We are getting here the last execution that was done on the testcase with its associated status.
                            String LastExeMessage;
                            LastExeMessage = "<i>Never Executed</i>";
                            if (tce != null) {
                                LastExeMessage = "Last <a width : 390px ; href=\"ExecutionDetail.jsp?id_tc=" + tce.getId() + "\">Execution</a> was ";
                                if (tce.getControlStatus().compareToIgnoreCase("OK") == 0) {
                                    LastExeMessage = LastExeMessage + "<a style=\"color : green\">" + tce.getControlStatus() + "</a>";
                                } else {
                                    LastExeMessage = LastExeMessage + "<a style=\"color : red\">" + tce.getControlStatus() + "</a>";
                                }
                                LastExeMessage = LastExeMessage + " in "
                                        + tce.getEnvironment() + " in "
                                        + tce.getCountry() + " on "
                                        + new Date(tce.getEnd())
                                        + "<a onclick='return checkUndefinedProperties();'  width : 390px ; href=\"RunTests.jsp?Test=" + test + "&TestCase=" + testcase + "&MySystem=" + appSystem
                                        + "&Country=" + tce.getCountry() + "&Environment=" + tce.getEnvironment() + "\"><i> (Run it again) </i></a>";
                            }
                            if ((tcase.getBugID() != null)
                                    && (tcase.getBugID().compareToIgnoreCase("") != 0)
                                    && (tcase.getBugID().compareToIgnoreCase("null") != 0)) {
                                SitdmossBugtrackingURL = SitdmossBugtrackingURL.replaceAll("%BUGID%", tcase.getBugID());
                            }
                        %>
                        <tr>
                            <td class="separation">
                                <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                    <tr>
                                        <td colspan="9" class="wob">
                                            <h4 style="color : blue">Activation Criterias</h4>
                                    <tr id="header">
                                        <td class="wob" style="width: 50px"><%out.print(docService.findLabelHTML("testcase", "tcactive", "Active", myLang));%></td>
                                        <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "FromBuild", "", myLang));%></td>
                                        <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "FromRev", "", myLang));%></td>
                                        <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ToBuild", "", myLang));%></td>
                                        <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "ToRev", "", myLang));%></td>
                                        <td class="wob" style="width: 390px"><%out.print(docService.findLabelHTML("page_testcase", "laststatus", "", myLang));%></td>
                                        <td class="wob" style="width: 70px"><%out.print(docService.findLabelHTML("testcase", "BugID", "", myLang));%></td>
                                        <td class="wob" style="width: 50px"><%out.print(docService.findLabelHTML("page_testcase", "BugIDLink", "", myLang));%></td>
                                        <td class="wob" style="width: 80px"><%out.print(docService.findLabelHTML("testcase", "TargetBuild", "", myLang));%></td>
                                        <td class="wob" style="width: 80px"><%out.print(docService.findLabelHTML("testcase", "TargetRev", "", myLang));%></td>
                                    </tr>
                                    <tr>
                                        <td class="wob"><%=ComboInvariant(appContext, "editTcActive", "width: 50px", "editTcActive", "active", "TCACTIVE", tcase.getActive(), "", null)%></td>
                                        <td class="wob">
                                            <select id="editFromBuild" name="editFromBuild" class="active" style="width: 70px" >
                                                <%  String fromBuild = ParameterParserUtil.parseStringParam(tcase.getFromSprint(), "");
                                                    String fromRev = ParameterParserUtil.parseStringParam(tcase.getFromRevision(), "");
                                                    String toBuild = ParameterParserUtil.parseStringParam(tcase.getToSprint(), "");
                                                    String toRev = ParameterParserUtil.parseStringParam(tcase.getToRevision(), "");
                                                    String targetBuild = ParameterParserUtil.parseStringParam(tcase.getTargetSprint(), "");
                                                    String targetRev = ParameterParserUtil.parseStringParam(tcase.getTargetRevision(), "");
                                                %>
                                                <option style="width: 100px" value="" <%=fromBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <% for (BuildRevisionInvariant myBR : listBuildRev) {%>
                                                <option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% }%>
                                            </select>
                                        </td>
                                        <td class="wob">
                                            <select id="editFromRev" name="editFromRev" class="active" style="width: 50px" >
                                                <option style="width: 100px" value="" <%=fromRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <%
                                                    listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 2));
                                                    for (BuildRevisionInvariant myBR : listBuildRev) {
                                                %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% }%>
                                            </select>
                                        </td>
                                        <td class="wob">
                                            <select id="editToBuild" name="editToBuild" class="active" style="width: 70px" >
                                                <option style="width: 100px" value="" <%=toBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <%
                                                    listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 1));
                                                    for (BuildRevisionInvariant myBR : listBuildRev) {
                                                %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% }%>
                                            </select>
                                        </td>
                                        <td class="wob">
                                            <select id="editToRev" name="editToRev" class="active" style="width: 50px" >
                                                <option style="width: 100px" value="" <%=toRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <%
                                                    listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 2));
                                                    for (BuildRevisionInvariant myBR : listBuildRev) {
                                                %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% }
                                                %></select>
                                        </td>
                                        <td class="wob"><%=LastExeMessage%></td>
                                        <td class="wob"><input id="BugID" style="width: 70px;" name="editBugID" value="<%=tcase.getBugID() == null ? "" : tcase.getBugID()%>"></td>
                                        <td class="wob"><% if (tcase.getBugID() != null) {%><a href="<%= SitdmossBugtrackingURL%>"><%=tcase.getBugID()%></a><%}%></td>
                                        <td class="wob">
                                            <select id="editTargetBuild" name="editTargetBuild" class="active" style="width: 70px" >
                                                <option style="width: 100px" value="" <%=targetBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <%
                                                    listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 1));
                                                    for (BuildRevisionInvariant myBR : listBuildRev) {
                                                %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% }%>
                                            </select>
                                        </td>
                                        <td class="wob">
                                            <select id="editTargetRev" name="editTargetRev" class="active" style="width: 50px" >
                                                <option style="width: 100px" value="" <%=targetRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                                <%
                                                    listBuildRev = buildRevisionInvariantService.convert(buildRevisionInvariantService.readBySystemLevel(MySystem, 2));
                                                    for (BuildRevisionInvariant myBR : listBuildRev) {
                                                %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                <% } %>
                                            </select>
                                        </td>
                                    </tr>
                                </table>
                                <table>
                                    <tr id ="header">
                                        <td class="wob" style="width: 650px"><%out.print(docService.findLabelHTML("testcase", "comment", "Comment", myLang));%></td>
                                    </tr>
                                    <tr> 
                                        <td class="wob"><input id="comment" style="width: 1200px;" name="editComment" 
                                                               value="<%=tcase.getComment() == null ? "" : tcase.getComment()%>">
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <%  if (canEdit) {%>
                        <tr>
                            <td class="wob">
                                <table>
                                    <tr>
                                        <td class="wob"><input type="submit" name="submitInformation" value="Save TestCase Info" id="submitButtonInformation"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <% }%>
                        <datalist id="functions">
                        </datalist>
                    </table>
                    <table id="parametergeneral" class="arrond" <%if (tinf == false) {%> style="display : table" <%} else {%>style="display : none"<%}%> >
                        <tr><td id="wob" style="width: 150px"><h4 style="color : blue">General Parameters:</h4></td>
                            <td id="wob" style="width: 150px">APP: [<%=tcase.getApplication()%>]  </td>
                            <td id="wob" style="width: 160px">GROUP: [<%=tcase.getGroup()%>]  </td>
                            <td id="wob" style="width: 200px">STATUS: [<%=tcase.getStatus()%>]  </td>
                            <td id="wob" style="width: 60px">ACT: [<%=tcase.getActive()%>]  </td>
                            <td id="wob" style="width: 170px">Last Exe: [<%=LastExeMessage%>]  </td>
                            <td id="wob" style="width: 300px">Countries: [<%=countries%>]</td>
                            <td id="wob" align="right"><input id="button1" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisible();"></td>
                        </tr>
                    </table>
                    <br>
                    <%
                        int i = 1;
                        int j = 1;
                        int rowNumber = 0;
                    %>
                    <script>
                        $(document).ready(function() {
                            var cookie = GetCookie('TestCasePageDefaultView');
                            $("#selectView option").each(function() {
                                if (this.value === cookie) {
                                    this.selected = 'selected';
                                }
                            });
                            customizeView(cookie);
                        });
                    </script>
                    <div id="AutomationScriptDiv" class="arrond" style="display : inline-block">
                        <div id="AutomationScriptFirstLine" style="clear:both; height:30px">
                            <div id="AutomationScriptTitle" style="float:left">
                                <h3>TestCase Detailed Description</h3>
                            </div>
                            <div id="AutomationScriptFunctionalButtonDiv" style="float:left;margin-left:30px">
                                <select id="selectView" style="float:left; height:20px; width:100px" onchange="javascript:customizeView(this.value);
                                        SetCookie('TestCasePageDefaultView', this.value)">
                                    <option value="full">Full View</option>
                                    <option value="onlyStep">Show Only Step</option>
                                    <option value="hideUseStep">Hide Use Step</option>
                                    <option value="fonctionalView">Functional Information Only</option>
                                    <option value="technicalView">Technical Information Only</option>
                                </select>
                            </div>
                        </div>
                        <div><input type="button" class="buttonSaveChanges" id="displayLib" value="show/hide Lib" onclick="showLib()"></div>

                        <div id="StepsMainDiv" style="width:100%;float:left">
                            <div id="StepsDivUnderTitle" style="width:97%;clear:both">
                                <div id="StepLibDiv" style="margin-top:10px; background-color:white; width:0%; float:left; display:none; ">
                                    <div style="width:13%;position:fixed;background-color:white;height:500px; overflow: auto">    
                                        <%
                                            List<TestCaseStep> tcsListOfUseStep = tcsService.getStepLibraryBySystem(MySystem);
                                            String testOfLib = "";
                                            for (TestCaseStep tcs : tcsListOfUseStep) {
                                                if (!tcs.getTest().equals(testOfLib)) {
                                                    testOfLib = tcs.getTest();
                                        %>
                                        <p style="font-size:10px; font-weight:bold" onclick="$('.libLineDiv[data-testLib=\'<%=testOfLib%>\']').toggleClass('libHideLineDiv');"><%=tcs.getTest()%>
                                        </p>
                                        <%
                                            }
                                        %>
                                        <div class="libLineDiv" data-testLib="<%=testOfLib%>" style="border-style: solid; border-width:thin ; border-color:#CCCCCC;" id="<%=tcs.getTest()%><%=tcs.getTestCase()%><%=tcs.getStep()%>" data-test="<%=tcs.getTest()%>"
                                             data-testcase="<%=tcs.getTestCase()%>" data-step="<%=tcs.getStep()%>" onmousedown="showTargetDiv()" onmouseup="hideTargetDiv()" draggable="true"  ondragstart="drag(event, this)" >
                                            <p style="margin-left:20px; font-size:10px; font-style: italic;font-weight: bold; color:dodgerblue"><%=tcs.getDescription()%></p></div>
                                            <%}%>
                                    </div>

                                </div>
                                <div id="StepsRightDiv" style="width:100%;float:left; margin:1%;">
                                    <div class="saveButtonDiv" ondragover="insertTCS(event, '0')" ondrop="drop(event, null)" style="display:block;clear:both;">
                                        <div style="float:left;height:25px" id="wob">
                                            <input name="divButtonSaveChange" style="display:none" value="Save Changes" onclick="submitTestCaseModificationNew('');"
                                                   id="submitButtonAction" name="submitChanges" class="buttonSaveChanges"
                                                   type="button"  >
                                        </div>
                                        <div id="ButtonDiv0" style="float:left; height:20px">
                                            <input type="button" value="Add Step" title="Add Step" class="buttonSaveChanges"
                                                   onclick="addTCSCNew('StepNumberDiv0', null)">
                                        </div>
                                        <div id="StepsEndDiv0" style="display:none; width:800px;"></div>
                                    </div>
                                    <div id="StepNumberDiv0" style="float:left;">
                                    </div>
                                    <input style="display:none" value="<%=MySystem%>">
                                    <%=ComboInvariant(appContext, "action_action_temp", "width: 136px; display:none", "action_action_temp", "wob", "ACTION", null, "", null)%>
                                    <%=ComboInvariant(appContext, "actions_action_", "width: 150px;visibility:hidden", "actions_action_", "actions_action_", "ACTION", "", "", null)%>

                                    <%
                                        int incrementStep = 0;
                                        List<String> listOfImportedProperties = new ArrayList<String>();
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
                                            String classStep = "ActionOfNormalStep";

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
                                                classStep = "ActionOfUseStep";
                                            }
                                            List<TestCaseStepAction> tcsaList = tcsaService.getListOfAction(testForQuery, testcaseForQuery, stepForQuery);

                                    %>
                                    <div id="listOfTestCaseUsingStep<%=incrementStep%>" style="display:none;">
                                        <%                                    if (!tcsUsingThisStep.isEmpty()) {
                                                for (TestCaseStep TcUsed : tcsUsingThisStep) {
                                        %>    
                                        <ul><%=TcUsed.getTest()%> - <%=TcUsed.getTestCase()%></ul><%    }

                                            }%>
                                    </div>
                                    <div style="display:block; clear:both; margin-top:5px">
                                        <div id="StepFirstLineDiv<%=incrementStep%>" class="StepHeaderDiv" style="width:100%; height:40px">
                                            <div id="StepComboDeleteDiv" style="float:left; width: 30px; text-align: center; height:100%">
                                                <a name="stepAnchor_<%=incrementStep%>"></a>
                                                <a name="stepAnchor_step<%=tcs.getStep()%>"></a>
                                                <%if (!stepusedByAnotherTest) {%>
                                                <img style="margin-top:12px" src="images/bin.png" id="img_delete_step_<%=incrementStep%>" onclick="checkDeleteBox('img_delete_step_<%=incrementStep%>', 'step_delete_<%=incrementStep%>', 'StepFirstLineDiv<%=incrementStep%>', 'StepHeaderDiv')">
                                                <input type="checkbox" name="step_delete_<%=incrementStep%>" id="step_delete_<%=incrementStep%>" style="display:none;margin-top:15px;font-weight: bold; width:20px"
                                                       value="<%=tcs.getStep()%>">
                                                <%}%>
                                                <%if (stepusedByAnotherTest) {%>
                                                <div id="StepWarnAlreadyInUse" title="Step In Use By Other Testcase" style="float:left;width:10px;height:100%;display:inline-block; background-color:yellow;"
                                                     onclick="showTestCaseUsingThisStep(<%=incrementStep%>)">
                                                </div>
                                                <%}%>
                                                <%if (useStep) {%>
                                                <div id="StepWarnAlreadyInUse" title="Step Using another step" style="float:left;width:10px;height:100%;display:inline-block; background-color:black;">
                                                </div>
                                                <%}%>
                                                <input style="display:none" name="step_InUseInOtherTestCase_<%=incrementStep%>" value="<%=stepusedByAnotherTest ? "Y" : "N"%>">
                                                <input style="display:none" name="step_increment" value="<%=incrementStep%>">
                                                <input id="incrementStepNumber" value="<%=incrementStep%>" style="display:none">

                                            </div>
                                            <%if (!useStep) {%>
                                            <div style="margin-top:10px;width:3%;float:left;color:blue;font-weight:bold;font-size:10px ;font-family: Trebuchet MS; background-color: transparent">

                                                <div style="width:100%;clear:both;color:blue;font-weight:bold;font-size:10px ;font-family: Trebuchet MS; background-color: transparent">
                                                    <div><div><img src="images/addAction.png" style="width:15px;height:15px" title="Add Action"
                                                                   data-fieldtype="addActionButton"
                                                                   onclick="addTCSANew('BeforeFirstAction<%=tcs.getStep()%>', '<%=incrementStep%>', null);
                                                                           enableField('submitButtonAction');">
                                                        </div></div>


                                                </div>

                                            </div>
                                            <%}%>
                                            <div id="StepNumberDiv" style="float:left; width:10%">
                                                &nbsp;&nbsp;Step&nbsp;&nbsp;
                                                <input value="<%=incrementStep%>" name="step_number_<%=incrementStep%>" data-fieldtype="stepNumber" style="margin-top:15px;font-weight: bold; width:20px;background-color:transparent; border-width:0px">
                                                <input type="hidden" name="step_technical_number_<%=incrementStep%>" value="<%=tcs.getStep()%>">
                                                <input type="hidden" name="initial_step_number_<%=incrementStep%>" id="initial_step_number_<%=incrementStep%>" value="<%=tcs.getStep()%>">
                                            </div>
                                            <div id="StepDescDiv" style="width:30%;float:left;margin-top:10px">
                                                <div><div><input maxlength="150" style="float:right;font-weight: bold; width: 100%;background-color:transparent; font-weight:bold;font-size:14px ;font-family: Trebuchet MS; color:#333333; border-color:#EEEEEE;border-style:solid; border-width:thin"
                                                                 placeholder="Description" data-fieldtype="Description" name="step_description_<%=incrementStep%>" id="step_description_<%=incrementStep%>" value="<%=tcs.getDescription()%>">
                                                    </div></div></div>
                                            <div id="StepUseStepDiv" style="float:left">UseStep
                                                <input type="checkbox" id="step_useStep_<%=incrementStep%>" name="step_useStep_<%=incrementStep%>" data-step-number="<%=incrementStep%>" style="margin-top:12.5px;font-weight: bold; width:20px" 
                                                       <% if (tcs.getUseStep().equals("Y")) {%>
                                                       checked value="Y"
                                                       <%}else{%>
                                                            value="N"
                                                       <%
                                                       }   
                                                        if (tcs.getInLibrary().equals("Y")) {%>
                                                           disabled="disabled" title ='This step uses another step!'
                                                       <%}%>> 
                                                <input type="hidden" name="initUseStep_<%=incrementStep%>"
                                                       <% if (tcs.getUseStep().equals("Y")) {%>
                                                       value="Y"
                                                       <%}else{%> 
                                                       value="N"
                                                       <%}%> 
                                                       id="initUseStep_<%=incrementStep%>" />
                                                <input type="hidden" name="isToCopySteps_<%=incrementStep%>" value="N" id="isToCopySteps_<%=incrementStep%>" />
                                                <input type="hidden" name="step_useStepChanged_<%=incrementStep%>" value="N" id="step_useStepChanged_<%=incrementStep%>" />
                                                       
                                            </div>
                                            <% if (tcs.getUseStep().equals("Y")) {%>
                                            <div id="StepCopiedFromDiv<%=incrementStep%>" style="float:left">
                                                <p style="margin-top:15px;"> Copied from : </p>
                                            </div>
                                            <div id="StepUseStepTestDiv<%=incrementStep%>" style="float:left; width:10%">
                                                <select id="step_useStepTest_<%=incrementStep%>" name="step_useStepTest_<%=incrementStep%>" style="width: 100%;margin-top:7.5px;font-weight: bold;" 
                                                        OnChange="findStepBySystemTest(this, '<%=MySystem%>', $('#step_useStepTestCase_<%=incrementStep%>'), 
                                                                    $('#load_step_inLibrary_<%=incrementStep%>'), '')">
                                                    <%  if (tcs.getUseStepTest() == null || tcs.getUseStepTest().equals("")) { %>
                                                    <option style="width: 200px" value="">-- Choose Test --
                                                    </option>
                                                    <%  }
                                                        List<TestCaseStep> tcsLib = tcsService.getStepLibraryBySystem(MySystem);
                                                        Set<String> tList = new HashSet();
                                                        HashMap tcListByTc = new HashMap();
                                                        List<String> tcList = new ArrayList();
                                                        String previousTc = "";
                                                        for (TestCaseStep tcsUnit : tcsLib) {
                                                            tList.add(tcsUnit.getTest());
                                                            if (!tcsUnit.getTest().equals(previousTc)) {
                                                                if (!previousTc.equals("")) {
                                                                    tcListByTc.put(previousTc, tcList);
                                                                }
                                                                tcList = new ArrayList();
                                                            }
                                                            tcList.add(tcsUnit.getTestCase());
                                                        }
                                                        String testCombo = test;
                                                        for (String tst : tList) {
                                                            String selected = "";
                                                            if (tcs.getUseStepTest() != null && tcs.getUseStepTest().compareTo(tst) == 0) {
                                                                testCombo = tst;
                                                                selected = " SELECTED ";
                                                            }
                                                    %>
                                                    <option style="width: 200px;" value="<%=tst%>" <%=selected%>><%=tst%>
                                                    </option>
                                                    <% }
                                                    %>
                                                </select>
                                            </div>

                                            <div id="StepUseStepTestCaseDiv<%=incrementStep%>" style="float:left;width:10%">
                                                <select name="step_useStepTestCase_<%=incrementStep%>" style="width: 100%;margin-top:7.5px;font-weight: bold;" 
                                                        OnChange="findStepBySystemTestTestCase($('#step_useStepTest_<%=incrementStep%>'), this, '<%=MySystem%>', 
                                                                    $('#step_useStepStep_<%=incrementStep%>'), $('#load_step_inLibrary_<%=incrementStep%>'), '', false)"
                                                        id="step_useStepTestCase_<%=incrementStep%>">
                                                    <%  if (tcs.getUseStepTestCase().equals("")) { %>
                                                    <option style="width: 400px" value="">---</option>
                                                    <%  } else {
                                                        AnswerList anstcaseList = testCaseService.readTestCaseByStepsInLibrary(testCombo);
                                                        List<TCase>tcaseList =  anstcaseList.getDataList();
                                                        for (TCase tc : tcaseList) {%>
                                                    <option style="width: 400px;" class="font_weight_bold_<%=tc.getActive()%>" value="<%=tc.getTestCase()%>" <%=tcs.getUseStepTestCase().compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%> [<%=tc.getApplication()%>] : <%=tc.getShortDescription()%>
                                                    </option>
                                                    <% }
                                                        }%>
                                                </select>
                                            </div>
                                            <div id="StepUseStepStepDiv<%=incrementStep%>" style="float:left; width:10%">
                                                <select name="step_useStepStep_<%=incrementStep%>" style="width: 100%;margin-top:7.5px;font-weight: bold;" 
                                                        id="step_useStepStep_<%=incrementStep%>" data-step-number="<%=incrementStep%>">
                                                    <%  if (tcs.getUseStepTest().equals("") || tcs.getUseStepTestCase().equals("")) { %>
                                                    <option style="width: 400px" value="">---</option>
                                                    <%  } else {
                                                        List<TestCaseStep> tcstepList = tcsService.getListOfSteps(tcs.getUseStepTest(), tcs.getUseStepTestCase());
                                                        for (TestCaseStep tcstep : tcstepList) {
                                                            if(tcstep.getInLibrary().equalsIgnoreCase("Y")){
                                                        %>

                                                    <option style="width: 400px;" value="<%=tcstep.getStep()%>" <%=tcs.getUseStepStep().compareTo(tcstep.getStep()) == 0 ? " SELECTED " : ""%>><%=tcstep.getSort()%> : <%=tcstep.getDescription()%>
                                                    </option>
                                                    <% }
                                                        }
                                                        }%>
                                                </select>
                                            </div>
     
                                            <div id="StepUseStepLinkDiv<%=incrementStep%>" class="StepUseStepLinkDiv">
                                                <a target="_blank" id="linkEditUsedStep<%=incrementStep%>" href="TestCase.jsp?Test=<%=tcs.getUseStepTest()%>&TestCase=<%=tcs.getUseStepTestCase()%>#stepAnchor_step<%=tcs.getUseStepStep()%>">
                                                    <span class="glyphicon glyphicon-new-window"></span>
                                                </a>
                                                <%if(useStep){%>    
                                                    <button type="button" class="btn btn-xs" id="load_step_inLibrary_<%=incrementStep%>" data-step-number="<%=incrementStep%>" disabled="disabled">
                                                        <span class="glyphicon glyphicon-refresh"></span>
                                                    </button>
                                                    <button type="button" class="btn btn-xs" id="reset_step_inLibrary_<%=incrementStep%>" data-step-number="<%=incrementStep%>" disabled="disabled">
                                                        <span class="glyphicon glyphicon-remove"></span>
                                                    </button>
                                                <%}%>
                                            </div>
                                            <%}%>
                                            <div style="margin-top:15px;float:right;width:5%">Library
                                                    <input type="hidden"  name="step_inLibrary_<%=incrementStep%>" value="<%=tcs.getInLibrary()%>" />
                                                    <input type="checkbox" style="font-weight: bold;" name="step_inLibrary_<%=incrementStep%>" id="step_inLibrary_<%=incrementStep%>"
                                                       data-step-number="<%=incrementStep%>" 
                                                       <%if (tcs.getUseStep().equals("Y")) {%>
                                                            disabled title ='This step can not be used as library, because it uses another step!'
                                                       <%}else
                                                       if (tcs.getInLibrary().equals("Y")) {%>
                                                       checked                                                       
                                                       <%}%>   
                                                       value="<%=tcs.getInLibrary()%>"
                                                       <%if (stepusedByAnotherTest) { %>                                                       
                                                            checked disabled title ='This step is being used by another step(s)!'
                                                       <%}%>/>
                                            </div>

                                        </div>
                                        <div id="StepsBorderDiv<%=incrementStep%>" style="display:block;margin-top:0px;border-style: solid; border-width:thin ; border-color:#EEEEEE; clear:both;">
                                            <div id="StepDetailsDiv" style="clear:both">
                                                <div id="ActionControlDivUnderTitle" style="height:100%;width:100%;clear:both">
                                                    <div id="Action<%=tcs.getStep()%>" style="height:100%; width:100%;text-align: left; clear:both" >
                                                        <div id="BeforeFirstAction<%=tcs.getStep()%>"></div>
                                                        <%
                                                            String actionColor = "";
                                                            String actionFontColor = "#333333";
                                                            int incrementAction = 0;
                                                            for (TestCaseStepAction tcsa : tcsaList) {
                                                                incrementAction++;
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
                                                        <div id="StepListOfActionDiv<%=incrementStep%><%=incrementAction%>" class="RowActionDiv <%=classStep%>" style="margin-top:0px;display:block;height:50px;width:100%;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                                                            <div name="actionRow_color_<%=incrementStep%>" style="background-color:blue; width:8px;height:100%;display:inline-block;float:left">
                                                            </div>
                                                            <div style="display:inline-block;float:left;width:2%;height:100%;text-align:center">
                                                                <% if (!useStep) {%>
                                                                <img style="margin-top:12px" src="images/bin.png" id="img_delete_<%=incrementStep%>_<%=incrementAction%>" onclick="checkDeleteBox('img_delete_<%=incrementStep%>_<%=incrementAction%>', 'action_delete_<%=incrementStep%>_<%=incrementAction%>', 'StepListOfActionDiv<%=incrementStep%><%=incrementAction%>', 'RowActionDiv')">
                                                                <input  class="wob" type="checkbox" data-action="delete_action" name="action_delete_<%=incrementStep%>_<%=incrementAction%>" style="display:none; margin-top:20px; background-color: transparent"
                                                                        id="action_delete_<%=incrementStep%>_<%=incrementAction%>" value="<%=tcsa.getStep() + "_" + tcsa.getSequence()%>" <%=isReadonly%>>
                                                                <%}%>
                                                                <input type="hidden" name="action_increment_<%=incrementStep%>" value="<%=incrementAction%>" >
                                                                <input type="hidden" name="action_step_<%=incrementStep%>_<%=incrementAction%>" data-fieldtype="stepNumber" value="<%=incrementStep%>" >
                                                            </div>
                                                            <div style="height:100%;width:3%;float:left;display:inline-block">
                                                                <%if (!useStep) {%>
                                                                <div style="margin-top: 5px;height:50%;width:100%;clear:both;display:inline-block">
                                                                    <img src="images/addAction.png" style="width:15px;height:15px" title="Add Action" data-fieldtype="addActionButton"
                                                                         onclick="addTCSANew('DivActionEndOfAction<%=incrementStep%><%=incrementAction%>', '<%=incrementStep%>', this)">
                                                                </div>
                                                                <div style="margin-top:-15px;height:50%;width:100%;clear:both;display:inline-block">
                                                                    <img src="images/addControl.png" style="width:15px;height:15px" title="Add Control" data-fieldtype="addControlButton"
                                                                         onclick="addTCSACNew('StepListOfActionDiv<%=incrementStep%><%=incrementAction%>', '<%=incrementStep%>', '<%=incrementAction%>', this)">
                                                                </div>
                                                                <%}%>
                                                            </div>
                                                            <div style="height:100%;width:4%;display:inline-block;float:left">
                                                                <input class="wob" style="width: 40px; font-weight: bold; background-color: transparent; height:100%; color:<%=actionFontColor%>"
                                                                       value="<%=incrementAction%>" data-fieldtype="action_<%=incrementStep%>" data-field="sequence"
                                                                       name="action_sequence_<%=incrementStep%>_<%=incrementAction%>" id="action_sequence_<%=incrementStep%>_<%=incrementAction%>" <%if (useStep) {%>readonly<%}%>/>
                                                                <input type="hidden" name="action_technical_sequence_<%=incrementStep%>_<%=incrementAction%>" value="<%=tcsa.getSequence()%>">
                                                            </div>
                                                            <div style="height:100%;width:80%;float:left; display:inline-block">
                                                                <div class="functional_description" style="height:20px;display:inline-block;clear:both;width:100%; background-color: transparent">

                                                                    <div style="float:left; width:80%">
                                                                        <div style="float:left;width:80px; "><p name="labelTestCaseStepActionDescription" style="float:right;font-weight:bold;" link="white" >Description</p>
                                                                        </div>
                                                                        <input class="wob" class="functional_description" data-fieldtype="Description" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#333333; width: 80%; font-weight:bold;font-size:12px ;font-family: Trebuchet MS; "
                                                                               value="<%=tcsa.getDescription()%>" placeholder="Description"
                                                                               name="action_description_<%=incrementStep%>_<%=incrementAction%>"
                                                                               id="action_description_<%=incrementStep%>_<%=incrementAction%>"<%=isReadonly%>
                                                                               onchange="showChangedRow(this.parentNode.parentNode.parentNode.parentNode)"
                                                                               >

                                                                    </div>
                                                                </div>
                                                                <div style="display:inline-block;clear:both; height:15px;width:100%;background-color:transparent">
                                                                    <div class="technical_part" style="width: 30%; float:left; background-color: transparent">
                                                                        <div style="float:left;width:80px; "><p name="labelTestCaseStepActionAction" style="float:right;font-weight:bold;" link="white" >Action</p>
                                                                        </div>
                                                                        <%if (!useStep) {%>
                                                                            <%=ComboInvariant(appContext, "action_action_" + incrementStep + "_" + incrementAction, "width:50%;border: 1px solid white; color:#888888", "action_action_" + incrementStep + "_" + incrementAction, "wob", "ACTION", 
                                                                                    tcsa.getAction(), "showChangedRow(this.parentNode.parentNode.parentNode.parentNode)", null)%>
                                                                        <%}else{
                                                                            String action_action_id = "action_action_" + incrementStep + "_" + incrementAction;
                                                                            %>
                                                                            <input id="<%=action_action_id%>" value="<%=tcsa.getAction()%>" readonly style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; color:#999999" />
                                                                        <%}%>
                                                                    </div>
                                                                    <div class="technical_part" style="width: 35%; float:left; background-color: transparent">
                                                                        <div style="float:left;"><p name="labelTestCaseStepActionObject" style="float:right;font-weight:bold;" link="white" >Object</p>
                                                                         </div>
                                                                        <input style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; color:#999999"
                                                                               value="<%=tcsa.getObject().replace("\"","&quot;")%>"
                                                                               onchange="showChangedRow(this.parentNode.parentNode.parentNode.parentNode)" 
                                                                               id="action_object_<%=incrementStep%>_<%=incrementAction%>" 
                                                                               name="action_object_<%=incrementStep%>_<%=incrementAction%>" <%=isReadonly%>>
                                                                        <% if (tcsa.getObject().startsWith("picture=")){%>
                                                                        <div>
                                                                            <%if(!useStep){%>
                                                                                <img class="wob" width="45" height="35" src="<%=tcsa.getObject().split("picture=")[1]%>" 
                                                                                 onclick="showPicture('<%=tcsa.getScreenshotFilename()%>', <%=incrementStep%>, <%=incrementAction%>, null)"/>                                                                                
                                                                            <%}else{%>
                                                                                <img class="wob" width="45" height="35" src="<%=tcsa.getObject().split("picture=")[1]%>" />
                                                                                
                                                                            <%}%>
                                                                            </div>
                                                                        <%}%>
                                                                    </div>
                                                                    <div class="technical_part" style="width: 20%; float:left; background-color:transparent">
                                                                        <div style="float:left;"><p name="labelTestCaseStepActionProperty" style="float:right;font-weight:bold;" link="white" >Property</p>
                                                                        </div>
                                                                        <input  class="wob property_value" style="width:75%;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#888888"
                                                                                value="<%=tcsa.getProperty()%>"
                                                                                <%if (useStep) {
                                                                                    //if is the step was imported, then adds the propertu to the list
                                                                                    listOfImportedProperties.add(tcsa.getProperty());
                                                                                    //defines each attribute related to the test case step that was imported
                                                                                %>
                                                                                data-usestep-test="<%=testForQuery%>"
                                                                                data-usestep-testcase="<%=testcaseForQuery%>"
                                                                                data-usestep-step="<%=stepForQuery%>"
                                                                                <%}
                                                                                else{
                                                                                    //verify if the property is currently in the list of imported properties
                                                                                    if(listOfImportedProperties.contains(tcsa.getProperty())){
                                                                                    //defines one attribute that will distinguish if the imported property
                                                                                    //from the undefined property
                                                                                    %>              
                                                                                        data-imported-property = "true"
                                                                                    <%                                                                              
                                                                                    }
                                                                                }                                                                            
                                                                                %>
                                                                                onchange="showChangedRow(this.parentNode.parentNode.parentNode.parentNode)" 
                                                                                id="action_property_<%=incrementStep%>_<%=incrementAction%>"
                                                                                name="action_property_<%=incrementStep%>_<%=incrementAction%>" <%=isReadonly%>>
                                                                    </div>
                                                                    <div class="technical_part" style="width: 15%; float:left; background-color: transparent">
                                                                        <div style="float:left;width:60px; "><p name="labelTestCaseStepActionForce" style="float:right;font-weight:bold;" link="white" >Force RC</p>
                                                                        </div>
                                                                        <%if (!useStep) {%>
                                                                            <%=ComboInvariant(appContext, "action_forceexestatus_" + incrementStep + "_" + incrementAction, "width:50%;border: 1px solid white; color:#888888", "action_forceexestatus_" + incrementStep + "_" + incrementAction, "wob", "ACTIONFORCEEXESTATUS", 
                                                                                    tcsa.getForceExeStatus(), "showChangedRow(this.parentNode.parentNode.parentNode.parentNode)", null)%>
                                                                        <%}else{
                                                                            String action_action_id = "action_forceexestatus_" + incrementStep + "_" + incrementAction;
                                                                            %>
                                                                            <input id="<%=action_action_id%>" value="<%=tcsa.getForceExeStatus()%>" readonly style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; color:#999999" />
                                                                        <%}%>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div style="background-color:blue; width:3px;height:100%;display:inline-block;float:right">
                                                            </div>
                                                            <div style="height:100%;width:5%;display:inline-block;float:right">
                                                                <div id="AttachPictureDiv_<%=incrementStep%>_<%=incrementAction%>">
                                                                <%if (tcsa.getScreenshotFilename() != null && !"".equals(tcsa.getScreenshotFilename())) {%>
                                                                    <%if(!useStep){%>
                                                                        <img id="displayedPicture_<%=incrementStep%>_<%=incrementAction%>" class="wob" width="45" height="35" src="<%=tcsa.getScreenshotFilename()%>" 
                                                                         onclick="showPicture('<%=tcsa.getScreenshotFilename()%>', <%=incrementStep%>, <%=incrementAction%>, null)"/>
                                                                    <%}else {%>
                                                                        <img id="displayedPicture_<%=incrementStep%>_<%=incrementAction%>" class="wob" width="45" height="35" src="<%=tcsa.getScreenshotFilename()%>"/>
                                                                    <%}%>   
                                                                <%}else {%>
                                                                    <%if(!useStep){%>
                                                                        <img class="AttachPictureClass"  style="margin-top:15px; margin-left:15px" width="15" height="15" src="./images/th.jpg" 
                                                                             onclick="showModalAddPicture(<%=incrementStep%>, <%=incrementAction%>, null)" />
                                                                    <%}else{%>    
                                                                        <img  width="45" height="35" src="<%=tcsa.getScreenshotFilename()%>" style="margin-top:15px; margin-left:15px" class="AttachPictureClass" />
                                                                    <%}%>   
                                                                <%}%>    
                                                                </div>
                                                                <%if(!useStep){%>
                                                                    <input style="display:none" name="action_screenshot_<%=incrementStep%>_<%=incrementAction%>" onchange="showChangedRow(this.parentNode.parentNode)" 
                                                                       id="action_screenshot_<%=incrementStep%>_<%=incrementAction%>" value="<%=tcsa.getScreenshotFilename() != null ? tcsa.getScreenshotFilename() : ""%>" <%if (useStep) {%>readonly<%}%>/>
                                                                <%}%>   
                                                            </div>

                                                        </div>

                                                        <%
                                                            List<TestCaseStepActionControl> tcsacList = tcsacService.findControlByTestTestCaseStepSequence(testForQuery, testcaseForQuery, stepForQuery, tcsa.getSequence());

                                                            int incrementControl = 0;
                                                            String controlColor = "white";
                                                            for (TestCaseStepActionControl tcsac : tcsacList) {
                                                                incrementControl++;
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
                                                        <div id="StepListOfControlDiv<%=incrementStep%><%=incrementAction%><%=incrementControl%>" data-associatedaction="StepListOfActionDiv<%=incrementStep%><%=incrementAction%>" class="RowActionDiv <%=classStep%>" style="width:100%;height:50px;clear:both;display:block;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                                                            <div style="background-color:#33CC33; width:8px;height:100%;display:inline-block;float:left">
                                                            </div>
                                                            <div style="height:100%;width: 2%;float:left; text-align: center;">
                                                                <%  if (!useStep) {%>
                                                                <img style="margin-top:12px" src="images/bin.png" id="img_delete_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" onclick="checkDeleteBox('img_delete_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>', 'control_delete_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>', 'StepListOfControlDiv<%=incrementStep%><%=incrementAction%><%=incrementControl%>', 'RowActionDiv')">
                                                                <input  class="wob" type="checkbox" data-associatedaction="action_delete_<%=incrementStep%>_<%=incrementAction%>" name="control_delete_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" style="display:none; margin-top:20px; background-color: transparent"
                                                                        id="control_delete_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" value="<%=tcsac.getStep() + '_' + tcsac.getSequence() + '_' + tcsac.getControl()%>">
                                                                <% }%>
                                                                <input type="hidden" value="<%=incrementControl%>" name="control_increment_<%=incrementStep%>_<%=incrementAction%>">
                                                                <input type="hidden" value="<%=incrementStep%>" name="control_step_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" data-fieldtype="stepNumber">
                                                            </div>
                                                            <div style="height:100%;width:3%;float:left;display:inline-block">
                                                                <% if (!useStep) {%>
                                                                <div style="margin-top:5px;height:50%;width:100%;clear:both;display:inline-block">
                                                                    <img src="images/addAction.png" style="width:15px;height:15px" title="Add Action" data-fieldtype="addActionButton"
                                                                         onclick="addTCSANew('DivActionEndOfAction<%=incrementStep%><%=incrementAction%>', '<%=incrementStep%>', this);
                                                                                 enableField('submitButtonAction');">
                                                                </div>
                                                                <div style="margin-top:-10px;height:50%;width:100%;clear:both;display:inline-block">
                                                                    <img src="images/addControl.png" style="width:15px;height:15px" title="Add Control" data-fieldtype="addControlButton"
                                                                         onclick="addTCSACNew('StepListOfControlDiv<%=incrementStep%><%=incrementAction%><%=incrementControl%>', '<%=incrementStep%>', '<%=incrementAction%>', this);
                                                                                 enableField('submitButtonChanges');">
                                                                </div>
                                                                <%}%>
                                                            </div>
                                                            <div style="width:2%;float:left;height:100%;display:inline-block">
                                                                <input data-fieldtype="ctrlseq_<%=incrementStep%>" data-field="sequence" class="wob" style="margin-top:20px;width: 20px; font-weight: bold;color:<%=actionFontColor%>"
                                                                       value="<%=incrementAction%>" name="control_sequence_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"  <%if (useStep) {%>readonly<%}%> />
                                                            </div>
                                                            <div style="width:2%;float:left;height:100%;display:inline-block">
                                                                <input class="wob" style="margin-top:20px;width: 20px; font-weight: bold; color:<%=actionFontColor%>" data-field="control"
                                                                       data-fieldtype="control_<%=incrementStep%>_<%=incrementAction%>" value="<%=incrementControl%>" 
                                                                       name="control_control_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"  <%if (useStep) {%>readonly<%}%> />
                                                                <input type="hidden" name="control_technical_control_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" value="<%=tcsac.getControl()%>">
                                                            </div>
                                                            <div style="height:100%;width:80%;float:left;display:inline-block">
                                                                <div class="functional_description" style="clear:both;width:100%;height:20px">
                                                                    <div style="float:left; width:80%">
                                                                        <div style="float:left;width:80px; "><p name="labelTestCaseStepActionControlDescription" style="float:right;font-weight:bold;" link="white" >Description</p>
                                                                        </div>
                                                                        <input class="wob" placeholder="Description" class="functional_description" style="border-style:groove;border-width:thin;border-color:white;border: 2px solid white; color:#333333; width: 80%; font-weight:bold;font-size:12px ;font-family: Trebuchet MS; "
                                                                               data-fieldtype="Description" value="<%=tcsac.getDescription()%>" id ="control_description_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" name="control_description_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" 
                                                                               maxlength="1000"  <%if (useStep) {%>readonly<%}%> />
                                                                    </div>
                                                                </div>
                                                                <div style="clear:both;display:inline-block; width:100%; height:15px">
                                                                    <div style="width:30%; float:left;">
                                                                        <div style="float:left;width:80px; "><p name="labelTestCaseStepActionControlType" style="float:right;font-weight:bold;" link="white" >Type</p>
                                                                        </div>
                                                                        <%if (!useStep) {%>
                                                                            <%=ComboInvariant(appContext, "control_type_" + incrementStep + "_" + incrementAction + "_" + incrementControl, "width:50%;font-size:10px ;border: 1px solid white;color:" + actionFontColor, "control_type_" + incrementStep + "_" + incrementAction + "_" + incrementControl, "technical_part", "CONTROL", tcsac.getType(), "", null)%>
                                                                        <%}else{
                                                                            String controlTypeid = "control_type_" + incrementStep + "_" + incrementAction + "_" + incrementControl;
                                                                            %>    
                                                                            <input value="<%=tcsac.getType()%>" name="<%=controlTypeid%>" style="width:50%;font-size:10px ;border: 1px solid white;color:grey" class="technical_part" id="<%=controlTypeid%>" readonly />
                                                                        <%}%>    
                                                                    </div>
                                                                    <div class="technical_part" style="width:30%;float:left;">
                                                                        <div style="float:left;"><p name="labelTestCaseStepActionControlProperty" style="float:right;font-weight:bold;" link="white" >Property</p>
                                                                        </div>
                                                                        <input class="wob" style="width: 70%;border: 1px solid white;  color:<%=actionFontColor%>"
                                                                               value="<%=tcsac.getControlProperty().replace("\"","&quot;")%>" 
                                                                               id="control_property_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"
                                                                               name="control_property_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" <%if (useStep) {%>readonly<%}%> />
                                                                    </div>
                                                                    <div class="technical_part" style="width:30%;float:left; ">
                                                                        <div style="float:left;"><p name="labelTestCaseStepActionControlValue" style="float:right;font-weight:bold;" link="white" >Value</p>
                                                                        </div><input class="wob" style="width: 70%;border: 1px solid white; color:<%=actionFontColor%>"
                                                                                     value="<%=tcsac.getControlValue().replace("\"","&quot;")%>" 
                                                                                     id="control_value_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>"
                                                                                     name="control_value_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" <%if (useStep) {%>readonly<%}%> />
                                                                    </div>
                                                                    <div class="technical_part" style="width:8%;float:left; ">
                                                                        <div style="float:left;"><p name="labelTestCaseStepActionControlFatal" style="float:right;font-weight:bold;" link="white" >Fatal</p>
                                                                        </div>
                                                                        <%if (!useStep) {%>
                                                                        <%=ComboInvariant(appContext, "control_fatal_" + incrementStep + "_" + incrementAction + "_" + incrementControl, "width: 40%;border: 1px solid white;color:" + actionFontColor, "control_fatal_" + incrementStep + "_" + incrementAction + "_" + incrementControl, "wob", "CTRLFATAL", tcsac.getFatal(), "trackChanges(this.value, '" + tcsac.getFatal() + "', 'submitButtonChanges')", null)%>
                                                                        <%}else{
                                                                            String controlfatalId = "control_fatal_" + incrementStep + "_" + incrementAction + "_" + incrementControl;
                                                                        %>        
                                                                            <input name="<%=controlfatalId%>" id="<%=controlfatalId%>" style="width: 40%;border: 1px solid white;color:grey" class="wob" value="<%=tcsac.getFatal()%>" readonly />
                                                                        <%}%>        
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div style="background-color:#33CC33; width:3px;height:100%;display:inline-block;float:right">
                                                            </div>
                                                            <div style="height:100%;width:5%;display:inline-block;float:right">
                                                                <div id="AttachPictureDiv_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>">
                                                                    <%if (tcsac.getScreenshotFilename() != null && !"".equals(tcsac.getScreenshotFilename())) {%>
                                                                            <%if(!useStep){%>
                                                                                <img id="displayedPicture_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" class="wob" width="45" height="35" src="<%=tcsac.getScreenshotFilename()%>" 
                                                                                 onclick="showPicture('<%=tcsac.getScreenshotFilename()%>', <%=incrementStep%>, <%=incrementAction%>, <%=incrementControl%>)" />
                                                                            <%}else {%>
                                                                                <img id="displayedPicture_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" class="wob" width="45" height="35" src="<%=tcsac.getScreenshotFilename()%>"/>
                                                                            <%}%>
                                                                    <%}else {%>
                                                                        <%if(!useStep){%>
                                                                            <img class="AttachPictureClass"  style="margin-top:15px; margin-left:15px" width="15" height="15" src="./images/th.jpg" 
                                                                                 onclick="showModalAddPicture(<%=incrementStep%>, <%=incrementAction%>, <%=incrementControl%>)"/>
                                                                        <%}else{%>
                                                                            <img  width="45" height="35" src="<%=tcsac.getScreenshotFilename()%>" style="margin-top:15px; margin-left:15px" class="AttachPictureClass" />
                                                                        <%}%>
                                                                    <%}%>
                                                                </div>
                                                                <%if(!useStep){%>
                                                                <input style="display:none" name="control_screenshot_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" onchange="showChangedRow(this.parentNode.parentNode)" 
                                                                       id="control_screenshot_<%=incrementStep%>_<%=incrementAction%>_<%=incrementControl%>" value="<%=tcsac.getScreenshotFilename() != null ? tcsac.getScreenshotFilename() : ""%>" <%if (useStep) {%>readonly<%}%> />
                                                                
                                                                <%}%>
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
                                                <%  if (canEdit) {%>

                                                <div style="clear:both; display:none" id="ActionButtonDiv">

                                                    <%if (!useStep) {%>
                                                    <div style="float:left" id="wob">
                                                        <input id="incrementActionNumber<%=incrementStep%>" value="<%=incrementAction%>" type="hidden">

                                                    </div>
                                                    <div style="float:left; display:none" id="wob">
                                                        <input type="button" value="import HTML Scenario" onclick="importer('ImportHTML.jsp?Test=<%=test%>&Testcase=<%=testcase%>&Step=<%=tcs.getStep()%>')">
                                                    </div>
                                                    <%}%>

                                                </div>
                                                <%}%>
                                            </div>
                                        </div>
                                        <div class="saveButtonDiv" ondragover="insertTCS(event, '<%=incrementStep%>')" ondrop="drop(event, '<%=incrementStep%>')" style="display:block;clear:both;margin-top:5px">
                                            <input name="divButtonSaveChange" style="display:none" value="Save Changes" class="buttonSaveChanges" onclick="submitTestCaseModificationNew('stepAnchor_<%=incrementStep%>');" id="submitButtonAction" name="submitChanges"
                                                   type="button" >
                                            <input id="addStepButton<%=incrementStep%>" type="button" value="Add Step" title="Add Step" class="buttonSaveChanges"
                                                   onclick="addTCSCNew('StepsEndDiv<%=incrementStep%>', this)">

                                        </div>
                                        <div id="StepsEndDiv<%=incrementStep%>" style="display:none; width:100%;"></div>
                                    </div>
                                    <% }%>
                                    <%=ComboInvariant(appContext, "controls_type_", "width: 200px;visibility:hidden", "controls_type_", "controls_type_", "CONTROL", "", "", null)%>
                                    <%=ComboInvariant(appContext, "controls_fatal_", "width: 40px;visibility:hidden", "controls_fatal_", "controls_fatal_", "CTRLFATAL", "", "", null)%>
                                    <%  if (canEdit) {%>
                                    <div id="hide_div"></div>
                                    <div id="ButtonAddStepDiv" style="width: 100%;display:none">
                                        <div id="wob">

                                            <input type="button" value="Import Step" id="ImportStepButton" style="display:inline"
                                                   onclick="displayImportStep('importStep()')">
                                            <input type="button" value="Use Step" id="UseStepButton" style="display:inline"
                                                   onclick="displayImportStep('useStep()')">
                                        </div>
                                        <div class="wob">
                                            <table border="0px" id="ImportStepTable" style="display: none; width: 100%">
                                                <tr>
                                                    <td  class="wob" style="font-weight: bold;">From :
                                                        <select id="fromTest" name="FromTest" onChange="getTestCasesForImportStep()">
                                                            <option value="All">-- Choose Test --</option>
                                                            <%  for (Test tst : testService.getListOfTest()) {%>
                                                            <option class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" ><%=tst.getTest()%></option>
                                                            <% } %>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="wob">
                                                        <table id="trImportTestCase" style="display: none; width: 100%"></table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td  class="wob" style="font-weight: bold;">To Step : <input type="text" class="wob" style="width: 60px; font-weight: bold;font-style: italic; color: #FF0000;" value="" name="import_step" id="import_step" ></td>
                                                </tr>
                                                <tr>
                                                    <td  class="wob" style="font-weight: bold;">Description : <input type="text" class="wob" style="width: 600px; font-weight: bold;font-style: italic; color: #FF0000;display:none" value="" name="import_description" id="import_description" ></td>
                                                </tr>
                                                <tr>
                                                    <td  class="wob" ><input id="importbutton" class="button" type="button" name="Import" value="Validate" onclick="importStep();"></td>
                                                </tr>
                                            </table>
                                        </div>

                                    </div>
                                    <% }%>
                                    <br>
                                    <br>
                                    <div id="wob"><h4>Properties</h4>
                                    </div>
                                    <div id="propertiesPartDiv">
                                        <a name="propertyAnchor"></a>
                                        <%
                                            if (tccpList != null) {
                                        %>
                                        <div id="testcaseproperties_table" style="text-align: left;">
                                            <div class="StepHeaderDiv">
                                                <div style="width: 3%; float:left"><%out.print(docService.findLabelHTML("page_testcase", "delete", "Delete", myLang));%>
                                                </div>
                                                <div style="width: 10%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "property", "Property", myLang));%>
                                                </div>
                                                <div style="width: <%=1.5 * countryListTestcase.size()%>%; float:left"><%out.print(docService.findLabelHTML("invariant", "country", "Country", myLang));%>
                                                </div>
                                                <div style="width: 10%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "type", "Type", myLang));%>
                                                </div>
                                                <div style="width: 5%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "database", "Database", myLang));%>
                                                </div>
                                                <div style="width: <%=55 - (1.5 * countryListTestcase.size())%>%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "value", "Value", myLang));%>
                                                </div>
                                                <div style="width: 3%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "length", "Length", myLang));%>
                                                </div>
                                                <div style="width: 3%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "rowlimit", "RowLimit", myLang));%>
                                                </div>
                                                <div style="width: 8%; float:left"><%out.print(docService.findLabelHTML("testcasecountryproperties", "nature", "Nature", myLang));%>
                                                </div>
                                            </div>
                                            <div id="cache_properties">
                                                <%//ComboInvariant(appContext, "properties_dtb_type_ID", "display: none;", "properties_dtb_type_ID", "wob", "PROPERTYDATABASE", tccpList.get(0).getDatabase(), "", null)%>
                                            </div>
                                            <div id="propertyRow0"></div><%

                                                int incrementProperty = 0;
                                                double widthValue = 55 - (1.5 * countryListTestcase.size());

                                                for (TestCaseCountryProperties tccp : tccpList) {
                                                    incrementProperty++;
                                                    List<String> countryOfProperty = tccpService.findCountryByProperty(tccp);

                                                    rowNumber = rowNumber + 1;
                                                    proplist = proplist + "" + tccp.getProperty() + "  /  ";

                                                    if (tccp.getType().equals("executeSqlFromLib")) {
                                                        //SqlLibrary sqllib = libService.findSqlLibraryByKey(tccp.getValue1().replaceAll("'", "''"));
                                                    }

                                                    double widthValue1 = widthValue;
                                                    double widthValue2 = 0;
                                                    String displayValue2 = "none";
                                                    if (tccp.getType().equals("getAttributeFromHtml")
                                                            || tccp.getType().equals("getFromXml")
                                                            || tccp.getType().equals("getFromCookie")
                                                            || tccp.getType().equals("getFromJson")
                                                            || tccp.getType().equals("getDifferencesFromXml")){
                                                        widthValue1 = widthValue / 2;
                                                        widthValue2 = widthValue / 2;
                                                        displayValue2 = "inline-block";
                                                    }

                                                    int nbline = tccp.getValue1().split("\n").length;
                                                    String valueID = rowNumber + "-" + tccp.getProperty();

                                                    String showEntireValueB1 = "showEntireValueB1" + valueID;
                                                    String showEntireValueB2 = "showEntireValueB2" + valueID;
                                                    String sqlDetails = "sqlDetails" + valueID;
                                                    String sqlDetailsB1 = "sqlDetailsB1" + valueID;
                                                    String sqlDetailsB2 = "sqlDetailsB2" + valueID;
                                                    String properties_dtbID = "properties_dtb" + valueID;
                                                    i++;

                                            %>
                                            <div id="propertyRow<%=incrementProperty%>" class="generalPropertyDiv" style="width:100%;height:100%; clear:both; display:inline-block;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                                                <div style="float:left;width: 8px; height:50px;position:relative; background-color: yellow; display:inline-block">
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:2%;float:left;display:inline-block;height:50px; text-align:center">
                                                    <%  if (canEdit) {%>
                                                    <img style="margin-top:20px" src="images/bin.png" id="img_delete_property_<%=incrementProperty%>" onclick="checkDeleteBox('img_delete_property_<%=incrementProperty%>', 'properties_delete_<%=incrementProperty%>', 'propertyRow<%=incrementProperty%>', 'generalPropertyDiv')">
                                                    <input style="display:none;margin-top:20px;" name="properties_delete_<%=incrementProperty%>" id="properties_delete_<%=incrementProperty%>" type="checkbox" value="">
                                                    <%}%>
                                                    <input type="hidden" name="property_increment" value="<%=incrementProperty%>">
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:10%;float:left;display:inline-block;height:50px">
                                                    <input class="wob properties_id_<%=rowNumber%> property_name" style="background-color:transparent;margin-top:20px;width:100%;font-weight: bold;"
                                                           name="properties_property_<%=incrementProperty%>" id="properties_property_<%=incrementProperty%>" value="<%=tccp.getProperty()%>">
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left; font-size : x-small ;display:inline-block;height:50px; width: <%=1.5 * countryListTestcase.size()%>%;">
                                                    <table>
                                                        <tr>
                                                            <%  for (String c : countryListTestcase) {%>
                                                            <td class="wob"><%=c%>
                                                            </td> 
                                                            <% 	} %>
                                                        </tr>
                                                        <tr>
                                                            <%
                                                                for (String c : countryListTestcase) {
                                                            %>
                                                            <td class="wob">
                                                                <input value="<%=c%>" type="checkbox" <% if (countryOfProperty.contains(c)) {%>  CHECKED  <% }%>
                                                                       class="properties_id_<%=rowNumber%>" name="properties_country_<%=incrementProperty%>">
                                                            </td>
                                                            <%  }%>
                                                        </tr>
                                                    </table>
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:10%; float:left;display:inline-block;height:50px"><%=ComboInvariant(appContext, "properties_type_" + incrementProperty, "background-color:transparent;margin-top:20px;width: 99%; float:left", "properties_type_" + incrementProperty, "wob", "PROPERTYTYPE", tccp.getType(), "activateDatabaseBox(this.value, 'properties_nodtb_" + incrementProperty + "' ,'properties_dtb_" + incrementProperty + "' );newActivateValue2(this.value, 'divProperties_value1_" + incrementProperty + "', 'divProperties_value2_" + incrementProperty + "','" + widthValue + "')", null)%>
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left;width:5%;display:inline-block;height:50px">
                                                    <%
                                                        String displayDtbList = "";
                                                        String displayNoList = "";
                                                        if (tccp.getType().equals("executeSqlFromLib")
                                                                || tccp.getType().equals("executeSql")
                                                                || tccp.getType().equals("executeSoapFromLib")) {
                                                            displayDtbList = "inline";
                                                            displayNoList = "none";
                                                        } else {
                                                            displayDtbList = "none";
                                                            displayNoList = "inline";
                                                        }
                                                    %>
                                                    <%=ComboInvariant(appContext, "properties_dtb_" + incrementProperty, "background-color:transparent;margin-top:20px;width: 100%; display: " + displayDtbList + " ;", "properties_dtb_" + incrementProperty, "wob", "PROPERTYDATABASE", tccp.getDatabase(), "", null)%>
                                                    <select name="properties_nodtb_<%=incrementProperty%>" style="background-color:transparent;margin-top:20px;width: 100%; display: <%=displayNoList%> ;" class="wob" id="properties_nodtb_<%=incrementProperty%>">
                                                        <option value="">---</option>
                                                    </select>
                                                </div>
                                                <div id="divProperties_value1_<%=incrementProperty%>" style="background-color:transparent;float:left;border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:<%=widthValue1%>%;display:inline-block;">
                                                    <div class="pull-left showInlineElement"                                                    
                                                        <% if (tccp.getType().equals("getFromDataLib_BETA")) {%>     
                                                            style="width: 90%;" 
                                                        <%}else{%>
                                                            style="width: 100%;" 
                                                        <%}%>        
                                                       >
                                                        <textarea id="properties_value1_<%=incrementProperty%>" rows="2"  
                                                                    <% if (tccp.getType().equals("getFromDataLib_BETA")) {%>     
                                                                        class ="wob getFromDataLib" 
                                                                    <%}else{%>
                                                                        class ="wob"
                                                                    <%}%>      


                                                                  style="background-color:transparent;width: 100%;height:50px" 
                                                                  name="properties_value1_<%=incrementProperty%>"><%=tccp.getValue1()%></textarea>
                                                    </div>

                                                       <div id="selectEntry_Data_<%=incrementProperty%>"  
                                                       <% if (!tccp.getType().equals("getFromDataLib_BETA")) {%>     
                                                            class="hideElement"
                                                       <%}else{%>
                                                            class="showInlineElement" 
                                                        <%}%>  
                                                            ><button title="Select an entry from the library" id="entryButton_<%=incrementProperty%>" type="button" class="btn btn-default btn-xs pull-right">
                                                            <span class="glyphicon glyphicon-search"></span> </button>
                                                       </div>    

                                                </div>
                                                 <div id="divProperties_value2_<%=incrementProperty%>" style="background-color:transparent;float:left;border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;display:<%=displayValue2%>;width:<%=widthValue2%>%;height:50px">
                                                    <div class="pull-left showInlineElement"  style="width: 100%;">
                                                        <textarea id="properties_value2_<%=incrementProperty%>" rows="2" class ="wob"
                                                              style="background-color:transparent;width: 100%;height:50px"
                                                              name="properties_value2_<%=incrementProperty%>"><%=tccp.getValue2()%></textarea>
                                                    </div>
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;background-color:transparent;border-right-color:#CCCCCC;float:left;width:3%;display:inline-block;height:50px">
                                                    <input class="wob" style="background-color:transparent;width:  100%;margin-top:20px;" name="properties_length_<%=incrementProperty%>"
                                                           value="<%=tccp.getLength()%>">
                                                </div>
                                                <div style="border-right-width:thin;border-right-style:solid;background-color:transparent;border-right-color:#CCCCCC;float:left;width:3%;display:inline-block;height:50px">
                                                    <input class="wob" style="background-color:transparent;width: 100%;margin-top:20px;" name="properties_rowlimit_<%=incrementProperty%>"
                                                           value="<%=tccp.getRowLimit()%>">
                                                </div>
                                                <div style="float:left;width:8%;display:inline-block;height:50px"><%=ComboInvariant(appContext, "properties_nature_" + incrementProperty, 
                                                        "background-color:transparent;margin-top:20px;width: 100%;", "properties_nature_" + incrementProperty, "wob", "PROPERTYNATURE", tccp.getNature(), 
                                                                   "trackChanges(0, this.selectedIndex, 'submitButtonChanges')", null)%>
                                                </div>
                                                <div style="background-color:yellow; width:3px;height:50px;display:inline-block;float:right">
                                                </div>
                                                <div style="float:right ; width:2%">
                                                    <%
                                                        if (tccp.getType().equals("executeSqlFromLib")
                                                                || tccp.getType().equals("executeSql")) {
                                                    %>
                                                    <div style="clear:both" class="wob">
                                                        <input style="display:inline; height:18px; width:18px; color:blue; font-weight:bolder" title="Open SQL Library" 
                                                               class="smallbutton" type="button" value="L" name="opensql-library"  onclick="openSqlLibraryPopin('properties_value1_<%=incrementProperty%>', 'properties_type_<%=incrementProperty%>')">
                                                    </div>
                                                    <% }%>
                                                    <%
                                                        if (tccp.getType().equals("getFromTestData")) {
                                                    %>
                                                    <div style="clear:both" class="wob">
                                                        <input style="display:inline; height:18px; width:18px; color:blue; font-weight:bolder" title="Open TestData" 
                                                               class="smallbutton" type="button" value="L" name="open-testdata"  onclick="openTestData('<%=tccp.getValue1()%>')">
                                                    </div>
                                                    <% }%>
                                                    <%
                                                        if (tccp.getType().equals("executeSqlFromLib")
                                                                || tccp.getType().equals("executeSql")
                                                                || tccp.getType().equals("getFromTestData")
                                                                || tccp.getType().equals("executeSoapFromLib")) {
                                                    %>
                                                    <div style="clear:both" class="wob">
                                                        <input style="display:inline; height:18px; width:18px; color:green; font-weight:bolder" title="View property" class="smallbutton" type="button" value="V" name="openview-library"  onclick="openViewPropertyPopin('<%=incrementProperty%>', '<%=test%>', '<%=testcase%>')">
                                                    </div>
                                                    <%}%>
                                                    <% if (nbline > 3) {%>
                                                    <div class="wob" style="clear:both;text-align: center; border-left-color:white">
                                                        <input style="display:inline; height:18px; width:18px; color: green; font-weight:bolder" class="smallbutton" title="Show the Full Sql" type="button" value="+" id="<%=showEntireValueB1%>" onclick="showEntireValue('<%=valueID%>', '<%=nbline%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                        <input style="display:none; height:18px; width:18px; color: red; font-weight:bolder" class="smallbutton" title="Hide Details" type="button" value="-" id="<%=showEntireValueB2%>" onclick="showLessValue('<%=valueID%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                    </div>
                                                    <%} else {%>
                                                    <div class="wob" style="clear:both;text-align: center; border-left-color:white">
                                                        <% if (tccp.getType().equals("executeSqlFromLib")) {%>
                                                        <input style="display:inline; height:18px; width:18px; color: orange; font-weight:bolder" class="smallbutton" type="button" value="e" title="Show the SQL" id="<%=sqlDetailsB1%>" onclick="showSqlInstruction('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                        <input style="display:none; height:18px; width:18px; color: orange; font-weight:bolder" class="smallbutton" type="button" value="-" title="Hide the SQL" id="<%=sqlDetailsB2%>" onclick="hideSqlInstruction('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                        <% } %>
                                                    </div>
                                                    <% }%>
                                                </div>

                                            </div>
                                            <%}%>
                                        </div>
                                        <br>
                                        <%  if (canEdit) {%>
                                        <input type="button" onclick="submitTestCaseModificationNew('propertyAnchor');" class="buttonSaveChanges" value="Save Changes" id="SavePropertyChanges">              
                                        <input type="button" class="buttonSaveChanges" value="Add Property" id="AddProperty"
                                               onclick="addPropertyNew('<%=widthValue%>')">
                                        <input type="hidden" id="Test" name="Test" value="<%=test%>">
                                        <input type="hidden" id="TestCase" name="TestCase" value="<%=testcase%>">
                                        <input type="hidden" name="testcase_hidden" value="<%=test + " - " + testcase%>">
                                        <input type="hidden" id="CountryList" name="CountryList" value="<%=countries%>">
                                        <%=ComboInvariant(appContext, "new_properties_type_new_properties_value", "width: 70px;visibility:hidden", "new_properties_type_new_properties_value", "new_properties_type_new_properties_value", "PROPERTYTYPE", "", "", null)%>
                                        <%=ComboInvariant(appContext, "properties_dtb_", "width: 40px;visibility:hidden", "properties_dtb_", "properties_dtb_", "PROPERTYDATABASE", "", "", null)%>
                                        <%=ComboInvariant(appContext, "properties_nature_", "width: 80px;visibility:hidden", "properties_nature_", "properties_nature_", "PROPERTYNATURE", "", "", null)%>
                                        <input type="hidden" name="testcase_hidden" value="<%=test + " - " + testcase%>">
                                        <input type="hidden" name="testcase_country_hidden" value="<%=countries%>">
                                        <% }%>
                                        </td>
                                        </tr>
                                        </table>
                                        <p id="hiddenProperty" style="font-size : x-small ; width: <%=countryListTestcase.size()%>px; visibility:hidden">
                                            <% for (String c : countryListTestcase) {%>
                                            <%=c%> 
                                            <% } %>
                                            <br>
                                            <% for (String c : countryListTestcase) {
                                            %>
                                            <input data-country="ctr" value="<%=c%>" type="checkbox" id="properties_country" 
                                                   name="properties_country" >
                                            <% } %>
                                        </p>
                                        <%
                                        } else {
                                        %>
                                        <table id="nocountrydefined" class="arrond">
                                            <tr>
                                                <td class="wob"></td>
                                            </tr>
                                            <tr>
                                                <td class="wob">
                                                    <h3> To add Properties,Actions and controls, select at least one country in the general parameters </h3>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="wob"></td>
                                            </tr>
                                        </table>
                                        <%   } %>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
                <br>
            </div>                            
            <table class="arrond" style="width:100%;text-align: left" border="1" >
                <tr>
                    <td colspan="3">
                        <h4>Contextual Actions</h4>
                    </td>
                </tr>
                <tr>
                    <% if (tcase.getGroup().equalsIgnoreCase("AUTOMATED")) {%>
                    <td>
                        <a onclick="return checkUndefinedProperties();" href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>">Run this Test Case.</a>
                    </td>
                    <%        } else if (tcase.getGroup().equalsIgnoreCase("MANUAL")) {%>
                    <td>
                        <a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>&manualExecution=Y">Run this Test Case.</a>
                    </td>
                    <%        }%>    
                    <td>
                        <a href="ExecutionDetailList.jsp?test=<%=test%>&testcase=<%=testcase%>&MySystem=<%=appSystem%>">See Last Executions..</a>
                    </td>
                    <%if (request.getUserPrincipal()
                                != null && request.isUserInRole("TestAdmin")) {
                    %>
                    <td>
                        <a href="LogViewer.jsp?Test=<%=test%>&TestCase=<%=testcase%>">See Log Viewer...</a>
                    </td>
                    <% }%>
                </tr>
            </table>
            <div id="StepActionTemplateDiv" style="padding:0; margin:0;display:none;height:45px;width:100%;border-style: solid; border-width:thin ; border-color:#CCCCCC;">
                <div style="background-color:blue; width:8px;height:45px;display:inline-block;float:left">
                </div>
                <div style="display:inline-block;float:left;width:2%;height:100%;text-align:center">
                    <img style="margin-top:12px" src="images/bin.png" data-id="action_img_delete">
                    <input  class="wob" type="checkbox" data-action="delete_action" data-id="action_delete_template" style="margin-top:20px;width: 30px; background-color: transparent">
                    <input type="hidden" data-id="action_increment_template">
                    <input type="hidden" data-id="action_step_template" data-fieldtype="stepNumber">
                </div>
                <div style="height:100%;width:3%;float:left;display:inline-block">
                    <div style="margin-top: 5px;height:50%;width:100%;clear:both;display:inline-block">
                        <img data-id="actionAddActionButton_template" data-fieldtype="addActionButton" src="images/addAction.png" style="width:15px;height:15px" title="Add Action">
                    </div>
                    <div style="margin-top:-15px;height:50%;width:100%;clear:both;display:inline-block">
                        <img data-id="actionAddControlButton_template" src="images/addControl.png" data-fieldtype="addControlButton" style="width:15px;height:15px" title="Add Control">
                    </div>
                </div>
                <div style="height:100%;width:4%;display:inline-block;float:left">
                    <input data-id="action_sequence_template" class="wob" style="width: 40px; font-weight: bold; background-color: transparent; height:100%;"
                           data-field="sequence">
                </div>
                <input type="hidden" data-id="action_technical_sequence_template">
                <div style="height:100%;width:89%;float:left; display:inline-block">
                    <div class="functional_description" style="height:20px;display:inline-block;clear:both;width:100%; background-color: transparent">
                        <div style="float:left; width:80%">
                            <div style="float:left;width:80px; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "description", "Description", myLang));%></p>
                            </div>
                            <input data-id="action_description_template" class="wob" class="functional_description" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#333333; width: 80%; background-color: transparent; font-weight:bold;font-size:14px ;font-family: Trebuchet MS; "
                                   data-fieldtype="Description" placeholder="Description">
                        </div>
                    </div>
                    <div style="display:inline-block;clear:both; height:15px;width:99%;background-color:transparent">
                        <div class="technical_part" style="width: 30%; float:left; background-color: transparent">
                            <div style="float:left;width:80px; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "action", "Action", myLang));%></p>
                            </div>
                            <%=ComboInvariant(appContext, "", "width: 50%;border: 1px solid white; background-color:transparent;", "action_action_template", "wob", "ACTION", "", "", null)%>
                        </div>
                        <div class="technical_part" style="width: 35%; float:left; background-color: transparent">
                            <div style="float:left;"><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "object", "Object", myLang));%></p>
                            </div>
                            <input style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; background-color: transparent;"
                                   data-id="action_object_template">
                        </div>
                        <div class="technical_part" style="width: 25%; float:left; background-color:transparent">
                            <div style="float:left; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "property", "Property", myLang));%></p>
                            </div>
                            <input  class="wob property_value" style="width:75%;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; background-color: transparent;"
                                    data-id="action_property_template">
                        </div>
                        <div class="technical_part" style="width: 10%; float:left; background-color: transparent">
                            <div style="float:left;width:60px; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "ForceExeStatus", "Force Execution Status", myLang));%></p>
                            </div>
                            <%=ComboInvariant(appContext, "", "width: 70%;border: 1px solid white; background-color:transparent;", "action_forceexestatus_template", "wob", "ACTIONFORCEEXESTATUS", "", "", null)%>
                        </div>
                    </div>
                </div>
                <div style="background-color:blue; width:3px;height:45px;display:inline-block;float:right">
                </div>
            </div>
            <div id="StepControlTemplateDiv" style="width:100%;height:45px;clear:both;display:none">
                <div data-id="control_color_id" style="background-color:#33CC33; width:8px;height:45px;display:block;float:left">
                </div>
                <div style="height:100%;width: 2%;float:left; text-align: center;">
                    <img style="margin-top:12px" src="images/bin.png" data-id="control_img_delete">
                    <input style="margin-top:20px;" type="checkbox" data-id="control_delete_template">
                    <input type="hidden" data-id="control_increment_template">
                    <input type="hidden" data-id="control_step_template" data-fieldtype="stepNumber">
                </div>
                <div style="height:100%;width:3%;float:left;display:inline-block">
                    <div style="margin-top:5px;height:50%;width:100%;clear:both;display:inline-block">
                        <img data-id="controlAddActionButton_template" src="images/addAction.png" data-fieldtype="addActionButton" style="width:15px;height:15px" title="Add Action">
                    </div>
                    <div style="margin-top:-10px;height:50%;width:100%;clear:both;display:inline-block">
                        <img data-id="controlAddControlButton_template" src="images/addControl.png" data-fieldtype="addControlButton" style="width:15px;height:15px" title="Add Control">
                    </div>
                </div>
                <div style="width:2%;float:left;height:100%;display:inline-block">
                    <input data-field="sequence" class="wob" style="margin-top:20px;width: 20px; font-weight: bold;"
                           data-id="control_sequence_template">
                </div>
                <div style="width:2%;float:left;height:100%;display:inline-block">
                    <input data-field="control" class="wob" style="margin-top:20px;width: 20px; font-weight: bold;"
                           data-id="control_control_template">
                </div>
                <input type="hidden" data-id="control_technical_control_template">
                <div style="height:100%;width:89%;float:left;display:inline-block">
                    <div class="functional_description_control" style="clear:both;width:100%;height:20px">
                        <div style="float:left; width:80%">
                            <div style="float:left;width:80px; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepaction", "description", "Description", myLang));%></p>
                            </div>
                            <input class="wob" placeholder="Description" class="functional_description_control" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; color:#333333; width: 80%; background-color: transparent; font-weight:bold;font-size:14px ;font-family: Trebuchet MS; "
                                   data-id="control_description_template" data-fieldtype="Description">
                        </div>
                    </div>
                    <div style="clear:both; width:100%; height:15px">
                        <div style="width:30%; float:left;">
                            <div style="float:left;width:80px; "><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "Type", "ControlType", myLang));%></p>
                            </div>
                            <%=ComboInvariant(appContext, "", "width: 70%;border: 1px solid white; background-color:transparent;", "control_type_template", "wob", "CONTROL", "", "", null)%>
                        </div>
                        <div class="technical_part" style="width:30%;float:left;">
                            <div style="float:left;"><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "controleproperty", "controleproperty", myLang));%></p>
                            </div>
                            <input class="wob" style="width: 75%;border: 1px solid white; background-color:transparent; "
                                   data-id="control_property_template">
                        </div>
                        <div class="technical_part" style="width:30%;float:left; ">
                            <div style="float:left;"><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "controlevalue", "controlevalue", myLang));%></p>
                            </div><input class="wob" style="width: 70%;border: 1px solid white; background-color:transparent;"
                                         data-id="control_value_template">
                        </div>
                        <div class="technical_part" style="width:8%;float:left; ">
                            <div style="float:left;"><p style="float:right;font-weight:bold;color:white;" link="white" ><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "fatal", "fatal", myLang));%></p>
                            </div>
                            <%=ComboInvariant(appContext, "", "width: 40%;border: 1px solid white; background-color:transparent;", "control_fatal_template", "wob", "CTRLFATAL", "", "", null)%>
                        </div>
                    </div>
                </div>
                <div style="background-color:#33CC33; width:3px;height:45px;display:inline-block;float:right">
                </div>

            </div>
            <div id="StepTemplateDiv" class="StepHeaderDiv" style="display:none; height:60px;">
                <div style="clear:both">
                    <div id="StepComboDeleteDiv" style="float:left; width: 30px; text-align: center; height:100%">
                        <a data-id="stepAnchor_template"></a>
                        <a data-id="stepAnchor_steptemplate"></a>
                        <img style="margin-top:12px" src="images/bin.png" data-id="step_img_delete">
                        <input type="checkbox" data-id="step_delete_template" style="margin-top:15px;font-weight: bold; width:20px">
                        <input type="hidden" data-id="step_increment">
                    </div>
                    <div style="margin-top:10px;width:3%;float:left;color:blue;font-weight:bold;font-size:10px ;font-family: Trebuchet MS; background-color: transparent">
                        <div style="width:100%;clear:both;color:blue;font-weight:bold;font-size:10px ;font-family: Trebuchet MS; background-color: transparent">
                            <div>

                            </div>
                        </div>
                    </div>
                    <div id="StepNumberDiv" style="float:left; width:80px">
                        &nbsp;&nbsp;Step&nbsp;&nbsp;
                        <input data-id="step_number_template" data-fieldtype="stepNumber" style="margin-top:15px;font-weight: bold; width:20px;background-color:transparent; border-width:0px">
                        <input type="hidden" data-id="initial_step_number_template">
                        <input type="hidden" data-id="step_technical_number_template">
                    </div>
                    <div id="StepDescDiv" style="width:550px;float:left">
                        <input style="float:right;margin-top:10px;font-weight: bold; width: 500px;background-color:transparent; font-weight:bold;font-size:16px ;font-family: Trebuchet MS;
                               color:#333333; border-color:#EEEEEE; border-width: 1px" maxlength="150" data-id="step_description_template">
                    </div>
                    <div id="StepUseStepDiv" style="float:left">UseStep
                        <input type="checkbox" data-id="step_useStep_template" style="margin-top:15px;font-weight: bold; width:20px">
                        <input type="hidden" name="step_useStepChanged_template" value="N" id="step_useStepChanged_template" />
                        <input type="hidden" name="isToCopySteps_template" value="N" id="isToCopySteps_template" />
                    </div>
                </div>
                <div data-id="useStepForNewStep" style="display:none; ">
                    <div id="StepCopiedFromDiv" style="float:left">
                        <p style="margin-top:15px;"> Copied from : </p>
                    </div>
                    <div id="StepUseStepTestDiv" style="float:left">
                        <select data-id="step_useStepTest_template" style="width: 150px;margin-top:12.5px;font-weight: bold;">
                            <option style="width: 400px" value="">Choose Test</option>                            
                            <% List<TestCaseStep> tcsLib = tcsService.getStepLibraryBySystem(MySystem);
                                Set<String> tList = new HashSet();
                                HashMap tcListByTc = new HashMap();
                                List<String> tcList = new ArrayList();
                                String previousTc = "";
                                for (TestCaseStep tcsUnit : tcsLib) {
                                    tList.add(tcsUnit.getTest());
                                    if (!tcsUnit.getTest().equals(previousTc)) {
                                        if (!previousTc.equals("")) {
                                            tcListByTc.put(previousTc, tcList);
                                        }
                                        tcList = new ArrayList();
                                    }
                                    tcList.add(tcsUnit.getTestCase());
                                }
                                String testCombo = test;

                                for (String tst : tList) {%>
                            <option style="width: 400px;" value="<%=tst%>"><%=tst%>
                            </option>
                            <% }%>
                        </select>
                    </div>
                    <div id="StepUseStepTestCaseDiv" style="float:left;">
                        <select data-id="step_useStepTestCase_template" style="width: 150px;margin-top:7.5px;font-weight: bold;">
                            <option style="width: 400px" value="All">---</option>
                        </select>
                    </div>
                    <div id="StepUseStepStepDiv" style="float:left">
                        <select data-id="step_useStepStep_template" style="width: 150px;margin-top:7.5px;font-weight: bold;">
                            <option style="width: 400px" value="0">---</option>
                        </select>
                    </div>
                </div>
            </div>
            <div id="StepButtonTemplateDiv" ondragover="insertTCS(event, '<%=incrementStep%>')"
                 ondrop="drop(event, '<%=incrementStep%>')" style="display:none;clear:both;margin-top:5px">
                <input value="Save Changes" class="buttonSaveChanges" 
                       data-id="submitButtonActionTemplate" name="submitChanges" type="button" >
                <input data-id="addStepButtonTemplate" type="button" value="Add Step" title="Add Step"
                       class="buttonSaveChanges">
            </div>
            <div id="PropertyTemplateDiv" style="display:none">
                <div data-id="property_color_id" style="float:left;width: 8px; height:100%;position:relative; background-color: yellow; display:inline-block">
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:2%;float:left;display:inline-block;height:100%; text-align:center">
                    <input style="margin-top:20px;" data-id="properties_delete_template" type="checkbox" value="">
                    <img style="margin-top:12px" src="images/bin.png" data-id="property_img_delete">
                    <input type="hidden" data-id="property_increment_template">
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:10%;float:left;display:inline-block;height:100%">
                    <input class="wob property_name" style="background-color:transparent;margin-top:20px;width:100%;font-weight: bold;"
                           data-id="properties_property_template">
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left; font-size : x-small ;display:inline-block;height:100%; width: <%=1.5 * countryListTestcase.size()%>%;">
                    <table>
                        <tr>
                            <%  for (String c : countryListTestcase) {%>
                            <td class="wob"><%=c%>
                            </td> 
                            <% 	} %>
                        </tr>
                        <tr>
                            <%
                                for (String c : countryListTestcase) {
                            %>
                            <td class="wob">
                                <input value="<%=c%>" type="checkbox"
                                       class="properties_id_template" data-id="properties_country_template">
                            </td>
                            <%  }%>
                        </tr>
                    </table>
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:10%; float:left;display:inline-block;height:100%">
                    <%=ComboInvariant(appContext, "", "background-color:transparent;margin-top:20px;width: 99%; float:left", "properties_type_template", "wob", "PROPERTYTYPE", "", "", null)%>
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left;width:5%;display:inline-block;height:100%">
                    <%=ComboInvariant(appContext, "", "display:none;background-color:transparent;margin-top:20px;width: 100%;", "properties_dtb_template", "wob", "PROPERTYDATABASE", "", "", null)%>
                    <select data-id="properties_nodtb_template" style="background-color:transparent;margin-top:20px;width: 100%; display: inline-block ;" class="wob">
                        <option value="">---</option>
                    </select>
                </div>
                <div data-id="divProperties_value1_template" style="background-color:transparent;float:left;border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;width:<%=55 - (1.5 * countryListTestcase.size())%>%;display:inline-block;height:100%">
                    <div class="pull-left showInlineElement" style="width:100%;">
                        <textarea data-id="properties_value1_template" rows="2" class="wob" style="background-color:transparent;width: 100%;height:50px" 
                                  ></textarea>
                    </div>
                    <div data-id="selectEntry_Data_template"  class="hideElement">
                        <button title="Select an entry from the library" data-id="entryButton_template" type="button" class="btn btn-default btn-xs pull-right">
                        <span class="glyphicon glyphicon-search"></span> </button>
                    </div>    
                </div>
                <div data-id="divProperties_value2_template" style="float:left;border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;display:none;width:0%;height:100%">
                    <div class="pull-left showInlineElement" style="width:100%;">
                        <textarea data-id="properties_value2_template" rows="2" class="wob" style="background-color:transparent;width: 100%;height:50px"></textarea>
                    </div>
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left;width:3%;display:inline-block;height:100%">
                    <input class="wob" style="background-color:transparent;width:  100%;margin-top:20px;" 
                           data-id="properties_length_template">
                </div>
                <div style="border-right-width:thin;border-right-style:solid;border-right-color:#CCCCCC;float:left;width:3%;display:inline-block;height:100%">
                    <input class="wob" style="background-color:transparent;width: 100%;margin-top:20px;" 
                           data-id="properties_rowlimit_template">
                </div>
                <div style="float:left;width:8%;display:inline-block;height:100%">
                    <%=ComboInvariant(appContext, "", "background-color:transparent;margin-top:20px;width: 100%;", "properties_nature_template", "wob", "PROPERTYNATURE", "", "", null)%>
                </div>
                <div style="background-color:yellow; width:3px;height:100%;display:inline-block;float:right">
                </div>
                <div style="float:right ; width:2%">
                </div>

            </div>
 
                <%
                            }
                        }
                    } catch (Exception e) {
                        out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
                    }
                %>
        </div>
        <% if (booleanFunction) {%>
        <script type="text/javascript">
            $(document).ready(function() {
                $.getJSON($('#urlForListOffunction').val(), function(data) {
                    for (var i = 0; i < data.length; i++) {
                        $("#functions").append($("<option></option>")
                                .attr("value", data[i].value));
                    }
                });
            });</script>
            <%}%>
        <script>
            function checkDeletePropertiesUncheckingCountry(country) {
                for (var a = 0; a < document.getElementsByName('properties_delete').length; a++) {
                    if (document.getElementsByName('properties_delete')[a].value.contains(country)) {
                        alert("BEWARE : Unchecking this country will automatically delete the associated properties saving the testcase");
                    }
                }
                ;
            }
            ;</script>

        <script type="text/javascript">
            function addOptionInSelect(newElementId, selectElementId) {                
                var newValue = $("#" + newElementId).attr("value").trim();
                var notFound = $("#" + selectElementId + " option[value='" + newValue + "']").length === 0;
                if(notFound){                   
                    //it only creates if the text inserted is different of empty
                    if(newValue !== ''){
                        var option = document.createElement('option');
                        option.innerHTML = newValue;
                        option.value = newValue;
                        document.getElementById(selectElementId).appendChild(option);
                        document.getElementById(selectElementId).value = newValue;
                    }
                }else{//already exists  
                    $("#" + selectElementId + " option[value='" + newValue + "']").attr("selected", "selected");
                    document.getElementById(selectElementId).value = newValue;
                }
            }
        </script>
        <script>
            function enableDuplicateField() {
                document.getElementById('inputAddTestCaseInSelectTestCase').style.display = 'inline-block';
                document.getElementById('inputAddTestInSelectTest').style.display = 'inline-block';
                document.getElementById('saveAs').style.display = 'none';
                document.getElementById('FirstSaveChanges').style.display = 'inline-block';
                document.getElementById('informationTest').disabled = false;
                document.getElementById('informationTestCase').disabled = false;
                document.getElementById('hiddenInformationTest').disabled = true;
                document.getElementById('hiddenInformationTestCase').disabled = true;
            }

        </script>

        <script>
            function showChangedRow(row) {
                $(row).css('background-color', '#FFEBC4');
            }
        </script>
        <script>
            $(document).ready(function() {
                return callEvent();
            });

            function callEvent() {
                $("input[data-fieldtype='Description']").each(function(index, field) {
                    $(field).off('keydown');
                    $(field).on('keydown', function(e) {
                        if (e.which === 67 && e.altKey) {
                            var f = $(field).parent().parent().parent().parent();
                            f.find('img[data-fieldtype="addActionButton"]').click();
                            f.nextUntil('.RowActionDiv').next().find("input[data-fieldtype='Description']").focus();

                        }
                        if (e.which === 86 && e.altKey) {
                            var f = $(field).parent().parent().parent().parent();
                            f.find('img[data-fieldtype="addControlButton"]').click();
                            f.next().find("input[data-fieldtype='Description']").focus();

                        }
                    });
                });
            }</script>
       
    <script>
    function showLib() {
    var expiration_date = new Date();
                expiration_date.setFullYear(expiration_date.getFullYear() + 1);
                
                if (document.getElementById("StepLibDiv").style.display === "none") {
                    document.getElementById("StepLibDiv").style.display = "block";
                    document.getElementById("StepLibDiv").style.width = "13%";
                    document.getElementById("StepsRightDiv").style.width = "85%";
                    document.cookie = "TestCaseDisplayStepLibrary=true;expires=" + expiration_date.toGMTString();
                } else {
                    document.getElementById("StepLibDiv").style.display = "none";
                    document.getElementById("StepLibDiv").style.width = "0%";
                    document.getElementById("StepsRightDiv").style.width = "100%";
                    document.cookie = "TestCaseDisplayStepLibrary=false;expires=" + expiration_date.toGMTString();
                }

            }

            function showTargetDiv() {
                $("div[class='saveButtonDiv']").each(function(index, field) {
                    $(field).attr('style', 'background-color:#C4FFEB');
                });

            }
            function hideTargetDiv() {
                $("div[class='saveButtonDiv']").each(function(index, field) {
                    $(field).attr('style', 'background-color:transparent');
                });

            }

            </script>
        <script type="text/javascript">
    $(document).ready(function() {
        var cookies = GetCookie('TestCaseDisplayStepLibrary');
        if (cookies === "true"){
            showLib();
        }
        
    });

</script>
        <!--<script>
        $(document).ready(function() {
                       
        var data = ;
          console.log(data);
        $("#StepLibDiv").fancytree({
           minExpandLevel: 1,
          source: data,
          checkbox: false
        });
                    });
        
        </script>-->

        <div id="popin"></div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
