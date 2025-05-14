/*
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


import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;
import org.cerberus.core.crud.dao.IUserPromptDAO;
import org.cerberus.core.crud.dao.IUserPromptMessageDAO;
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.crud.service.IParameterService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

@Service
public class AISessionManager {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AISessionManager.class);

    @Autowired
    IUserPromptDAO iUserPromptDAO;
    @Autowired
    IUserPromptMessageDAO iUserPromptMessageDAO;
    @Autowired
    IParameterService parameterService;

    private HashMap<String, HashMap<String, List<MessageParam>>> listOfSessionsByUser;



    public void addMessage(String login, String session, String role, String message) {

        UserPrompt up = iUserPromptDAO.findUserPromptByUserSessionID(login, session);
        StringBuilder messageWithInitialContext = new StringBuilder();

        if (up == null){
            String iaModel = parameterService.getParameterStringByKey("cerberus_anthropic_defaultmodel", "", "claude-3-5-sonnet-latest");
            Integer iaMaxTokens = parameterService.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 1024);
            String title = "";

            UserPrompt upToInsert = UserPrompt.builder()
                    .login(login).sessionID(session).iaModel(iaModel).iaMaxTokens(iaMaxTokens).title(title).usrCreated(login)
                    .build();

            iUserPromptDAO.insertUserPrompt(upToInsert);

            messageWithInitialContext.append("I'm working in a Software development context, in a job related to Quality assurance (automation, tester).");
            messageWithInitialContext.append("The context of the question is related to Cerberus-testing, a low code testing framework.");
            messageWithInitialContext.append("Respond in HTML format, including any formatting like bold, lists, icon, and code inside proper HTML tags.The maximum font-size cannot exceed 18px. ");
        }

        messageWithInitialContext.append(message);


        UserPromptMessage upmToInsert = UserPromptMessage.builder()
                .sessionID(session).role(role).message(messageWithInitialContext.toString()).usrCreated(login)
                .build();
        iUserPromptMessageDAO.insertUserPromptMessage(upmToInsert);

    }

    public List<UserPromptMessage> getAllMessages(String user, String session){
        List<UserPromptMessage> upm = iUserPromptMessageDAO.findBySessionID(session);
        return upm;
    }

    public JSONArray getAllPromptByUser(String user){
        List<UserPrompt> upList = iUserPromptDAO.findUserPromptsByLogin(user);
        JSONArray result = new JSONArray();
        for (UserPrompt up : upList){
            result.put(up.toJSON());
        }
        return result;
    }

    public JSONArray getAllMessagesFromPrompt(String sessionID){
        List<UserPromptMessage> upmList = iUserPromptMessageDAO.findBySessionID(sessionID);
        JSONArray result = new JSONArray();
        for (UserPromptMessage upm : upmList){
            result.put(upm.toJSON());
        }
        return result;
    }

    public void updateTitle(String user, String session, String title){
        UserPrompt up = iUserPromptDAO.findUserPromptByUserSessionID(user, session);
        up.setTitle(title);
        up.setUsrModif(user);
        up.setDateModif(new Timestamp(System.currentTimeMillis()));
        iUserPromptDAO.updateUserPrompt(up);
    }

}