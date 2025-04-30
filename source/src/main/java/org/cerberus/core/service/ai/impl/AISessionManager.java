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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AISessionManager {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AISessionManager.class);


    @PostConstruct
    public void init(){
        LOG.warn("init AISessionManager");
        this.listOfSessionsByUser = new HashMap<String, HashMap<String, List<MessageParam>>>();
    }


    private HashMap<String, HashMap<String, List<MessageParam>>> listOfSessionsByUser;


    public void addMessage(String user, String session, MessageParam messageParam) {
        this.listOfSessionsByUser.putIfAbsent(user, new HashMap<>());

        Map<String, List<MessageParam>> sessions = this.listOfSessionsByUser.get(user);
        sessions.putIfAbsent(session, new ArrayList<MessageParam>());

        sessions.get(session).add(messageParam);
    }

    public List<MessageParam> getAllMessages(String user, String session){
        if (this.listOfSessionsByUser.containsKey(user) && this.listOfSessionsByUser.get(user).containsKey(session)) {
            return this.listOfSessionsByUser.get(user).get(session);
        }
        return null;
    }

}
