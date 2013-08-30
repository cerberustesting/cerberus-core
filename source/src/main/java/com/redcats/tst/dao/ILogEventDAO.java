package com.redcats.tst.dao;

import com.redcats.tst.entity.LogEvent;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author vertigo
 */
public interface ILogEventDAO {

    /**
     * @return a list of all LogEvent.
     * @throws CerberusException in case no LogEvent can be found.
     */
    List<LogEvent> findAllLogEvent() throws CerberusException;

    /**
     * @return a list of all LogEvent.
     * @throws CerberusException in case no LogEvent can be found.
     */
    List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir) throws CerberusException;

    /**
     *
     * @return Total number of LogEvent inside the database.
     * @throws CerberusException
     */
    Integer getNumberOfLogEvent() throws CerberusException;

    /**
     * Insert user into the database.
     *
     * @param logevent
     * @return true is log was inserted
     * @throws CerberusException if we did not manage to insert the user.
     */
    boolean insertLogEvent(LogEvent logevent) throws CerberusException;
}
