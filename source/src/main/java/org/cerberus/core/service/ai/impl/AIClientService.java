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
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.RawMessageStreamEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class AIClientService {

    private static final Logger LOG = LogManager.getLogger(AIClientService.class);

    @Autowired
    AIConfig aiConfig;

    /**
     * Build AnthropicClient
     * @return AnthropicClient
     */
    private AnthropicClient buildClient() {
        return AnthropicOkHttpClient.builder()
                .apiKey(aiConfig.apiKey())
                .build();
    }

    /**
     * Build MessageCreateParams
     * @param messageParamList : List of message for the session
     * @return MessageCreateParams
     */
    private MessageCreateParams buildMessageCreateParams(List<MessageParam> messageParamList) {
        return MessageCreateParams.builder()
                .model(aiConfig.modelName())
                .maxTokens(aiConfig.maxTokens())
                .messages(messageParamList)
                .build();
    }


    /**
     *
     * @param prompt
     * @return
     */
    public Message getSyncMessage(String prompt){

        AnthropicClient anthropicClient = buildClient();

        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());
        MessageCreateParams createParams = buildMessageCreateParams(messageParamList);

        return anthropicClient.messages().create(createParams);
    }


    public MessageAccumulator streamResponseAndAccumulate(List<MessageParam> messageParamList, Consumer<String> onToken) {

        AnthropicClient client = buildClient();
        MessageCreateParams createParams = buildMessageCreateParams(messageParamList);
        MessageAccumulator messageAccumulator = MessageAccumulator.create();

        try (StreamResponse<RawMessageStreamEvent> stream =
                     client.messages().createStreaming(createParams)) {

            stream.stream()
                    .peek(messageAccumulator::accumulate)
                    .flatMap(e -> e.contentBlockDelta().stream())
                    .flatMap(delta -> delta.delta().text().stream())
                    .forEach(t -> onToken.accept(t.text()));

        } catch (UnauthorizedException e){
            throw new RuntimeException("Authentication Error : " + e.getMessage() + ". Please check your cerberus_anthropic_apikey in the parameter.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error : " + e.getMessage(), e);
        }
        return messageAccumulator;
    }


    public MessageAccumulator streamResponseObjectAndAccumulate(String prompt,Consumer<String> onToken) {

        AnthropicClient client = buildClient();

        List<MessageParam> messageParamList = new ArrayList<MessageParam>();
        messageParamList.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());
        MessageCreateParams createParams = buildMessageCreateParams(messageParamList);

        MessageAccumulator messageAccumulator = MessageAccumulator.create();

        StringBuilder fullResponse = new StringBuilder();
        StringBuilder unitResponse = new StringBuilder();
        StringBuilder buffer = new StringBuilder();

        try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(createParams)) {

            stream.stream()
                    .peek(messageAccumulator::accumulate)
                    .flatMap(e -> e.contentBlockDelta().stream())
                    .flatMap(delta -> delta.delta().text().stream())
                    .forEach(textDelta -> {
                        buffer.append(textDelta.text());
                        fullResponse.append(textDelta.text());

                        // Split per lines
                        String[] lines = buffer.toString().split("\\r?\\n");
                        buffer.setLength(0);

                        for (String line : lines) {
                            unitResponse.append(line);

                            long openCount = unitResponse.chars().filter(ch -> ch == '{').count();
                            long closeCount = unitResponse.chars().filter(ch -> ch == '}').count();

                            if (openCount > 0 && openCount == closeCount) {
                                onToken.accept(unitResponse.toString());
                                unitResponse.setLength(0);
                            }
                        }
                    });
        } catch (UnauthorizedException e){
            throw new RuntimeException("Authentication Error : " + e.getMessage() + ". Please check your cerberus_anthropic_apikey in the parameter.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error : " + e.getMessage(), e);
        }

        return messageAccumulator;
    }


    public static String sanitizeJson(String raw) {

        /* if null return empty */
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
