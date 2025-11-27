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
package org.cerberus.core.crud.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IUserPromptMessageDAO;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.crud.service.IUserPromptMessageService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class UserPromptMessageService implements IUserPromptMessageService {

    private static final Logger LOG = LogManager.getLogger(UserPromptMessageService.class);

    @Autowired
    private IUserPromptMessageDAO userPromptMessageDAO;

    @Override
    public AnswerItem<UserPromptMessage> readByKey(Integer id) {
        return userPromptMessageDAO.readByKey(id);
    }

    @Override
    public AnswerList<UserPromptMessage> readBySessionId(String sessionId) {
        return userPromptMessageDAO.readBySessionID(sessionId);
    }

    @Override
    public Answer create(UserPromptMessage userPromptMessage) {
        return userPromptMessageDAO.create(userPromptMessage);
    }

}
