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
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.service.ai.IAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class AIService implements IAIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AIService.class);
    private String sessionId;

    @Autowired
    AISessionManager aiSessionManager;
    @Autowired
    ParameterService parameterService;

    @Override
    public void askClaude(String user, WebSocketSession websocketSession, String newQuestion) {

        String apikey = parameterService.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(apikey)
                .build();

        LOG.debug("Message from :"+user+" : "+newQuestion);
        aiSessionManager.addMessage(user, websocketSession.getId(), MessageParam.Role.USER.toString(), newQuestion);

        List<UserPromptMessage> upmList = aiSessionManager.getAllMessages(user, websocketSession.getId());
        LOG.debug("messages retrieved : " + upmList.size());
        List<MessageParam> init = new ArrayList<MessageParam>();
        for (UserPromptMessage upm : upmList){
            MessageParam.Role role = MessageParam.Role.USER;
            switch (upm.getRole()) {
                case "user":
                    role = MessageParam.Role.USER;
                    break;
                case "assistant":
                    role = MessageParam.Role.ASSISTANT;
                    break;
            }
            init.add(MessageParam.builder().role(role).content(upm.getMessage()).build());
        }

        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
                .maxTokens(1024)
                .messages(init)
                .build();

        try (StreamResponse<RawMessageStreamEvent> streamResponse =
                     client.messages().createStreaming(createParams)) {

            StringBuilder fullResponse = new StringBuilder();
            streamResponse.stream()
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                    .forEach(textDelta -> {
                        String text = textDelta.text();
                        fullResponse.append(text);
                        try {
                            websocketSession.sendMessage(new TextMessage(text));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            aiSessionManager.addMessage(user, websocketSession.getId(), MessageParam.Role.ASSISTANT.toString(), fullResponse.toString());

        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }
    }
}