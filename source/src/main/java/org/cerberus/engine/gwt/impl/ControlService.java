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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IControlService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.service.json.IJsonService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;
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

    private static final Logger LOG = LogManager.getLogger(ControlService.class);

    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private IJsonService jsonService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IVariableService variableService;

    @Override
    public TestCaseStepActionControlExecution doControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        MessageEvent res;
        TestCaseExecution tCExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        /**
         * Decode the step action control description
         */
        try {
            // When starting a new control, we reset the property list that was already calculated.
            tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getDescription(),
                    tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false);
            testCaseStepActionControlExecution.setDescription((String) answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the control result.
                testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Description"));
                testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                testCaseStepActionControlExecution.setEnd(new Date().getTime());
                LOG.debug("Control interupted due to decode 'Description' Error.");
                return testCaseStepActionControlExecution;
            }
        } catch (CerberusEventException cex) {
            testCaseStepActionControlExecution.setControlResultMessage(cex.getMessageError());
            testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            return testCaseStepActionControlExecution;
        }

        /**
         * Decode the 2 fields property and values before doing the control.
         */
        try {

            // for both control property and control value
            //if the getvalue() indicates that the execution should stop then we stop it before the doControl  or
            //if the property service was unable to decode the property that is specified in the object,
            //then the execution of this control should not performed
            if (testCaseStepActionControlExecution.getValue1() == null) {
                testCaseStepActionControlExecution.setValue1("");
            }
            if (testCaseStepActionControlExecution.getValue1().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getValue1(), tCExecution,
                        testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false);
                testCaseStepActionControlExecution.setValue1((String) answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value1"));
                    testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    testCaseStepActionControlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interupted due to decode 'Control Value1' Error.");
                    return testCaseStepActionControlExecution;
                }

            }

            if (testCaseStepActionControlExecution.getValue2() == null) {
                testCaseStepActionControlExecution.setValue2("");
            }
            if (testCaseStepActionControlExecution.getValue2().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getValue2(),
                        tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false);
                testCaseStepActionControlExecution.setValue2((String) answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value2"));
                    testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    testCaseStepActionControlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interupted due to decode 'Control Value2' Error.");
                    return testCaseStepActionControlExecution;
                }

            }

            if (testCaseStepActionControlExecution.getValue3() == null) {
                testCaseStepActionControlExecution.setValue3("");
            }
            if (testCaseStepActionControlExecution.getValue3().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getValue3(),
                        tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), false);
                testCaseStepActionControlExecution.setValue3((String) answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value3"));
                    testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    testCaseStepActionControlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interupted due to decode 'Control Value3' Error.");
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

        // When starting a new control, we reset the property list that was already calculated.
        tCExecution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

        try {

            switch (testCaseStepActionControlExecution.getControl()) {

                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGEQUAL:
                    res = this.verifyStringEqual(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGDIFFERENT:
                    res = this.verifyStringDifferent(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGGREATER:
                    res = this.verifyStringGreater(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGMINOR:
                    res = this.verifyStringMinor(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGCONTAINS:
                    res = this.verifyStringContains(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGNOTCONTAINS:
                    res = this.verifyStringNotContains(testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL:
                    res = this.evaluateControl_ifNumericXXX(testCaseStepActionControlExecution.getControl(), testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT:
                    //TODO validate properties
                    res = this.verifyElementPresent(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT:
                    //TODO validate properties
                    res = this.verifyElementNotPresent(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE:
                    //TODO validate properties
                    res = this.verifyElementVisible(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE:
                    //TODO validate properties
                    res = this.verifyElementNotVisible(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS:
                    res = this.verifyElementEquals(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT:
                    res = this.verifyElementDifferent(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTINELEMENT:
                    //TODO validate properties
                    res = this.verifyElementInElement(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE:
                    res = this.verifyElementClickable(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE:
                    res = this.verifyElementNotClickable(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL, tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX:
                    res = this.VerifyElementTextMatchRegex(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTINPAGE:
                    res = this.VerifyTextInPage(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTNOTINPAGE:
                    res = this.VerifyTextNotInPage(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLE:
                    res = this.verifyTitle(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYURL:
                    res = this.verifyUrl(tCExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTINDIALOG:
                    res = this.verifyTextInDialog(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYXMLTREESTRUCTURE:
                    res = this.verifyXmlTreeStructure(tCExecution, testCaseStepActionControlExecution.getValue1(), testCaseStepActionControlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_TAKESCREENSHOT:
                    res = this.takeScreenshot(tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution, testCaseStepActionControlExecution.getValue1());
                    break;
                case TestCaseStepActionControl.CONTROL_GETPAGESOURCE:
                    res = this.getPageSource(tCExecution, testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution);
                    break;

                default:
                    res = new MessageEvent(MessageEventEnum.CONTROL_FAILED_UNKNOWNCONTROL);
                    res.setDescription(res.getDescription().replace("%CONTROL%", testCaseStepActionControlExecution.getControl()));
            }
        } catch (final CerberusEventException exception) {
            res = exception.getMessageError();

        } catch (final Exception unexpected) {
            LOG.debug("Unexpected exception on control!", unexpected);
            res = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC).resolveDescription("ERROR", unexpected.getMessage());
        }

        testCaseStepActionControlExecution.setControlResultMessage(res);
        /**
         * Updating Control result message only if control is not successful.
         * This is to keep the last KO information and preventing KO to be
         * transformed to OK.
         */
        if (!(res.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
            testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));
        }

        /**
         * We only stop the test if Control Event message is in stop status AND
         * the control is FATAL. If control is not fatal, we continue the test
         * but refresh the Execution status.
         */
        if (res.isStopTest()) {
            if (testCaseStepActionControlExecution.getFatal().equals("Y")) {
                testCaseStepActionControlExecution.setStopExecution(true);
            }
        }

        testCaseStepActionControlExecution.setEnd(new Date().getTime());
        return testCaseStepActionControlExecution;
    }

    private MessageEvent verifyStringDifferent(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !value1.equals(value2) : !value1.equalsIgnoreCase(value2)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_STRINGDIFFERENT);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_DIFFERENT);
        mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
        mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
        mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
        return mes;
    }

    private MessageEvent verifyStringEqual(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.equals(value2) : value1.equalsIgnoreCase(value2)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_STRINGEQUAL);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_EQUAL);
        mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
        mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
        mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
        return mes;

    }

    private MessageEvent verifyStringContains(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.indexOf(value2) >= 0 : value1.toLowerCase().indexOf(value2.toLowerCase()) >= 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CONTAINS);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CONTAINS);
        mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
        mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
        mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
        return mes;

    }

    private MessageEvent verifyStringNotContains(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.indexOf(value2) >= 0 : value1.toLowerCase().indexOf(value2.toLowerCase()) >= 0) {

            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCONTAINS);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));

            return mes;
        }
        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCONTAINS);
        mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
        mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
        mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
        return mes;

    }

    private MessageEvent verifyStringGreater(String value1, String value2) {
        MessageEvent mes;
        if (value1.compareToIgnoreCase(value2) > 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            return mes;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            return mes;
        }
    }

    private MessageEvent verifyStringMinor(String value1, String value2) {
        MessageEvent mes;
        if (value1.compareToIgnoreCase(value2) < 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            return mes;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
            mes.setDescription(mes.getDescription().replace("%STRING1%", value1));
            mes.setDescription(mes.getDescription().replace("%STRING2%", value2));
            return mes;
        }
    }

    private MessageEvent evaluateControl_ifNumericXXX(String control, String controlValue1, String controlValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Numeric Equals");
        }
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_PENDING);

        // We first prepare the string for nueric conversion to replace , by .
        String newControlValue1 = StringUtil.prepareToNumeric(controlValue1);
        String newControlValue2 = StringUtil.prepareToNumeric(controlValue2);

        // We try to convert the strings value1 to numeric.
        Double value1 = 0.0;
        try {
            value1 = Double.valueOf(newControlValue1);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", control)
                    .replace("%STRINGVALUE%", newControlValue1));
            return mes;
        }

        // We try to convert the strings value2 to numeric.
        Double value2 = 0.0;
        try {
            value2 = Double.valueOf(newControlValue2);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", control)
                    .replace("%STRINGVALUE%", newControlValue2));
            return mes;
        }

        // Now that both values are converted to double we ceck the operator here.
        boolean execute_Action = true;
        switch (control) {

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS:
                if (Objects.equals(value1, value2)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_EQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_EQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT:
                if (!(Objects.equals(value1, value2))) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_DIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_DIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER:
                if (value1 > value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL:
                if (value1 >= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR:
                if (value1 < value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL:
                if (value1 <= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", control)
                            .replace("%STRING1%", value1.toString()).replace("%STRING2%", value2.toString())
                    );
                }
                break;

        }

        return mes;
    }

    private MessageEvent verifyElementPresent(TestCaseExecution tCExecution, String elementPath) {
        LOG.debug("Control : verifyElementPresent on : " + elementPath);
        MessageEvent mes;

        if (!StringUtil.isNull(elementPath)) {
            Identifier identifier = identifierService.convertStringToIdentifier(elementPath);

            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    if (identifier.getIdentifier().equals("picture")) {
                        return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator());
                    } else if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {

                if (tCExecution.getLastServiceCalled() != null) {
                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();

                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (xmlUnitService.isElementPresent(responseBody, elementPath)) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                return mes;
                            } else {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                return mes;
                            }
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                            try {
                                if (jsonService.getFromJson(responseBody, null, elementPath) != null) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                                    mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                    return mes;
                                } else {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                                    mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                    return mes;
                                }
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                                return mes;
                            }
                        }
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementPresent"));
                            return mes;
                    }

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }
            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {

                return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator());

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "VerifyElementPresent"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT_NULL);
        }
    }

    private MessageEvent verifyElementNotPresent(TestCaseExecution tCExecution, String elementPath) {
        LOG.debug("Control : verifyElementNotPresent on : " + elementPath);
        MessageEvent mes;
        if (!StringUtil.isNull(elementPath)) {
            Identifier identifier = identifierService.convertStringToIdentifier(elementPath);

            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    if (identifier.getIdentifier().equals("picture")) {
                        return sikuliService.doSikuliVerifyElementNotPresent(tCExecution.getSession(), identifier.getLocator());
                    } else if (!this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {
                return sikuliService.doSikuliVerifyElementNotPresent(tCExecution.getSession(), identifier.getLocator());

            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {

                if (tCExecution.getLastServiceCalled() != null) {

                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (!(xmlUnitService.isElementPresent(responseBody, elementPath))) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                return mes;
                            } else {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                return mes;
                            }
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                            try {
                                if (jsonService.getFromJson(responseBody, null, elementPath) == null) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                    return mes;
                                } else {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%STRING1%", elementPath));
                                    return mes;
                                }
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                                return mes;
                            }
                        }
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementNotPresent"));
                            return mes;
                    }

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementNotPresent"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT_NULL);
        }
    }

    private MessageEvent verifyElementInElement(TestCaseExecution tCExecution, String element, String childElement) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Control : verifyElementInElement on : '" + element + "' is child of '" + childElement + "'");
        }
        MessageEvent mes;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {

            if (!StringUtil.isNull(element) && !StringUtil.isNull(childElement)) {
                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(element);
                    Identifier childIdentifier = identifierService.convertStringToIdentifier(childElement);
                    if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        if (this.webdriverService.isElementInElement(tCExecution.getSession(), identifier, childIdentifier)) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTINELEMENT);
                            mes.setDescription(mes.getDescription().replace("%STRING2%", element).replace("%STRING1%", childElement));
                            return mes;
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                            mes.setDescription(mes.getDescription().replace("%STRING2%", element).replace("%STRING1%", childElement));
                            return mes;
                        }
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NO_SUCH_ELEMENT);
                        mes.setDescription(mes.getDescription().replace("%SELEX%", new NoSuchElementException("").toString()).replace("%ELEMENT%", element));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                mes.setDescription(mes.getDescription().replace("%STRING2%", element).replace("%STRING1%", childElement));
                return mes;
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementInElement"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;
        }
    }

    private MessageEvent verifyElementVisible(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control : verifyElementVisible on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(html);
                    if (this.webdriverService.isElementVisible(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_VISIBLE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else {

                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementVisible"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;

            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE_NULL);
        }
    }

    private MessageEvent verifyElementNotVisible(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control : verifyElementNotVisible on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(html);
                    if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        if (this.webdriverService.isElementNotVisible(tCExecution.getSession(), identifier)) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTVISIBLE);
                            mes.setDescription(mes.getDescription().replace("%STRING1%", html));
                            return mes;
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE);
                            mes.setDescription(mes.getDescription().replace("%STRING1%", html));
                            return mes;
                        }
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementNotVisible"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;

            }

        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE_NULL);
        }
    }

    private MessageEvent verifyElementEquals(TestCaseExecution tCExecution, String xpath, String expectedElement) {
        LOG.debug("Control : verifyElementEquals on : " + xpath + " expected Element : " + expectedElement);
        MessageEvent mes = null;

        // If case of not compatible application then exit with error
        if (!tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementEquals"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;
        }

        // Check if element on the given xpath is equal to the given expected element
        if (tCExecution.getLastServiceCalled() != null) {

            String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
            switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                    mes = xmlUnitService.isElementEquals(xmlResponse, xpath, expectedElement) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTEQUALS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTEQUALS);
                    mes.setDescription(mes.getDescription().replace("%XPATH%", xpath));
                    mes.setDescription(mes.getDescription().replace("%EXPECTED_ELEMENT%", expectedElement));
                    return mes;
                default:
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                    mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                    mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementEquals"));
                    return mes;
            }

            // TODO Give the actual element found into the description.
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
            return mes;
        }

    }

    private MessageEvent verifyElementDifferent(TestCaseExecution tCExecution, String xpath, String differentElement) {
        LOG.debug("Control : verifyElementDifferent on : " + xpath + " expected Element : " + differentElement);
        MessageEvent mes = null;

        // If case of not compatible application then exit with error
        if (!tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementDifferent"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;
        }

        // Check if element on the given xpath is different from the given different element
//        SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
        if (tCExecution.getLastServiceCalled() != null) {

            switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                    String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                    mes = xmlUnitService.isElementEquals(xmlResponse, xpath, differentElement) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTDIFFERENT);
                    mes.setDescription(mes.getDescription().replace("%XPATH%", xpath));
                    mes.setDescription(mes.getDescription().replace("%DIFFERENT_ELEMENT%", differentElement));
                    return mes;
                default:
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                    mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                    mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementDifferent"));
                    return mes;
            }

            // TODO Give the actual element found into the description.
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
            return mes;
        }

    }

    @Override
    public MessageEvent verifyElementXXX(String control, TestCaseExecution tCExecution, String path, String expected, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Control: verifyElementXXX (" + control + ") on " + path + " element against value: " + expected + " AppType : " + tCExecution.getAppTypeEngine());
        }

        MessageEvent mes;
        // Get value from the path element according to the application type
        String actual = null;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getAppTypeEngine();

            switch (applicationType) {

                case Application.TYPE_GUI:
                case Application.TYPE_APK:
                case Application.TYPE_IPA:

                    actual = webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);
                    // In case of null actual value then we alert user
                    if (actual == null) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NULL);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", path));
                        return mes;
                    }

                    // Get the result depending on the control required.
                    mes = switchControl(control, path, actual, expected, isCaseSensitive);
                    return mes;

                case Application.TYPE_SRV:
                    if (tCExecution.getLastServiceCalled() != null) {
                        String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                        switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                                if (!xmlUnitService.isElementPresent(responseBody, path)) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
                                    return mes;
                                }
                                String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                                actual = xmlUnitService.getFromXml(responseBody, newPath);
                                // In case of null actual value then we alert user
                                if (actual == null) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NULL);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
                                    return mes;
                                }
                                // Get the result depending on the control required.
                                mes = switchControl(control, path, actual, expected, isCaseSensitive);
                                return mes;

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                                try {
                                    actual = jsonService.getFromJson(responseBody, null, path);
                                } catch (Exception ex) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                    mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                                    return mes;
                                }
                            }
                            // In case of null actual value then we alert user
                            if (actual == null) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
                                return mes;
                            }
                            // Get the result depending on the control required.
                            mes = switchControl(control, path, actual, expected, isCaseSensitive);
                            return mes;

                            default:
                                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                                mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                                mes.setDescription(mes.getDescription().replace("%CONTROL%", control));
                                return mes;
                        }

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                        return mes;
                    }

                default:
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                    mes.setDescription(mes.getDescription().replace("%CONTROL%", control));
                    mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                    return mes;

            }

        } catch (NoSuchElementException exception) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }

    }

    private MessageEvent switchControl(String control, String path, String actual, String expected, String isCaseSensitive) {
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        mes.setDescription(mes.getDescription().replace("%CONTROL%", "switchControl-" + control));

        switch (control) {
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL:
                mes = verifyElementTextEqualCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT:
                mes = verifyElementTextDifferentCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                Double value1 = 0.0;
                try {
                    value1 = Double.valueOf(actual);
                } catch (NumberFormatException nfe) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
                    mes.setDescription(mes.getDescription().replace("%COND%", control).replace("%STRINGVALUE%", actual));
                    return mes;
                }

                // We try to convert the strings value2 to numeric.
                Double value2 = 0.0;
                try {
                    value2 = Double.valueOf(expected);
                } catch (NumberFormatException nfe) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
                    mes.setDescription(mes.getDescription().replace("%COND%", control).replace("%STRINGVALUE%", expected));
                    return mes;
                }
                mes = checkNumericVerifyElement(control, value1, value2);
                break;
        }
        mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
        mes.setDescription(mes.getDescription().replace("%ELEMENTVALUE%", actual));
        mes.setDescription(mes.getDescription().replace("%VALUE%", expected));
        mes.setDescription(mes.getDescription().replace("%CASESENSITIVE%", caseSensitiveMessageValue(isCaseSensitive)));
        return mes;
    }

    private MessageEvent verifyElementTextEqualCaseSensitiveCheck(String actual, String expected, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            mes = actual.equals(expected) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTEQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTEQUAL);
        } else {
            mes = actual.equalsIgnoreCase(expected) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTEQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTEQUAL);
        }
        return mes;
    }

    private MessageEvent verifyElementTextDifferentCaseSensitiveCheck(String actual, String expected, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            mes = actual.equals(expected) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTDIFFERENT);
        } else {
            mes = actual.equalsIgnoreCase(expected) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTDIFFERENT);
        }
        return mes;
    }

    private MessageEvent checkNumericVerifyElement(String control, Double actual, Double expected) {
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        mes.setDescription(mes.getDescription().replace("%CONTROL%", "checkNumericVerifyElement-" + control));

        switch (control) {
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
                mes = actual.equals(expected) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICEQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICEQUAL);
                return mes;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
                mes = !actual.equals(expected) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICDIFFERENT);
                return mes;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
                mes = actual > expected ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICGREATER) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICGREATER);
                return mes;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
                mes = actual >= expected ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICGREATEROREQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICGREATEROREQUAL);
                return mes;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
                mes = actual < expected ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICMINOR) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICMINOR);
                return mes;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                mes = actual <= expected ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICMINOROREQUAL) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICMINOROREQUAL);
                return mes;
        }
        return mes;
    }

    private MessageEvent VerifyElementTextMatchRegex(TestCaseExecution tCExecution, String path, String regex) {
        LOG.debug("Control : VerifyElementTextMatchRegex on : " + path + " element against value : " + regex);
        MessageEvent mes;
        String pathContent = null;
        try {

            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getAppTypeEngine();
            // Get value from the path element according to the application type
            if (Application.TYPE_GUI.equalsIgnoreCase(applicationType)
                    || Application.TYPE_APK.equalsIgnoreCase(applicationType)
                    || Application.TYPE_IPA.equalsIgnoreCase(applicationType)) {

                pathContent = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);
            } else if (Application.TYPE_SRV.equalsIgnoreCase(applicationType)) {
                if (tCExecution.getLastServiceCalled() != null) {
                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (!xmlUnitService.isElementPresent(responseBody, path)) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
                                return mes;
                            }
                            String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                            pathContent = xmlUnitService.getFromXml(responseBody, newPath);
                            break;

                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                            try {
                                pathContent = jsonService.getFromJson(responseBody, null, path);
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                                return mes;
                            }
                            break;

                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONTROL%", "VerifyElementTextMatchRegex"));
                            return mes;
                    }

                    // TODO Give the actual element found into the description.
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "VerifyElementTextMatchRegex"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;
            }
            LOG.debug("Control : VerifyElementMatchRegex element : " + path + " has value : " + StringUtil.sanitize(pathContent));
            if (path != null && pathContent != null) {
                try {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(pathContent);
                    if (matcher.find()) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_REGEXINELEMENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", path));
                        mes.setDescription(mes.getDescription().replace("%STRING2%", StringUtil.sanitize(pathContent)));
                        mes.setDescription(mes.getDescription().replace("%STRING3%", regex));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", path));
                        mes.setDescription(mes.getDescription().replace("%STRING2%", StringUtil.sanitize(pathContent)));
                        mes.setDescription(mes.getDescription().replace("%STRING3%", regex));
                        return mes;
                    }
                } catch (PatternSyntaxException e) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_INVALIDPATERN);
                    mes.setDescription(mes.getDescription().replace("%PATERN%", regex));
                    mes.setDescription(mes.getDescription().replace("%ERROR%", e.getMessage()));
                    return mes;
                }
            } else if (pathContent != null) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NULL);
                return mes;
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
                mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
                return mes;
            }
        } catch (NoSuchElementException exception) {
            LOG.debug(exception.toString());
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", path));
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
    }

    private MessageEvent verifyTextInDialog(TestCaseExecution tCExecution, String property, String value) {
        LOG.debug("Control : verifyTextInDialog against value : " + value);
        MessageEvent mes;

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            try {
                String str = this.webdriverService.getAlertText(tCExecution.getSession());
                LOG.debug("Control : verifyTextInAlertPopup has value : " + str);
                if (str != null) {
                    String valueToTest = property;
                    if (valueToTest == null || "".equals(valueToTest.trim())) {
                        valueToTest = value;
                    }

                    if (str.trim().equalsIgnoreCase(valueToTest)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINALERT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", str));
                        mes.setDescription(mes.getDescription().replace("%STRING2%", valueToTest));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", str));
                        mes.setDescription(mes.getDescription().replace("%STRING2%", valueToTest));
                        return mes;
                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT_NULL);
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }

        } else {

            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyTextInDialog"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }

    }

    private MessageEvent VerifyTextInPage(TestCaseExecution tCExecution, String regex) {
        LOG.debug("Control : verifyTextInPage on : " + regex);
        MessageEvent mes;

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_APK.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_IPA.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

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
                        mes.setDescription(mes.getDescription().replace("%STRING1%", Pattern.quote(regex)));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", Pattern.quote(regex)));
                        return mes;
                    }
                } catch (PatternSyntaxException e) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE_INVALIDPATERN);
                    mes.setDescription(mes.getDescription().replace("%PATERN%", Pattern.quote(regex)));
                    mes.setDescription(mes.getDescription().replace("%ERROR%", e.getMessage()));
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }

        } else if (Application.TYPE_FAT.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {
            return sikuliService.doSikuliVerifyTextInPage(tCExecution.getSession(), regex);
        } else {

            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyTextInPage"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }
    }

    private MessageEvent VerifyTextNotInPage(TestCaseExecution tCExecution, String regex) {
        LOG.debug("Control : VerifyTextNotInPage on : " + regex);
        MessageEvent mes;

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_APK.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_IPA.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

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
                        mes.setDescription(mes.getDescription().replace("%STRING1%", Pattern.quote(regex)));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", Pattern.quote(regex)));
                        return mes;
                    }
                } catch (PatternSyntaxException e) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE_INVALIDPATERN);
                    mes.setDescription(mes.getDescription().replace("%PATERN%", Pattern.quote(regex)));
                    mes.setDescription(mes.getDescription().replace("%ERROR%", e.getMessage()));
                    return mes;
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }

        } else {

            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "VerifyTextNotInPage"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }
    }

    private MessageEvent verifyUrl(TestCaseExecution tCExecution, String value1) throws CerberusEventException {
        LOG.debug("Control : verifyUrl on : " + value1);

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            MessageEvent mes;
            String url = "";
            // Control is made forcing the / at the beginning of URL. getCurrentUrl from Selenium
            //  already have that control but value1 is specified by user and could miss it.
            final String controlUrl = StringUtil.addPrefixIfNotAlready(value1, "/");
//            url = this.webdriverService.getCurrentUrl(tCExecution.getSession(), tCExecution.getUrl());

            try {
                LOG.debug("Before wait : " + System.currentTimeMillis());
                WebDriverWait wait = new WebDriverWait(tCExecution.getSession().getDriver(),
                        TimeUnit.MILLISECONDS.toSeconds(tCExecution.getSession().getCerberus_selenium_wait_element()));

                //Wait until the url is the expected one
                wait.until(new Function<WebDriver, Boolean>() {

                    String value = "";
                    String expectedValue = controlUrl;

                    public Boolean apply(WebDriver driver) {
                        try {
                            this.value = webdriverService.getCurrentUrl(tCExecution.getSession(), tCExecution.getUrl());
                            LOG.debug("Get new url : " + value + " >> Expected url : " + expectedValue);
                        } catch (CerberusEventException ex) {
                            LOG.warn(ex.getMessageError().getDescription());
                        }
                        return value.equalsIgnoreCase(expectedValue);
                    }

                });

                LOG.debug("After wait : " + System.currentTimeMillis());
                url = this.webdriverService.getCurrentUrl(tCExecution.getSession(), tCExecution.getUrl());

                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL);
                mes.setDescription(mes.getDescription().replace("%STRING1%", url));
                mes.setDescription(mes.getDescription().replace("%STRING2%", controlUrl));
                return mes;

            } catch (TimeoutException exception) {
                url = this.webdriverService.getCurrentUrl(tCExecution.getSession(), tCExecution.getUrl());
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL);
                mes.setDescription(mes.getDescription().replace("%STRING1%", url));
                mes.setDescription(mes.getDescription().replace("%STRING2%", controlUrl));
                return mes;

            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }

        } else {

            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyUrl"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }
    }

    private MessageEvent verifyTitle(TestCaseExecution tCExecution, String title, String isCaseSensitive) {
        LOG.debug("Control : verifyTitle on : " + title);
        MessageEvent mes;
        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            String pageTitle = this.webdriverService.getTitle(tCExecution.getSession());

            try {
                LOG.debug("Before wait" + System.currentTimeMillis());
                WebDriverWait wait = new WebDriverWait(tCExecution.getSession().getDriver(),
                        TimeUnit.MILLISECONDS.toSeconds(tCExecution.getSession().getCerberus_selenium_wait_element()));

                //Wait until the title is the expected one
                wait.until(new Function<WebDriver, Boolean>() {

                    String value;
                    String expectedValue = title;

                    public Boolean apply(WebDriver driver) {
                        this.value = webdriverService.getTitle(tCExecution.getSession());
                        LOG.debug("Get new title : " + value + " >> Expected title : " + expectedValue);
                        return ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? expectedValue.equals(value) : expectedValue.equalsIgnoreCase(value);
                    }
                });
                LOG.debug("After wait" + System.currentTimeMillis());

                pageTitle = this.webdriverService.getTitle(tCExecution.getSession());

                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE);
                mes.setDescription(mes.getDescription().replace("%STRING1%", pageTitle));
                mes.setDescription(mes.getDescription().replace("%STRING2%", title));
                mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
                return mes;

            } catch (TimeoutException exception) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE);
                mes.setDescription(mes.getDescription().replace("%STRING1%", pageTitle));
                mes.setDescription(mes.getDescription().replace("%STRING2%", title));
                mes.setDescription(mes.getDescription().replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive)));
                return mes;
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }

        } else {

            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyTitle"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }
    }

    private MessageEvent verifyXmlTreeStructure(TestCaseExecution tCExecution, String controlProperty, String controlValue) {
        LOG.debug("Control : verifyXmlTreeStructure on : " + controlProperty);
        MessageEvent mes;
        if (Application.TYPE_SRV.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            try {

                if (tCExecution.getLastServiceCalled() != null) {

                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:

                            String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                            if (this.xmlUnitService.isSimilarTree(xmlResponse, controlProperty, controlValue)) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_SIMILARTREE);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", StringUtil.sanitize(controlProperty)));
                                mes.setDescription(mes.getDescription().replace("%STRING2%", StringUtil.sanitize(controlValue)));
                                return mes;
                            } else {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SIMILARTREE);
                                mes.setDescription(mes.getDescription().replace("%STRING1%", StringUtil.sanitize(controlProperty)));
                                mes.setDescription(mes.getDescription().replace("%STRING2%", StringUtil.sanitize(controlValue)));
                                return mes;
                            }
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyXmlTreeStructure"));
                            return mes;
                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }

            } catch (Exception exception) {
                LOG.fatal(exception.toString());
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED);
                return mes;
            }
        } else {

            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyXmlTreeStructure"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
            return mes;

        }
    }

    private MessageEvent verifyElementClickable(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control : verifyElementClickable : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {
                try {
                    if (this.webdriverService.isElementClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CLICKABLE);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementClickable"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE_NULL);
        }
    }

    private MessageEvent verifyElementNotClickable(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control : verifyElementNotClickable on : " + html);
        MessageEvent mes;
        if (!StringUtil.isNull(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {
                try {
                    if (this.webdriverService.isElementNotClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCLICKABLE);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", html));
                        return mes;
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", html));
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONTROL%", "VerifyElementNotClickable"));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getAppTypeEngine()));
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE_NULL);
        }
    }

    private String caseSensitiveMessageValue(String isCaseSensitive) {

        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            return "case sensitive";
        } else {
            return "case insensitive";
        }
    }

    private MessageEvent takeScreenshot(TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution, String cropValues) {
        MessageEvent message;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {
            TestCaseExecutionFile file = recorderService.recordScreenshot(tCExecution, testCaseStepActionExecution, testCaseStepActionControlExecution.getControlSequence(), cropValues);
            testCaseStepActionControlExecution.addFileList(file);
            message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TAKESCREENSHOT);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%CONTROL%", "takeScreenShot"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine()));
        return message;
    }

    private MessageEvent getPageSource(TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        MessageEvent message;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {
            TestCaseExecutionFile file = recorderService.recordPageSource(tCExecution, testCaseStepActionExecution, testCaseStepActionControlExecution.getControlSequence());
            if (file != null) {
                List<TestCaseExecutionFile> fileList = new ArrayList<>();
                fileList.add(file);
                testCaseStepActionControlExecution.setFileList(fileList);
            }
            message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GETPAGESOURCE);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%CONTROL%", "takeScreenShot"));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine()));
        return message;
    }

    /**
     * @author memiks
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.error(exception.toString(), exception);
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
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
