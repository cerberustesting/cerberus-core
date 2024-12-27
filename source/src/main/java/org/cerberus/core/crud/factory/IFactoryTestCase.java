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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import java.util.List;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.json.JSONArray;

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
     * @param executor
     * @param usrModif
     * @param application
     * @param isActiveQA
     * @param isActiveUAT
     * @param isActivePROD
     * @param priority
     * @param type
     * @param status
     * @param description
     * @param detailedDescription
     * @param isActive
     * @param conditionOperator
     * @param conditionValue1
     * @param conditionValue2
     * @param conditionValue3
     * @param conditionOptions
     * @param fromMajor
     * @param fromMinor
     * @param toMajor
     * @param toMinor
     * @param lastExecutionStatus
     * @param bugs
     * @param targetMajor
     * @param targetMinor
     * @param comment
     * @param userAgent
     * @param screenSize
     * @param testCaseCountry
     * @param testCaseCountryProperties
     * @param testCaseStep
     * @return
     */
    TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated,
            String implementer, String executor, String usrModif, String application, boolean isActiveQA, boolean isActiveUAT,
            boolean isActivePROD, int priority, String type, String status, String description, String detailedDescription,
            boolean isActive, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions,
            String fromMajor, String fromMinor, String toMajor, String toMinor, String lastExecutionStatus, JSONArray bugs,
            String targetMajor, String targetMinor, String comment, String userAgent, String screenSize,
            List<TestCaseCountry> testCaseCountry, List<TestCaseCountryProperties> testCaseCountryProperties,
            List<TestCaseStep> testCaseStep);

    /**
     *
     * @param test
     * @param testCase
     * @param origine
     * @param refOrigine
     * @param usrCreated
     * @param implementer
     * @param executor
     * @param usrModif
     * @param application
     * @param isActiveQA
     * @param isActiveUAT
     * @param isActivePROD
     * @param priority
     * @param type
     * @param status
     * @param description
     * @param detailedDescription
     * @param isActive
     * @param conditionOperator
     * @param conditionValue1
     * @param conditionValue2
     * @param conditionValue3
     * @param conditionOptions
     * @param fromMajor
     * @param fromMinor
     * @param toMajor
     * @param toMinor
     * @param lastExecutionStatus
     * @param bugs
     * @param targetMajor
     * @param targetMinor
     * @param comment
     * @param dateCreated
     * @param userAgent
     * @param screenSize
     * @param dateModif
     * @param version
     * @return
     */
    TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated,
            String implementer, String executor, String usrModif, String application,
            boolean isActiveQA, boolean isActiveUAT, boolean isActivePROD, int priority, String type, String status,
            String description, String detailedDescription, boolean isActive, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String fromMajor,
            String fromMinor, String toMajor, String toMinor, String lastExecutionStatus, JSONArray bugs,
            String targetMajor, String targetMinor, String comment, Timestamp dateCreated, String userAgent, String screenSize, Timestamp dateModif, int version);

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
