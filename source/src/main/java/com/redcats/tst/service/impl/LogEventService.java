/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ILogEventDAO;
import com.redcats.tst.entity.LogEvent;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ILogEventService;
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
