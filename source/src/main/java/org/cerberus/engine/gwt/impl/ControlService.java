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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.log.MyLogger;
import org.cerberus.engine.gwt.IControlService;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.SoapUtil;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 24/01/2013
 * @since 2.0.0
 */
@Service
public class ControlService implements IControlService {

    private static final Logger LOG = Logger.getLogger(ControlService.class);

    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IRecorderService recorderService;

    @Override
    public TestCaseStepActionControlExecution doControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        MessageEvent res;

        /**
         * Decode the 2 fields property and values before doing the control.
         */
        try {

            // for both control property and control value
            //if the getvalue() indicates that the execution should stop then we stop it before the doControl  or
            //if the property service was unable to decode the property that is specified in the object, 
            //then the execution of this control should not performed
            if (testCaseStepActionControlExecution.getControlProperty().contains("%")) {
                testCaseStepActionControlExecution.setControlProperty(propertyService.decodeValueWithExistingProperties(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false));

                if (!isPropertyGetValueSucceed(testCaseStepActionControlExecution)) {
                    return testCaseStepActionControlExecution;
                }
            }

            if (testCaseStepActionControlExecution.getControlValue().contains("%")) {
                testCaseStepActionControlExecution.setControlValue(propertyService.decodeValueWithExistingProperties(testCaseStepActionControlExecution.getControlValue(), testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false));

                if (!isPropertyGetValueSucceed(testCaseStepActionControlExecution)) {
                    return testCaseStepActionControlExecution;
                }
            }
        } catch (CerberusEventException cex) {
            testCaseStepActionControlExecution.setControlResultMessage(cex.getMessageError());
            testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            return testCaseStepActionControlExecution;
        }

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionControlExecution.setStart(new Date().getTime());

        TestCaseExecution tCExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();

