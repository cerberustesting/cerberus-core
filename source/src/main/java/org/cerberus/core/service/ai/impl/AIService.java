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

import com.anthropic.core.JsonValue;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.service.impl.ApplicationObjectService;
import org.cerberus.core.crud.service.impl.ApplicationService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.crud.service.impl.TestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.ai.IAIService;
import org.cerberus.core.service.ai.impl.parsing.ApplicationObjectFromAI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Service
public class AIService implements IAIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AIService.class);
    private String sessionId;

    @Autowired
    AISessionManager aiSessionManager;
    @Autowired
    AIBuildPrompt aiBuildPrompt;
    @Autowired
    ObjectFromAiResponse objectFromAiResponse;
    @Autowired
    TestCaseService testCaseService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    ApplicationObjectService applicationObjectService;
    @Autowired
    ApplicationObjectFromAI applicationObjectFromAI;
    @Autowired
    TestCaseExecutionService testCaseExecutionService;
    @Autowired
    AIClientService aiClientService;

    private TestCaseStepAction lastAction;


    @Override
    public void chatWithAI(String user, WebSocketSession websocketSession, String aiSessionID,  String newMessage) {

        /**
         * Generate aiSessionID if not exists)
         */
        aiSessionID = aiSessionID.equals("") ? UUID.randomUUID().toString() : aiSessionID;

        /**
         * Get all message of session and add the new message
         */
        List<MessageParam> messageParamList = aiSessionManager.getMessageParamListOfSession(aiSessionID);
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(newMessage).build());

        /**
         * After the first question (request N°3), generate a title to retrieve conversation
         */
        if (messageParamList.size()==3) {
            generateTitleForSession(newMessage, websocketSession, user, aiSessionID);
        }

        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();
        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseAndAccumulate(messageParamList, null,  text -> {
                fullResponse.append(text);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> message = new HashMap<>();
                    message.put("type", "chat");
                    message.put("done", "false");
                    message.put("data", text);
                    websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> message = new HashMap<>();
            message.put("type", "end");
            message.put("done", "true");
            message.put("data", "");
            websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));

            /**
             * store message and answer
             */
            LOG.debug("New message from :"+user+" in session :"+aiSessionID+" - message : "+newMessage);
            LOG.debug("New message from :"+user+" in session :"+aiSessionID+" - answer : "+fullResponse.toString());
            aiSessionManager.saveMessage(user, aiSessionID, newMessage, fullResponse.toString(), messageAccumulator.message(), "chatWithAI");

        } catch (Exception e) {
            LOG.error("Error during chatWithAI:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }
    }

    private void generateTitleForSession(String newMessage, WebSocketSession websocketSession, String user, String aiSessionID) {
        String title = aiBuildPrompt.buildPromptForSessionTitle(newMessage);

        List<MessageParam> messageParamTitle = new ArrayList<MessageParam>();
        messageParamTitle.add(MessageParam.builder().role(MessageParam.Role.USER).content(title).build());
        StringBuilder fullTitle = new StringBuilder();
        aiClientService.streamResponseAndAccumulate(messageParamTitle,null,  text -> {
            fullTitle.append(text);
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> message = new HashMap<>();
                message.put("type", "title");
                message.put("data", text);
                websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
            } catch (Exception e) {
                LOG.error("Error during streaming callback:", e);
            }
        });
        LOG.debug(fullTitle.toString());
        aiSessionManager.updateTitle(user, aiSessionID, fullTitle.toString());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> message = new HashMap<>();
        message.put("type", "title");
        message.put("title", fullTitle.toString());
        message.put("sessionID", aiSessionID);
        try {
            websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generateTestCaseProposal(String user, WebSocketSession websocketSession, String session,  String featureDescription, String application, String testFolderId) throws JsonProcessingException {

        String prompt = aiBuildPrompt.buildPromptForTestcase(testFolderId, featureDescription);
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder fullResponse = new StringBuilder();
        List<String> streamingErrors = new CopyOnWriteArrayList<>();

        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseObjectAndAccumulate(prompt, null, objectRetrieved -> {
                try {
                    String cleanedJson = aiClientService.sanitizeJson(objectRetrieved);
                    LOG.info("TestcaseGenerated : {}", cleanedJson);

                    JsonNode json = mapper.readTree(cleanedJson);
                    if (json.isObject()) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "testcaseCreated");
                        message.put("sessionID", session);
                        message.put("application", application);
                        message.put("testFolder", testFolderId);
                        message.put("data", json);
                        fullResponse.append(mapper.writeValueAsString(message));

                        websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                    }
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            // Log AI usage
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCaseProposal");

            //update Title
            aiSessionManager.updateTitle(user, session, featureDescription);

            // Notify completion
            websocketSession.sendMessage(new TextMessage("{\"type\":\"done\",\"message\":\"Generation complete\"}"));

            // Notify accumulated errors if any
            if (!streamingErrors.isEmpty()) {
                Map<String, Object> errorMessage = new HashMap<>();
                errorMessage.put("type", "error");
                errorMessage.put("message", "Errors occurred during streaming callback : "+streamingErrors);
                websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(errorMessage)));
            }

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }
    }


    public void createTestCaseAndGenerateContent(String user, WebSocketSession websocketSession, String session, String testFolder,  String testCaseJsonObject, String detailedDescription, String tempId) throws JsonProcessingException {

        //Get Testcase Ids existing for specific folder
        Map<String, List<String>> individualSearch = new HashMap<>();
        individualSearch.put("tec.test", List.of(testFolder));
        List<String> existingTestcases = testCaseService.readDistinctValuesByCriteria(null, testFolder, null, individualSearch, "tec.testcase").getDataList();
        LOG.info("Existing TestcaseID : " + existingTestcases.toString());

        //Generate Prompt to Guess the next ID
        String promptNextTestCaseID = aiBuildPrompt.buildPromptForNextTestcaseIdGeneration(String.join(",", existingTestcases));
        LOG.debug(promptNextTestCaseID);

        //Call To get next ID
        String testcaseId = this.getNextTestcaseId(promptNextTestCaseID, session, user);
        LOG.info("Next TestcaseID proposal : " + testcaseId);

        //Create Test
        TestCase testCase = null;
        try {
            testCase = objectFromAiResponse.createTestCase(testCaseJsonObject, testcaseId, user);
            // Notify testcase created
            websocketSession.sendMessage(new TextMessage("{\"type\":\"testcase_created\",\"tempId\":\""+tempId+"\"}"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //update Title
        aiSessionManager.updateTitle(user, session, testCase.getTest()+" - "+testCase.getTestcase()+" : "+testCase.getDescription());

        //Build prompt
        String prompt = aiBuildPrompt.buildPromptForSteps(detailedDescription);
        LOG.debug(prompt);
        ObjectMapper mapper = new ObjectMapper();
        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        try {
            TestCase finalTestCase = testCase;
            MessageAccumulator messageAccumulator = aiClientService.streamResponseObjectAndAccumulate(prompt, null, objectRetrieved -> {
                try {
                    String cleanedJson = aiClientService.sanitizeJson(objectRetrieved);
                    LOG.debug("Step : "+cleanedJson);

                    JsonNode json = mapper.readTree(cleanedJson);
                    if (json.isObject()) {
                        TestCaseStep testCaseStep = objectFromAiResponse.createTestCaseStep(json.toString(), finalTestCase);
                        this.createTestCaseStepActionAndControlAndGenerateContent(user, websocketSession, session, testCaseStep,finalTestCase.getDetailedDescription(), json.get("promptForActionDefinition").toString());
                        fullResponse.append(mapper.writeValueAsString(testCaseStep));
                        websocketSession.sendMessage(new TextMessage("{\"type\":\"testcasestep_created\",\"tempId\":\""+tempId+"\"}"));
                    }
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            // Log AI usage
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCase");

            // Notify completion
            websocketSession.sendMessage(new TextMessage("{\"type\":\"test_created_ack\",\"tempId\":\""+tempId+"\",\"test\":\""+finalTestCase.getTest()+"\",\"testcase\":\""+finalTestCase.getTestcase()+"\"}"));

            // Notify accumulated errors if any
            if (!streamingErrors.isEmpty()) {
                Map<String, Object> errorMessage = new HashMap<>();
                errorMessage.put("type", "error");
                errorMessage.put("message", "Errors occurred during streaming callback : "+streamingErrors);
                websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(errorMessage)));
            }

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }
    }

    private String getNextTestcaseId(String prompt, String sessionId, String user){

        Message response = aiClientService.getSyncMessage(prompt);

        //Get content (text only)
        String result = response.content().stream()
                .map(block -> block
                                .text()
                                .map(tb -> {
                                    String txt = tb.text();
                                    return txt == null ? "" : txt;})
                                .orElse(""))
                .collect(Collectors.joining())
                .trim();

        LOG.debug("Response AI = " + result);

        //Log AI Usage
        aiSessionManager.saveMessage(user, sessionId, prompt, result, response, "generateTestCase");

        return result;
    }


    private void createTestCaseStepActionAndControlAndGenerateContent(String user, WebSocketSession websocketSession, String session, TestCaseStep testCaseStep, String testCaseDescription,  String promptForActionDefinition) throws JsonProcessingException {

        //Build prompt
        String prompt = aiBuildPrompt.buildPromptForActionsAndControls(testCaseStep, testCaseDescription, promptForActionDefinition);
        LOG.debug(prompt);

        ObjectMapper mapper = new ObjectMapper();
        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseObjectAndAccumulate(prompt, null, objectRetrieved -> {
                try {
                    String cleanedJson = aiClientService.sanitizeJson(objectRetrieved);
                    LOG.info("Step : {}", cleanedJson);
                    JsonNode json = mapper.readTree(cleanedJson);

                    if (json.isObject()) {
                        JsonNode actionNode = json.get("action");
                        JsonNode controlNode = json.get("control");

                        if (actionNode != null && !actionNode.isNull()) {
                            // C’est une action
                            LOG.debug("Detected ACTION: " + actionNode.asText());
                            lastAction = objectFromAiResponse.createTestCaseStepAction(
                                    json.toString(),
                                    testCaseStep
                            );
                            fullResponse.append(mapper.writeValueAsString(lastAction));
                        } else if (controlNode != null && !controlNode.isNull()) {
                            // C’est un contrôle — nécessite la dernière action
                            if (lastAction == null) {
                                LOG.warn("Received CONTROL without a previous ACTION, skipping...");
                            } else {
                                LOG.debug("Detected CONTROL: " + controlNode.asText());
                                TestCaseStepActionControl tcsac = objectFromAiResponse.createTestCaseStepActionControl(json.toString(),lastAction);
                                fullResponse.append(mapper.writeValueAsString(tcsac));
                            }
                        } else {
                            LOG.warn("Unrecognized JSON type (no 'action' or 'control' field): " + json);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            // Log AI usage
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCase");

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }
    }


    public void executionDebugWithAI(String user, WebSocketSession websocketSession, String aiSessionID, long executionId) {

        /*
        Retreive TestCaseExecution
         */
        TestCaseExecution tce = testCaseExecutionService.findTCExecutionByKey(executionId);
        TestCase tc = null;
        try {
            tc = testCaseService.findTestCaseByKey(tce.getTest(), tce.getTestCase());
        } catch (CerberusException e) {
            throw new RuntimeException(e);
        }

        /*
        Build prompt
         */
        String prompt = aiBuildPrompt.buildPromptForSelfHealing(tce, tc);
        LOG.debug(prompt);

        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());

        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();
        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseAndAccumulate(messageParamList,null,  text -> {
                fullResponse.append(text);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> message = new HashMap<>();
                    message.put("type", "self_healing_explain");
                    message.put("data", text);
                    websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            /**
             * store message and answer
             */
            aiSessionManager.saveMessage(user, aiSessionID, prompt, fullResponse.toString(), messageAccumulator.message(), "self_healing_explain");

        } catch (Exception e) {
            LOG.error("Error during chatWithAI:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }


    }

    public void generateApplicationObjectProposalWithAI(String user, WebSocketSession websocketSession, String aiSessionID, String applicationId, String pageName, String htmlPath, String screenshotPath, List<String> targets ) {

        /*
        Retreive Application and Application Object
         */
        Application application = applicationService.readByKey(applicationId).getItem();
        List<ApplicationObject> aoList = applicationObjectService.readByApplication(applicationId).getDataList();

        String screenshotBase64 = "";
        String htmlContent = "";

        /*
        Get Picture and HTML
         */
        try {
            byte[] screenshotBytes = Files.readAllBytes(Paths.get(screenshotPath));
            screenshotBase64 = Base64.getEncoder().encodeToString(screenshotBytes);

            htmlContent = Files.readString(Paths.get(htmlPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        /*
        Build prompt and context
         */
        String prompt = aiBuildPrompt.buildPromptForApplicationObjectGeneration(application, aoList, pageName, targets);
        LOG.debug(prompt);
        String systemContext = aiBuildPrompt.buildPromptForApplicationObjectSystemContext();
        LOG.debug(systemContext);


        List<ContentBlockParam> contentOfBlockParams = new ArrayList<>();
        // Image block
        contentOfBlockParams.add(ContentBlockParam.ofImage(
                ImageBlockParam.builder()
                        .type(JsonValue.from("image"))
                        .source(Base64ImageSource.builder()
                                .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                                .data(screenshotBase64)
                                .build())
                        .build()));

        // Document block HTML — correction
        contentOfBlockParams.add(ContentBlockParam.ofDocument(
                DocumentBlockParam.builder()
                        .type(JsonValue.from("document"))
                        .source(JsonValue.from(Map.of(
                                "type", "text",
                                "media_type", "text/plain",
                                "data", removeScriptsAndStyles(htmlContent)
                        )))
                        .build()));

        /**
         * Get all message of session and add the new message
         */
        List<MessageParam> messageParamList = aiSessionManager.getMessageParamListOfSession(aiSessionID);
        MessageParam msg =  MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .content(prompt)
                        .contentOfBlockParams(contentOfBlockParams)
                        .build();
        messageParamList.add(msg);
        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();
        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseAndAccumulate(messageParamList,systemContext, text -> {
                fullResponse.append(text);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> message = new HashMap<>();
                    message.put("type", "chat");
                    message.put("data", text);
                    websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            LOG.debug(fullResponse.toString());

            List<ApplicationObject> aos = applicationObjectFromAI.parseAndCropAOBlocks(
                    fullResponse.toString(),
                    applicationId,
                    user,
                    screenshotPath
            );

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ao_proposals");
            message.put("data", aos);

            websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));

            /**
             * store message and answer
             */
            aiSessionManager.saveMessage(user, aiSessionID, prompt, fullResponse.toString(), messageAccumulator.message(), "ao_proposals");

        } catch (Exception e) {
            LOG.error("Error during chatWithAI:", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }


    }

    private String removeScriptsAndStyles(String html) {

        if (html == null || html.isEmpty()) {
            return html;
        }
        LOG.info(html.length());
        html = html.replaceAll("(?is)<script.*?>.*?</script>", "");
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");
        html = html.replaceAll("(?is)<!--.*?-->", "");
        LOG.info(html.length());

        return html;
    }


}