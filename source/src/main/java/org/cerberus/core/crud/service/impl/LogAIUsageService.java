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
import org.cerberus.core.crud.dao.ILogAIUsageDAO;
import org.cerberus.core.crud.entity.LogAIUsage;
import org.cerberus.core.crud.entity.LogAIUsageStats;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogAIUsageService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bcivel
 */
@Service
public class LogAIUsageService implements ILogAIUsageService {

    private static final Logger LOG = LogManager.getLogger(LogAIUsageService.class);

    @Autowired
    private ILogAIUsageDAO logAIUsageDAO;

    @Override
    public AnswerItem<LogAIUsage> readByKey(Integer ID) {
        return logAIUsageDAO.readByKey(ID);
    }

    @Override
    public AnswerList<LogAIUsage> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return logAIUsageDAO.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(LogAIUsage logAIUsage) {
        return logAIUsageDAO.create(logAIUsage);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return logAIUsageDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerItem<LogAIUsageStats> readSumByPeriod(Timestamp startDate, Timestamp endDate, String user) {
        LOG.debug("Fetching AI usage statistics from {} to {}, for user {}", startDate, endDate, user);
        return logAIUsageDAO.readSumByPeriod(startDate, endDate, user);
    }

    @Override
    public AnswerList<LogAIUsageStats> getUsageByDay(Timestamp startDate, Timestamp endDate, String user) {
        return logAIUsageDAO.readUsageByDay(startDate, endDate, user);
    }
}