        try {
            //TODO On JDK 7 implement switch with string
            if (testCaseStepActionControlExecution.getControlType().equals("verifyStringEqual")) {
                res = this.verifyStringEqual(testCaseStepActionControlExecution.getControlValue(), testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyStringDifferent")) {
                res = this.verifyStringDifferent(testCaseStepActionControlExecution.getControlValue(), testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyStringGreater")) {
                res = this.verifyStringGreater(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyStringMinor")) {
                res = this.verifyStringMinor(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyIntegerGreater")) {
                res = this.verifyIntegerGreater(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyIntegerMinor")) {
                res = this.verifyIntegerMinor(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyIntegerEquals")) {
                res = this.verifyIntegerEquals(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyIntegerDifferent")) {
                res = this.verifyIntegerDifferent(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementPresent")) {
                //TODO validate properties
                res = this.verifyElementPresent(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementNotPresent")) {
                //TODO validate properties
                res = this.verifyElementNotPresent(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementInElement")) {
                //TODO validate properties
                res = this.verifyElementInElement(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementVisible")) {
                //TODO validate properties
                res = this.verifyElementVisible(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementNotVisible")) {
                //TODO validate properties
                res = this.verifyElementNotVisible(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementEquals")) {
                res = this.verifyElementEquals(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementDifferent")) {
                res = this.verifyElementDifferent(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextInElement")) {
                res = this.verifyTextInElement(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextNotInElement")) {
                res = this.verifyTextNotInElement(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextInDialog")) {
                res = this.verifyTextInDialog(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyRegexInElement")) {
                res = this.VerifyRegexInElement(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextInPage")) {
                res = this.VerifyTextInPage(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextNotInPage")) {
                res = this.VerifyTextNotInPage(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTitle")) {
                res = this.verifyTitle(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementClickable")) {
                res = this.verifyElementClickable(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyElementNotClickable")) {
                res = this.verifyElementNotClickable(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyUrl")) {
                res = this.verifyUrl(tCExecution, testCaseStepActionControlExecution.getControlProperty());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyStringContains")) {
                res = this.verifyStringContains(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyXmlTreeStructure")) {
                res = this.verifyXmlTreeStructure(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

            } else if (testCaseStepActionControlExecution.getControlType().equals("takeScreenshot")) {
                res = this.takeScreenshot(tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution);

            } else if (testCaseStepActionControlExecution.getControlType().equals("skipControl")) {
                res = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_SKIPCONTROL);

            } else {
                res = new MessageEvent(MessageEventEnum.CONTROL_FAILED_UNKNOWNCONTROL);
                res.setDescription(res.getDescription().replaceAll("%CONTROL%", testCaseStepActionControlExecution.getControlType()));
            }

            testCaseStepActionControlExecution.setControlResultMessage(res);
            /**
             * Updating Control result message only if control is not
             * successful. This is to keep the last KO information and
             * preventing KO to be transformed to OK.
             */
            if (!(res.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));
            }

            /**
             * We only stop the test if Control Event message is in stop status
             * AND the control is FATAL. If control is not fatal, we continue
             * the test but refresh the Execution status.
             */
            if (res.isStopTest()) {
                if (testCaseStepActionControlExecution.getFatal().equals("Y")) {
                    testCaseStepActionControlExecution.setStopExecution(true);
                }
            }
        } catch (CerberusEventException exception) {
            testCaseStepActionControlExecution.setControlResultMessage(exception.getMessageError());
        }

        testCaseStepActionControlExecution.setEnd(new Date().getTime());
        return testCaseStepActionControlExecution;
    }

    private MessageEvent verifyStringDifferent(String object, String property) {
        MessageEvent mes;
        if (!object.equalsIgnoreCase(property)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_DIFFERENT);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", object));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", property));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_DIFFERENT);
        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", object));
        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", property));
        return mes;
    }

    private MessageEvent verifyStringEqual(String object, String property) {
        MessageEvent mes;
        if (object.equalsIgnoreCase(property)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_EQUAL);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", object));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", property));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_EQUAL);
        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", object));
        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", property));
        return mes;

    }

    private MessageEvent verifyStringContains(String property, String value) {
        MessageEvent mes;
        if (property.indexOf(value) >= 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CONTAINS);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CONTAINS);
        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
        return mes;

    }

    private MessageEvent verifyStringGreater(String property, String value) {
        MessageEvent mes;
        if (property.compareToIgnoreCase(value) > 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        }
    }

    private MessageEvent verifyStringMinor(String property, String value) {
        MessageEvent mes;
        if (property.compareToIgnoreCase(value) < 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        }
    }

    private MessageEvent verifyIntegerGreater(String property, String value) {
        MessageEvent mes;
        if (StringUtil.isNumeric(property) && StringUtil.isNumeric(value)) {
            int prop = Integer.parseInt(property);
            int val = Integer.parseInt(value);
            if (prop > val) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
                return mes;
            }
        }
        return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PROPERTY_NOTNUMERIC);
    }

    private MessageEvent verifyIntegerMinor(String property, String value) {
        MessageEvent mes;
        if (StringUtil.isNumeric(property) && StringUtil.isNumeric(value)) {
            int prop = Integer.parseInt(property);
            int val = Integer.parseInt(value);
            if (prop < val) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
                return mes;
            }
        }
        return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PROPERTY_NOTNUMERIC);
    }

    private MessageEvent verifyIntegerEquals(String property, String value) {
        if (StringUtil.isNumeric(property) && StringUtil.isNumeric(value)) {
            MessageEvent mes = Integer.parseInt(property) == Integer.parseInt(value) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_EQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_EQUAL);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        }
        return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PROPERTY_NOTNUMERIC);
    }

    private MessageEvent verifyIntegerDifferent(String property, String value) {
        if (StringUtil.isNumeric(property) && StringUtil.isNumeric(value)) {
            MessageEvent mes = Integer.parseInt(property) != Integer.parseInt(value) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_DIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_DIFFERENT);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", property));
            mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
            return mes;
        }
        return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PROPERTY_NOTNUMERIC);
    }

    private MessageEvent verifyElementPresent(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementPresent on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(html);
                    if (identifier.getIdentifier().equals("picture")) {
                        return sikuliService.doSikuliAction(tCExecution.getSession(), "verifyElementPresent", identifier.getLocator(), "");
                    } else if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("WS")) {
                SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
                if (xmlUnitService.isElementPresent(xmlResponse, html)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "VerifyElementPresent"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT_NULL);
        }
    }

    private MessageEvent verifyElementInElement(TestCaseExecution tCExecution, String element, String childElement) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Control : verifyElementInElement on : '" + element + "' is child of '" + childElement + "'");
        }
        MessageEvent mes;
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI") || tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
            if (!StringUtil.isNull(element) && !StringUtil.isNull(childElement)) {
                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(element);
                    Identifier childIdentifier = identifierService.convertStringToIdentifier(childElement);
                    if (this.webdriverService.isElementInElement(tCExecution.getSession(), identifier, childIdentifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTINELEMENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", element).replaceAll("%STRING1%", childElement));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", element).replaceAll("%STRING1%", childElement));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", element).replaceAll("%STRING1%", childElement));
                return mes;
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyElementInElement"));
            mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return mes;
        }
    }

    private MessageEvent verifyElementNotPresent(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementNotPresent on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            try {
                Identifier identifier = identifierService.convertStringToIdentifier(html);
                if (!this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT_NULL);
        }
    }

    private MessageEvent verifyElementVisible(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementVisible on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            try {
                Identifier identifier = identifierService.convertStringToIdentifier(html);
                if (this.webdriverService.isElementVisible(tCExecution.getSession(), identifier)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_VISIBLE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE_NULL);
        }
    }

    private MessageEvent verifyElementNotVisible(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementNotVisible on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            try {
                Identifier identifier = identifierService.convertStringToIdentifier(html);
                if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                    if (this.webdriverService.isElementNotVisible(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTVISIBLE);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        return mes;
                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE_NULL);
        }
    }

    private MessageEvent verifyElementEquals(TestCaseExecution tCExecution, String xpath, String expectedElement) {
        MessageEvent mes = null;

        // If case of not compatible application then exit with error
        if (!tCExecution.getApplication().getType().equalsIgnoreCase("WS")) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyElementEquals"));
            mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return mes;
        }

        // Check if element on the given xpath is equal to the given expected element
        SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
        String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
        mes = xmlUnitService.isElementEquals(xmlResponse, xpath, expectedElement) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTEQUALS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTEQUALS);
        mes.setDescription(mes.getDescription().replaceAll("%XPATH%", xpath));
        mes.setDescription(mes.getDescription().replaceAll("%EXPECTED_ELEMENT%", expectedElement));
        // TODO Give the actual element found into the description.
        return mes;
    }

    private MessageEvent verifyElementDifferent(TestCaseExecution tCExecution, String xpath, String differentElement) {
        MessageEvent mes = null;

        // If case of not compatible application then exit with error
        if (!tCExecution.getApplication().getType().equalsIgnoreCase("WS")) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyElementDifferent"));
            mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
            return mes;
        }

        // Check if element on the given xpath is different from the given different element
        SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
        String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
        mes = xmlUnitService.isElementEquals(xmlResponse, xpath, differentElement) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTDIFFERENT);
        mes.setDescription(mes.getDescription().replaceAll("%XPATH%", xpath));
        mes.setDescription(mes.getDescription().replaceAll("%DIFFERENT_ELEMENT%", differentElement));
        // TODO Give the actual element found into the description.
        return mes;
    }

    private MessageEvent verifyTextInElement(TestCaseExecution tCExecution, String path, String expected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Control: verifyTextInElement on " + path + " element against value: " + expected);
        }

        // Get value from the path element according to the application type
        String actual = null;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getApplication().getType();

            if ("GUI".equalsIgnoreCase(applicationType) || "APK".equalsIgnoreCase(applicationType)) {
                actual = webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);

            } else if ("WS".equalsIgnoreCase(applicationType)) {
                SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
                if (!xmlUnitService.isElementPresent(xmlResponse, path)) {
                    throw new NoSuchElementException("Unable to find element " + path);
                }
                String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                actual = xmlUnitService.getFromXml(xmlResponse, null, newPath);

            } else {
                MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyTextInElement"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }

        } catch (NoSuchElementException exception) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", path));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }

