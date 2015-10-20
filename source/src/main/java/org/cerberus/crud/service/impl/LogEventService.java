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
package org.cerberus.crud.service.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.crud.dao.ILogEventDAO;
import org.cerberus.crud.entity.LogEvent;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.factory.IFactoryLogEvent;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
    public AnswerItem readByKey(long logEventID) {
        return logEventDAO.readByKey(logEventID);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        return logEventDAO.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(LogEvent logevent) {
        return logEventDAO.create(logevent);
    }

    @Override
    public void createPrivateCalls(String page, String action, String log, HttpServletRequest request) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        String myUser = "";
        if (!(request.getUserPrincipal() == null)) {
            myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
        }
        this.create(factoryLogEvent.create(0, 0, myUser, null, page, action, log, request.getRemoteAddr(), request.getLocalAddr()));
    }

    @Override
    public void createPrivateCalls(String page, String action, String log) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        this.create(factoryLogEvent.create(0, 0, "", null, page, action, log, null, null));
    }

    @Override
    public void createPublicCalls(String page, String action, String log, HttpServletRequest request) {
        // Only log if cerberus_log_publiccalls parameter is equal to Y.
        String doit = "";
        try {
            Parameter p = parameterService.findParameterByKey("cerberus_log_publiccalls", "");
            //p can be null if the DAO fail to obain the parameter
            if (p != null) {
                doit = p.getValue();
            } else {
                //if some exception occurrs while accessing the parameter then we will activate the logs in order to 
                //Check if there are more problems
                doit = "Y";
                Logger.getLogger(LogEventService.class.getName()).log(Level.WARNING, "Parameter is not available: cerberus_log_publiccalls", "");
            }
            
        } catch (CerberusException ex) {
            Logger.getLogger(LogEventService.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (doit.equalsIgnoreCase("Y")) { // The parameter cerberus_log_publiccalls is activated so we log all Public API calls.
            String myUser = "";
            if (!(request.getUserPrincipal() == null)) {
                myUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
            }
            this.create(factoryLogEvent.create(0, 0, myUser, null, page, action, log, request.getRemoteAddr(), request.getLocalAddr()));
        }
    }

}
