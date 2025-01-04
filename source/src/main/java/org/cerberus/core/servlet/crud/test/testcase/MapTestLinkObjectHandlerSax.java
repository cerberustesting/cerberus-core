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
package org.cerberus.core.servlet.crud.test.testcase;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseService;
import static org.cerberus.core.engine.execution.enums.ConditionOperatorEnum.CONDITIONOPERATOR_ALWAYS;
import org.cerberus.core.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author vertigo
 */
public class MapTestLinkObjectHandlerSax extends DefaultHandler {

    private static final Logger LOG = LogManager.getLogger(MapTestLinkObjectHandlerSax.class);

    private StringBuilder currentValue = new StringBuilder();
    List<TestCase> result;
    TestCase currentTestCase;
    TestCaseStep currentStep;
    List<TestCaseStep> currentStepList;
    int stepId;

    TestCaseStepAction currentAction;
    List<TestCaseStepAction> currentActionList;
    int actionId;

    TestCaseStepActionControl currentControl;
    List<TestCaseStepActionControl> currentControlList;
    int controlId;

    String testFolder;
    String application;
    String userCreated;

    private ITestCaseService testcaseService;

    public MapTestLinkObjectHandlerSax(String test, String application, String userCreated, ITestCaseService testcaseService) {
        this.testFolder = test;
        this.application = application;
        this.testcaseService = testcaseService;
        this.userCreated = userCreated;
    }

    public List<TestCase> getResult() {
        return result;
    }

    @Override
    public void startDocument() {
        LOG.debug("Start Document.");

        result = new ArrayList<>();
    }

    @Override
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) {

        // reset the tag value
        currentValue.setLength(0);

        LOG.debug("Start Element. '{}' '{}' '{}' '{}'", uri, localName, qName, attributes);

        // start of loop
        if (qName.equalsIgnoreCase("testcase")) {

            // new staff
            currentTestCase = new TestCase();

            currentTestCase.setTest(testFolder);
            currentTestCase.setTestcase(testcaseService.getNextAvailableTestcaseId(testFolder));

            currentTestCase.setOrigine("TestLink");
            currentTestCase.setActive(true);
            currentTestCase.setActiveQA(true);
            currentTestCase.setActiveUAT(true);
            currentTestCase.setActivePROD(false);
            currentTestCase.setApplication(application);
            currentTestCase.setType(TestCase.TESTCASE_TYPE_MANUAL);
            currentTestCase.setStatus("WORKING");
            currentTestCase.setConditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition());
            currentTestCase.setUsrCreated(userCreated);

            // staff id
            String internalid = attributes.getValue("internalid");
            currentTestCase.setRefOrigine(internalid);

            String name = attributes.getValue("name");
            currentTestCase.setDescription(name);
        }

        if (qName.equalsIgnoreCase("steps")) {
            currentStepList = new ArrayList<>();
            stepId = 1;
        }

        if (qName.equalsIgnoreCase("step")) {
            actionId = 1;
            controlId = 1;
            
            currentStep = new TestCaseStep();
            currentStep.setLibraryStepStepId(0);
            currentStep.setStepId(stepId);
            currentStep.setSort(stepId);
            currentStep.setConditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition());
            currentStep.setLoop(TestCaseStep.LOOP_ONCEIFCONDITIONTRUE);
            currentStep.setUsrCreated(userCreated);
        }

        if (qName.equalsIgnoreCase("actions")) {
            currentActionList = new ArrayList<>();
            currentAction = new TestCaseStepAction();
            currentAction.setAction(TestCaseStepAction.ACTION_DONOTHING);
            currentAction.setConditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition());
            currentAction.setUsrCreated(userCreated);
            currentAction.setStepId(stepId);
            currentAction.setActionId(actionId);
            currentAction.setSort(actionId);
            currentAction.setScreenshotFilename("");
            currentAction.setValue1("");
            currentAction.setValue2("");
            currentAction.setValue3("");
            currentAction.setConditionValue1("");
            currentAction.setConditionValue2("");
            currentAction.setConditionValue3("");

            currentControlList = new ArrayList<>();
            currentControl = new TestCaseStepActionControl();
            currentControl.setControl(TestCaseStepActionControl.CONTROL_UNKNOWN);
            currentControl.setConditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition());
            currentControl.setUsrCreated(userCreated);
            currentControl.setStepId(stepId);
            currentControl.setActionId(actionId);
            currentControl.setControlId(controlId);
            currentControl.setSort(controlId);
            currentControl.setScreenshotFilename("");
            currentControl.setValue1("");
            currentControl.setValue2("");
            currentControl.setValue3("");
            currentControl.setConditionValue1("");
            currentControl.setConditionValue2("");
            currentControl.setConditionValue3("");

        }

    }

    @Override
    public void endElement(String uri,
            String localName,
            String qName) {
        LOG.debug("End Element. '{}' '{}' '{}' '{}'", uri, localName, qName);

        if (qName.equalsIgnoreCase("summary")) {
            currentTestCase.setDetailedDescription(currentValue.toString());
        }

        if (qName.equalsIgnoreCase("testcase")) {
            result.add(currentTestCase);
        }

        if (qName.equalsIgnoreCase("steps")) {
            currentTestCase.setSteps(currentStepList);
        }

        if (qName.equalsIgnoreCase("step")) {
            currentControlList.add(currentControl);

            currentAction.setControls(currentControlList);
            currentActionList.add(currentAction);

            currentStep.setActions(currentActionList);
            currentStepList.add(currentStep);
            stepId++;
        }
        if (qName.equalsIgnoreCase("step_number")) {
            currentStep.setDescription(currentValue.toString());
        }

        if (qName.equalsIgnoreCase("actions")) {
            currentAction.setDescription(StringUtil.getLeftString(StringUtil.convertHtmlToString(currentValue.toString()), 250));
        }

        if (qName.equalsIgnoreCase("expectedresults")) {
            currentControl.setDescription(StringUtil.getLeftString(StringUtil.convertHtmlToString(currentValue.toString()), 250));
        }

    }

    @Override
    public void characters(char ch[], int start, int length) {
        currentValue.append(ch, start, length);

    }

}
