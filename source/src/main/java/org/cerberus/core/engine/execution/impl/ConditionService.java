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
package org.cerberus.core.engine.execution.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IConditionService;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.engine.execution.IRobotServerService;
import org.cerberus.core.engine.execution.enums.ConditionOperatorEnum;
import org.cerberus.core.engine.gwt.IControlService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.json.IJsonService;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.service.webdriver.IWebDriverService;
import org.cerberus.core.service.xmlunit.IXmlUnitService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONArray;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author vertigo17
 */
@Service
public class ConditionService implements IConditionService {

    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private IJsonService jsonService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private IControlService controlService;
    @Autowired
    private IRobotServerService robotServerService;

    // The associated {@link org.apache.logging.log4j.Logger} to this class
    private static final Logger LOG = LogManager.getLogger(ConditionService.class);

    @Override
    public AnswerItem<Boolean> evaluateCondition(String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, TestCaseExecution execution, JSONArray options) {

        LOG.debug("Starting Evaluation condition : {}", conditionOperator);

        ConditionOperatorEnum conditionToEvaluate = ConditionOperatorEnum.getConditionOperatorEnumFromString(conditionOperator);

        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;
        boolean isOperationToBeExecuted = true;

        // Define Options
        HashMap<String, String> optionsMap = robotServerService.getMapFromOptions(options);
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TIMEOUT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX).isEmpty()) {
            Integer newTimeout = Integer.valueOf(optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
            robotServerService.setOptionsTimeout(execution.getSession(), newTimeout);
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX).isEmpty()) {
            Integer newHighlightElement = Integer.valueOf(optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX));
            robotServerService.setOptionsHighlightElement(execution.getSession(), newHighlightElement);
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX).isEmpty()) {
            String minSimilarity = optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX);
            robotServerService.setOptionsMinSimilarity(execution.getSession(), minSimilarity);
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX).isEmpty()) {
            String typeDelay = optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX);
            robotServerService.setOptionsTypeDelay(execution.getSession(), typeDelay);
        }

        /*
         * CONDITION Management is treated here. Checking if the
         * action/control/step/execution can be execued here depending on the
         * condition operator and value.
         */
        switch (Objects.requireNonNull(conditionToEvaluate)) {
            case CONDITIONOPERATOR_ALWAYS:
            case CONDITIONOPERATOR_UNDEFINED: // In case condition is not defined, it is considered as always.
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ALWAYS);
                break;

            case CONDITIONOPERATOR_IFELEMENTPRESENT:
                ans = evaluateCondition_ifElementPresent(conditionToEvaluate.getCondition(), conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFELEMENTNOTPRESENT:
                ans = evaluateCondition_ifElementNotPresent(conditionToEvaluate.getCondition(), conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFELEMENTVISIBLE:
                ans = evaluateCondition_ifElementVisible(conditionToEvaluate.getCondition(), conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFELEMENTNOTVISIBLE:
                ans = evaluateCondition_ifElementNotVisible(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFPROPERTYEXIST:
                ans = evaluateCondition_ifPropertyExist(conditionToEvaluate.getCondition(), conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFPROPERTYNOTEXIST:
                ans = evaluateCondition_ifPropertyNotExist(conditionToEvaluate.getCondition(), conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFNUMERICEQUAL:
            case CONDITIONOPERATOR_IFNUMERICDIFFERENT:
            case CONDITIONOPERATOR_IFNUMERICGREATER:
            case CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL:
            case CONDITIONOPERATOR_IFNUMERICMINOR:
            case CONDITIONOPERATOR_IFNUMERICMINOROREQUAL:
                ans = evaluateCondition_ifNumericXXX(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGEQUAL:
                ans = evaluateCondition_ifStringEqual(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGDIFFERENT:
                ans = evaluateCondition_ifStringDifferent(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGGREATER:
                ans = evaluateCondition_ifStringGreater(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGMINOR:
                ans = evaluateCondition_ifStringMinor(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGCONTAINS:
                ans = evaluateCondition_ifStringContains(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTRINGNOTCONTAINS:
                ans = evaluateCondition_ifStringNotContains(conditionToEvaluate.getCondition(), conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_NEVER:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NEVER);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionToEvaluate.getCondition()));
                break;

            case CONDITIONOPERATOR_IFTEXTINELEMENT:
                ans = evaluateCondition_ifTextInElement(execution, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFTEXTNOTINELEMENT:
                ans = evaluateCondition_ifTextNotInElement(execution, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTEPSTATUSOK:
                ans = evaluateCondition_ifStepStatusOK(conditionValue1, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFSTEPSTATUSNE:
                ans = evaluateCondition_ifStepStatusNE(conditionValue1, execution);
                mes = ans.getResultMessage();
                break;
            case CONDITIONOPERATOR_IFACTIONSTATUSOK:
                ans = evaluateCondition_ifActionStatusOK(conditionValue1, conditionValue2, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFACTIONSTATUSNE:
                ans = evaluateCondition_ifActionStatusNE(conditionValue1, conditionValue2, execution);
                mes = ans.getResultMessage();
                break;
            case CONDITIONOPERATOR_IFCONTROLSTATUSOK:
                ans = evaluateCondition_ifControlStatusOK(conditionValue1, conditionValue2, conditionValue3, execution);
                mes = ans.getResultMessage();
                break;

            case CONDITIONOPERATOR_IFCONTROLSTATUSNE:
                ans = evaluateCondition_ifControlStatusNE(conditionValue1, conditionValue2, conditionValue3, execution);
                mes = ans.getResultMessage();
                break;

            default:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionToEvaluate.getCondition()));
        }
        LOG.debug("Finished Evaluation condition : {}", mes.getCodeString());

        // Reset Timeout to default
        robotServerService.setOptionsToDefault(execution.getSession());

        // the decision whether we execute the action/control/step is taken from the codeString of the message.
        isOperationToBeExecuted = mes.getCodeString().equals("OK");

        ans.setItem(isOperationToBeExecuted);
        ans.setResultMessage(mes);

        return ans;
    }

    @Override
    public String cleanValue1(String condition, String value1) {

        ConditionOperatorEnum conditionToEvaluate = ConditionOperatorEnum.getConditionOperatorEnumFromString(condition);

        switch (Objects.requireNonNull(conditionToEvaluate)) {
            case CONDITIONOPERATOR_ALWAYS:
            case CONDITIONOPERATOR_UNDEFINED: // In case condition is not defined, it is considered as always.
            case CONDITIONOPERATOR_NEVER:
                return "";
            default:
                return value1;
        }
    }

    @Override
    public String cleanValue2(String condition, String value2) {
        ConditionOperatorEnum conditionToEvaluate = ConditionOperatorEnum.getConditionOperatorEnumFromString(condition);

        switch (Objects.requireNonNull(conditionToEvaluate)) {
            case CONDITIONOPERATOR_ALWAYS:
            case CONDITIONOPERATOR_UNDEFINED: // In case condition is not defined, it is considered as always.
            case CONDITIONOPERATOR_NEVER:
            case CONDITIONOPERATOR_IFPROPERTYEXIST:
            case CONDITIONOPERATOR_IFPROPERTYNOTEXIST:
            case CONDITIONOPERATOR_IFELEMENTPRESENT:
            case CONDITIONOPERATOR_IFELEMENTNOTPRESENT:
            case CONDITIONOPERATOR_IFELEMENTVISIBLE:
                return "";
            default:
                return value2;
        }
    }

    @Override
    public String cleanValue3(String condition, String value3) {
        ConditionOperatorEnum conditionToEvaluate = ConditionOperatorEnum.getConditionOperatorEnumFromString(condition);

        switch (Objects.requireNonNull(conditionToEvaluate)) {
            case CONDITIONOPERATOR_IFSTRINGEQUAL:
            case CONDITIONOPERATOR_IFSTRINGDIFFERENT:
            case CONDITIONOPERATOR_IFSTRINGCONTAINS:
            case CONDITIONOPERATOR_IFSTRINGNOTCONTAINS:
            case CONDITIONOPERATOR_IFTEXTINELEMENT:
            case CONDITIONOPERATOR_IFTEXTNOTINELEMENT:
            case CONDITIONOPERATOR_IFCONTROLSTATUSOK:
            case CONDITIONOPERATOR_IFCONTROLSTATUSNE:
                return value3;
            default:
                return "";
        }
    }

    private AnswerItem<Boolean> evaluateCondition_ifTextInElement(TestCaseExecution tCExecution, String path, String expected, String isCaseSensitive) {
        LOG.debug("Checking ifTextInElement on {} element against value: {}", path, expected);

        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent resultControlMes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        if (tCExecution.getManualExecution().equals("Y")) {
            resultControlMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_TEXTINELEMENT);
            resultControlMes.resolveDescription("STRING1", path);
            resultControlMes.resolveDescription("STRING2", expected);
            resultControlMes.resolveDescription("STRING3", isCaseSensitive);
        } else {

            resultControlMes = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);

            // TODO AJOUTER DANS CONDITION OPERATOR ENUM = TRUE + nouveau message
            resultControlMes = controlService.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL, tCExecution, path, expected, isCaseSensitive);

            MessageEvent resultCondMes;
            if ("OK".equals(resultControlMes.getCodeString())) {
                resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_TEXTINELEMENT);
                ans.setItem(true);
            } else {
                resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_TEXTINELEMENT);
                resultCondMes.setDescription(resultCondMes.getDescription().replace("%ERRORMESS%", resultControlMes.getDescription()));
                ans.setItem(false);
            }
            ans.setResultMessage(resultCondMes);
            return ans;
        }
        ans.setResultMessage(resultControlMes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifTextNotInElement(TestCaseExecution tCExecution, String path, String expected, String isCaseSensitive) {
        LOG.debug("Checking ifTextInElement on {} element against value: {}", path, expected);

        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent resultMes;
        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        if (tCExecution.getManualExecution().equals("Y")) {
            resultMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_TEXTNOTINELEMENT);
            resultMes.resolveDescription("STRING1", path);
            resultMes.resolveDescription("STRING2", expected);
            resultMes.resolveDescription("STRING3", isCaseSensitive);
        } else {
            resultMes = controlService.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT, tCExecution, path, expected, isCaseSensitive);
            MessageEvent resultCondMes;
            if ("OK".equals(resultMes.getCodeString())) {
                resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_TEXTNOTINELEMENT);
                ans.setItem(true);
            } else {
                resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_TEXTNOTINELEMENT);
                resultCondMes.setDescription(resultCondMes.getDescription().replace("%ERRORMESS%", resultMes.getDescription()));
                ans.setItem(false);
            }
            ans.setResultMessage(resultCondMes);
            return ans;
        }

        ans.setResultMessage(resultMes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifPropertyExist(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        LOG.debug("Checking if property Exist");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFPROPERTYEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            String myCountry = tCExecution.getCountry();
            boolean doExecuteAction = false;
            for (TestCaseCountryProperties prop : tCExecution.getTestCaseCountryPropertyList()) {
                LOG.debug("{} - {} - {} - {}", prop.getCountry(), myCountry, prop.getProperty(), conditionValue1);
                if ((prop.getCountry().equals(myCountry)) && (prop.getProperty().equals(conditionValue1))) {
                    doExecuteAction = true;
                }
            }
            if (!doExecuteAction) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFPROPERTYEXIST);
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFPROPERTYEXIST);
            }
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
            mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifPropertyNotExist(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        LOG.debug("Checking if property Does not Exist");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFPROPERTYNOTEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            String myCountry = tCExecution.getCountry();
            boolean doExecuteAction = true;
            for (TestCaseCountryProperties prop : tCExecution.getTestCaseCountryPropertyList()) {
                LOG.debug("{} - {} - {} - {}", prop.getCountry(), myCountry, prop.getProperty(), conditionValue1);
                if ((prop.getCountry().equals(myCountry)) && (prop.getProperty().equals(conditionValue1))) {
                    doExecuteAction = false;
                }
            }
            if (!doExecuteAction) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFPROPERTYNOTEXIST);
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFPROPERTYNOTEXIST);
            }
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
            mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementPresent(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        LOG.debug("Checking if Element Present");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_IFELEMENTPRESENT);
            mes.resolveDescription("ELEMENT", conditionValue1);
        } else if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTPRESENT_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            boolean conditionResult = false;

            Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);

            switch (tCExecution.getApplicationObj().getType()) {
                case Application.TYPE_GUI:
                case Application.TYPE_APK:
                case Application.TYPE_IPA:
                    try {

                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), null);
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), null, identifier.getLocator());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        conditionResult = true;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    }
                } catch (WebDriverException exception) {
                    conditionResult = false;
                    mes = parseWebDriverException(exception);
                }
                break;

                case Application.TYPE_FAT:

                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), null);
                        LOG.debug("Sikuli : {}", mes.getCode());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), null, identifier.getLocator());
                        LOG.debug("Sikuli : {}", mes.getCode());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                        mes.resolveDescription("ERROR", "Element must start by picture=");
                    }
                    break;

                case Application.TYPE_SRV:

                    if (tCExecution.getLastServiceCalled() != null) {
                        String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();

                        switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Checking if Element Present - XML");
                                }

                                if (xmlUnitService.isElementPresent(responseBody, conditionValue1)) {
                                    conditionResult = true;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                } else {
                                    conditionResult = false;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                }
                                break;

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Checking if Element Present - JSON");
                                }
                                try {
                                    if (jsonService.getFromJson(tCExecution, responseBody, null, conditionValue1, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST) != null) {
                                        conditionResult = true;
                                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                    } else {
                                        conditionResult = false;
                                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                    }
                                } catch (Exception ex) {
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                                    mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                                }
                                break;

                            default:
                                conditionResult = false;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
                                mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                                mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                        }
                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOOBJECTINMEMORY);
                    }

                    break;
                default:
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_APPLICATION);
                    mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                    mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            }

        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementNotPresent(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        LOG.debug("Checking if Element is Not Present");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_IFELEMENTNOTPRESENT);
            mes.resolveDescription("ELEMENT", conditionValue1);
        } else if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTNOTPRESENT_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            boolean conditionResult = false;

            Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);

            switch (tCExecution.getApplicationObj().getType()) {
                case Application.TYPE_GUI:
                case Application.TYPE_APK:
                case Application.TYPE_IPA:
                    try {
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), null);
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), null, identifier.getLocator());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (!this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        conditionResult = true;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    }
                } catch (WebDriverException exception) {
                    conditionResult = false;
                    mes = parseWebDriverException(exception);
                }
                break;

                case Application.TYPE_FAT:
                    if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_PICTURE)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), identifier.getLocator(), null);
                        LOG.debug("Sikuli : {}", mes.getCode());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else if (identifier.getIdentifier().equals(Identifier.IDENTIFIER_TEXT)) {
                        mes = sikuliService.doSikuliVerifyElementPresent(tCExecution.getSession(), null, identifier.getLocator());
                        LOG.debug("Sikuli : {}", mes.getCode());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT))) {
                            conditionResult = true;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        } else if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT))) {
                            conditionResult = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                            mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                        }

                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                        mes.resolveDescription("ERROR", "Element must start by picture=");
                    }
                    break;

                case Application.TYPE_SRV:

                    if (tCExecution.getLastServiceCalled() != null) {
                        String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();

                        switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                                if (!xmlUnitService.isElementPresent(responseBody, conditionValue1)) {
                                    conditionResult = true;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                } else {
                                    conditionResult = false;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                }
                                break;

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                                try {
                                if (jsonService.getFromJson(tCExecution, responseBody, null, conditionValue1, false, 0, TestCaseCountryProperties.VALUE3_VALUELIST) == null) {
                                    conditionResult = true;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                } else {
                                    conditionResult = false;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                }
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                            }
                            break;

                            default:
                                conditionResult = false;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
                                mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                                mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                        }

                    } else {
                        conditionResult = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOOBJECTINMEMORY);
                    }
                    break;
                default:
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_APPLICATION);
                    mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                    mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            }

        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementVisible(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        LOG.debug("Checking if Element Visible");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_IFELEMENTVISIBLE);
            mes.resolveDescription("ELEMENT", conditionValue1);
        } else if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTVISIBLE_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {

            try {
                Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);
                if (this.webdriverService.isElementVisible(tCExecution.getSession(), identifier)) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTVISIBLE);
                    mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));

                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTVISIBLE);
                    mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));

                }
            } catch (WebDriverException exception) {
                mes = parseWebDriverException(exception);
            }

        } else {

            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementVisible"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));

        }

        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementNotVisible(String conditionOperator, String conditionValue1, String conditionValue2, TestCaseExecution tCExecution) {
        LOG.debug("Checking if Element is Not Visible");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUEMANUAL_IFELEMENTNOTVISIBLE);
            mes.resolveDescription("ELEMENT", conditionValue1);
        } else if (StringUtil.isEmptyOrNull(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTNOTVISIBLE_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {

            try {
                Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);
                boolean elementMustBePresent = ParameterParserUtil.parseBooleanParam(conditionValue2, true);

                if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)==elementMustBePresent) {
                    if (this.webdriverService.isElementNotVisible(tCExecution.getSession(), identifier)) {
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTVISIBLE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));

                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTVISIBLE);
                        mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));

                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTVISIBLEELEMENTPRESENT);
                    mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));

                }
            } catch (WebDriverException exception) {
                mes = parseWebDriverException(exception);
            }

        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
            mes.setDescription(mes.getDescription().replace("%CONTROL%", "verifyElementNotVisible"));
            mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));

        }

        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringEqual(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        LOG.debug("Checking if String Equal");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.equals(conditionValue2) : conditionValue1.equalsIgnoreCase(conditionValue2)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGEQUAL);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGEQUAL);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringDifferent(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        LOG.debug("Checking if String Different");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean doExecuteAction = true;
        if (!(ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.equals(conditionValue2) : conditionValue1.equalsIgnoreCase(conditionValue2))) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGDIFFERENT);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGDIFFERENT);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
        }
        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringGreater(String conditionOperator, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Greater");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        boolean doExecuteAction = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) > 0)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGGREATER);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGGREATER);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        }
        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringMinor(String conditionOperator, String conditionValue1, String conditionValue2) {
        LOG.debug("Checking if String Minor");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        boolean doExecuteAction = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) < 0)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGMINOR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGMINOR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        }
        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringContains(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        LOG.debug("Checking if String Contains");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean doExecuteAction = true;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.contains(conditionValue2) : conditionValue1.toLowerCase().contains(conditionValue2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            doExecuteAction = true;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            doExecuteAction = false;
        }
        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringNotContains(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        LOG.debug("Checking if String Does Not Contains");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean doExecuteAction = true;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.contains(conditionValue2) : conditionValue1.toLowerCase().contains(conditionValue2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            doExecuteAction = true;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            doExecuteAction = false;
        }
        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifNumericXXX(String conditionOperator, String conditionValue1, String conditionValue2) {
        LOG.debug("Checking if Numeric Equals");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_PENDING);

        // We first prepare the string for numeric conversion to replace , by .
        String newConditionValue1 = StringUtil.prepareToNumeric(conditionValue1);
        String newConditionValue2 = StringUtil.prepareToNumeric(conditionValue2);

        // We try to convert the strings value1 to numeric.
        double value1;
        try {
            value1 = Double.parseDouble(newConditionValue1);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFNUMERIC_GENERICCONVERSIONERROR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STRINGVALUE%", newConditionValue1));
            ans.setItem(false);
            ans.setResultMessage(mes);
            return ans;
        }

        // We try to convert the strings value2 to numeric.
        double value2;
        try {
            value2 = Double.parseDouble(newConditionValue2);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFNUMERIC_GENERICCONVERSIONERROR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STRINGVALUE%", newConditionValue2));
            ans.setItem(false);
            ans.setResultMessage(mes);
            return ans;
        }

        // Now that both values are converted to double we ceck the operator here.
        boolean doExecuteAction = true;
        switch (conditionOperator) {

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICEQUAL:
                mes = Objects.equals(value1, value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICEQUAL)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICEQUAL);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICDIFFERENT:
                mes = Objects.equals(value1, value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICDIFFERENT)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICDIFFERENT);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATER:
                mes = (value1 > value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATER)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATER);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL:
                mes = (value1 >= value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATEROREQUAL)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATEROREQUAL);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOR:
                mes = (value1 < value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOR)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOR);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOROREQUAL:
                mes = (value1 <= value2)
                        ? new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOROREQUAL)
                        : new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOROREQUAL);
                break;
        }
        mes.setDescription(mes.getDescription()
                .replace("%COND%", conditionOperator)
                .replace("%STR1%", Double.toString(value1)).replace("%STR2%", Double.toString(value2))
        );

        ans.setItem(doExecuteAction);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStepStatusOK(String conditionValue1, TestCaseExecution execution) {
        LOG.debug("Checking if Step Status OK");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {

            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1)).getReturnCode();


            if (status.equals("OK")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STEPEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STEPEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                );
            }

        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_STEPEXECUTIONOK);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStepStatusNE(String conditionValue1, TestCaseExecution execution) {
        LOG.debug("Checking if Step Status NE");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {
            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1)).getReturnCode();


            if (status.equals("NE")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STEPEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STEPEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                );
            }
        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_STEPEXECUTIONNE);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifActionStatusOK(String conditionValue1, String conditionValue2, TestCaseExecution execution) {
        LOG.debug("Checking if Action Status OK");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {
            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1))
                    .getTestCaseStepActionExecutionByActionId(Integer.valueOf(conditionValue2))
                    .getReturnCode();


            if (status.equals("OK")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ACTIONEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_ACTIONEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                );
            }
        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_ACTIONEXECUTIONOK);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifActionStatusNE(String conditionValue1, String conditionValue2, TestCaseExecution execution) {
        LOG.debug("Checking if Action Status NE");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {
            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1))
                    .getTestCaseStepActionExecutionByActionId(Integer.valueOf(conditionValue2))
                    .getReturnCode();


            if (status.equals("NE")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ACTIONEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_ACTIONEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                );
            }
        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_ACTIONEXECUTIONNE);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifControlStatusOK(String conditionValue1, String conditionValue2, String conditionValue3, TestCaseExecution execution) {
        LOG.debug("Checking if Control Status OK");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {
            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1))
                    .getTestCaseStepActionExecutionByActionId(Integer.valueOf(conditionValue2))
                    .getTestCaseStepActionControlExecutionByControlId(Integer.valueOf(conditionValue3))
                    .getReturnCode();

            if (status.equals("OK")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_CONTROLEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                        .replace("%STR3%", conditionValue3)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_CONTROLEXECUTIONOK);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                        .replace("%STR3%", conditionValue3)
                );
            }
        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_CONTROLEXECUTIONOK);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STR3%", conditionValue3)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifControlStatusNE(String conditionValue1, String conditionValue2, String conditionValue3, TestCaseExecution execution) {
        LOG.debug("Checking if Control Status NE");
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        try {
            String status = execution.getTestCaseStepExecutionByStepId(Integer.valueOf(conditionValue1))
                    .getTestCaseStepActionExecutionByActionId(Integer.valueOf(conditionValue2))
                    .getTestCaseStepActionControlExecutionByControlId(Integer.valueOf(conditionValue3))
                    .getReturnCode();


            if (status.equals("NE")) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_CONTROLEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                        .replace("%STR3%", conditionValue3)
                );
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_CONTROLEXECUTIONNE);
                mes.setDescription(mes.getDescription()
                        .replace("%STR1%", conditionValue1)
                        .replace("%STR2%", conditionValue2)
                        .replace("%STR3%", conditionValue3)
                );
            }
        } catch (Exception ex){
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_CONTROLEXECUTIONNE);
            mes.setDescription(mes.getDescription()
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STR3%", conditionValue3)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private String caseSensitiveMessageValue(String isCaseSensitive) {

        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false)) {
            return "case sensitive";
        } else {
            return "case insensitive";
        }
    }

    /**
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     * @author memiks
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.fatal(exception::toString);
        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

    private String defaultIsSensitiveValue(String isCaseSensitive) {
        if (StringUtil.isEmptyOrNull(isCaseSensitive)) {
            isCaseSensitive = "N";
        }
        return isCaseSensitive;
    }
}
