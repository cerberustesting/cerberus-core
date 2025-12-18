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
import org.cerberus.core.crud.dao.IUserPromptDAO;
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.stats.UserPromptStats;
import org.cerberus.core.crud.service.IUserPromptService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bcivel
 */
@Service
public class UserPromptService implements IUserPromptService {

    private static final Logger LOG = LogManager.getLogger(UserPromptService.class);

    @Autowired
    private IUserPromptDAO userPromptDAO;

    @Override
    public AnswerItem<UserPrompt> readByKey(Integer id) {
        return userPromptDAO.readByKey(id);
    }

    @Override
    public AnswerList<UserPrompt> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return userPromptDAO.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(UserPrompt userPrompt) {
        return userPromptDAO.create(userPrompt);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return userPromptDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerItem<UserPromptStats> readSumByPeriod(Timestamp startDate, Timestamp endDate, String user) {
        LOG.debug("Fetching AI usage statistics from {} to {}, for user {}", startDate, endDate, user);
        return userPromptDAO.readSumByPeriod(startDate, endDate, user);
    }

    @Override
    public AnswerList<UserPromptStats> getUsageByDay(Timestamp startDate, Timestamp endDate, String user) {
        return userPromptDAO.readUsageByDay(startDate, endDate, user);
    }

    @Override
    public boolean incrementUsage(String user, String aiSessionId, Integer inputTokens, Integer outputTokens, Double cost){
        return userPromptDAO.incrementUsage(user, aiSessionId, inputTokens, outputTokens, cost);
    }

    @Override
    public AnswerItem<UserPromptStats> getUserPromptStats(String fromDate, String toDate, String user) {
        return userPromptDAO.readStats(fromDate, toDate, user);
    }

}
