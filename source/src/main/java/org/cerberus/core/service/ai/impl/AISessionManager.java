/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageParam;
import org.cerberus.core.crud.dao.IUserPromptDAO;
import org.cerberus.core.crud.dao.IUserPromptMessageDAO;
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AISessionManager {

    private static final Logger LOG = LogManager.getLogger(AISessionManager.class);

    @Autowired
    IUserPromptDAO iUserPromptDAO;
    @Autowired
    IUserPromptMessageDAO iUserPromptMessageDAO;
    @Autowired
    AIConfig aiConfig;


    public UserPrompt initSession(String login, String session, String type) {

        UserPrompt userPrompt = iUserPromptDAO.readByUserSessionID(login, session).getItem();
        LOG.debug("UserPrompt: " + userPrompt);

        if (userPrompt == null){
            userPrompt = UserPrompt.builder()
                    .login(login).sessionID(session).iaModel(aiConfig.modelName()).iaMaxTokens(aiConfig.maxTokens()).title("")
                    .totalCalls(0).totalInputTokens(0).totalOutputTokens(0).totalCost(0.0).type(type).usrCreated(login)
                    .build();

            iUserPromptDAO.create(userPrompt);
        }
        return userPrompt;
    }

    public static final String SESSION_TYPE_ONBOARDING = "onboarding";

    /**
     * Starts the welcome conversation of the AI assistant for a user who never used it.
     * <p>
     * Only when the MCP is activated (cerberus_ai_use_mcp + cerberus_ai_mcp_host). "First login" is defined as "no AI session yet" : the session created here is
     * what makes the next call return null, so the welcome is proposed once, whatever the
     * browser. The session is created empty on purpose : the welcome text is displayed by the
     * UI, and no assistant message is stored so the history later sent to the model still
     * starts with a user message.
     *
     * @param login login of the user
     * @return the sessionID of the new onboarding session, or null when there is nothing to start
     */
    public String startOnboardingIfFirstTime(String login) {
        // The welcome paths (first test, external AI) rely on the MCP tools : same condition as AIService.
        if (login == null || !aiConfig.useMcp() || aiConfig.mcpHost() == null || aiConfig.mcpHost().isBlank()) {
            return null;
        }
        // -1 (read error) must not be mistaken for "no session" : we would greet the user again.
        if (iUserPromptDAO.countByUser(login) != 0) {
            return null;
        }
        String sessionID = UUID.randomUUID().toString();
        initSession(login, sessionID, SESSION_TYPE_ONBOARDING);
        updateTitle(login, sessionID, "Bienvenue");
        LOG.info("AI onboarding started - login : {} - session : {}", login, sessionID);
        return sessionID;
    }

    /**
     *
     * @param login
     * @param aiSessionID
     * @param prompt
     * @param response
     * @param message
     * @return
     */
    public UserPrompt saveMessage(String login, String aiSessionID, String prompt, String response, Message message, String type, long in, long out) {

        in = in == 0 ? message.usage().inputTokens() : in;
        out = out == 0 ? message.usage().outputTokens() : out;
        double costIn = (in / 1_000_000.0) * aiConfig.priceInput();
        double costOut = (out / 1_000_000.0) * aiConfig.priceOutput();

        LOG.info("Init Session - login : "+login+" - session : "+aiSessionID);
        initSession(login, aiSessionID, type);

        /*
        Insert Message
         */
        UserPromptMessage upmToInsert = UserPromptMessage.builder()
                .sessionID(aiSessionID).role(MessageParam.Role.USER.toString()).message(prompt).tokens((int) in).cost(costIn).usrCreated(login)
                .build();
        iUserPromptMessageDAO.create(upmToInsert);

        /*
        Insert Response
         */
        upmToInsert = UserPromptMessage.builder()
                .sessionID(aiSessionID).role(MessageParam.Role.ASSISTANT.toString()).message(response).tokens((int) out).cost(costOut).usrCreated(login)
                .build();
        iUserPromptMessageDAO.create(upmToInsert);

        /*
        Increment Usage
         */
        iUserPromptDAO.incrementUsage(login, aiSessionID, (int) in, (int) out, costIn + costOut);

        return iUserPromptDAO.readByUserSessionID(login, aiSessionID).getItem();
    }

    public List<UserPromptMessage> getAllMessages(String aiSession) {
        List<UserPromptMessage> upm = iUserPromptMessageDAO.readBySessionID(aiSession).getDataList();
        return upm;
    }

    public List<MessageParam> getMessageParamListOfSession(String sessionID) {

        List<MessageParam> messageParamList = new ArrayList<>();
        List<UserPromptMessage> messageListFromSession = getAllMessages(sessionID);

        for (UserPromptMessage messageFromSession : messageListFromSession) {
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
        return messageParamList;

    }

    public JSONArray getAllPromptByUser(String user) {

        List<UserPrompt> upList = Optional.ofNullable(iUserPromptDAO.readByUser(user).getDataList())
                .orElse(Collections.emptyList());

        JSONArray result = new JSONArray();

        for (UserPrompt up : upList) {
            result.put(up.toJSON());
        }
        return result;
    }

    public JSONArray getAllMessagesFromPrompt(String sessionID) {
        List<UserPromptMessage> upmList = iUserPromptMessageDAO.readBySessionID(sessionID).getDataList();
        JSONArray result = new JSONArray();
        for (UserPromptMessage upm : upmList) {
            result.put(upm.toJSON());
        }
        return result;
    }

    public void updateTitle(String user, String session, String title) {
        LOG.info("updateTitle - User:" + user + " session:" + session + " title:" + title);
        UserPrompt up = iUserPromptDAO.readByUserSessionID(user, session).getItem();
        LOG.info(iUserPromptDAO.readByUserSessionID(user, session).getResultMessage().getDescription());
        up.setTitle(title);
        up.setUsrModif(user);
        up.setDateModif(new Timestamp(System.currentTimeMillis()));
        iUserPromptDAO.update(up);
    }

}