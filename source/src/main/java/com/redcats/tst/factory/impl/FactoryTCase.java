/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.*;
import com.redcats.tst.factory.IFactoryTCase;
import org.springframework.stereotype.Service;

import java.util.List;

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
