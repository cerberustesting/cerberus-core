/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.engine.gwt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.Ints;
import com.jayway.jsonpath.PathNotFoundException;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.execution.IRobotServerService;
import org.cerberus.core.engine.execution.impl.RobotServerService;
import org.cerberus.core.engine.gwt.IControlService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.service.json.IJsonService;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.service.robotextension.impl.SikuliService;
import org.cerberus.core.service.webdriver.IWebDriverService;
import org.cerberus.core.service.xmlunit.IXmlUnitService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    @Autowired
    private IRobotServerService robotServerService;

    @Override
    public TestCaseStepActionControlExecution doControl(TestCaseStepActionControlExecution controlExecution) {
        MessageEvent res;
        TestCaseExecution execution = controlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // Empty Execution values depending of the action.
        controlExecution = cleanValues(controlExecution);

        // Decode the step action control description
        try {
            // When starting a new control, we reset the property list that was already calculated.
            execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(controlExecution.getDescription(),
                    execution, controlExecution.getTestCaseStepActionExecution(), false);
            controlExecution.setDescription(answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the control result.
                controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Description"));
                controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                controlExecution.setEnd(new Date().getTime());
                LOG.debug("Control interrupted due to decode 'Description' Error.");
                return controlExecution;
            }
        } catch (CerberusEventException cex) {
            controlExecution.setControlResultMessage(cex.getMessageError());
            controlExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            return controlExecution;
        }

        //Decode the 2 fields property and values before doing the control.
        try {

            // for both control property and control value
            //if the getvalue() indicates that the execution should stop then we stop it before the doControl  or
            //if the property service was unable to decode the property that is specified in the object,
            //then the execution of this control should not performed
            if (controlExecution.getValue1() == null) {
                controlExecution.setValue1("");
            }
            if (controlExecution.getValue1().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(controlExecution.getValue1(), execution,
                        controlExecution.getTestCaseStepActionExecution(), false);
                controlExecution.setValue1(answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value1"));
                    controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    controlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interrupted due to decode 'Control Value1' Error.");
                    return controlExecution;
                }

            }

            if (controlExecution.getValue2() == null) {
                controlExecution.setValue2("");
            }
            if (controlExecution.getValue2().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(controlExecution.getValue2(),
                        execution, controlExecution.getTestCaseStepActionExecution(), false);
                controlExecution.setValue2(answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value2"));
                    controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    controlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interrupted due to decode 'Control Value2' Error.");
                    return controlExecution;
                }

            }

            if (controlExecution.getValue3() == null) {
                controlExecution.setValue3("");
            }
            if (controlExecution.getValue3().contains("%")) {

                // When starting a new control, we reset the property list that was already calculated.
                execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

                answerDecode = variableService.decodeStringCompletly(controlExecution.getValue3(),
                        execution, controlExecution.getTestCaseStepActionExecution(), false);
                controlExecution.setValue3(answerDecode.getItem());

                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the control result.
                    controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Value3"));
                    controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                    controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    controlExecution.setEnd(new Date().getTime());
                    LOG.debug("Control interrupted due to decode 'Control Value3' Error.");
                    return controlExecution;
                }

            }
        } catch (CerberusEventException cex) {
            controlExecution.setControlResultMessage(cex.getMessageError());
            controlExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            return controlExecution;
        }

        //Timestamp starts after the decode.
        controlExecution.setStart(new Date().getTime());
        controlExecution.setEnd(new Date().getTime());

        // When starting a new control, we reset the property list that was already calculated.
        execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

        String value1 = controlExecution.getValue1();
        String value2 = controlExecution.getValue2();
        String value3 = controlExecution.getValue3();

        // Define Timeout
        HashMap<String, String> optionsMap = robotServerService.getMapFromOptions(controlExecution.getOptions());
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TIMEOUT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX).isEmpty()) {
            Optional<Integer> timeoutOptionValue = Optional.ofNullable(Ints.tryParse(optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX)));
            if (timeoutOptionValue.isPresent()) {
                robotServerService.setOptionsTimeout(execution.getSession(), timeoutOptionValue.get());
            } else {
                //TODO return a message alerting about the failed cast
                LOG.debug("failed to parse option value : {}", optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
            }
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX).isEmpty()) {
            Optional<Integer> highlightOptionValue = Optional.ofNullable(Ints.tryParse(optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX)));
            if (highlightOptionValue.isPresent()) {
                robotServerService.setOptionsHighlightElement(execution.getSession(), highlightOptionValue.get());
            } else {
                //TODO return a message alerting about the failed cast
                LOG.debug("failed to parse option value : {}", optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
            }
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX).isEmpty()) {
            String minSimilarity = optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX);
            robotServerService.setOptionsMinSimilarity(execution.getSession(), minSimilarity);
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX).isEmpty()) {
            String typeDelay = optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX);
            robotServerService.setOptionsTypeDelay(execution.getSession(), typeDelay);
        }

        // Record picture= files at action level.
        Identifier identifier = identifierService.convertStringToIdentifier(value1);
        if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
            LOG.debug("Saving Image 1 on Control : {}", identifier.getLocator());
            controlExecution.addFileList(recorderService.recordPicture(controlExecution.getTestCaseStepActionExecution(), controlExecution.getControlId(), identifier.getLocator(), "1"));
        }
        identifier = identifierService.convertStringToIdentifier(value2);
        if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
            LOG.debug("Saving Image 2 on Control : {}", identifier.getLocator());
            controlExecution.addFileList(recorderService.recordPicture(controlExecution.getTestCaseStepActionExecution(), controlExecution.getControlId(), identifier.getLocator(), "2"));
        }

        /**
         * Wait in ms before the control.
         */
        if (controlExecution.getWaitBefore() > 0) {
            try {
                Thread.sleep(Long.parseLong(String.valueOf(controlExecution.getWaitBefore())));
            } catch (InterruptedException ex) {
                LOG.error("Exception when waiting before control. {}-{}-{}-{}", execution.getId(), controlExecution.getStepId(), controlExecution.getActionId(), controlExecution.getId(), ex);
            }
        }
        /**
         * TODO add a wait in ms before the control.
         */
