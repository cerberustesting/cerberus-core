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
package org.cerberus.crud.factory;

import java.sql.Timestamp;
import java.util.List;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepBatch;

/**
 * @author bcivel
 */
public interface IFactoryTestCase {

    /**
     *
     * @param test
     * @param testCase
     * @param origine
     * @param refOrigine
     * @param usrCreated
     * @param implementer
     * @param usrModif
     * @param project
     * @param ticket
     * @param function
     * @param application
     * @param activeQA
     * @param activeUAT
     * @param activePROD
     * @param priority
     * @param group
     * @param status
     * @param description
     * @param behavior
     * @param howTo
     * @param tcActive
     * @param conditionOper
     * @param conditionVal1
     * @param fromBuild
     * @param fromRev
     * @param toBuild
     * @param toRev
     * @param lastExecutionStatus
     * @param bugID
     * @param targetBuild
     * @param targetRev
     * @param comment
     * @param userAgent
     * @param testCaseCountry
     * @param testCaseCountryProperties
     * @param testCaseStep
     * @param testCaseStepBatch
     * @return
     */
    TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated,
                 String implementer, String usrModif, String project, String ticket, String function, String application,
                 String activeQA, String activeUAT, String activePROD, int priority, String group, String status,
                 String description, String behavior, String howTo, String tcActive, String conditionOper, String conditionVal1, String fromBuild,
                 String fromRev, String toBuild, String toRev, String lastExecutionStatus, String bugID,
                 String targetBuild, String targetRev, String comment, String userAgent, List<TestCaseCountry> testCaseCountry,
                 List<TestCaseCountryProperties> testCaseCountryProperties, List<TestCaseStep> testCaseStep,
                 List<TestCaseStepBatch> testCaseStepBatch);
    
    /**
     *
     * @param test
     * @param testCase
     * @param origine
     * @param refOrigine
     * @param usrCreated
     * @param implementer
     * @param usrModif
     * @param project
     * @param ticket
     * @param function
     * @param application
     * @param activeQA
     * @param activeUAT
     * @param activePROD
     * @param priority
     * @param group
     * @param status
     * @param description
     * @param behavior
     * @param howTo
     * @param tcActive
     * @param conditionOper
     * @param conditionVal1
     * @param fromBuild
     * @param fromRev
     * @param toBuild
     * @param toRev
     * @param lastExecutionStatus
     * @param bugID
     * @param targetBuild
     * @param targetRev
     * @param comment
     * @param dateCreated
     * @param userAgent
     * @param dateModif
     * @return
     */
    TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated,
                 String implementer, String usrModif, String project, String ticket, String function, String application,
                 String activeQA, String activeUAT, String activePROD, int priority, String group, String status,
                 String description, String behavior, String howTo, String tcActive, String conditionOper, String conditionVal1, String fromBuild,
                 String fromRev, String toBuild, String toRev, String lastExecutionStatus, String bugID,
                 String targetBuild, String targetRev, String comment,  String dateCreated, String userAgent, Timestamp dateModif);

    /**
     *
     * @param test
     * @param testCase
     * @return
     */
    TestCase create(String test, String testCase);
    
    /**
     *
     * @param test
     * @param testCase
     * @param description
     * @return
     */
    TestCase create(String test, String testCase, String description);

}
