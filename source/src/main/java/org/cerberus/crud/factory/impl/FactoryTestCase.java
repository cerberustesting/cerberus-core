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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepBatch;
import org.springframework.stereotype.Service;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.json.JSONArray;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCase implements IFactoryTestCase {

    private static final Logger LOG = LogManager.getLogger(FactoryTestCase.class);

//    private TestCase newTestCase;
    @Override
    public TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated, String implementer, String executor, String usrModif, String application,
            boolean isActiveQA, boolean isActiveUAT, boolean isActivePROD, int priority, String type, String status, String description, String detailedDescription, boolean isActive, String conditionOperator, String conditionVal1, String conditionVal2, String conditionVal3, String fromMajor, String fromMinor,
            String toMajor, String toMinor, String lastExecutionStatus, JSONArray bugs, String targetMajor, String targetMinor, String comment, String userAgent, String screenSize, List<TestCaseCountry> testCaseCountry,
            List<TestCaseCountryProperties> testCaseCountryProperties, List<TestCaseStep> testCaseStep, List<TestCaseStepBatch> testCaseStepBatch) {
        TestCase newTestCase = new TestCase();
        newTestCase.setActive(isActive);
        newTestCase.setConditionOperator(conditionOperator);
        newTestCase.setConditionVal1(conditionVal1);
        newTestCase.setConditionVal2(conditionVal2);
        newTestCase.setConditionVal3(conditionVal3);
        newTestCase.setApplication(application);
        newTestCase.setBugs(bugs);
        newTestCase.setComment(comment);
        newTestCase.setDetailedDescription(detailedDescription);
        newTestCase.setFromMinor(fromMinor);
        newTestCase.setFromMajor(fromMajor);
        newTestCase.setType(type);
        newTestCase.setImplementer(implementer);
        newTestCase.setExecutor(executor);
        newTestCase.setOrigine(origine);
        newTestCase.setPriority(priority);
        newTestCase.setRefOrigine(refOrigine);
        newTestCase.setActivePROD(isActivePROD);
        newTestCase.setActiveQA(isActiveQA);
        newTestCase.setActiveUAT(isActiveUAT);
        newTestCase.setDescription(description);
        newTestCase.setStatus(status);
        newTestCase.setTargetMinor(targetMinor);
        newTestCase.setTargetMajor(targetMajor);
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setToMinor(toMinor);
        newTestCase.setToMajor(toMajor);
        newTestCase.setUsrCreated(usrCreated);
        newTestCase.setUsrModif(usrModif);
        newTestCase.setUserAgent(userAgent);
        newTestCase.setScreenSize(screenSize);
        newTestCase.setLastExecutionStatus(lastExecutionStatus);
        newTestCase.setTestCaseCountries(testCaseCountry);
        newTestCase.setTestCaseCountryProperties(testCaseCountryProperties);
        newTestCase.setSteps(testCaseStep);
        newTestCase.setTestCaseStepBatch(testCaseStepBatch);

        return newTestCase;
    }

    @Override
    public TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated, String implementer, String executor, String usrModif, String application,
            boolean isActiveQA, boolean isActiveUAT, boolean isActivePROD, int priority, String type, String status, String description, String detailedDescription, boolean isActive, String conditionOperator, String conditionVal1, String conditionVal2, String conditionVal3, String fromMajor, String fromMinor,
            String toMajor, String toMinor, String lastExecutionStatus, JSONArray bugs, String targetMajor, String targetMinor, String comment, String dateCreated, String userAgent, String screenSize, Timestamp dateModif, int version) {
        TestCase newTestCase = new TestCase();
        newTestCase.setActive(isActive);
        newTestCase.setConditionOperator(conditionOperator);
        newTestCase.setConditionVal1(conditionVal1);
        newTestCase.setConditionVal2(conditionVal2);
        newTestCase.setConditionVal3(conditionVal3);
        newTestCase.setApplication(application);
        newTestCase.setBugs(bugs);
        newTestCase.setComment(comment);
        newTestCase.setDetailedDescription(detailedDescription);
        newTestCase.setFromMinor(fromMinor);
        newTestCase.setFromMajor(fromMajor);
        newTestCase.setType(type);
        newTestCase.setImplementer(implementer);
        newTestCase.setExecutor(executor);
        newTestCase.setLastExecutionStatus(lastExecutionStatus);
        newTestCase.setOrigine(origine);
        newTestCase.setPriority(priority);
        newTestCase.setRefOrigine(refOrigine);
        newTestCase.setActivePROD(isActivePROD);
        newTestCase.setActiveQA(isActiveQA);
        newTestCase.setActiveUAT(isActiveUAT);
        newTestCase.setDescription(description);
        newTestCase.setStatus(status);
        newTestCase.setTargetMinor(targetMinor);
        newTestCase.setTargetMajor(targetMajor);
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setToMinor(toMinor);
        newTestCase.setToMajor(toMajor);
        newTestCase.setUsrCreated(usrCreated);
        newTestCase.setDateCreated(dateCreated);
        newTestCase.setDateModif(dateModif);
        newTestCase.setUsrModif(usrModif);
        newTestCase.setUserAgent(userAgent);
        newTestCase.setScreenSize(screenSize);
        newTestCase.setVersion(version);

        return newTestCase;
    }

    @Override
    public TestCase create(String test, String testCase) {
        TestCase newTestCase = new TestCase();
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        return newTestCase;
    }

    @Override
    public TestCase create(String test, String testCase, String description) {
        TestCase newTestCase = new TestCase();
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setDescription(description);
        return newTestCase;
    }
}
