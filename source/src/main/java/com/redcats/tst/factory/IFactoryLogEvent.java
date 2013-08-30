/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.LogEvent;
import java.sql.Timestamp;

/**
 *
 * @author vertigo
 */
public interface IFactoryLogEvent {

    LogEvent create(long logEventID, long userID, String login, Timestamp time, String page, String action, String log, String remoteIP, String localIP);
}
