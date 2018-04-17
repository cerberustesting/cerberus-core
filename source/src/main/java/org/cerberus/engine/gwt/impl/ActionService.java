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

import java.util.ArrayList;
import java.util.Date;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
import org.cerberus.service.rest.IRestService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.sikuli.impl.SikuliService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
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
        String propertyName = testCaseStepActionExecution.getPropertyName();
        LOG.debug("Doing Action : " + testCaseStepActionExecution.getAction() + " with value1 : " + value1 + " and value2 : " + value2);

        // When starting a new action, we reset the property list that was already calculated.
        tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

        try {
            switch (testCaseStepActionExecution.getAction()) {
                case TestCaseStepAction.ACTION_CLICK:
                    res = this.doActionClick(tCExecution, value1, value2);
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
                case TestCaseStepAction.ACTION_EXECUTEJS:
                    res = this.doActionExecuteJS(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_OPENAPP:
                    res = this.doActionOpenApp(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_CLOSEAPP:
                    res = this.doActionCloseApp(tCExecution, value1);
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
                case TestCaseStepAction.ACTION_HIDEKEYBOARD:
                    res = this.doActionHideKeyboard(tCExecution);
                    break;
                case TestCaseStepAction.ACTION_SWIPE:
                    res = this.doActionSwipe(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_WAIT:
                    res = this.doActionWait(tCExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_WAITVANISH:
                    res = this.doActionWaitVanish(tCExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_CALLSERVICE:
                    res = this.doActionCallService(testCaseStepActionExecution, value1);
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
                case TestCaseStepAction.ACTION_DONOTHING:
                    res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    break;
                case TestCaseStepAction.ACTION_EXECUTESHELL:
                    res = this.doActionExecuteShell(tCExecution, value1, value2);
                    break;
                // DEPRECATED ACTIONS FROM HERE.
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

    private MessageEvent doActionExecuteShell(TestCaseExecution tCExecution, String command, String args) {
        MessageEvent message;

        try {

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.executeShell(tCExecution.getSession(), command, args);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "executeJS"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTESHELL);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%EXCEPTION%", messageString));
            LOG.debug("Exception Running Shell :" + messageString,e);
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
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
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
            LOG.fatal("Error doing Action Click :" + ex);
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
                return webdriverService.doSeleniumActionMouseDown(tCExecution.getSession(), identifier);
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
                return webdriverService.doSeleniumActionMouseUp(tCExecution.getSession(), identifier);
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

    private MessageEvent doActionManageDialog(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "manageDialog", tCExecution);
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
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, true, true);
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

    private MessageEvent doActionType(TestCaseExecution tCExecution, String object, String property, String propertyName) {
        try {
            /**
             * Check object and property are not null for GUI/APK/IPA Check
             * property is not null for FAT Application
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (object == null || property == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (property == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            }
            /**
             * Get Identifier (identifier, locator) if object not null
             */
            Identifier identifier = new Identifier();
            if (object != null) {
                identifier = identifierService.convertStringToIdentifier(object);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionType(tCExecution.getSession(), identifier.getLocator(), property);
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, property, propertyName);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.type(tCExecution.getSession(), identifier, property, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.type(tCExecution.getSession(), identifier, property, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                String locator = "";
                if (!StringUtil.isNullOrEmpty(object)) {
                    identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                    locator = identifier.getLocator();
                }
                return sikuliService.doSikuliActionType(tCExecution.getSession(), locator, property);
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
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier);
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
                    message = webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier);
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

    private MessageEvent doActionWait(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        long timeToWaitInMs = 0;
        Identifier identifier = null;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, null if both are empty
             */
            element = getElementToUse(object, property, "wait", tCExecution);

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
                    return webdriverService.doSeleniumActionWait(tCExecution.getSession(), identifier);
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
             * Check object and property are not null For IPA and APK, only
             * value2 (key to press) is mandatory For GUI and FAT, both
             * parameters are mandatory
             */
//            if (appType.equalsIgnoreCase(Application.TYPE_APK) || appType.equalsIgnoreCase(Application.TYPE_IPA)) {
            if (StringUtil.isNullOrEmpty(value2)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_MISSINGKEY).resolveDescription("APPLICATIONTYPE", appType);
            }
//            } else if (appType.equalsIgnoreCase(Application.TYPE_GUI) || appType.equalsIgnoreCase(Application.TYPE_FAT)) {
//                if (StringUtil.isNullOrEmpty(value1) || StringUtil.isNullOrEmpty(value2)) {
//                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS);
//                }
//            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier objectIdentifier = identifierService.convertStringToIdentifier(value1);

            if (appType.equalsIgnoreCase(Application.TYPE_GUI)) {
                if (objectIdentifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionKeyPress(tCExecution.getSession(), objectIdentifier.getLocator(), value2);
                } else {
                    identifierService.checkWebElementIdentifier(objectIdentifier.getIdentifier());
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), objectIdentifier, value2);
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
            LOG.fatal("Error doing Action KeyPress :" + ex);
            return ex.getMessageError();
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

    private MessageEvent doActionOpenApp(TestCaseExecution tCExecution, String value1) {
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
        if (value1 == null || "".equals(value1)) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
        }

        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return sikuliService.doSikuliActionCloseApp(tCExecution.getSession(), value1);
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
                return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierObject, identifierValue);
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

    private MessageEvent doActionCallService(TestCaseStepActionExecution testCaseStepActionExecution, String value1) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        AnswerItem lastServiceCalledAnswer;

        lastServiceCalledAnswer = serviceService.callService(value1, null, null, null, null, tCExecution);
        message = lastServiceCalledAnswer.getResultMessage();

        if (lastServiceCalledAnswer.getItem() != null) {
            AppService lastServiceCalled = (AppService) lastServiceCalledAnswer.getItem();
            tCExecution.setLastServiceCalled(lastServiceCalled);

            /**
             * Record the Request and Response in filesystem.
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
                            tccp.getValue1(), tccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
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
                        testCaseExecutionDataService.convert(testCaseExecutionDataService.save(tcExeData));
                        LOG.debug("Adding into Execution data list. Property : '" + tcExeData.getProperty() + "' Index : '" + tcExeData.getIndex() + "' Value : '" + tcExeData.getValue() + "'");
                        tCExecution.getTestCaseExecutionDataMap().put(tcExeData.getProperty(), tcExeData);
                        if (tcExeData.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                            for (int i = 1; i < (tcExeData.getDataLibRawData().size()); i++) {
                                now = new Date().getTime();
                                TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tcExeData.getId(), tcExeData.getProperty(), (i + 1),
                                        tcExeData.getDescription(), tcExeData.getDataLibRawData().get(i).get(""), tcExeData.getType(), "", "",
                                        tcExeData.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", "", "", 0, "", "", "", "", "", "", "N");
                                testCaseExecutionDataService.convert(testCaseExecutionDataService.save(tcedS));
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
            LOG.info(exception.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
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

}
