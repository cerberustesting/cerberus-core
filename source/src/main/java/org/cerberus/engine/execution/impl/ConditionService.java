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
package org.cerberus.engine.execution.impl;

import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IConditionService;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.gwt.IControlService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.json.IJsonService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author vertigo17
 */

// TEST
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

    /**
     * The associated {@link org.apache.logging.log4j.Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(ConditionService.class);

    @Override
    public AnswerItem<Boolean> evaluateCondition(String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, TestCaseExecution tCExecution) {

        LOG.debug("Starting Evaluation condition : " + conditionOperator);
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;
        boolean execute_Operation = true;

        /**
         * CONDITION Management is treated here. Checking if the
         * action/control/step/execution can be execued here depending on the
         * condition operator and value.
         */
        switch (conditionOperator) {
            case TestCaseStepAction.CONDITIONOPERATOR_ALWAYS:
            case "": // In case condition is not defined, it is considered as always.
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ALWAYS);
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFELEMENTPRESENT:
                ans = evaluateCondition_ifElementPresent(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFELEMENTNOTPRESENT:
                ans = evaluateCondition_ifElementNotPresent(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;
                
            case TestCaseStepAction.CONDITIONOPERATOR_IFELEMENTVISIBLE:
                ans = evaluateCondition_ifElementVisible(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFELEMENTNOTVISIBLE:
                ans = evaluateCondition_ifElementNotVisible(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;
                
            case TestCaseStepAction.CONDITIONOPERATOR_IFPROPERTYEXIST:
                ans = evaluateCondition_ifPropertyExist(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFPROPERTYNOTEXIST:
                ans = evaluateCondition_ifPropertyNotExist(conditionOperator, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICEQUAL:
            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICDIFFERENT:
            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATER:
            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL:
            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOR:
            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOROREQUAL:
                ans = evaluateCondition_ifNumericXXX(conditionOperator, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGEQUAL:
                ans = evaluateCondition_ifStringEqual(conditionOperator, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGDIFFERENT:
                ans = evaluateCondition_ifStringDifferent(conditionOperator, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGGREATER:
                ans = evaluateCondition_ifStringGreater(conditionOperator, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGMINOR:
                ans = evaluateCondition_ifStringMinor(conditionOperator, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGCONTAINS:
                ans = evaluateCondition_ifStringContains(conditionOperator, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFSTRINGNOTCONTAINS:
                ans = evaluateCondition_ifStringNotContains(conditionOperator, conditionValue1, conditionValue2, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_NEVER:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NEVER);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFTEXTINELEMENT:
                ans = evaluateCondition_ifTextInElement(tCExecution, conditionValue1, conditionValue2, conditionOperator, conditionValue3);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFTEXTNOTINELEMENT:
                ans = evaluateCondition_ifTextNotInElement(tCExecution, conditionValue1, conditionValue2, conditionOperator, conditionValue3);
                mes = ans.getResultMessage();
                break;

            default:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        }
        LOG.debug("Finished Evaluation condition : " + mes.getCodeString());

        // the decision whether we execute the action/control/step is taken from the codeString of the message.
        if (mes.getCodeString().equals("OK")) { // If code is OK, we execute the Operation.
            execute_Operation = true;
        } else { // Any other status and we don't execute anything.
            execute_Operation = false;
        }

        ans.setItem(execute_Operation);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifTextInElement(TestCaseExecution tCExecution, String path, String expected, String conditionOperator, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking ifTextInElement on " + path + " element against value: " + expected);

        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent resultControlMes = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        resultControlMes = controlService.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL, tCExecution, path, expected, isCaseSensitive);

        if ("OK".equals(resultControlMes.getCodeString())) {

            MessageEvent resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_TEXTINELEMENT);
            ans.setItem(true);
            ans.setResultMessage(resultCondMes);
            return ans;

        } else {

            MessageEvent resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_TEXTINELEMENT);
            resultCondMes.setDescription(resultCondMes.getDescription().replace("%ERRORMESS%", resultControlMes.getDescription()));
            ans.setItem(false);
            ans.setResultMessage(resultCondMes);
            return ans;

        }

    }

    private AnswerItem<Boolean> evaluateCondition_ifTextNotInElement(TestCaseExecution tCExecution, String path, String expected, String conditionOperator, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking ifTextInElement on " + path + " element against value: " + expected);

        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent resultMes = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        resultMes = controlService.verifyElementXXX(TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT, tCExecution, path, expected, isCaseSensitive);

        if ("OK".equals(resultMes.getCodeString())) {

            MessageEvent resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_TEXTNOTINELEMENT);
            ans.setItem(true);
            ans.setResultMessage(resultCondMes);
            return ans;

        } else {

            MessageEvent resultCondMes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_TEXTNOTINELEMENT);
            resultCondMes.setDescription(resultCondMes.getDescription().replace("%ERRORMESS%", resultMes.getDescription()));
            ans.setItem(false);
            ans.setResultMessage(resultCondMes);
            return ans;

        }

    }

    private AnswerItem<Boolean> evaluateCondition_ifPropertyExist(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if property Exist");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFPROPERTYEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));

        } else {
            String myCountry = tCExecution.getCountry();
            String myProperty = conditionValue1;
            boolean execute_Action = false;
            for (TestCaseCountryProperties prop : tCExecution.getTestCaseCountryPropertyList()) {
                LOG.debug(prop.getCountry() + " - " + myCountry + " - " + prop.getProperty() + " - " + myProperty);
                if ((prop.getCountry().equals(myCountry)) && (prop.getProperty().equals(myProperty))) {
                    execute_Action = true;
                }
            }
            if (execute_Action == false) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFPROPERTYEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFPROPERTYEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            }
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifPropertyNotExist(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if property Does not Exist");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFPROPERTYNOTEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));

        } else {
            String myCountry = tCExecution.getCountry();
            String myProperty = conditionValue1;
            boolean execute_Action = true;
            for (TestCaseCountryProperties prop : tCExecution.getTestCaseCountryPropertyList()) {
                LOG.debug(prop.getCountry() + " - " + myCountry + " - " + prop.getProperty() + " - " + myProperty);
                if ((prop.getCountry().equals(myCountry)) && (prop.getProperty().equals(myProperty))) {
                    execute_Action = false;
                }
            }
            if (execute_Action == false) {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFPROPERTYNOTEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFPROPERTYNOTEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            }
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementPresent(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Element Present");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_NOTPOSSIBLE);
        } else if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTPRESENT_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            boolean condition_result = false;

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {

                Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);

                try {
                    if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        condition_result = true;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    } else {
                        condition_result = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    }
                } catch (WebDriverException exception) {
                    condition_result = false;
                    mes = parseWebDriverException(exception);
                }

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_SRV)) {

                if (tCExecution.getLastServiceCalled() != null) {
                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();

                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {
                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Checking if Element Present - XML");
                            }

                            if (xmlUnitService.isElementPresent(responseBody, conditionValue1)) {
                                condition_result = true;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                            } else {
                                condition_result = false;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                            }
                            break;

                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Checking if Element Present - JSON");
                            }
                            try {
                                if (jsonService.getFromJson(responseBody, null, conditionValue1) != null) {
                                    condition_result = true;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                } else {
                                    condition_result = false;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                }
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                            }
                            break;

                        default:
                            condition_result = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                    }

                } else {
                    condition_result = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOOBJECTINMEMORY);
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            }

        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementNotPresent(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Element is Not Present");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_NOTPOSSIBLE);
        } else if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTNOTPRESENT_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
            boolean condition_result = false;

            Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    if (identifier.getIdentifier().equals("picture")) {
                        mes = sikuliService.doSikuliVerifyElementNotPresent(tCExecution.getSession(), identifier.getLocator());
                        if (mes.equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT))) {
                            condition_result = true;
                        } else {
                            condition_result = false;
                        }

                    } else if (!this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        condition_result = true;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    } else {
                        condition_result = false;
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                        mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                    }
                } catch (WebDriverException exception) {
                    condition_result = false;
                    mes = parseWebDriverException(exception);
                }

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_SRV)) {

                if (tCExecution.getLastServiceCalled() != null) {
                    String responseBody = tCExecution.getLastServiceCalled().getResponseHTTPBody();

                    switch (tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()) {

                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                            if (!xmlUnitService.isElementPresent(responseBody, conditionValue1)) {
                                condition_result = true;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                            } else {
                                condition_result = false;
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                                mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                            }
                            break;

                        case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                            try {
                                if (jsonService.getFromJson(responseBody, null, conditionValue1) == null) {
                                    condition_result = true;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                } else {
                                    condition_result = false;
                                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTPRESENT);
                                    mes.setDescription(mes.getDescription().replace("%ELEMENT%", conditionValue1));
                                }
                            } catch (Exception ex) {
                                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_GENERIC);
                                mes.setDescription(mes.getDescription().replace("%ERROR%", ex.toString()));
                            }
                            break;

                        default:
                            condition_result = false;
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_MESSAGETYPE);
                            mes.setDescription(mes.getDescription().replace("%TYPE%", tCExecution.getLastServiceCalled().getResponseHTTPBodyContentType()));
                            mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                    }

                } else {
                    condition_result = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOOBJECTINMEMORY);
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_NOTSUPPORTED_FOR_APPLICATION);
                mes.setDescription(mes.getDescription().replace("%CONDITION%", conditionOperator));
                mes.setDescription(mes.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            }

        }
        ans.setResultMessage(mes);
        return ans;
    }
    
    private AnswerItem<Boolean> evaluateCondition_ifElementVisible(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Element Visible");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_NOTPOSSIBLE);
        } else if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTVISIBLE_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
        	if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
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
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifElementNotVisible(String conditionOperator, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Element is Not Visible");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        if (tCExecution.getManualExecution().equals("Y")) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_NOTPOSSIBLE);
        } else if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFELEMENTNOTVISIBLE_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOperator));
        } else {
        	if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {

                try {
                    Identifier identifier = identifierService.convertStringToIdentifier(conditionValue1);
                    if (this.webdriverService.isElementPresent(tCExecution.getSession(), identifier)) {
                        if (this.webdriverService.isElementNotVisible(tCExecution.getSession(), identifier)) {
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFELEMENTNOTVISIBLE);
                            mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));
                            
                        } else {
                            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTVISIBLE);
                            mes.setDescription(mes.getDescription().replace("%STRING1%", conditionValue1));
                            
                        }
                    } else {
                        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_IFELEMENTNOTVISIBLE);
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

        }
        ans.setResultMessage(mes);
        return ans;
    }
   
    private AnswerItem<Boolean> evaluateCondition_ifStringEqual(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Equal");
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Different");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean execute_Action = true;
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
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringGreater(String conditionOperator, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Greater");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        boolean execute_Action = true;
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
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringMinor(String conditionOperator, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Minor");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        boolean execute_Action = true;
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
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringContains(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Contains");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean execute_Action = true;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.contains(conditionValue2) : conditionValue1.toLowerCase().contains(conditionValue2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            execute_Action = true;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            execute_Action = false;
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringNotContains(String conditionOperator, String conditionValue1, String conditionValue2, String isCaseSensitive) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Does Not Contains");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes;

        isCaseSensitive = defaultIsSensitiveValue(isCaseSensitive);

        boolean execute_Action = true;
        if (ParameterParserUtil.parseBooleanParam(isCaseSensitive, false) ? conditionValue1.contains(conditionValue2) : conditionValue1.toLowerCase().contains(conditionValue2.toLowerCase())) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            execute_Action = true;
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOperator)
                    .replace("%STR1%", conditionValue1)
                    .replace("%STR2%", conditionValue2)
                    .replace("%STRING3%", caseSensitiveMessageValue(isCaseSensitive))
            );
//            execute_Action = false;
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifNumericXXX(String conditionOperator, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Numeric Equals");
        }
        AnswerItem<Boolean> ans = new AnswerItem<>();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_PENDING);

        // We first prepare the string for nueric conversion to replace , by .
        String newConditionValue1 = StringUtil.prepareToNumeric(conditionValue1);
        String newConditionValue2 = StringUtil.prepareToNumeric(conditionValue2);

        // We try to convert the strings value1 to numeric.
        Double value1 = 0.0;
        try {
            value1 = Double.valueOf(newConditionValue1);
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
        Double value2 = 0.0;
        try {
            value2 = Double.valueOf(newConditionValue2);
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
        boolean execute_Action = true;
        switch (conditionOperator) {

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICEQUAL:
                if (Objects.equals(value1, value2)) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICEQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICEQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICDIFFERENT:
                if (!(Objects.equals(value1, value2))) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICDIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICDIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATER:
                if (value1 > value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL:
                if (value1 >= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOR:
                if (value1 < value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPERATOR_IFNUMERICMINOROREQUAL:
                if (value1 <= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOperator)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

        }

        ans.setItem(execute_Action);
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
     * @author memiks
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.fatal(exception.toString());
        mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

    private String defaultIsSensitiveValue(String isCaseSensitive) {
        if (StringUtil.isNullOrEmpty(isCaseSensitive)) {
            isCaseSensitive = "N";
        }
        return isCaseSensitive;
    }

}
