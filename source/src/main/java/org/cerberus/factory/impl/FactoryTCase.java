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
package org.cerberus.factory.impl;

import java.util.List;

import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.entity.TestCaseStepBatch;
import org.cerberus.factory.IFactoryTCase;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTCase implements IFactoryTCase {

    private TCase newTestCase;

    @Override
    public TCase create(String test, String testCase, String origin, String refOrigin, String creator, String implementer, String lastModifier, String project, String ticket, String application, String runQA, String runUAT, String runPROD, int priority, String group, String status, String shortDescription, String description, String howTo, String active, String fromSprint, String fromRevision, String toSprint, String toRevision, String lastExecutionStatus, String bugID, String targetSprint, String targetRevision, String comment, List<TestCaseCountry> testCaseCountry, List<TestCaseCountryProperties> testCaseCountryProperties, List<TestCaseStep> testCaseStep, List<TestCaseStepBatch> testCaseStepBatch) {
        newTestCase = new TCase();
        newTestCase.setActive(active);
        newTestCase.setApplication(application);
        newTestCase.setBugID(bugID);
        newTestCase.setComment(comment);
        newTestCase.setCreator(creator);
        newTestCase.setDescription(description);
        newTestCase.setFromRevision(fromRevision);
        newTestCase.setFromSprint(fromSprint);
        newTestCase.setGroup(group);
        newTestCase.setHowTo(howTo);
        newTestCase.setImplementer(implementer);
        newTestCase.setLastExecutionStatus(lastExecutionStatus);
        newTestCase.setLastModifier(lastModifier);
        newTestCase.setOrigin(origin);
        newTestCase.setPriority(priority);
        newTestCase.setProject(project);
        newTestCase.setRefOrigin(refOrigin);
        newTestCase.setRunPROD(runPROD);
        newTestCase.setRunQA(runQA);
        newTestCase.setRunUAT(runUAT);
        newTestCase.setShortDescription(shortDescription);
        newTestCase.setStatus(status);
        newTestCase.setTargetRevision(targetRevision);
        newTestCase.setTargetSprint(targetSprint);
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        newTestCase.setTicket(ticket);
        newTestCase.setToRevision(toRevision);
        newTestCase.setToSprint(toSprint);
        newTestCase.setTestCaseCountry(testCaseCountry);
        newTestCase.setTestCaseCountryProperties(testCaseCountryProperties);
        newTestCase.setTestCaseStep(testCaseStep);
        newTestCase.setTestCaseStepBatch(testCaseStepBatch);

        return newTestCase;
    }

    @Override
    public TCase create(String test, String testCase) {
        newTestCase = new TCase();
        newTestCase.setTest(test);
        newTestCase.setTestCase(testCase);
        return newTestCase;
    }

}
