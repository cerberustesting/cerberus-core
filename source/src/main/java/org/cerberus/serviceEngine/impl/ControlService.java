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
package org.cerberus.serviceEngine.impl;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.IControlService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.serviceEngine.IXmlUnitService;
import org.cerberus.util.StringUtil;
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

    @Autowired
    private ISeleniumService seleniumService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IXmlUnitService xmlUnitService;

    @Override
    public TestCaseStepActionControlExecution doControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        /**
         * Decode the 2 fields property and values before doing the control.
         */
        if (testCaseStepActionControlExecution.getControlProperty().contains("%")) {
            String decodedValue = propertyService.decodeValue(testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseExecutionDataList(), testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution());
            testCaseStepActionControlExecution.setControlProperty(decodedValue);
        }
        if (testCaseStepActionControlExecution.getControlValue().contains("%")) {
            String decodedValue = propertyService.decodeValue(testCaseStepActionControlExecution.getControlValue(), testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseExecutionDataList(), testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution());
            testCaseStepActionControlExecution.setControlValue(decodedValue);
        }

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionControlExecution.setStart(new Date().getTime());

        MessageEvent res;
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

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyTextInElement")) {
                res = this.VerifyTextInElement(tCExecution, testCaseStepActionControlExecution.getControlProperty(), testCaseStepActionControlExecution.getControlValue());

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

            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyUrl")) {
                res = this.verifyUrl(tCExecution, testCaseStepActionControlExecution.getControlProperty());
            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyStringContains")) {
                res = this.verifyStringContains(testCaseStepActionControlExecution.getControlProperty(),
                        testCaseStepActionControlExecution.getControlValue());
            } else if (testCaseStepActionControlExecution.getControlType().equals("verifyXmlTreeStructure")) {
                res = this.verifyXmlTreeStructure(tCExecution, testCaseStepActionControlExecution.getControlProperty(),
                        testCaseStepActionControlExecution.getControlValue());
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

    private MessageEvent verifyElementPresent(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementPresent on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                try {
                    if (this.seleniumService.isElementPresent(tCExecution.getSelenium(), html)) {
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
                if (xmlUnitService.isElementPresent(tCExecution, html)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "VerifyElementPresent"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT_NULL);
        }
    }

    private MessageEvent verifyElementInElement(TestCaseExecution tCExecution, String element, String childElement) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementInElement on : '" + element + "' is child of '" + childElement + "'");
        MessageEvent mes;
        if (!StringUtil.isNull(element) && !StringUtil.isNull(childElement)) {
            try {
                if (this.seleniumService.isElementInElement(tCExecution.getSelenium(), element, childElement)) {
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
    }

    private MessageEvent verifyElementNotPresent(TestCaseExecution tCExecution, String html) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyElementNotPresent on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            try {
                if (!this.seleniumService.isElementPresent(tCExecution.getSelenium(), html)) {
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
                if (this.seleniumService.isElementVisible(tCExecution.getSelenium(), html)) {
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
                if (this.seleniumService.isElementPresent(tCExecution.getSelenium(), html)) {
                    if (this.seleniumService.isElementNotVisible(tCExecution.getSelenium(), html)) {
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

    private MessageEvent VerifyTextInElement(TestCaseExecution tCExecution, String html, String value) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : VerifyTextInElement on : " + html + " element against value : " + value);
        MessageEvent mes;
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
        try {
            String str = this.seleniumService.getValueFromHTML(tCExecution.getSelenium(), html);
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : VerifyTextInElement element : " + html + " has value : " + str);
            if (str != null) {
                if (str.equalsIgnoreCase(value)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINELEMENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING2%", str));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING3%", value));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING2%", str));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING3%", value));
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NULL);
                mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                return mes;
            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, exception.toString());
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replaceAll("%ELEMENT%", html));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
        } else if (tCExecution.getApplication().getType().equalsIgnoreCase("WS")) {
        if (xmlUnitService.isTextInElement(tCExecution, html, value)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINELEMENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    return mes;
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT);
                    mes.setDescription(mes.getDescription().replaceAll("%STRING1%", html));
                    mes.setDescription(mes.getDescription().replaceAll("%STRING2%", value));
                    return mes;
                }
        } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replaceAll("%CONTROL%", "VerifyElementPresent"));
                mes.setDescription(mes.getDescription().replaceAll("%APPLICATIONTYPE%", tCExecution.getApplication().getType()));
                return mes;
            }
        
    }

    private MessageEvent verifyTextInDialog(TestCaseExecution tCExecution, String property, String value) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyTextInAlertPopup against value : " + value);
        MessageEvent mes;
        try {
            String str = this.seleniumService.getAlertText(tCExecution.getSelenium());
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
            String str = this.seleniumService.getValueFromHTML(tCExecution.getSelenium(), html);
            MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyRegexInElement element : " + html + " has value : " + StringUtil.sanitize(str));
            if (html != null) {
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
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NULL);
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
            pageSource = this.seleniumService.getPageSource(tCExecution.getSelenium());
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, pageSource);
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
            pageSource = this.seleniumService.getPageSource(tCExecution.getSelenium());
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, pageSource);
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
            String url = this.seleniumService.getCurrentUrl(tCExecution.getSelenium());

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
            String pageTitle = this.seleniumService.getTitle(tCExecution.getSelenium());
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
        MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replaceAll("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

    private MessageEvent verifyXmlTreeStructure(TestCaseExecution tCExecution, String controlProperty, String controlValue) {
        MyLogger.log(ControlService.class.getName(), Level.DEBUG, "Control : verifyXmlTreeStructure on : " + controlProperty);
        MessageEvent mes;
        try {
           
            if (this.xmlUnitService.isSimilarTree(tCExecution, controlProperty, controlValue)) {
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
        MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED);
        return mes;
        }
    }
}
