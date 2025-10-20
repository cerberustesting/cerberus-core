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

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.crud.entity.LogAIUsage;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.crud.service.impl.LogAIUsageService;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.crud.service.impl.TestCaseService;
import org.cerberus.core.service.ai.IAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
public class AIService implements IAIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AIService.class);
    private String sessionId;

    @Autowired
    AISessionManager aiSessionManager;
    @Autowired
    ParameterService parameterService;
    @Autowired
    TestCaseCreationGeneratePromptAI testCaseCreationGeneratePromptAI;
    @Autowired
    TestCaseGenerationPromptAI testCaseGenerationPromptAI;
    @Autowired
    TestCaseService testCaseService;
    @Autowired
    LogAIUsageService logAIUsageService;

    @Override
    public void askClaude(String user, WebSocketSession websocketSession, String sessionID,  String newQuestion) {

        /**
         * Insert new message (it create session if not exists)
         */
        sessionID = sessionID.equals("") ? websocketSession.getId() : sessionID;

        LOG.debug("New message from :"+user+" in session :"+sessionID+" : "+newQuestion);
        aiSessionManager.addMessage(user, sessionID, MessageParam.Role.USER.toString(), newQuestion);

        /**
         * Get all session message and generate context
         */
        List<UserPromptMessage> messageListFromSession = aiSessionManager.getAllMessages(user, sessionID);
        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        for (UserPromptMessage messageFromSession : messageListFromSession){
            MessageParam.Role role = MessageParam.Role.USER;
            switch (messageFromSession.getRole()) {
                case "user":
                    role = MessageParam.Role.USER;
                    break;
                case "assistant":
                    role = MessageParam.Role.ASSISTANT;
                    break;
            }
            messageParamList.add(MessageParam.builder().role(role).content(messageFromSession.getMessage()).build());
        }

        /**
         * After the first question (request N°3), generate a title to retrieve conversation
         */
        if (messageParamList.size()==3){
            StringBuilder getTitle = new StringBuilder();
            getTitle.append("find a title for this request in 5 words maximum : ");
            getTitle.append(newQuestion);
            List<MessageParam> messageParamTitle = new ArrayList<MessageParam>();
            messageParamTitle.add(MessageParam.builder().role(MessageParam.Role.USER).content(getTitle.toString()).build());
            String title = CreateStreamAndGetResponse(messageParamTitle, websocketSession, "title", false);
            aiSessionManager.updateTitle(user, sessionID, title);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> message = new HashMap<>();
            message.put("type", "title");
            message.put("title", title);
            message.put("sessionID", sessionID);
            try {
                websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Call AI and store answer
         */
        aiSessionManager.addMessage(user, sessionID, MessageParam.Role.ASSISTANT.toString(), CreateStreamAndGetResponse(messageParamList, websocketSession, "chat", true));
    }

    /**
     * Build call to Anthropic, submit question and build answer to return it. Response can be streamed also through websocket
     * @param messageParamList
     * @param websocketSession
     * @param type
     * @param streamedResponse
     * @return
     */
    private String CreateStreamAndGetResponse(List<MessageParam> messageParamList, WebSocketSession websocketSession, String type, boolean streamedResponse){

        String apikey = parameterService.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
        String defaultModel = parameterService.getParameterStringByKey("cerberus_anthropic_defaultmodel", "", "claude-3-5-sonnet-latest");
        Integer maxToken = parameterService.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 1024);
        StringBuilder fullResponse = new StringBuilder();

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(apikey)
                .build();

        /**
         * Build Call
         */
        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_7_SONNET_20250219)
                .maxTokens(maxToken)
                .messages(messageParamList)
                .build();

        try (StreamResponse<RawMessageStreamEvent> streamResponse =
                     client.messages().createStreaming(createParams)) {


            streamResponse.stream()
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                    .forEach(textDelta -> {
                        String text = textDelta.text();
                        fullResponse.append(text);
                        try {
                            if (streamedResponse) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> message = new HashMap<>();
                                message.put("type", type);
                                message.put("data", text);
                                websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }
        return fullResponse.toString();
    }


    public String generateTestCaseProposal(String user, WebSocketSession websocketSession, String session,  String featureDescription, String application, String testFolderId) throws JsonProcessingException {
        String prompt = testCaseCreationGeneratePromptAI.buildPromptForTestcase(testFolderId,featureDescription);

        String apikey = parameterService.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
        String modelString = parameterService.getParameterStringByKey("cerberus_anthropic_defaultmodel", "", "claude-3-5-sonnet-latest");
        Integer maxToken = parameterService.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 1024);

        double priceInputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_input_per_million", "", 3.0);
        double priceOutputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_output_per_million", "", 12.0);

        AnthropicClient anthropicClient = AnthropicOkHttpClient.builder()
                .apiKey(apikey)
                .build();

        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());


        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5_20250929)
                .maxTokens(maxToken)
                .messages(messageParamList)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder fullResponse = new StringBuilder();
        StringBuilder unitResponse = new StringBuilder();
        StringBuilder buffer = new StringBuilder();

        MessageAccumulator messageAccumulator = MessageAccumulator.create();

        try (StreamResponse<RawMessageStreamEvent> streamResponse =
                     anthropicClient.messages().createStreaming(createParams)) {

            streamResponse.stream()
                    .peek(messageAccumulator::accumulate)
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                    .forEach(textDelta -> {
                        buffer.append(textDelta.text());
                        fullResponse.append(textDelta.text());

                        // Split per lines and empty buffer for next block
                        String[] lines = buffer.toString().split("\\r?\\n");
                        buffer.setLength(0);

                        for (String line : lines) {

                            unitResponse.append(line);

                            long openCount = unitResponse.chars().filter(ch -> ch == '{').count();
                            long closeCount = unitResponse.chars().filter(ch -> ch == '}').count();

                            if (openCount > 0 && openCount == closeCount) {
                                try {
                                    String cleanedJson = sanitizeJson(unitResponse.toString());
                                    LOG.debug("TestcaseGenerated : "+cleanedJson);

                                    JsonNode json = mapper.readTree(cleanedJson);
                                    if (json.isObject()) {
                                        Map<String, Object> message = new HashMap<>();
                                        message.put("type", "testcaseCreated");
                                        message.put("sessionID", session);
                                        message.put("application", application);
                                        message.put("testFolder", testFolderId);
                                        message.put("data", json);
                                        websocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                                        unitResponse.setLength(0);
                                    }
                                } catch (Exception e) {
                                    LOG.error("Error generating TestCase :", e);
                                }
                            } else {

                            }
                        }
                    });

            //Log AI Usage
            long inTokens = messageAccumulator.message().usage().inputTokens();
            long outTokens = messageAccumulator.message().usage().outputTokens();
            double cost = 0.0;
            if (priceInputPerMillion > 0 || priceOutputPerMillion > 0) {
                cost = (inTokens / 1_000_000.0) * priceInputPerMillion
                        + (outTokens / 1_000_000.0) * priceOutputPerMillion;
            }
            LogAIUsage logAI = LogAIUsage.builder().sessionId(session).model(modelString).prompt(prompt).inputTokens((int) inTokens).outputTokens((int) outTokens)
                    .cost(cost).usrCreated(user).dateCreated(new Timestamp(System.currentTimeMillis())).build();
            logAIUsageService.create(logAI);

            websocketSession.sendMessage(new TextMessage("{\"type\":\"done\",\"message\":\"Generation complete\"}"));

        } catch (Exception e) {
            LOG.error("Error streaming AI response: ", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
            } catch (IOException ignored) {}

            try {
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }

        return fullResponse.toString();
    }

    public String createTestCaseAndGenerateContent(String user, WebSocketSession websocketSession, String session, String testFolder,  String testCaseJsonObject, String detailedDescription, String tempId) throws JsonProcessingException {

        //Get Testcase Ids existing for specific folder
        Map<String, List<String>> individualSearch = new HashMap<>();
        individualSearch.put("tec.test", List.of(testFolder));
        List<String> existingTestcases = testCaseService.readDistinctValuesByCriteria(null, testFolder, null, individualSearch, "tec.testcase").getDataList();
        LOG.info("Existing TestcaseID : " + existingTestcases.toString());

        //Generate Prompt to Guess the next ID
        String promptNextTestCaseID = testCaseCreationGeneratePromptAI.buildPromptForNextTestcaseIdGeneration(String.join(",", existingTestcases));
        LOG.debug(promptNextTestCaseID);

        //Call To get next ID
        String testcaseId = this.getNextTestcaseId(promptNextTestCaseID, session, user);
        LOG.info("Next TestcaseID proposal : " + testcaseId);

        //Create Test
        TestCase testCase = null;
        try {
            testCase = testCaseGenerationPromptAI.createTestCaseFromAiResponse(testCaseJsonObject, testcaseId, user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Build prompt
        String prompt = testCaseCreationGeneratePromptAI.buildPromptForSteps(detailedDescription);
        LOG.debug(prompt);

        String apikey = parameterService.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
        String modelString = parameterService.getParameterStringByKey("cerberus_anthropic_defaultmodel", "", "claude-3-5-sonnet-latest");
        Integer maxToken = parameterService.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 1024);

        double priceInputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_input_per_million", "", 3.0);
        double priceOutputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_output_per_million", "", 12.0);

        AnthropicClient anthropicClient = AnthropicOkHttpClient.builder()
                .apiKey(apikey)
                .build();

        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());


        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5_20250929)
                .maxTokens(maxToken)
                .messages(messageParamList)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder fullResponse = new StringBuilder();
        StringBuilder unitResponse = new StringBuilder();
        StringBuilder buffer = new StringBuilder();

        MessageAccumulator messageAccumulator = MessageAccumulator.create();

        try (StreamResponse<RawMessageStreamEvent> streamResponse =
                     anthropicClient.messages().createStreaming(createParams)) {

            TestCase finalTestCase = testCase;
            streamResponse.stream()
                    .peek(messageAccumulator::accumulate)
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                    .forEach(textDelta -> {
                        buffer.append(textDelta.text());
                        fullResponse.append(textDelta.text());

                        // Split par lignes pour ne pas perdre des JSON complets
                        String[] lines = buffer.toString().split("\\r?\\n");
                        buffer.setLength(0); // vide buffer pour stocker ligne incomplète

                        for (String line : lines) {

                            unitResponse.append(line);

                            long openCount = unitResponse.chars().filter(ch -> ch == '{').count();
                            long closeCount = unitResponse.chars().filter(ch -> ch == '}').count();

                            if (openCount > 0 && openCount == closeCount) {
                                try {
                                    String cleanedJson = sanitizeJson(unitResponse.toString());
                                    LOG.info("Step : "+cleanedJson);

                                    JsonNode json = mapper.readTree(cleanedJson);
                                    if (json.isObject()) {
                                        testCaseGenerationPromptAI.createTestCaseStepFromAiResponse(json.toString(), finalTestCase);
                                        //todo websocket
                                        unitResponse.setLength(0);
                                    }
                                } catch (Exception e) {
                                    LOG.error("Error creating step :", e);
                                }
                            } else {

                            }
                        }
                    });


            //Log AI Usage
            long inTokens = messageAccumulator.message().usage().inputTokens();
            long outTokens = messageAccumulator.message().usage().outputTokens();
            double cost = 0.0;
            if (priceInputPerMillion > 0 || priceOutputPerMillion > 0) {
                cost = (inTokens / 1_000_000.0) * priceInputPerMillion
                        + (outTokens / 1_000_000.0) * priceOutputPerMillion;
            }
            LogAIUsage logAI = LogAIUsage.builder().sessionId(session).model(modelString).prompt(prompt).inputTokens((int) inTokens).outputTokens((int) outTokens)
                    .cost(cost).usrCreated(user).dateCreated(new Timestamp(System.currentTimeMillis())).build();
            logAIUsageService.create(logAI);

            websocketSession.sendMessage(new TextMessage("{\"type\":\"test_created_ack\",\"tempId\":\""+tempId+"\",\"test\":\""+finalTestCase.getTest()+"\",\"testcase\":\""+finalTestCase.getTestcase()+"\"}"));

        } catch (Exception e) {
            LOG.error("Error streaming AI response: ", e);
            try {
                websocketSession.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
            } catch (IOException ignored) {}

            try {
                websocketSession.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {}
        }

        return fullResponse.toString();
    }

    public String getNextTestcaseId(String prompt, String sessionId, String user){
        String apikey = parameterService.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
        String modelString = parameterService.getParameterStringByKey("cerberus_anthropic_defaultmodel", "", "claude-3-5-sonnet-latest");
        Integer maxToken = parameterService.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 256);

        double priceInputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_input_per_million", "", 3.0);
        double priceOutputPerMillion = parameterService.getParameterDoubleByKey("cerberus_anthropic_price_output_per_million", "", 12.0);

        AnthropicClient anthropicClient = AnthropicOkHttpClient.builder()
                .apiKey(apikey)
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5_20250929)
                .maxTokens(maxToken)
                .messages(List.of(
                        MessageParam.builder()
                                .role(MessageParam.Role.USER)
                                .content(prompt)
                                .build()
                ))
                .build();

        // SYNCHRONE (No streaming)
        Message response = anthropicClient.messages().create(params);

        //Log AI Usage
        long inTokens = response.usage().inputTokens();
        long outTokens = response.usage().outputTokens();
        double cost = 0.0;
        if (priceInputPerMillion > 0 || priceOutputPerMillion > 0) {
            cost = (inTokens / 1_000_000.0) * priceInputPerMillion
                    + (outTokens / 1_000_000.0) * priceOutputPerMillion;
        }
        LogAIUsage logAI = LogAIUsage.builder().sessionId(sessionId).model(modelString).prompt(prompt).inputTokens((int) inTokens).outputTokens((int) outTokens)
                .cost(cost).usrCreated(user).dateCreated(new Timestamp(System.currentTimeMillis())).build();
        logAIUsageService.create(logAI);

        //Get content (text only)
        String result = response.content().stream()
                .map(block ->
                        block.text()
                                .map(tb -> {
                                    String txt = tb.text();
                                    return txt == null ? "" : txt;
                                })
                                .orElse("")
                )
                .collect(Collectors.joining())
                .trim();

        LOG.debug("Response AI = " + result);

        return result;
    }


    private static String sanitizeJson(String raw) {

        if (raw == null) {
            return "";
        }

        String cleaned = raw
                .replaceAll("(?i)```json", "")
                .replaceAll("```", "")
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();

        cleaned = cleaned.replaceAll("^`+", "").replaceAll("`+$", "").trim();

        return cleaned;
    }
}