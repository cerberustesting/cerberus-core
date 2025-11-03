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

import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.messages.Message;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

@Service
public class TestCaseCreationGeneratePromptAI {


    private static String readJsonFromResources(String fileName) {
        InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new RuntimeException("File not found : " + fileName);
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public String buildPromptForTestcase(String testFolderId, String featureDescription){

        String structureExample = readJsonFromResources("static/structure_testcase.json");

        String prompt = new StringBuilder()
                .append("You are an assistant specialized in generating automated test cases for Cerberus Testing.\n\n")
                .append("Based on the following feature description, identify 3 or 4 test scenarios to validate it.\n\n")
                .append("Respond ONLY with standalone JSON objects, one per line, each following the structure below.\n")
                .append("Do NOT wrap them inside an array — no '[' or ']', just multiple JSON objects separated by line breaks.\n\n")
                .append("Do NOT use Markdown, code blocks, or backticks — only plain JSON.\n")
                .append("Each object must strictly follow this structure:\n")
                .append(structureExample).append("\n\n")
                .append("The testFolderId is:\n").append(testFolderId).append("\n\n")
                .append("Generate a concise key for TestcaseId (e.g., TEST001A, TEST001B, etc.).\n\n")
                .append("Feature description:\n> ").append(featureDescription).append("\n\n")
                .append("Respond in the same language as the feature description.\n")
                .append("Respond ONLY with valid JSON objects, one per line, no additional text.\n")
                .toString();

        return prompt;
    }

    public String buildPromptForSteps(String identifiedSteps){

        String structureExample = readJsonFromResources("static/structure_step.json");
        String libraryStepsList = " ";

        String prompt = new StringBuilder()
                .append("You are an expert assistant specialized in generating automated test cases for Cerberus Testing.\n\n")
                .append("Based on the following feature description and the identified functional steps, generate a JSON array of functional steps.\n")
                .append("A functional step is a group of actions and controls representing a distinct functional phase or user-level interaction within a test case.\n")
                .append("You may use available steps from the library if relevant.\n")
                .append("Available Steps in library:\n").append(libraryStepsList).append("\n\n")
                .append("Feature Description and Identified Steps:\n> ").append(identifiedSteps).append("\n\n")
                .append("Respond strictly in the language of the feature description.\n")
                .append("Respond ONLY with a JSON array of objects following the structure below :\n")
                .append(structureExample).append("\n\n")
                .append("Convert each identified step into one object respecting the contract.\n")
                .append("Maintain logical order of steps.\n")
                .append("Do NOT wrap them inside an array — no '[' or ']', just multiple JSON objects separated by line breaks.\n\n")
                .append("Do NOT use Markdown, code blocks, or backticks — only plain JSON.\n")
                .append("Fill all fields according to the template.\n")
                .append("Do NOT add any explanation or text outside the JSON array.\n")
                .append("Respond ONLY with valid JSON objects, one per line, no additional text.\n")
                .append("Do NOT prefix with ```json or any other text.\n")
                .append("The output must be parseable directly by a JSON parser.\n")
                .toString();

        return prompt;
    }

    public String buildPromptForActionsAndControls(TestCaseStep testCaseStep, String testCaseDescription, String promptForActionDefinition){

        String availableActions = readJsonFromResources("static/actions.json");
        String availableControls = readJsonFromResources("static/controls.json");
        String structureActions = readJsonFromResources("static/structure_action.json");
        String structureControls = readJsonFromResources("static/structure_control.json");

        String prompt = new StringBuilder()
                .append("You are an expert assistant specialized in generating automated test cases for Cerberus Testing.\n\n")
                .append("Based on the following feature description and functional step description, generate a JSON array of functional actions and controls.\n")
                .append("The testcase you are implementing has this detailed description :\n").append(testCaseDescription).append("\n\n")
                .append("The step you are implementing has this detailed description :\n").append(testCaseStep.getDescription()).append("\n\n")
                .append("What you try to achieve :\n").append(promptForActionDefinition).append("\n\n")
                .append("**Documentation Actions :**\n").append(availableActions).append("\n\n")
                .append("Chaque action disponible dans Cerberus est décrite par un objet contenant les champs suivants : action (le type d'action à réaliser, par exemple click), param1, param2, param3 (paramètres à utiliser dans les actions), applicationType (types d'application où l'action est applicable, ex. GUI, IPA, APK, FAT), et description (explication concise de ce que fait l'action, par exemple « Click on the element »).").append("\n\n")
                .append("**Documentation Controls  :**\n").append(availableControls).append("\n\n")
                .append("chaque contrôle est défini par control (nom du contrôle, ex. verifyStringEqual), param1, param2 et param3 (valeurs ou éléments à utiliser), applicationType (types d’application concernés, ex. ALL), et description (ce que fait le contrôle)").append("\n\n")
                .append("Respond strictly in the language of the feature description.\n")
                .append("For Action, respond ONLY with objects following the structure below :\n")
                .append(structureActions).append("\n\n")
                .append("For Control, respond ONLY with objects following the structure below :\n")
                .append(structureControls).append("\n\n")
                .append("Maintain logical order of actions and controls.\n")
                .append("Controls are attached to action.\n")
                .append("Do NOT wrap them inside an array — no '[' or ']', just multiple JSON objects separated by line breaks.\n\n")
                .append("Do NOT use Markdown, code blocks, or backticks — only plain JSON.\n")
                .append("Fill all fields according to the template.\n")
                .append("Do NOT add any explanation or text outside the JSON array.\n")
                .append("Respond ONLY with valid JSON objects, one per line, no additional text.\n")
                .append("Do NOT prefix with ```json or any other text.\n")
                .append("The output must be parseable directly by a JSON parser.\n")
                .toString();

        return prompt;
    }


    public String buildPromptForNextTestcaseIdGeneration(String existingTestcaseId){

        String prompt = new StringBuilder()
                .append("Propose me the next testcaseID regarding the already existing ones.\n\n")
                .append("**Existing Testcase ID :**\n").append(existingTestcaseId).append("\n\n")
                .append("TestcaseID generated must not exceed 10 caracters.\n")
                .append("Respond ONLY the testcase id, no additional text, no explanation, no icon, no special characters, ONLY the testcase id.\n")
                .toString();

        return prompt;
    }


}
