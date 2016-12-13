/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import org.cerberus.engine.execution.impl.RunTestCaseService;
import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.engine.entity.SwipeAction;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ISoapLibraryService;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.engine.gwt.IActionService;
import org.cerberus.service.appium.IAppiumService;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.soap.ISoapService;
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
    private IPropertyService propertyService;
    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private ISoapLibraryService soapLibraryService;
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

    private static final Logger LOG = Logger.getLogger(ActionService.class);
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";

    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent res;

        /**
         * Decode the object field before doing the action.
         */
        if (false) {
            if (testCaseStepActionExecution.getValue1().contains("%")) {
                boolean isCalledFromCalculateProperty = false;
                if (testCaseStepActionExecution.getAction().equals("calculateProperty")) {
                    isCalledFromCalculateProperty = true;
                }
                try {
                    // We decode here the object with any potencial variables (ex : %TOTO%). If the Current action if calculateProperty, we force a new calculation of the Property.
                    testCaseStepActionExecution.setValue1(variableService.decodeVariableWithExistingObject(testCaseStepActionExecution.getValue1(), testCaseStepActionExecution, isCalledFromCalculateProperty));
                    //if the getvalue() indicates that the execution should stop then we stop it before the doAction 
                    //or if the property service was unable to decode the property that is specified in the object, 
                    //then the execution of this action should not performed
                    if (testCaseStepActionExecution.isStopExecution()
                            || (testCaseStepActionExecution.getActionResultMessage().getCode()
                            == MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION.getCode())) {
                        return testCaseStepActionExecution;
                    }
                } catch (CerberusEventException cex) {
                    testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
                    testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
                    return testCaseStepActionExecution;
                }
            }
        }

        try {
            testCaseStepActionExecution.setValue1(variableService.decodeVariableWithExistingObject(testCaseStepActionExecution.getValue1(), testCaseStepActionExecution, false));
        } catch (CerberusEventException cex) {
            testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            return testCaseStepActionExecution;
        }
        try {
            testCaseStepActionExecution.setValue2(variableService.decodeVariableWithExistingObject(testCaseStepActionExecution.getValue2(), testCaseStepActionExecution, false));
        } catch (CerberusEventException cex) {
            testCaseStepActionExecution.setActionResultMessage(cex.getMessageError());
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
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
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing Action : " + testCaseStepActionExecution.getAction() + " with object : " + value1 + " and property : " + value2);

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        //TODO On JDK 7 implement switch with string [Edit @abourdon: prefer use of chain of responsibility pattern instead of a big switch]
        if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_KEYPRESS)) {
            res = this.doActionKeyPress(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_HIDEKEYBOARD)) {
            res = this.doActionHideKeyboard(tCExecution);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_SWIPE)) {
            res = this.doActionSwipe(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_CLICK)) {
            res = this.doActionClick(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS)) {
            res = this.doActionMouseLeftButtonPress(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE)) {
            res = this.doActionMouseLeftButtonRelease(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_DOUBLECLICK)) {
            res = this.doActionDoubleClick(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_RIGHTCLICK)) {
            res = this.doActionRightClick(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_FOCUSTOIFRAME)) {
            res = this.doActionFocusToIframe(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME)) {
            res = this.doActionFocusDefaultIframe(tCExecution);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_SWITCHTOWINDOW)) {
            res = this.doActionSwitchToWindow(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_MANAGEDIALOG)) {
            res = this.doActionManageDialog(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_MOUSEOVER)) {
            res = this.doActionMouseOver(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_MOUSEOVERANDWAIT)) {
            res = this.doActionMouseOverAndWait(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_OPENURLWITHBASE)) {
            res = this.doActionOpenURL(tCExecution, value1, value2, true);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_OPENURLLOGIN)) {
            testCaseStepActionExecution.setValue1(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountryEnvironmentParameters().getUrlLogin());
            res = this.doActionUrlLogin(tCExecution);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_OPENURL)) {
            res = this.doActionOpenURL(tCExecution, value1, value2, false);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_SELECT)) {
            res = this.doActionSelect(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_TYPE)) {
            res = this.doActionType(tCExecution, value1, value2, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_WAIT)) {
            res = this.doActionWait(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_CALLSOAP)) {
            res = this.doActionMakeSoapCall(testCaseStepActionExecution, value1, value2, false);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_CALLSOAPWITHBASE)) {
            res = this.doActionMakeSoapCall(testCaseStepActionExecution, value1, value2, true);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_REMOVEDIFFERENCE)) {
            res = this.doActionRemoveDifference(testCaseStepActionExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_EXECUTESQLUPDATE)) {
            res = this.doActionExecuteSQLUpdate(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_EXECUTESQLSTOREPROCEDURE)) {
            res = this.doActionExecuteSQLStoredProcedure(tCExecution, value1, value2);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_CALCULATEPROPERTY)) {
            res = this.doActionCalculateProperty(testCaseStepActionExecution, value1, value2, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_DONOTHING)) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_SKIPACTION)) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SKIPACTION);

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_GETPAGESOURCE)) {
            res = this.doActionGetPageSource(testCaseStepActionExecution);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "getPageSource", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action getPageSource triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_TAKESCREENSHOT)) {
            res = this.doActionTakeScreenshot(testCaseStepActionExecution);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "takeScreenshot", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action takeScreenshot triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_CLICKANDWAIT)) { // DEPRECATED ACTION
            res = this.doActionClickWait(tCExecution, value1, value2);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "clickAndWait", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action clickAndWait triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_ENTER)) { // DEPRECATED ACTION
            res = this.doActionKeyPress(tCExecution, value1, "RETURN");
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "enter", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action enter triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals(TestCaseStepAction.ACTION_SELECTANDWAIT)) { // DEPRECATED ACTION
            res = this.doActionSelect(tCExecution, value1, value2);
            this.doActionWait(tCExecution, StringUtil.NULL, StringUtil.NULL);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "selectAndWait", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action selectAndWait triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else {
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
            res.setDescription(res.getDescription().replace("%ACTION%", testCaseStepActionExecution.getAction()));
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Result of the action : " + res.getCodeString() + " " + res.getDescription());

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
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), TestCaseStepAction.ACTION_CLICK, identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")) {
                return androidAppiumService.click(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return iosAppiumService.click(tCExecution.getSession(), identifier);
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "rightClick", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionRightClick(tCExecution.getSession(), identifier);
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionSwitchToWindow(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")) {
                return androidAppiumService.switchToContext(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return iosAppiumService.switchToContext(tCExecution.getSession(), identifier);
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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

    private MessageEvent doActionClickWait(TestCaseExecution tCExecution, String string1, String string2) {
        MessageEvent message;
        try {

            Identifier identifier = identifierService.convertStringToIdentifier(string1);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                message = webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                if (message.getCodeString().equals("OK")) {
                    message = this.doActionWait(tCExecution, string2, null);
                }
                return message;
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", "ClickAndWait"));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", string1));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action ClickAndWait :" + ex);
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
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "doubleClick", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, true, true);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, true, false);
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
             * Check object and property are not null
             */
            if (object == null || property == null) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(object);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "type", identifier.getLocator(), property);
                } else {
                    return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, property, propertyName);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")) {
                return androidAppiumService.type(tCExecution.getSession(), identifier, property, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return iosAppiumService.type(tCExecution.getSession(), identifier, property, propertyName);
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
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "mouseOver", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier);
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    message = sikuliService.doSikuliAction(tCExecution.getSession(), "mouseOver", identifier.getLocator(), "");
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) { // If application are Selenium or appium based, we have a session and can use it to wait.

                /**
                 * if element is integer, set time to that value else Get
                 * Identifier (identifier, locator)
                 */
                if (StringUtil.isNullOrEmpty(element)) {
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isNumeric(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                } else {
                    identifier = identifierService.convertStringToIdentifier(element);
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                }

                if (identifier != null && identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "wait", identifier.getLocator(), "");
                } else if (identifier != null) {
                    return webdriverService.doSeleniumActionWait(tCExecution.getSession(), identifier);
                } else {
                    return this.waitTime(timeToWaitInMs);
                }
            } else { // For any other application we wait for the integer value.
                if (StringUtil.isNullOrEmpty(element)) {
                    // Get default wait from parameter
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isNumeric(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                }
                return this.waitTime(timeToWaitInMs);
            }

        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Wait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionKeyPress(TestCaseExecution tCExecution, String object, String property) {
        try {
            /**
             * Check object and property are not null
             */
            if (object == null && property == null) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(object);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "keyPress", identifier.getLocator(), property);
                } else {
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), identifier, property);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")) {
                return androidAppiumService.keyPress(tCExecution.getSession(), object);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return iosAppiumService.keyPress(tCExecution.getSession(), object);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "KeyPress")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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

    private MessageEvent doActionSelect(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        try {
            /**
             * Check object and property are not null
             */
            if (object == null && property == null) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifierObject = identifierService.convertStringToIdentifier(object);
            Identifier identifierProperty = identifierService.convertStringToSelectIdentifier(property);

            identifierService.checkWebElementIdentifier(identifierObject.getIdentifier());
            identifierService.checkSelectOptionsIdentifier(identifierProperty.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")) {
                return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierObject, identifierProperty);
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
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
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
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
            return webdriverService.doSeleniumActionFocusDefaultIframe(tCExecution.getSession());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "FocusDefaultIframe"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;

    }

    private MessageEvent doActionMakeSoapCall(TestCaseStepActionExecution testCaseStepActionExecution, String object, String property, boolean withBase) {
        MessageEvent message;
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        String decodedEnveloppe;
        String decodedServicePath = null;
        String decodedMethod;
        AnswerItem lastSoapCalled;
        //if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("WS")) {
        try {
            SoapLibrary soapLibrary = soapLibraryService.findSoapLibraryByKey(object);
            String servicePath;
            if (withBase) {
                servicePath = tCExecution.getCountryEnvironmentParameters().getIp() + tCExecution.getCountryEnvironmentParameters().getUrl() + soapLibrary.getServicePath();
            } else {
                servicePath = soapLibrary.getServicePath();
            }
            if (!(StringUtil.isURL(servicePath))) {
                servicePath = "http://" + servicePath;
            }
            /**
             * Decode Envelope, ServicePath and Method replacing properties
             * encapsulated with %
             */
            decodedEnveloppe = soapLibrary.getEnvelope();
            decodedServicePath = servicePath;
            decodedMethod = soapLibrary.getMethod();

            try {
                if (soapLibrary.getEnvelope().contains("%")) {
                    decodedEnveloppe = variableService.decodeVariableWithExistingObject(decodedEnveloppe, testCaseStepActionExecution, false);
                }
                if (soapLibrary.getServicePath().contains("%")) {
                    decodedServicePath = variableService.decodeVariableWithExistingObject(decodedServicePath, testCaseStepActionExecution, false);
                }
                if (soapLibrary.getMethod().contains("%")) {
                    decodedMethod = variableService.decodeVariableWithExistingObject(decodedMethod, testCaseStepActionExecution, false);
                }

                //if the process of decoding originates a message that isStopExecution then we will stop the current action execution
                if (testCaseStepActionExecution.isStopExecution()) {
                    return testCaseStepActionExecution.getActionResultMessage();
                }
            } catch (CerberusEventException cee) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replace("%SOAPNAME%", object));
                message.setDescription(message.getDescription().replace("%SERVICEPATH%", decodedServicePath));
                message.setDescription(message.getDescription().replace("%DESCRIPTION%", cee.getMessageError().getDescription()));
                return message;
            }

            /*
             * Add attachment
             */
            String attachement = "";
            //TODO: the picture url should be used instead of the property value
            //the database does not include the attachmentURL field 
            /*if (!property.isEmpty()) {
             attachement = property; 
             } else {
             attachement = soapLibrary.getAttachmentUrl();
             }*/
            lastSoapCalled = soapService.callSOAP(decodedEnveloppe, decodedServicePath, decodedMethod, attachement);
            tCExecution.setLastSOAPCalled(lastSoapCalled);

        } catch (CerberusException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription().replace("%SOAPNAME%", object));
            message.setDescription(message.getDescription().replace("%SERVICEPATH%", decodedServicePath));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", ex.getMessageError().getDescription()));
            return message;
        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription().replace("%SOAPNAME%", object));
            message.setDescription(message.getDescription().replace("%SERVICEPATH%", decodedServicePath));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", ex.toString()));
            return message;
        }

        //Record the Request and Response in filesystem.
        SOAPExecution se = (SOAPExecution) lastSoapCalled.getItem();
        recorderService.recordSOAPCall(tCExecution, testCaseStepActionExecution, 0, se);

        return lastSoapCalled.getResultMessage();
        //}
    }

    private MessageEvent doActionTakeScreenshot(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent message;
        if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("GUI")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("APK")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("IPA")) {
            recorderService.recordScreenshot(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(),
                    testCaseStepActionExecution, 0);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TAKESCREENSHOT);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "TakeScreenShot"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionGetPageSource(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent message;
        if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("GUI")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("APK")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType().equalsIgnoreCase("IPA")) {
            recorderService.recordPageSource(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, 0);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_GETPAGESOURCE);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", "getPageSource"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType()));
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

    private MessageEvent doActionCalculateProperty(TestCaseStepActionExecution testCaseStepActionExecution, String value1, String value2, String propertyName) {
        MessageEvent message;
        if (StringUtil.isNullOrEmpty(value1)) { // Value1 is a mandatory parameter.
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_MISSINGPROPERTY);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY));
        } else {
            try {
                String propertyValueResult = "";
                // if value2 is not defined, then decode the property defined in value1.
                if (StringUtil.isNullOrEmpty(value2)) {
                    propertyValueResult = variableService.decodeVariableWithExistingObject("%" + value1 + "%", testCaseStepActionExecution, true);
                }
                // If not, then set value1 property to the decoded value2 property
                else {
                    propertyValueResult =  variableService.decodeVariableWithExistingObject("%" + value2 + "%", testCaseStepActionExecution, true);
                    TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                    for (TestCaseExecutionData property : tCExecution.getTestCaseExecutionDataList()) {
                        if (value1.equals(property.getProperty())) {
                            property.setValue(propertyValueResult);
                        }
                    }
                }
                if ((testCaseStepActionExecution.getActionResultMessage().getCodeString().equals("FA"))
                        || (testCaseStepActionExecution.getActionResultMessage().getCodeString().equals("NA"))) {
                    message = testCaseStepActionExecution.getActionResultMessage();
                } else {
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_PROPERTYCALCULATED);
                    message.setDescription(message.getDescription().replace("%PROP%", value1).replace("%VALUE%", propertyValueResult));
                }
            } catch (CerberusEventException cex) {
                message = cex.getMessageError();
            }
        }

