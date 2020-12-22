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
package org.cerberus.engine.execution.enums;

/**
 *
 * @author mlombard
 */


public enum ConditionOperatorEnum {
    
    CONDITIONOPERATOR_ALWAYS("always", false),
    CONDITIONOPERATOR_UNDEFINED("", false),
    CONDITIONOPERATOR_IFELEMENTPRESENT("ifElementPresent", false),
    CONDITIONOPERATOR_IFELEMENTNOTPRESENT("ifElementNotPresent", false),
    CONDITIONOPERATOR_IFELEMENTVISIBLE("ifElementVisible", true),
    CONDITIONOPERATOR_IFELEMENTNOTVISIBLE("ifElementNotVisible", false),
    CONDITIONOPERATOR_IFPROPERTYEXIST("ifPropertyExist", false),
    CONDITIONOPERATOR_IFPROPERTYNOTEXIST("ifPropertyNotExist", false),
    CONDITIONOPERATOR_IFNUMERICEQUAL("ifNumericEqual", false),
    CONDITIONOPERATOR_IFNUMERICDIFFERENT("ifNumericDifferent", false),
    CONDITIONOPERATOR_IFNUMERICGREATER("ifNumericGreater", false),
    CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL("ifNumericGreaterOrEqual", false),
    CONDITIONOPERATOR_IFNUMERICMINOR("ifNumericMinor", false),
    CONDITIONOPERATOR_IFNUMERICMINOROREQUAL("ifNumericMinorOrEqual", false),
    CONDITIONOPERATOR_IFSTRINGEQUAL("ifStringEqual", false),
    CONDITIONOPERATOR_IFSTRINGDIFFERENT("ifStringDifferent", false),
    CONDITIONOPERATOR_IFSTRINGGREATER("ifStringGreater", false),
    CONDITIONOPERATOR_IFSTRINGMINOR("ifStringMinor", false),
    CONDITIONOPERATOR_IFSTRINGCONTAINS("ifStringContains", false),
    CONDITIONOPERATOR_IFSTRINGNOTCONTAINS("ifStringNotContains", false),
    CONDITIONOPERATOR_IFTEXTINELEMENT("ifTextInElement", false),
    CONDITIONOPERATOR_IFTEXTNOTINELEMENT("ifTextNotInElement", false),
    CONDITIONOPERATOR_NEVER("never", false);
    
    private final String condition;
    private final boolean isOperatorEvaluationRequired;

    private ConditionOperatorEnum(String condition, boolean isOperatorEvaluationRequired) {
        this.condition = condition;
        this.isOperatorEvaluationRequired = isOperatorEvaluationRequired;
    }

    public String getCondition() {
        return condition;
    }

    public boolean isOperatorEvaluationRequired() {
        return isOperatorEvaluationRequired;
    }
    
    public static ConditionOperatorEnum getConditionOperatorEnumFromString(String conditionOperator) {
        for (ConditionOperatorEnum conditionOperatorEnum : ConditionOperatorEnum.values()) {
            if (conditionOperatorEnum.getCondition().equals(conditionOperator)) {
                return conditionOperatorEnum;
            }
        }
        return null;
    }
}
