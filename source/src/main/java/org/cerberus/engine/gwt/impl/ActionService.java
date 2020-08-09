/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.engine.gwt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.SwipeAction;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IActionService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.appium.IAppiumService;
import org.cerberus.service.appservice.IServiceService;
import org.cerberus.service.cerberuscommand.ICerberusCommand;
import org.cerberus.service.executor.IExecutorService;
import org.cerberus.service.har.IHarService;
import org.cerberus.service.rest.IRestService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.sikuli.impl.SikuliService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ActionService implements IActionService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private IRestService restService;
    @Autowired
    private IHarService harService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    @Qualifier("AndroidAppiumService")
    private IAppiumService androidAppiumService;
    @Autowired
    @Qualifier("IOSAppiumService")
    private IAppiumService iosAppiumService;
    @Autowired
    @Qualifier("CerberusCommand")
    private ICerberusCommand cerberusCommand;
    @Autowired
    private ISQLService sqlService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IServiceService serviceService;
    @Autowired
    private IExecutorService executorService;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;

    private static final Logger LOG = LogManager.getLogger(ActionService.class);
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";

    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent res;
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        /**
         * Decode the step action description
         */
        try {
            // When starting a new action, we reset the property list that was already calculated.
            tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getDescription(),
                    tCExecution, testCaseStepActionExecution, false);
            testCaseStepActionExecution.setDescription((String) answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Description"));
                testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                testCaseStepActionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Description' Error.");
                return testCaseStepActionExecution;
            }
        } catch (CerberusEventException cex) {
            testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            testCaseStepActionExecution.setEnd(new Date().getTime());
            return testCaseStepActionExecution;
        }

        /**
         * Decode the object field before doing the action.
         */
        try {

            // When starting a new action, we reset the property list that was already calculated.
            tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getValue1(),
                    tCExecution, testCaseStepActionExecution, false);
            testCaseStepActionExecution.setValue1((String) answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Value1"));
                testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                testCaseStepActionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Action Value1' Error.");
                return testCaseStepActionExecution;
            }
        } catch (CerberusEventException cex) {
            testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            testCaseStepActionExecution.setEnd(new Date().getTime());
            return testCaseStepActionExecution;
        }

        try {

            // When starting a new action, we reset the property list that was already calculated.
            tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getValue2(),
                    tCExecution, testCaseStepActionExecution, false);
            testCaseStepActionExecution.setValue2((String) answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Value2"));
                testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                testCaseStepActionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Action Value2' Error.");
                return testCaseStepActionExecution;
            }
        } catch (CerberusEventException cex) {
            testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            testCaseStepActionExecution.setEnd(new Date().getTime());
            return testCaseStepActionExecution;
        }

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionExecution.setStart(new Date().getTime());

        String value1 = testCaseStepActionExecution.getValue1();
        String value2 = testCaseStepActionExecution.getValue2();
        String value3 = testCaseStepActionExecution.getValue3();
        String propertyName = testCaseStepActionExecution.getPropertyName();
        LOG.debug("Doing Action : " + testCaseStepActionExecution.getAction() + " with value1 : " + value1 + " and value2 : " + value2 + " and value3 : " + value3);

        // When starting a new action, we reset the property list that was already calculated.
        tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

        try {
            switch (testCaseStepActionExecution.getAction()) {
                case TestCaseStepAction.ACTION_CLICK:
                    res = this.doActionClick(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_LONGPRESS:
                    res = this.doActionLongPress(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS:
                    res = this.doActionMouseLeftButtonPress(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE:
                    res = this.doActionMouseLeftButtonRelease(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_DOUBLECLICK:
                    res = this.doActionDoubleClick(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_RIGHTCLICK:
                    res = this.doActionRightClick(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSEOVER:
                    res = this.doActionMouseOver(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_FOCUSTOIFRAME:
                    res = this.doActionFocusToIframe(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME:
                    res = this.doActionFocusDefaultIframe(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_SWITCHTOWINDOW:
                    res = this.doActionSwitchToWindow(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MANAGEDIALOG:
                    res = this.doActionManageDialog(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MANAGEDIALOGKEYPRESS:
                    res = this.doActionManageDialogKeyPress(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_OPENURLWITHBASE:
                    res = this.doActionOpenURL(tCExecution, value1, value2, true);
                    break;
                case TestCaseStepAction.ACTION_OPENURLLOGIN:
                    testCaseStepActionExecution.setValue1(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountryEnvironmentParameters().getUrlLogin());
                    res = this.doActionUrlLogin(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_OPENURL:
                    res = this.doActionOpenURL(tCExecution, value1, value2, false);
                    break;
                case TestCaseStepAction.ACTION_REFRESHCURRENTPAGE:
                    res = this.doActionRefreshCurrentPage(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_EXECUTEJS:
                    res = this.doActionExecuteJS(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_EXECUTECOMMAND:
                    res = this.doActionExecuteCommand(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_EXECUTECERBERUSCOMMAND:
                    res = this.doActionExecuteCerberusCommand(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_OPENAPP:
                    res = this.doActionOpenApp(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_CLOSEAPP:
                    res = this.doActionCloseApp(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_DRAGANDDROP:
                    res = this.doActionDragAndDrop(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SELECT:
                    res = this.doActionSelect(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_KEYPRESS:
                    res = this.doActionKeyPress(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_TYPE:
                    res = this.doActionType(tCExecution, value1, value2, propertyName);
                    break;
                case TestCaseStepAction.ACTION_CLEARFIELD:
                    res = this.doActionClearField(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_HIDEKEYBOARD:
                    res = this.doActionHideKeyboard(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_SWIPE:
                    res = this.doActionSwipe(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SCROLLTO:
                    res = this.doActionScrollTo(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_INSTALLAPP:
                    res = this.doActionInstallApp(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_REMOVEAPP:
                    res = this.doActionRemoveApp(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_WAIT:
                    res = this.doActionWait(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_WAITVANISH:
                    res = this.doActionWaitVanish(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_WAITNETWORKTRAFFICIDLE:
                    res = this.doActionWaitNetworkTrafficIdle(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_CALLSERVICE:
                    res = this.doActionCallService(testCaseStepActionExecution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_EXECUTESQLUPDATE:
                    res = this.doActionExecuteSQLUpdate(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_EXECUTESQLSTOREPROCEDURE:
                    res = this.doActionExecuteSQLStoredProcedure(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_CALCULATEPROPERTY:
                    res = this.doActionCalculateProperty(testCaseStepActionExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SETNETWORKTRAFFICCONTENT:
                    res = this.doActionSetNetworkTrafficContent(tCExecution, testCaseStepActionExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SETSERVICECALLCONTENT:
                    res = this.doActionSetServiceCallContent(tCExecution, testCaseStepActionExecution);
                    break;
                case TestCaseStepAction.ACTION_DONOTHING:
                    res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    break;
                /**
                 * DEPRECATED ACTIONS FROM HERE.
                 */
                case TestCaseStepAction.ACTION_MOUSEOVERANDWAIT:
                    res = this.doActionMouseOverAndWait(tCExecution, value1, value2);
                    res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
                    logEventService.createForPrivateCalls("ENGINE", "mouseOverAndWait", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
                    LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action mouseOverAndWait triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");
                    break;
                case TestCaseStepAction.ACTION_REMOVEDIFFERENCE:
                    res = this.doActionRemoveDifference(testCaseStepActionExecution, value1, value2);
                    res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
                    logEventService.createForPrivateCalls("ENGINE", "removeDifference", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
                    LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action removeDifference triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");
                    break;
                default:
                    res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
                    res.setDescription(res.getDescription().replace("%ACTION%", testCaseStepActionExecution.getAction()));

            }
        } catch (final Exception unexpected) {
            LOG.error("Unexpected exception: " + unexpected.getMessage(), unexpected);
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC).resolveDescription("DETAIL", unexpected.getMessage());
        }

        LOG.debug("Result of the action : " + res.getCodeString() + " " + res.getDescription());

        /**
         * In case 1/ the action is flaged as being Forced with a specific
         * return code = PE and 2/ the return of the action is stoping the test
         * --> whatever the return of the action is, we force the return to move
         * forward the test with no screenshot, pagesource.
         */
        if (testCaseStepActionExecution.getForceExeStatus().equals("PE") && res.isStopTest()) {
            res.setDescription(res.getDescription() + " -- Execution forced to continue.");
            res.setDoScreenshot(false);
            res.setGetPageSource(false);
            res.setStopTest(false);
            res.setMessage(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING);
        }

        testCaseStepActionExecution.setActionResultMessage(res);

        /**
         * Determine here the impact of the Action on the full test return code
         * from the ResultMessage of the Action.
         */
        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));

        /**
         * Determine here if we stop the test from the ResultMessage of the
         * Action.
         */
        testCaseStepActionExecution.setStopExecution(res.isStopTest());

        testCaseStepActionExecution.setEnd(new Date().getTime());
        return testCaseStepActionExecution;
    }

    private MessageEvent doActionInstallApp(TestCaseExecution tCExecution, String appPath) {
        MessageEvent message;

        try {
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.installApp(tCExecution.getSession(), appPath);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.installApp(tCExecution.getSession(), appPath);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "scrollTo"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running install app  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionRemoveApp(TestCaseExecution tCExecution, String appPackage) {
        MessageEvent message;

        try {
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.removeApp(tCExecution.getSession(), appPackage);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.removeApp(tCExecution.getSession(), appPackage);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "scrollTo"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running remove app  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionScrollTo(TestCaseExecution tCExecution, String element, String text) {
        MessageEvent message;

        try {
            Identifier identifier = null;
            if (!StringUtil.isNullOrEmpty(element)) {
                identifier = identifierService.convertStringToIdentifier(element);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.scrollTo(tCExecution.getSession(), identifier, text);

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.scrollTo(tCExecution.getSession(), identifier, text);
                
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.scrollTo(tCExecution.getSession(), identifier, text);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "scrollTo"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running scroll to  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionExecuteCommand(TestCaseExecution tCExecution, String command, String args) {
        MessageEvent message;

        try {

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK) || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return androidAppiumService.executeCommand(tCExecution.getSession(), command, args);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_EXECUTECOMMAND));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%EXCEPTION%", messageString));
            LOG.debug("Exception Running Shell :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionExecuteCerberusCommand(TestCaseExecution tCExecution, String command) {

        MessageEvent message;

        try {
            return cerberusCommand.executeCerberusCommand(command);
        } catch (CerberusEventException e) {
            message = e.getMessageError();
            LOG.debug("Exception Running Shell :" + message.getMessage().getDescription());
            return message;
        }
    }

    private MessageEvent doActionClick(TestCaseExecution tCExecution, String value1, String value2) {
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, value2, TestCaseStepAction.ACTION_CLICK, tCExecution);
            /**
             * Get Identifier (identifier, locator) and check it's valid
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionClick(tCExecution.getSession(), identifier.getLocator(), "");
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", identifier.getLocator());
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.click(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.click(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", identifier.getLocator());
                }
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Click")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Click :" + ex, ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionExecuteJS(TestCaseExecution tCExecution, String value1, String value2) {

        MessageEvent message;
        String script = value1;
        String valueFromJS;
        try {

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {

                valueFromJS = this.webdriverService.getValueFromJS(tCExecution.getSession(), script);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTEJS);
                message.setDescription(message.getDescription().replace("%SCRIPT%", script));
                message.setDescription(message.getDescription().replace("%VALUE%", valueFromJS));
                return message;

            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "executeJS"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTEJS);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%EXCEPTION%", messageString));
            LOG.debug("Exception Running JS Script :" + messageString);
            return message;
        }
    }

    private MessageEvent doActionMouseLeftButtonPress(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseLeftButtonPress", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionMouseDown(tCExecution.getSession(), identifier, true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "MouseDown"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseDown :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionRightClick(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "rightClick", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionRightClick(tCExecution.getSession(), identifier);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), "", identifier.getLocator());
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "rightClick"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action RightClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseLeftButtonRelease(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseLeftButtonRelease", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionMouseUp(tCExecution.getSession(), identifier, true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "MouseUp"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseUp :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionSwitchToWindow(TestCaseExecution tCExecution, String object, String property) {
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "switchToWindow", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            //identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionSwitchToWindow(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.switchToContext(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.switchToContext(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                return sikuliService.doSikuliActionSwitchApp(tCExecution.getSession(), identifier.getLocator());
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "SwitchToWindow")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action SwitchToWindow :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionManageDialog(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, value2, "manageDialog", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionManageDialog(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "ManageDialog"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action ManageDialog :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionManageDialogKeyPress(TestCaseExecution tCExecution, String value1) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, "", "manageDialogKeyPress", tCExecution);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionManageDialogKeyPress(tCExecution.getSession(), element);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "ManageDialogKeypress"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action ManageDialogKeypress :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionDoubleClick(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "doubleClick", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), identifier.getLocator(), "");
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", identifier.getLocator());
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, true, false);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", identifier.getLocator());
                }
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "doubleClick"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action DoubleClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionType(TestCaseExecution tCExecution, String value1, String value2, String propertyName) {
        try {
            /**
             * Check object and property are not null for GUI/APK/IPA Check
             * property is not null for FAT Application
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (value1 == null || value2 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (value2 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            }
            /**
             * Get Identifier (identifier, locator) if object not null
             */
            Identifier identifier = new Identifier();
            if (value1 != null) {
                identifier = identifierService.convertStringToIdentifier(value1);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, value2, propertyName, false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionType(tCExecution.getSession(), identifier.getLocator(), value2);
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, value2, propertyName, true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.type(tCExecution.getSession(), identifier, value2, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.type(tCExecution.getSession(), identifier, value2, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                String locator = "";
                if (!StringUtil.isNullOrEmpty(value1)) {
                    identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                    locator = identifier.getLocator();
                }
                return sikuliService.doSikuliActionType(tCExecution.getSession(), locator, value2);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Type")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Type : " + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseOver(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseOver", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier, false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "");
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator());
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier, true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator());
                }
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "mouseOver"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseOver :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseOverAndWait(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        try {
            /**
             * Check object is not null
             */
            if (object == null) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_GENERIC);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(object);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    message = sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    message = sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    message = webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier, true, true);
                }
                if (message.getCodeString().equals("OK")) {
                    message = this.doActionWait(tCExecution, property, null);
                }
                return message;
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "mouseOverAndWait"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseOverAndWait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionWait(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        String element;
        long timeToWaitInMs = 0;
        Identifier identifier = null;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, null if both are empty
             */
            element = getElementToUse(value1, value2, "wait", tCExecution);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) { // If application are Selenium or appium based, we have a session and can use it to wait.

                /**
                 * if element is integer, set time to that value else Get
                 * Identifier (identifier, locator)
                 */
                if (StringUtil.isNullOrEmpty(element)) {
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isInteger(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                } else {
                    identifier = identifierService.convertStringToIdentifier(element);
                }

                if (identifier != null && identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWait(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier != null && identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionWait(tCExecution.getSession(), "", identifier.getLocator());
                } else if (identifier != null) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                        return androidAppiumService.wait(tCExecution.getSession(), identifier);
                    } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                        return iosAppiumService.wait(tCExecution.getSession(), identifier);
                    } else {
                        return webdriverService.doSeleniumActionWait(tCExecution.getSession(), identifier);
                    }
                } else {
                    return this.waitTime(timeToWaitInMs);
                }
            } else { // For any other application we wait for the integer value.
                if (StringUtil.isNullOrEmpty(element)) {
                    // Get default wait from parameter
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isInteger(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                }
                return this.waitTime(timeToWaitInMs);
            }

        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Wait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionKeyPress(TestCaseExecution tCExecution, String value1, String value2) {
        try {
            String appType = tCExecution.getApplicationObj().getType();
            /**
             * Check value1 and value2 are not null For IPA and APK, only value2
             * (key to press) is mandatory For GUI and FAT, both parameters are
             * mandatory
             */
            if (StringUtil.isNullOrEmpty(value2)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_MISSINGKEY).resolveDescription("APPLICATIONTYPE", appType);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            if (StringUtil.isNullOrEmpty(value1) && appType.equalsIgnoreCase(Application.TYPE_GUI)) {
                value1 = "xpath=//body";
            }
            Identifier objectIdentifier = identifierService.convertStringToIdentifier(value1);

            if (appType.equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.MAC.toString())
                        || tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.IOS.toString())) {
                    return iosAppiumService.keyPress(tCExecution.getSession(), value2);

                } else if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), objectIdentifier, value2, false, false);
                }
                if (objectIdentifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionKeyPress(tCExecution.getSession(), objectIdentifier.getLocator(), value2);

                } else {
                    identifierService.checkWebElementIdentifier(objectIdentifier.getIdentifier());
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), objectIdentifier, value2, true, true);
                }

            } else if (appType.equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.keyPress(tCExecution.getSession(), value2);

            } else if (appType.equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.keyPress(tCExecution.getSession(), value2);

            } else if (appType.equalsIgnoreCase(Application.TYPE_FAT)) {
                return sikuliService.doSikuliActionKeyPress(tCExecution.getSession(), objectIdentifier.getLocator(), value2);

            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "KeyPress")
                        .resolveDescription("APPLICATIONTYPE", appType);
            }
        } catch (CerberusEventException ex) {
            LOG.debug("Error doing Action KeyPress :" + ex);
            return ex.getMessageError();

        } catch (Exception ex) {
            LOG.debug("Error doing Action KeyPress :" + ex);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", ex.toString());
        }
    }

    private MessageEvent doActionOpenURL(TestCaseExecution tCExecution, String object, String property, boolean withBase) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "openUrl[WithBase]", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = new Identifier();
            identifier.setIdentifier("url");
            identifier.setLocator(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionOpenURL(tCExecution.getSession(), tCExecution.getUrl(), identifier, withBase);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "OpenURL[WithBase]"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action OpenUrl :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionOpenApp(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;

        /**
         * Check value1 is not null or empty
         */
        if (value1 == null || "".equals(value1)) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENAPP);
        }

        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return sikuliService.doSikuliActionOpenApp(tCExecution.getSession(), value1);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
            return androidAppiumService.openApp(tCExecution.getSession(), value1, value2);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            return iosAppiumService.openApp(tCExecution.getSession(), value1, value2);

        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "OpenApp"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionCloseApp(TestCaseExecution tCExecution, String value1) {
        MessageEvent message;

        /**
         * Check value1 is not null or empty
         */
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            if (value1 == null || "".equals(value1)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
            }
            return sikuliService.doSikuliActionCloseApp(tCExecution.getSession(), value1);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
            return androidAppiumService.closeApp(tCExecution.getSession());

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            return iosAppiumService.closeApp(tCExecution.getSession());
        }

        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "CloseApp"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionWaitVanish(TestCaseExecution tCExecution, String value1) {
        try {
            /**
             * Check value1 is not null or empty
             */
            if (value1 == null || "".equals(value1)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(value1);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionWaitVanish(tCExecution.getSession(), identifier);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return webdriverService.doSeleniumActionWaitVanish(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), "", identifier.getLocator());
                }
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "WaitVanish")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action KeyPress :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionWaitNetworkTrafficIdle(TestCaseExecution tCExecution) {
        try {

            return executorService.waitForIdleNetwork(tCExecution.getRobotExecutorObj().getExecutorExtensionHost(), tCExecution.getRobotExecutorObj().getExecutorExtensionPort(),
                    tCExecution.getRemoteProxyUUID(), tCExecution.getSystem());

        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action WaitNetworkTrafficIdle :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionSelect(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        try {
            /**
             * Check object and property are not null
             */
            if (StringUtil.isNullOrEmpty(value1) || StringUtil.isNullOrEmpty(value2)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifierObject = identifierService.convertStringToIdentifier(value1);
            Identifier identifierValue = identifierService.convertStringToSelectIdentifier(value2);

            identifierService.checkWebElementIdentifier(identifierObject.getIdentifier());
            identifierService.checkSelectOptionsIdentifier(identifierValue.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierObject, identifierValue, false, false);
                }
                return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierObject, identifierValue, true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "Select"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Select :" + ex);
            return ex.getMessageError();
        }

    }

    private MessageEvent doActionUrlLogin(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return webdriverService.doSeleniumActionUrlLogin(tCExecution.getSession(), tCExecution.getUrl(), tCExecution.getCountryEnvironmentParameters().getUrlLogin());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "UrlLogin"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionFocusToIframe(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "focusToIframe", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionFocusToIframe(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "FocusToIframe"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action FocusToIframe :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionFocusDefaultIframe(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return webdriverService.doSeleniumActionFocusDefaultIframe(tCExecution.getSession());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "FocusDefaultIframe"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;

    }

    public MessageEvent doActionDragAndDrop(TestCaseExecution tCExecution, String value1, String value2) throws IOException {
        MessageEvent message;
        try {
            /**
             * Check source and target are not null
             */
            if (StringUtil.isNullOrEmpty(value1)) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP);
                message.setDescription(message.getDescription().replace("%ELEMENT%", value1));
                return message;
            } else if (StringUtil.isNullOrEmpty(value2)) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP);
                message.setDescription(message.getDescription().replace("%ELEMENT%", value2));
                return message;
            }
            Identifier identifierDrag = identifierService.convertStringToIdentifier(value1);
            Identifier identifierDrop = identifierService.convertStringToIdentifier(value2);
            identifierService.checkWebElementIdentifier(identifierDrag.getIdentifier());
            identifierService.checkWebElementIdentifier(identifierDrop.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionDragAndDrop(tCExecution.getSession(), identifierDrag, identifierDrop, true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "Select"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action DragAndDrop :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionCallService(TestCaseStepActionExecution testCaseStepActionExecution, String value1, String value2, String value3) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        AnswerItem lastServiceCalledAnswer;

        lastServiceCalledAnswer = serviceService.callService(value1, value2, value3, null, null, null, null, tCExecution);
        message = lastServiceCalledAnswer.getResultMessage();

        if (lastServiceCalledAnswer.getItem() != null) {
            AppService lastServiceCalled = (AppService) lastServiceCalledAnswer.getItem();
            tCExecution.setLastServiceCalled(lastServiceCalled);
            tCExecution.setOriginalLastServiceCalled(lastServiceCalled.getResponseHTTPBody());
            tCExecution.setOriginalLastServiceCalledContent(lastServiceCalled.getResponseHTTPBodyContentType());

            /**
             * Record the Request and Response in file system.
             */
            testCaseStepActionExecution.addFileList(recorderService.recordServiceCall(tCExecution, testCaseStepActionExecution, 0, null, lastServiceCalled));
        }

        return message;

    }

    private MessageEvent doActionRemoveDifference(TestCaseStepActionExecution testCaseStepActionExecution, String object, String property) {
        // Filters differences from the given object pattern
        String filteredDifferences = xmlUnitService.removeDifference(object, property);

        // If filtered differences are null then service has returned with errors
        if (filteredDifferences == null) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_REMOVEDIFFERENCE);
            message.setDescription(message.getDescription().replace("%DIFFERENCE%", object));
            message.setDescription(message.getDescription().replace("%DIFFERENCES%", property));
            return message;
        }

        // Sets the property value to the new filtered one
        for (TestCaseExecutionData data : testCaseStepActionExecution.getTestCaseExecutionDataList()) {
            if (data.getProperty().equals(testCaseStepActionExecution.getPropertyName())) {
                data.setValue(filteredDifferences);
                break;
            }
        }

        // Sends success
        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_REMOVEDIFFERENCE);
        message.setDescription(message.getDescription().replace("%DIFFERENCE%", object));
        message.setDescription(message.getDescription().replace("%DIFFERENCES%", property));
        return message;
    }

    private MessageEvent doActionCalculateProperty(TestCaseStepActionExecution testCaseStepActionExecution, String value1, String value2) {
        MessageEvent message;
        AnswerItem<String> answerDecode = new AnswerItem<>();
        if (StringUtil.isNullOrEmpty(value1)) {

            // Value1 is a mandatory parameter.
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_MISSINGPROPERTY);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY));

        } else {
            try {

                TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                // Getting the Country property definition.
                TestCaseCountryProperties tccp = null;
                boolean propertyExistOnAnyCountry = false;
                for (TestCaseCountryProperties object : tCExecution.getTestCaseCountryPropertyList()) {
                    if ((object.getProperty().equalsIgnoreCase(value1)) && (object.getCountry().equalsIgnoreCase(tCExecution.getCountry()))) {
                        tccp = object;
                    }
                    if ((object.getProperty().equalsIgnoreCase(value1))) {
                        propertyExistOnAnyCountry = true;
                    }
                }
                if (tccp == null) { // Could not find a country property inside the existing execution.
                    if (propertyExistOnAnyCountry) {
                        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                .replace("%PROP%", value1)
                                .replace("%COUNTRY%", tCExecution.getCountry()));
                        return message;
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_PROPERTYNOTFOUND);
                        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                .replace("%PROP%", value1)
                                .replace("%COUNTRY%", tCExecution.getCountry()));
                        return message;
                    }

                } else {
                    if (!(StringUtil.isNullOrEmpty(value2))) {
                        // If value2 is fed with something, we control here that value is a valid property name and gets its defintion.
                        tccp = null;
                        propertyExistOnAnyCountry = false;
                        for (TestCaseCountryProperties object : tCExecution.getTestCaseCountryPropertyList()) {
                            if ((object.getProperty().equalsIgnoreCase(value2)) && (object.getCountry().equalsIgnoreCase(tCExecution.getCountry()))) {
                                tccp = object;
                            }
                            if ((object.getProperty().equalsIgnoreCase(value2))) {
                                propertyExistOnAnyCountry = true;
                            }
                        }
                        if (tccp == null) { // Could not find a country property inside the existing execution.
                            if (propertyExistOnAnyCountry) {
                                message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                                message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                        .replace("%PROP%", value2)
                                        .replace("%COUNTRY%", tCExecution.getCountry()));
                                return message;

                            } else {
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_PROPERTYNOTFOUND);
                                message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                        .replace("%PROP%", value2)
                                        .replace("%COUNTRY%", tCExecution.getCountry()));
                                return message;

                            }
                        }
                    }

                    // We calculate the property here.
                    long now = new Date().getTime();
                    TestCaseExecutionData tcExeData;

                    tcExeData = factoryTestCaseExecutionData.create(tCExecution.getId(), tccp.getProperty(), 1, tccp.getDescription(), null, tccp.getType(),
                            tccp.getRank(), tccp.getValue1(), tccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
                            tccp.getRetryNb(), tccp.getRetryPeriod(), tccp.getDatabase(), tccp.getValue1(), tccp.getValue2(), tccp.getLength(), tccp.getLength(),
                            tccp.getRowLimit(), tccp.getNature(), "", "", "", "", "", "N");
                    tcExeData.setTestCaseCountryProperties(tccp);
                    propertyService.calculateProperty(tcExeData, tCExecution, testCaseStepActionExecution, tccp, true);
                    // Property message goes to Action message.
                    message = tcExeData.getPropertyResultMessage();
                    if (message.getCodeString().equals("OK")) {
                        // If Property calculated successfully we summarize the message to a shorter version.
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALCULATEPROPERTY);
                        message.setDescription(message.getDescription()
                                .replace("%PROP%", value1)
                                .replace("%VALUE%", tcExeData.getValue()));
                        if (tcExeData.getDataLibRawData() != null) {
                            message.setDescription(message.getDescription() + " %NBROWS% row(s) with %NBSUBDATA% Subdata(s) calculated."
                                    .replace("%NBROWS%", String.valueOf(tcExeData.getDataLibRawData().size()))
                                    .replace("%NBSUBDATA%", String.valueOf(tcExeData.getDataLibRawData().get(0).size())));
                        }
                    }

                    if (!(StringUtil.isNullOrEmpty(value2))) {
                        // If value2 is fed we force the result to value1.
                        tcExeData.setProperty(value1);
                    }
                    //saves the result
                    try {
                        testCaseExecutionDataService.save(tcExeData);
                        LOG.debug("Adding into Execution data list. Property : '" + tcExeData.getProperty() + "' Index : '" + tcExeData.getIndex() + "' Value : '" + tcExeData.getValue() + "'");
                        tCExecution.getTestCaseExecutionDataMap().put(tcExeData.getProperty(), tcExeData);
                        if (tcExeData.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                            for (int i = 1; i < (tcExeData.getDataLibRawData().size()); i++) {
                                now = new Date().getTime();
                                TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tcExeData.getId(), tcExeData.getProperty(), (i + 1),
                                        tcExeData.getDescription(), tcExeData.getDataLibRawData().get(i).get(""), tcExeData.getType(), tcExeData.getRank(), "", "",
                                        tcExeData.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", "", "", 0, "", "", "", "", "", "", "N");
                                testCaseExecutionDataService.save(tcedS);
                            }
                        }
                    } catch (CerberusException cex) {
                        LOG.error(cex.getMessage(), cex);
                    }

                }

            } catch (Exception ex) {
                LOG.error(ex.toString(), ex);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC).resolveDescription("DETAIL", ex.toString());
            }
        }
        return message;
    }

    public MessageEvent doActionSetNetworkTrafficContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe, String urlToFilter, String withResponseContent) throws IOException {
        MessageEvent message;
        try {
            // Check that robot has executor activated
            if (!"Y".equalsIgnoreCase(exe.getRobotExecutorObj().getExecutorProxyActive()) || StringUtil.isNullOrEmpty(exe.getRobotExecutorObj().getExecutorProxyHost())) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETNETWORKTRAFFICCONTENT_ROBOTEXECUTORPROXYNOTACTIVATED);
                message.setDescription(message.getDescription().replace("%ROBOT%", exe.getRobotExecutorObj().getRobot()).replace("%EXECUTOR%", exe.getRobotExecutorObj().getExecutor()));
                return message;
            }

            /**
             * Building the url to get the Har file from cerberus-executor
             */
            String url = executorService.getExecutorURL(urlToFilter, ParameterParserUtil.parseBooleanParam(withResponseContent, false),
                    exe.getRobotExecutorObj().getExecutorExtensionHost(), exe.getRobotExecutorObj().getExecutorExtensionPort(), exe.getRemoteProxyUUID());

            LOG.debug("Getting Network Traffic content from URL : " + url);

            AnswerItem<AppService> result = new AnswerItem<>();
            result = restService.callREST(url, "", AppService.METHOD_HTTPGET, new ArrayList<>(), new ArrayList<>(), null, 10000, "", exe);

            AppService appSrv = result.getItem();
            JSONObject har = new JSONObject(appSrv.getResponseHTTPBody());

            har = harService.enrichWithStats(har, exe.getCountryEnvironmentParameters().getDomain(), exe.getSystem());
            appSrv.setResponseHTTPBody(har.toString());
            appSrv.setResponseHTTPBodyContentType(AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON);
            appSrv.setRecordTraceFile(false);

            exe.setLastServiceCalled(appSrv);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordNetworkTrafficContent(exe, actionexe, 0, null, result.getItem(), true));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETNETWORKTRAFFICCONTENT);
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setNetworkTrafficContent :" + ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETNETWORKTRAFFICCONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    public MessageEvent doActionSetServiceCallContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe) throws IOException {
        MessageEvent message;
        try {
            
            // Check that robot has executor activated
            if (exe.getLastServiceCalled() == null) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETSERVICECALLCONTENT_NOLASTCALLDONE);
                return message;
            }
            
            // Force last service call content to JSON Service Call Structure & disable file save.
            exe.getLastServiceCalled().setResponseHTTPBody(exe.getLastServiceCalled().toJSONOnExecution().toString());
            exe.getLastServiceCalled().setResponseHTTPBodyContentType(AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON);
            exe.getLastServiceCalled().setRecordTraceFile(false);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordServiceCallContent(exe, actionexe, exe.getLastServiceCalled()));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETSERVICECALLCONTENT);
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setServiceCallContent :" + ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETSERVICECALLCONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    private String getElementToUse(String value1, String value2, String action, TestCaseExecution tCExecution) throws CerberusEventException {
        if (!StringUtil.isNullOrEmpty(value1)) {
            return value1;
        } else if (!StringUtil.isNullOrEmpty(value2)) {
            logEventService.createForPrivateCalls("ENGINE", action, MESSAGE_DEPRECATED + " Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + tCExecution.getTest() + "'|'" + tCExecution.getTestCase() + "'] Property : " + value2);
            LOG.warn(MESSAGE_DEPRECATED + " Action : " + action + ". Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + tCExecution.getTest() + "'|'" + tCExecution.getTestCase() + "'] Property : " + value2);
            return value2;
        }
        if (!(action.equals("wait"))) { // Wait is the only action can be excuted with no parameters. For all other actions we raize an exception as this should never happen.
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_PERFORM_ACTION);
            message.setDescription(message.getDescription().replace("%ACTION%", action));
            throw new CerberusEventException(message);
        }
        return null;
    }

    private MessageEvent waitTime(Long timeToWaitMs) {
        MessageEvent message;
        /**
         * if timeToWait is null, throw CerberusException
         */
        if (timeToWaitMs == 0) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_INVALID_FORMAT);
            return message;
        }
        try {
            LOG.debug("TIME TO WAIT = " + timeToWaitMs);
            Thread.sleep(timeToWaitMs);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
            message.setDescription(message.getDescription().replace("%TIME%", String.valueOf(timeToWaitMs)));
            return message;
        } catch (InterruptedException exception) {
            LOG.warn(exception.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME_WITHWARNINGS);
            message.setDescription(message.getDescription()
                    .replace("%TIME%", String.valueOf(timeToWaitMs))
                    .replace("%MESSAGE%", exception.toString()));
            return message;
        }
    }

    private MessageEvent doActionExecuteSQLUpdate(TestCaseExecution tCExecution, String object, String property) {
        return sqlService.executeUpdate(tCExecution.getApplicationObj().getSystem(),
                tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
    }

    private MessageEvent doActionExecuteSQLStoredProcedure(TestCaseExecution tCExecution, String object, String property) {
        return sqlService.executeCallableStatement(tCExecution.getApplicationObj().getSystem(),
                tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
    }

    private MessageEvent doActionHideKeyboard(TestCaseExecution tCExecution) {
        // Check argument
        if (tCExecution == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Hide keyboard according to application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.hideKeyboard(tCExecution.getSession());
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.hideKeyboard(tCExecution.getSession());
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "Hide keyboard")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionSwipe(TestCaseExecution tCExecution, String object, String property) {
        // Check arguments
        if (tCExecution == null || object == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Create the associated swipe action to the given arguments
        SwipeAction action = null;
        try {
            action = SwipeAction.fromStrings(object, property);
        } catch (Exception e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                    .resolveDescription("DIRECTION", action == null ? "Unknown" : action.getActionType().name())
                    .resolveDescription("REASON", e.getMessage());
        }

        // Swipe screen according to the application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.swipe(tCExecution.getSession(), action);
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.swipe(tCExecution.getSession(), action);
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "Swipe screen")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionLongPress(TestCaseExecution tCExecution, String value1, String value2) {
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, value2, TestCaseStepAction.ACTION_LONGPRESS, tCExecution);
            /**
             * Get Identifier (identifier, locator) and check it's valid
             */
            Integer longPressTime = 8000;
            try {
                longPressTime = Integer.parseInt(value2);
            } catch (NumberFormatException e) {
                // do nothing
            }
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.longPress(tCExecution.getSession(), identifier, longPressTime);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.longPress(tCExecution.getSession(), identifier, longPressTime);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Long Click")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Click :" + ex, ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionClearField(TestCaseExecution tCExecution, String value1) {
        String element;
        try {
            /**
             * Check object and property are not null for GUI/APK/IPA Check
             * property is not null for FAT Application
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (value1 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLEARFIELD);
                }
            }
            /**
             * Get Identifier (identifier, locator) if object not null
             */
            Identifier identifier = new Identifier();
            if (value1 != null) {
                identifier = identifierService.convertStringToIdentifier(value1);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.clearField(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.clearField(tCExecution.getSession(), identifier);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "ClearField")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Type : " + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionRefreshCurrentPage(TestCaseExecution tCExecution) {
        MessageEvent message;

        try {
            LOG.debug("REFRESH CURRENT PAGE");
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return this.webdriverService.doSeleniumActionRefreshCurrentPage(tCExecution.getSession());
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "refreshCurrentPage"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_REFRESHCURRENTPAGE);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception doing action refreshCurrentPage  :" + messageString, e);
            return message;
        }
    }

}
