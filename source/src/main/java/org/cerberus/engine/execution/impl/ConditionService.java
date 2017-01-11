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

        LOG.debug("Starting Evaluation condition : " + conditionOper);

        AnswerItem ans = new AnswerItem();
        MessageEvent mes;
        boolean execute_Operation = true;

        /**
         * CONDITION Management is treated here. Checking if the
         * action/control/step/execution can be execued here depending on the
         * condition operator and value.
         */
        switch (conditionOper) {
            case TestCaseStepAction.CONDITIONOPER_ALWAYS:
            case "": // In case condition is not defined, it is considered as always.
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ALWAYS);
                break;

            case TestCaseStepAction.CONDITIONOPER_IFPROPERTYEXIST:
                ans = evaluateCondition_ifPropertyExist(conditionOper, conditionValue1, tCExecution);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICEQUAL:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICDIFFERENT:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATER:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATEROREQUAL:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOR:
            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOROREQUAL:
                ans = evaluateCondition_ifNumericXXX(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGEQUAL:
                ans = evaluateCondition_ifStringEqual(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGDIFFERENT:
                ans = evaluateCondition_ifStringDifferent(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGGREATER:
                ans = evaluateCondition_ifStringGreater(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGMINOR:
                ans = evaluateCondition_ifStringMinor(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_IFSTRINGCONTAINS:
                ans = evaluateCondition_ifStringContains(conditionOper, conditionValue1, conditionValue2);
                mes = ans.getResultMessage();
                break;

            case TestCaseStepAction.CONDITIONOPER_NEVER:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NEVER);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                break;

            default:
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
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

    private AnswerItem<Boolean> evaluateCondition_ifPropertyExist(String conditionOper, String conditionValue1, TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if property Exist");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes;

        if (StringUtil.isNullOrEmpty(conditionValue1)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFPROPERTYEXIST_MISSINGPARAMETER);
            mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));

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
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            } else {
                mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_IFPROPERTYEXIST);
                mes.setDescription(mes.getDescription().replace("%COND%", conditionOper));
                mes.setDescription(mes.getDescription().replace("%PROP%", conditionValue1));
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            }
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringEqual(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Equal");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes;

        if (conditionValue1.equals(conditionValue2)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGEQUAL);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGEQUAL);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        }
        ans.setResultMessage(mes);
        return ans;
    }

    private AnswerItem<Boolean> evaluateCondition_ifStringDifferent(String conditionOper, String conditionValue1, String conditionValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if String Different");
        }
        AnswerItem ans = new AnswerItem();
        MessageEvent mes;

        boolean execute_Action = true;
        if (!(conditionValue1.equals(conditionValue2))) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGDIFFERENT);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGDIFFERENT);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
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
        MessageEvent mes;

        boolean execute_Action = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) > 0)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGGREATER);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGGREATER);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
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
        MessageEvent mes;

        boolean execute_Action = true;
        if ((conditionValue1.compareToIgnoreCase(conditionValue2) < 0)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGMINOR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGMINOR);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
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
        MessageEvent mes;

        boolean execute_Action = true;
        if (conditionValue1.contains(conditionValue2)) {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
        } else {
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_STRINGCONTAINS);
            mes.setDescription(mes.getDescription()
                    .replace("%COND%", conditionOper)
                    .replace("%STR1%", conditionValue1).replace("%STR2%", conditionValue2)
            );
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
            mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_IFNUMERIC_GENERICCONVERSIONERROR);
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
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICEQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICEQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICDIFFERENT:
                if (!(Objects.equals(value1, value2))) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICDIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICDIFFERENT);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATER:
                if (value1 > value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATER);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICGREATEROREQUAL:
                if (value1 >= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICGREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICGREATEROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOR:
                if (value1 < value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOR);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

            case TestCaseStepAction.CONDITIONOPER_IFNUMERICMINOROREQUAL:
                if (value1 <= value2) {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_NUMERICMINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                } else {
                    mes = new MessageEvent(MessageEventEnum.CONDITIONEVAL_FALSE_NUMERICMINOROREQUAL);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", conditionOper)
                            .replace("%STR1%", value1.toString()).replace("%STR2%", value2.toString())
                    );
                }
                break;

        }

        ans.setItem(execute_Action);
        ans.setResultMessage(mes);
        return ans;
    }

}
