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
package org.cerberus.core.service.ai.impl;

import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class AIBuildPrompt {


    private static String readResourcesAsText(String fileName) {
        InputStream inputStream = AIBuildPrompt.class.getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new RuntimeException("File not found : " + fileName);
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public String buildPromptForSessionTitle(String newMessage){

        String template = readResourcesAsText("prompts/chatwithai_generate_title.prompt");
        return template.replace("{{newMessage}}", newMessage);

    }

    public String buildPromptForTestcase(String testFolderId, String featureDescription){

        String structureExample = readResourcesAsText("static/structure_testcase.json");
        String template = readResourcesAsText("prompts/suggest_testcase_scenario.prompt");

        return template.replace("{{structureExample}}", structureExample)
                .replace("{{testFolderId}}", testFolderId)
                .replace("{{featureDescription}}", featureDescription);

    }

    public String buildPromptForSteps(String identifiedSteps){

        String structureExample = readResourcesAsText("static/structure_step.json");
        String libraryStepsList = " ";

        String template = readResourcesAsText("prompts/generate_testcase_steps.prompt");
        return template.replace("{{libraryStepsList}}", libraryStepsList)
                .replace("{{identifiedSteps}}", identifiedSteps)
                .replace("{{structureExample}}", structureExample);

    }

    public String buildPromptForActionsAndControls(TestCaseStep testCaseStep, String testCaseDescription, String promptForActionDefinition){

        String availableActions = readResourcesAsText("static/actions.json");
        String availableControls = readResourcesAsText("static/controls.json");
        String structureActions = readResourcesAsText("static/structure_action.json");
        String structureControls = readResourcesAsText("static/structure_control.json");

        String template = readResourcesAsText("prompts/generate_testcase_actionsandcontrols.prompt");
        return template.replace("{{testCaseDescription}}", testCaseDescription)
                .replace("{{testCaseStepDescription}}", testCaseStep.getDescription())
                .replace("{{promptForActionDefinition}}", promptForActionDefinition)
                .replace("{{availableActions}}", availableActions)
                .replace("{{availableControls}}", availableControls)
                .replace("{{structureActions}}", structureActions)
                .replace("{{structureControls}}", structureControls);

    }


    public String buildPromptForNextTestcaseIdGeneration(String existingTestcaseId){

        String template = readResourcesAsText("prompts/generate_testcase_next_testcase_id.prompt");
        return template.replace("{{existingTestcaseId}}", existingTestcaseId);

    }

    public String buildPromptForSelfHealing(TestCaseExecution execution, TestCase testCase) {

        String template = readResourcesAsText("prompts/self_healing.prompt");

        return template.replace("{{testShortDescription}}", testCase.getDescription())
                .replace("{{testDescription}}", testCase.getDetailedDescription())
                .replace("{{testShortDescription}}", testCase.getDescription())
                .replace("{{testImplementation}}", execution.toJson(true).toString());
    }

    public String buildPromptForApplicationObjectGeneration(Application application, List<ApplicationObject> existingApplictionObjects, String pageName, List<String> targets) {

        List<String> existingAOName = new ArrayList<> ();
        for (ApplicationObject ao : existingApplictionObjects){
            existingAOName.add(ao.getObject());
        }
        String template = readResourcesAsText("prompts/generate_applicationobject.prompt");
        return template.replace("{{targetElements}}", String.join(",", targets))
                .replace("{{pageName}}", pageName)
                .replace("{{application}}", application.getApplication())
                .replace("{{existingElements}}", String.join(",", existingAOName));
    }

    public String buildPromptForApplicationObjectSystemContext() {
        String template = readResourcesAsText("prompts/generate_applicationobject_systemcontext.prompt");
        return template;
    }


}
