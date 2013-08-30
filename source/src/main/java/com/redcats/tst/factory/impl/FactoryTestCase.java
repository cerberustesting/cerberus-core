/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCase;
import com.redcats.tst.factory.IFactoryTestCase;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCase implements IFactoryTestCase {

    @Override
    public TestCase create(String test, String testCase, String origin, String refOrigin, String creator, String implementer, String lastModifier, String project, String ticket, String application, String runQA, String runUAT, String runPROD, int priority, String group, String status, String shortDescription, String description, String howTo, String active, String fromSprint, String fromRevision, String toSprint, String toRevision, String lastExecutionStatus, String bugID, String targetSprint, String targetRevision, String comment) {
        TestCase newTestCase = new TestCase();
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
        newTestCase.setOrigin(refOrigin);
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

        return newTestCase;
    }
}
