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
package org.cerberus.core.crud.dao;

import org.cerberus.core.crud.entity.LogAIUsage;
import org.cerberus.core.crud.entity.LogAIUsageStats;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bcivel
 */
public interface ILogAIUsageDAO {

    Answer create(LogAIUsage logAIUsage);

    AnswerItem<LogAIUsage> readByKey(Integer id);

    AnswerList<LogAIUsage> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    LogAIUsage loadFromResultSet(ResultSet resultSet) throws SQLException;

    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    AnswerItem<LogAIUsageStats> readSumByPeriod(Timestamp startDate, Timestamp endDate, String user);

    AnswerList<LogAIUsageStats> readUsageByDay(Timestamp startDate, Timestamp endDate, String user);
}
