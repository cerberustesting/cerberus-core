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
package org.cerberus.crud.factory;

import java.sql.Timestamp;
import org.cerberus.crud.entity.TestCaseStep;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStep {

    /**
     * 
     * @param test
     * @param testCase
     * @param step
     * @param sort
     * @param loop
     * @param conditionOperator
     * @param conditionVal1
     * @param conditionVal2
     * @param conditionVal3
     * @param description
     * @param isUsingLibraryStep 
     * @param useStepTest
     * @param libraryStepTestcase
     * @param libraryStepStepId
     * @param isLibraryTest
     * @param isExecutionForced
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return 
     */
    TestCaseStep create(String test, String testCase, int step, int sort, String loop, String conditionOperator, String conditionVal1, String conditionVal2, String conditionVal3, String description,
            boolean isUsingLibraryStep , String libraryStepTest, String libraryStepTestcase, Integer libraryStepStepId, boolean isLibraryTest, boolean isExecutionForced, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);

}