        // In case of null actual value then we alert user
        if (actual == null) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NULL);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", path));
            return mes;
        }

        // Construct the message from the actual response
        MessageEvent mes = actual.equalsIgnoreCase(expected) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINELEMENT) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT);
        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", path));
        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", actual));
        mes.setDescription(mes.getDescription().replaceAll("%STRING3%", expected));
        return mes;
    }

    private MessageEvent verifyTextNotInElement(TestCaseExecution tCExecution, String path, String expected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Control: verifyTextNotInElement on " + path + " element against value: " + expected);
        }

        // Get value from the path element according to the application type
        String actual = null;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getApplication().getType();

            if ("GUI".equalsIgnoreCase(applicationType) || "APK".equalsIgnoreCase(applicationType)) {
                actual = webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);

            } else if ("WS".equalsIgnoreCase(applicationType)) {
                SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
                if (!xmlUnitService.isElementPresent(xmlResponse, path)) {
                    throw new NoSuchElementException("Unable to find element " + path);
                }
                String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                actual = xmlUnitService.getFromXml(xmlResponse, null, newPath);

            } else {
                MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyTextNotInElement"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }

        } catch (NoSuchElementException exception) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", path));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }

        // In case of null actual value then we alert user
        if (actual == null) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT_NULL);
            mes.setDescription(mes.getDescription().replaceAll("%STRING1%", path));
            return mes;
        }

        // Construct the message from the actual response
        MessageEvent mes = actual.equalsIgnoreCase(expected) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTNOTINELEMENT);
        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", path));
        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", actual));
        mes.setDescription(mes.getDescription().replaceAll("%STRING3%", expected));
        return mes;
    }

    private MessageEvent verifyTextInDialog(TestCaseExecution tCExecution, String property, String value) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyTextInAlertPopup against value : " + value);
        MessageEvent mes;
        try {
            String str = this.webdriverService.getAlertText(tCExecution.getSession());
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyTextInAlertPopup has value : " + str);
            if (str != null) {
                String valueToTest = property;
                if (valueToTest == null || "".equals(valueToTest.trim())) {
                    valueToTest = value;
                }

                if (str.trim().equalsIgnoreCase(valueToTest)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINALERT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", str));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING2%", valueToTest));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", str));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING2%", valueToTest));
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT_NULL);
                return mes;
            }
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent VerifyRegexInElement(TestCaseExecution tCExecution, String html, String regex) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyRegexInElement on : " + html + " element against value : " + regex);
        MessageEvent mes;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            String str = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyRegexInElement element : " + html + " has value : " + StringUtil.sanitize(str));
            if (html != null && str != null) {
                try {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_REGEXINELEMENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", StringUtil.sanitize(str)));
                        mes.setDescription(mes.getDescription().replaceAll("%STRING3%", regex));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT);
                        mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                        mes.setDescription(mes.getDescription().replaceAll("%STRING2%", StringUtil.sanitize(str)));
                        mes.setDescription(mes.getDescription().replaceAll("%STRING3%", regex));
                        return mes;
                    }
                } catch (PatternSyntaxException e) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_INVALIDPATERN);
                    mes.setDescription(mes.getDescription().replaceAll("%PATERN%", regex));
                    mes.setDescription(mes.getDescription().replaceAll("%ERROR%", e.getMessage()));
                    return mes;
                }
            } else if (str != null) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NULL);
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
                mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
                return mes;
            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, exception.toString());
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent VerifyTextInPage(TestCaseExecution tCExecution, String regex) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyTextInPage on : " + regex);
        MessageEvent mes;
        String pageSource;
        try {
            pageSource = this.webdriverService.getPageSource(tCExecution.getSession());
            if (LOG.isDebugEnabled()) {
                LOG.debug(pageSource);
            }
            try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pageSource);
                if (matcher.find()) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINPAGE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", Pattern.quote(regex)));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", Pattern.quote(regex)));
                    return mes;
                }
            } catch (PatternSyntaxException e) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE_INVALIDPATERN);
                mes.setDescription(mes.getDescription().replaceAll("%PATERN%", Pattern.quote(regex)));
                mes.setDescription(mes.getDescription().replaceAll("%ERROR%", e.getMessage()));
                return mes;
            }
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent VerifyTextNotInPage(TestCaseExecution tCExecution, String regex) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : VerifyTextNotInPage on : " + regex);
        MessageEvent mes;
        String pageSource;
        try {
            pageSource = this.webdriverService.getPageSource(tCExecution.getSession());
            if (LOG.isDebugEnabled()) {
                LOG.debug(pageSource);
            }
            try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pageSource);
                if (!(matcher.find())) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTNOTINPAGE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", Pattern.quote(regex)));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", Pattern.quote(regex)));
                    return mes;
                }
            } catch (PatternSyntaxException e) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE_INVALIDPATERN);
                mes.setDescription(mes.getDescription().replaceAll("%PATERN%", Pattern.quote(regex)));
                mes.setDescription(mes.getDescription().replaceAll("%ERROR%", e.getMessage()));
                return mes;
            }
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent verifyUrl(TestCaseExecution tCExecution, String page) throws CerberusEventException {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyUrl on : " + page);
        MessageEvent mes;
        try {
            String url = this.webdriverService.getCurrentUrl(tCExecution.getSession(), tCExecution.getUrl());

            if (url.equalsIgnoreCase(page)) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", url));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", page));
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", url));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", page));
                return mes;
            }
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent verifyTitle(TestCaseExecution tCExecution, String title) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyTitle on : " + title);
        MessageEvent mes;
        try {
            String pageTitle = this.webdriverService.getTitle(tCExecution.getSession());
            if (pageTitle.equalsIgnoreCase(title)) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", pageTitle));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", title));
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", pageTitle));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", title));
                return mes;
            }
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    /**
     * @author memiks
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.fatal(exception.toString());
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replaceAll("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

    private MessageEvent verifyXmlTreeStructure(TestCaseExecution tCExecution, String controlProperty, String controlValue) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyXmlTreeStructure on : " + controlProperty);
        MessageEvent mes;
        try {
            SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
            String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
            if (this.xmlUnitService.isSimilarTree(xmlResponse, controlProperty, controlValue)) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_SIMILARTREE);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", StringUtil.sanitize(controlProperty)));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", StringUtil.sanitize(controlValue)));
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SIMILARTREE);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", StringUtil.sanitize(controlProperty)));
                mes.setDescription(mes.getDescription().replaceAll("%STRING2%", StringUtil.sanitize(controlValue)));
                return mes;
            }
        } catch (Exception exception) {
            LOG.fatal(exception.toString());
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED);
            return mes;
        }
    }

    private MessageEvent verifyElementClickable(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementClickable : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
                try {
                    if (this.webdriverService.isElementClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CLICKABLE);
                        mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE);
                        mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "verifyElementClickable"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE_NULL);
        }
    }

    private MessageEvent verifyElementNotClickable(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementNotClickable on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                    || tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
                try {
                    if (this.webdriverService.isElementNotClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCLICKABLE);
                        mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE);
                        mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "VerifyElementNotClickable"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE_NULL);
        }
    }

    private MessageEvent takeScreenshot(TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        MessageEvent message;
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                || tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
            String screenshotPath = recorderService.recordScreenshotAndGetName(tCExecution,
                    testCaseStepActionExecution, testCaseStepActionControlExecution.getControl());
            testCaseStepActionControlExecution.setScreenshotFilename(screenshotPath);
            message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TAKESCREENSHOT);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%CONTROL%", "takeScreenShot"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType()));
        return message;
    }

    /**
     * Updates the test case messages if the control failed to calculate the
     * property values that it needs.
     *
     * @param testCaseStepActionControlExecution
     * @return false if the property value was not retrieved with success, true
     * otherwise
     */
    private boolean isPropertyGetValueSucceed(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        if (testCaseStepActionControlExecution.getTestCaseStepActionExecution().isStopExecution()
                || testCaseStepActionControlExecution.getTestCaseStepActionExecution().getActionResultMessage().getCode()
                == MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION.getCode()) {
            testCaseStepActionControlExecution.setStopExecution(testCaseStepActionControlExecution.getTestCaseStepActionExecution().
                    isStopExecution());
            testCaseStepActionControlExecution.setControlResultMessage(testCaseStepActionControlExecution.getTestCaseStepActionExecution().
                    getActionResultMessage());
            testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(testCaseStepActionControlExecution.
                    getTestCaseStepActionExecution().getActionResultMessage().getMessage()));
            return false;
        }
        return true;
    }

}
