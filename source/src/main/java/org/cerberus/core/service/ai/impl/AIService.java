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
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.service.ai.IAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AIService implements IAIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AIService.class);
    private String sessionId;

    @Autowired
    AISessionManager aiSessionManager;
    @Autowired
    ParameterService parameterService;

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
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
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

}