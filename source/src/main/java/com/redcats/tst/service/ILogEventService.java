/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.LogEvent;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author vertigo
 */
public interface ILogEventService {

    public List<LogEvent> findAllLogEvent() throws CerberusException;

    public List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir) throws CerberusException;

    public Integer getNumberOfLogEvent() throws CerberusException;

    public boolean insertLogEvent(LogEvent logevent) throws CerberusException;
}
