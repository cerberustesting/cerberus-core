package com.redcats.tst.serviceEngine.impl;

import com.redcats.tst.entity.*;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.*;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.*;
import com.redcats.tst.serviceEngine.*;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 23/01/2013
 * @since 2.0.0
 */
@Service
public class RunTestCaseService implements IRunTestCaseService {

    @Autowired
    private IExecutionCheckService executionCheckService;
    @Autowired
    private ISeleniumService seleniumService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IControlService controlService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseStepExecutionService testCaseStepExecutionService;
    @Autowired
    private ITestCaseStepActionExecutionService testCaseStepActionExecutionService;
    @Autowired
    private ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ITestCaseExecutionSysVerService testCaseExecutionSysVerService;
    @Autowired
    private ICountryEnvLinkService countryEnvLinkService;
    @Autowired
    private ITestCaseExecutionWWWService testCaseExecutionWWWService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private ICountryEnvironmentApplicationService countryEnvironmentApplicationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILoadTestCaseService loadTestCaseService;
    @Autowired
    private IFactoryTestCaseStepExecution factoryTestCaseStepExecution;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;
    @Autowired
    private IFactoryTestCaseStepActionControlExecution factoryTestCaseStepActionControlExecution;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;
    @Autowired
    private IFactoryCountryEnvLink factoryCountryEnvLink;
    @Autowired
    private IFactoryCountryEnvironmentApplication factorycountryEnvironmentApplication;
    @Autowired
    private IInvariantService invariantService;

    @Override
    public TCExecution runTestCase(TCExecution tCExecution) {

        /**
         * Start timestamp.
         */
        long executionStart = new Date().getTime();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Initializing Start Timestamp : " + executionStart);
        tCExecution.setStart(executionStart);

        /**
         * Checking the parameters.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("OUTPUTFORMAT", tCExecution.getOutputFormat());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", tCExecution.getOutputFormat()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("VERBOSE", String.valueOf(tCExecution.getVerbose()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getVerbose())));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("SCREENSHOT", String.valueOf(tCExecution.getScreenshot()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getScreenshot())));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }

        /**
         * Load TestCase information and set TCase to the TCExecution object.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDATA));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Test Case Information. " + tCExecution.getTest() + "-" + tCExecution.getTestCase());
        // Integrate this.loadTestCaseService.loadTestCase(tCExecution); inside with Dependency.
        try {
            TCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            tCExecution.settCase(tCase);
            /**
             * Copy the status of the testcase to the status column of the
             * Execution. This is done to know how stable was the testcase at
             * the time of the execution.
             */
            tCExecution.setStatus(tCase.getStatus());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%TEST%", tCExecution.getTest()));
            mes.setDescription(mes.getDescription().replaceAll("%TESTCASE%", tCExecution.getTestCase()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Test Case Information Loaded - " + tCExecution.getTest() + "-" + tCExecution.getTestCase());


        /**
         * Load Application information and Set Application to the TCExecution
         * object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Application Information");
        try {
            tCExecution.setApplication(this.applicationService.findApplicationByKey(tCExecution.gettCase().getApplication()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%APPLI%", tCExecution.gettCase().getApplication()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Application Information Loaded - " + tCExecution.getApplication().getDescription());


        /**
         * Load Country information and Set it to the TCExecution object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country Information");
        try {
            tCExecution.setCountryObj(this.invariantService.findInvariantByIdValue("COUNTRY", tCExecution.getCountry()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Country Information Loaded - " + tCExecution.getCountryObj().getDescription());


        /**
         * Checking if execution is manual or automaticaly configured. If
         * Manual, CountryEnvironmentApplication object is manually created with
         * the servlet parameters. If automatic, parameters are build from the
         * CountryEnvironmentApplication table in the database. Environmentdata
         * will always be filled with the environment. Environment will be empty
         * if execution is manual.
         *
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking if connectivity parameters are manual or automatic from the database. '" + tCExecution.isManualURL() + "'");
        if (tCExecution.isManualURL()) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Execution will be done with manual application connectivity setting.");
            if (StringUtil.isNullOrEmpty(tCExecution.getMyHost())) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID);
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            } else {
                CountryEnvironmentApplication cea;
                cea = this.factorycountryEnvironmentApplication.create(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication(), tCExecution.getMyHost(), tCExecution.getMyContextRoot(), tCExecution.getMyLoginRelativeURL());
                cea.setIp(tCExecution.getMyHost());
                cea.setUrl(tCExecution.getMyContextRoot());
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                tCExecution.setCountryEnvironmentApplication(cea);
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, " -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            }
            /**
             * If execution is manual, we force the env at empty string.
             */
            tCExecution.setEnvironment("");
        } else {
            /**
             * Automatic application configuration execution.
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Execution will be done with automatic application connectivity setting.");
            /**
             * Load Country/Environment/Application information and set them to
             * the TCExecution object
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country/Environment/Application Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironment() + "-" + tCExecution.getApplication().getApplication());
            CountryEnvironmentApplication cea;
            try {
                cea = this.countryEnvironmentApplicationService.findCountryEnvironmentParameterByKey(
                        tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication());
                tCExecution.setCountryEnvironmentApplication(cea);
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                /**
                 * Forcing the IP URL and Login config from DevIP, DevURL and
                 * DevLogin parameter only if DevURL is defined.
                 */
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironment()));
                mes.setDescription(mes.getDescription().replaceAll("%APPLI%", tCExecution.gettCase().getApplication()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "-> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            tCExecution.setEnvironmentData(tCExecution.getEnvironment());
        }

        /**
         * Load Environment object from invariant table.
         */
        try {
            tCExecution.setEnvironmentDataObj(this.invariantService.findInvariantByIdValue("ENVIRONMENT", tCExecution.getEnvironmentData()));
        } catch (CerberusException ex) {
            if (tCExecution.isManualURL()) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN);
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }
        }