//        Thead.sleep();

        try {

            switch (controlExecution.getControl()) {

                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGEQUAL:
                    res = this.verifyStringEqual(value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGDIFFERENT:
                    res = this.verifyStringDifferent(value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGGREATER:
                    res = this.verifyStringGreater(value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGMINOR:
                    res = this.verifyStringMinor(value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGCONTAINS:
                    res = this.verifyStringContains(value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGNOTCONTAINS:
                    res = this.verifyStringNotContains(value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR:
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL:
                    res = this.evaluateControlIfNumericXXX(controlExecution.getControl(), value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT:
                    res = this.verifyElementPresent(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT:
                    res = this.verifyElementNotPresent(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE:
                    res = this.verifyElementVisible(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE:
                    res = this.verifyElementNotVisible(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTCHECKED:
                    res = this.verifyElementChecked(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCHECKED:
                    res = this.verifyElementNotChecked(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS:
                    res = this.verifyElementEquals(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT:
                    res = this.verifyElementDifferent(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTINELEMENT:
                    res = this.verifyElementInElement(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE:
                    res = this.verifyElementClickable(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE:
                    res = this.verifyElementNotClickable(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL, execution, value1, controlExecution.getValue2(), controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX:
                    res = this.verifyElementTextMatchRegex(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYSTRINGARRAYCONTAINS:
                    res = this.verifyStringArrayContains(value1, value2, value3);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYNUMERICARRAYCONTAINS:
                    res = this.verifyNumericArrayContains(value1, value2);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS, execution, value1, value2, value3);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS:
                    res = this.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS, execution, value1, value2, value3);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTINPAGE:
                    res = this.verifyTextInPage(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTNOTINPAGE:
                    res = this.verifyTextNotInPage(execution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEEQUAL:
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEDIFFERENT:
                case TestCaseStepActionControl.CONTROL_VERIFYTITLECONTAINS:
                case TestCaseStepActionControl.CONTROL_VERIFYTITLENOTCONTAINS:
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEMATCHREGEX:
                    res = this.verifyTitle(execution, controlExecution.getControl(), value1, controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
                case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
                case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
                case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
                case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
                    res = this.verifyUrl(execution, controlExecution.getControl(), value1, controlExecution.getValue3());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTEXTINDIALOG:
                    res = this.verifyTextInDialog(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYXMLTREESTRUCTURE:
                    res = this.verifyXmlTreeStructure(execution, value1, controlExecution.getValue2());
                    break;
                case TestCaseStepActionControl.CONTROL_TAKESCREENSHOT:
                    res = this.takeScreenshot(execution, controlExecution.getTestCaseStepActionExecution(), controlExecution, value1);
                    break;
                case TestCaseStepActionControl.CONTROL_GETPAGESOURCE:
                    res = this.getPageSource(execution, controlExecution.getTestCaseStepActionExecution(), controlExecution);
                    break;

                default:
                    res = new MessageEvent(MessageEventEnum.CONTROL_FAILED_UNKNOWNCONTROL);
                    res.resolveDescription("CONTROL", controlExecution.getControl());
            }
        } catch (final CerberusEventException exception) {
            res = exception.getMessageError();

        } catch (final Exception unexpected) {
            LOG.debug("Unexpected exception on control!", unexpected);
            res = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC).resolveDescription("ERROR", unexpected.getMessage());
        }

        // Reset Timeout to default
        robotServerService.setOptionsToDefault(execution.getSession());

        /**
         * Put Wait in ms before the action to message.
         */
        if (controlExecution.getWaitBefore() > 0) {
            res.setDescription(res.getDescription() + " -- Waited " + String.valueOf(controlExecution.getWaitBefore()) + " ms Before.");
        }

        /**
         * Wait in ms after the action.
         */
        if (controlExecution.getWaitAfter() > 0) {
            try {
                Thread.sleep(Long.parseLong(String.valueOf(controlExecution.getWaitAfter())));
                res.setDescription(res.getDescription() + " -- Waited " + String.valueOf(controlExecution.getWaitAfter()) + " ms After.");
            } catch (InterruptedException ex) {
                LOG.error("Exception when waiting after control. {}-{}-{}-{}", execution.getId(), controlExecution.getStepId(), controlExecution.getActionId(), controlExecution.getId(), ex);
            }
        }

        controlExecution.setControlResultMessage(res);

        /*
         * Updating Control result message only if control is not successful.
         * This is to keep the last KO information and preventing KO to be
         * transformed to OK.
         */
        if (!(res.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
            controlExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));
        }

        /*
         * We only stop the test if Control Event message is in stop status AND
         * the control is FATAL. If control is not fatal, we continue the test
         * but refresh the Execution status.
         */
        if (res.isStopTest() && controlExecution.getFatal().equals("Y")) {
            controlExecution.setStopExecution(true);
        }

        controlExecution.setEnd(new Date().getTime());
        return controlExecution;
    }

    private TestCaseStepActionControlExecution cleanValues(TestCaseStepActionControlExecution controlExecution) {
        switch (controlExecution.getControl()) {

            // No parameters
            case TestCaseStepActionControl.CONTROL_GETPAGESOURCE:
                controlExecution.setValue1("");
                controlExecution.setValue1Init("");
                controlExecution.setValue2("");
                controlExecution.setValue2Init("");
                controlExecution.setValue3("");
                controlExecution.setValue3Init("");
                break;
            // Only Value1
            case TestCaseStepActionControl.CONTROL_VERIFYTEXTINPAGE:
            case TestCaseStepActionControl.CONTROL_VERIFYTEXTNOTINPAGE:
            case TestCaseStepActionControl.CONTROL_VERIFYTEXTINDIALOG:
            case TestCaseStepActionControl.CONTROL_TAKESCREENSHOT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTCHECKED:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCHECKED:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE:
                controlExecution.setValue2("");
                controlExecution.setValue2Init("");
                controlExecution.setValue3("");
                controlExecution.setValue3Init("");
                break;
            // Only Value 1 and Value 3
            case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
            case TestCaseStepActionControl.CONTROL_VERIFYTITLEEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYTITLEDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYTITLECONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYTITLENOTCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYTITLEMATCHREGEX:
                controlExecution.setValue2("");
                controlExecution.setValue2Init("");
                break;
            // Only Value 1 and Value 2
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGMINOR:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX:
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICARRAYCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS:
                controlExecution.setValue3("");
                controlExecution.setValue3Init("");
                break;
            // Value 1 and Value 2 and Value 3
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGGREATER:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGARRAYCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYSTRINGNOTCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTINELEMENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS:
            case TestCaseStepActionControl.CONTROL_VERIFYXMLTREESTRUCTURE:
                break;
            default:

        }
        return controlExecution;
    }

    private MessageEvent verifyStringDifferent(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !value1.equals(value2) : !value1.equalsIgnoreCase(value2)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_STRINGDIFFERENT);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_STRINGDIFFERENT);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));
        return mes;
    }

    private MessageEvent verifyStringEqual(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.equals(value2) : value1.equalsIgnoreCase(value2)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_STRINGEQUAL);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_STRINGEQUAL);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));
        return mes;

    }

    private MessageEvent verifyStringContains(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.contains(value2) : value1.toLowerCase().contains(value2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CONTAINS);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CONTAINS);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));
        return mes;

    }

    private MessageEvent verifyStringNotContains(String value1, String value2, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? value1.contains(value2) : value1.toLowerCase().contains(value2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCONTAINS);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCONTAINS);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));
        return mes;

    }

    private MessageEvent verifyStringGreater(String value1, String value2) {
        MessageEvent mes;
        if (value1.compareToIgnoreCase(value2) > 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        return mes;
    }

    private MessageEvent verifyStringMinor(String value1, String value2) {
        MessageEvent mes;
        if (value1.compareToIgnoreCase(value2) < 0) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
        }
        mes.resolveDescription("STRING1", value1);
        mes.resolveDescription("STRING2", value2);
        return mes;
    }

    private MessageEvent evaluateControlIfNumericXXX(String control, String controlValue1, String controlValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Numeric Equals");
        }
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_PENDING);

        // We first prepare the string for numeric conversion to replace , by .
        String newControlValue1 = StringUtil.prepareToNumeric(controlValue1);
        String newControlValue2 = StringUtil.prepareToNumeric(controlValue2);

        // We try to convert the strings value1 to numeric.
        Double value1;
        try {
            value1 = Double.valueOf(newControlValue1);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
            mes.resolveDescription("COND", control);
            mes.resolveDescription("NEWSTRING", newControlValue1);
            mes.resolveDescription("STRINGVALUE", controlValue1);
            return mes;
        }

        // We try to convert the strings value2 to numeric.
        Double value2;
        try {
            value2 = Double.valueOf(newControlValue2);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
            mes.resolveDescription("COND", control);
            mes.resolveDescription("NEWSTRING", newControlValue2);
            mes.resolveDescription("STRINGVALUE", controlValue2);
            return mes;
        }

        // Now that both values are converted to double we check the operator here.
        switch (control) {
            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS:
                if (Objects.equals(value1, value2)) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_EQUAL);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_EQUAL);
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT:
                if (!(Objects.equals(value1, value2))) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_DIFFERENT);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_DIFFERENT);
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER:
                if (value1 > value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATER);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATER);
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL:
                if (value1 >= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GREATEROREQUAL);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GREATEROREQUAL);
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR:
                if (value1 < value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOR);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOR);
                }
                break;

            case TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL:
                if (value1 <= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_MINOROREQUAL);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_MINOROREQUAL);
                }
                break;

            default:
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED);
        }

        mes.resolveDescription("COND", control);
        mes.resolveDescription("STRING1", value1.toString());
        mes.resolveDescription("STRING2", value2.toString());
        return mes;
    }

    private MessageEvent verifyElementPresent(TestCaseExecution tCExecution, String elementPath) {
        LOG.debug("Control: verifyElementPresent on: {}", elementPath);
        MessageEvent mes;

        if (!StringUtil.isEmptyOrNULLString(elementPath)) {
            Identifier identifier = identifierService.convertStringToIdentifier(elementPath);

            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), "");

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), "", identifier.getLocator());

                    } else if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                        mes.resolveDescription("STRING1", elementPath);
                        return mes;

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                        mes.resolveDescription("STRING1", elementPath);
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
                            } else {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                            }
                            mes.resolveDescription("STRING1", elementPath);
                            return mes;
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                            try {
                                //Return of getFromJson can be "[]" in case when the path has this pattern "$..ex" and no elements found. Two dots after $ return a list.
                                if (!jsonService.getFromJson(tCExecution, responseBody, null, elementPath, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST).equals("[]")) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
                                    mes.resolveDescription("STRING1", elementPath);
                                    return mes;
                                } else {
                                    throw new PathNotFoundException();
                                }
                            } catch (PathNotFoundException ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                                mes.resolveDescription("STRING1", elementPath);
                                return mes;
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                mes.resolveDescription("ERROR", ex.toString());
                                return mes;
                            }
                        }
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT);
                            return mes;
                    }

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }
            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {

                if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), "");

                } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), "", identifier.getLocator());

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION_AND_IDENTIFIER);
                    mes.resolveDescription("IDENTIFIER", identifier.getIdentifier());
                    mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT);
                    mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                    return mes;

                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT_NULL);
        }
    }

    private MessageEvent verifyElementNotPresent(TestCaseExecution execution, String elementPath) {
        LOG.debug("Control: verifyElementNotPresent on: {}", elementPath);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(elementPath)) {
            Identifier identifier = identifierService.convertStringToIdentifier(elementPath);

            if (execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), identifier.getLocator(), "");

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), "", identifier.getLocator());

                    } else if (!this.webdriverService.isElementPresent(execution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                    }
                    mes.resolveDescription("STRING1", elementPath);
                    return mes;
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else if (execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {

                if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), identifier.getLocator(), "");

                } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), "", identifier.getLocator());

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION_AND_IDENTIFIER);
                    mes.resolveDescription("IDENTIFIER", identifier.getIdentifier());
                    mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT);
                    mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                    return mes;

                }

            } else if (execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {

                if (execution.getLastServiceCalled() != null) {

                    String responseBody = execution.getLastServiceCalled().getResponseHTTPBody();
                    switch (execution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (!(xmlUnitService.isElementPresent(responseBody, elementPath))) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                            } else {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                            }
                            mes.resolveDescription("STRING1", elementPath);
                            return mes;
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                            try {
                                //Return of getFromJson can be "[]" in case when the path has this pattern "$..ex" and no elements found. Two dots after $ return a list.
                                if (!jsonService.getFromJson(execution, responseBody, null, elementPath, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST).equals("[]")) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
                                    mes.resolveDescription("STRING1", elementPath);
                                    return mes;
                                } else {
                                    throw new PathNotFoundException();
                                }
                            } catch (PathNotFoundException ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
                                mes.resolveDescription("STRING1", elementPath);
                                return mes;
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                mes.resolveDescription("ERROR", ex.toString());
                                return mes;
                            }

                        }
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.resolveDescription("TYPE", execution.getLastServiceCalled().getResponseHTTPBodyContentType());
                            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT);
                            return mes;
                    }

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT);
                mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                return mes;
            }
        } else {
            return new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT_NULL);
        }
    }

    private MessageEvent verifyElementInElement(TestCaseExecution tCExecution, String element, String childElement) {
        LOG.debug("Control: verifyElementInElement on: '{}' is child of '{}'", element, childElement);

        MessageEvent mes;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {

            if (!StringUtil.isEmptyOrNULLString(element) && !StringUtil.isEmptyOrNULLString(childElement)) {
                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(element);
                    Identifier childIdentifier = identifierService.convertStringToIdentifier(childElement);
                    if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        if (this.webdriverService.isElementInElement(tCExecution.getSession(), identifier, childIdentifier)) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTINELEMENT);
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                        }
                        mes.resolveDescription("STRING2", element);
                        mes.resolveDescription("STRING1", childElement);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NO_SUCH_ELEMENT);
                        mes.resolveDescription("SELEX", new NoSuchElementException("").toString());
                        mes.resolveDescription("ELEMENT", element);
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTINELEMENT);
                mes.resolveDescription("STRING2", element);
                mes.resolveDescription("STRING1", childElement);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTINELEMENT);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyElementVisible(TestCaseExecution tCExecution, String elementPath) {
        LOG.debug("Control: verifyElementVisible on: {}", elementPath);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(elementPath)) {
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(elementPath);
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), "");

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), "", identifier.getLocator());

                    } else if (this.webdriverService.isElementVisible(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_VISIBLE);
                        mes.resolveDescription("STRING1", elementPath);
                        return mes;

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE);
                        mes.resolveDescription("STRING1", elementPath);
                        return mes;
                    }
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {

                Identifier identifier = identifierService.convertStringToIdentifier(elementPath);
                if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), "");

                } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), "", identifier.getLocator());

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION_AND_IDENTIFIER);
                    mes.resolveDescription("IDENTIFIER", identifier.getIdentifier());
                    mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE);
                    mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                    return mes;

                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VISIBLE_NULL);
        }
        return mes;
    }

    private MessageEvent verifyElementNotVisible(TestCaseExecution execution, String elementPath) {
        LOG.debug("Control: verifyElementNotVisible on: {}", elementPath);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(elementPath)) {
            if (execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(elementPath);
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), identifier.getLocator(), "");

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), "", identifier.getLocator());

                    } else if (this.webdriverService.isElementPresent(execution.getSession(), identifier)) {
                        if (this.webdriverService.isElementNotVisible(execution.getSession(), identifier)) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTVISIBLE);
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE);
                        }
                        mes.resolveDescription("STRING1", elementPath);

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
                    }
                    mes.resolveDescription("STRING1", elementPath);
                    return mes;
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }

            } else if (execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {

                Identifier identifier = identifierService.convertStringToIdentifier(elementPath);
                if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), identifier.getLocator(), "");

                } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliVerifyElementNotPresent(execution.getSession(), "", identifier.getLocator());

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION_AND_IDENTIFIER);
                    mes.resolveDescription("IDENTIFIER", identifier.getIdentifier());
                    mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE);
                    mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                    return mes;

                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE);
                mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTVISIBLE_NULL);
        }
        return mes;
    }

    private MessageEvent verifyElementChecked(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control: verifyElementChecked on: {}", html);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(html)) {
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(html);
                    if (this.webdriverService.isElementChecked(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CHECKED);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CHECKED);
                    }
                    mes.resolveDescription("STRING1", html);
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTCHECKED);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CHECKED_NULL);
        }
        return mes;
    }

    private MessageEvent verifyElementNotChecked(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control: verifyElementNotChecked on: {}", html);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(html)) {
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(html);
                    if (this.webdriverService.isElementNotChecked(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCHECKED);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCHECKED);
                    }
                    mes.resolveDescription("STRING1", html);
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCHECKED);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCHECKED_NULL);
        }
        return mes;
    }

    private MessageEvent verifyElementEquals(TestCaseExecution tCExecution, String xpath, String expectedElement) {
        LOG.debug("Control: verifyElementEquals on: {} expected Element: {}", xpath, expectedElement);
        MessageEvent mes;

        // If case of not compatible application then exit with error
        if (!tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        } else if (tCExecution.getLastServiceCalled() != null) {
            // Check if element on the given xpath is equal to the given expected element
            String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
            if (AppService.RESPONSEHTTPBODYCONTENTTYPE_XML.equals(tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType())) {
                mes = xmlUnitService.isElementEquals(xmlResponse, xpath, expectedElement) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTEQUALS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTEQUALS);
                mes.resolveDescription("XPATH", xpath);
                mes.resolveDescription("EXPECTED_ELEMENT", expectedElement);
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
        }
        return mes;
    }

    private MessageEvent verifyElementDifferent(TestCaseExecution tCExecution, String xpath, String differentElement) {
        LOG.debug("Control: verifyElementDifferent on: {} expected Element: {}", xpath, differentElement);
        MessageEvent mes = null;

        // If case of not compatible application then exit with error
        if (!tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_SRV)) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        } else if (tCExecution.getLastServiceCalled() != null) {
            //Check if element on the given xpath is different from the given different element
            if (AppService.RESPONSEHTTPBODYCONTENTTYPE_XML.equals(tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType())) {
                String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                mes = xmlUnitService.isElementEquals(xmlResponse, xpath, differentElement) ? new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTDIFFERENT) : new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTDIFFERENT);
                mes.resolveDescription("XPATH", xpath);
                mes.resolveDescription("DIFFERENT_ELEMENT", differentElement);
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
        }
        return mes;
    }

    @Override
    public MessageEvent verifyElementXXX(String control, TestCaseExecution tCExecution, String path, String expected, String isCaseSensitive) {
        LOG.debug("Control: verifyElementXXX ({}) on {} element against value: {} AppType: {}", control, path, expected, tCExecution.getAppTypeEngine());

        MessageEvent mes;
        // Get value from the path element according to the application type
        String actual;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getAppTypeEngine();

            switch (applicationType) {

                case Application.TYPE_GUI:
                case Application.TYPE_APK:
                case Application.TYPE_IPA:

                    actual = webdriverService.getValueFromHTML(tCExecution.getSession(), identifier, false, 0);
                    // In case of null actual value then we alert user
                    if (actual == null) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NULL);
                        mes.resolveDescription("STRING1", path);
                        return mes;
                    }

                    // Get the result depending on the control required.
                    mes = switchControl(control, path, actual, expected, isCaseSensitive);
                    return mes;

                case Application.TYPE_SRV:
                    if (tCExecution.getLastServiceCalled() != null && tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType() != null) {

                        String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                        switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                                if (!xmlUnitService.isElementPresent(responseBody, path)) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                    mes.resolveDescription("ELEMENT", path);
                                    return mes;
                                }
                                String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                                actual = xmlUnitService.getFromXml(responseBody, newPath);
                                // In case of null actual value then we alert user
                                if (actual == null) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NULL);
                                    mes.resolveDescription("ELEMENT", path);
                                    return mes;
                                }
                                // Get the result depending on the control required.
                                mes = switchControl(control, path, actual, expected, isCaseSensitive);
                                return mes;

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON: {
                                try {
                                    actual = jsonService.getFromJson(tCExecution, responseBody, null, path, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST);
                                } catch (Exception ex) {
                                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                                    mes.resolveDescription("ERROR", ex.toString());
                                    return mes;
                                }
                            }
                            // In case of null actual value then we alert user
                            if (actual == null) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                mes.resolveDescription("ELEMENT", path);
                                return mes;
                            }
                            // Get the result depending on the control required.
                            mes = switchControl(control, path, actual, expected, isCaseSensitive);
                            return mes;

                            default:
                                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                                mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                                mes.resolveDescription("CONTROL", control);
                                return mes;
                        }

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                        return mes;
                    }

                default:
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                    mes.resolveDescription("CONTROL", control);
                    mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                    return mes;

            }

        } catch (NoSuchElementException exception) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
            mes.resolveDescription("ELEMENT", path);
            return mes;
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }

    }

    private MessageEvent switchControl(String control, String path, String actual, String expected, String isCaseSensitive) {
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        mes.resolveDescription("CONTROL", "switchControl-" + control);

        switch (control) {
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL:
                mes = verifyElementTextEqualCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT:
                mes = verifyElementTextDifferentCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS:
                mes = verifyElementTextContainsCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS:
                mes = verifyElementTextNotContainsCaseSensitiveCheck(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS:
                //We use verifyStringArrayContains because it's the same behaviour. Difference is that here we retrieve array using json path or xpath
                mes = this.verifyElementTextArrayContains(actual, expected, isCaseSensitive);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS:
                //We use verifyStringArrayContains because it's the same behaviour. Difference is that here we retrieve array using json path or xpath
                mes = this.verifyElementNumericArrayContains(actual, expected);
                break;
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                double value1;
                String actualCleaned = StringUtil.prepareToNumeric(actual);
                try {
                    value1 = Double.parseDouble(actualCleaned);
                } catch (NumberFormatException nfe) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
                    mes.resolveDescription("COND", control);
                    mes.resolveDescription("NEWSTRING", actualCleaned);
                    mes.resolveDescription("STRINGVALUE", actual);
                    return mes;
                }

                // We try to convert the strings value2 to numeric.
                double value2;
                String expectedCleaned = StringUtil.prepareToNumeric(expected);
                try {
                    value2 = Double.parseDouble(expectedCleaned);
                } catch (NumberFormatException nfe) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_VALUES_NOTNUMERIC);
                    mes.resolveDescription("COND", control);
                    mes.resolveDescription("NEWSTRING", expectedCleaned);
                    mes.resolveDescription("STRINGVALUE", expected);
                    return mes;
                }
                mes = checkNumericVerifyElement(control, value1, value2);
                break;
        }
        mes.resolveDescription("ELEMENT", path);
        mes.resolveDescription("ELEMENTVALUE", actual);
        mes.resolveDescription("VALUE", expected);
        mes.resolveDescription("CASESENSITIVE", caseSensitiveMessageValue(isCaseSensitive));
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

    private MessageEvent verifyElementTextContainsCaseSensitiveCheck(String text, String textToSearch, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            mes = text.contains(textToSearch) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTCONTAINS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTCONTAINS);
        } else {
            mes = text.toLowerCase().contains(textToSearch.toLowerCase()) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTCONTAINS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTCONTAINS);
        }
        return mes;
    }

    private MessageEvent verifyElementTextNotContainsCaseSensitiveCheck(String text, String textToSearch, String isCaseSensitive) {
        MessageEvent mes;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            mes = !text.contains(textToSearch) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTNOTCONTAINS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTNOTCONTAINS);
        } else {
            mes = !text.toLowerCase().contains(textToSearch.toLowerCase()) ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTNOTCONTAINS) : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTNOTCONTAINS);
        }
        return mes;
    }

    private MessageEvent checkNumericVerifyElement(String control, Double actual, Double expected) {
        switch (control) {
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL:
                return actual.equals(expected)
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICEQUAL)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICEQUAL);
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT:
                return !actual.equals(expected)
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICDIFFERENT)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICDIFFERENT);
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER:
                return actual > expected
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICGREATER)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICGREATER);
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL:
                return actual >= expected
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICGREATEROREQUAL)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICGREATEROREQUAL);
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR:
                return actual < expected
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICMINOR)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICMINOR);
            case TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL:
                return actual <= expected
                        ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICMINOROREQUAL)
                        : new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICMINOROREQUAL);
            default:
                MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                return mes.resolveDescription("CONTROL", "checkNumericVerifyElement-" + control);
        }
    }

    private MessageEvent verifyElementTextMatchRegex(TestCaseExecution tCExecution, String path, String regex) {
        LOG.debug("Control: VerifyElementTextMatchRegex on: {} element against value: {}", path, regex);
        MessageEvent mes;
        String pathContent;
        try {

            Identifier identifier = identifierService.convertStringToIdentifier(path);
            String applicationType = tCExecution.getAppTypeEngine();
            // Get value from the path element according to the application type
            if (Application.TYPE_GUI.equalsIgnoreCase(applicationType)
                    || Application.TYPE_APK.equalsIgnoreCase(applicationType)
                    || Application.TYPE_IPA.equalsIgnoreCase(applicationType)) {

                pathContent = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier, false, 0);
            } else if (Application.TYPE_SRV.equalsIgnoreCase(applicationType)) {
                if (tCExecution.getLastServiceCalled() != null) {
                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (!xmlUnitService.isElementPresent(responseBody, path)) {
                                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_ELEMENT_NOSUCHELEMENT);
                                mes.resolveDescription("ELEMENT", path);
                                return mes;
                            }
                            String newPath = StringUtil.addSuffixIfNotAlready(path, "/text()");
                            pathContent = xmlUnitService.getFromXml(responseBody, newPath);
                            break;

                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                            try {
                            pathContent = jsonService.getFromJson(tCExecution, responseBody, null, path, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST);
                        } catch (Exception ex) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
                            mes.resolveDescription("ERROR", ex.toString());
                            return mes;
                        }
                        break;

                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX);
                            return mes;
                    }

                    // TODO Give the actual element found into the description.
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                    return mes;
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                return mes;
            }
            LOG.debug("Control: VerifyElementMatchRegex element: {} has value: {}", path, StringUtil.sanitize(pathContent));
            if (path != null && pathContent != null) {
                try {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(pathContent);
                    if (matcher.find()) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_REGEXINELEMENT);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT);
                    }
                    mes.resolveDescription("STRING1", path);
                    mes.resolveDescription("STRING2", StringUtil.sanitize(pathContent));
                    mes.resolveDescription("STRING3", regex);
                } catch (PatternSyntaxException e) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_INVALIDPATTERN);
                    mes.resolveDescription("PATTERN", regex);
                    mes.resolveDescription("ERROR", e.getMessage());
                }
            } else if (pathContent != null) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NULL);
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
                mes.resolveDescription("ELEMENT", path);
            }
        } catch (NoSuchElementException exception) {
            LOG.debug(exception.toString());
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT);
            mes.resolveDescription("ELEMENT", path);
        } catch (WebDriverException exception) {
            return parseWebDriverException(exception);
        }
        return mes;
    }

    private MessageEvent verifyStringArrayContains(String array, String valueToSearch, String isCaseSensitive) {
        MessageEvent mes;
        try {
            List<String> strings = StringUtil.convertStringToStringArray(array);
            //When user choose case sensitive option
            boolean isContained = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)
                    ? strings.stream().anyMatch(valueToSearch::equals)
                    : strings.stream().anyMatch(valueToSearch::equalsIgnoreCase);

            mes = isContained
                    ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_STRINGARRAYCONTAINS)
                    : new MessageEvent(MessageEventEnum.CONTROL_FAILED_STRINGARRAYCONTAINS);
            mes.resolveDescription("ELEMENT", array);
            mes.resolveDescription("VALUE", valueToSearch);
            mes.resolveDescription("CASESENSITIVE", caseSensitiveMessageValue(isCaseSensitive));
        } catch (JsonProcessingException exception) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
            mes.resolveDescription("ERROR", "Incorrect array structure.");
        }
        return mes;
    }

    private MessageEvent verifyNumericArrayContains(String array, String numberToSearch) {
        MessageEvent mes;
        try {
            List<Double> doubles = StringUtil.convertStringToDoubleArray(array);
            Double number = Double.parseDouble(numberToSearch);
            mes = doubles.stream().anyMatch(number::equals)
                    ? new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NUMERICARRAYCONTAINS)
                    : new MessageEvent(MessageEventEnum.CONTROL_FAILED_NUMERICARRAYCONTAINS);
            mes.resolveDescription("ELEMENT", array);
            mes.resolveDescription("VALUE", numberToSearch);
        } catch (JsonProcessingException exception) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
            mes.resolveDescription("ERROR", "Incorrect array structure.");
        } catch (NumberFormatException exception) {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC)
                    .resolveDescription("ERROR", "Value to search must be a number and the array must be an array of numbers.");
        }
        return mes;
    }

    private MessageEvent verifyElementTextArrayContains(String array, String valueToSearch, String isCaseSensitive) {
        //We use verifyStringArrayContains because it's the same behaviour. The difference is that here we have an array retrieved using json path or xpath.
        MessageEvent mes = this.verifyStringArrayContains(array, valueToSearch, isCaseSensitive);
        //Change the message event to adapt to this control.
        if (!mes.getSource().equals(MessageEventEnum.CONTROL_FAILED_GENERIC)) {
            if (mes.getCodeString().equals("OK")) {
                mes.setDescription(MessageEventEnum.CONTROL_SUCCESS_ELEMENTTEXTARRAYCONTAINS.getDescription());
            } else {
                mes.setDescription(MessageEventEnum.CONTROL_FAILED_ELEMENTTEXTARRAYCONTAINS.getDescription());
            }
        }
        return mes;
    }

    private MessageEvent verifyElementNumericArrayContains(String array, String numberToSearch) {
        //We use verifyStringArrayContains because it's the same behaviour. The difference is that here we have an array retrieved using json path or xpath.
        MessageEvent mes = this.verifyNumericArrayContains(array, numberToSearch);
        //Change the message event to adapt to this control.
        if (!mes.getSource().equals(MessageEventEnum.CONTROL_FAILED_GENERIC)) {
            if (mes.getCodeString().equals("OK")) {
                mes.setDescription(MessageEventEnum.CONTROL_SUCCESS_ELEMENTNUMERICARRAYCONTAINS.getDescription());
            } else {
                mes.setDescription(MessageEventEnum.CONTROL_FAILED_ELEMENTNUMERICARRAYCONTAINS.getDescription());
            }
        }
        return mes;
    }

    private MessageEvent verifyTextInDialog(TestCaseExecution tCExecution, String property, String value) {
        LOG.debug("Control: verifyTextInDialog against value: {}", value);
        MessageEvent mes;

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {
            try {
                String str = this.webdriverService.getAlertText(tCExecution.getSession());
                LOG.debug("Control: verifyTextInAlertPopup has value: {}", str);
                if (str != null) {
                    String valueToTest = property;
                    if (valueToTest == null || "".equals(valueToTest.trim())) {
                        valueToTest = value;
                    }

                    if (str.trim().equalsIgnoreCase(valueToTest)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINALERT);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT);
                    }
                    mes.resolveDescription("STRING1", str);
                    mes.resolveDescription("STRING2", valueToTest);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINALERT_NULL);
                }
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYTEXTINDIALOG);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyTextInPage(TestCaseExecution tCExecution, String regex) {
        LOG.debug("Control: verifyTextInPage on : {}", regex);
        MessageEvent mes;

        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_APK.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_IPA.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            String pageSource;
            try {
                pageSource = this.webdriverService.getPageSource(tCExecution.getSession());
                LOG.debug(pageSource);
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pageSource);
                if (matcher.find()) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINPAGE);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE);
                }
                mes.resolveDescription("STRING1", Pattern.quote(regex));
            } catch (PatternSyntaxException e) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE_INVALIDPATTERN);
                mes.resolveDescription("PATTERN", Pattern.quote(regex));
                mes.resolveDescription("ERROR", e.getMessage());
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else if (Application.TYPE_FAT.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {
            mes = sikuliService.doSikuliVerifyTextInPage(tCExecution.getSession(), regex);
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYTEXTINPAGE);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyTextNotInPage(TestCaseExecution tCExecution, String regex) {
        LOG.debug("Control: VerifyTextNotInPage on: {}", regex);
        MessageEvent mes;
        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_APK.equalsIgnoreCase(tCExecution.getAppTypeEngine())
                || Application.TYPE_IPA.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            String pageSource;
            try {
                pageSource = this.webdriverService.getPageSource(tCExecution.getSession());
                LOG.debug(pageSource);

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pageSource);
                if (!(matcher.find())) {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTNOTINPAGE);
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE);
                }
                mes.resolveDescription("STRING1", Pattern.quote(regex));

            } catch (PatternSyntaxException e) {
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTNOTINPAGE_INVALIDPATTERN);
                mes.resolveDescription("PATTERN", Pattern.quote(regex));
                mes.resolveDescription("ERROR", e.getMessage());
            } catch (WebDriverException exception) {
                return parseWebDriverException(exception);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYTEXTNOTINPAGE);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyUrl(TestCaseExecution execution, String control, String value1, String isCaseSensitive) throws CerberusEventException {
        LOG.debug("Control: verifyUrl on: {}", value1);

        MessageEvent mes;
        if (Application.TYPE_GUI.equalsIgnoreCase(execution.getAppTypeEngine())) {

            // Control is made forcing the / at the beginning of URL. getCurrentUrl from Selenium
            //  already have that control but value1 is specified by user and could miss it.
            String expectedValue = value1;

            String url = "";
            boolean continueChecking = true;
            long startLoopingTimestamp = new Date().getTime();

            do {

                try {
                    url = webdriverService.getCurrentUrl(execution.getSession(), execution.getUrl());
                    LOG.debug("Get new url: {} >> Expected url: {}", url, expectedValue);

                    if (url.startsWith("/")) {
                        if ((TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL.equals(control)) || (TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT.equals(control))) {
                            expectedValue = StringUtil.addPrefixIfNotAlready(value1, "/");
                        }
                    }

                } catch (CerberusEventException ex) {
                    LOG.warn(ex.getMessageError().getDescription());
                }

                boolean result;
                switch (control) {
                    case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
                        result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? url.equals(expectedValue) : url.equalsIgnoreCase(expectedValue);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
                        result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !url.equals(expectedValue) : !url.equalsIgnoreCase(expectedValue);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
                        result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? url.contains(expectedValue) : url.toLowerCase().contains(expectedValue.toLowerCase());
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
                        result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !url.contains(expectedValue) : !url.toLowerCase().contains(expectedValue.toLowerCase());
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
                        Pattern pattern;
                        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
                            pattern = Pattern.compile(expectedValue);
                        } else {
                            pattern = Pattern.compile(expectedValue, Pattern.CASE_INSENSITIVE);
                        }
                        Matcher matcher = pattern.matcher(url);
                        result = matcher.find();
                        break;
                    default:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                        mes.resolveDescription("CONTROL", control);
                        mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                        return mes;
                }

                if (result) {

                    switch (control) {
                        case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL_EQUAL);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL_DIFFERENT);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL_CONTAINS);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL_NOTCONTAINS);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_URL_MATCHREGEX);
                            break;
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                            mes.resolveDescription("CONTROL", control);
                            mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                            return mes;
                    }
                } else {

                    switch (control) {
                        case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_EQUAL);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_DIFFERENT);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_CONTAINS);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOTCONTAINS);
                            break;
                        case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_MATCHREGEX);
                            break;
                        default:
                            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                            mes.resolveDescription("CONTROL", control);
                            mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
                            return mes;
                    }
                }
                mes.resolveDescription("STRING1", url);
                mes.resolveDescription("STRING2", expectedValue);
                mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));

                // Retry condition
                switch (control) {
                    case TestCaseStepActionControl.CONTROL_VERIFYURLEQUAL:
                    case TestCaseStepActionControl.CONTROL_VERIFYURLCONTAINS:
                    case TestCaseStepActionControl.CONTROL_VERIFYURLMATCHREGEX:
                        if (mes.getCode() == MessageEventEnum.CONTROL_SUCCESS_URL_EQUAL.getCode()) {
                            // We found the correct expected URL so we can stop looping
                            continueChecking = false;
                        } else {
                            continueChecking = true;
                        }
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYURLDIFFERENT:
                    case TestCaseStepActionControl.CONTROL_VERIFYURLNOTCONTAINS:
                        continueChecking = true;
                        break;
                    default:
                        continueChecking = false;
                }

                if (continueChecking && (new Date().getTime() - startLoopingTimestamp) < execution.getSession().getCerberus_selenium_wait_element()) {
                    try {
                        Thread.sleep(200);
                        LOG.debug("Waiting 200 ms");
                    } catch (InterruptedException ex) {
                        LOG.error(ex, ex);
                    }
                }

                LOG.debug("Do we continue : {} - return code {} - Duration {} ms", continueChecking, mes.getCode(), (new Date().getTime() - startLoopingTimestamp));
            } while (continueChecking && (new Date().getTime() - startLoopingTimestamp) < execution.getSession().getCerberus_selenium_wait_element());

        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", control);
            mes.resolveDescription("APPLICATIONTYPE", execution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyTitle(TestCaseExecution tCExecution, String control, String title, String isCaseSensitive) throws CerberusEventException {
        LOG.debug("Control: verifyTitle on: {}", title);

        MessageEvent mes;
        if (Application.TYPE_GUI.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            // Control is made forcing the / at the beginning of URL. getCurrentUrl from Selenium
            //  already have that control but value1 is specified by user and could miss it.
            String expectedValue = title;

            String pageTitle = "";

            pageTitle = this.webdriverService.getTitle(tCExecution.getSession());
            LOG.debug("Get Title: {} >> Expected Title: {}", pageTitle, expectedValue);

            boolean result;
            switch (control) {
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEEQUAL:
                    result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? expectedValue.equals(pageTitle) : expectedValue.equalsIgnoreCase(pageTitle);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEDIFFERENT:
                    result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !expectedValue.equals(pageTitle) : !expectedValue.equalsIgnoreCase(pageTitle);
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLECONTAINS:
                    result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? pageTitle.contains(expectedValue) : pageTitle.toLowerCase().contains(expectedValue.toLowerCase());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLENOTCONTAINS:
                    result = ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? !pageTitle.contains(expectedValue) : !pageTitle.toLowerCase().contains(expectedValue.toLowerCase());
                    break;
                case TestCaseStepActionControl.CONTROL_VERIFYTITLEMATCHREGEX:
                    Pattern pattern;
                    if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
                        pattern = Pattern.compile(expectedValue);
                    } else {
                        pattern = Pattern.compile(expectedValue, Pattern.CASE_INSENSITIVE);
                    }
                    Matcher matcher = pattern.matcher(pageTitle);
                    result = matcher.find();
                    break;
                default:
                    mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                    mes.resolveDescription("CONTROL", control);
                    mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                    return mes;
            }

            if (result) {

                switch (control) {
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEEQUAL:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE_EQUAL);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEDIFFERENT:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE_DIFFERENT);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLECONTAINS:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE_CONTAINS);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLENOTCONTAINS:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE_NOTCONTAINS);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEMATCHREGEX:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TITLE_MATCHREGEX);
                        break;
                    default:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                        mes.resolveDescription("CONTROL", control);
                        mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                        return mes;
                }
            } else {

                switch (control) {
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEEQUAL:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE_EQUAL);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEDIFFERENT:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE_DIFFERENT);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLECONTAINS:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE_CONTAINS);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLENOTCONTAINS:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE_NOTCONTAINS);
                        break;
                    case TestCaseStepActionControl.CONTROL_VERIFYTITLEMATCHREGEX:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TITLE_MATCHREGEX);
                        break;
                    default:
                        mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                        mes.resolveDescription("CONTROL", control);
                        mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
                        return mes;
                }
            }
            mes.resolveDescription("STRING1", pageTitle);
            mes.resolveDescription("STRING2", expectedValue);
            mes.resolveDescription("STRING3", caseSensitiveMessageValue(isCaseSensitive));
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", control);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyXmlTreeStructure(TestCaseExecution tCExecution, String controlProperty, String controlValue) {
        LOG.debug("Control: verifyXmlTreeStructure on: {}", controlProperty);
        MessageEvent mes;
        if (Application.TYPE_SRV.equalsIgnoreCase(tCExecution.getAppTypeEngine())) {

            try {
                if (tCExecution.getLastServiceCalled() != null) {
                    if (AppService.RESPONSEHTTPBODYCONTENTTYPE_XML.equals(tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType())) {
                        String xmlResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
                        if (this.xmlUnitService.isSimilarTree(xmlResponse, controlProperty, controlValue)) {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_SIMILARTREE);
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SIMILARTREE);
                        }
                        mes.resolveDescription("STRING1", StringUtil.sanitize(controlProperty));
                        mes.resolveDescription("STRING2", StringUtil.sanitize(controlValue));
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_MESSAGETYPE);
                        mes.resolveDescription("TYPE", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType());
                        mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYXMLTREESTRUCTURE);
                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOOBJECTINMEMORY);
                }
            } catch (Exception exception) {
                LOG.fatal(exception.toString());
                mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED);
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYXMLTREESTRUCTURE);
            mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
        }
        return mes;
    }

    private MessageEvent verifyElementClickable(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control: verifyElementClickable: {}", html);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {
                try {
                    if (this.webdriverService.isElementClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_CLICKABLE);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE);
                    }
                    mes.resolveDescription("ELEMENT", html);
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_CLICKABLE_NULL);
        }
        return mes;
    }

    private MessageEvent verifyElementNotClickable(TestCaseExecution tCExecution, String html) {
        LOG.debug("Control: verifyElementNotClickable on: {}", html);
        MessageEvent mes;
        if (!StringUtil.isEmptyOrNULLString(html)) {
            Identifier identifier = identifierService.convertStringToIdentifier(html);
            if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)) {
                try {
                    if (this.webdriverService.isElementNotClickable(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTCLICKABLE);
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE);
                    }
                    mes.resolveDescription("ELEMENT", html);
                } catch (WebDriverException exception) {
                    return parseWebDriverException(exception);
                }
            } else {
                mes = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
                mes.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE);
                mes.resolveDescription("APPLICATIONTYPE", tCExecution.getAppTypeEngine());
            }
        } else {
            mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTCLICKABLE_NULL);
        }
        return mes;
    }

    private String caseSensitiveMessageValue(String isCaseSensitive) {
        return (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false))
                ? "(case sensitive)"
                : "(case insensitive)";
    }

    private MessageEvent takeScreenshot(TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution, String cropValues) {
        MessageEvent message;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_FAT)) {
            List<TestCaseExecutionFile> file = recorderService.recordScreenshot(tCExecution, testCaseStepActionExecution, testCaseStepActionControlExecution.getControlId(), cropValues, "Screenshot", "screenshot");
            testCaseStepActionControlExecution.addFileList(file);
            message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TAKESCREENSHOT);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_TAKESCREENSHOT);
        message.resolveDescription("APPLICATIONTYPE", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine());
        return message;
    }

    private MessageEvent getPageSource(TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        MessageEvent message;
        if (tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_IPA)) {
            TestCaseExecutionFile file = recorderService.recordPageSource(tCExecution, testCaseStepActionExecution, testCaseStepActionControlExecution.getControlId());
            if (file != null) {
                List<TestCaseExecutionFile> fileList = new ArrayList<>();
                fileList.add(file);
                testCaseStepActionControlExecution.setFileList(fileList);
            }
            message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_GETPAGESOURCE);
            return message;
        }
        message = new MessageEvent(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.resolveDescription("CONTROL", TestCaseStepActionControl.CONTROL_TAKESCREENSHOT);
        message.resolveDescription("APPLICATIONTYPE", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine());
        return message;
    }

    /**
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     * @author memiks
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.error(exception.toString(), exception);
        mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_SELENIUM_CONNECTIVITY);
        mes.resolveDescription("ERROR", exception.getMessage().split("\n")[0]);
        return mes;
    }
}
