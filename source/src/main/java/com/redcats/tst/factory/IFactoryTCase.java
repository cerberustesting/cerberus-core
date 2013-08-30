/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.*;

import java.util.List;

/**
 * @author bcivel
 */
public interface IFactoryTCase {

    TCase create(String test, String testCase, String origin, String refOrigin, String creator,
                 String implementer, String lastModifier, String project, String ticket, String application,
                 String runQA, String runUAT, String runPROD, int priority, String group, String status,
                 String shortDescription, String description, String howTo, String active, String fromSprint,
                 String fromRevision, String toSprint, String toRevision, String lastExecutionStatus, String bugID,
                 String targetSprint, String targetRevision, String comment, List<TestCaseCountry> testCaseCountry,
                 List<TestCaseCountryProperties> testCaseCountryProperties, List<TestCaseStep> testCaseStep,
                 List<TestCaseStepBatch> testCaseStepBatch);

    TCase create(String test, String testCase);

}
