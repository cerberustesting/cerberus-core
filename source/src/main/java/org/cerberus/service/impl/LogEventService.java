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
package org.cerberus.service.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.cerberus.dao.ILogEventDAO;
import org.cerberus.entity.LogEvent;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IParameterService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerList;
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
    public List<LogEvent> readAll_Deprecated() throws CerberusException {
        return logEventDAO.readAll_Deprecated();
    }

    @Override
    public AnswerList readByCriteria_Deprecated(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) throws CerberusException {
        return logEventDAO.readByCriteria_Deprecated(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public boolean create_Deprecated(LogEvent logevent) throws CerberusException {
        return logEventDAO.create_Deprecated(logevent);
    }

    @Override
    public void create_Deprecated(String page, String action, String log, HttpServletRequest request) {
        String myUser = "";
        if (!(request.getUserPrincipal() == null)) {
            myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
        }
        try {
            this.create_Deprecated(factoryLogEvent.create(0, 0, myUser, null, page, action, log, request.getRemoteAddr(), request.getLocalAddr()));
        } catch (CerberusException ex) {
            org.apache.log4j.Logger.getLogger(LogEventService.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
        }
    }

    @Override
    public void createPublicCalls_Deprecated(String page, String action, String log, HttpServletRequest request) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        String doit = "";
        try {
            doit = parameterService.findParameterByKey("cerberus_log_publiccalls", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(LogEventService.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (doit.equalsIgnoreCase("Y")) {
            this.create_Deprecated(page, action, log, request);
        }
    }

    @Override
    public Integer getNumberOfLogEvent(String searchTerm) throws CerberusException {
        return logEventDAO.getNumberOfLogEvent(searchTerm);
    }

}
