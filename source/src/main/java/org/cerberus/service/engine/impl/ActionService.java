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
package org.cerberus.service.engine.impl;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ISoapLibraryService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IActionService;
import org.cerberus.service.engine.IAppiumService;
import org.cerberus.service.engine.IIdentifierService;
import org.cerberus.service.engine.IPropertyService;
import org.cerberus.service.engine.IRecorderService;
import org.cerberus.service.engine.ISQLService;
import org.cerberus.service.engine.ISikuliService;
import org.cerberus.service.engine.ISoapService;
import org.cerberus.service.engine.IWebDriverService;
import org.cerberus.service.engine.IXmlUnitService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.MessageEventUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    private IAppiumService appiumService;
    @Autowired
    private ISQLService sqlService;
    @Autowired
    private ILogEventService logEventService;

    private static final Logger LOG = Logger.getLogger(ActionService.class);
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";

    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent res;

        /**
         * Decode the object field before doing the action.
         */
        if (testCaseStepActionExecution.getObject().contains("%")) {
            boolean isCalledFromCalculateProperty = false;
            if (testCaseStepActionExecution.getAction().equals("calculateProperty")) {
                isCalledFromCalculateProperty = true;
            }
            try {
                testCaseStepActionExecution.setObject(propertyService.getValue(testCaseStepActionExecution.getObject(), testCaseStepActionExecution, isCalledFromCalculateProperty));
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

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionExecution.setStart(new Date().getTime());

        String object = testCaseStepActionExecution.getObject();
        String property = testCaseStepActionExecution.getProperty();
        String propertyName = testCaseStepActionExecution.getPropertyName();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing Action : " + testCaseStepActionExecution.getAction() + " with object : " + object + " and property : " + property);

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        //TODO On JDK 7 implement switch with string
        if (testCaseStepActionExecution.getAction().equals("click")) {
            res = this.doActionClick(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("doubleClick")) {
            res = this.doActionDoubleClick(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("keypress")) {
            res = this.doActionKeyPress(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOver")) {
            res = this.doActionMouseOver(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOverAndWait")) {
            res = this.doActionMouseOverAndWait(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlWithBase")) {
            res = this.doActionOpenURL(tCExecution, object, property, true);

        } else if (testCaseStepActionExecution.getAction().equals("openUrl")) {
            res = this.doActionOpenURL(tCExecution, object, property, false);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlLogin")) {
            testCaseStepActionExecution.setObject(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountryEnvironmentParameters().getUrlLogin());
            res = this.doActionUrlLogin(tCExecution);

        } else if (testCaseStepActionExecution.getAction().equals("select")) {
            res = this.doActionSelect(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("focusToIframe")) {
            res = this.doActionFocusToIframe(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("focusDefaultIframe")) {
            res = this.doActionFocusDefaultIframe(tCExecution);

        } else if (testCaseStepActionExecution.getAction().equals("type")) {
            res = this.doActionType(tCExecution, object, property, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals("wait")) {
            res = this.doActionWait(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseDown")) {
            res = this.doActionMouseDown(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseUp")) {
            res = this.doActionMouseUp(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("switchToWindow")) {
            res = this.doActionSwitchToWindow(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("manageDialog")) {
            res = this.doActionManageDialog(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("callSoapWithBase")) {
            res = this.doActionMakeSoapCall(testCaseStepActionExecution, object, property, true);

        } else if (testCaseStepActionExecution.getAction().equals("callSoapWithBase_BETA")) { // ACTION that is plugged to the TestDataLib
            res = this.doActionCallSoapFromDataLib(testCaseStepActionExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("callSoap")) {
            res = this.doActionMakeSoapCall(testCaseStepActionExecution, object, property, false); 

        } else if (testCaseStepActionExecution.getAction().equals("callSoap_BETA")) { // ACTION that is plugged to the TestDataLib
            res = this.doActionCallSoapFromDataLib(testCaseStepActionExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("calculateProperty")) {
            res = this.doActionCalculateProperty(object, property, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals("getPageSource")) {
            res = this.doActionGetPageSource(testCaseStepActionExecution);

        } else if (testCaseStepActionExecution.getAction().equals("removeDifference")) {
            res = this.doActionRemoveDifference(testCaseStepActionExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("executeSqlUpdate")) {
            res = this.doActionExecuteSQLUpdate(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("executeSqlStoredProcedure")) {
            res = this.doActionExecuteSQLStoredProcedure(tCExecution, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("skipAction")) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);

        } else if (testCaseStepActionExecution.getAction().equals("takeScreenshot")) {
            res = this.doActionTakeScreenshot(testCaseStepActionExecution);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "takeScreenshot", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action takeScreenshot triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals("clickAndWait")) { // DEPRECATED ACTION
            res = this.doActionClickWait(tCExecution, object, property);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "clickAndWait", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action clickAndWait triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals("enter")) { // DEPRECATED ACTION
            res = this.doActionKeyPress(tCExecution, object, "RETURN");
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "enter", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action enter triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else if (testCaseStepActionExecution.getAction().equals("selectAndWait")) { // DEPRECATED ACTION
            res = this.doActionSelect(tCExecution, object, property);
            this.doActionWait(tCExecution, StringUtil.NULL, StringUtil.NULL);
            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
            logEventService.createPrivateCalls("ENGINE", "selectAndWait", MESSAGE_DEPRECATED + " Deprecated Action triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "|" + testCaseStepActionExecution.getTestCase() + "']");
            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Action selectAndWait triggered by TestCase : ['" + testCaseStepActionExecution.getTest() + "'|'" + testCaseStepActionExecution.getTestCase() + "']");

        } else {
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
            res.setDescription(res.getDescription().replaceAll("%ACTION%", testCaseStepActionExecution.getAction()));
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Result of the action : " + res.getCodeString() + " " + res.getDescription());
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

    private MessageEvent doActionClick(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "click", tCExecution);
            /**
             * Get Identifier (identifier, locator) and check it's valid
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "click", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                }
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return appiumService.click(tCExecution.getSession(), identifier);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "Click"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Click :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseDown(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseDown", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionMouseDown(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return appiumService.press(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "MouseDown"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "rightClick", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionRightClick(tCExecution.getSession(), identifier);
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "rightClick"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action RightClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseUp(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseUp", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionMouseUp(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "MouseUp"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseUp :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionSwitchToWindow(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionSwitchToWindow(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return appiumService.switchToContext(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "SwitchToWindow"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionManageDialog(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "ManageDialog"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                message = webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                if (message.getCodeString().equals("OK")) {
                    message = this.doActionWait(tCExecution, string2, null);
                }
                return message;
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "doubleClick", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, true);
                }
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, true, false);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "doubleClick"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action DoubleClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionType(TestCaseExecution tCExecution, String object, String property, String propertyName) {
        MessageEvent message;
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "type", identifier.getLocator(), property);
                } else {
                    return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, property, propertyName);
                }
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    ||tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return appiumService.type(tCExecution.getSession(), identifier, property, propertyName);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "Type"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Type :" + ex);
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "mouseOver", identifier.getLocator(), "");
                } else {
                    return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier);
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "mouseOver"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
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
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "mouseOverAndWait"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseOverAndWait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionWait(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        long timeToWait = 0;
        Identifier identifier = null;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, null if both are empty
             */
            element = getElementToUse(object, property, "wait", tCExecution);
            /**
             * if element is integer, set time to that value else Get Identifier
             * (identifier, locator)
             */
            if (element == null) {
                timeToWait = 1000 * tCExecution.getSession().getDefaultWait();
            } else {
                if (StringUtil.isNumeric(element)) {
                    timeToWait = Long.valueOf(element);
                } else {
                    identifier = identifierService.convertStringToIdentifier(element);
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                }
            }

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")){
                if (identifier != null && identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "wait", identifier.getLocator(), "");
                } else if (identifier != null) {
                    return webdriverService.doSeleniumActionWait(tCExecution.getSession(), identifier);
                } else {
                    return this.waitTime(timeToWait);
                }
            }
            if (tCExecution.getApplication().getType().equalsIgnoreCase("CMP")) {
                return this.waitTime(timeToWait);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "Wait"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Wait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionKeyPress(TestCaseExecution tCExecution, String object, String property) {
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
            Identifier identifier = identifierService.convertStringToIdentifier(object);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                if (identifier.getIdentifier().equals("picture")) {
                    return sikuliService.doSikuliAction(tCExecution.getSession(), "keyPress", identifier.getLocator(), property);
                } else {
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), identifier, property);
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "KeyPress"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionOpenURL(tCExecution.getSession(), tCExecution.getUrl(), identifier, withBase);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "OpenURL[WithBase]"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("APK")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")) {
                return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierObject, identifierProperty);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "Select"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Select :" + ex);
            return ex.getMessageError();
        }

    }

    private MessageEvent doActionUrlLogin(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            return webdriverService.doSeleniumActionUrlLogin(tCExecution.getSession(), tCExecution.getUrl(), tCExecution.getCountryEnvironmentParameters().getUrlLogin());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "UrlLogin"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
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

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionFocusToIframe(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "FocusToIframe"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action FocusToIframe :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionFocusDefaultIframe(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            return webdriverService.doSeleniumActionFocusDefaultIframe(tCExecution.getSession());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "FocusDefaultIframe"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
        return message;

    }

    private MessageEvent doActionMakeSoapCall(TestCaseStepActionExecution testCaseStepActionExecution, String object, String property, boolean withBase) {
        MessageEvent message;
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        //if (tCExecution.getApplication().getType().equalsIgnoreCase("WS")) {
        try {
            SoapLibrary soapLibrary = soapLibraryService.findSoapLibraryByKey(object);
            String servicePath;
            if (withBase) {
                servicePath = tCExecution.getCountryEnvironmentParameters().getIp() + tCExecution.getCountryEnvironmentParameters().getUrl();
            } else {
                servicePath = soapLibrary.getServicePath();
            }
            /**
             * Decode Envelope replacing properties encapsulated with %
             */
            String decodedEnveloppe = soapLibrary.getEnvelope();
            if (soapLibrary.getEnvelope().contains("%")) {
                try {
                    decodedEnveloppe = propertyService.getValue(soapLibrary.getEnvelope(), testCaseStepActionExecution, false);
                    //if the process of decoding originates a message that isStopExecution then we will stop the current action execution
                    if (testCaseStepActionExecution.isStopExecution()) {
                        return testCaseStepActionExecution.getActionResultMessage();
                    }
                } catch (CerberusEventException cee) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                    message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", object));
                    message.setDescription(message.getDescription().replaceAll("%DESCRIPTION%", cee.getMessageError().getDescription()));
                    return message;
                }
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
            return soapService.callSOAPAndStoreResponseInMemory(tCExecution.getExecutionUUID(), decodedEnveloppe, servicePath, soapLibrary.getMethod(), attachement, false);
        } catch (CerberusException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", object));
            message.setDescription(message.getDescription().replaceAll("%DESCRIPTION%", ex.getMessageError().getDescription()));
            return message;
        }
        //}
    }

    private MessageEvent doActionMouseDownMouseUp(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "mouseDownMouseUp", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                return webdriverService.doSeleniumActionMouseDownMouseUp(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", "mouseDownMouseUp"));
            message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseDownMouseUp :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionTakeScreenshot(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent message;
        if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("GUI")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("APK")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("IPA")) {
            String screenshotPath = recorderService.recordScreenshotAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(),
                    testCaseStepActionExecution, 0);
            testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TAKESCREENSHOT);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "TakeScreenShot"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType()));
        return message;
    }

    private MessageEvent doActionGetPageSource(TestCaseStepActionExecution testCaseStepActionExecution) {
        MessageEvent message;
        if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("GUI")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("APK")
                || testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equalsIgnoreCase("IPA")) {
            String screenshotPath = recorderService.recordPageSourceAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(),
                    testCaseStepActionExecution, 0);
            testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_GETPAGESOURCE);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "getPageSource"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType()));
        return message;
    }

    private MessageEvent doActionRemoveDifference(TestCaseStepActionExecution testCaseStepActionExecution, String object, String property) {
        // Filters differences from the given object pattern
        String filteredDifferences = xmlUnitService.removeDifference(object, property);

        // If filtered differences are null then service has returned with errors
        if (filteredDifferences == null) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_REMOVEDIFFERENCE);
            message.setDescription(message.getDescription().replaceAll("%DIFFERENCE%", object));
            message.setDescription(message.getDescription().replaceAll("%DIFFERENCES%", property));
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
        message.setDescription(message.getDescription().replaceAll("%DIFFERENCE%", object));
        message.setDescription(message.getDescription().replaceAll("%DIFFERENCES%", property));
        return message;
    }

    private MessageEvent doActionCalculateProperty(String object, String property, String propertyName) {
        MessageEvent message;
        //if the object and the property are not defined for the calculatePropery action then an exception should be raised and 
        //the execution stopped
//        if(StringUtil.isNullOrEmpty(object) && StringUtil.isNullOrEmpty(property)){
//            message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_CALCULATE_OBJECTPROPERTYNULL);                        
//         }else{         
        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_PROPERTYCALCULATED);
        message.setDescription(message.getDescription().replaceAll("%PROP%", propertyName));
//        }
        return message;
    }

    private String getElementToUse(String object, String property, String action, TestCaseExecution tCExecution) throws CerberusEventException {
        if (!StringUtil.isNullOrEmpty(object)) {
            return object;
        } else if (!StringUtil.isNullOrEmpty(property)) {
            logEventService.createPrivateCalls("ENGINE", action, MESSAGE_DEPRECATED + " Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + tCExecution.getTest() + "'|'" + tCExecution.getTestCase() + "'] Property : " + property);
            LOG.warn(MESSAGE_DEPRECATED + " Action : "+ action + ". Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + tCExecution.getTest() + "'|'" + tCExecution.getTestCase() + "'] Property : " + property);
            return property;
        }
        if (!(action.equals("wait"))) { // Wait is the only action can be excuted with no parameters. For all other actions we raize an exception as this should never happen.
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_PERFORM_ACTION);
            message.setDescription(message.getDescription().replaceAll("%ACTION%", action));
            throw new CerberusEventException(message);
        }
        return null;
    }

    private MessageEvent waitTime(Long timeToWait) {
        MessageEvent message;
        /**
         * if timeToWait is null, throw CerberusException
         */
        if (timeToWait == 0) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_INVALID_FORMAT);
            return message;
        }
        try {
            org.apache.log4j.Logger.getLogger(ActionService.class.getName()).log(org.apache.log4j.Level.DEBUG, "TIME TO WAIT = " + timeToWait);
            Thread.sleep(timeToWait);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
            message.setDescription(message.getDescription().replaceAll("%TIME%", String.valueOf(timeToWait)));
            return message;
        } catch (InterruptedException exception) {
            MyLogger.log(ActionService.class.getName(), Level.INFO, exception.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
            return message;
        }
    }

    private MessageEvent doActionExecuteSQLUpdate(TestCaseExecution tCExecution, String object, String property) {
        if (object.equals("cerberus" + System.getProperty("org.cerberus.environment"))) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_AGAINST_CERBERUS);
        } else {
            return sqlService.executeUpdate(tCExecution.getApplication().getSystem(),
                    tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
        }
    }

    private MessageEvent doActionExecuteSQLStoredProcedure(TestCaseExecution tCExecution, String object, String property) {
        if (object.equals("cerberus" + System.getProperty("org.cerberus.environment"))) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_AGAINST_CERBERUS);
        } else {
            return sqlService.executeCallableStatement(tCExecution.getApplication().getSystem(),
                    tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
        }
    }

    private MessageEvent doActionCallSoapFromDataLib(TestCaseStepActionExecution testCaseStepActionExecution,
            String object, String property) {
        MessageEvent message;
        Answer ansCallSoap = propertyService.callSoapProperty(testCaseStepActionExecution, object);

        if (ansCallSoap.isCodeEquals(MessageEventEnum.PROPERTY_SUCCESS.getCode())) {
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("type", "SOAP");
            options.put("description", MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getDescription().
                    replaceAll("%SOAPNAME%", object));

            if(StringUtil.isNullOrEmpty(property)){
                options.put("request", "%REQUEST_NAME%");
                options.put("response", "%RESPONSE_NAME%");
            }else{
                String descId = String.valueOf(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
                options.put("request", descId + File.separator + property + "_request.xml");
                options.put("response", descId + File.separator + property + ".xml");
            }
            
            message = MessageEventUtil.createMessageDescriptionJSONFormat(MessageEventEnum.ACTION_SUCCESS_CALLSOAP, options);
        } else {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", object));
            message.setDescription(message.getDescription().replaceAll("%DESCRIPTION%",
                    ansCallSoap.getResultMessage().getDescription()));
        }

        return message;
    }
}
