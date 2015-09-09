/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service;

import javax.servlet.http.HttpServletRequest;

import org.cerberus.entity.LogEvent;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ILogEventService {

    /**
     *
     * @param logEventID
     * @return
     */
    AnswerItem readByKey(long logEventID);

    /**
     *
     * @param start
     * @param amount
     * @param colName
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param logevent
     * @return
     */
    Answer create(LogEvent logevent);

    /**
     * This method is to be used when log is done from a public Servlet. It will
     * automatically check if the log of Public API Calls is activated before
     * recording the log
     *
     * @param page
     * @param action
     * @param request
     * @param log
     */
    void createPublicCalls(String page, String action, String log, HttpServletRequest request);
}
