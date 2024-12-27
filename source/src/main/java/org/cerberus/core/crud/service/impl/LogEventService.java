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

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.core.crud.dao.ILogEventDAO;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo
 */
@Service
public class LogEventService implements ILogEventService {

    @Autowired
    private ILogEventDAO logEventDAO;
    @Autowired
    private IFactoryLogEvent factoryLogEvent;
    @Autowired
    private IParameterService parameterService;

    @Override
    public AnswerItem<LogEvent> readByKey(long logEventID) {
        return logEventDAO.readByKey(logEventID);
    }

    @Override
    public AnswerList<LogEvent> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return logEventDAO.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(LogEvent logevent) {
        return logEventDAO.create(logevent);
    }

    @Override
    public void createForPrivateCalls(String page, String action, String status, String log, HttpServletRequest request) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        String myUser = "";
        String remoteIP = "";
        String localIP = "";
        if (request != null) {
            remoteIP = request.getRemoteAddr();
            if (request.getHeader("x-forwarded-for") != null) {
                remoteIP = request.getHeader("x-forwarded-for");
            }
            if (!(request.getUserPrincipal() == null)) {
                myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
            }
            localIP = request.getLocalAddr();
        }
        this.create(factoryLogEvent.create(0, 0, myUser, null, page, action, status, log, remoteIP, localIP));
    }

    @Override
    public void createForPrivateCalls(String page, String action, String status, String log) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        this.create(factoryLogEvent.create(0, 0, "", null, page, action, status, log, null, null));
    }

    @Override
    public void createForPublicCalls(String page, String action, String status, String log, HttpServletRequest request) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.

        if (parameterService.getParameterBooleanByKey("cerberus_log_publiccalls", "", false)) { // The parameter cerberus_log_publiccalls is activated so we log all Public API calls.
            String myUser = "";
            if (!(request.getUserPrincipal() == null)) {
                myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
            }
            this.create(factoryLogEvent.create(0, 0, myUser, null, page, action, status, log, request.getRemoteAddr(), request.getLocalAddr()));
        }
    }

    @Override
    public void createForPublicCalls(String page, String action, String status, String log, HttpServletRequest request, String login) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.

        if (parameterService.getParameterBooleanByKey("cerberus_log_publiccalls", "", false)) { // The parameter cerberus_log_publiccalls is activated so we log all Public API calls.
            if (!(request.getUserPrincipal() == null)) {
                login = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
            }
            this.create(factoryLogEvent.create(0, 0, login, null, page, action, status, log, request.getRemoteAddr(), request.getLocalAddr()));
        }
    }
    
    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return logEventDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

}
