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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.service.impl.*;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.ai.IAIService;
import org.cerberus.core.service.ai.impl.parsing.ApplicationObjectFromAI;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
public class AIService implements IAIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AIService.class);

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
    TestCaseStepService testCaseStepService;
    @Autowired
    AIClientService aiClientService;
    @Autowired
    AIConfig aiConfig;
    @Autowired
    AIMcpClientService aiMcpClientService;
    @Autowired
    private WebSocketEventSender webSocketEventSender;
    @Autowired
    private AIToolResultHandler aIToolResultHandler;

    private static final int MAX_TOOL_ITERATIONS = 20;

    private TestCaseStepAction lastAction;


    @Override
    public void chatWithAI(String user, String aiSessionID, String newMessage) {

        //Generate aiSessionID if not exists
        final String currentAiSessionID = aiSessionID == null || aiSessionID.isBlank()
                        ? UUID.randomUUID().toString() : aiSessionID;

        //clear suggestions
        aIToolResultHandler.clearSuggestions(currentAiSessionID);

        //Get all message of session and add the new message
        List<MessageParam> messageParamList = aiSessionManager.getMessageParamListOfSession(currentAiSessionID);
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(newMessage).build());

        // Usage already persisted for this session before this exchange — the header must show
        // the session's cumulative totals, not just this exchange's own tokens, while streaming.
        UserPrompt sessionUsage = aiSessionManager.initSession(user, currentAiSessionID, "chatWithAI");
        long baselineInputTokens = sessionUsage.getTotalInputTokens() == null ? 0 : sessionUsage.getTotalInputTokens();
        long baselineOutputTokens = sessionUsage.getTotalOutputTokens() == null ? 0 : sessionUsage.getTotalOutputTokens();
        long baselineCalls = sessionUsage.getTotalCalls() == null ? 0 : sessionUsage.getTotalCalls();
        double baselineCost = sessionUsage.getTotalCost();

        //For the first question (request N°1), generate a title to retrieve conversation
        if (messageParamList.size() == 1) {
            generateTitleForSession(newMessage, user, currentAiSessionID);
        }

        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        //If MCP is enabled, open a client to the configured server and expose its tools to Claude.
        McpSyncClient mcpClient = null;
        List<Tool> tools = new ArrayList<>();
        // AtomicLong so the running totals can be read from the streaming callback below,
        // which needs an effectively-final reference even though the totals keep growing.
        AtomicLong totalInputTokens = new AtomicLong(0);
        AtomicLong totalOutputTokens = new AtomicLong(0);

        try {
            if (aiConfig.useMcp() && aiConfig.mcpHost() != null && !aiConfig.mcpHost().isBlank()) {
                // Reused across messages of this session (see AIMcpClientService) instead of
                // reopening a connection + re-running the MCP handshake on every single message.
                mcpClient = aiMcpClientService.getOrOpenSessionClient(currentAiSessionID);
                tools = new ArrayList<>(aiMcpClientService.getSessionTools(currentAiSessionID));
                LOG.debug("MCP enabled for chat session {} — {} tools available", currentAiSessionID, tools.size());
            }
            // Always available, regardless of MCP: lets Claude offer quick-reply buttons
            // phrased in the conversation's own language instead of hardcoding them in tools.
            tools.add(aiMcpClientService.buildQuickReplyTool());

            MessageAccumulator messageAccumulator = null;
            //Agentic loop : stream a turn, and while Claude requests tool calls,
            //execute them through MCP and feed the results back for the next turn.
            for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {

                messageAccumulator = aiClientService.streamResponseAndAccumulate(
                        messageParamList, aiBuildPrompt.buildSystemContextForChatWithAI(), tools,
                        text -> {
                            fullResponse.append(text);
                            try {
                                // Stream response : WebSocket type=chat.delta, channel=ai
                                // Session-cumulative totals (baseline + this exchange so far), matching
                                // the field names read by the frontend header (usage.total*). totalCalls
                                // only bumps once this exchange is saved, on chat.done.
                                webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_CHAT_DELTA,
                                        Map.of("done", false, "data", text,
                                                "totalInputTokens", baselineInputTokens + totalInputTokens.get(),
                                                "totalOutputTokens", baselineOutputTokens + totalOutputTokens.get(),
                                                "totalCalls", baselineCalls,
                                                "totalCost", baselineCost + costOf(totalInputTokens.get(), totalOutputTokens.get())));
                            } catch (Exception e) {
                                LOG.error("Error during streaming callback:", e);
                                streamingErrors.add(e.getMessage());
                            }
                        }
                );

                Message response = messageAccumulator.message();

                //Increment Usage
                if (response.usage().isValid()) {
                    Usage usage = response.usage();
                    totalInputTokens.addAndGet(usage.inputTokens());
                    totalOutputTokens.addAndGet(usage.outputTokens());
                    totalInputTokens.addAndGet(usage.cacheCreationInputTokens().orElse(0L));
                    totalInputTokens.addAndGet(usage.cacheReadInputTokens().orElse(0L));
                }

                // Replay Claude's turn (text + tool_use blocks) in the conversation history.
                // A tool called with no arguments can come back with a missing (not empty) input,
                // which the API rejects on the next turn ("tool_use.input: Field required") — default it to {}.
                messageParamList.add(MessageParam.builder()
                        .role(MessageParam.Role.ASSISTANT)
                        .contentOfBlockParams(response.content().stream()
                                .map(this::normalizeToolUseInput)
                                .map(ContentBlock::toParam)
                                .collect(Collectors.toList()))
                        .build());

                // Note: the quick-reply pseudo-tool is always usable, even without MCP enabled.
                boolean wantsTool = response.stopReason().isPresent()
                        && response.stopReason().get().equals(StopReason.TOOL_USE);
                if (!wantsTool) {
                    break;
                }

                // Execute every requested tool and gather the tool_result blocks.
                List<ContentBlockParam> toolResults = new ArrayList<>();
                var currentToolUsed = "";
                for (ContentBlock block : response.content()) {
                    if (block.toolUse().isEmpty()) {
                        continue;
                    }

                    ToolUseBlock toolUse = block.toolUse().get();
                    currentToolUsed = toolUse.name();

                    //Send tool start through Websocket
                    webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_TOOL_START,
                            Map.of("toolName", currentToolUsed));

                    String toolResult;
                    boolean isError = false;

                    try {
                        // A tool with no required parameters can be called with a missing (not
                        // just empty) input — convert() would throw on that JsonMissing sentinel.
                        Map<String, Object> arguments = toolUse._input().isMissing()
                                ? new HashMap<>()
                                : toolUse._input().convert(new TypeReference<Map<String, Object>>() {});

                        if (AIMcpClientService.QUICK_REPLY_TOOL_NAME.equals(currentToolUsed)) {
                            // Client-side-only pseudo-tool: never forwarded to the MCP server.
                            toolResult = aIToolResultHandler.handleQuickReplies(currentAiSessionID, currentToolUsed, arguments);
                        } else {
                            //Call tool
                            arguments.put("_context", Map.of("source", "GUI","user", user,"appSessionID", currentAiSessionID,"channel", WebSocketStatic.CHANNEL_CHAT_DELTA));
                            toolResult = aiMcpClientService.callTool(mcpClient, toolUse.name(), arguments);

                            //Send result through Websocket
                            webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_TOOL_RESULT,
                                    Map.of("toolName", currentToolUsed, "data", toolResult));
                        }

                    } catch (Exception ex) {
                        LOG.error("MCP tool call '{}' failed:", toolUse.name(), ex);
                        toolResult = "Tool execution failed: " + ex.getMessage();
                        isError = true;

                        //Send tool error through Websocket
                        webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_TOOL_ERROR,
                                Map.of("toolName", currentToolUsed, "error", ex.toString()));
                    }

                    toolResults.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder().toolUseId(toolUse.id()).content(toolResult).isError(isError).build()));
                }

                //Send tool end through Websocket
                webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_TOOL_DONE,
                        Map.of("toolName", currentToolUsed));

                messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).contentOfBlockParams(toolResults).build());
            }

            //store message and answer
            LOG.debug("New message from :"+user+" in session :"+currentAiSessionID+" - message : "+newMessage);
            LOG.debug("New message from :"+user+" in session :"+currentAiSessionID+" - answer : "+fullResponse.toString());
            UserPrompt updatedUsage = aiSessionManager.saveMessage(user, currentAiSessionID, newMessage, fullResponse.toString(), messageAccumulator.message(), "chatWithAI", totalInputTokens.get(), totalOutputTokens.get());

            // Notify WebSocket type=chat.done — authoritative post-save totals, straight from the DB.
            webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_CHAT_DONE,
                    Map.of("done", true, "data", "",
                            "totalInputTokens", updatedUsage.getTotalInputTokens(),
                            "totalOutputTokens", updatedUsage.getTotalOutputTokens(),
                            "totalCalls", updatedUsage.getTotalCalls(),
                            "totalCost", updatedUsage.getTotalCost()));

        } catch (Exception e) {
            LOG.error("Error during chatWithAI:", e);
            // Discard the cached MCP client : it may be the cause of, or left in a bad state by,
            // the failure — the next message opens a fresh one instead of reusing it.
            if (mcpClient != null) {
                aiMcpClientService.invalidateSessionClient(currentAiSessionID);
            }
            // Notify WebSocket type=chat.error, channel=ai
            webSocketEventSender.sendToAppSession(currentAiSessionID, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
        }
    }

    /**
     * A tool called with no arguments can come back from the streaming accumulator with a
     * missing (not empty) input, which the Anthropic API rejects if replayed as-is on the next
     * turn ("tool_use.input: Field required"). Defaults it to an empty object in that case.
     */
    private ContentBlock normalizeToolUseInput(ContentBlock block) {
        if (block.toolUse().isPresent() && block.toolUse().get()._input().isMissing()) {
            ToolUseBlock fixed = block.toolUse().get().toBuilder()
                    .input(JsonValue.from(Map.of()))
                    .build();
            return ContentBlock.ofToolUse(fixed);
        }
        return block;
    }

    /**
     * Cost of this exchange so far, matching the pricing formula used to persist usage
     * in {@link AISessionManager#saveMessage}.
     */
    private double costOf(long inputTokens, long outputTokens) {
        return (inputTokens / 1_000_000.0) * aiConfig.priceInput() + (outputTokens / 1_000_000.0) * aiConfig.priceOutput();
    }


    /**
     * Generate Session Title, notify user and insert into database.
     */
    private void generateTitleForSession(String newMessage, String user, String aiSessionID) {
        String title = aiBuildPrompt.buildPromptForSessionTitle(newMessage);

        List<MessageParam> messageParamTitle = new ArrayList<>();
        messageParamTitle.add(MessageParam.builder().role(MessageParam.Role.USER).content(title).build());
        StringBuilder fullTitle = new StringBuilder();
        aiClientService.streamResponseAndAccumulate(messageParamTitle, null, text -> {
            fullTitle.append(text);
            try {
                // The frontend replaces the displayed title wholesale on each event (it doesn't
                // append), so it must receive the accumulated title so far under "title", not
                // just the latest chunk under "data".
                webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_TITLE,
                        Map.of("title", fullTitle.toString()));
            } catch (Exception e) {
                LOG.error("Error during streaming callback:", e);
            }
        });
        LOG.debug(fullTitle.toString());
        aiSessionManager.updateTitle(user, aiSessionID, fullTitle.toString());
    }


    public void generateTestCaseProposal(String user, String session, String featureDescription, String application, String testFolderId) throws JsonProcessingException {

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
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("sessionID", session);
                        payload.put("application", application);
                        payload.put("testFolder", testFolderId);
                        payload.put("data", json);
                        fullResponse.append(mapper.writeValueAsString(payload));
                        webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_TESTCASE_PROPOSALS, payload);
                    }
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            // Log AI usage
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCaseProposal", 0, 0);

            //update Title
            aiSessionManager.updateTitle(user, session, featureDescription);

            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_DONE, Map.of("message", "Generation complete"));

            if (!streamingErrors.isEmpty()) {
                webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", "Errors occurred during streaming callback : " + streamingErrors));
            }

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
        }
    }


    public void createTestCaseAndGenerateContent(String user, String session, String testFolder, String testCaseJsonObject, String detailedDescription, String tempId) throws JsonProcessingException {

        //Get Testcase Ids existing for specific folder
        Map<String, List<String>> individualSearch = new HashMap<>();
        individualSearch.put("tec.test", List.of(testFolder));
        List<String> existingTestcases = testCaseService.readDistinctValuesByCriteria(null, testFolder, null, individualSearch, "tec.testcase").getDataList();
        LOG.info("Existing TestcaseID : " + existingTestcases);

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
            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_OBJECTCREATION_TESTCASE, Map.of("tempId", tempId));
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
                    LOG.debug("Step : " + cleanedJson);

                    JsonNode json = mapper.readTree(cleanedJson);
                    if (json.isObject()) {
                        TestCaseStep testCaseStep = objectFromAiResponse.createTestCaseStep(json.toString(), finalTestCase);
                        this.createTestCaseStepActionAndControlAndGenerateContent(user, session, testCaseStep, finalTestCase.getDetailedDescription(), json.get("promptForActionDefinition").toString());
                        fullResponse.append(mapper.writeValueAsString(testCaseStep));
                        webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_OBJECTCREATION_TESTCASESTEP, Map.of("tempId", tempId));
                    }
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            // Log AI usage
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCase", 0, 0);
            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_OBJECTCREATION_TESTCASE, Map.of("tempId", tempId, "test", finalTestCase.getTest(), "testcase", finalTestCase.getTestcase()));

            if (!streamingErrors.isEmpty()) {
                webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", "Errors occurred during streaming callback : " + streamingErrors));
            }

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
        }
    }

    private String getNextTestcaseId(String prompt, String sessionId, String user) {

        Message response = aiClientService.getSyncMessage(prompt);

        String result = response.content().stream()
                .map(block -> block
                        .text()
                        .map(tb -> {
                            String txt = tb.text();
                            return txt == null ? "" : txt;
                        })
                        .orElse(""))
                .collect(Collectors.joining())
                .trim();

        LOG.debug("Response AI = " + result);

        //Log AI Usage
        aiSessionManager.saveMessage(user, sessionId, prompt, result, response, "generateTestCase", 0, 0);

        return result;
    }


    private void createTestCaseStepActionAndControlAndGenerateContent(String user, String session, TestCaseStep testCaseStep, String testCaseDescription, String promptForActionDefinition) throws JsonProcessingException {

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
                            LOG.debug("Detected ACTION: " + actionNode.asText());
                            lastAction = objectFromAiResponse.createTestCaseStepAction(json.toString(), testCaseStep);
                            fullResponse.append(mapper.writeValueAsString(lastAction));
                        } else if (controlNode != null && !controlNode.isNull()) {
                            if (lastAction == null) {
                                LOG.warn("Received CONTROL without a previous ACTION, skipping...");
                            } else {
                                LOG.debug("Detected CONTROL: " + controlNode.asText());
                                TestCaseStepActionControl tcsac = objectFromAiResponse.createTestCaseStepActionControl(json.toString(), lastAction);
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
            aiSessionManager.saveMessage(user, session, prompt, fullResponse.toString(), messageAccumulator.message(), "generateTestCase", 0, 0);

        } catch (Exception e) {
            LOG.error("Error generating test case proposal:", e);
            webSocketEventSender.sendToAppSession(session, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
        }
    }


    public void executionDebugWithAI(String user, String aiSessionID, long executionId) {

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

        List<MessageParam> messageParamList = new ArrayList<>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());

        List<String> streamingErrors = new CopyOnWriteArrayList<>();
        StringBuilder fullResponse = new StringBuilder();
        try {
            MessageAccumulator messageAccumulator = aiClientService.streamResponseAndAccumulate(messageParamList, null, text -> {
                fullResponse.append(text);
                try {
                    webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_DELTA, Map.of("data", text));
                } catch (Exception e) {
                    LOG.error("Error during streaming callback:", e);
                    streamingErrors.add(e.getMessage());
                }
            });

            aiSessionManager.saveMessage(user, aiSessionID, prompt, fullResponse.toString(), messageAccumulator.message(), "self_healing_explain", 0, 0);

        } catch (Exception e) {
            LOG.error("Error during executionDebugWithAI:", e);
            webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
        }
    }

    public void generateApplicationObjectProposalWithAI(String user, String aiSessionID, String applicationId, String pageName, String htmlPath, String screenshotPath, String prompt, String subject) {

        /*
        Retreive existing Application Objects
         */
        List<ApplicationObject> aoList = applicationObjectService.readByApplication(applicationId).getDataList();


        /*
        Get Picture and HTML
         */
        String screenshotBase64 = "";
        String htmlContent = "";
        try {
            byte[] screenshotBytes = Files.readAllBytes(Paths.get(screenshotPath));
            screenshotBase64 = Base64.getEncoder().encodeToString(screenshotBytes);
            htmlContent = Files.readString(Paths.get(htmlPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String screenshotBase64Final = screenshotBase64;

        /*
        Build prompt and context. For first call, generate prompt
         */
        String aoPrompt = prompt;
        if (WebSocketStatic.CHANNEL_AO_GENERATE_REQUEST.equals(subject)) {
            aoPrompt = aiBuildPrompt.buildPromptForApplicationObjectGeneration(applicationId, aoList, pageName, prompt);
        }
        LOG.debug(aoPrompt);
        String systemContext = aiBuildPrompt.buildPromptForApplicationObjectSystemContext();
        LOG.debug(systemContext);

        List<ContentBlockParam> contentOfBlockParams = new ArrayList<>();
        // Image block
        @SuppressWarnings("unchecked")
        ImageBlockParam imgBlock = ImageBlockParam.builder()
                .type(JsonValue.from("image"))
                .source(Base64ImageSource.builder()
                        .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                        .data(screenshotBase64)
                        .build())
                .build();
        contentOfBlockParams.add(ContentBlockParam.ofImage(imgBlock));

        @SuppressWarnings("unchecked")
        DocumentBlockParam docBlock = DocumentBlockParam.builder()
                .type(JsonValue.from("document"))
                .source(JsonValue.from(Map.of(
                        "type", "text",
                        "media_type", "text/plain",
                        "data", removeScriptsAndStyles(htmlContent)
                )))
                .build();
        contentOfBlockParams.add(ContentBlockParam.ofDocument(docBlock));

        /**
         * Get all message of session and add the new message
         */
        List<MessageParam> messageParamList = aiSessionManager.getMessageParamListOfSession(aiSessionID);
        MessageParam.Builder mpBuilder = MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(aoPrompt);
        if (WebSocketStatic.CHANNEL_AO_GENERATE_REQUEST.equals(subject)) {
            mpBuilder.contentOfBlockParams(contentOfBlockParams);
        }

        MessageParam msg = mpBuilder.build();
        messageParamList.add(msg);

        StringBuilder fullResponse = new StringBuilder();
        try {
            StringBuilder textBuffer = new StringBuilder();
            AtomicBoolean insideJsonBlock = new AtomicBoolean(false);

            MessageAccumulator messageAccumulator = aiClientService.streamResponseAndAccumulate(messageParamList, systemContext, chunk -> {

                textBuffer.append(chunk);
                String current = textBuffer.toString();

                while (true) {

                    if (!insideJsonBlock.get()) {
                        int start = current.indexOf("```json");
                        if (start >= 0) {

                            String chatPart = current.substring(0, start).trim();
                            if (!chatPart.isEmpty()) {
                                webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_DELTA, Map.of("data", chatPart));
                            }

                            insideJsonBlock.set(true);
                            current = current.substring(start + 7);
                            textBuffer.setLength(0);
                            textBuffer.append(current);

                        } else {
                            if (!current.isEmpty()) {
                                webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_DELTA, Map.of("data", current));
                                textBuffer.setLength(0);
                            }
                            break;
                        }
                    } else {

                        int end = current.indexOf("```");
                        if (end >= 0) {

                            String jsonPart = current.substring(0, end).trim();

                            try {
                                String cleaned = aiClientService.sanitizeJson(jsonPart);
                                LOG.info("Cleaned JSON: " + cleaned);
                                ApplicationObject ao = applicationObjectFromAI.parseAndCropAOJson(applicationId, screenshotBase64Final, cleaned, user);
                                webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_AO_PROPOSALS,  Map.of("data", ao));
                            } catch (Exception e) {
                                LOG.error("Failed parsing streamed AO", e);
                            }

                            insideJsonBlock.set(false);
                            current = current.substring(end + 3);
                            textBuffer.setLength(0);
                            textBuffer.append(current);

                        } else {
                            break;
                        }
                    }
                }
            });
            LOG.debug(fullResponse.toString());

            /**
             * store message and answer
             */
            aiSessionManager.saveMessage(user, aiSessionID, aoPrompt, fullResponse.toString(), messageAccumulator.message(), "ao_proposals", 0, 0);

            //update Title
            aiSessionManager.updateTitle(user, aiSessionID, "Application Object Generation for page " + pageName);

        } catch (Exception e) {
            LOG.error("Error during generateApplicationObjectProposalWithAI:", e);
            webSocketEventSender.sendToAppSession(aiSessionID, WebSocketStatic.CHANNEL_CHAT_ERROR, Map.of("message", e.getMessage()));
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