//        }
        return message;
    }

    private String getElementToUse(String value1, String value2, String action, TestCaseExecution tCExecution) throws CerberusEventException {
        if (!StringUtil.isNullOrEmpty(value1)) {
            return value1;
        } else if (!StringUtil.isNullOrEmpty(value2)) {
            logEventService.createPrivateCalls("ENGINE", action, MESSAGE_DEPRECATED + " Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + tCExecution.getTest() + "'|'" + tCExecution.getTestCase() + "'] Property : " + value2);
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
            org.apache.log4j.Logger.getLogger(ActionService.class.getName()).log(org.apache.log4j.Level.DEBUG, "TIME TO WAIT = " + timeToWaitMs);
            Thread.sleep(timeToWaitMs);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
            message.setDescription(message.getDescription().replace("%TIME%", String.valueOf(timeToWaitMs)));
            return message;
        } catch (InterruptedException exception) {
            MyLogger.log(ActionService.class.getName(), Level.INFO, exception.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
            message.setDescription(message.getDescription().replace("%TIME%", String.valueOf(timeToWaitMs)));
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
        if ("APK".equals(applicationType)) {
            return androidAppiumService.hideKeyboard(tCExecution.getSession());
        }
        if ("IPA".equals(applicationType)) {
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
        if ("APK".equals(applicationType)) {
            return androidAppiumService.swipe(tCExecution.getSession(), action);
        }
        if ("IPA".equals(applicationType)) {
            return iosAppiumService.swipe(tCExecution.getSession(), action);
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "Swipe screen")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

}
