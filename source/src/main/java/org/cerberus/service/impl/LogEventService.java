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

import org.cerberus.dao.ILogEventDAO;
import org.cerberus.entity.LogEvent;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ILogEventService;
import java.util.List;
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
    
    @Override
    public List<LogEvent> findAllLogEvent() throws CerberusException {
        return logEventDAO.findAllLogEvent();
    }

    @Override
    public List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir) throws CerberusException {
        return logEventDAO.findAllLogEvent(start, amount, colName, dir);
    }

    @Override
    public Integer getNumberOfLogEvent() throws CerberusException {
        return logEventDAO.getNumberOfLogEvent();
    }

    
    @Override
    public boolean insertLogEvent(LogEvent logevent) throws CerberusException {
        return logEventDAO.insertLogEvent(logevent);
    }
    
    
    
}
