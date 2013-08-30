/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.LogEvent;
import com.redcats.tst.factory.IFactoryLogEvent;
import java.sql.Timestamp;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo
 */
@Service
public class FactoryLogEvent implements IFactoryLogEvent {
    
    @Override
    public LogEvent create(long logEventID, long userID, String login, Timestamp time, String page, String action, String log, String remoteIP, String localIP) {
        LogEvent newLogEvent = new LogEvent();
        newLogEvent.setLogEventID(logEventID);
        newLogEvent.setUserID(userID);
        newLogEvent.setLogin(login);
        newLogEvent.setTime(time);
        newLogEvent.setPage(page);
        newLogEvent.setAction(action);
        newLogEvent.setLog(log);
        newLogEvent.setremoteIP(remoteIP);
        newLogEvent.setLocalIP(localIP);
        return newLogEvent;
    }

}
