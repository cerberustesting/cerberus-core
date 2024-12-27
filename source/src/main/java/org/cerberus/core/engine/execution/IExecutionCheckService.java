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
package org.cerberus.core.engine.execution;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestCaseExecution;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 2.0.0
 */
public interface IExecutionCheckService {

    /**
     * Global method that checks if Execution can be triggered.
     *
     * @param tCExecution
     * @return
     */
    MessageGeneral checkTestCaseExecution(TestCaseExecution tCExecution);

    /**
     * Method that checks if testCase match the range of build revision on the
     * corresponding environment.
     *
     * @param tc
     * @param envBuild
     * @param envRevision
     * @param envSystem
     * @return
     */
    boolean checkRangeBuildRevision(TestCase tc, String envBuild, String envRevision, String envSystem);
}
