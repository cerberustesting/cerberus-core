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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;

import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo
 */
@Service
public class FactoryLogEvent implements IFactoryLogEvent {
    
    @Override
    public LogEvent create(long logEventID, long userID, String login, Timestamp time, String page, String action, String status, String log, String remoteIP, String localIP) {
        LogEvent newLogEvent = new LogEvent();
        newLogEvent.setLogEventID(logEventID);
        newLogEvent.setUserID(userID);
        newLogEvent.setLogin(login);
        newLogEvent.setTime(time);
        newLogEvent.setPage(page);
        newLogEvent.setAction(action);
        newLogEvent.setStatus(status);
        newLogEvent.setLog(log);
        newLogEvent.setRemoteIP(remoteIP);
        newLogEvent.setLocalIP(localIP);
        return newLogEvent;
    }

}
