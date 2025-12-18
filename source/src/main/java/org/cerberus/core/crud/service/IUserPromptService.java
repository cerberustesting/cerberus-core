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
package org.cerberus.core.crud.service;

import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.stats.UserPromptStats;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bcivel
 */
public interface IUserPromptService {

    AnswerItem<UserPrompt> readByKey(Integer id);

    AnswerList<UserPrompt> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    Answer create(UserPrompt userPrompt);

    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    AnswerItem<UserPromptStats> readSumByPeriod(Timestamp startDate, Timestamp endDate, String user);

    AnswerList<UserPromptStats> getUsageByDay(Timestamp startDate, Timestamp endDate, String user);

    boolean incrementUsage(String user, String aiSessionId, Integer inputTokens, Integer outputTokens, Double cost);

    AnswerItem<UserPromptStats>  getUserPromptStats(String fromDate, String toDate, String user);
}