        /**
         * Load Country/Environment information and set them to the TCExecution
         * object. Environment considered here is the data environment.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country/Environment Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironmentData());
        CountryEnvParam countEnvParam;
        try {
            countEnvParam = this.countryEnvParamService.findCountryEnvParamByKey(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironmentData());
            tCExecution.setCountryEnvParam(countEnvParam);
            /**
             * Copy the Build/Revision of the environment to the Execution. This
             * is done to keep track of all execution done on a specific version
             * of system
             */
            tCExecution.setBuild(countEnvParam.getBuild());
            tCExecution.setRevision(countEnvParam.getRevision());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
            mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }


        /**
         * Check if test can be executed TODO : Replace Message with try/catch
         * cerberus exception
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_VALIDATIONSTARTING));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Performing the Checks before starting the execution");
        MessageGeneral canExecuteTestCase = this.executionCheckService.checkTestCaseExecution(tCExecution);
        tCExecution.setResultMessage(canExecuteTestCase);
        /**
         * We stop if the result is not OK
         */
        if (!(tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
            return tCExecution;
        }

        /**
         * Check if Browser is supported and if selenium server is reachable.
         */
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {

            try {
                myInvariant = this.invariantService.findInvariantByIdValue("BROWSER", tCExecution.getBrowser());
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replaceAll("%BROWSER%", tCExecution.getBrowser()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }

            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Application is GUI. Trying to reach selenium server.");
            if (!this.seleniumService.isSeleniumServerReachable(tCExecution.getIp(), tCExecution.getPort())) {
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%SSIP%", tCExecution.getIp()));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%SSPORT%", tCExecution.getPort()));
                return tCExecution;
            }
        }

        /**
         * Register RunID inside database.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CREATINGRUNID));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Execution ID on database");
        long runID = this.testCaseExecutionService.registerRunID(tCExecution);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - RunID Registered on database.");
        if (runID <= 0) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            return tCExecution;
        }

        /**
         * Feeding Build Rev of main Application system to
         * testcaseexecutionsysver table. Only if execution is not manual.
         */
        if (!(tCExecution.isManualURL())) {
            TestCaseExecutionSysVer myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, tCExecution.getApplication().getSystem(), tCExecution.getBuild(), tCExecution.getRevision());
            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);

            /**
             * For all Linked environment, we also keep track on the build/rev
             * information inside testcaseexecutionsysver table.
             */
            List<CountryEnvLink> ceLink = null;
            try {
                ceLink = countryEnvLinkService.findCountryEnvLinkByCriteria(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment());
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - Linked environment found.");
                for (CountryEnvLink myCeLink : ceLink) {
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - Linked environment found : " + myCeLink.getSystemLink() + myCeLink.getCountryLink() + myCeLink.getEnvironmentLink());

                    CountryEnvParam mycountEnvParam;
                    try {
                        mycountEnvParam = this.countryEnvParamService.findCountryEnvParamByKey(myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink());
                        myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, myCeLink.getSystemLink(), mycountEnvParam.getBuild(), mycountEnvParam.getRevision());
                        testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                    } catch (CerberusException ex) {
                        // Referencial Integrity link between countryEnvLink and CountryEnvParam table should secure that exception to never happen.
                        Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, ex.getMessage());
                        return tCExecution;
                    }



                }
            } catch (CerberusException ex) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - No Linked environment found.");
            }
        }



        /**
         * Start Selenium server
         */
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            String url = tCExecution.getCountryEnvironmentApplication().getIp() + tCExecution.getCountryEnvironmentApplication().getUrl();
            String login = tCExecution.getCountryEnvironmentApplication().getUrlLogin();
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Starting Selenium Server.");
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_SELENIUMSTARTING));
            try {
                testCaseExecutionService.updateTCExecution(tCExecution);
            } catch (CerberusException ex) {
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            tCExecution.setResultMessage(this.seleniumService.startSeleniumServer(runID, tCExecution.getSeleniumIP(), tCExecution.getSeleniumPort(), tCExecution.getBrowser(), url, login, tCExecution.getVerbose(), tCExecution.getCountry()));
            /**
             * We stop if the result is not OK
             */
            if (!(tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING)))) {
                tCExecution.setEnd(new Date().getTime());
                try {
                    testCaseExecutionService.updateTCExecution(tCExecution);
                } catch (CerberusException ex) {
                    Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return tCExecution;
            }
        }


        /**
         * Load PreTestCase information and set PreTCase to the TCExecution
         * object
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading Pre testcases.");
        List<TCase> preTests = testCaseService.findTestCaseActiveByCriteria("Pre Testing", tCExecution.gettCase().getApplication(), tCExecution.getCountry());
        tCExecution.setPreTCase(preTests);
        if (!(preTests == null)) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loaded PreTest List. " + tCExecution.getPreTCase().size() + " found.");
        }


        /**
         * Load Main TestCase with Step dependencies (Actions/Control)
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading all Steps information of main testcase.");
        List<TestCaseStep> testCaseStepList;
        testCaseStepList = this.loadTestCaseService.loadTestCaseStep(tCExecution.gettCase());
        tCExecution.gettCase().setTestCaseStep(testCaseStepList);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loaded all Steps information of main testcase. " + tCExecution.gettCase().getTestCaseStep().size() + " Step(s) found.");

        /**
         * Load Pre TestCase with Step dependencies (Actions/Control)
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading all Steps information of all pre testcase.");
        //TODO Pretest this.loadTestCaseService.loadTestCaseStep(tCExecution.getPreTCase());
        List<TestCaseStep> preTestCaseStepList = new ArrayList<TestCaseStep>();
        List<TCase> preTestCase = new ArrayList<TCase>();
        for (TCase myTCase : tCExecution.getPreTCase()) {
            myTCase.setTestCaseStep(this.loadTestCaseService.loadTestCaseStep(myTCase));
            preTestCaseStepList.addAll(myTCase.getTestCaseStep());
            preTestCase.add(myTCase);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Pre testcase : " + myTCase.getTest() + "-" + myTCase.getTestCase() + " With " + myTCase.getTestCaseStep().size() + " Step(s) found.");
        }
        tCExecution.setPreTCase(preTestCase);


        /**
         * Start Execution of the steps/Actions/controls Iterate Steps.
         * mainExecutionTestCaseStepList will contain the list of steps to
         * execute for both pretest and test. This is where we schedule the
         * execution of the steps using mainExecutionTestCaseStepList object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Starting the execution with step iteration.");
        List<TestCaseStep> mainExecutionTestCaseStepList;
        mainExecutionTestCaseStepList = new ArrayList<TestCaseStep>();
        mainExecutionTestCaseStepList.addAll(preTestCaseStepList);
        mainExecutionTestCaseStepList.addAll(testCaseStepList);

        /**
         * Initialise the global TCExecution Data List.
         */
        // 
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING));
        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        List<TestCaseExecutionData> myExecutionDataList = new ArrayList<TestCaseExecutionData>();
        tCExecution.setTestCaseExecutionDataList(myExecutionDataList);

        for (TestCaseStep testCaseStep : mainExecutionTestCaseStepList) {

            /**
             * Start Execution of TestCaseStep
             */
            long startStep = new Date().getTime();

            /**
             * Create and Register TestCaseStepExecution
             */
            TestCaseStepExecution testCaseStepExecution = factoryTestCaseStepExecution.create(
                    runID, testCaseStep.getTest(), testCaseStep.getTestCase(),
                    testCaseStep.getStep(), null,
                    startStep, 0, startStep, 0, 0, null, new MessageEvent(MessageEventEnum.STEP_PENDING), testCaseStep, tCExecution);
            testCaseStepExecutionService.insertTestCaseStepExecution(testCaseStepExecution);
            testCaseStepExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

            /**
             * Execute Step
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Executing step : " + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());
            testCaseStepExecution = this.executeStep(testCaseStepExecution);

            /**
             * Adding the data to the execution datalist.
             */
            myExecutionDataList.addAll(testCaseStepExecution.getTestCaseExecutionDataList());
            tCExecution.setTestCaseExecutionDataList(myExecutionDataList);

            /**
             * Updating Execution Result Message only if execution result
             * message of the step is not PE or OK.
             */
            if ((!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED))))
                    && (!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))) {
                tCExecution.setResultMessage(testCaseStepExecution.getExecutionResultMessage());
            }
            if (testCaseStepExecution.getStepResultMessage().equals(new MessageEvent(MessageEventEnum.STEP_PENDING))) {
                testCaseStepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.STEP_SUCCESS));
            }

            testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

            if (testCaseStepExecution.isStopExecution()) {
                break;
            }
        }

        /**
         * Stop Execution
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Stop the execution.");
        this.stopRunTestCase(tCExecution);

        /**
         * Collecting and calculating Statistics.
         */
        this.collectExecutionStats(tCExecution);

        /**
         * Saving TCExecution object.
         */
        tCExecution.setEnd(new Date().getTime());
        /**
         * If at that time the execution is still PE, we move it to OK. It means
         * that no issue were met.
         */
        if ((tCExecution.getResultMessage() == null) || (tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED)))) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK));
        }
        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        return tCExecution;
    }

    private TestCaseStepExecution executeStep(TestCaseStepExecution testCaseStepExecution) {

        // Initialise the Step Data List.
        List<TestCaseExecutionData> myStepDataList = new ArrayList<TestCaseExecutionData>();
        testCaseStepExecution.setTestCaseExecutionDataList(myStepDataList);
        // Initialise the Data List used to enter the action.
        List<TestCaseExecutionData> myActionDataList = new ArrayList<TestCaseExecutionData>();
        /**
         * Iterate Actions
         */
        List<TestCaseStepAction> testCaseStepActionList = testCaseStepExecution.getTestCaseStep().getTestCaseStepAction();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Getting list of actions of the step. " + testCaseStepActionList.size() + " action(s) to perform.");

        for (TestCaseStepAction testCaseStepAction : testCaseStepActionList) {

            /**
             * Start Execution of TestCaseStepAction
             */
            long startAction = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionExecution.
             */
            TestCaseStepActionExecution testCaseStepActionExecution = factoryTestCaseStepActionExecution.create(
                    testCaseStepExecution.getId(), testCaseStepAction.getTest(), testCaseStepAction.getTestCase(),
                    testCaseStepAction.getStep(), testCaseStepAction.getSequence(),
                    null, null, testCaseStepAction.getAction(), testCaseStepAction.getObject(), testCaseStepAction.getProperty(),
                    startAction, 0, startAction, 0, null, new MessageEvent(MessageEventEnum.ACTION_PENDING), testCaseStepAction, testCaseStepExecution);
            this.testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);

            /**
             * Preparing the previously calculated data coming from 1/ the other
             * steps 2/ the one current step. Attaching them to the current
             * action execution.
             */
            myActionDataList.clear();
            myActionDataList.addAll(testCaseStepExecution.gettCExecution().getTestCaseExecutionDataList());
            myActionDataList.addAll(testCaseStepExecution.getTestCaseExecutionDataList());
            testCaseStepActionExecution.setTestCaseExecutionDataList(myActionDataList);

            /**
             * We calculate the property.
             */
            String propertyToCalculate = testCaseStepActionExecution.getProperty();
            if ((!propertyToCalculate.equals("null")) && !StringUtil.isNullOrEmpty(propertyToCalculate)) {
                /**
                 * Only calculate property if it is feed.
                 */
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Calculating property : " + propertyToCalculate);
                /**
                 * Calculating the data (Property).
                 */
                TestCaseExecutionData testCaseExecutionData = calculateProperty(testCaseStepActionExecution);
                /**
                 * Adding the calculated data to the current step Execution and
                 * ActionExecution.
                 */
                myStepDataList.add(testCaseExecutionData);
                testCaseStepExecution.setTestCaseExecutionDataList(myStepDataList);
                myActionDataList.add(testCaseExecutionData);
                testCaseStepActionExecution.setTestCaseExecutionDataList(myActionDataList);


                if (testCaseExecutionData.getPropertyResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS))) {
                    /**
                     * If property could be calculated, we execute the action.
                     */
                    testCaseStepActionExecution.setProperty(testCaseExecutionData.getValue());
                    testCaseStepActionExecution.setPropertyName(testCaseExecutionData.getProperty());
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing action : " + testCaseStepActionExecution.getAction() + " with property : " + testCaseStepActionExecution.getProperty());
                    testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution);
                } else {
                    /**
                     * Any other cases (Property does not exist or failed to be
                     * calculated), we just don't execute the action and move
                     * Property Execution message to the action.
                     */
                    testCaseStepActionExecution.setStopExecution(testCaseExecutionData.isStopExecution());
                    testCaseStepActionExecution.setExecutionResultMessage(testCaseExecutionData.getExecutionResultMessage());

                    /**
                     * Screenshot only done when : screenshot parameter is eq to
                     * 2 or screenshot parameter is eq to 1 with the correct
                     * doScreenshot flag on the last action MessageEvent.
                     */
                    if (((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 2)
                            || (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 1))
                            && (testCaseExecutionData.getPropertyResultMessage().isDoScreenshot())) {
                        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing screenshot.");
                        File myFile = null;
                        String screenshotFilename = testCaseStepActionExecution.getTest() + "-" + testCaseStepActionExecution.getTestCase()
                                + "-St" + testCaseStepActionExecution.getStep()
                                + "Sq" + testCaseStepActionExecution.getSequence() + ".jpg";
                        screenshotFilename = screenshotFilename.replaceAll(" ", "");
                        this.seleniumService.doScreenShot(Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()), screenshotFilename);
                        testCaseStepActionExecution.setScreenshotFilename(Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename);
                        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + testCaseStepActionExecution.getScreenshotFilename());
                    }

                    /**
                     * Register the empty Action in database.
                     */
                    if (testCaseExecutionData.getPropertyResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION))) {
                        MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                        mes.setDescription(mes.getDescription().replaceAll("%PROP%", testCaseStepActionExecution.getProperty()));
                        mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountry()));
                        testCaseStepActionExecution.setActionResultMessage(mes);
                    } else {
                        testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.ACTION_FAILED_PROPERTYFAILED));
                    }
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Action : " + testCaseStepActionExecution.getAction());
                    this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Action");

                }
            } else {
                /**
                 * If no property defined, we just execute the action.
                 */
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing action : " + testCaseStepActionExecution.getAction() + " without property.");
                testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution);
            }

            /**
             * If Action or property reported to stop the testcase, we stop it
             * and update the step with the message.
             */
            testCaseStepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
            if ((!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))
                    && (!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING))))) {
                testCaseStepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
            }
            if (testCaseStepActionExecution.isStopExecution()) {
                break;
            }

        }
        testCaseStepExecution.setEnd(new Date().getTime());
        this.testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

        return testCaseStepExecution;
    }

    private TestCaseStepActionExecution executeAction(TestCaseStepActionExecution testCaseStepActionExecution) {

        testCaseStepActionExecution = this.seleniumService.doAction(testCaseStepActionExecution);

        /**
         * Screenshot only done when : screenshot parameter is eq to 2 or
         * screenshot parameter is eq to 1 with the correct doScreenshot flag on
         * the last action MessageEvent.
         */
        if ((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 2)
                || ((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 1)
                && (testCaseStepActionExecution.getActionResultMessage().isDoScreenshot()))) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing screenshot.");
            File myFile = null;
            String screenshotFilename = testCaseStepActionExecution.getTest() + "-" + testCaseStepActionExecution.getTestCase()
                    + "-St" + testCaseStepActionExecution.getStep()
                    + "Sq" + testCaseStepActionExecution.getSequence() + ".jpg";
            screenshotFilename = screenshotFilename.replaceAll(" ", "");
            this.seleniumService.doScreenShot(Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()), screenshotFilename);
            testCaseStepActionExecution.setScreenshotFilename(Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + testCaseStepActionExecution.getScreenshotFilename());
        } else {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Not Doing screenshot after action because of the screenshot parameter or flag on the last Action result.");
        }


        /**
         * Register Action in database
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Action : " + testCaseStepActionExecution.getAction());
        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Action");


        if (testCaseStepActionExecution.isStopExecution()) {
            return testCaseStepActionExecution;
        }

        /**
         * Iterate Control
         */
        List<TestCaseStepActionControl> tcsacList = testCaseStepActionExecution.getTestCaseStepAction().getTestCaseStepActionControl();
        for (TestCaseStepActionControl testCaseStepActionControl : tcsacList) {

            /**
             * Start Execution of TestCAseStepActionControl
             */
            long startControl = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionControlExecution
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Creating TestCaseStepActionControlExecution");
            TestCaseStepActionControlExecution testCaseStepActionControlExecution =
                    factoryTestCaseStepActionControlExecution.create(testCaseStepActionExecution.getId(), testCaseStepActionControl.getTest(),
                    testCaseStepActionControl.getTestCase(), testCaseStepActionControl.getStep(), testCaseStepActionControl.getSequence(), testCaseStepActionControl.getControl(),
                    null, null, testCaseStepActionControl.getType(), testCaseStepActionControl.getControlProperty(), testCaseStepActionControl.getControlValue(),
                    testCaseStepActionControl.getFatal(), startControl, 0, 0, 0, null, testCaseStepActionExecution, new MessageEvent(MessageEventEnum.CONTROL_PENDING));
            this.testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);

            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing control : " + testCaseStepActionControlExecution.getControl() + " type : " + testCaseStepActionControlExecution.getControlType());
            testCaseStepActionControlExecution = executeControl(testCaseStepActionControlExecution);


            /**
             * We update the Action with the execution message and stop flag
             * from the control. We update the status only if the control is not
             * OK. This is to prevent moving the status to OK when it should
             * stay KO when a control failed previously.
             */
            testCaseStepActionExecution.setStopExecution(testCaseStepActionControlExecution.isStopExecution());
            if (!(testCaseStepActionControlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                testCaseStepActionExecution.setExecutionResultMessage(testCaseStepActionControlExecution.getExecutionResultMessage());
            }
            /**
             * If Control reported to stop the testcase, we stop it.
             */
            if (testCaseStepActionControlExecution.isStopExecution()) {
                break;
            }

        }

        return testCaseStepActionExecution;

    }

    private TestCaseStepActionControlExecution executeControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        testCaseStepActionControlExecution = this.controlService.doControl(testCaseStepActionControlExecution);

        TCExecution myExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();

        /**
         * Screenshot only done when : screenshot parameter is eq to 2 or
         * screenshot parameter is eq to 1 with the correct doScreenshot flag on
         * the last control MessageEvent.
         */
        if ((myExecution.getScreenshot() == 2)
                || ((myExecution.getScreenshot() == 1) && (testCaseStepActionControlExecution.getControlResultMessage().isDoScreenshot()))) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing screenshot.");
            File myFile = null;
            String screenshotFilename = testCaseStepActionControlExecution.getTest() + "-" + testCaseStepActionControlExecution.getTestCase()
                    + "-St" + testCaseStepActionControlExecution.getStep()
                    + "Sq" + testCaseStepActionControlExecution.getSequence()
                    + "Ct" + testCaseStepActionControlExecution.getControl() + ".jpg";
            screenshotFilename = screenshotFilename.replaceAll(" ", "");
            this.seleniumService.doScreenShot(Long.toString(myExecution.getId()), screenshotFilename);
            testCaseStepActionControlExecution.setScreenshotFilename(Long.toString(myExecution.getId()) + File.separator + screenshotFilename);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + testCaseStepActionControlExecution.getScreenshotFilename());
        } else {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Not Doing screenshot after control because of parameter of result of last control execution.");
        }
        /**
         * Register Control in database
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Control : " + testCaseStepActionControlExecution.getControl());
        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Control");

        return testCaseStepActionControlExecution;
    }

    private TCExecution stopRunTestCase(TCExecution tCExecution) {
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            try {
                this.seleniumService.stopSeleniumServer();
            } catch (UnreachableBrowserException exception) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, "Selenium didn't manage to close browser - " + exception.toString());
            }
        }
        return tCExecution;
    }

    private TCExecution collectExecutionStats(TCExecution tCExecution) {
        if (tCExecution.getVerbose() > 0) {
            this.testCaseExecutionWWWService.registerSummary(tCExecution.getId());
        }
        return tCExecution;
    }

    private TestCaseExecutionData calculateProperty(TestCaseStepActionExecution testCaseStepActionExecution) {
        String propertyName = testCaseStepActionExecution.getProperty();
        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String country = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountry();

        /**
         * Search for property already calculated
         */
        for (TestCaseExecutionData tced : testCaseStepActionExecution.getTestCaseExecutionDataList()) {
            if (tced.getProperty().equalsIgnoreCase(propertyName)) {
                return tced;
            }
        }

        /**
         * Create and initialize the TestCaseExecutionData object
         */
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_PENDING);
        long now = new Date().getTime();
        TestCaseExecutionData testCaseExecutionData = factoryTestCaseExecutionData.create(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId(), propertyName, null, null, null, null, null, now, now, now, now, msg);

        /**
         * Find TestCaseCountryProperty from database.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Getting TestCaseCountryProperty definition from database : " + test + "-" + testCase + "-" + country + "-" + propertyName);
        TestCaseCountryProperties testCaseCountryProperty = null;
        try {
            testCaseCountryProperty = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(test, testCase, country, propertyName);
            /**
             * Feed TestCaseExecutionData object
             */
            testCaseExecutionData.setType(testCaseCountryProperty.getType());
            testCaseExecutionData.setObject(testCaseCountryProperty.getValue());
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Calculating property : " + testCaseCountryProperty.getProperty());
            testCaseExecutionData = this.propertyService.calculateProperty(testCaseExecutionData, testCaseStepActionExecution, testCaseCountryProperty);
            try {
                /**
                 * Inserting testCaseExecutionData into the database.
                 */
                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
            } catch (CerberusException ex1) {
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            }

        } catch (CerberusException ex) {
            MessageEvent msg1 = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
            msg1.setDescription(msg1.getDescription().replaceAll("%COUNTRY%", country));
            msg1.setDescription(msg1.getDescription().replaceAll("%PROP%", propertyName));
            testCaseExecutionData.setPropertyResultMessage(msg1);

            try {
                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
            } catch (CerberusException ex1) {
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            }
        }

        return testCaseExecutionData;
    }
}
