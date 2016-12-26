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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import java.util.List;
import org.apache.log4j.Logger;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepBatch;
import org.springframework.stereotype.Service;
import org.cerberus.crud.factory.IFactoryTestCase;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCase implements IFactoryTestCase {

    private static final Logger LOG = Logger.getLogger(FactoryTestCase.class);
    
//    private TestCase newTestCase;

    @Override
    public TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated, String implementer, String usrModif, String project, String ticket, String function, String application,
            String activeQA, String activeUAT, String activePROD, int priority, String group, String status, String description, String behavior, String howTo, String tcActive, String conditionOper, String conditionVal1, String conditionVal2, String fromBuild, String fromRev,
            String toBuild, String toRev, String lastExecutionStatus, String bugID, String targetBuild, String targetRev, String comment, String userAgent, List<TestCaseCountry> testCaseCountry,
            List<TestCaseCountryProperties> testCaseCountryProperties, List<TestCaseStep> testCaseStep, List<TestCaseStepBatch> testCaseStepBatch) {
        TestCase newTestCase = new TestCase();
        newTestCase.setTcActive(tcActive);
        newTestCase.setConditionOper(conditionOper);
        newTestCase.setConditionVal1(conditionVal1);
        newTestCase.setConditionVal2(conditionVal2);
        newTestCase.setApplication(application);
        newTestCase.setBugID(bugID);
        newTestCase.setComment(comment);
        newTestCase.setBehaviorOrValueExpected(behavior);
        newTestCase.setFromRev(fromRev);
        newTestCase.setFromBuild(fromBuild);
        newTestCase.setGroup(group);
        newTestCase.setHowTo(howTo);
        newTestCase.setImplementer(implementer);
        newTestCase.setOrigine(origine);
        newTestCase.setPriority(priority);
        newTestCase.setProject(project);
        newTestCase.setRefOrigine(refOrigine);
        newTestCase.setActivePROD(activePROD);
        newTestCase.setActiveQA(activeQA);
        newTestCase.setActiveUAT(activeUAT);
        newTestCase.setDescription(description);
        newTestCase.setStatus(status);
        newTestCase.setTargetRev(targetRev);
        newTestCase.setTargetBuild(targetBuild);
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setTicket(ticket);
        newTestCase.setToRev(toRev);
        newTestCase.setToBuild(toBuild);
        newTestCase.setUsrCreated(usrCreated);
        newTestCase.setUsrModif(usrModif);
        newTestCase.setUserAgent(userAgent);
        newTestCase.setLastExecutionStatus(lastExecutionStatus);
        newTestCase.setTestCaseCountry(testCaseCountry);
        newTestCase.setTestCaseCountryProperties(testCaseCountryProperties);
        newTestCase.setTestCaseStep(testCaseStep);
        newTestCase.setTestCaseStepBatch(testCaseStepBatch);
        newTestCase.setFunction(function);

        return newTestCase;
    }

    @Override
    public TestCase create(String test, String testCase, String origine, String refOrigine, String usrCreated, String implementer, String usrModif, String project, String ticket, String function, String application,
            String activeQA, String activeUAT, String activePROD, int priority, String group, String status, String description, String behavior, String howTo, String tcActive, String conditionOper, String conditionVal1, String conditionVal2, String fromBuild, String fromRev,
            String toBuild, String toRev, String lastExecutionStatus, String bugID, String targetBuild, String targetRev, String comment, String dateCreated, String userAgent, Timestamp dateModif) {
        TestCase newTestCase = new TestCase();
        newTestCase.setTcActive(tcActive);
        newTestCase.setConditionOper(conditionOper);
        newTestCase.setConditionVal1(conditionVal1);
        newTestCase.setConditionVal2(conditionVal2);
        newTestCase.setApplication(application);
        newTestCase.setBugID(bugID);
        newTestCase.setComment(comment);
        newTestCase.setBehaviorOrValueExpected(behavior);
        newTestCase.setFromRev(fromRev);
        newTestCase.setFromBuild(fromBuild);
        newTestCase.setGroup(group);
        newTestCase.setHowTo(howTo);
        newTestCase.setImplementer(implementer);
        newTestCase.setLastExecutionStatus(lastExecutionStatus);
        newTestCase.setOrigine(origine);
        newTestCase.setPriority(priority);
        newTestCase.setProject(project);
        newTestCase.setRefOrigine(refOrigine);
        newTestCase.setActivePROD(activePROD);
        newTestCase.setActiveQA(activeQA);
        newTestCase.setActiveUAT(activeUAT);
        newTestCase.setDescription(description);
        newTestCase.setStatus(status);
        newTestCase.setTargetRev(targetRev);
        newTestCase.setTargetBuild(targetBuild);
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setTicket(ticket);
        newTestCase.setToRev(toRev);
        newTestCase.setToBuild(toBuild);
        newTestCase.setFunction(function);
        newTestCase.setUsrCreated(usrCreated);
        newTestCase.setDateCreated(dateCreated);
        newTestCase.setDateModif(dateModif);
        newTestCase.setUsrModif(usrModif);
        newTestCase.setUserAgent(userAgent);

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
