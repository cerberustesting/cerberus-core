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
package org.cerberus.engine.execution.impl;

import java.util.Objects;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IConditionService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author vertigo17
 */
@Service
public class ConditionService implements IConditionService {

    /**
     * The associated {@link org.apache.log4j.Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(ConditionService.class);

    @Override
    public AnswerItem<Boolean> evaluateCondition(String conditionOper, String conditionValue1, String conditionValue2, TestCaseExecution tCExecution) {

        AnswerItem ans = new AnswerItem();
        /**
         * CONDITION Management is treated here. Checking if the
         * action/control/step/execution can be execued here depending on the
         * condition operator and value.
         */
        boolean execute_Action = true;
        LOG.debug("Starting Evaluation condition : " + conditionOper);
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);
        switch (conditionOper) {
            case TestCaseStepAction.CONDITIONOPER_ALWAYS:
            case "": // In case condition is not defined, it is considered as always.
                mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);
                execute_Action = true;
                break;

            case TestCaseStepAction.CONDITIONOPER_IFPROPERTYEXIST:
                ans = evaluateCondition_ifPropertyExist(conditionOper, conditionValue1, tCExecution);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICEQUAL:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICDIFFERENT:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATER:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATEROREQUAL:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOR:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOROREQUAL:
                ans = evaluateCondition_ifNumericXXX(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGEQUAL:
                ans = evaluateCondition_ifStringEqual(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGDIFFERENT:
                ans = evaluateCondition_ifStringDifferent(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGGREATER:
                ans = evaluateCondition_ifStringGreater(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGMINOR:
                ans = evaluateCondition_ifStringMinor(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGCONTAINS:
                ans = evaluateCondition_ifStringContains(conditionOper, conditionValue1, conditionValue2);
                execute_Action = (Boolean) ans.getItem();
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_NEVER:
                mes = new MessageEvent(MessageEventEnum.CONDITION_NEVER);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                execute_Action = false;
                break;

            default:
                mes = new MessageEvent(MessageEventEnum.CONDITION_UNKNOWN);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                execute_Action = false;
        }
        LOG.debug("Finished Evaluation condition : " + execute_Action);

        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifPropertyExist(String conditionOper, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if property Exist");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITION_IFPROPERTYEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
            execute_Action = false;

        } else {
            String myCountry = tCExecution.getCountry();
            String myProperty = conditionValue1;
            execute_Action = false;
            for (TestCaseCountryProperties prop : tCExecution.getTestCaseCountryPropertyList()) {
                LOG.debug(prop.getCountry() + " - " + myCountry + " - " + prop.getProperty() + " - " + myProperty);
                if ((prop.getCountry().equals(myCountry)) && (prop.getProperty().equals(myProperty))) {
                    execute_Action = true;
                }
            }
            if (execute_Action == false) {
                mes = new MessageEvent(MessageEventEnum.CONDITION_IFPROPERTYEXIST_NOTEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            }
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringEqual(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Equal");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if (conditionValue1.equals(conditionValue2)) {
            execute_Action = true;
        } else {
            execute_Action = false;
            mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%MESSAGE%", "String '" + conditionValue1 + "' is not equal to '" + conditionValue2 + "'"));
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringDifferent(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Different");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if (!(conditionValue1.equals(conditionValue2))) {
            execute_Action = true;
        } else {
            execute_Action = false;
            mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%MESSAGE%", "String '" + conditionValue1 + "' is not different to '" + conditionValue2 + "'"));
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringGreater(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Greater");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) > 0)) {
            execute_Action = true;
        } else {
            execute_Action = false;
            mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%MESSAGE%", "String '" + conditionValue1 + "' is not greater than '" + conditionValue2 + "'"));
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringMinor(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Minor");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) < 0)) {
            execute_Action = true;
        } else {
            execute_Action = false;
            mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%MESSAGE%", "String '" + conditionValue1 + "' is not minor to '" + conditionValue2 + "'"));
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringContains(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Contains");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        boolean execute_Action = true;
        if (conditionValue1.indexOf(conditionValue2) >= 0) {
            execute_Action = true;
        } else {
            execute_Action = false;
            mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%MESSAGE%", "String '" + conditionValue1 + "' does not contain '" + conditionValue2 + "'"));
        }
        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifNumericXXX(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if Numeric Equals");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes = new MessageEvent(MessageEventEnum.CONDITION_PENDING);

        // We first prepare the string for nueric conversion to replace , by .
        String newConditionValue1 = StringUtil.prepareToNumeric(conditionValue1);
        String newConditionValue2 = StringUtil.prepareToNumeric(conditionValue2);
        
        // We try to convert the strings value1 to numeric.
        Double value1 = 0.0;
        try {
            value1 = Double.valueOf(newConditionValue1);
        } catch (NumberFormatException nfe) {
            mes = new MessageEvent(MessageEventEnum.CONDITION_IFNUMERIC_GENERICCONVERSIONERROR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
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
            mes = new MessageEvent(MessageEventEnum.CONDITION_IFNUMERIC_GENERICCONVERSIONERROR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STRINGVALUE%", newConditionValue2));
            ans.setItem(false);
            ans.setResultMessage(mes);
            return ans;
        }

        // Now that both values are converted to double we ceck the operator here.
        boolean execute_Action = true;
        switch (conditionOper) {

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICEQUAL:
                if (Objects.equals(value1, value2)) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not equal to '" + conditionValue2 + "'"));
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICDIFFERENT:
                if (!(Objects.equals(value1, value2))) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not different to '" + conditionValue2 + "'"));
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATER:
                if (value1 > value2) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not greater than '" + conditionValue2 + "'"));
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATEROREQUAL:
                if (value1 >= value2) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not greater or equal than '" + conditionValue2 + "'"));
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOR:
                if (value1 < value2) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not lower than '" + conditionValue2 + "'"));
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOROREQUAL:
                if (value1 <= value2) {
                    execute_Action = true;
                } else {
                    execute_Action = false;
                    mes = new MessageEvent(MessageEventEnum.CONDITION_GENERIC_NOTEXECUTED);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%MESSAGE%", "Numeric '" + conditionValue1 + "' is not lower or equal than '" + conditionValue2 + "'"));
                }
                break;

        }

        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

}
