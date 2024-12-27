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
package org.cerberus.core.engine.gwt;

import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.util.answer.AnswerItem;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface IPropertyService {

    /**
     *
     * @param stringToDecode
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param forceCalculation
     * @return
     * @throws CerberusEventException
     */
    AnswerItem<String> decodeStringWithExistingProperties(String stringToDecode, TestCaseExecution testCaseExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException;

    /**
     *
     * @param stringToDecode
     * @param execution
     * @param forceCalculation
     * @return
     * @throws CerberusEventException
     */
    String decodeStringWithDatalib(String stringToDecode, TestCaseExecution execution, boolean forceCalculation) throws CerberusEventException;

    /**
     *
     * @param testCaseExecutionData
     * @param tCExecution
     * @param testCaseStepActionExecution
     * @param testCaseCountryProperty
     * @param forceRecalculation
     */
    void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation);
}
